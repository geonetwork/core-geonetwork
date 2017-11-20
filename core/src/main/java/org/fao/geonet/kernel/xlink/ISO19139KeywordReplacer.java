//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.xlink;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.schema.subtemplate.Status;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by fgravin on 10/26/17.
 */
public class ISO19139KeywordReplacer {

    protected static final String ROOT_XML_PATH = ".//gmd:MD_Keywords/parent::*";

    protected static final String localXlinkUrlPrefix = "local://srv/api/registries/vocabularies/keyword?";

    @Autowired
    protected IsoLanguagesMapper isoLanguagesMapper;

    @Autowired
    private ThesaurusManager thesaurusManager;

    public ISO19139KeywordReplacer() {}

    public Status replaceAll(Element md) {
       ReplacerWorker worker = new ReplacerWorker(isoLanguagesMapper, thesaurusManager);
       return worker.replaceAll(md);
    }

    protected KeywordBean searchInAnyThesaurus(String keyword) {
        ReplacerWorker worker = new ReplacerWorker(isoLanguagesMapper, thesaurusManager);
        return worker.searchInAnyThesaurus(keyword);

    }
}
