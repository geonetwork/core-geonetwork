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

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.MultiPanel;
import org.fao.gast.gui.panels.FormPanel;
import org.fao.gast.lib.Lib;
import org.fao.gast.localization.Messages;

//==============================================================================

public class MainPanel extends FormPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = -2764938018735318934L;

	public MainPanel()
	{
		for (DbmsPanel p : panels)
		{
			multiPanel.add(p.getLabel(), p);
			cmbDbms.addItem(p.getLabel());
		}

		multiPanel.show(panels[0].getLabel());

		//--- show proper panel

		String url = Lib.config.getDbmsURL();

		for (DbmsPanel p : panels)
			if (p.matches(url))
			{
				p.retrieve();
				multiPanel.show(p.getLabel());
				cmbDbms.setSelectedItem(p.getLabel());
				break;
			}

		//--- setup combobox

		cmbDbms.addItemListener(itemList);
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		DbmsPanel p = panels[cmbDbms.getSelectedIndex()];

		try
		{
			p.save();
			Lib.gui.showInfo(this, Messages.getString("configSaved"));
		}

		catch (IOException ex)
		{
			Lib.gui.showError(this, Messages.getString("configException")+
										ex.getMessage());

			ex.printStackTrace();
		}

		catch (Exception ex)
		{
			Lib.gui.showError(this, ex.getMessage());
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected JComponent buildInnerPanel()
	{
		//--- setup container

		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(2,3);
		fl.setColProp(1, FlexLayout.EXPAND);
		fl.setRowProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0",       new JLabel("DBMS"));
		p.add("1,0,x",     cmbDbms);
		p.add("0,2,x,x,2", multiPanel);

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JComboBox  cmbDbms    = new JComboBox();
	private MultiPanel multiPanel = new MultiPanel();

	//---------------------------------------------------------------------------

	private static final DbmsPanel panels[] =
	{
		new EmbeddedPanel(),
		new OraclePanel(),
		new MySQLPanel(),
		new PostgresPanel(),
		new GenericPanel()  //--- this must be the last one
	};

	//---------------------------------------------------------------------------

	private ItemListener itemList = new ItemListener()
	{
		public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
				multiPanel.show(e.getItem().toString());
		}
	};
}

//==============================================================================

abstract class DbmsPanel extends JPanel
{
	public abstract String  getLabel();
	public abstract boolean matches(String url);
	public abstract void    retrieve();
	public abstract void    save() throws Exception;
}

//==============================================================================

