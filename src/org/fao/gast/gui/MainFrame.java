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
import javax.swing.JSplitPane;
import org.fao.gast.boot.Starter;
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

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewPanel, workPanel);
		sp.setDividerLocation(150);
		sp.setContinuousLayout(true);

		getContentPane().add(sp, BorderLayout.CENTER);

		viewPanel.setActionListener(this);
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void start(String appPath, String[] args) throws Exception
	{
		Lib.init(appPath);

		GuiBuilder builder = new GuiBuilder(appPath, viewPanel, workPanel);
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
		workPanel.show(e.getActionCommand());
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ViewPanel viewPanel = new ViewPanel();
	private WorkPanel workPanel = new WorkPanel();
}

//==============================================================================

