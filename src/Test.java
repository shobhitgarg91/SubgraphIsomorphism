/**
 * Created by shobhitgarg on 9/15/17.
 */
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.*;
import java.util.*;


public class Test {
    HashMap<Integer, Vertex> queryGraphNodes = new HashMap<>();
    HashMap<Integer, Long> nodesHuman = new HashMap<>();
    ArrayList<Edge> edgeList = new ArrayList<>();
    Vertex root = null;

    public static void main(String[] args) throws IOException{

        String graphFilePath = "/Users/shobhitgarg/Documents/GraphDB5/";
        File queryFile = new File("/Users/shobhitgarg/Downloads/Proteins/Proteins/query/backbones_1EMA.8.sub.grf");
        String newDbPath = "/Users/shobhitgarg/Documents/BidirectionalGraphs";
        GraphDatabaseService databaseServiceQuery = Utilities.loadQueryGraph(queryFile, newDbPath);
        GraphDatabaseFactory []dbFactoryArray = new GraphDatabaseFactory[4];
        for(int i = 0; i<dbFactoryArray.length; i ++)
            dbFactoryArray[i] = new GraphDatabaseFactory();
        String targetSet []= new String[]{"backbones_1O54.grf", "backbones_1UOW.grf", "backbones_2AXQ.grf", "saccharomyces_cerevisiae_1ZTA.grf"};
        GraphDatabaseService [] databaseService = new GraphDatabaseService[4];
        for(int i = 0; i<databaseService.length; i++)   {
            databaseService[i]  = dbFactoryArray[i].newEmbeddedDatabase(new File(graphFilePath + targetSet[i]));
        }
        //GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(graphFile);
        Test obj = new Test();
        //obj.readQueryGraph(true, queryFile, -1);
        System.out.println("Hello");
        //ArrayList<ArrayList<Integer>> soln = Utilities.findTreeFeatures(databaseService[0], 3);
//        CalcQISequence qiSequence = new CalcQISequence();
//        qiSequence.calcQI(obj.root, obj.queryGraphNodes);

//
        CalculateAvgInnerSupport avgInnerSupport = new CalculateAvgInnerSupport();
        avgInnerSupport.calcInnerSupportEdge(databaseServiceQuery, databaseService);
        avgInnerSupport.calcInnerSupportVertex(databaseServiceQuery, databaseService);
        avgInnerSupport.calcTotalEdges(databaseService);
        //avgInnerSupport.eliminateEdges(databaseService, 100);
        avgInnerSupport.calculateTotalVertices(databaseService);
        CalcMST calcMST = new CalcMST(obj.queryGraphNodes, obj.edgeList);
        GraphDatabaseService databaseServiceSeq = calcMST.calculateMST1(databaseServiceQuery, newDbPath);
        QuickSI quickSI = new QuickSI(databaseServiceSeq, databaseService[0]);
        //calcMST.calculateMST1(databaseServiceQuery);
        Collections.sort(obj.edgeList, (o1, o2) -> o1.weight - o2.weight>0? 1: o1.weight - o2.weight <0 ? -1: 0);
        System.out.println("Hello");
    }
}
