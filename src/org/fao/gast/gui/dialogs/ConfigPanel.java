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

package org.fao.gast.gui.dialogs;

import javax.swing.JPanel;
import org.dlib.gui.FlexLayout;

//==============================================================================

public class ConfigPanel extends JPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ConfigPanel()
	{
		FlexLayout fl = new FlexLayout(1,2);
		fl.setColProp(0, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0,x", panServer);
		add("0,1,x", panAccount);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String  getHost()     { return panServer.getHost();      }
	public int     getPort()     { return panServer.getPort();      }
	public String  getServlet()  { return panServer.getServlet();   }
	public String  getUsername() { return panAccount.getUsername(); }
	public String  getPassword() { return panAccount.getPassword(); }
	public boolean useAccount()  { return panAccount.useAccount();  }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ServerPanel  panServer  = new ServerPanel();
	private AccountPanel panAccount = new AccountPanel();
}

//==============================================================================

