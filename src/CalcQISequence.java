import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by shobhitgarg on 9/18/17.
 */
public class CalcQISequence {

    Stack<Vertex> stack = new Stack<>();
    HashSet<String> visitedVertex = new HashSet<>();
    HashSet<String> seenEdges = new HashSet<>();

    public void calcQI(Vertex root, HashMap<Integer, Vertex> queryGraphNodes) {
        stack.push(root);
        Vertex prev = null;
        while (!stack.isEmpty())    {
            Vertex temp = stack.pop();
            if(!visitedVertex.contains(temp.label)) {
                if(prev != null)    {
                    String s = prev.label + temp.label;
                    char[] arr = s.toCharArray();
                    Arrays.sort(arr);
                    s = arr[0] + "-" + arr[1];
                    seenEdges.add(s);

                }
                visitedVertex.add(temp.label);

                for(int neighborID: queryGraphNodes.get(temp.id1).edges.keySet())   {
                    Vertex neighbor = queryGraphNodes.get(neighborID);
                    if(!visitedVertex.contains(neighbor.label)) {
                        neighbor.parent = temp;
                        stack.push(neighbor);
                    }

                }

            }
            prev = temp;
        }

    }
}
