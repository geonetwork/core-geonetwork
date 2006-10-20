//==============================================================================
//===
//===   MainFrame
//===
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

package org.fao.geonet.apps.migration;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.geonet.apps.common.SimpleLogger;
import org.fao.geonet.apps.common.Starter;
import org.fao.geonet.apps.common.Util;

//==============================================================================

public class MainFrame extends JFrame implements Starter, ActionListener, SimpleLogger
{
	private JTextArea  txaLog     = new JTextArea(30,40);
	private JButton    btnStart   = new JButton("Start");
	private JCheckBox  chbTestOnly= new JCheckBox("Mapping test only", true);

	private String geonetDir;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainFrame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//------------------------------------------------------------------------

		btnStart.addActionListener(this);
		btnStart.setActionCommand("start");

		txaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txaLog.setEditable(false);

		//------------------------------------------------------------------------

		add(buildParamPanel(), BorderLayout.NORTH);
		add(buildLogPanel(),   BorderLayout.CENTER);

		setSize(800,600);
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

		if (cmd.equals("start"))	handleStart();
	}

	//---------------------------------------------------------------------------

	private void handleStart()
	{
		enableControls(false);
		new Migrator(geonetDir, this, chbTestOnly.isSelected()).start();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private JPanel buildParamPanel()
	{
		JPanel p = new TPanel("Parameters");

		FlexLayout flexL = new FlexLayout(1,1);
		flexL.setColProp(0, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0,x,c", chbTestOnly);

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel buildLogPanel()
	{
		JPanel p = new TPanel("Migration log");

		FlexLayout flexL = new FlexLayout(1,2);
		flexL.setColProp(0, FlexLayout.EXPAND);
		flexL.setRowProp(0, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0,x,x", new JScrollPane(txaLog));
		p.add("0,1,c",   btnStart);

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- SimpleLogger interface
	//---
	//---------------------------------------------------------------------------

	public void clear()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				txaLog.setText("");
			}
		});
	}

	//---------------------------------------------------------------------------

	public void logInfo(final String message)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				txaLog.append("| I | "+ message +"\n");
			}
		});
	}

	//---------------------------------------------------------------------------

	public void logError(final String message)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				txaLog.append("|EEE| "+ message +"\n");
			}
		});
	}

	//---------------------------------------------------------------------------

	public void finish()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				enableControls(true);
			}
		});
	}

	//---------------------------------------------------------------------------
	//---
	//--- Starter interface
	//---
	//---------------------------------------------------------------------------

	public void start(String installDir) throws Exception
	{
		geonetDir = installDir;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void enableControls(boolean yesno)
	{
		btnStart.setEnabled(yesno);
	}
}

//==============================================================================

