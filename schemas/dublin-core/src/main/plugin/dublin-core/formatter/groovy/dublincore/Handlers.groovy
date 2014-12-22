package dublincore

import org.fao.geonet.services.metadata.format.groovy.Environment
import org.fao.geonet.services.metadata.format.groovy.util.*

public class Handlers {
    public static final String TITLE_EL_NAME = 'dc:title'
    public static final String DESC_EL_NAME = 'dc:description'
    protected org.fao.geonet.services.metadata.format.groovy.Handlers handlers;
    protected org.fao.geonet.services.metadata.format.groovy.Functions f
    protected Environment env
    common.Handlers commonHandlers
    public String rootEl
    def excludedEls = []

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
        summary.addCompleteNavItem = false
        summary.addOverviewNavItem = false

        LinkBlock linkBlock = new LinkBlock(f.translate("links"));
        summary.links.add(linkBlock)
        def toLink = { linkEl ->
            Link link;
            try {
                def href = linkEl.text()
                if (href.contains("://")) {
                    href = "window.open('${new URI(linkEl.text())}', 'link')"
                }

                link = new Link(href, href);

            } catch (URISyntaxException e) {
                link = new Link("alert('${f.translate('notValidUri')}')", linkEl.text());
            }

            return link
        }

        def relatedLinkType = new LinkType("related", null)
        def referencesLinkType = new LinkType("references", null)
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
                singles.append(handlers.fileResult("html/text-el.html", [label: f.nodeLabel(el), text: el.text()]))
            }
        }

        return handlers.fileResult("html/2-level-entry.html", [label: f.nodeLabel(rootEl, null), childData: singles.toString() + multiples])
    }

    public String getAbstract(el) {
        return firstNonEmpty(el[DESC_EL_NAME])
    }
}