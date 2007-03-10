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

package org.fao.gast.gui.dialogs;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.gast.lib.Lib;

//==============================================================================

public class ServerPanel extends TPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ServerPanel()
	{
		super("Server");

		FlexLayout fl = new FlexLayout(3,5);
		fl.setColProp(2, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0,x,c,3", jrbEmbed);
		add("0,1,x,c,3", jrbExter);

		add("1,2",   new JLabel("Host"));
		add("2,2,x", txtHost);

		add("1,3",   new JLabel("Port"));
		add("2,3,x", txtPort);

		add("1,4",   new JLabel("Servlet"));
		add("2,4,x", txtServlet);

		btgServer.add(jrbEmbed);
		btgServer.add(jrbExter);
		btgServer.setSelected(jrbEmbed.getModel(), true);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()
	{
		if (jrbEmbed.isSelected())
		{
			String host = Lib.embeddedSC.getHost();

			return (host == null) ? "localhost" : host;
		}
		else
		{
			return txtHost.getText();
		}
	}

	//---------------------------------------------------------------------------

	public int getPort() throws NumberFormatException
	{
		if (jrbEmbed.isSelected())
		{
			String port = Lib.embeddedSC.getPort();

			return (port == null) ? 80 : Integer.parseInt(port);
		}
		else
		{
			String port = txtPort.getText().trim();

			if (port.length() == 0)
				return 80;

			return Integer.parseInt(port);
		}
	}

	//---------------------------------------------------------------------------

	public String getServlet()
	{
		if (jrbEmbed.isSelected())		return Lib.embeddedSC.getServlet();
			else								return txtServlet.getText();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ButtonGroup    btgServer = new ButtonGroup();
	private JRadioButton   jrbEmbed  = new JRadioButton("Embedded");
	private JRadioButton   jrbExter  = new JRadioButton("External");
	private JTextField     txtHost   = new JTextField(20);
	private JTextField     txtPort   = new JTextField(20);
	private JTextField     txtServlet= new JTextField(20);
}

//==============================================================================

