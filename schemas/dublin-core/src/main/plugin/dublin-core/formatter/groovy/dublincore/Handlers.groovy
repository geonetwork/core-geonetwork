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

package dublincore

import org.fao.geonet.api.records.formatters.groovy.Environment

class Handlers {
    public static final String TITLE_EL_NAME = 'dc:title'
    protected org.fao.geonet.api.records.formatters.groovy.Handlers handlers
    protected org.fao.geonet.api.records.formatters.groovy.Functions f
    protected Environment env
    common.Handlers commonHandlers
    public String rootEl
    def urlElts = ['dct:references']
    String rootEl



    Handlers(handlers, f, env) {
        this(handlers, f, env, "simpledc")
    }

    Handlers(handlers, f, env, rootEl) {
        this.handlers = handlers
        this.f = f
        this.env = env
        this.rootEl = rootEl
        commonHandlers = new common.Handlers(handlers, f, env)
    }

    void addDefaultHandlers() {
        commonHandlers.addDefaultStartAndEndHandlers()
        handlers.add name: "Normal Elements", select: {!it.text().isEmpty()}, group:true, handleNormalEls
    }

    def handleNormalEls = { els ->
        def replacements = els
                .groupBy({f.nodeLabel(it.name(), null)})
                .sort({el1, el2 ->
                    if (el1.key == f.nodeLabel(TITLE_EL_NAME, null))    return -1
                    if (el2.key == f.nodeLabel(TITLE_EL_NAME, null))    return 1
                    if (el1.value.size < el2.value.size)                return -1
                    if (el2.value.size < el1.value.size)                return 1
                    return el1.key <=> el2.key})
                .inject('', {entries, entry ->
                    if (entry.value.size() > 1) {
                        return entries + handlers.fileResult("html/list-entry.html", [label: entry.key, listItems: entry.value])
                    }
                    def el = entry.value.iterator().next()
                    if(!urlElts.contains(el.name())) {
                        return entries + handlers.fileResult("html/text-el.html", [label: entry.key, text: el.text()])
                    }
                    return entries + handlers.fileResult("html/url-el.html", ["label": entry.key, "href" : el.text(), "text" :
                            el.text().length() > 50 ? (el.text().substring(0, 50) + "...") : el.text()])})

        return handlers.fileResult("html/2-level-entry.html", [label: f.nodeLabel(rootEl, null), childData: replacements])
    }
}
