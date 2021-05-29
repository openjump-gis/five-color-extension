package org.freevoice.mapcoloring.model;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.SimpleGraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;

/**
 * Created by lreeder on 4/19/14.
 */
public class GraphUtil
{
   public boolean isPlanar(SimpleGraph graph)
   {
      //Planarity criteria: Theorem 1. If v ≥ 3 then e ≤ 3v − 6;
      boolean isPlanar = true;
      int edgeCount = graph.edgeSet().size();
      int vertexCount = graph.vertexSet().size();

      if (vertexCount >= 3)
      {
         isPlanar = edgeCount <= 3 * vertexCount - 6;
      }

      return isPlanar;
   }


   public String printGraph(SimpleGraph graph)
   {
      Set vertexes = graph.vertexSet();

      NeighborCache nIdx = new NeighborCache(graph);
      StringBuilder builder = new StringBuilder();
      for (Object vertex : vertexes)
      {
         builder.append("V: " + vertex + " N: ");
         Set neighbors = nIdx.neighborsOf(vertex);
         for (Object neighbor : neighbors)
         {
            builder.append(" " + neighbor);
         }
         builder.append("\n");
      }
      return builder.toString();
   }

   public void writeGraph(SimpleGraph graph, String graphName) throws IOException
   {
      FileOutputStream fout = new FileOutputStream(graphName);
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(graph);

   }


}
