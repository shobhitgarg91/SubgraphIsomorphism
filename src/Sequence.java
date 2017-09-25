import java.util.ArrayList;

/**
 * Created by shobhitgarg on 9/19/17.
 */
public class Sequence {

    Edge edge;
    Vertex vertex;
    Vertex parent;
    ArrayList<String> labels;
    int degree;
    ArrayList<Edge> edges;
    Sequence(Edge e) {
        edge = e;
        edges = new ArrayList<>();
    }

}
