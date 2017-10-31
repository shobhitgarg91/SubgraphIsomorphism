import org.neo4j.graphdb.Label;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by shobhitgarg on 10/24/17.
 */
public class FeatureNode {
    long id;
    HashSet<Label> labels = new HashSet<>();
    HashSet<Integer> graphIDs = new HashSet<>();
    boolean isFeature = false;
    boolean isRoot = false;
    HashSet<FeatureNode> children = new HashSet<>();
}
