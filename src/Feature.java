import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Created by shobhitgarg on 10/8/17.
 */
public class Feature {
//    ArrayList<GraphDatabaseService> fList = new ArrayList<>();
//    Node node;
//    Relationship relationship;
//    boolean isNode;

    ArrayList<Long> nodes = new ArrayList<>();
    HashMap<Long, HashSet<Label>> labels = new HashMap<>();
    HashMap<Long, ArrayList<Long>> edges = new HashMap<>();
    HashSet<Integer> graphIDs = new HashSet<>();
}
