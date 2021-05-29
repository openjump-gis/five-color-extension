/*
 *
 *  The JUMP DB Query Plugin is Copyright (C) 2007  Larry Reeder
 *  JUMP is Copyright (C) 2003 Vivid Solutions
 *
 *  This file is part of the JUMP DB Query Plugin.
 *
 *  The JUMP DB Query Plugin is free software; you can redistribute it and/or
 *  modify it under the terms of the Lesser GNU General Public License as
 *  published *  by the Free Software Foundation; either version 3 of the
 *  License, or  (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freevoice.mapcoloring.ui;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import org.freevoice.mapcoloring.model.ColorGraph;
import org.freevoice.mapcoloring.model.ColoringProgressListener;
import org.freevoice.mapcoloring.model.FiveColorMap;
import org.freevoice.mapcoloring.util.StringManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FiveColorDialog extends JDialog implements ActionListener, ColoringProgressListener
{

   public static final String CANCEL_ACTION_COMMAND = "CancelAction";
   public static final String RUN_ACTION_COMMAND = "RunAction";

   private ColorTask task = null;
   private final PlugInContext context;

   public JProgressBar progressBar = null;

   JCheckBox bufferCheck = new JCheckBox();

   private final JFrame parent;
   private final String title;

   public static void main(String[] args)
   {
      FiveColorDialog fcd = new FiveColorDialog(
            null,
            new JFrame(),
            "test"
      );

      fcd.setVisible(true);
   }

   public FiveColorDialog(
         PlugInContext context,
         JFrame parent,
         String title)
   {
      this.title = title;
      this.parent = parent;
      this.context = context;
      setModalityType(ModalityType.APPLICATION_MODAL);
      initUICode();
   }


   private void initUICode()
   {

      setTitle(this.title);
      this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new GridBagLayout());


      mainPanel.add(
            bufferCheck, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 2, 2
      )
      );
      String bufferToolTip = StringManager.getString("mapcoloring.buffer.tooltip");
      bufferCheck.setToolTipText(bufferToolTip);

      JLabel bufferLabel = new JLabel(StringManager.getString("mapcoloring.bufferquestion"));
      bufferLabel.setToolTipText(bufferToolTip);

      mainPanel.add(
            bufferLabel, new GridBagConstraints(
            1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 2
      )
      );

      JPanel progressPanel = new JPanel();
      progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
      progressPanel.setBackground(Color.red);
      progressPanel.setForeground(Color.red);


      progressBar = new JProgressBar(0, 100);
      progressBar.setSize(20, 20);
      progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      progressPanel.add(progressBar);

      progressBar.setVisible(true);


      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

      JButton runButton = new JButton();
      runButton.setText(StringManager.getString("mapcoloring.run"));
      runButton.setMnemonic('R');
      runButton.setActionCommand(RUN_ACTION_COMMAND);
      runButton.addActionListener(this);
      buttonPanel.add(runButton);


      JButton cancelButton = new JButton();
      cancelButton.setText(StringManager.getString("mapcoloring.cancel"));
      cancelButton.setMnemonic('C');
      cancelButton.setActionCommand(CANCEL_ACTION_COMMAND);
      cancelButton.addActionListener(this);
      buttonPanel.add(cancelButton);

      mainPanel.add(
            buttonPanel, new GridBagConstraints(
            0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 2
      )
      );

      this.getContentPane().add(mainPanel);
      this.getContentPane().add(progressPanel);

      pack();

      //call after pack to center this on the parent
      this.setLocationRelativeTo(parent);
   }

   /**
    * Handle the action when the user clicks on the "OK" or "Cancel" button.
    *
    * @param e an action event
    */
   public void actionPerformed(ActionEvent e)
   {

      if (CANCEL_ACTION_COMMAND.equals(e.getActionCommand()))
      {
         if (task != null)
         {
            task.cancel();
            //null out task to mark as GC-ready
            task = null;
         }
         this.setVisible(false);
      }
      else if (RUN_ACTION_COMMAND.equals(e.getActionCommand()))
      {
         if (task == null || task.isDone())
         {
            progressBar.setValue(0);

            task = new ColorTask(parent, bufferCheck.isSelected());
            task.execute();
         }
      }
   }


   @Override
   public void update(int percentDone, String message)
   {

   }

   @Override
   public void update(int percentDone)
   {
      progressBar.setValue(percentDone);
   }

   class ColorTask extends SwingWorker<Void, Void>
   {

      private final FiveColorMap<Feature> colorMap = new FiveColorMap<>();
      private final boolean bufferForIntersection;
      private final JFrame parentFrame;

      public ColorTask(JFrame parentFrame, boolean bufferForIntersection)
      {
         this.bufferForIntersection = bufferForIntersection;
         this.parentFrame = parentFrame;
      }


      @Override
      public Void doInBackground()
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         Layer[] selectedLayers = context.getSelectedLayers();

         if (selectedLayers.length <= 0)
         {
            JOptionPane.showMessageDialog(
                  parentFrame,
                  StringManager.getString("mapcoloring.selectlayer"),
                  StringManager.getString("mapcoloring.error"),
                  JOptionPane.ERROR_MESSAGE
            );
         }

         try
         {

            for (Layer selectedLayer : selectedLayers)
            {
               colorMap.setProgressListener(FiveColorDialog.this);
               FeatureCollection featureCollection = colorMap.colorMap(selectedLayer, bufferForIntersection);
               if (!isCancelled())
               {
                  String coloredName = selectedLayer.getName() + "_" + StringManager.getString("mapcoloring.suffix");
                  Layer coloredLayer = context.addLayer(StandardCategoryNames.SYSTEM, coloredName, featureCollection);
                  coloredLayer.getBasicStyle().setEnabled(false);
                  ColorThemingStyle themedStyle = createColorThemingStyle();
                  coloredLayer.addStyle(themedStyle);
               }
               else
               {
                  break;
               }
            }
         }
         catch (Exception exc)
         {

            exc.printStackTrace();
            JOptionPane.showMessageDialog(
                  parentFrame,
                  exc.getMessage(),
                  StringManager.getString("mapcoloring.error"),
                  JOptionPane.ERROR_MESSAGE
            );

         }
         catch (StackOverflowError se)
         {
            JOptionPane.showMessageDialog(
                  parentFrame,
                  "Stack overflow.  Try increasing JVM thread stack size",
                  StringManager.getString("mapcoloring.error"),
                  JOptionPane.ERROR_MESSAGE
            );
         }
         catch (Error e)
         {
            e.printStackTrace();
            throw e;
         }

         return null;
      }

      private ColorThemingStyle createColorThemingStyle()
      {
         ColorScheme dark2 = ColorScheme.create("Dark 2 (ColorBrewer)");

         Map<Object,BasicStyle> attributeToStyleMap = new HashMap<>();
         for(int i = 1; i <= ColorGraph.NUMBER_OF_COLORS; i++)
         {
            attributeToStyleMap.put(Integer.toString(i), new BasicStyle(dark2.next()));
         }

         BasicStyle defaultStyle = new BasicStyle(Color.white);
         ColorThemingStyle themeStyle = new ColorThemingStyle(FiveColorMap.COLOR_ATTRIBUTE, attributeToStyleMap, defaultStyle);
         themeStyle.setEnabled(true);

         return themeStyle;
      }


      public void cancel()
      {
         colorMap.cancel();
         super.cancel(true);
      }

      /*
       * Executed in event dispatching thread
       */
      @Override
      public void done()
      {
         Toolkit.getDefaultToolkit().beep();
         setCursor(null); //turn off the wait cursor
         FiveColorDialog.this.dispose();
      }
   }

}
