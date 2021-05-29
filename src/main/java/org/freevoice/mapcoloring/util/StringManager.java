package org.freevoice.mapcoloring.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: lreeder
 * Date: 4/22/13
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringManager
{
   static ResourceBundle bundle;

   public static String getString(String key)
   {

      if(bundle == null)
      {
         bundle = ResourceBundle.getBundle("/org/freevoice/mapcoloring/mapcolorstrings");
      }

      if(bundle == null)
      {
         return "?" + key + "?";
      }
      else
      {
         return bundle.getString(key);
      }
   }

   public static String getFormattedString(String key, Object[] parameters)
   {
      MessageFormat formatter = new MessageFormat("");
      formatter.applyPattern(getString(key));
      return formatter.format(parameters);
   }
}
