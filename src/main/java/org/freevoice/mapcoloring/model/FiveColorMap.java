package org.freevoice.mapcoloring.model;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.workbench.model.Layer;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provides map coloring using five color algorithm for an OpenJump layer.
 * <p/>
 * Created by lreeder on 2/9/14.
 */
public class FiveColorMap<V>
{

   public final static String COLOR_ATTRIBUTE = "autoclr";
   private ColoringProgressListener progressListener = null;

   private boolean isCancelled = false;

   Logger logger = LoggerFactory.getLogger(FiveColorMap.class);

   /**
    * Adds a customer progress listener.
    *
    * @param progressListener a progress listener
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


   public FeatureCollection colorMap(Layer selectedLayer, boolean bufferForIntersection)
   {

      logger.info("start map coloring");
      long startTime = System.currentTimeMillis();
      @SuppressWarnings("unchecked")
      List<Feature> featureList = selectedLayer.getFeatureCollectionWrapper().getFeatures();

      SpatialIndex spatIdx = new STRtree();

      //create spatidx for quick searches
      for (Feature feature : featureList)
      {
         spatIdx.insert(feature.getGeometry().getEnvelopeInternal(), feature);
      }

      SimpleGraph<Integer, String> mapGraph = new SimpleGraph<>(String.class);

      logger.info("Start generating graph model for features");
      long graphStart = System.currentTimeMillis();

      //find minimum dimension for buffering
      double minDimension = Double.MAX_VALUE;
      if (bufferForIntersection)
      {
         logger.info("Determining min dimension");
         long minDimStart = System.currentTimeMillis();
         for (Feature feature : featureList)
         {
            double width = feature.getGeometry().getEnvelopeInternal().getWidth();
            double height = feature.getGeometry().getEnvelopeInternal().getHeight();

            minDimension = Math.min(minDimension, width);
            minDimension = Math.min(minDimension, height);
         }
         logger.info("Found minimum dimension " + minDimension + " in " + (System.currentTimeMillis() - minDimStart));
      }

      int numFeatures = featureList.size();

      Map<Integer, Set<Integer>> revIntersections = new HashMap<>();

      //main work loop: buffer if necessary, check for intersections
      //and create the graph
      for (int i = 0; i < numFeatures && !isCancelled; i++)
      {
         Feature feature = featureList.get(i);
         long featureStart = System.currentTimeMillis();

         long eventStart = System.currentTimeMillis();

         Geometry bufferedGeometry = feature.getGeometry();
         if (bufferForIntersection)
         {
            //buffer feature out to 2% of its minimum dimension
            bufferedGeometry = feature.getGeometry().buffer(minDimension / 50);
            logger.info("Buffered feature " + feature.getID() + " in " + (System.currentTimeMillis() - eventStart));
         }


         int mainVertexId = feature.getID();

         mapGraph.addVertex(mainVertexId);
         eventStart = System.currentTimeMillis();

         //use spatial idx to find initial intersection set
         //Since this is intersection with bounding box, the actual intersection
         //set will be smaller, but this removes the need to test every
         //feature
         List<Feature> maybeIntersecting = spatIdx.query(bufferedGeometry.getEnvelopeInternal());
         logger.info("Found spatial index intersections for " + feature.getID() + " in " + (System.currentTimeMillis() - eventStart));

         eventStart = System.currentTimeMillis();
         for (Feature maybeIntersects : (List<Feature>) maybeIntersecting)
         {
            int maybeId = maybeIntersects.getID();

            boolean intersects = false;


            //Ignore the original feature id which will always
            // be returned when intersected
            //against its own buffered geometry
            if (maybeId == mainVertexId)
            {
               //no-op, just making it obvious
               intersects = false;
            }
            else
            {
               //intersection is associative,  check neighbor map and see if we
               // already intersected this neighbor
               // without going through the more expensive spatial checks below
               Set<Integer> revNeighbor = revIntersections.get(maybeId);
               if (revNeighbor != null)
               {
                  if (revNeighbor.contains(mainVertexId))
                  {
                     intersects = true;
                  }
               }

               Geometry intersectingGeometry = maybeIntersects.getGeometry();

               //now check for real intersection of neighboring features
               // against the buffered feature geometry.
               //
               //I'm using the intersection matrix here as an extra check because
               //intersects returns true even when only one point intersects,
               //and it's OK to color if the regions only touch at one point
               IntersectionMatrix imatrix = intersectingGeometry.relate(bufferedGeometry);
               final String DE9IM_INTERSECTS_AT_POINT = "FF2F01212";
               if (! intersects && imatrix.isIntersects() && !imatrix.matches(DE9IM_INTERSECTS_AT_POINT))
               {
                  intersects = true;
               }
            }

            if (intersects)
            {
               //update map for reverse neighbor lookup
               Set<Integer> revNeighbor = revIntersections.get(maybeId);
               if (revNeighbor == null)
               {
                  revNeighbor = new HashSet<>();
                  revIntersections.put(maybeId, revNeighbor);
               }
               revNeighbor.add(mainVertexId);


               //update adjacency graph.
               if (!mapGraph.containsVertex(maybeId))
               {
                  mapGraph.addVertex(maybeId);
               }

               mapGraph.addEdge(
                     mainVertexId, maybeId,
                     mainVertexId + "-" + maybeId
               );
            }

         }
         logger.info("Found real intersections for " + feature.getID() + " in " + (System.currentTimeMillis() - eventStart));

         logger.info("Completed feature intersection model for feature " + feature.getID() + " in " + (System.currentTimeMillis() - featureStart));

         updateProgressListener(((i + 1) * 100) / numFeatures);
      }

      logger.info("Completed graph model in " + (System.currentTimeMillis() - graphStart));

      ColorGraph<Integer> innerColorMap = new ColorGraph<>();
      logger.info("Coloring graph");

      long colorStart = System.currentTimeMillis();

      Map<Integer, String> colorMap = innerColorMap.colorGraph(mapGraph);
      logger.info("Total time coloring graph: " + (System.currentTimeMillis() - colorStart));

      long copyStart = System.currentTimeMillis();
      FeatureCollection featureCollection = addColorToFeatures(selectedLayer, featureList, colorMap);
      logger.info("Total time copying layer: " + (System.currentTimeMillis() - copyStart));

      logger.info("Total time for map coloring: " + (System.currentTimeMillis() - startTime));

      //show progress at 100 - due to int truncation it might not quite be there
      updateProgressListener(100);

      return featureCollection;
   }


   //Copies feature collection, adding a color attribute for each feature
   private FeatureCollection addColorToFeatures(Layer selectedLayer, List<Feature> featureList, Map<Integer, String> colorMap)
   {
      FeatureSchema newSchema = selectedLayer.getFeatureCollectionWrapper().getFeatureSchema().clone();
      newSchema.addAttribute(COLOR_ATTRIBUTE, AttributeType.STRING);

      FeatureCollection featureCollection = new FeatureDataset(newSchema);
      for (Feature feature : featureList)
      {
         Feature copy = new BasicFeature(newSchema);

         Object[] currAttributes = feature.getAttributes();

         for (int i = 0; i < currAttributes.length; i++)
         {
            copy.setAttribute(i, currAttributes[i]);
         }

         copy.setAttribute(COLOR_ATTRIBUTE, colorMap.get(feature.getID()));
         featureCollection.add(copy);
      }
      return featureCollection;
   }

   private void updateProgressListener(int progress)
   {
      if (progressListener != null)
      {
         progressListener.update(progress);
      }
   }

   private Set<String> createColorSet()
   {
      Set<String> colorSet = new HashSet<>();

      for (int i = 1; i <= ColorGraph.NUMBER_OF_COLORS; i++)
      {
         colorSet.add(String.valueOf(i));
      }

      return colorSet;
   }

}
