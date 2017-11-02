import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by shobhitgarg on 9/26/17.
 */
public class QIFramework {
    GraphDatabaseService [] databaseServices;
    FeatureNode root;
    Feature queryGraph;
    GraphDatabaseService databaseServiceSeq;
    ArrayList<Integer> soln;
    HashSet<Integer> graphIDs = new HashSet<>();
    static String tempPath = "/Users/shobhitgarg/Documents/tempGraphs/";
    static long count = 1;
    static FeatureNode pre;
    public QIFramework(Feature feature, FeatureNode root, GraphDatabaseService[] databaseServices, ArrayList<Integer> soln) throws IOException {
        this.queryGraph = feature;
        this.root = root;
        this.databaseServices = databaseServices;
        this.soln = soln;
        HashSet<Integer> graphIDs = filtering();
        if(graphIDs.size() == 0)
            return;
        //  create a qi sequence

        File file1 = new File(tempPath + "feature" + count++);
        if(count - 1 == 29)
            System.out.println("Issue 29");
        if((count -1 ) % 10 == 0)
            System.out.println("need break");
        BatchInserter inserter = BatchInserters.inserter(file1);
        // for each node in the feature
        int index = 0;
        HashMap<Long, Long> nodeMap = new HashMap<>();
        for (Long nodeID : feature.nodes) {
            HashMap<String, Object> inserterMap = new HashMap<>();
            HashSet<Label> labels = feature.labels.get(nodeID);
            ArrayList<Label>lbSet = new ArrayList<>();
            for (Label l : labels)
                lbSet.add(l);
            inserterMap.put("labels", lbSet.toString());
            inserterMap.put("id", index++);
            nodeMap.put(nodeID, inserter.createNode(inserterMap, lbSet.toArray(new Label[lbSet.size()])));

        }
        for (Long edge1 : feature.edges.keySet()) {
            try {
                for(Long edge2: feature.edges.get(edge1))
                    inserter.createRelationship(nodeMap.get(edge1), nodeMap.get(edge2), RelationshipType.withName(""), null);
            }
            catch (Exception e) {
                System.out.println("Exception! + edge: " + edge1 );
            }
        }
        inserter.shutdown();

        GraphDatabaseService featureService = new GraphDatabaseFactory().newEmbeddedDatabase(file1);

        CalculateAvgInnerSupport avgInnerSupport = new CalculateAvgInnerSupport();
        avgInnerSupport.calcInnerSupportEdge(featureService, databaseServices);
        avgInnerSupport.calcInnerSupportVertex(featureService, databaseServices);
        CalcMST calcMST = new CalcMST();
        databaseServiceSeq = calcMST.calculateMST1(featureService, tempPath + "weightedFeature" + (count - 1));
//        if(databaseServiceSeq == null) // need to check this thingy!
//            return;


    }

    HashSet<Integer>  filtering()    {

        for(FeatureNode child : root.children)  {
            HashSet<Long> F =new HashSet<>();
            HashMap<FeatureNode, Long> H = new HashMap<>();
            prefixQuickSI(child, queryGraph, H, F, 1);


        }
        return graphIDs;
    }

    void prefixQuickSI(FeatureNode n, Feature q,HashMap<FeatureNode, Long> H, HashSet<Long> F, int d)    {
        if(n == null)
            return;
        ArrayList<Long> V = new ArrayList<>();
        if(d == 1)  {
            for( Long nodeID: q.labels.keySet())    {
                if(q.labels.get(nodeID).containsAll(n.labels))
                    V.add(nodeID);

            }
        }
        else    {
            // if d > 1
            for(Long nodeID: q.labels.keySet()) {
                try {
                    long l1 = H.get(pre);
                    if (q.labels.get(nodeID).containsAll(n.labels) && ((q.edges.containsKey(l1) && q.edges.get(l1).contains(nodeID))  || q.edges.containsKey(nodeID) && q.edges.get(nodeID).contains(l1)) && !F.contains(nodeID))
                        V.add(nodeID);
                }
                catch (Exception e) {
                    System.out.println("Exception!");
                }

            }

        }

        if(V.size()==0)
            return;
        for(Long v: V)  {
            if(d == 1)
                graphIDs = n.graphIDs;
            else
                graphIDs.retainAll(n.graphIDs);
            if(n.children.size() == 0)
                return;
            H.put(n, v);
            F.add(v);
            for(FeatureNode child: n.children)  {
                FeatureNode oldNode = pre;
                pre = n;
                prefixQuickSI(child, q, H, F, d + 1);
                pre = oldNode;
            }
            F.remove(v);

        }
        return;
    }

}
