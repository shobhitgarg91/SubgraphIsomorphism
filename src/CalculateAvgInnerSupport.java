import org.neo4j.graphdb.*;

import java.util.*;

/**
 * Created by shobhitgarg on 9/15/17.
 */
public class CalculateAvgInnerSupport {
    HashMap<String,Edge> edgeMap = new HashMap<>();
    HashMap<String, Vertex> vertexMap = new HashMap<>();


    public void calcTotalEdges( GraphDatabaseService[] databaseServiceArray)    {
        for(GraphDatabaseService databaseService: databaseServiceArray) {
            try (Transaction tx = databaseService.beginTx()) {
                ResourceIterable<Relationship> relationships = databaseService.getAllRelationships();
                Iterator<Relationship> iterator = relationships.iterator();
                while (iterator.hasNext()) {
                    Relationship rel = iterator.next();
                        String key = rel.getStartNode().getLabels().toString().substring(1,2) + rel.getEndNode().getLabels().toString().substring(1,2);
                         char[] charArray = key.toCharArray();
                         Arrays.sort(charArray);
                         key = charArray[0] + "-" + charArray[1];
                        if(edgeMap.containsKey(key))    {
                            edgeMap.get(key).count++;
                        }
                        else {
                            Edge e1 = new Edge((int)rel.getStartNode().getProperty("id"), (int) rel.getEndNode().getProperty("id"), -1);
                            edgeMap.put(key, e1);
                        }
                }
                tx.success();
            }
        }
    }

    public void eliminateEdges(GraphDatabaseService[] databaseServiceArray, float minSup)   {
        int count = (int) (minSup/100 * databaseServiceArray.length);


        for(int i = 0; i<count; i++)    {
            GraphDatabaseService databaseService = databaseServiceArray[i];
            HashSet<String> keysFound = new HashSet<>();
            try (Transaction tx = databaseService.beginTx())    {
                ResourceIterable<Relationship> relationships = databaseService.getAllRelationships();
                Iterator<Relationship> iterator = relationships.iterator();
                while(iterator.hasNext())   {
                    Relationship rel = iterator.next();
                    String key = rel.getStartNode().getLabels().toString().substring(1,2) + "-" + rel.getEndNode().getLabels().toString().substring(1,2);
                    keysFound.add(key);
                }
                tx.success();
            }
            for(String key: edgeMap.keySet())   {
                if(!keysFound.contains(key))
                    edgeMap.get(key).foundEveryWhere = false;
            }

        }
        HashSet<String>toBeDel = new HashSet<>();
        for(String key: edgeMap.keySet())   {
            if(!edgeMap.get(key).foundEveryWhere)
                toBeDel.add(key);
        }
        for(String key: toBeDel)
            edgeMap.remove(key);
    }

    public void calculateTotalVertices(GraphDatabaseService[]databaseServiceArray ) {
        for(GraphDatabaseService databaseService: databaseServiceArray) {
            try(Transaction tx = databaseService.beginTx()) {
                    ResourceIterable<Node> nodes = databaseService.getAllNodes();
                    for(Node n: nodes)  {
                        String l1 = n.getLabels().toString().substring(1,2);
                        if(vertexMap.containsKey(l1))   {
                            vertexMap.get(l1).count ++;
                        }
                        else    {
                            Vertex vc = new Vertex();
                            vc.id1 = (int) n.getProperty("id");
                            vc.label = l1;
                            vertexMap.put(l1, vc);
                        }
                    }
            }
        }
    }


    public void calcInnerSupportEdge(GraphDatabaseService databaseServiceQuery, GraphDatabaseService[] databaseServiceArray) {
        try (Transaction tx = databaseServiceQuery.beginTx())   {
            ResourceIterable<Relationship> relationships = databaseServiceQuery.getAllRelationships();
            Iterator<Relationship> iterator = relationships.iterator();
            while(iterator.hasNext())   {
                // for each edge of the query graph:
                Relationship rel = iterator.next();
                Node beginNode = rel.getStartNode();
                Node endNode = rel.getEndNode();
                Iterable<Label> labels = beginNode.getLabels();
                StringBuilder labelSetBegin = new StringBuilder();
                for(Label label: labels) {
                    labelSetBegin.append(label.toString() + ":");
                }
                labelSetBegin.setLength(labelSetBegin.length() - 1);
                labels = endNode.getLabels();
                StringBuilder labelSetEnd = new StringBuilder();

                for(Label label: labels)
                    labelSetEnd.append(label.toString() + ":");
                labelSetEnd.setLength(labelSetEnd.length() - 1);
                long totalCount = 0;
                long totalGraph = 0;

                for(GraphDatabaseService databaseService: databaseServiceArray) {
                    try (Transaction tx1 = databaseService.beginTx())   {


                        Result result = databaseService.execute("Match (n:" + labelSetBegin.toString() +")--(n1:" +labelSetEnd.toString() + ") return count(n)");
                        Map<String, Object> row = result.next();
                        long individualCount = (long) row.get("count(n)");
                        if(individualCount>0)   {
                            totalGraph ++;
                            totalCount += individualCount;
                        }
                        tx1.success();
                    }
                }
                double weight = Math.round(((double)totalCount / (double)totalGraph)*1000.0)/1000.0;
                rel.setProperty("Weight", weight);
            }
            tx.success();
        }
    }


    public void calcInnerSupportVertex(GraphDatabaseService databaseServiceQuery, GraphDatabaseService[] databaseServiceArray) {
        try (Transaction tx = databaseServiceQuery.beginTx())   {
                ResourceIterable<Node> nodes = databaseServiceQuery.getAllNodes();
            for(Node node: nodes) {
                Iterable<Label> labels = node.getLabels();
                StringBuilder labelSet  = new StringBuilder();
                for(Label label: labels)
                    labelSet.append(label.toString() + ":");
                labelSet.setLength(labelSet.length() - 1);
                int totalCount = 0;
                int totalGraph = 0;
                for(GraphDatabaseService databaseService: databaseServiceArray) {
                    try (Transaction tx1 = databaseService.beginTx())   {
                        Result result = databaseService.execute("Match (n:" + labelSet.toString() + ") return count(n)");
                        Map<String, Object> row = result.next();
                        long individualCount = (long)row.get("count(n)");
                        if(individualCount>0)   {
                            totalGraph ++;
                            totalCount += individualCount;
                        }
                        tx1.success();
                    }
                }
                double weight = Math.round(((double)totalCount / (double)totalGraph)*1000.0)/1000.0;
                node.setProperty("Weight", weight);
            }
            tx.success();
        }
    }



//    public void calcInnerSupportEdge(HashMap<Integer, Vertex> queryGraphNodes, GraphDatabaseService[] databaseServiceArray, ArrayList<Edge> edgeList) {
//        // for each node in query graph:
//        for (int nodeID : queryGraphNodes.keySet()) {
//            // for each edge for the current node:
//            for (int otherNode : queryGraphNodes.get(nodeID).edges.keySet()) {
//                int total_graph = 0;
//                Label label1 = Label.label(queryGraphNodes.get(nodeID).label);
//                Label label2 = Label.label(queryGraphNodes.get(otherNode).label);
//                int total_count = 0;
//                for(GraphDatabaseService databaseService: databaseServiceArray) {
//                    boolean graphCovered = false;
//                    try (Transaction tx = databaseService.beginTx()) {
//                        ResourceIterable<Relationship> relationships = databaseService.getAllRelationships();
//                        Iterator<Relationship> iterator = relationships.iterator();
//                        while (iterator.hasNext()) {
//                            Relationship rel = iterator.next();
//
//                            if ((rel.getStartNode().hasLabel(label1) && rel.getEndNode().hasLabel(label2)) ||
//                                    (rel.getEndNode().hasLabel(label2) && rel.getStartNode().hasLabel(label1))) {
//                                // edges match with each other
//                                total_count++;
//                                if(!graphCovered)   {
//                                    graphCovered = true;
//                                    total_graph++;
//                                }
//                            }
//                        }
//                        tx.success();
//                    }// end of tx
//                }
//                    double weight = Math.round(((double)total_count / (double)total_graph)*1000.0)/1000.0;
//                    queryGraphNodes.get(nodeID).edgeWeights.put(otherNode,weight);
//                    Edge edge = new Edge(nodeID, otherNode, weight);
//                    edge.degU = queryGraphNodes.get(nodeID).edges.size();
//                    edge.degV = queryGraphNodes.get(otherNode).edges.size();
//                    edgeList.add(edge);
//            }
//
//        }
//    }
//
//
//
//
//    public void calcInnerSupportVertex(HashMap<Integer, Vertex> queryGraphNodes, GraphDatabaseService[] databaseServiceArray) {
//
//        // for each node in query graph:
//        for (int nodeID : queryGraphNodes.keySet()) {
//            int total_graph = 0;
//            int total_count = 0;
//
//            for(GraphDatabaseService databaseService: databaseServiceArray) {
//                boolean graphCovered = false;
//            try(Transaction tx = databaseService.beginTx()) {
//               Result result = databaseService.execute("Match (n:" + queryGraphNodes.get(nodeID).label + ") RETURN count(n) ");
//                Map<String, Object> row = result.next();
//                int val =  Integer.parseInt(String.valueOf(row.get("count(n)")));
//                if(val>0)   {
//                    total_count+= val;
//                    if(!graphCovered)   {
//                        graphCovered = true;
//                        total_graph ++;
//                    }
//                }
//                tx.success();
//            }// end of tx
//        }
//            queryGraphNodes.get(nodeID).vertexWeight = ((double)total_count / (double) total_graph * 1000.0 )/ 1000.0;
//
//        }
//    }


}
