import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by shobhitgarg on 9/19/17.
 */
public class Utilities {

    public static void main(String[] args) {
        GraphDatabaseService databaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File("/Users/shobhitgarg/Documents/GraphDB5/backbones_1O54.grf"));
        int size = 1;

      //  ArrayList<Feature> findFeatures = Utilities.findFeatures(databaseService, 3, 0);
        System.out.println("Check");
        // findTreeFeatures(databaseService, 3);
    }

    public static HashMap<String, Feature> findFeatures(GraphDatabaseService[] databaseServices, int size) {
        HashMap<String, Feature> featureSoln = new HashMap<>();
        HashMap<String, HashSet<Integer>> featurePatternSet = new HashMap<>(); // this is created to ensure that the same pattern is not being repeated again. for example C-N pattern may be repeating
        // multiple times, hence we need to ensure that once we have added a pattern, we shouldn't add the same pattern again.
        // for size 1

        ArrayList<String> queries = new ArrayList<>();


        if (size == 1) {
            String query1 = "match(n) return distinct(labels(n))";
            queries.add(query1);
        } else if (size == 2) {
            queries.add("match(n)-->(n1) return n.id, n1.id");
        } else if (size == 3) {
            //queries.add("match(n)-->(n1)-->(n2) return n.id, n1.id, n2.id");
            queries.add("match(n)-->(n1)-->(n2) where n.id <> n2.id return n.id, n1.id, n2.id");
            queries.add("match(n)-->(n1), (n)-->(n2) return n.id, n1.id, n2.id");
            //queries.add("match(n)-->(n1), (n)-->(n2), (n1)--(n2) return n.id, n1.id, n2.id");
        } else if (size == 4) {
            //queries.add("match(n)-->(n1)-->(n2)-->(n3) return n.id, n1.id, n2.id, n3.id");
            queries.add("match(n)-->(n1)-->(n2)-->(n3) where n.id <> n3.id and n.id <> n2.id return n.id, n1.id, n2.id, n3.id");
            queries.add("match(n)-->(n1), (n)-->(n2), (n)-->(n3) return n.id, n1.id, n2.id, n3.id");
            queries.add("match(n)-->(n1)-->(n2), (n)-->(n3) where n.id <> n3.id and n.id <> n2.id return n.id, n1.id, n2.id, n3.id");
            queries.add("match(n)-->(n1), (n1)-->(n2), (n1)-->(n3) return n.id, n1.id, n2.id, n3.id");

            //queries.add("match(n)-->(n1)-->(n2)-->(n3)-->(n1) return n.id, n1.id, n2.id, n3.id");
            //queries.add("match(n)-->(n1), (n)-->(n2), (n)-->(n3), (n1)--(n2) return n.id, n1.id, n2.id, n3.id");
            //queries.add("match(n)-->(n1)-->(n2)-->(n3)-->(n1), (n)--(n2) return n.id, n1.id, n2.id, n3.id");
            //queries.add("match(n)-->(n1)-->(n2)-->(n3)-->(n1), (n)--(n2), (n1)--(n3) return n.id, n1.id, n2.id, n3.id");
        } else {
            System.out.println("Incorrect feature size, feature size cannot be more than 4 for this implementation");
            System.exit(1);
        }
        for(int i = 0; i<databaseServices.length; i++)  {
            HashSet<String> featurePatternSet1= new HashSet<>();
            GraphDatabaseService databaseService = databaseServices[i];
        try (Transaction tx = databaseService.beginTx()) {
            if (size == 1) {
                Result result = databaseService.execute(queries.get(0));
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    String tempLabel = String.valueOf(row.get("(labels(n))"));
                    tempLabel = tempLabel.substring(1, tempLabel.length() - 1);
                    if (!featureSoln.containsKey(tempLabel)) {
                        featurePatternSet1.add(tempLabel);
                        Result result1 = databaseService.execute("match (n:" + tempLabel + ") return n.id limit 1");
                        Map<String, Object> row1 = result1.next();
                        Feature feature = new Feature();
                        feature.graphIDs.add(i);
                        feature.nodes.add(Long.parseLong(String.valueOf(row1.get("n.id"))));
                        // getting all the labels
                        for (Long nodeID : feature.nodes)
                            feature.labels.put(nodeID, Utilities.getLabels(databaseService.getNodeById(nodeID).getLabels()));
                        featureSoln.put(tempLabel, feature);
                    }
                    else {
                        featureSoln.get(tempLabel).graphIDs.add(i);
                    }
                }

            } else if (size == 2) {
                Result result = databaseService.execute(queries.get(0));
                while (result.hasNext()) {
                    Map<String, Object> row = result.next();
                    ArrayList<Long> list = new ArrayList<>();
                    list.add(Long.parseLong(String.valueOf(row.get("n.id"))));
                    list.add(Long.parseLong(String.valueOf(row.get("n1.id"))));
                    Iterable<Label> labels = databaseService.getNodeById(list.get(0)).getLabels();
                    String label = labels.toString();
                    labels = databaseService.getNodeById(list.get(1)).getLabels();
                    label += labels.toString();
                    if (!featureSoln.containsKey(label)) {
                        featurePatternSet1.add(label);
                        Feature feature = new Feature();
                        feature.graphIDs.add(i);
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n.id"))));
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n1.id"))));
                        // getting all the labels
                        for (Long nodeID : feature.nodes)
                            feature.labels.put(nodeID, Utilities.getLabels(databaseService.getNodeById(nodeID).getLabels()));

                        feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                        featureSoln.put(label, feature);
                    }
                    else {
                        featureSoln.get(label).graphIDs.add(i);
                    }
                }
            } else if (size == 3) {
                // for each query
                for (String query : queries) {
                    Result result = databaseService.execute(query);
                    while (result.hasNext()) {
                        Map<String, Object> row = result.next();
                        ArrayList<Long> list = new ArrayList<>();
                         list.add(Long.parseLong(String.valueOf(row.get("n.id"))));
                        list.add(Long.parseLong(String.valueOf(row.get("n1.id"))));
                        list.add(Long.parseLong(String.valueOf(row.get("n2.id"))));
                        Iterable<Label> labels = databaseService.getNodeById(list.get(0)).getLabels();
                        String label = labels.toString();
                        labels = databaseService.getNodeById(list.get(1)).getLabels();
                        label += labels.toString();
                        labels = databaseService.getNodeById(list.get(2)).getLabels();
                        label += labels.toString();
                        Feature feature = new Feature();
                        feature.graphIDs.add(i);
                         feature.nodes.add(Long.parseLong(String.valueOf(row.get("n.id"))));
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n1.id"))));
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n2.id"))));
                        // getting all the labels
                        for (Long nodeID : feature.nodes)
                            feature.labels.put(nodeID, Utilities.getLabels(databaseService.getNodeById(nodeID).getLabels()));
                        if (query.equals("match(n)-->(n1)-->(n2) where n.id <> n2.id return n.id, n1.id, n2.id")) {
                            label += "n,n1,n2";
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n1.id"))), Long.parseLong(String.valueOf(row.get("n2.id"))));
                        } else {
                            label += "n,n1,n,n2";
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n2.id"))));
                        }
                        if (!featureSoln.containsKey(label)) {
                            featurePatternSet1.add(label);
                            featureSoln.put(label, feature);
                        }
                        else {
                            featureSoln.get(label).graphIDs.add(i);
                        }
                    }

                } // for each query
            } else {
                // for each query
                for (String query : queries) {
                    Result result = databaseService.execute(query);
                    while  (result.hasNext()) {
                        Map<String, Object> row = result.next();
                        ArrayList<Long> list = new ArrayList<>();
                        list.add(Long.parseLong(String.valueOf(row.get("n.id"))));
                        list.add(Long.parseLong(String.valueOf(row.get("n1.id"))));
                        list.add(Long.parseLong(String.valueOf(row.get("n2.id"))));
                        list.add(Long.parseLong(String.valueOf(row.get("n3.id"))));
                        Iterable<Label> labels = databaseService.getNodeById(list.get(0)).getLabels();
                        String label = labels.toString();
                        labels = databaseService.getNodeById(list.get(1)).getLabels();
                        label += labels.toString();
                        labels = databaseService.getNodeById(list.get(2)).getLabels();
                        label += labels.toString();
                        labels = databaseService.getNodeById(list.get(3)).getLabels();
                        label += labels.toString();
                        Feature feature = new Feature();
                        feature.graphIDs.add(i);
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n.id"))));
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n1.id"))));
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n2.id"))));
                        feature.nodes.add(Long.parseLong(String.valueOf(row.get("n3.id"))));
                        // getting all the labels
                        for (Long nodeID : feature.nodes)
                            feature.labels.put(nodeID, Utilities.getLabels(databaseService.getNodeById(nodeID).getLabels()));
                        if (query.equals("match(n)-->(n1)-->(n2)-->(n3) where n.id <> n3.id and n.id <> n2.id return n.id, n1.id, n2.id, n3.id")) {
                            label += "n,n1,n2,n3";
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n1.id"))), Long.parseLong(String.valueOf(row.get("n2.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n2.id"))), Long.parseLong(String.valueOf(row.get("n3.id"))));
                        } else if (query.equals("match(n)-->(n1), (n)-->(n2), (n)-->(n3) return n.id, n1.id, n2.id, n3.id")) {
                            label += "n,n1,n,n2,n,n3";
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n2.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n3.id"))));
                        } else if (query.equals("match(n)-->(n1)-->(n2), (n)-->(n3) where n.id <> n3.id and n.id <> n2.id return n.id, n1.id, n2.id, n3.id")) {
                            label += "n,n1,n2,n,n3";
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n1.id"))), Long.parseLong(String.valueOf(row.get("n2.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n3.id"))));
                        } else {
                            label += "n,n1,n2,n1,n3";
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n.id"))), Long.parseLong(String.valueOf(row.get("n1.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n1.id"))), Long.parseLong(String.valueOf(row.get("n2.id"))));
                            feature.edges.put(Long.parseLong(String.valueOf(row.get("n1.id"))), Long.parseLong(String.valueOf(row.get("n3.id"))));

                        }
                        if (!featureSoln.containsKey(label)) {
                            featurePatternSet1.add(label);
                            featureSoln.put(label, feature);
                        }
                        else {
                            featureSoln.get(label).graphIDs.add(i);
                        }
                    }
                } // for each query
            }

            tx.success();
            tx.close();
        }
    }// for each graph


        return featureSoln;
    }


    public static String getLabels(Node n)  {
        StringBuilder sb = new StringBuilder();
        Iterable<Label> labels = n.getLabels();
        for(Label l: labels)
            sb.append(" n:" + l.toString() + " or");
        sb.setLength(sb.length() - 3);
        return sb.toString();
    }

    public static long countNodesInGraph(GraphDatabaseService databaseService)   {
        Result result = databaseService.execute("match (n) return count(n)");
        Map<String, Object> row = result.next();
        return (long) row.get("count(n)");
    }

    public static long insertNode(BatchInserter inserter, Node n, Long parent, int index)   {
        Map<String, Object> inserterMap = new HashMap<>();
        inserterMap.put("parent", parent);
        inserterMap.put("idd", n.getProperty("id"));
        inserterMap.put("index", index);
        inserterMap.put("degree", n.getDegree(Direction.OUTGOING));
        Iterable<Label> labels = n.getLabels();
        ArrayList<Label> lbSet = new ArrayList<>();
        for(Label l: labels)
            lbSet.add(l);
        inserterMap.put("labels", lbSet.toString());
        return inserter.createNode(inserterMap, lbSet.toArray(new Label[lbSet.size()]));
    }

    public static ArrayList<Relationship> findEdgesWithMinWeight(GraphDatabaseService databaseServiceQuery)  {
        ArrayList<Relationship> P = new ArrayList<>();
        Result result = databaseServiceQuery.execute("match(n)-[r1]-(n1) return r1.Weight Order by r1.Weight limit 1");
        Map<String, Object> row = result.next();

        double weight = (double)row.get("r1.Weight");
        result = databaseServiceQuery.execute("match(n)-[r1]-(n1) where r1.Weight = " + weight + " return r1");

        //reading all the edges with minimum weight
        while (result.hasNext()) {
            row = result.next();
            P.add((Relationship) row.get("r1"));
        }
        return P;
    }

    public static GraphDatabaseService loadQueryGraph(File queryFile, String newDBPath) throws IOException{
        if(!newDBPath.endsWith("/"))
            newDBPath+= "/";
        loadProteinGraph(queryFile, newDBPath);
        return new GraphDatabaseFactory().newEmbeddedDatabase(new File(newDBPath + queryFile.getName()));
    }

    /**
     * Used to load the protein graph. It iterates over all the files present in the protein folder and loads each of
     * the file as a separate graph in Neo4j.
     * @param file  path of the folder of protein graph
     * @throws IOException
     */
    static void loadProteinGraph(File file, String newDBPath) throws IOException{
        HashMap<Integer, StringBuilder> nodesProfile = new HashMap<>();
        HashMap<Integer, Long> nodes = new HashMap<>();

        File file1 = new File(newDBPath + file.getName());
        BatchInserter inserter = BatchInserters.inserter(file1);
        FileReader fileReader = new FileReader(file.getPath());
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;
        int lineread = -1;
        boolean edgesEncountered = false;
        while ((line = br.readLine()) != null) {
            lineread++;

            if (lineread == 0)
                continue;
            String lineData[] = line.split(" ");
            if (lineData.length == 2) {
                if (!edgesEncountered) {
                    int id = Integer.parseInt(lineData[0]);
                    HashMap<String, Object> insertData = new HashMap<>();
                    insertData.put("id", id);
                    nodes.put(id, inserter.createNode(insertData, Label.label(lineData[1])));


                } else {
                    int id1 = Integer.parseInt(lineData[0]);
                    int id2 = Integer.parseInt(lineData[1]);
                    if (nodesProfile.containsKey(id1)) {
                        nodesProfile.put(id1, new StringBuilder(nodesProfile.get(id1).toString() + id2 + ", "));
                    } else
                        nodesProfile.put(id1, new StringBuilder(id2 + ", "));

                    inserter.createRelationship(nodes.get(id1), nodes.get(id2), RelationshipType.withName(""), null);
                }
            } else {
                edgesEncountered = true;
            }
        }

        // storing profiles
        for (int id : nodesProfile.keySet()) {
            StringBuilder sb = nodesProfile.get(id);
            sb.setLength(sb.length() - 2);
            nodesProfile.put(id, sb);

        }

        //

        inserter.shutdown();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // inserting the profiles
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();

        GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(file1);

        try(Transaction tx = databaseService.beginTx()) {

            for (int id : nodesProfile.keySet()) {
                String[] order = nodesProfile.get(id).toString().split(", ");
                String[] lexOrder = new String[order.length];
                int count = -1;
                for (String n1 : order) {
                    int id1 = Integer.parseInt(n1);
                    Iterable<Label> labels = null;
                    try {
                        labels = databaseService.getNodeById(nodes.get(id1)).getLabels();
                    }
                    catch (Exception e) {
                        Long id2 = nodes.get(id1);
                        Node n = databaseService.getNodeById(id2);
                        System.out.println("Errororr");

                    }
                    for(Label l: labels)
                        lexOrder[++count] = l.toString();
                }
                Arrays.sort(lexOrder);
                StringBuilder sb1 = new StringBuilder();
                for (String x : lexOrder)
                    sb1.append(x);
                databaseService.getNodeById(nodes.get(id)).setProperty("Profile", sb1.toString());
            }
            tx.success();
        }
        databaseService.shutdown();
    }

    static HashSet<Label>  getLabels(Iterable<Label> labels)  {
        HashSet<Label> lbSet = new HashSet<>();
        for (Label l : labels)
            lbSet.add(l);
        return lbSet;
    }

}
