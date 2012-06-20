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

package org.fao.geonet.kernel.search;

import java.io.IOException;
import java.util.List;

import org.fao.geonet.util.LangUtils;
import org.jdom.Element;
import org.jdom.JDOMException;


/**
 * Translates code list keys into a language
 * @author jesse
 */
public class CodeListTranslator extends Translator
{
    private final List<Element> _codeList;
    public CodeListTranslator(String schemaDir, String langCode, String codeListName) throws IOException, JDOMException
    {
        _codeList = LangUtils.loadCodeListFile(schemaDir, langCode, codeListName);
    }

    public String translate(String key)
    {
        if( _codeList==null ){
            return key;
        }

        for (Element entry : _codeList) {
            // Case insensitive match because most of Lucene
            // fields are processed by StandardAnalyzer (which turn terms to lowercase).
            if(entry.getChildText("code").toLowerCase().equals(key.toLowerCase())){
                return entry.getChildText("label");
            }
        }

        return key;
    }

}
