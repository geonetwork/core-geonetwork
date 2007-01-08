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

package org.fao.geonet.services.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

//=============================================================================

/** Returns default values for the search parameters
  */

public class MainUtil
{
	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public static Element getDefaultSearch(ServiceContext srvContext, Element request)
	{
		UserSession session = srvContext.getUserSession();
		Element     elData  = (Element) session.getProperty(Geonet.Session.MAIN_SEARCH);

		if (elData == null)
		{
			srvContext.info("Creating default search data");

			elData = new Element(Jeeves.Elem.RESPONSE)
						.addContent(new Element(Geonet.SearchResult.TITLE)        .setText(""))
						.addContent(new Element(Geonet.SearchResult.ABSTRACT)     .setText(""))
						.addContent(new Element(Geonet.SearchResult.ANY)          .setText(""))
						.addContent(new Element(Geonet.SearchResult.REGION)       .setText(""))
						.addContent(new Element(Geonet.SearchResult.SOUTH_BL)     .setText(""))
						.addContent(new Element(Geonet.SearchResult.NORTH_BL)     .setText(""))
						.addContent(new Element(Geonet.SearchResult.EAST_BL)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.WEST_BL)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.RELATION)     .setText(Geonet.SearchResult.Relation.EQUAL))
						.addContent(new Element(Geonet.SearchResult.FROM)         .setText(""))
						.addContent(new Element(Geonet.SearchResult.TO)           .setText(""))
						.addContent(new Element(Geonet.SearchResult.GROUP)        .setText(""))
						.addContent(new Element(Geonet.SearchResult.PROFILE)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.SERVERS))
						.addContent(new Element(Geonet.SearchResult.TIMEOUT)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.KEYWORDS))
						.addContent(new Element(Geonet.SearchResult.THEME_KEY)    .setText(""))
						.addContent(new Element(Geonet.SearchResult.CATEGORY)     .setText(""))
						.addContent(new Element(Geonet.SearchResult.SITE_ID)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.DOWNLOAD)     .setText(Geonet.Text.OFF))
						.addContent(new Element(Geonet.SearchResult.ONLINE)       .setText(Geonet.Text.OFF))
						.addContent(new Element(Geonet.SearchResult.DIGITAL)      .setText(Geonet.Text.ON))
						.addContent(new Element(Geonet.SearchResult.PAPER)        .setText(Geonet.Text.OFF))
						.addContent(new Element(Geonet.SearchResult.TEMPLATE)     .setText("n"))
						.addContent(new Element(Geonet.SearchResult.EXTENDED)     .setText(Geonet.Text.OFF))
						.addContent(new Element(Geonet.SearchResult.HELP)         .setText(Geonet.Text.OFF))
						.addContent(new Element(Geonet.SearchResult.REMOTE)       .setText(Geonet.Text.OFF))
						.addContent(new Element(Geonet.SearchResult.HITS_PER_PAGE).setText("10"));
			session.setProperty(Geonet.Session.MAIN_SEARCH, elData);
		}
		Element  elTitle       = elData.getChild(Geonet.SearchResult.TITLE);
		Element  elAbstract    = elData.getChild(Geonet.SearchResult.ABSTRACT);
		Element  elAny         = elData.getChild(Geonet.SearchResult.ANY);
		Element  elThemeKey    = elData.getChild(Geonet.SearchResult.THEME_KEY);
		Element  elRegion      = elData.getChild(Geonet.SearchResult.REGION);
		Element  elSouthBL     = elData.getChild(Geonet.SearchResult.SOUTH_BL);
		Element  elNorthBL     = elData.getChild(Geonet.SearchResult.NORTH_BL);
		Element  elEastBL      = elData.getChild(Geonet.SearchResult.EAST_BL);
		Element  elWestBL      = elData.getChild(Geonet.SearchResult.WEST_BL);
		Element  elRelation    = elData.getChild(Geonet.SearchResult.RELATION);
		Element  elFrom        = elData.getChild(Geonet.SearchResult.FROM);
		Element  elTo          = elData.getChild(Geonet.SearchResult.TO);
		Element  elDownload    = elData.getChild(Geonet.SearchResult.DOWNLOAD);
		Element  elOnLine      = elData.getChild(Geonet.SearchResult.ONLINE);
		Element  elDigital     = elData.getChild(Geonet.SearchResult.DIGITAL);
		Element  elPaper       = elData.getChild(Geonet.SearchResult.PAPER);
		Element  elCategory    = elData.getChild(Geonet.SearchResult.CATEGORY);
		Element  elSource      = elData.getChild(Geonet.SearchResult.SITE_ID);
		Element  elTemplate    = elData.getChild(Geonet.SearchResult.TEMPLATE);

		Element  elGroup       = elData.getChild(Geonet.SearchResult.GROUP);

		Element  elProfile     = elData.getChild(Geonet.SearchResult.PROFILE);
		Element  elServer      = elData.getChild(Geonet.SearchResult.SERVERS);
		Element  elTimeout     = elData.getChild(Geonet.SearchResult.TIMEOUT);

		Element  elExtended    = elData.getChild(Geonet.SearchResult.EXTENDED);
		Element  elHelp        = elData.getChild(Geonet.SearchResult.HELP);
		Element  elRemote      = elData.getChild(Geonet.SearchResult.REMOTE);

		Element  elHitsPerPage = elData.getChild(Geonet.SearchResult.HITS_PER_PAGE);

		// get params from request
		if (request != null)
		{
			String   sTitle        = request.getChildText(Geonet.SearchResult.TITLE);
			String   sAbstract     = request.getChildText(Geonet.SearchResult.ABSTRACT);
			String   sAny          = request.getChildText(Geonet.SearchResult.ANY);
			String   sThemeKey     = request.getChildText(Geonet.SearchResult.THEME_KEY);
			String   sRegion       = request.getChildText(Geonet.SearchResult.REGION);
			String   sSouthBL      = request.getChildText(Geonet.SearchResult.SOUTH_BL);
			String   sNorthBL      = request.getChildText(Geonet.SearchResult.NORTH_BL);
			String   sEastBL       = request.getChildText(Geonet.SearchResult.EAST_BL);
			String   sWestBL       = request.getChildText(Geonet.SearchResult.WEST_BL);
			String   sRelation     = request.getChildText(Geonet.SearchResult.RELATION);
			String   sFrom         = request.getChildText(Geonet.SearchResult.FROM);
			String   sTo           = request.getChildText(Geonet.SearchResult.TO);
			String   sDownload     = request.getChildText(Geonet.SearchResult.DOWNLOAD);
			String   sOnLine       = request.getChildText(Geonet.SearchResult.ONLINE);
			String   sDigital      = request.getChildText(Geonet.SearchResult.DIGITAL);
			String   sPaper        = request.getChildText(Geonet.SearchResult.PAPER);
			String   sTemplate     = request.getChildText(Geonet.SearchResult.TEMPLATE);
			String   sCategory     = request.getChildText(Geonet.SearchResult.CATEGORY);
			String   sSource       = request.getChildText(Geonet.SearchResult.SITE_ID);
			String   sGroup        = request.getChildText(Geonet.SearchResult.GROUP);
			String   sProfile      = request.getChildText(Geonet.SearchResult.PROFILE);
			String   sTimeout      = request.getChildText(Geonet.SearchResult.TIMEOUT);
			String   sHitsPerPage  = request.getChildText(Geonet.SearchResult.HITS_PER_PAGE);
			String   sExtended     = request.getChildText(Geonet.SearchResult.EXTENDED);
			String   sHelp         = request.getChildText(Geonet.SearchResult.HELP);
			String   sRemote       = request.getChildText(Geonet.SearchResult.REMOTE);
			Iterator iServer       = request.getChildren (Geonet.SearchResult.SERVERS).iterator();

			if (sTitle       != null) elTitle.setText(sTitle);
			if (sAbstract    != null) elAbstract.setText(sAbstract);
			if (sAny         != null) elAny.setText(sAny);
			if (sThemeKey    != null) elThemeKey.setText(sThemeKey);
			if (sRegion      != null) elRegion.setText(sRegion);
			if (sSouthBL     != null) elSouthBL.setText(sSouthBL);
			if (sNorthBL     != null) elNorthBL.setText(sNorthBL);
			if (sEastBL      != null) elEastBL.setText(sEastBL);
			if (sWestBL      != null) elWestBL.setText(sWestBL);
			if (sRelation    != null) elRelation.setText(sRelation);
			if (sFrom        != null) elFrom.setText(sFrom);
			if (sTo          != null) elTo.setText(sTo);
			if (sGroup       != null) elGroup.setText(sGroup);
			if (sProfile     != null) elProfile.setText(sProfile);
			if (sTimeout     != null) elTimeout.setText(sTimeout);
			if (sHelp        != null) elHelp.setText(sHelp);
			if (sRemote      != null) elRemote.setText(sRemote);
			if (sHitsPerPage != null) elHitsPerPage.setText(sHitsPerPage);
			if (sCategory    != null) elCategory.setText(sCategory);
			if (sSource      != null) elSource.setText(sSource);

			if (sDigital     != null) elDigital.setText(sDigital);
			else                      elDigital.setText(Geonet.Text.OFF);

			if (sPaper       != null) elPaper.setText(sPaper);
			else                      elPaper.setText(Geonet.Text.OFF);

			// only save these checkbox values if mode is extended
			if (elExtended.getText().equals(Geonet.Text.ON))
			{
				if (sTemplate  != null) elTemplate.setText(sTemplate);

				if (sDownload != null) elDownload.setText(sDownload);
				else                   elDownload.setText(Geonet.Text.OFF);

				if (sOnLine != null)   elOnLine.setText(sOnLine);
				else                   elOnLine.setText(Geonet.Text.OFF);
			}
			// now you can change sExtended
			if (sExtended    != null) elExtended.setText(sExtended);

			elServer.removeContent();
			while (iServer.hasNext())
			{
				Element elSrv = (Element)iServer.next();
				elServer.addContent(new Element(Geonet.SearchResult.SERVER).addContent(elSrv.getText()));
			}
		}
		return elData;
	}

	//--------------------------------------------------------------------------

	public static String splitWord(String requestStr)
	{
		Analyzer a = new StandardAnalyzer();
//    Analyzer a = new CJKAnalyzer();

		StringReader sr = new StringReader(requestStr);
		TokenStream  ts = a.tokenStream(sr);

		String result=new String("");

		try
		{
			Token t = ts.next();

			while(t != null)
			{
				result += (" "+ t.termText());
				t = ts.next();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return result;
	}
}

//=============================================================================

