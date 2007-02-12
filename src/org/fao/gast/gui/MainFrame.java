//==============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.gast.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import org.fao.gast.app.App;
import org.fao.gast.boot.Starter;
import org.fao.gast.gui.dialogs.ConfigDialog;
import org.fao.gast.lib.Lib;

//==============================================================================

public class MainFrame extends JFrame implements Starter, ActionListener
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainFrame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("GAST : GeoNetwork's administrator survival tool");

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panView, panWork);
		sp.setDividerLocation(150);
		sp.setContinuousLayout(true);

		getContentPane().add(sp, BorderLayout.CENTER);

		panView.setActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panWork.show(e.getActionCommand());
			}
		});

		setJMenuBar(createMenuBar());
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void start(String appPath, String[] args) throws Exception
	{
		Lib.init(appPath);
		App.init(dlgConfig.getConfig());

		GuiBuilder builder = new GuiBuilder(appPath, panView, panWork);
		builder.build("/gast/data/gui.xml");

		setSize(700, 500);
		setVisible(true);
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals("config"))
			handleConfig();
	}

	//---------------------------------------------------------------------------

	private void handleConfig()
	{
		dlgConfig.showDialog();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private Methods
	//---
	//---------------------------------------------------------------------------

	private JMenuBar createMenuBar()
	{
		JMenuBar  menu    = new JMenuBar();
		JMenu     options = new JMenu("Options");
		JMenuItem config  = new JMenuItem("Config");

		menu   .add(options);
		options.add(config);

		config.addActionListener(this);
		config.setActionCommand("config");
		config.setAccelerator(KeyStroke.getKeyStroke("alt C"));
		return menu;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ViewPanel    panView   = new ViewPanel();
	private WorkPanel    panWork   = new WorkPanel();
	private ConfigDialog dlgConfig = new ConfigDialog(this);
}

//==============================================================================

