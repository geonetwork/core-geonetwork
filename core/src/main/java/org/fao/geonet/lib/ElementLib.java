//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.lib;

import org.fao.geonet.Util;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

//=============================================================================

public class ElementLib {
    //-----------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //-----------------------------------------------------------------------------

    public Set<String> getIds(Element elem) {
        HashSet<String> hs = new HashSet<>();

        for (Object child : elem.getChildren())
            hs.add(((Element) child).getChildText("id"));

        return hs;
    }

    //-----------------------------------------------------------------------------

    public Element pruneChildren(Element elem, Set<Integer> ids) {
        ArrayList<Element> alToPrune = new ArrayList<>();

        //--- collect elements to prune

        for (Object obj : elem.getChildren()) {
            Element child = (Element) obj;
            String id = child.getChildText("id");

            if (!ids.contains(Integer.valueOf(id)))
                alToPrune.add(child);
        }

        //--- remove collected elements

        for (Element child : alToPrune)
            child.detach();

        return elem;
    }

    //-----------------------------------------------------------------------------

    public void add(Element el, String name, Object value) {
        if (value != null)
            el.addContent(new Element(name).setText(value.toString()));
    }

    //-----------------------------------------------------------------------------

    public String eval(Element elem, String path) {
        StringTokenizer st = new StringTokenizer(path, "/");

        while (st.hasMoreTokens()) {
            elem = elem.getChild(st.nextToken());

            if (elem == null)
                return null;
        }

        return elem.getText().trim();
    }

    //-----------------------------------------------------------------------------

    public void substitute(Element el, Map<String, ? extends Object> vars) {
        //--- handle attributes

        @SuppressWarnings("unchecked")
        List<Attribute> attributes = el.getAttributes();
        for (Attribute a : attributes) {
            String text = a.getValue();
            text = substitute(text, vars);
            a.setValue(text);
        }

        //--- handle children

        for (int i = 0; i < el.getContentSize(); i++) {
            Content c = el.getContent(i);

            if (c instanceof Element)
                substitute((Element) c, vars);

            else if (c instanceof Text) {
                Text t = (Text) c;

                String text = t.getText();
                text = substitute(text, vars);
                t.setText(text);
            }
        }
    }

    //-----------------------------------------------------------------------------

    private String substitute(String text, Map<String, ? extends Object> vars) {
        for (Map.Entry<String, ? extends Object> entry : vars.entrySet()) {
            String name = entry.getKey();
            String value = "";
            if (entry.getValue() != null) value = entry.getValue().toString();
            text = Util.replaceString(text, name, value);
        }

        return text;
    }
}

//=============================================================================

