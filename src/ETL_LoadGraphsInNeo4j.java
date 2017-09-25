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
 * Created by shobhitgarg on 3/18/17.
 */

/**
 * This class is used to perform the ETL process for loading the graphs and the queries in Neo4j.
 * It also calculates the profile of the node and stores it as a node property in Neo4j.
 */
public class ETL_LoadGraphsInNeo4j {
    HashMap<Integer, Long> nodes = new HashMap<>();
    HashMap<Integer, StringBuilder> nodesProfile = new HashMap<>();
    HashMap<Integer, Long> nodesHuman = new HashMap<>();

    File [] files;
    public static void main(String[] args) throws IOException {
        ETL_LoadGraphsInNeo4j obj = new ETL_LoadGraphsInNeo4j();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter the new DB path: ");
        String path2 = sc.next();
        System.out.println("Enter the path to the folder where protein and igraph is present");
        String graphPath = sc.next();

        obj.insertInGraphDB(graphPath, path2);
    }

    /**
     * Used to insert different graphs in Neo4j.
     * @throws IOException
     */
    void insertInGraphDB(String graphPath, String newDBpath) throws IOException {
        long time1 = System.currentTimeMillis();
        if(!newDBpath.endsWith("/"))
            newDBpath+="/";

        //human igraph
        String path = newDBpath+ "humanIgraph";
        File file2 = new File(graphPath + "/iGraph/human.igraph");
        FileReader fileReader = new FileReader(file2);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        loadIGraph(bufferedReader, path, false);
        System.out.println("Human igraph loaded");

        //human queries
         file2 = new File(graphPath+ "/iGraph/human_q10.igraph");
         path = newDBpath + "human_q10Igraph";
         fileReader = new FileReader(file2);
         bufferedReader = new BufferedReader(fileReader);
        loadIGraph(bufferedReader, path, true);
        System.out.println("Human queries loaded");

        // yeast igraph
        file2 = new File(graphPath + "/iGraph/yeast.igraph");
        path = newDBpath + "yeastIgraph";
        fileReader = new FileReader(file2);
        bufferedReader = new BufferedReader(fileReader);
        loadIGraph(bufferedReader, path, false);
        System.out.println("Yeast igraph loaded");

    // yeast queries
        file2 = new File(graphPath + "/iGraph/yeast_q10.igraph");
        path =newDBpath + "yeast_q10Igraph";
        fileReader = new FileReader(file2);
        bufferedReader = new BufferedReader(fileReader);
        loadIGraph(bufferedReader, path, true);
        System.out.println("Yeast queries loaded");
        long time2 = System.currentTimeMillis();
        System.out.println("ETL Time: " + (time2 - time1));

        File file = new File(graphPath + "/Proteins/Proteins/target/");
        //loading the protein graphs
        loadProteinGraph(file, newDBpath);
        // loading the protein queries
        file = new File(graphPath + "/Proteins/Proteins/query/");
        loadProteinGraph(file, newDBpath);
        // reading in the igraphs
    }

    /**
     * Used to load the protein graph. It iterates over all the files present in the protein folder and loads each of
     * the file as a separate graph in Neo4j.
     * @param file  path of the folder of protein graph
     * @throws IOException
     */
    void loadProteinGraph(File file, String newDBPath) throws IOException{
        files = file.listFiles();
        int fileCount = 0;
        for (File f : files) {

            if (!f.isDirectory()) {
                nodesProfile = new HashMap<>();
                if(!newDBPath.endsWith("/"))
                    newDBPath+= "/";
                File file1 = new File(newDBPath + f.getName());
                BatchInserter inserter = BatchInserters.inserter(file1);
                FileReader fileReader = new FileReader(f.getPath());
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
                            if (id1 < id2)  {
                                if (nodesProfile.containsKey(id1)) {
                                    nodesProfile.put(id1, new StringBuilder(nodesProfile.get(id1).toString() + id2 + ", "));
                                } else
                                    nodesProfile.put(id1, new StringBuilder(id2 + ", "));

                            inserter.createRelationship(nodes.get(id1), nodes.get(id2), RelationshipType.withName(""), null);
                        }
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
        }
        nodesProfile = new HashMap<>();
    }

    /**
     * this function is used to load the Igraph in Neo4j. It iterates over the Igraph file and reads different graphs present
     * in the file (in case the file is a query Igraph file).
     * @param bufferedReader        reader that is used to read the file
     * @param path                  path at which the graph is to be stored.
     * @param isQuery               boolean used to determine whether the file is a target file or a query file.
     * @throws IOException
     */
    void loadIGraph(BufferedReader bufferedReader, String path, boolean isQuery) throws IOException  {

int id = -1;
System.out.println(path);
        BatchInserter inserter = null;
        String line = null;
        while ((line = bufferedReader.readLine())!= null)   {
            String[] lineData = line.split(" ");
            if(lineData[0].equals("t")) {
                // new graph
                if(isQuery) {
                 if(id != -1)
                     inserter.shutdown();
                 String oldFile = path + id + "/";
                    inserter = BatchInserters.inserter(new File(path + ++id + "/"));
                    //loadProfiles(path);
                    nodesProfile = new HashMap<>();
                }
                else
                    inserter = BatchInserters.inserter(new File(path));

                nodesHuman = new HashMap<>();
            }
            else if(lineData[0].equals("v"))    {
                // vertex in the graph
                int id1 = Integer.parseInt(lineData[1]);
                HashMap<String, Object> insertData = new HashMap<>();
                insertData.put("id", id1);
                //insertData.put("graphID", id);
                int count = 0;
                Label[]lb = new Label[lineData.length - 2];
                for(int i = 2; i<lineData.length; i++)  {
                    lb[count ++] = Label.label(lineData[i]);
                }
                nodesHuman.put(id1, inserter.createNode(insertData, lb));

            }
            else if(lineData[0].equals("e"))    {
                // edge in the graph
                try {
                    int id1 = Integer.parseInt(lineData[1]);
                    int id2 = Integer.parseInt(lineData[2]);
                    if (nodesProfile.containsKey(id1)) {
                        nodesProfile.put(id1, new StringBuilder(nodesProfile.get(id1).toString() + id2 + ", "));

                    }
                    else
                        nodesProfile.put(id1, new StringBuilder(id2 + ", "));
                    inserter.createRelationship(nodesHuman.get(id1), nodesHuman.get(id2), RelationshipType.withName(lineData[3]), null);
                }
                catch (Exception e) {
                    System.out.println(line);
                    System.exit(1);
                }
            }
        }
        if(!isQuery)    {
            inserter.shutdown();
            for (int id1 : nodesProfile.keySet()) {
                StringBuilder sb = nodesProfile.get(id1);
                sb.setLength(sb.length() - 2);
                nodesProfile.put(id1, sb);

            }
            loadProfiles(path);
        }

    }

    /**
     * This function is used used to load the profiles in the Neo4j graph. It computes the set of labels that all the
     * neighbors of the nodes has and adds that as a profile in the node.
     * @param file      file where the graph is stored.aa
     */
    void loadProfiles(String file) {
        System.out.println("loadProfiles: " + file);
        // inserting the profiles
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();

        GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(new File(file));

        try(Transaction tx = databaseService.beginTx()) {

            for (int id : nodesProfile.keySet()) {
                String[] order = nodesProfile.get(id).toString().split(", ");
                ArrayList<String > lexOrder = new ArrayList<>();
                int count = -1;
                for (String n1 : order) {
                    int id1 = Integer.parseInt(n1);
                    Iterable<Label> labels = null;
                    Long nodeID = nodesHuman.get(id1);

                    try {
                        Node node = databaseService.getNodeById(nodeID);
                        labels = node.getLabels();
                    }

                    catch (Exception e) {
                        Long id2 = nodesHuman.get(id1);

                        System.out.println("Errororr: " + id2);

                    }

                    if(labels == null)  {
                        System.out.println("No labels");
                    }

                    for(Label l: labels)
                        lexOrder.add(l.toString());
                }
                Collections.sort(lexOrder);
                StringBuilder sb1 = new StringBuilder();
                for (String x : lexOrder)
                    sb1.append(x + ",");
                sb1.setLength(sb1.length() - 1);
                databaseService.getNodeById(nodesHuman.get(id)).setProperty("Profile", sb1.toString());
            }
            tx.success();
        }
        nodesProfile = new HashMap<>();
        databaseService.shutdown();

    }
}
