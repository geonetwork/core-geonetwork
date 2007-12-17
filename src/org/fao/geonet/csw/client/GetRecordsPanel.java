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

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.geonet.csw.common.Csw.ConstraintLanguage;
import org.fao.geonet.csw.common.Csw.ElementSetName;
import org.fao.geonet.csw.common.Csw.OutputSchema;
import org.fao.geonet.csw.common.Csw.ResultType;
import org.fao.geonet.csw.common.Csw.TypeName;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordsRequest;

//=============================================================================

public class GetRecordsPanel extends TPanel
{
	//--- general panel

	private JTextField txtHopCount    = new JTextField();
	private JCheckBox  chbDataset     = new JCheckBox("Dataset");
	private JCheckBox  chbDatasetColl = new JCheckBox("Dataset collection");
	private JCheckBox  chbService     = new JCheckBox("Service");
	private JCheckBox  chbApplication = new JCheckBox("Application");
	private JCheckBox  chbDistrSearch = new JCheckBox("Enable");

	//--- query panel

	private JComboBox  cmbLanguage     = new JComboBox();
	private JTextField txtLangVersion  = new JTextField();
	private JTextArea  txaConstrCQL    = new JTextArea();
	private JTextArea  txaConstrFilter = new JTextArea();

	//--- results panel

	private JComboBox  cmbResultType  = new JComboBox();
	private JComboBox  cmbElemSetName = new JComboBox();
	private JComboBox  cmbOutSchema   = new JComboBox();

	private JTextField txtOutFormat   = new JTextField();
	private JTextField txtStartPos    = new JTextField();
	private JTextField txtMaxRecords  = new JTextField();
	private JTextField txtSortBy      = new JTextField();

	//---------------------------------------------------------------------------

	private static final String EXAMPLE_CQL = "AnyText like %africa%";

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetRecordsPanel()
	{
		super("GetRecords parameters");

		setLayout(new BorderLayout());
		JTabbedPane pane = new JTabbedPane();

		pane.addTab("General", getGeneralPanel());
		pane.addTab("Query",   getQueryPanel());
		pane.addTab("Results", getResultsPanel());

		add(pane, BorderLayout.CENTER);
	}

	//---------------------------------------------------------------------------
	//--- General panel
	//---------------------------------------------------------------------------

	private JPanel getGeneralPanel()
	{
		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(2,2);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0,x,c,2", getTypeNamesPanel());
		p.add("0,1,x,c,2", getDistribPanel());

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel getTypeNamesPanel()
	{
		TPanel p = new TPanel("Type names");

		FlexLayout fl = new FlexLayout(1,4);
		fl.setColProp(0, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0,x", chbDataset);
		p.add("0,1,x", chbDatasetColl);
		p.add("0,2,x", chbService);
		p.add("0,3,x", chbApplication);

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel getDistribPanel()
	{
		TPanel p = new TPanel("Distributed search");

		FlexLayout fl = new FlexLayout(2,2);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0,x,c,2", chbDistrSearch);

		p.add("0,1",   new JLabel("Hop count"));
		p.add("1,1,x", txtHopCount);

		return p;
	}

	//---------------------------------------------------------------------------
	//--- Query panel
	//---------------------------------------------------------------------------

	private JPanel getQueryPanel()
	{
		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(2,3);
		fl.setColProp(1, FlexLayout.EXPAND);
		fl.setRowProp(2, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0", new JLabel("Language"));
		p.add("0,1", new JLabel("Version"));

		p.add("1,0,x", cmbLanguage);
		p.add("1,1,x", txtLangVersion);

		JTabbedPane pane = new JTabbedPane();
		pane.addTab("CQL",    new JScrollPane(txaConstrCQL));
		pane.addTab("Filter", new JScrollPane(txaConstrFilter));

		p.add("0,2,x,x,2", pane);

		cmbLanguage.addItem(ConstraintLanguage.CQL);
		cmbLanguage.addItem(ConstraintLanguage.FILTER);

		txtLangVersion .setText("1.1.0");
		txaConstrCQL   .setText(EXAMPLE_CQL);
		txaConstrCQL   .setTabSize(3);
		txaConstrFilter.setText(getFilterExample());
		txaConstrFilter.setTabSize(3);

		return p;
	}

	//---------------------------------------------------------------------------

	private String getFilterExample()
	{
		File file = new File("filter-example.xml");

		FileInputStream is = null;

		try
		{
			is = new FileInputStream(file);

			byte[] data = new byte[(int) file.length()];
			is.read(data);

			return new String(data, "UTF-8");
		}
		catch (IOException e)
		{
			return "Cannot open file :\n"+file;
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
			}
			catch (IOException e) {}
		}
	}

	//---------------------------------------------------------------------------
	//--- Results panel
	//---------------------------------------------------------------------------

	private JPanel getResultsPanel()
	{
		JPanel p = new JPanel();

		FlexLayout fl = new FlexLayout(2,7);
		fl.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(fl);

		p.add("0,0", new JLabel("Result type"));
		p.add("0,1", new JLabel("Output schema"));
		p.add("0,2", new JLabel("Element set name"));
		p.add("0,3", new JLabel("Output format"));
		p.add("0,4", new JLabel("Start position"));
		p.add("0,5", new JLabel("Max records"));
		p.add("0,6", new JLabel("Sort by"));

		p.add("1,0,x", cmbResultType);
		p.add("1,1,x", cmbOutSchema);
		p.add("1,2,x", cmbElemSetName);
		p.add("1,3,x", txtOutFormat);
		p.add("1,4,x", txtStartPos);
		p.add("1,5,x", txtMaxRecords);
		p.add("1,6,x", txtSortBy);

		cmbResultType.addItem("(default)");
		cmbResultType.addItem(ResultType.HITS);
		cmbResultType.addItem(ResultType.RESULTS);
		cmbResultType.addItem(ResultType.VALIDATE);

		cmbOutSchema.addItem("(default)");
		cmbOutSchema.addItem(OutputSchema.OGC_CORE);
		cmbOutSchema.addItem(OutputSchema.ISO_PROFILE);

		cmbElemSetName.addItem("(default)");
		cmbElemSetName.addItem(ElementSetName.BRIEF);
		cmbElemSetName.addItem(ElementSetName.SUMMARY);
		cmbElemSetName.addItem(ElementSetName.FULL);

		txtSortBy.setToolTipText("field1, field2, ...");

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API method
	//---
	//---------------------------------------------------------------------------

	public CatalogRequest createRequest()
	{
		GetRecordsRequest request = new GetRecordsRequest();

		//--- handle typeName

		if (chbDataset.isSelected())
			request.addTypeName(TypeName.DATASET);

		if (chbDatasetColl.isSelected())
			request.addTypeName(TypeName.DATASET_COLLECTION);

		if (chbService.isSelected())
			request.addTypeName(TypeName.SERVICE);

		if (chbApplication.isSelected())
			request.addTypeName(TypeName.APPLICATION);

		//--- handle distributed search

		if (chbDistrSearch.isSelected())
			request.setDistributedSearch(true);

		if (txtHopCount.getText().length() != 0)
			request.setHopCount(txtHopCount.getText());

		//--- handle query params

		if (txtLangVersion.getText().length() != 0)
			request.setConstraintLangVersion(txtLangVersion.getText());

		request.setConstraintLanguage((ConstraintLanguage) cmbLanguage.getSelectedItem());
		request.setConstraint(
							cmbLanguage.getSelectedItem() == ConstraintLanguage.CQL
							? txaConstrCQL.getText()
							: txaConstrFilter.getText());

		//--- handle results type parameters

		if (cmbResultType.getSelectedIndex() != 0)
			request.setResultType((ResultType) cmbResultType.getSelectedItem());

		if (cmbOutSchema.getSelectedIndex() != 0)
			request.setOutputSchema((OutputSchema) cmbOutSchema.getSelectedItem());

		if (cmbElemSetName.getSelectedIndex() != 0)
			request.setElementSetName((ElementSetName) cmbElemSetName.getSelectedItem());

		if (txtStartPos.getText().length() != 0)
			request.setStartPosition(txtStartPos.getText());

		if (txtMaxRecords.getText().length() != 0)
			request.setMaxRecords(txtMaxRecords.getText());

		if (txtOutFormat.getText().length() != 0)
			request.setOutputFormat(txtOutFormat.getText());

		//--- 'SortBy' parameter

		String sortBy = txtSortBy.getText();

		if (sortBy.length() != 0)
		{
			StringTokenizer st = new StringTokenizer(sortBy, ",");

			while (st.hasMoreTokens())
				request.addSortBy(st.nextToken().trim(), true);
		}

		return request;
	}
}

//=============================================================================

