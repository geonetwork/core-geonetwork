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

package org.fao.gast.gui.panels.migration.oldinst;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

		if (cmd.equals("migrate"))
			migrate();
	}

	//---------------------------------------------------------------------------

	private void browse()
	{
		jfcBrowser.setSelectedFile(new File(txtOldDir.getText()));

		int res = jfcBrowser.showDialog(this, "Choose");

		if (res == JFileChooser.APPROVE_OPTION)
			txtOldDir.setText(jfcBrowser.getSelectedFile().getAbsolutePath());
	}

	//---------------------------------------------------------------------------

	private void migrate()
	{
		Frame          owner  = GuiUtil.getFrame(this);
		ProgressDialog dialog = new ProgressDialog(owner, "Migrating data");
		Worker         worker = new Worker(dialog);

		worker.setOldDir(txtOldDir.getText());
		worker.setTestOnly(chbTestOnly.isSelected());
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

		FlexLayout fl = new FlexLayout(3,2);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0",   new JLabel("Old GeoNetwork folder"));
		p.add("1,0,x", txtOldDir);
		p.add("2,0",   btnBrowse);

		p.add("0,1,x,c,2", chbTestOnly);

		btnBrowse.addActionListener(this);
		btnBrowse.setActionCommand("browse");

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField   txtOldDir  = new JTextField(20);
	private JButton      btnBrowse  = new JButton("Browse");
	private JFileChooser jfcBrowser = new JFileChooser();
	private JCheckBox    chbTestOnly= new JCheckBox("Mapping test only", true);
}

//==============================================================================

