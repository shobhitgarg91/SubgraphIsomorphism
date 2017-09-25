import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shobhitgarg on 9/19/17.
 */
public class Utilities {
    static ArrayList<ArrayList<Integer>> soln = new ArrayList<>();

    public static ArrayList<ArrayList<Integer>> findTreeFeatures(GraphDatabaseService databaseService, int size)  {
        char ch = 'a';
        StringBuilder query = new StringBuilder("Match ");
        for(int i = 0; i<size; i++) {
            query.append("(" + ch++ + ")--");
        }
        query.setLength(query.length() - 2);
        query.append(" return ");
        for(char x = 'a'; x< ch; x++)
            query.append(x + ".id, ");
        query.setLength(query.length() - 2);
        System.out.println(query.toString());


        try (Transaction tx = databaseService.beginTx())    {
            Result result = databaseService.execute(query.toString());
            while (result.hasNext())    {
                Map<String, Object> row = result.next();
                ArrayList<Integer> path = new ArrayList<>();
                path.add((int)row.get("a.id"));
                path.add((int)row.get("b.id"));
                path.add((int)(row.get("c.id")));
                soln.add(path);
            }
            tx.success();
        }

        return soln;
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

    public static long insertNode(BatchInserter inserter, Node n, Long parent)   {
        Map<String, Object> inserterMap = new HashMap<>();
        inserterMap.put("parent", parent);
        inserterMap.put("idd", n.getId());
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


}
