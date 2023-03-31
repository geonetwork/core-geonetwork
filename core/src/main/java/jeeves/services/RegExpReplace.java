//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.services;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//=============================================================================

/**
 * This service reads a configuration xml file containing pattern-replacement pairs and
 * applies all the pairs to each text element of the output. The configuration is read from
 * xml/regexp.xml and is formatted: <regexp> <expr pattern="..." replacement="..."/> </regexp>
 */

public class RegExpReplace implements Service {
    private Vector<Pattern> patterns;
    private Vector<String> replacements;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void init(Path appPath, ServiceConfig params) throws Exception {
        String file = params.getMandatoryValue(Jeeves.Config.FILE);
        Element config = Xml.loadFile(appPath.resolve(file));

        patterns = new Vector<Pattern>();
        replacements = new Vector<String>();

        // read all pattern-replacement pairs
        for (Element expr : (List<Element>) config.getChildren()) {
            String pattern = expr.getAttributeValue(Jeeves.Attr.PATTERN);
            String replacement = expr.getAttributeValue(Jeeves.Attr.REPLACEMENT);

            patterns.add(Pattern.compile(pattern));
            replacements.add(getRealText(replacement));
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        replaceInElement(params);

        return params;
    }

    //--------------------------------------------------------------------------
    //--- replace some entities

    private String getRealText(String text) {
        String noLt = replace(text, "&lt;", "<");
        String noGt = replace(noLt, "&gt;", ">");
        String noAmp = replace(noGt, "&amp;", "&");

        return noAmp;
    }

    //--------------------------------------------------------------------------

    private String replace(String text, String delim, String replacement) {
        StringBuffer result = new StringBuffer();
        StringTokenizer st = new StringTokenizer(text, delim, true);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (token.equals(delim)) result.append(replacement);
            else result.append(token);
        }

        return result.toString();
    }

    //--------------------------------------------------------------------------
    //--- apply all pattern-replacement pairs

    @SuppressWarnings("unchecked")
    private void replaceInElement(Element e) {
        if (e.getChildren().size() != 0)
            for (Iterator<Element> iter = e.getChildren().iterator(); iter.hasNext(); )
                replaceInElement(iter.next());
        else {
            String text = e.getText();

            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = (Pattern) patterns.get(i);
                String replacement = (String) replacements.get(i);
                Matcher matcher = pattern.matcher(text);

                text = matcher.replaceAll(replacement);
            }
            e.setText(text);
        }
    }
}

//=============================================================================

