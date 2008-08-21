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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.geonet.csw.common.Csw.TypeName;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.DescribeRecordRequest;
//=============================================================================

public class DescribeRecordPanel extends TPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5788319394312789631L;
	private JTextField txtOutputFormat = new JTextField("application/xml");
	private JTextField txtSchemaLang   = new JTextField("http://www.w3.org/XML/Schema");

	private JCheckBox chbService     = new JCheckBox("Service");
	private JCheckBox chbDataset     = new JCheckBox("Dataset");
	private JCheckBox chbApplication = new JCheckBox("Application");
	private JCheckBox chbCollection  = new JCheckBox("Dataset collection");

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public DescribeRecordPanel()
	{
		super("DescribeRecord parameters");

		FlexLayout fl = new FlexLayout(2,6);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel("Output Format"));
		add("0,1", new JLabel("Schema language"));

		add("1,0,x", txtOutputFormat);
		add("1,1,x", txtSchemaLang);

		add("0,2,x,c,2", chbService);
		add("0,3,x,c,2", chbDataset);
		add("0,4,x,c,2", chbApplication);
		add("0,5,x,c,2", chbCollection);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API method
	//---
	//---------------------------------------------------------------------------

	public CatalogRequest createRequest()
	{
		DescribeRecordRequest request = new DescribeRecordRequest();

		String outputFormat = txtOutputFormat.getText();
		String schemaLang   = txtSchemaLang.getText();

		if (!outputFormat.equals(""))
			request.setOutputFormat(outputFormat);

		if (!schemaLang.equals(""))
			request.setSchemaLanguage(schemaLang);

		if (chbService.isSelected())
			request.addTypeName(TypeName.SERVICE);

		if (chbDataset.isSelected())
			request.addTypeName(TypeName.DATASET);

		if (chbApplication.isSelected())
			request.addTypeName(TypeName.APPLICATION);

		if (chbCollection.isSelected())
			request.addTypeName(TypeName.DATASET_COLLECTION);

		return request;
	}
}

//=============================================================================

