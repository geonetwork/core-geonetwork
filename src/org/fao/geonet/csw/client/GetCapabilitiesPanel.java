//=============================================================================
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

package org.fao.geonet.csw.client;

import java.util.StringTokenizer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.Csw.Section;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetCapabilitiesRequest;

//=============================================================================

public class GetCapabilitiesPanel extends TPanel
{
	private JTextField txtUpdateSeq   = new JTextField();
	private JTextField txtAccFormats  = new JTextField("application/xml");
	private JTextField txtAccVersions = new JTextField(Csw.CSW_VERSION);

	private JCheckBox chbServIdent  = new JCheckBox("Service identification");
	private JCheckBox chbServProvid = new JCheckBox("Service provider");
	private JCheckBox chbOperMeta   = new JCheckBox("Operations metadata");
	private JCheckBox chbFiltCapab  = new JCheckBox("Filter capabilities");

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetCapabilitiesPanel()
	{
		super("GetCapabilities Parameters");

		FlexLayout fl = new FlexLayout(2,7);
		fl.setColProp(1, FlexLayout.EXPAND);
		setLayout(fl);

		add("0,0", new JLabel("Update sequence"));
		add("0,1", new JLabel("Accept format(s)"));
		add("0,2", new JLabel("Accept version(s)"));

		add("1,0,x", txtUpdateSeq);
		add("1,1,x", txtAccFormats);
		add("1,2,x", txtAccVersions);

		add("0,3,x,c,2", chbServIdent);
		add("0,4,x,c,2", chbServProvid);
		add("0,5,x,c,2", chbOperMeta);
		add("0,6,x,c,2", chbFiltCapab);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API method
	//---
	//---------------------------------------------------------------------------

	public CatalogRequest createRequest()
	{
		GetCapabilitiesRequest request = new GetCapabilitiesRequest();

		String updateSequence = txtUpdateSeq  .getText();
		String acceptFormats  = txtAccFormats .getText();
		String acceptVersions = txtAccVersions.getText();

		if (!updateSequence.equals(""))
			request.setUpdateSequence(updateSequence);

		//--- 'AcceptFormats' parameter

		if (!acceptFormats.equals(""))
		{
			StringTokenizer st = new StringTokenizer(acceptFormats, ",");

			while (st.hasMoreTokens())
				request.addOutputFormat(st.nextToken());
		}

		//--- 'AcceptVersions' parameter

		if (!acceptVersions.equals(""))
		{
			StringTokenizer st = new StringTokenizer(acceptVersions, ",");

			while (st.hasMoreTokens())
				request.addVersion(st.nextToken());
		}

		//--- 'Sections' parameter

		if (chbServIdent.isSelected())
			request.addSection(Section.ServiceIdentification);

		if (chbServProvid.isSelected())
			request.addSection(Section.ServiceProvider);

		if (chbOperMeta.isSelected())
			request.addSection(Section.OperationsMetadata);

		if (chbFiltCapab.isSelected())
			request.addSection(Section.Filter_Capabilities);

		return request;
	}
}

//=============================================================================

