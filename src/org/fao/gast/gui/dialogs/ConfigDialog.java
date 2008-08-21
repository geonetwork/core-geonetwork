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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TDialog;
import org.fao.gast.app.Configuration;

//==============================================================================

public class ConfigDialog extends TDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3014136660541158677L;
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ConfigDialog(Frame owner)
	{
		super(owner, "Configuration", true);

		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(1,2);
		fl.setColProp(0, FlexLayout.EXPAND);
		fl.setRowProp(0, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0,x,x", panConfig);
		p.add("0,1,c",   btnOk);

		getContentPane().add(p, BorderLayout.CENTER);

		btnOk.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Configuration getConfig() { return config; }

	//---------------------------------------------------------------------------
	//---
	//--- Configuration interface
	//---
	//---------------------------------------------------------------------------

	private Configuration config = new Configuration()
	{
		public String  getHost()     { return panConfig.getHost();     }
		public int     getPort()     { return panConfig.getPort();     }
		public String  getServlet()  { return panConfig.getServlet();  }
		public String  getUsername() { return panConfig.getUsername(); }
		public String  getPassword() { return panConfig.getPassword(); }
		public boolean useAccount()  { return panConfig.useAccount();  }
	};

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ConfigPanel panConfig = new ConfigPanel();
	private JButton     btnOk     = new JButton("Ok");
}

//==============================================================================

