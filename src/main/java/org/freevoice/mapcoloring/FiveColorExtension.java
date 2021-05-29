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

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;


/**
 * OJ extension class for map coloring plugin
 */
public class FiveColorExtension extends Extension
{

	public void configure(PlugInContext context)
	{
		new FiveColorPlugin().initialize(context);
	}
	@Override
	public String getVersion() {
		return "1.0.0 (2021-05-25)";
	}

	@Override
	public String getName() {
		return "Map Coloring (Five color theorem - https://en.wikipedia.org/wiki/Five_color_theorem)";
	}
}
