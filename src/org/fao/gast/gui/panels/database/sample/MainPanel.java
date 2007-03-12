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

package org.fao.gast.gui.panels.database.sample;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jeeves.utils.Util;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.GuiUtil;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.gui.panels.FormPanel;

//==============================================================================

public class MainPanel extends FormPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainPanel() {}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals("import"))
			doImport();
	}

	//---------------------------------------------------------------------------

	private void doImport()
	{
		Frame          owner  = GuiUtil.getFrame(this);
		ProgressDialog dialog = new ProgressDialog(owner, "Importing data");
		Worker         worker = new Worker(dialog);

		String runs = cmbRuns.getSelectedItem().toString();
		runs = Util.replaceString(runs, ".", "");

		worker.setImportMetadata (chbMetadata .isSelected());
		worker.setImportTemplates(chbTemplates.isSelected());
		worker.setImportRuns(Integer.parseInt(runs));

		dialog.run(worker);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected JComponent buildInnerPanel()
	{
		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(2,3);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0", new JLabel("Metadata"));
		p.add("1,0", chbMetadata);

		p.add("0,1", new JLabel("Templates"));
		p.add("1,1", chbTemplates);

		p.add("0,2", new JLabel("Runs"));
		p.add("1,2", cmbRuns);


		cmbRuns.addItem("1");
		cmbRuns.addItem("10");
		cmbRuns.addItem("100");
		cmbRuns.addItem("1.000");
		cmbRuns.addItem("10.000");
		cmbRuns.addItem("100.000");
		cmbRuns.addItem("1.000.000");

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JCheckBox chbMetadata = new JCheckBox();
	private JCheckBox chbTemplates= new JCheckBox();
	private JComboBox cmbRuns     = new JComboBox();
}

//==============================================================================

