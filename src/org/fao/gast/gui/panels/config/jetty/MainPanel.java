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

package org.fao.gast.gui.panels.config.jetty;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.fao.gast.gui.panels.FormPanel;
import org.fao.gast.lib.Lib;

//==============================================================================

public class MainPanel extends FormPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainPanel()
	{
		txtServlet.setText(Lib.embSC.getServlet());
		txtPort   .setText(Lib.embSC.getPort());
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		save();
	}

	//---------------------------------------------------------------------------

	private void save()
	{
		if (!Lib.type.isInteger(txtPort.getText()))
			Lib.gui.showError(this, "The port must be an integer");
		else
		{
			Lib.embSC.setServlet(txtServlet.getText());
			Lib.embSC.setPort(txtPort.getText());

			try
			{
				Lib.embSC.save();
				Lib.gui.showInfo(this, "Configuration saved");
			}
			catch (IOException e)
			{
				Lib.gui.showError(this, e);
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected JComponent buildInnerPanel()
	{
		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(2,2);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0",   new JLabel("Servlet"));
		p.add("0,1",   new JLabel("Port"));
		p.add("1,0,x", txtServlet);
		p.add("1,1,x", txtPort);

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JTextField txtServlet = new JTextField(20);
	private JTextField txtPort    = new JTextField(20);
}

//==============================================================================

