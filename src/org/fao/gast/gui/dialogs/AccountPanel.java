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
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;

//==============================================================================

public class AccountPanel extends TPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public AccountPanel()
	{
		super("Account");

		FlexLayout fl = new FlexLayout(3,4);
		fl.setColProp(2, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0,x,c,3", jrbNoAuth);
		add("0,1,x,c,3", jrbAuth);

		add("1,2",   new JLabel("Username"));
		add("2,2,x", txtUser);

		add("1,3",   new JLabel("Password"));
		add("2,3,x", txtPass);

		btgAccount.add(jrbNoAuth);
		btgAccount.add(jrbAuth);
		btgAccount.setSelected(jrbNoAuth.getModel(), true);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public boolean useAccount()  { return jrbAuth.isSelected(); }
	public String  getUsername() { return txtUser.getText();    }
	public String  getPassword() { return txtPass.getText();    }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ButtonGroup    btgAccount= new ButtonGroup();
	private JRadioButton   jrbNoAuth = new JRadioButton("No authentication");
	private JRadioButton   jrbAuth   = new JRadioButton("Use this account");
	private JTextField     txtUser   = new JTextField(20);
	private JPasswordField txtPass   = new JPasswordField(20);
}

//==============================================================================

