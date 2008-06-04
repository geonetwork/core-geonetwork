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

public class MainUtil
{
	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/**
	 * Returns default values for the search parameters. If parameters have
	 * changed in the user session, they are read out here.
	 * @param srvContext
	 * @param request
	 * @return
	 */
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
						.addContent(new Element(Geonet.SearchResult.PHRASE)       .setText(""))
						.addContent(new Element(Geonet.SearchResult.OR)           .setText(""))
						.addContent(new Element(Geonet.SearchResult.WITHOUT)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.SOUTH_BL)     .setText("-90"))
						.addContent(new Element(Geonet.SearchResult.NORTH_BL)     .setText("90"))
						.addContent(new Element(Geonet.SearchResult.EAST_BL)      .setText("180"))
						.addContent(new Element(Geonet.SearchResult.WEST_BL)      .setText("-180"))
						.addContent(new Element(Geonet.SearchResult.RELATION)     .setText(Geonet.SearchResult.Relation.OVERLAPS))
						.addContent(new Element(Geonet.SearchResult.DATE_FROM)    .setText(""))
						.addContent(new Element(Geonet.SearchResult.DATE_TO)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.THEME_KEY)    .setText(""))
                        .addContent(new Element(Geonet.SearchResult.CATEGORY)     .setText(""))
                        .addContent(new Element(Geonet.SearchResult.TOPIC_CAT)    .setText(""))
                        .addContent(new Element(Geonet.SearchResult.PROTOCOL)     .setText(""))
                        .addContent(new Element(Geonet.SearchResult.DOWNLOAD)     .setText(Geonet.Text.OFF))
                        .addContent(new Element(Geonet.SearchResult.DYNAMIC)      .setText(Geonet.Text.OFF))
                        .addContent(new Element(Geonet.SearchResult.DIGITAL)      .setText(Geonet.Text.OFF))
                        .addContent(new Element(Geonet.SearchResult.PAPER)        .setText(Geonet.Text.OFF))
                        .addContent(new Element(Geonet.SearchResult.SITE_ID)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.GROUP)        .setText(""))
						.addContent(new Element(Geonet.SearchResult.PROFILE)      .setText(""))
						.addContent(new Element(Geonet.SearchResult.SERVERS))
                        .addContent(new Element(Geonet.SearchResult.TEMPLATE)     .setText("n"))
                        .addContent(new Element(Geonet.SearchResult.EXTENDED)     .setText(Geonet.Text.OFF))
                        .addContent(new Element(Geonet.SearchResult.REMOTE)       .setText(Geonet.Text.OFF))
                        .addContent(new Element(Geonet.SearchResult.TIMEOUT)      .setText("20"))
                        .addContent(new Element(Geonet.SearchResult.HITS_PER_PAGE).setText("10"))
                        .addContent(new Element(Geonet.SearchResult.SIMILARITY)   .setText(".8"))
						.addContent(new Element(Geonet.SearchResult.OUTPUT)       .setText(Geonet.SearchResult.Output.FULL))
						.addContent(new Element(Geonet.SearchResult.SORT_BY)      .setText(Geonet.SearchResult.SortBy.RELEVANCE))
			            .addContent(new Element(Geonet.SearchResult.INTERMAP)     .setText(Geonet.Text.ON));
			session.setProperty(Geonet.Session.MAIN_SEARCH, elData);
		}

		Element  elTitle       = elData.getChild(Geonet.SearchResult.TITLE);
		Element  elAbstract    = elData.getChild(Geonet.SearchResult.ABSTRACT);
		Element  elAny         = elData.getChild(Geonet.SearchResult.ANY);
		Element  elRegion      = elData.getChild(Geonet.SearchResult.REGION);
		Element  elPhrase      = elData.getChild(Geonet.SearchResult.PHRASE);
		Element  elOr          = elData.getChild(Geonet.SearchResult.OR);
		Element  elWithout     = elData.getChild(Geonet.SearchResult.WITHOUT);
		Element  elSouthBL     = elData.getChild(Geonet.SearchResult.SOUTH_BL);
		Element  elNorthBL     = elData.getChild(Geonet.SearchResult.NORTH_BL);
		Element  elEastBL      = elData.getChild(Geonet.SearchResult.EAST_BL);
		Element  elWestBL      = elData.getChild(Geonet.SearchResult.WEST_BL);
		Element  elRelation    = elData.getChild(Geonet.SearchResult.RELATION);
		Element  elDateFrom    = elData.getChild(Geonet.SearchResult.DATE_FROM);
		Element  elDateTo      = elData.getChild(Geonet.SearchResult.DATE_TO);
        Element  elThemeKey    = elData.getChild(Geonet.SearchResult.THEME_KEY);
        Element  elCategory    = elData.getChild(Geonet.SearchResult.CATEGORY);
        Element  elTopicCat    = elData.getChild(Geonet.SearchResult.TOPIC_CAT);
        Element  elProtocol    = elData.getChild(Geonet.SearchResult.PROTOCOL);
		Element  elDownload    = elData.getChild(Geonet.SearchResult.DOWNLOAD);
		Element  elDynamic     = elData.getChild(Geonet.SearchResult.DYNAMIC);
		Element  elDigital     = elData.getChild(Geonet.SearchResult.DIGITAL);
		Element  elPaper       = elData.getChild(Geonet.SearchResult.PAPER);
		Element  elSource      = elData.getChild(Geonet.SearchResult.SITE_ID);
        Element  elGroup       = elData.getChild(Geonet.SearchResult.GROUP);
        Element  elProfile     = elData.getChild(Geonet.SearchResult.PROFILE);
        Element  elServer      = elData.getChild(Geonet.SearchResult.SERVERS);
        Element  elTemplate    = elData.getChild(Geonet.SearchResult.TEMPLATE);
        Element  elExtended    = elData.getChild(Geonet.SearchResult.EXTENDED);
        Element  elRemote      = elData.getChild(Geonet.SearchResult.REMOTE);
		Element  elTimeout     = elData.getChild(Geonet.SearchResult.TIMEOUT);
        Element  elHitsPerPage = elData.getChild(Geonet.SearchResult.HITS_PER_PAGE);
        Element  elSimilarity  = elData.getChild(Geonet.SearchResult.SIMILARITY);
		Element  elOutput      = elData.getChild(Geonet.SearchResult.OUTPUT);
		Element  elSortBy      = elData.getChild(Geonet.SearchResult.SORT_BY);
        Element  elIntermap    = elData.getChild(Geonet.SearchResult.INTERMAP);

		// get params from request
		if (request != null)
		{
			String   sTitle        = request.getChildText(Geonet.SearchResult.TITLE);
			String   sAbstract     = request.getChildText(Geonet.SearchResult.ABSTRACT);
			String   sAny          = request.getChildText(Geonet.SearchResult.ANY);
			String   sRegion       = request.getChildText(Geonet.SearchResult.REGION);
			String   sPhrase       = request.getChildText(Geonet.SearchResult.PHRASE);
			String   sOr           = request.getChildText(Geonet.SearchResult.OR);
			String   sWithout      = request.getChildText(Geonet.SearchResult.WITHOUT);
			String   sSouthBL      = request.getChildText(Geonet.SearchResult.SOUTH_BL);
			String   sNorthBL      = request.getChildText(Geonet.SearchResult.NORTH_BL);
			String   sEastBL       = request.getChildText(Geonet.SearchResult.EAST_BL);
			String   sWestBL       = request.getChildText(Geonet.SearchResult.WEST_BL);
			String   sRelation     = request.getChildText(Geonet.SearchResult.RELATION);
			String   sDateFrom     = request.getChildText(Geonet.SearchResult.DATE_FROM);
			String   sDateTo       = request.getChildText(Geonet.SearchResult.DATE_TO);
            String   sThemeKey     = request.getChildText(Geonet.SearchResult.THEME_KEY);
            String   sCategory     = request.getChildText(Geonet.SearchResult.CATEGORY);
            String   sTopicCat     = request.getChildText(Geonet.SearchResult.TOPIC_CAT);
            String   sProtocol     = request.getChildText(Geonet.SearchResult.PROTOCOL);
            String   sDownload     = request.getChildText(Geonet.SearchResult.DOWNLOAD);
			String   sDynamic      = request.getChildText(Geonet.SearchResult.DYNAMIC);
			String   sDigital      = request.getChildText(Geonet.SearchResult.DIGITAL);
			String   sPaper        = request.getChildText(Geonet.SearchResult.PAPER);
            String   sSource       = request.getChildText(Geonet.SearchResult.SITE_ID);
            String   sGroup        = request.getChildText(Geonet.SearchResult.GROUP);
            String   sProfile      = request.getChildText(Geonet.SearchResult.PROFILE);
            Iterator iServer       = request.getChildren (Geonet.SearchResult.SERVERS).iterator();
			String   sTemplate     = request.getChildText(Geonet.SearchResult.TEMPLATE);
            String   sExtended     = request.getChildText(Geonet.SearchResult.EXTENDED);
            String   sRemote       = request.getChildText(Geonet.SearchResult.REMOTE);
			String   sTimeout      = request.getChildText(Geonet.SearchResult.TIMEOUT);
			String   sHitsPerPage  = request.getChildText(Geonet.SearchResult.HITS_PER_PAGE);
			String   sSimilarity   = request.getChildText(Geonet.SearchResult.SIMILARITY);
			String   sOutput       = request.getChildText(Geonet.SearchResult.OUTPUT);
			String   sSortBy       = request.getChildText(Geonet.SearchResult.SORT_BY);
            String   sIntermap     = request.getChildText(Geonet.SearchResult.INTERMAP);

			if (sTitle       != null) elTitle.setText(sTitle);
			if (sAbstract    != null) elAbstract.setText(sAbstract);
			if (sAny         != null) elAny.setText(sAny);
			if (sRegion      != null) elRegion.setText(sRegion);
			if (sPhrase      != null) elPhrase.setText(sPhrase);	
			if (sOr          != null) elOr.setText(sOr);
			if (sWithout     != null) elWithout.setText(sWithout);
			if (sSouthBL     != null) elSouthBL.setText(sSouthBL);
			if (sNorthBL     != null) elNorthBL.setText(sNorthBL);
			if (sEastBL      != null) elEastBL.setText(sEastBL);
			if (sWestBL      != null) elWestBL.setText(sWestBL);
			if (sRelation    != null) elRelation.setText(sRelation);
			if (sDateFrom    != null) elDateFrom.setText(sDateFrom);
			if (sDateTo      != null) elDateTo.setText(sDateTo);
            if (sThemeKey    != null) elThemeKey.setText(sThemeKey);
            if (sCategory    != null) elCategory.setText(sCategory);
            if (sTopicCat    != null) elTopicCat.setText(sTopicCat);
            if (sProtocol    != null) elProtocol.setText(sProtocol);
            elDownload.setText(sDownload != null ? sDownload : Geonet.Text.OFF);
            elDynamic .setText(sDynamic  != null ? sDynamic  : Geonet.Text.OFF);
            elDigital .setText(sDigital  != null ? sDigital  : Geonet.Text.OFF);
            elPaper   .setText(sPaper    != null ? sPaper    : Geonet.Text.OFF);
            if (sSource      != null) elSource.setText(sSource);
            if (sGroup       != null) elGroup.setText(sGroup);
            if (sProfile     != null) elProfile.setText(sProfile);

            elServer.removeContent();
            while (iServer.hasNext())
            {
                Element elSrv = (Element)iServer.next();
                elServer.addContent(new Element(Geonet.SearchResult.SERVER).addContent(elSrv.getText()));
            }
            
            if (sTemplate    != null) elTemplate.setText(sTemplate);
            if (sExtended != null) elExtended.setText(sExtended); // now you can change sExtended
            if (sRemote      != null) elRemote.setText(sRemote);
            if (sTimeout     != null) elTimeout.setText(sTimeout);
            if (sHitsPerPage != null) elHitsPerPage.setText(sHitsPerPage);
            if (sSimilarity  != null) elSimilarity.setText(sSimilarity);

			if (sOutput != null)
			{
				if (sOutput.equals(Geonet.SearchResult.Output.TEXT))
					elOutput.setText(sOutput);
				else
					elOutput.setText(Geonet.SearchResult.Output.FULL);
			}

			if (sSortBy != null)
			{
				if (sSortBy.equals(Geonet.SearchResult.SortBy.DATE))
					elSortBy.setText(sSortBy);

				else if (sSortBy.equals(Geonet.SearchResult.SortBy.POPULARITY))
					elSortBy.setText(sSortBy);

				else if (sSortBy.equals(Geonet.SearchResult.SortBy.RATING))
					elSortBy.setText(sSortBy);
				else
					elSortBy.setText(Geonet.SearchResult.SortBy.RELEVANCE);
			}

            if (sIntermap    != null) elIntermap.setText(sIntermap);

		}

		srvContext.info("Returning search data");

		return elData;
	}

	//--------------------------------------------------------------------------

	/**
	 * Splits a text in tokens.
	 * @param requestStr
	 * @return
	 */
	public static String splitWord(String requestStr)
	{
		Analyzer a = new StandardAnalyzer();
//    Analyzer a = new CJKAnalyzer();

		StringReader sr = new StringReader(requestStr);
		TokenStream  ts = a.tokenStream(null, sr);

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

