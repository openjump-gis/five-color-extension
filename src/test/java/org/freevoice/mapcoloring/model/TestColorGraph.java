package org.freevoice.mapcoloring.model;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.SimpleGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by lreeder on 2/20/14.
 */
public class TestColorGraph
{
   @Before
   public void setUp()
   {
   }

   @After
   public void tearDown()
   {
   }

   @Test
   public void testColorGraph()
   {
      SimpleGraph<String, String> simpleGraph = new SimpleGraph<>(String.class);

      simpleGraph.addVertex("A");
      simpleGraph.addVertex("B");
      simpleGraph.addVertex("C");
      simpleGraph.addVertex("D");
      simpleGraph.addVertex("E");
      simpleGraph.addVertex("F");

      simpleGraph.addEdge("A", "F", "A-F");
      simpleGraph.addEdge("B", "F", "B-F");
      simpleGraph.addEdge("C", "F", "C-F");
      simpleGraph.addEdge("D", "F", "D-F");
      simpleGraph.addEdge("E", "F", "E-F");

      simpleGraph.addEdge("A", "E", "A-E");
      simpleGraph.addEdge("A", "C", "A-C");
      simpleGraph.addEdge("C", "D", "C-D");
      simpleGraph.addEdge("D", "B", "D-B");
      simpleGraph.addEdge("B", "E", "B-E");

      ColorGraph<String> nc = new ColorGraph<>();

      Map<String, String> colorMap = nc.colorGraph(simpleGraph);

      verifyColors(simpleGraph, colorMap);

      SimpleGraph<String, String> simpleGraph2 = new SimpleGraph<>(String.class);

      simpleGraph2.addVertex("A");
      simpleGraph2.addVertex("B");
      simpleGraph2.addVertex("C");
      simpleGraph2.addVertex("D");
      simpleGraph2.addVertex("E");
      simpleGraph2.addVertex("F");

      simpleGraph2.addEdge("A", "F", "A-F");
      simpleGraph2.addEdge("B", "F", "B-F");
      simpleGraph2.addEdge("C", "F", "C-F");
      simpleGraph2.addEdge("D", "F", "D-F");
      simpleGraph2.addEdge("E", "F", "E-F");

      simpleGraph2.addEdge("A", "E", "A-E");
      simpleGraph2.addEdge("A", "C", "A-C");
      simpleGraph2.addEdge("C", "D", "C-D");
      simpleGraph2.addEdge("D", "B", "D-B");
      simpleGraph2.addEdge("B", "E", "B-E");
      simpleGraph2.addEdge("C", "B", "C-B");

      System.out.println("");

      colorMap = nc.colorGraph(simpleGraph2);

      verifyColors(simpleGraph2, colorMap);
   }

   @Test
   public void testTwoNodesWithDegreeFive()
   {
      SimpleGraph<String, String> simpleGraph = new SimpleGraph<>(String.class);

      simpleGraph.addVertex("A");
      simpleGraph.addVertex("B");
      simpleGraph.addVertex("C");
      simpleGraph.addVertex("D");
      simpleGraph.addVertex("E");
      simpleGraph.addVertex("F");


      simpleGraph.addEdge("A", "B", "A-B");
      simpleGraph.addEdge("A", "C", "A-C");
      simpleGraph.addEdge("A", "D", "A-D");
      simpleGraph.addEdge("A", "E", "A-E");
      simpleGraph.addEdge("A", "F", "A-F");

      simpleGraph.addEdge("B", "A", "B-A"); //DUP
      simpleGraph.addEdge("B", "C", "B-C");
      simpleGraph.addEdge("B", "D", "B-D");
      simpleGraph.addEdge("B", "E", "B-E");
      simpleGraph.addEdge("B", "F", "B-F");

      simpleGraph.addEdge("C", "A", "C-A"); //DUP
      simpleGraph.addEdge("C", "B", "C-B"); //DUP
      simpleGraph.addEdge("C", "D", "C-D");

      simpleGraph.addEdge("D", "A", "D-A"); //DUP
      simpleGraph.addEdge("D", "B", "D-B"); //DUP
      simpleGraph.addEdge("D", "C", "D-C"); //DUP
      simpleGraph.addEdge("D", "F", "D-F");

      simpleGraph.addEdge("E", "A", "E-A"); //DUP
      simpleGraph.addEdge("E", "B", "E-B"); //DUP
      simpleGraph.addEdge("E", "F", "E-F");

      simpleGraph.addEdge("F", "A", "F-A"); //DUP
      simpleGraph.addEdge("F", "B", "F-B"); //DUP
      simpleGraph.addEdge("F", "D", "F-D"); //DUP
      simpleGraph.addEdge("F", "E", "F-E");


      ColorGraph<String> nc = new ColorGraph<>();

      Map<String, String> colorMap = nc.colorGraph(simpleGraph);

      verifyColors(simpleGraph, colorMap);
   }

   @Test
   public void testAllNodesHaveDegreeFive()
   {
      SimpleGraph<String, String> simpleGraph = new SimpleGraph<>(String.class);

      //set up every node to have a degree of five.
      //See http://www.devx.com/dotnet/Article/32927/0/page/2 for
      //a nice diagram of this graph.
      simpleGraph.addVertex("A");
      simpleGraph.addVertex("B");
      simpleGraph.addVertex("C");
      simpleGraph.addVertex("D");
      simpleGraph.addVertex("E");
      simpleGraph.addVertex("F");
      simpleGraph.addVertex("G");
      simpleGraph.addVertex("H");
      simpleGraph.addVertex("I");
      simpleGraph.addVertex("J");
      simpleGraph.addVertex("K");
      simpleGraph.addVertex("L");


      addEdgeList("A", "BCDEF", simpleGraph);
      addEdgeList("B", "GHFAC", simpleGraph);
      addEdgeList("C", "HIDAB", simpleGraph);
      addEdgeList("D", "CAIEJ", simpleGraph);
      addEdgeList("E", "ADFKJ", simpleGraph);
      addEdgeList("F", "ABGKE", simpleGraph);
      addEdgeList("G", "LHBFK", simpleGraph);
      addEdgeList("H", "LGBCI", simpleGraph);
      addEdgeList("I", "LHCDJ", simpleGraph);
      addEdgeList("J", "KEDIL", simpleGraph);
      addEdgeList("K", "GFEJL", simpleGraph);
      addEdgeList("L", "GKIJH", simpleGraph);


      ColorGraph<String> nc = new ColorGraph<>();

      Map<String, String> colorMap = nc.colorGraph(simpleGraph);

      verifyColors(simpleGraph, colorMap);
   }

   private void addEdge(String v1, String v2, SimpleGraph<String, String> simpleGraph)
   {
      simpleGraph.addEdge(v1, v2, v1 + "-" + v2);
      simpleGraph.addEdge(v2, v1, v2 + "-" + v1);
   }

   private void addEdgeList(String v1, String edgeList, SimpleGraph<String, String> simpleGraph)
   {
      char[] edges = edgeList.toCharArray();

      for (char edge : edges)
      {
         String edgeString = Character.toString(edge);
         addEdge(v1, edgeString, simpleGraph);
      }
   }

   @Test
   @Ignore
   public void testBigGraph() throws Exception
   {
      FileInputStream fin = new FileInputStream("/home/lreeder/psap.ser");
      ObjectInputStream oos = new ObjectInputStream(fin);
      SimpleGraph simpleGraph = (SimpleGraph) oos.readObject();

      ColorGraph<Integer> nc = new ColorGraph<Integer>();

      Map<Integer, String> colorMap = nc.colorGraph(simpleGraph);

      verifyColors(simpleGraph, colorMap);
   }


   private <V> void verifyColors(SimpleGraph<V, String> graph, Map<V, String> coloredMap)
   {
      NeighborCache neighborIndex = new NeighborCache(graph);

      Set<V> vertices = graph.vertexSet();

      for (V vertex : vertices)
      {
         String color = coloredMap.get(vertex);

         //make sure a color was assigned
         assertNotNull("Vertex " + vertex + " has no color", color);

         assertNotEquals("", color);

         Set<String> neighbors = neighborIndex.neighborsOf(vertex);

         for (String neighbor : neighbors)
         {
            String neighborColor = coloredMap.get(neighbor);
            assertNotSame("Found neighbor with same color", color, neighborColor);
         }

      }

   }
}
