import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by shobhitgarg on 3/23/17.
 */

/**
 * This class represents a node in the query graph. It is used to store the query graph in memory.
 */
public class Vertex {
    int id1;
    boolean isProtein = false;
    String label;
    Double vertexWeight;
    int count = 1;
    ArrayList<Integer> labels = new ArrayList<>();
    boolean foundEveryWhere = true;
    HashSet<Integer> found = new HashSet<>();
    HashMap<Integer, Integer> edges = new HashMap<>();

    Vertex parent;

    // value used to store edge weight
    HashMap<Integer, Double> edgeWeights = new HashMap<>();

}
