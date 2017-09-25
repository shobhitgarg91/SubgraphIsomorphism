import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by shobhitgarg on 9/24/17.
 */
public class QuickSI {
    static int max = 0;
    long beta = 0;

    public QuickSI(GraphDatabaseService databaseServiceSeq, GraphDatabaseService databaseServiceGraph)    {
        boolean ans = false;
        try (Transaction tx = databaseServiceGraph.beginTx())   {
            try (Transaction tx1 = databaseServiceSeq.beginTx())    {
                beta = Utilities.countNodesInGraph(databaseServiceSeq);

                ans = performQuickSI(databaseServiceSeq, databaseServiceGraph, new HashMap<Long, Long>(), new HashSet<Long>(), 0);
                System.out.println("MAX depth reached: " + max);
                tx1.success();
            }
            tx.success();
        }
        System.out.println("found: " + ans);
    }

    public boolean performQuickSI(GraphDatabaseService databaseServiceSeq, GraphDatabaseService databaseServiceGraph, HashMap<Long, Long> H, HashSet<Long> F, int d)    {
        if(d>beta - 1)
            return true;
        if(d>max)
            max = d;
        Node t = databaseServiceSeq.getNodeById(d);
        HashSet<Node> v = new HashSet<>();
        String labels = Utilities.getLabels(t);
        String query = "" ;
        if(d == 0)
            query = "match (n) where " + labels + " return n";
        else    {
            long parent = (long)t.getProperty("parent");
            long parentID = H.get(parent);
            query = "match (n1)-->(n) where " + labels + " and ID(n1) = " + parentID + " return n";
        }

        Result result = databaseServiceGraph.execute(query);
        while (result.hasNext())    {
            Map<String, Object> row = result.next();
            Node n = (Node)row.get("n");
            if(!F.contains(n.getId()))
                v.add((Node) row.get("n"));
        }

        for(Node vertex: v) {
            int degree = (int) t.getProperty("degree");
            if (vertex.getDegree(Direction.OUTGOING) < degree)
                continue;
            if (t.hasProperty("Extra edge"))    {

                String extraEdge = (String) t.getProperty("Extra edge");

            extraEdge = extraEdge.substring(1, extraEdge.length() - 1);
            String[] individualEgdes = extraEdge.split(", ");
            ArrayList<Long> edges = new ArrayList<>();
            for (String x : individualEgdes) {
                x = x.trim();
                edges.add((Long.parseLong(x)));
            }


            for (Long edge : edges) {
                Result result1 = databaseServiceGraph.execute("match(n) --> (n1) where ID(n) = " + vertex.getId() + " and ID(n1) = " + H.get(edge) + " return n");
                if(!result1.hasNext())
                    return false;

            }


        }
                H.put((long) d, vertex.getId());
                F.add(vertex.getId());
                if (performQuickSI(databaseServiceSeq,databaseServiceGraph, H, F, d + 1))
                    return true;
                F.remove(vertex.getId());
        }
        return false;
    }

}
