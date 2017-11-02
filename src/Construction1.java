import com.sun.org.apache.bcel.internal.generic.LADD;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by shobhitgarg on 10/8/17.
 */
public class Construction1 {

    HashSet<Feature> features;
    String newDbPath = "/Users/shobhitgarg/Documents/BidirectionalGraphs";
    GraphDatabaseService[] graphDatabaseServices;
    BatchInserter swiftIndexInserter;
    FeatureNode index;
    static String tempPath = "/Users/shobhitgarg/Documents/tempGraphs/";
    static long count = 1;
    static long siCount = 0;
    public Construction1(GraphDatabaseService[] graphDatabaseServices)   {
        this.graphDatabaseServices = graphDatabaseServices;
//    try {
//        setUpDatabase();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }

    }

    public static void main(String[] args) throws IOException {
        String graphFilePath = "/Users/shobhitgarg/Documents/GraphDB5/";
        String targetSet []= new String[]{"backbones_1O54.grf", "backbones_1UOW.grf", "backbones_2AXQ.grf", "saccharomyces_cerevisiae_1ZTA.grf"};
        GraphDatabaseService [] databaseService = new GraphDatabaseService[4];
        for(int i = 0; i<databaseService.length; i++)   {
            databaseService[i]  = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphFilePath + targetSet[i]));
        }
        Construction1 obj = new Construction1(databaseService);
        obj.createIndex();
        obj.swiftIndexInserter.shutdown();
    }

    void setUpDatabase() throws IOException {
        File file1 = new File(newDbPath + "/SwiftIndex");
        swiftIndexInserter = BatchInserters.inserter(file1);
    }

    HashSet<Label>  getLabels(Iterable<Label> labels)  {
        HashSet<Label> lbSet = new HashSet<>();
        for (Label l : labels)
            lbSet.add(l);
        return lbSet;
    }

    FeatureNode getChildFeatureNodeWithLabels(FeatureNode root ,HashSet<Label>labels)   {
        for(FeatureNode f: root.children)   {
            if(f.labels.containsAll(labels))
                return f;
        }
        return null;
    }

    void createIndex() throws IOException {
        FeatureNode root = new FeatureNode();
        root.id = siCount++;
        root.isRoot = true; root.labels.add(Label.label("root"));
// for each feature size
        int maxSize = 4;
        for(int size =1; size<= maxSize; size ++) {
// for each graph database
         //   for (int i = 0; i<graphDatabaseServices.length; i++) {
            HashMap<String, Feature> featureSoln = Utilities.findFeatures(graphDatabaseServices, size);

                    // if size is 1, just add the node, if the size is greater than 1, then iterate through the existing swift index and then do something
                if(size <= 2) {
                    if (size == 1) {

                            for (String pattern : featureSoln.keySet()) {
                                Feature feature = featureSoln.get(pattern);

                           // Node node = databaseService.getNodeById(feature.nodes.get(0)); // since it contains only one node
                                HashSet<Label> labels = feature.labels.get(feature.nodes.get(0));
                            FeatureNode fnode = getChildFeatureNodeWithLabels(root, labels);
                            if(fnode == null) {
//                            inserterMap.put("id", siCount++);
//                            inserterMap.put("labels", lbSet.toString());
//                            inserterMap.put("graphID", feature.graphID);
//                            inserterMap.put("isFeature", true);
                                FeatureNode newNode = new FeatureNode();
                                newNode.labels = labels;
                                newNode.isFeature = true;
                                newNode.id = siCount++;
                                newNode.graphIDs.addAll(feature.graphIDs);
                                root.children.add(newNode);
                            }
                            else {
                                fnode.graphIDs.addAll(feature.graphIDs);
                            }
//                            long newNodeID = swiftIndexInserter.createNode(inserterMap,lbSet.toArray(new Label[lbSet.size()]) );
//                            swiftIndexInserter.createRelationship(rootNodeID, newNodeID, RelationshipType.withName("Parent"), null);
                        }

                    }
                    //size == 2
                    else {
                        for (String pattern : featureSoln.keySet()) {
                            Feature feature = featureSoln.get(pattern);
                                HashSet<Label> labels = feature.labels.get(feature.nodes.get(0));
                                HashSet<Label> labels1 = feature.labels.get(feature.nodes.get(1));
                            FeatureNode fnode = getChildFeatureNodeWithLabels(root, labels);
                            if(fnode == null)   {
                                System.err.println("This cannot/shouldn't happen");
                            }
                            else {
                                fnode.graphIDs.addAll(feature.graphIDs);
                            }
                            FeatureNode fnode1 = getChildFeatureNodeWithLabels(fnode, labels1);
                            if(fnode1 == null) {
                                FeatureNode newNode = new FeatureNode();
                                newNode.labels = getLabels(labels1);
                                newNode.id = siCount++;
                                newNode.isFeature = true;
                                newNode.graphIDs.addAll(feature.graphIDs);
                                fnode.children.add(newNode);
                            }
                            else
                                fnode1.graphIDs.addAll(feature.graphIDs);

                        }

                    } // size == 2
                } // if size <=2
                // if size is greater than 2
                else{
                        // for each feature
                    for (String pattern : featureSoln.keySet()) {
                        Feature feature = featureSoln.get(pattern);
                            // initial prep for qi framework
                            ArrayList<Integer> soln = new ArrayList<>();
                            QIFramework qiFramework = new QIFramework(feature, root, graphDatabaseServices, soln );
                            if(qiFramework.databaseServiceSeq!= null)   {
                                // if size == 3
                                GraphDatabaseService databaseService = qiFramework.databaseServiceSeq;
                                try(Transaction tx = databaseService.beginTx()) {
                                    Node n1 = databaseService.getNodeById(0);
                                    Node n2 = databaseService.getNodeById(1);
                                    Node n3 = databaseService.getNodeById(2);
                                    HashSet<Label> labels1 = getLabels(n1.getLabels());
                                    HashSet<Label> labels2 = getLabels(n2.getLabels());
                                    HashSet<Label> labels3 = getLabels(n3.getLabels());
                                    FeatureNode fnode = getChildFeatureNodeWithLabels(root, labels1);
                                    if(fnode == null)
                                        System.out.println("This cannot happen");
                                    fnode = getChildFeatureNodeWithLabels(fnode, labels2);
                                    if(fnode == null)
                                        System.out.println("This cannot happen1");
                                    if(size == 3)   {
                                       FeatureNode fnode1 = getChildFeatureNodeWithLabels(fnode, labels3);
                                        if(fnode1 == null)   {
                                            FeatureNode newNode = new FeatureNode();
                                            newNode.labels = getLabels(labels3);
                                            newNode.id = siCount++;
                                            newNode.isFeature = true;
                                            newNode.graphIDs.addAll(feature.graphIDs);
                                            fnode.children.add(newNode);
                                        }
                                        else
                                            fnode1.graphIDs.addAll(feature.graphIDs);
                                    }
                                    else {
                                        fnode = getChildFeatureNodeWithLabels(fnode, labels3);
                                        if(fnode == null)
                                            System.out.println("This cannot happen 2");
                                        Node n4 = databaseService.getNodeById(3);
                                        HashSet<Label> labels4 = getLabels(n4.getLabels());

                                        FeatureNode fnode1 = getChildFeatureNodeWithLabels(fnode, labels4);
                                        if(fnode1 == null)  {
                                            FeatureNode newNode = new FeatureNode();
                                            newNode.labels = getLabels(labels4);
                                            newNode.id = siCount++;
                                            newNode.isFeature = true;
                                            newNode.graphIDs.addAll(feature.graphIDs);
                                            fnode.children.add(newNode);
                                        }
                                        else
                                            fnode1.graphIDs.addAll(feature.graphIDs);
                                    }

                                    tx.success();
                                    tx.close();
                                }

                            }


                        } // for each feature


                }

         //   } // for each graph db

        }
        System.out.println("Checkk");
    }

}
