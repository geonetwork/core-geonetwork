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

package org.fao.gast.gui.panels.config.dbms;

import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.fao.gast.lib.Lib;

//==============================================================================

public class OraclePanel extends DbmsPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public OraclePanel()
	{
		FlexLayout fl = new FlexLayout(3,5);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel("Server"));
		add("0,1", new JLabel("Port"));
		add("0,2", new JLabel("SID"));
		add("0,3", new JLabel("Username"));
		add("0,4", new JLabel("Password"));

		add("1,0", txtServer);
		add("1,1", txtPort);
		add("1,2", txtSid);
		add("1,3", txtUser);
		add("1,4", txtPass);

		add("2,0", new JLabel("<html><font color='red'>(REQ)</font>"));
		add("2,1", new JLabel("<html><font color='red'>(REQ)</font>"));
		add("2,2", new JLabel("<html><font color='red'>(REQ)</font>"));

		txtPort.setText("1521");
		txtPort.setToolTipText("The default port is 1521");
	}

	//---------------------------------------------------------------------------
	//---
	//--- DbmsPanel methods
	//---
	//---------------------------------------------------------------------------

	public String getLabel() { return "Oracle"; }

	//---------------------------------------------------------------------------

	public boolean matches(String url)
	{
		return url.startsWith("jdbc:oracle:");
	}

	//---------------------------------------------------------------------------
	//--- jdbc:oracle:thin:@<host>:<1521>:<database>

	public void retrieve()
	{
		String url = Lib.config.getDbmsURL();

		//--- cut prefix +'@'
		url = url.substring(PREFIX.length() +1);

		StringTokenizer st = new StringTokenizer(url, ":");

		String server= st.nextToken();
		String port  = st.hasMoreTokens() ? st.nextToken() : "1521";
		String sid   = st.hasMoreTokens() ? st.nextToken() : "????";

		txtServer.setText(server);
		txtPort  .setText(port);
		txtSid   .setText(sid);
		txtUser  .setText(Lib.config.getDbmsUser());
		txtPass  .setText(Lib.config.getDbmsPassword());
	}

	//---------------------------------------------------------------------------

	public void save() throws Exception
	{
		String server= txtServer.getText();
		String port  = txtPort  .getText();
		String sid   = txtSid   .getText();

		if (server.equals(""))
			throw new Exception("The server cannot be empty");

		if (!Lib.type.isInteger(port))
			throw new Exception("The port must be an integer");

		if (sid.equals(""))
			throw new Exception("The sid cannot be empty");

		String url = PREFIX +"@"+ server +":"+ port +":"+ sid;

		Lib.config.setDbmsDriver  ("oracle.jdbc.driver.OracleDriver");
		Lib.config.setDbmsURL     (url);
		Lib.config.setDbmsUser    (txtUser.getText());
		Lib.config.setDbmsPassword(txtPass.getText());
		Lib.config.removeActivator();
		Lib.config.save();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField txtServer = new JTextField(15);
	private JTextField txtPort   = new JTextField(6);
	private JTextField txtSid    = new JTextField(12);
	private JTextField txtUser   = new JTextField(12);
	private JTextField txtPass   = new JTextField(12);

	//---------------------------------------------------------------------------

	private static final String PREFIX = "jdbc:oracle:thin:";
}

//==============================================================================

