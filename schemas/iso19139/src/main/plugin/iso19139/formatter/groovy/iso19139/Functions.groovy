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

package iso19139

import org.fao.geonet.domain.ISODate

import java.text.SimpleDateFormat

public class Functions {
    static final def CHAR_PATTERN = /\W/

    def handlers;
    def f
    def env
    common.Handlers commonHandlers

    def clean = { text ->
        if (text == null) {
            return ''
        }
        def trimmed = text.trim()
        if ((trimmed  =~ CHAR_PATTERN).matches()) {
            trimmed = '';
        }
        return trimmed;
    }

    def isoUrlText = { el ->
        el.'gmd:URL'.text()
    }

    def isoAnchorUrlLink = { el ->
        el.'gmx:Anchor'['@xlink:href'].text()
    }

    def isoAnchorUrlText = { el ->
        el.'gmx:Anchor'.text()
    }

    def isoText = { el ->
        def uiCode2 = '#'+env.lang2.toUpperCase()
        def uiCode3 = '#'+env.lang3.toUpperCase()

        def locStrings = el.'**'.findAll{ it.name() == 'gmd:LocalisedCharacterString' && !it.text().isEmpty()}
        def ptEl = locStrings.find{(it.'@locale' == uiCode2 || it.'@locale' == uiCode3)}
        if (ptEl != null) return ptEl.text()

        def charString = el.'**'.findAll {it.name() == 'gco:CharacterString' && !it.text().isEmpty()}
        if (!charString.isEmpty()) return charString[0].text()
        if (!locStrings.isEmpty()) return locStrings[0].text()
        ""
    }

    def dateText = { el ->

        String date = el.'gco:Date'.text()
        String dateTime = el.'gco:DateTime'.text()
        if (!date.isEmpty()) {
            return date;
        } else if (!dateTime.isEmpty()){
            ISODate isoDate = new ISODate(dateTime)
            return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(isoDate.toDate())
        }
    }
    /**
     * A shortcut for: commonHandlers.func.textEl(node), text))
     * @return
     */
    def isoTextEl(node, text) {
        return commonHandlers.func.textEl(f.nodeLabel(node), text)
    }

    /**
     * A shortcut for: commonHandlers.func.wikiTextEl(node), text))
     * @return
     */
    def isoWikiTextEl(node, text) {
        return commonHandlers.func.wikiTextEl(f.nodeLabel(node), text)
    }

    /**
     * A shortcut for: commonHandlers.func.textEl(node), text))
     * @return
     */
    def isoUrlEl(node, href, text) {
        return commonHandlers.func.urlEl(f.nodeLabel(node), href, text)
    }
}