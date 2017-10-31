import org.neo4j.cypher.internal.compiler.v2_3.No;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import scala.Array;
import scala.collection.Seq;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by shobhitgarg on 9/15/17.
 */
public class CalcMSTOfSubgraph {
    HashMap<Integer, Vertex> queryGraphNodes;
    HashMap<Integer, Vertex> queryGraphNodes1;
    ArrayList<Edge> edgeList;
    static int index = 0;
    ArrayList<Relationship> P = new ArrayList<>();
    ArrayList<Relationship> et = new ArrayList<>();
    HashSet<Relationship> removed = new HashSet<>();
    LinkedHashSet<Relationship> seq = new LinkedHashSet<>();
    //LinkedHashMap<Node,SequenceNode> seq = new LinkedHashMap<>();
    //LinkedHashSet<Node> seq = new LinkedHashSet<>();
    HashMap<Integer, Long> map = new HashMap<>();
    ArrayList<Relationship> rij = new ArrayList<>();
    HashMap<Integer,ArrayList<Edge>> sequenceEdges = new HashMap<>();
    ArrayList<Sequence> sequence = new ArrayList<>();
    public CalcMSTOfSubgraph(HashMap<Integer, Vertex> queryGraphNodes, ArrayList<Edge> edgeList)   {
        this.queryGraphNodes = queryGraphNodes;
        queryGraphNodes1 = new HashMap<>(this.queryGraphNodes);
        this.edgeList = new ArrayList<>(edgeList);
    }


//    public Feature calculateMST1(Feature feature) throws IOException {
//        if(!newDbPath.endsWith("/"))
//            newDbPath += "/";
//        GraphDatabaseService databaseServiceSeq =  null;
//        File file1 = new File(newDbPath + "QISeq");
//        BatchInserter inserter = BatchInserters.inserter(file1);
//        ArrayList<Relationship> extraEdges = new ArrayList<>();
//        HashMap<Relationship, ArrayList<Relationship>> seqEdges = new HashMap<>();
//        HashSet<Node> vt = new HashSet<>();
//        try (Transaction tx = databaseServiceQuery.beginTx()) {
//            // find edge with min weight
//            P = Utilities.findEdgesWithMinWeight(databaseServiceQuery);
//            // finding the number of nodes
//            long noOfNodes = Utilities.countNodesInGraph(databaseServiceQuery);
//            Result result;
//            Map<String, Object> row;
//            // selecting the first edge
//            Relationship e = selectFirstEdge1(P);
//            et.add(e);
//            map.put((int) e.getStartNode().getProperty("id"), Utilities.insertNode(inserter, e.getStartNode(), new Long(-1), index ++));
//            map.put((int) e.getEndNode().getProperty("id"), Utilities.insertNode(inserter, e.getEndNode(), map.get((int)e.getStartNode().getProperty("id")), index ++));
//            inserter.createRelationship(map.get((int)e.getStartNode().getProperty("id")), map.get((int)e.getEndNode().getProperty("id")), RelationshipType.withName("Parent"), null);
//            vt.add(e.getStartNode());
//            vt.add(e.getEndNode());
//            e.delete();
//            removed.add(e);
//
//            while (vt.size() != noOfNodes) {
//                P = new ArrayList<>();
//                StringBuilder nodeIDsInVt = new StringBuilder("[");
//                for (Node n : vt)
//                    nodeIDsInVt.append(n.getId() + ",");
//                nodeIDsInVt.setLength(nodeIDsInVt.length() - 1);
//                nodeIDsInVt.append("]");
//
//                result = databaseServiceQuery.execute("match(n)-[r1]->(n1) where n.id in " + nodeIDsInVt.toString() + " and not n1.id in " + nodeIDsInVt.toString() + " return r1");
//                while (result.hasNext()) {
//                    row = result.next();
//                    Relationship edge = (Relationship) row.get("r1");
//                    if (!removed.contains(edge)) {
//                        P.add(edge);
//                    }
//                }
//
//                e = selectSpanningEdge1(P, vt, databaseServiceQuery);
//                et.add(e);
//                map.put((int) e.getEndNode().getProperty("id"), Utilities.insertNode(inserter, e.getEndNode(), map.get((int)e.getStartNode().getProperty("id")), index ++));
//                inserter.createRelationship(map.get((int)e.getStartNode().getProperty("id")), map.get((int)e.getEndNode().getProperty("id")), RelationshipType.withName("Parent"), null);
//
//                vt.add(e.getEndNode());
//
//                // checking for edges that are that have both nodes in vt
//                nodeIDsInVt = new StringBuilder("[");
//                for (Node n : vt)
//                    nodeIDsInVt.append(n.getProperty("id") + ",");
//                nodeIDsInVt.setLength(nodeIDsInVt.length() - 1);
//                nodeIDsInVt.append("]");
//
//                result = databaseServiceQuery.execute("match(n)-[r1]->(n1) where n.id in " + nodeIDsInVt.toString() + " and n1.id in " + nodeIDsInVt.toString() + " return distinct(r1)");
//                ArrayList<Relationship> tempP = new ArrayList<>();
//                while (result.hasNext()) {
//                    row = result.next();
//                    Relationship edge = (Relationship) row.get("r1");
//                    tempP.add(edge);
//                }
//                Collections.sort(tempP, (o1, o2) -> (double) o1.getProperty("Weight") - (double) o2.getProperty("Weight") > 0 ? 1 : (double) o1.getProperty("Weight") - (double) o2.getProperty("Weight") < 0 ? -1 : 0);
//                for (Relationship rel : tempP) {
//                    extraEdges.add(rel);
//                    rel.delete();
//                }
//            }
//
//
//            inserter.shutdown();
//
//
//
//            // adding the extra edges in the qI Sequence
//            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
//            databaseServiceSeq = dbFactory.newEmbeddedDatabase(file1);
//            Map<Long, HashSet<Long>> extraEdgeMap = new HashMap<>();
//            try (Transaction tx1 = databaseServiceSeq.beginTx()){
//                for(Relationship rel: extraEdges)   {
//                    long startID = map.get((int) rel.getStartNode().getProperty("id"));
//                    long endID =  map.get((int) rel.getEndNode().getProperty("id"));
//
//                    // doing this to ensure that there is a vertex available when checking for extra edges in QuickSI algorithm,
//                    if(startID<endID)   {
//                        long temp = startID;
//                        startID = endID;
//                        endID = temp;
//                    }
//                    Node startNode = databaseServiceSeq.getNodeById(startID);
//                    Node endNode = databaseServiceSeq.getNodeById(endID);
//                    // keeping the actual node id's in the extraEdge map, as it will help in querying the graph quickly.
//                    if(extraEdgeMap.containsKey(startNode.getId())) {
//                        extraEdgeMap.get(startNode.getId()).add(endNode.getId());
//                    }
//                    else    {
//                        extraEdgeMap.put(startNode.getId(), new HashSet<>());
//                        extraEdgeMap.get(startNode.getId()).add(endNode.getId());
//                    }
//
//                }
//                for(Long key: extraEdgeMap.keySet()) {
//                    Node node = databaseServiceSeq.getNodeById(key);
//                    node.setProperty("Extra edge", extraEdgeMap.get(key).toString());
//                }
//
//
//                tx1.success();
//                tx1.close();
//            }
//            catch (Exception e1) {
//                System.out.println(e.toString());
//            }
//
//
//            tx.success();
//            tx.close();
//        }
//        catch (Exception e) {
//            System.out.println(e.toString());
//        }
//        return databaseServiceSeq;
//    }


    Relationship selectFirstEdge1(ArrayList<Relationship>P)  {
        if(P.size() == 1)
            return P.get(0);

        Relationship ans = P.get(0);
        for(Relationship e: P)  {
            if((ans.getStartNode().getDegree(Direction.OUTGOING) + ans.getEndNode().getDegree(Direction.OUTGOING))>(e.getStartNode().getDegree(Direction.OUTGOING) + e.getEndNode().getDegree(Direction.OUTGOING)))
                ans = e;
        }
        return ans;
    }

    Relationship selectSpanningEdge1(ArrayList<Relationship>P, HashSet<Node> vt, GraphDatabaseService databaseServiceQuery)  {
        Collections.sort(P, (o1, o2) -> (double)o1.getProperty("Weight") - (double)o2.getProperty("Weight")>0? 1 : (double)o1.getProperty("Weight") - (double)o2.getProperty("Weight") <0 ?-1: 0 );
        double minWeight = (double)P.get(0).getProperty("Weight");
        ArrayList<Relationship> tempP = new ArrayList<>();
        for(Relationship edge: P)   {
            if((double)edge.getProperty("Weight")>minWeight)
                break;
            tempP.add(edge);
        }
        P = tempP;
        if(P.size() == 1)
            return P.get(0);
        tempP = new ArrayList<>();
        ArrayList<Node> tempVt = new ArrayList<>(vt);
        long maxInducedSubgraphSize = 0;
        ArrayList<Long> inducedSizes = new ArrayList<>();


        HashSet<Long> nodeIDset = new HashSet<>();
        for (Node n: vt)
            nodeIDset.add(n.getId());

        for(Relationship rel : P)   {
            tempVt.add(rel.getEndNode());
            nodeIDset.add(rel.getEndNode().getId());
            long currInducedSubgraphSize = 0;
            for(Node node: tempVt)  {
                nodeIDset.remove(node.getId());
                Result result = databaseServiceQuery.execute("match (n) -[r]-> (n1) where ID(n) = " + node.getId() + " and ID(n1) IN " + nodeIDset.toString() + " return count(r)");
                Map<String, Object> row = result.next();
                currInducedSubgraphSize += (long)row.get("count(r)");
                nodeIDset.add(node.getId());
            }
            inducedSizes.add(currInducedSubgraphSize);

            if(currInducedSubgraphSize>maxInducedSubgraphSize)
                maxInducedSubgraphSize = currInducedSubgraphSize;
            tempVt.remove(tempVt.size() - 1);

        }

        for(int i = 0; i<inducedSizes.size(); i++)  {
            if(inducedSizes.get(i)== maxInducedSubgraphSize)
                tempP.add(P.get(i));
        }
        P = tempP;
        if(P.size() == 1)
            return P.get(0);

        int minDegree = Integer.MAX_VALUE;
        Relationship e = null;
        for(Relationship rel: P)    {
            if(rel.getEndNode().getDegree(Direction.OUTGOING)< minDegree)   {
                minDegree = rel.getEndNode().getDegree(Direction.OUTGOING);
                e = rel;
            }
        }
        return e;

    }
}
