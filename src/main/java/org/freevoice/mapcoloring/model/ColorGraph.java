package org.freevoice.mapcoloring.model;

import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Created by lreeder on 2/9/14.
 */
public class ColorGraph<V>
{

   public static final int NUMBER_OF_COLORS = 5;
   private ColoringProgressListener progressListener = null;

   private boolean isCancelled = false;
   private final Stack<V> removedVertices = new Stack<>();

   private SimpleGraph<V, String> workingGraph;


   private final GraphUtil graphUtil = new GraphUtil();

   /**
    * Adds a customer progress listener.
    *
    * @param progressListener a ColoringProgressListener
    */
   public void setProgressListener(ColoringProgressListener progressListener)
   {
      this.progressListener = progressListener;
   }

   /**
    * Stops calculation of the five colormap.
    */
   public void cancel()
   {
      isCancelled = true;
   }

   /**
    * Returns a map of vertices with associated color
    *
    * @param graph Graph to color.
    * @return Map of each vertex with assigned colors.  Colors will be in range 1-5.  A vertex may
    * not be in this map if the graph couldn't be properly colored, e.g. the graph was not planar.
    */
   public Map<V, String> colorGraph(SimpleGraph<V, String> graph)
   {
      Map<V, Set<V>> neighborMap = new HashMap<>();
      //copy all edges before we decompose the graph
      NeighborCache<V,String> neighborIndex = new NeighborCache<>(graph);
      for (V s : graph.vertexSet())
      {
         neighborMap.put(s, new HashSet<>(neighborIndex.neighborsOf(s)));
      }
      removedVertices.clear();

      workingGraph = graph;
      decomposeGraph();

      return assignColors(neighborMap);
   }

   private Map<V,String> assignColors(Map<V, Set<V>> neighborMap)
   {
      SimpleGraph<ColoredVertex<V>, ColoredVertex<V>> graph =
          new SimpleGraph(ColoredVertex.class);

      Map<V, ColoredVertex<V>> addedVertices = new HashMap<>();

      //re-add removed vertices, coloring each one
      int previousColor = 0;
      while (! removedVertices.empty())
      {
         V vertex = removedVertices.pop();

         Set<V> removedNeighbors = neighborMap.get(vertex);

         List<Integer> colors = createShuffledColorSet(previousColor);

         //iterate over edges currently added to graph and
         //remove the colors for each one
         for (V neighbor : removedNeighbors)
         {
            ColoredVertex<V> addedEdge = addedVertices.get(neighbor);

            if(addedEdge != null)
            {
               int matchingIndex = colors.indexOf(addedEdge.getColor());
               if(matchingIndex >= 0)
               {
                 colors.remove(matchingIndex);
               }
            }
         }

         //shouldn't happen if the algorithm is working correctly
         if(colors.size() <= 0)
         {
            throw new IllegalStateException("No colors left");
         }

         ColoredVertex<V> newVertex = new ColoredVertex<>(vertex);
         previousColor = colors.get(0);
         newVertex.setColor(previousColor);

         addedVertices.put(newVertex.getOriginal(), newVertex);
         graph.addVertex(newVertex);

      }

      Set<ColoredVertex<V>> coloredSet = graph.vertexSet();

      Map<V, String> colorMap = new HashMap<>(coloredSet.size());
      for (ColoredVertex<V> coloredVertex : coloredSet)
      {
         //add one so that colors are ones-based
         colorMap.put(coloredVertex.getOriginal(), String.valueOf(coloredVertex.getColor() + 1));
      }

      return colorMap;

   }

   //Recursive method that moves nodes from the graph
   private void decomposeGraph()
   {
      //graph must be planar for algorithm to work properly
      if(! graphUtil.isPlanar(workingGraph))
      {
         //fixme - communicate to Jump somehow
         //System.err.println("GRAPH IS NOT PLANAR");
         throw new IllegalStateException("GRAPH IS NOT PLANAR");
      }

      //create a copy of the starting vertices since the actual
      // graph vertexset will be changing as the graph is decomposed.
      Set<V> originalVertices = new HashSet<>(workingGraph.vertexSet());

      Set<V> degreeOfFiveSet = new HashSet<>();

      NeighborCache<V, String> neigborIndex = new NeighborCache<>(workingGraph);

      if(originalVertices.size() != 0)
      {
         boolean decomposed = false;
         for (V s : originalVertices)
         {
            int degree = workingGraph.degreeOf(s);
            if (degree < 5)
            {
               removedVertices.push(s);
               workingGraph.removeVertex(s);
               decomposed = true;
               decomposeGraph();
               break;
            }
            else if (degree == 5)
            {
               //note this vertex for later use if there a no
               //vertices of degree < 5
               degreeOfFiveSet.add(s);
            }
         }

         if (!decomposed && graphUtil.isPlanar(workingGraph))
         {
            decomposed = handleGraphWithOnlyDegreeOfFive(workingGraph, degreeOfFiveSet, neigborIndex);
         }

         if (!decomposed && graphUtil.isPlanar(workingGraph))
         {
            throw new IllegalStateException("Graph decomposition failed.");
         }
      }
   }

   private boolean handleGraphWithOnlyDegreeOfFive(SimpleGraph<V, String> graph,
                                                   Set<V> degreeOfFive, NeighborCache<V, String> neigborIndex)
   {
      V n1 = null;
      V n2 = null;
      boolean decomposed = false;

      for (V n : degreeOfFive)
      {
         Set<V> neighbors = neigborIndex.neighborsOf(n);

         List<V> candidates = new ArrayList<>();
         for (V neighbor : neighbors)
         {
            if (graph.degreeOf(neighbor) <= 7)
            {
               candidates.add(neighbor);
            }
         }

         //find two candidates that are not neighbors
         ijloop:
         for (V i : candidates)
         {
            for (V j : candidates)
            {
               if(! i.equals(j) && ! neigborIndex.neighborsOf(i).contains(j))
               {
                  n1 = i;
                  n2 = j;
                  break ijloop; // fix on 2021-05-29 by mmichaud
               }
            }
         }

         //remove n from the graph
         removedVertices.push(n);
         graph.removeVertex(n);

         // merge n1 and n2 into a new node (just reuse n1)
         // and add their neighbors to the new node
         //add all of n2s neighbors to n1
         List<V> n2Neighbors = neigborIndex.neighborListOf(n2);
         for (V n2Neighbor : n2Neighbors)
         {
            if(! neigborIndex.neighborsOf(n1).contains(n2Neighbor))
            {
               graph.addEdge(n1, n2Neighbor);
               graph.addEdge(n2Neighbor, n1);
            }
         }
         graph.removeVertex(n2);
         removedVertices.push(n2);

         decomposed = true;
         decomposeGraph();
         break;
      }
      return decomposed;
   }



   private void updateProgressListener(int progress)
   {
      if(progressListener != null)
      {
         progressListener.update(progress);
      }
   }

   private List<Integer> createShuffledColorSet(int lastColorIndex)
   {
       List<Integer> colorSet = new ArrayList<>();

      int totalColors = NUMBER_OF_COLORS;

      //shuffle so that the last color is the last one
      //on the list
      //it would be easier to randomly shuffle,
      //but using this algorithm causes reproducible coloring
      for(int i = 0; i < totalColors; i++)
      {
         int color = (lastColorIndex + 1 + i ) % totalColors;
         colorSet.add(color);
      }

      return colorSet;
   }

}
