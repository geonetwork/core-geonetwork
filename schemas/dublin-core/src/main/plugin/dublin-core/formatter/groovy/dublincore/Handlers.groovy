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
import org.fao.geonet.api.records.formatters.groovy.util.*

public class Handlers {
    public static final String TITLE_EL_NAME = 'dc:title'
    public static final String DESC_EL_NAME = 'dc:description'
    protected org.fao.geonet.api.records.formatters.groovy.Handlers handlers;
    protected org.fao.geonet.api.records.formatters.groovy.Functions f
    protected Environment env
    common.Handlers commonHandlers
    public String rootEl
    def excludedEls = []
    def urlElts = ['dc:relation']

    public Handlers(handlers, f, env) {
        this(handlers, f, env, "simpledc")
    }

    public Handlers(handlers, f, env, rootEl) {
        this.handlers = handlers
        this.f = f
        this.env = env
        commonHandlers = new common.Handlers(handlers, f, env)
        this.rootEl = rootEl
        excludedEls << rootEl
    }

    public void addDefaultHandlers() {
        commonHandlers.addDefaultStartAndEndHandlers()

        handlers.add name: "Normal Elements", select: {
            !excludedEls.contains(it.name()) && !it.text().isEmpty()
        }, group:true, handleNormalEls
        handlers.add name: 'Root Element', select: rootEl, priority: -1, handleRootEl
    }

    def firstNonEmpty(el) {
        if (el.text().isEmpty()) {
            return null;
        } else {
            return el.find { !it.text().isEmpty() }.text()
        }
    }

    def handleRootEl = { el ->
        Summary summary = new Summary(handlers, env, f);

        summary.title = firstNonEmpty(el[TITLE_EL_NAME])
        summary.abstr = getAbstract(el)
        summary.content = handlers.processElements(el.children())
        summary.addNavBarItem(new NavBarItem(f.translate('complete'), null, '.container > .entry:not(.overview)'))
        summary.addNavBarItem(commonHandlers.createXmlNavBarItem())
        summary.addCompleteNavItem = false
        summary.addOverviewNavItem = true

        LinkBlock linkBlock = new LinkBlock(f.translate("links"), "fa fa-link");
        summary.links.add(linkBlock)
        def toLink = { linkEl ->
            Link link;
            try {
                def href = linkEl.text()
                link = new Link(href, href);

            } catch (URISyntaxException e) {
                link = new Link("alert('${f.translate('notValidUri')}')", linkEl.text());
            }

            return link
        }

        def relatedLinkType = new LinkType("related", null, null, "fa fa-sitemap")
        def referencesLinkType = new LinkType("references", null, null, "fa fa-arrows-h")
        el.'dc:relation'.each{linkBlock.put(relatedLinkType, toLink(it))}
        el.'dc:URI'.each{linkBlock.put(referencesLinkType, toLink(it))}

        summary.result
    }

    def handleNormalEls = { els ->
        def sections = new TreeMap(
                [compare:{el1, el2 ->
                    if (el1 == TITLE_EL_NAME) return -1
                    else if (el2 == TITLE_EL_NAME) return 1
                    else return f.nodeLabel(el1, null).compareTo(f.nodeLabel(el2, null))
                }] as Comparator);


        els.each{
            def list = sections[it.name()]
            list = list == null ? [] : list
            list << it
            sections.put(it.name(), list)
        }

        def singles = new StringBuilder()
        def multiples = new StringBuilder()
        sections.entrySet().each { entry ->
            if (entry.value.size() > 1) {
                multiples.append(handlers.fileResult("html/list-entry.html", [label: f.nodeLabel(entry.key, null), listItems: entry.value]))
            } else {
                def el = entry.value.iterator().next()
                if(!urlElts.contains(el.name()))
                    singles.append(handlers.fileResult("html/text-el.html", [label: f.nodeLabel(el), text: el.text()]))
                else
                    singles.append(handlers.fileResult("html/url-el.html", ["label": f.nodeLabel(el), "href" : el.text(), "text" :
                            el.text().length() > 50 ? (el.text().substring(0, 50) + "...") : el.text()]))

            }
        }

        return handlers.fileResult("html/2-level-entry.html", [label: f.nodeLabel(rootEl, null), childData: singles.toString() + multiples])
    }

    public String getAbstract(el) {
        return firstNonEmpty(el[DESC_EL_NAME])
    }
}
