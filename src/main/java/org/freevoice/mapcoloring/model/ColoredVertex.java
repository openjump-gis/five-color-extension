package org.freevoice.mapcoloring.model;

/**
 * Created by lreeder on 2/14/14.
 */

public class ColoredVertex<V>
{

   private final V original;
   private int color;

   public ColoredVertex(V vertex)
   {
      this.original = vertex;
   }

   public V getOriginal()
   {
      return original;
   }

   public int getColor()
   {
      return color;
   }

   public void setColor(int color)
   {
      this.color = color;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      ColoredVertex that = (ColoredVertex) o;

      if (color != that.color)
      {
         return false;
      }
      if (!original.equals(that.original))
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      return original.hashCode();
   }
}
