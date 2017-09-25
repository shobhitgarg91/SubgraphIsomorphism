import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;

/**
 * Created by shobhitgarg on 9/22/17.
 */
public class SequenceNode {

    Node parent;
    Node currNode;
    Relationship edge;
    Iterable<Label> labels;
    int degree = 0;
    ArrayList<Relationship> rij = new ArrayList<>();
//    SequenceNode(Relationship edge) {
//        this.edge = edge;
//    }

    SequenceNode(Node currNode) {
        this.currNode = currNode;
        this.degree = currNode.getDegree();
        findLabels();
    }

    void findLabels()   {
        labels = currNode.getLabels();
    }
}
