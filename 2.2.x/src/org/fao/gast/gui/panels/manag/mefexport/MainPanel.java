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

package org.fao.gast.gui.panels.manag.mefexport;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

	public MainPanel()
	{
		txtOutDir.setText(System.getProperty("user.home", ""));
		jfcBrowser.setDialogTitle("Choose output folder");
		jfcBrowser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals("browse"))
			browse();

		else if (cmd.equals("export"))
			export();
	}

	//---------------------------------------------------------------------------

	private void browse()
	{
		jfcBrowser.setSelectedFile(new File(txtOutDir.getText()));

		int res = jfcBrowser.showDialog(this, "Choose");

		if (res == JFileChooser.APPROVE_OPTION)
			txtOutDir.setText(jfcBrowser.getSelectedFile().getAbsolutePath());
	}

	//---------------------------------------------------------------------------

	private void export()
	{
		Frame          owner  = GuiUtil.getFrame(this);
		ProgressDialog dialog = new ProgressDialog(owner, "Exporting data");
		Worker         worker = new Worker(dialog, panSearch);

		worker.setOutDir(txtOutDir.getText());
		worker.setFormat(cmbFormat.getSelectedItem().toString().toLowerCase());
		worker.setSkipUuid(chbSkipUuid.isSelected());

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

		FlexLayout fl = new FlexLayout(3,4);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0",   new JLabel("Output folder"));
		p.add("1,0,x", txtOutDir);
		p.add("2,0",   btnBrowse);

		p.add("0,1", new JLabel("Format"));
		p.add("1,1", cmbFormat);

		p.add("0,2", new JLabel("Skip UUID"));
		p.add("1,2", chbSkipUuid);

		p.add("0,3,x,c,3,1", panSearch);

		btnBrowse.addActionListener(this);
		btnBrowse.setActionCommand("browse");

		cmbFormat.addItem("Simple");
		cmbFormat.addItem("Partial");
		cmbFormat.addItem("Full");

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField   txtOutDir  = new JTextField(20);
	private JButton      btnBrowse  = new JButton("Browse");
	private JComboBox    cmbFormat  = new JComboBox();
	private JCheckBox    chbSkipUuid= new JCheckBox();
	private JFileChooser jfcBrowser = new JFileChooser();
	private SearchPanel  panSearch  = new SearchPanel();
}

//==============================================================================

