//import org.neo4j.graphdb.*;
//import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.unsafe.batchinsert.BatchInserter;
//import org.neo4j.unsafe.batchinsert.BatchInserters;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//
///**
// * Created by shobhitgarg on 10/8/17.
// */
//public class Construction {
//
//HashSet<Feature> features;
//String newDbPath = "/Users/shobhitgarg/Documents/BidirectionalGraphs";
//GraphDatabaseService[] graphDatabaseServices;
//BatchInserter swiftIndexInserter;
// FeatureNode index;
// static String tempPath = "/Users/shobhitgarg/Documents/tempGraphs/";
// static long count = 1;
// static long siCount = 0;
//public Construction(GraphDatabaseService[] graphDatabaseServices)   {
//    this.graphDatabaseServices = graphDatabaseServices;
//    try {
//        setUpDatabase();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//
//}
//
//    public static void main(String[] args) throws IOException {
//        String graphFilePath = "/Users/shobhitgarg/Documents/GraphDB5/";
//        String targetSet []= new String[]{"backbones_1O54.grf", "backbones_1UOW.grf", "backbones_2AXQ.grf", "saccharomyces_cerevisiae_1ZTA.grf"};
//        GraphDatabaseService [] databaseService = new GraphDatabaseService[4];
//        for(int i = 0; i<databaseService.length; i++)   {
//            databaseService[i]  = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphFilePath + targetSet[i]));
//        }
//        Construction obj = new Construction(databaseService);
//        obj.createIndex();
//        obj.swiftIndexInserter.shutdown();
//    }
//
//void setUpDatabase() throws IOException {
//    File file1 = new File(newDbPath + "/SwiftIndex");
//    swiftIndexInserter = BatchInserters.inserter(file1);
//}
//
//void createIndex() throws IOException {
//    Map<String, Object> inserterMap = new HashMap<>();
//    ArrayList<Label> lbSet = new ArrayList<>();
//    lbSet.add(Label.label("root"));
//    inserterMap.put("id", siCount++);
//    inserterMap.put("labels", "root");
//    long rootNodeID = swiftIndexInserter.createNode(inserterMap, lbSet.toArray(new Label[lbSet.size()]));
//    index = new FeatureNode();
//    //index.labels.add("root");
//// for each feature size
//    int maxSize = 4;
//    for(int size =1; size<= maxSize; size ++) {
//// for each graph database
//        for (int i = 0; i<graphDatabaseServices.length; i++) {
//            HashMap<String, Feature> featureSoln = Utilities.findFeatures(graphDatabaseServices, size);
//            GraphDatabaseService databaseService = graphDatabaseServices[i];
//            try(Transaction tx = databaseService.beginTx()) {
//            // if size is 1, just add the node, if the size is greater than 1, then iterate through the existing swift index and then do something
//       if(size <= 2) {
//           if (size == 1) {
//               for (Feature feature : featureSoln) {
//                   Node node = databaseService.getNodeById(feature.nodes.get(0)); // since it contains only one node
//                   inserterMap = new HashMap<>();
//                   Iterable<Label> labels = node.getLabels();
//                   lbSet = new ArrayList<>();
//                   for (Label l : labels)
//                       lbSet.add(l);
//                   inserterMap.put("id", siCount++);
//                   inserterMap.put("labels", lbSet.toString());
//                   inserterMap.put("graphID", feature.graphID);
//                   inserterMap.put("isFeature", true);
//                   long newNodeID = swiftIndexInserter.createNode(inserterMap,lbSet.toArray(new Label[lbSet.size()]) );
//                   swiftIndexInserter.createRelationship(rootNodeID, newNodeID, RelationshipType.withName("Parent"), null);
//               }
//           }
//           //size == 2
//           else {
//               for (Feature feature : featureSoln) {
//
//                   Node node = databaseService.getNodeById(feature.nodes.get(0));
//                   Node node1 = databaseService.getNodeById(feature.nodes.get(1));
//                   inserterMap = new HashMap<>();
//                   Iterable<Label> labels = node.getLabels();
//                   lbSet = new ArrayList<>();
//                   for (Label l : labels)
//                       lbSet.add(l);
//                   inserterMap.put("id", siCount++);
//                   inserterMap.put("labels", lbSet.toString());
//                   long newNodeID1 = swiftIndexInserter.createNode(inserterMap,lbSet.toArray(new Label[lbSet.size()]) );
//
//                   inserterMap = new HashMap<>();
//                   labels = node1.getLabels();
//                   lbSet = new ArrayList<>();
//                   for (Label l : labels)
//                       lbSet.add(l);
//                   inserterMap.put("graphID", feature.graphID);
//                   inserterMap.put("isFeature", true);
//                   inserterMap.put("id", siCount++);
//                   inserterMap.put("labels", lbSet.toString());
//                   long newNodeID2 = swiftIndexInserter.createNode(inserterMap,lbSet.toArray(new Label[lbSet.size()]) );
//                   swiftIndexInserter.createRelationship(newNodeID1, newNodeID2, RelationshipType.withName("Parent"), null);
//                   swiftIndexInserter.createRelationship(rootNodeID, newNodeID1, RelationshipType.withName("Parent"), null);
//
//               }
//           } // size == 2
//        } // if size <=2
//       // if size is greater than 2
//            else{
//               // for each feature
//               for (Feature feature : featureSoln) {
//                   File file1 = new File(tempPath + "feature" + count++);
//                   BatchInserter inserter = BatchInserters.inserter(file1);
//                   // for each node in the feature
//                   int index = 0;
//                   HashMap<Long, Long> nodeMap = new HashMap<>();
//                   for (Long nodeID : feature.nodes) {
//                       Node node = databaseService.getNodeById(nodeID);
//                       //FeatureNode featureNode = new FeatureNode();
//                       inserterMap = new HashMap<>();
//                       Node node1 = databaseService.getNodeById(nodeID);
//                       Iterable<Label> labels = node1.getLabels();
//                       lbSet = new ArrayList<>();
//                       for (Label l : labels)
//                           lbSet.add(l);
//                       inserterMap.put("labels", lbSet.toString());
//                       inserterMap.put("id", index++);
//                       nodeMap.put(nodeID, inserter.createNode(inserterMap, lbSet.toArray(new Label[lbSet.size()])));
//
//                   }
//                   for (Long edge1 : feature.edges.keySet()) {
//                       inserter.createRelationship(nodeMap.get(edge1), nodeMap.get(feature.edges.get(edge1)), RelationshipType.withName(""), null);
//                   }
//                   inserter.shutdown();
//
//                   GraphDatabaseService featureService = new GraphDatabaseFactory().newEmbeddedDatabase(file1);
//                   GraphDatabaseService[] databaseServiceArray = new GraphDatabaseService[1];
//                   databaseServiceArray[0] = databaseService;
//
//                   CalculateAvgInnerSupport avgInnerSupport = new CalculateAvgInnerSupport();
//                   avgInnerSupport.calcInnerSupportEdge(featureService, databaseServiceArray);
//                   avgInnerSupport.calcInnerSupportVertex(featureService, databaseServiceArray);
//                   CalcMST calcMST = new CalcMST();
//                   GraphDatabaseService databaseServiceSeq = calcMST.calculateMST1(featureService, tempPath + "weightedFeature" + (count - 1));
//               } // for each feature
//
//
//       }
//            tx.success();
//            tx.close();
//        }
//
//        } // for each graph db
//
//    }
//}
//
//}
