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
import org.fao.gast.lib.Lib;

//==============================================================================

public class MainPanel extends FormPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6138884263046015312L;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainPanel()
	{
		jfcBrowser.setDialogTitle("Choose input folder");
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
		Object source = e.getSource();

		if (cmd.equals("browse"))
			browse();

		else if (cmd.equals("migrate"))
			migrate();

		else if (source.equals(jcbCreateUser))
		{
			boolean enable = jcbCreateUser.isSelected();
			txtUser.setEnabled(enable);
			txtGroup.setEnabled(enable);
		}
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

		if ("".equals(txtOldDir.getText()))
		{
			Lib.gui.showError(this, "Please choose a directory");
			return;
		}

		if (jcbCreateUser.isSelected())
		{
			if ("".equals(txtUser.getText()))
			{
				Lib.gui.showError(this, "Please enter a user name");
				return;
			}
			else if ("".equals(txtGroup.getText()))
			{
				Lib.gui.showError(this, "Please enter a group name");
				return;
			}
			worker.setOldUser   (txtUser.getText());
			worker.setOldGroup  (txtGroup.getText());
		}

		worker.setUserDialog(jcbUserDialog.isSelected());
		worker.setOldDir(txtOldDir.getText());
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

		FlexLayout fl = new FlexLayout(3,5);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0",   new JLabel("Old GeoNetwork"));
		p.add("1,0,x", txtOldDir);
		p.add("2,0",   btnBrowse);

		p.add("0,1,1,1,3", jcbCreateUser);
		p.add("0,2", new JLabel("User"));
		p.add("1,2", txtUser);
		p.add("0,3", new JLabel("Group"));
		p.add("1,3", txtGroup);

		p.add("0,4,x,c,3", jcbUserDialog);

		txtUser.setEnabled(false);
		txtGroup.setEnabled(false);

		btnBrowse.addActionListener(this);
		btnBrowse.setActionCommand("browse");

		jcbCreateUser.addActionListener(this);

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

	private JCheckBox  jcbCreateUser = new JCheckBox("Assign unowned metadata to this user:");
	private JTextField txtUser       = new JTextField(20);
	private JTextField txtGroup      = new JTextField(20);

	private JCheckBox  jcbUserDialog = new JCheckBox("Popup dialog to choose users");
}

//==============================================================================

