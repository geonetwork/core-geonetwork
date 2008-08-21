//=============================================================================
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

package org.fao.geonet.csw.client;

import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.geonet.csw.common.Csw.ElementSetName;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordByIdRequest;

//=============================================================================

public class GetRecordByIdPanel extends TPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9180478969902538378L;
	private JComboBox  cmbElemSet = new JComboBox();
	private JTextField txtIds     = new JTextField();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetRecordByIdPanel()
	{
		super("GetRecordById parameters");

		FlexLayout fl = new FlexLayout(2,2);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel("Element set name"));
		add("0,1", new JLabel("Id(s)"));

		add("1,0,x", cmbElemSet);
		add("1,1,x", txtIds);

		cmbElemSet.addItem("(default)");
		cmbElemSet.addItem(ElementSetName.BRIEF);
		cmbElemSet.addItem(ElementSetName.SUMMARY);
		cmbElemSet.addItem(ElementSetName.FULL);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API method
	//---
	//---------------------------------------------------------------------------

	public CatalogRequest createRequest()
	{
		GetRecordByIdRequest request = new GetRecordByIdRequest();

		if (cmbElemSet.getSelectedIndex() != 0)
			request.setElementSetName((ElementSetName) cmbElemSet.getSelectedItem());

		//--- 'AcceptFormats' parameter

		String ids = txtIds.getText();

		StringTokenizer st = new StringTokenizer(ids, ",");

		while (st.hasMoreTokens())
			request.addId(st.nextToken());

		return request;
	}
}

//=============================================================================

