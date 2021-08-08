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
package org.freevoice.mapcoloring;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import org.freevoice.mapcoloring.ui.FiveColorDialog;

/**
 * OJ plugin to apply 5-color algorithm to color a OJ map layer.
 */
public class FiveColorPlugin extends AbstractPlugIn {

    private static final I18N i18n = I18N.getInstance("org.freevoice.mapcoloring");

    /**
     * @param context plugin context
     */
    @Override
    public void initialize(PlugInContext context) {
        context.getFeatureInstaller().addMainMenuPlugin(this,
            new String[] {MenuNames.PLUGINS},
            i18n.get("mapcoloring.plugin.menu"),
            false, null, null);
    }

    @Override
    public boolean execute(PlugInContext context)
    {
       FiveColorDialog dialog = new FiveColorDialog(context, context.getWorkbenchFrame(),
             i18n.get("mapcoloring.plugin.menu"));
       dialog.setVisible(true);

       return true;
    }


}
