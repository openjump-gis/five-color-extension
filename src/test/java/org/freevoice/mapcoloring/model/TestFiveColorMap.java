package org.freevoice.mapcoloring.model;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.workbench.model.Layer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Created by lreeder on 2/20/14.
 */
public class TestFiveColorMap
{
   private static final String ATTR_NAME = "NAME";
   private static final String ATTR_GEOM = "GEOMETRY";
   GeometryFactory geometryFactory = new GeometryFactory();

   @Before
   public void setUp()
   {
   }

   @After
   public void tearDown()
   {
   }


   @Test
   public void testColorLayer()
   {
      Layer layer = new Layer();

      FeatureSchema featureSchema = new FeatureSchema();
      featureSchema.addAttribute(ATTR_NAME, AttributeType.STRING);
      featureSchema.addAttribute(ATTR_GEOM, AttributeType.GEOMETRY);

      FeatureCollection featureCollection = new FeatureDataset(featureSchema);

      featureCollection.add(makeFeature(featureSchema, "A", makeSquare(0,0,1,1)));
      featureCollection.add(makeFeature(featureSchema, "B", makeSquare(0,1,1,2)));
      featureCollection.add(makeFeature(featureSchema, "C", makeSquare(0,2,1,3)));
      featureCollection.add(makeFeature(featureSchema, "F", makeSquare(1,1,2,2)));
      featureCollection.add(makeFeature(featureSchema, "D", makeSquare(2,0,3,1)));
      featureCollection.add(makeFeature(featureSchema, "E", makeSquare(2,1,3,2)));

      layer.setFeatureCollection(featureCollection);

      FiveColorMap<Feature> nc = new FiveColorMap<>();

      FeatureCollection fc = nc.colorMap(layer, true);

      List<Feature> featureList = fc.getFeatures();

      verifyFeatureColors(featureList);
   }

   @Test
   public void testTouchesAtPoint()
   {
      Layer layer = new Layer();

      FeatureSchema featureSchema = new FeatureSchema();
      featureSchema.addAttribute(ATTR_NAME, AttributeType.STRING);
      featureSchema.addAttribute(ATTR_GEOM, AttributeType.GEOMETRY);

      FeatureCollection featureCollection = new FeatureDataset(featureSchema);

      //creates a hexagon with six slices
      double sixtydeg = Math.toRadians(60);
      featureCollection.add(makeFeature(featureSchema, "A", makeTriangle(0,0, -1,0, -1*Math.cos(sixtydeg), Math.sin(sixtydeg))));
      featureCollection.add(makeFeature(featureSchema, "B", makeTriangle(0, 0, -1 * Math.cos(sixtydeg), Math.sin(sixtydeg), Math.cos(sixtydeg), Math.sin(sixtydeg))));
      featureCollection.add(makeFeature(featureSchema, "C", makeTriangle(0,0, 1,0, Math.cos(sixtydeg), Math.sin(sixtydeg))));
      featureCollection.add(makeFeature(featureSchema, "D", makeTriangle(0,0, 1,0, Math.cos(sixtydeg), -1*Math.sin(sixtydeg))));
      featureCollection.add(makeFeature(featureSchema, "E", makeTriangle(0,0, Math.cos(sixtydeg), -1*Math.sin(sixtydeg), -1*Math.cos(sixtydeg), -1*Math.sin(sixtydeg))));
      featureCollection.add(makeFeature(featureSchema, "F", makeTriangle(0,0, -1,0, -1*Math.cos(sixtydeg), -1*Math.sin(sixtydeg))));

      layer.setFeatureCollection(featureCollection);

      FiveColorMap<Feature> nc = new FiveColorMap<>();

      FeatureCollection fc = nc.colorMap(layer, false);

      List<Feature> featureList = fc.getFeatures();

      verifyFeatureColors(featureList);

      Map<String,String> nameAndColor =  getFeatureColors(featureList);

      //verify no adjacent pizza slices have the same color
      assertNotEquals(nameAndColor.get("A"), nameAndColor.get("B"));
      assertNotEquals(nameAndColor.get("B"), nameAndColor.get("C"));
      assertNotEquals(nameAndColor.get("C"), nameAndColor.get("D"));
      assertNotEquals(nameAndColor.get("D"), nameAndColor.get("E"));
      assertNotEquals(nameAndColor.get("E"), nameAndColor.get("F"));
      assertNotEquals(nameAndColor.get("F"), nameAndColor.get("A"));

   }

   private Map<String,String> getFeatureColors(List<Feature> featureList)
   {

      Map<String, String> colorMap = new ConcurrentHashMap<>();
      for (Feature feature : featureList)
      {
         String color = (String) feature.getAttribute(FiveColorMap.COLOR_ATTRIBUTE);
         String name = (String) feature.getAttribute(ATTR_NAME);

         colorMap.put(name, color);
      }

      return colorMap;

   }

   private void verifyFeatureColors(List<Feature> featureList)
   {

      for (Feature feature : featureList)
      {

         System.out.println(feature.getGeometry());

         String color = (String) feature.getAttribute(FiveColorMap.COLOR_ATTRIBUTE);

//         assertNotNull("Color for feature " + feature.getID() + " is null", color);
//         assertNotEquals("", color);
      }

   }

   private Feature makeFeature(FeatureSchema featureSchema, String name, Geometry geometry)
   {

      Feature featureA = new BasicFeature(featureSchema);
      featureA.setAttribute(ATTR_NAME, name);
      featureA.setAttribute(ATTR_GEOM, geometry);
      return featureA;
   }

   private Geometry makeTriangle(double x1, double y1, double x2, double y2, double x3, double y3)
   {
      return geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[]{
            new Coordinate(x1, y1),
            new Coordinate(x2, y2),
            new Coordinate(x3, y3),
            new Coordinate(x1, y1),
      }
      ),  null);
   }

   private Geometry makeSquare(double xMin, double yMin, double xMax, double yMax)
   {
      return geometryFactory.createPolygon(geometryFactory.createLinearRing(new Coordinate[]{
            new Coordinate(xMin, yMin),
            new Coordinate(xMax, yMin),
            new Coordinate(xMax, yMax),
            new Coordinate(xMin, yMax),
            new Coordinate(xMin, yMin),
      }
      ),  null);
   }
}
