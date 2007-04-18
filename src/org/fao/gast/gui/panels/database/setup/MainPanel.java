//==============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.gast.gui.panels.database.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import org.dlib.gui.GuiUtil;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.gui.panels.FormPanel;
import org.fao.gast.lib.Lib;

//==============================================================================

public class MainPanel extends FormPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Initialization
	//---
	//---------------------------------------------------------------------------

	protected JComponent buildInnerPanel() { return null; }

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		Frame owner = GuiUtil.getFrame(this);

		if (!Lib.gui.confirm(owner, WARNING))
			return;

		ProgressDialog dialog = new ProgressDialog(owner, "Setup in progress");
		Worker         worker = new Worker(dialog);

		dialog.run(worker);
	}

	//---------------------------------------------------------------------------

	private static final String WARNING = 	"The current database will be erased.\n"+
														"Do you want to continue ?";
}

//==============================================================================

