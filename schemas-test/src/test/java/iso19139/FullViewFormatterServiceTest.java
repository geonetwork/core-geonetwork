/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package iso19139;

import com.google.common.collect.Lists;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterServiceTest extends AbstractFullViewFormatterTest {

    @Test
    public void testDummy() {
    }

    @Test
    @Ignore
    public void testServiceMdFormatting() throws Exception {
        super.testPrintFormat();
    }

    protected List<String> excludes() {
        return Lists.newArrayList(
            "> gmd:MD_Metadata > gmd:parentIdentifier > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:citation > gmd:CI_Citation > gmd:edition > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:graphicOverview > gmd:MD_BrowseGraphic > gmd:fileDescription > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:graphicOverview > gmd:MD_BrowseGraphic > gmd:fileType > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:date > gco:Date > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:identifier > gmd:MD_Identifier > gmd:code > gmx:Anchor > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:aggregationInfo > gmd:MD_AggregateInformation > gmd:aggregateDataSetIdentifier > gmd:RS_Identifier > gmd:code > gmx:FileName > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > srv:serviceType > gco:LocalName > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > srv:containsOperations > srv:SV_OperationMetadata > srv:connectPoint > gmd:CI_OnlineResource > gmd:protocol > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > srv:containsOperations > srv:SV_OperationMetadata > srv:connectPoint > gmd:CI_OnlineResource > gmd:function > gmd:CI_OnLineFunctionCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:type > gmd:MD_KeywordTypeCode> @codeListValue"
        );
    }

    @Override
    protected File getTestMetadataFile() throws Exception {
        final URL mdFile = FullViewFormatterServiceTest.class.getResource("/iso19139/example-service.xml");
        return new File(mdFile.toURI());
    }
}
