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
import org.fao.gast.localization.Messages;

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
		super(Messages.getString("ServerPanel.title"));

		FlexLayout fl = new FlexLayout(3,3);
		fl.setColProp(2, FlexLayout.EXPAND);
		setLayout(fl);

		txtHost.setText(getHost());
		add("0,0",   new JLabel(Messages.getString("host")));
		add("1,0,x", txtHost);

		txtPort.setText(getPort()+"");
		add("0,1",   new JLabel(Messages.getString("port")));
		add("1,1,x", txtPort);

		txtServlet.setText(getServlet());
		add("0,2",   new JLabel(Messages.getString("servlet")));
		add("1,2,x", txtServlet);

	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()
	{
		if (txtHost.getText().trim().equals("")) {
			return Lib.embeddedSC.getHost();
		} else {
			String host = txtHost.getText().trim();
			if (host.equals("")) host="localhost";
			return host;
		}
	}

	//---------------------------------------------------------------------------

	public int getPort() throws NumberFormatException
	{
		if (txtPort.getText().trim().equals("")) {
			String port = Lib.embeddedSC.getPort();
			return Integer.parseInt(port);
		} else {
			String port = txtPort.getText().trim();
			if (port.length() == 0) port="8080";
			return Integer.parseInt(port);
		}
	}

	//---------------------------------------------------------------------------

	public String getServlet()
	{
		if (txtServlet.getText().trim().equals("")) {
			return Lib.embeddedSC.getServlet();
		} else {
			String servlet = txtServlet.getText().trim();
			if (servlet.equals("")) servlet="geonetwork";
			return servlet;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField     txtHost   = new JTextField(20);
	private JTextField     txtPort   = new JTextField(20);
	private JTextField     txtServlet= new JTextField(20);
}

//==============================================================================

