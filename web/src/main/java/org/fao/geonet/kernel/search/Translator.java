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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jeeves.resources.dbms.Dbms;

import org.jdom.JDOMException;

/**
 * Takes a key and looks up a translation for that key.
 * 
 * @author jesse
 */
public abstract class Translator
{
    
    public static Translator PASS_THROUGH = new Translator(){
        public String translate(String key){ return key; }
    };
    
    public abstract String translate(String key);
    
    
    private final static Map<String, Translator> cache = Collections.synchronizedMap(new HashMap<String, Translator>()); 

    public static synchronized Translator createTranslator(String translatorString, String schemaDir, String langCode, Dbms dbms) throws IOException, JDOMException
    {
        if( translatorString == null || translatorString.length()==0){
            return Translator.PASS_THROUGH;
        }
        String key = translatorString + langCode;
        synchronized (cache) {
            if(cache.containsKey(key)) {
                return cache.get(key);
            }
        }

        String[] parts = translatorString.split(":",2);
        if( parts.length!=2 ){
            throw new AssertionError("the 'translation' element of the config-summary.xml must be of the form nameOfTranslator:TranslatorParam");
        }
        String type = parts[0];
        String param = parts[1];

        Translator translator;
        if( type.equals("codelist") ){
            translator = new CodeListTranslator(schemaDir, langCode, param);
        } else if( dbms != null && type.equals("db") ){
            // do not cache
            return new DbDescTranslator(dbms, langCode, param);
        } else {
            throw new AssertionError(type+" is not a recognized type of translator");
        }
        
        synchronized (cache) {
            cache.put(key, translator);
            return translator;
        }
    }

}
