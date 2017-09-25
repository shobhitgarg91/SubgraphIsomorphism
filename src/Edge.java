/**
 * Created by shobhitgarg on 9/15/17.
 */
public class Edge {

   int u;
   int v;
   int degU; int degV;
   double weight;
   int count = 1;
    boolean foundEveryWhere = true;



    Edge(int u, int v, double weight)   {
    this.u = u;
    this.v = v;
    this.weight = weight;
}

public boolean equals(Edge edge)    {
 return ((u == edge.u) && (v == edge.v));
}

}
