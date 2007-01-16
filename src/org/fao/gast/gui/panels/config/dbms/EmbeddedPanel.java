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

import javax.swing.JLabel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.fao.gast.lib.Lib;

//==============================================================================

public class EmbeddedPanel extends DbmsPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public EmbeddedPanel()
	{
		FlexLayout fl = new FlexLayout(3,1);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel("Port"));
		add("1,0", txtPort);
		add("2,0", new JLabel("<html><font color='red'>(REQ)</font>"));

		txtPort.setText("9157");
		txtPort.setToolTipText("The default port is 9157");
	}

	//---------------------------------------------------------------------------
	//---
	//--- DbmsPanel methods
	//---
	//---------------------------------------------------------------------------

	public String getLabel() { return "Embedded"; }

	//---------------------------------------------------------------------------

	public boolean matches(String url)
	{
		return url.startsWith("jdbc:mckoi:");
	}

	//---------------------------------------------------------------------------

	public void retrieve()
	{
		txtPort.setText(Lib.embDB.getPort());
	}

	//---------------------------------------------------------------------------

	public void save() throws Exception
	{
		String port = txtPort.getText();
		String user = Lib.embDB.getUser();
		String pass = Lib.embDB.getPassword();

		if (!Lib.type.isInteger(port))
			throw new Exception("The port must be an integer");

		if (user == null || pass == null)
			throw new Exception("The data files are missing.\n"+
									  "Please create them using the 'database/data files' panel.");

		Lib.config.setDbmsDriver  ("com.mckoi.JDBCDriver");
		Lib.config.setDbmsURL     ("jdbc:mckoi://localhost:"+port+"/");
		Lib.config.setDbmsUser    (user);
		Lib.config.setDbmsPassword(pass);
		Lib.config.addActivator();
		Lib.config.save();

		Lib.embDB.setPort(port);
		Lib.embDB.save();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField txtPort = new JTextField(6);
}

//==============================================================================

