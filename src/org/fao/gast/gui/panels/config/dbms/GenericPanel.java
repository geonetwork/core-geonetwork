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

//==============================================================================

public class GenericPanel extends DbmsPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6566776767912014673L;
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GenericPanel()
	{
		FlexLayout fl = new FlexLayout(3,4);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel("JDBC Driver"));
		add("0,1", new JLabel("URL"));
		add("0,2", new JLabel("Username"));
		add("0,3", new JLabel("Password"));

		add("1,0,x", txtDriver);
		add("1,1,x", txtURL);
		add("1,2",   txtUser);
		add("1,3",   txtPass);

		add("2,0", new JLabel("<html><font color='red'>(REQ)</font>"));
		add("2,1", new JLabel("<html><font color='red'>(REQ)</font>"));
	}

	//---------------------------------------------------------------------------
	//---
	//--- DbmsPanel methods
	//---
	//---------------------------------------------------------------------------

	public String getLabel() { return "Generic JDBC connection"; }

	//---------------------------------------------------------------------------

	public boolean matches(String url)
	{
		return true;
	}

	//---------------------------------------------------------------------------

	public void retrieve()
	{
		txtDriver.setText(Lib.config.getDbmsDriver());
		txtURL   .setText(Lib.config.getDbmsURL());
		txtUser  .setText(Lib.config.getDbmsUser());
		txtPass  .setText(Lib.config.getDbmsPassword());
	}

	//---------------------------------------------------------------------------

	public void save() throws Exception
	{
		Lib.config.setDbmsDriver  (txtDriver.getText());
		Lib.config.setDbmsURL     (txtURL.getText());
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

	private JTextField txtDriver = new JTextField();
	private JTextField txtURL    = new JTextField();
	private JTextField txtUser   = new JTextField(12);
	private JTextField txtPass   = new JTextField(12);
}

//==============================================================================

