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

package org.fao.gast.gui.panels.config.dbms;

import javax.swing.JLabel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.fao.gast.lib.Lib;
import org.fao.gast.localization.Messages;

//==============================================================================

public class EmbeddedPanel extends DbmsPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 7290449012011589464L;

	public EmbeddedPanel()
	{
		FlexLayout fl = new FlexLayout(3,1);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel(Messages.getString("port")));
		add("1,0", txtPort);
		add("2,0", new JLabel("<html><font color='red'>(REQ)</font>"));

		txtPort.setText("9157");
		txtPort.setToolTipText(Messages.getString("mckoi.defaultPort"));
	}

	//---------------------------------------------------------------------------
	//---
	//--- DbmsPanel methods
	//---
	//---------------------------------------------------------------------------

	public String getLabel() { return Messages.getString("embedded"); }

	//---------------------------------------------------------------------------

	public boolean matches(String url)
	{
		return url.startsWith("jdbc:mckoi:");
	}

	//---------------------------------------------------------------------------

	public void retrieve()
	{
		txtPort.setText(Lib.embeddedDB.getPort());
	}

	//---------------------------------------------------------------------------

	public void save() throws Exception
	{
		String port = txtPort.getText();
		String user = Lib.embeddedDB.getUser();
		String pass = Lib.embeddedDB.getPassword();

		if (!Lib.type.isInteger(port))
			throw new Exception(Messages.getString("portInt"));

		Lib.config.setDbmsDriver  ("com.mckoi.JDBCDriver");
		Lib.config.setDbmsURL     ("jdbc:mckoi://localhost:"+port+"/");
		Lib.config.setDbmsUser    (user);
		Lib.config.setDbmsPassword(pass);
		Lib.config.addActivator();
		Lib.config.save();

		Lib.embeddedDB.setPort(port);
		Lib.embeddedDB.save();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField txtPort = new JTextField(6);
}

//==============================================================================

