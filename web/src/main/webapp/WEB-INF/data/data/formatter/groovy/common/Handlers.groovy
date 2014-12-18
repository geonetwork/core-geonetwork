package common

import org.fao.geonet.services.metadata.format.groovy.Environment

public class Handlers {
    private org.fao.geonet.services.metadata.format.groovy.Handlers handlers;
    private org.fao.geonet.services.metadata.format.groovy.Functions f
    private Environment env

    common.Matchers matchers
    common.Functions func

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        func = new common.Functions(handlers: handlers, f:f, env:env)
        matchers =  new common.Matchers(handlers: handlers, f:f, env:env)
    }

    def addDefaultStartAndEndHandlers() {
        handlers.start htmlOrXmlStart
        handlers.end htmlOrXmlEnd
    }

    def entryEl(labeller) {
        return entryEl(labeller, null)
    }
    /**
     * Creates a function that will process all children and sort then according to the sorter that applies to the elements. Then
     * returns the default html for the container elements.
     *
     * @param labeller a function for creating a label from the element
     * @param classer a function taking the element class(es) to add to the entry element.  The method should return a string.
     */
    def entryEl(labeller, classer) {
        return { el ->
            def childData = handlers.processElements(el.children(), el);
            def replacement = [label: labeller(el), childData: childData, name:'']

            if (classer != null) {
                replacement.name = classer(el);
            }

            if (!childData.isEmpty()) {
                return handlers.fileResult('html/2-level-entry.html', replacement)
            }
            return null
        }
    }
    def processChildren(childSelector) {
        return {el ->
            handlers.processElements(childSelector(el), el);
        }
    }
    /**
     * Creates a function which will:
     *
     * 1. Select a single element using the selector function
     * 2. Process all children of the element selected in step 1 with sorter that applies to the element selected in step 1
     * 3. Create a label using executing the labeller on the element passed to handler functions (not element selected in step 1)
     *
     * @param selector a function that will select a single element from the descendants of the element passed to it
     * @param labeller a function for creating a label from the element
     */
    def flattenedEntryEl(selector, labeller) {
        return { parentEl ->
            def el = selector(parentEl)
            def childData = handlers.processElements(el.children(), el);

            if (!childData.isEmpty()) {
                return handlers.fileResult('html/2-level-entry.html', [label: labeller(el), childData: childData])
            }
            return null
        }
    }

    def selectIsotype(name) {
        return {
            it.children().find { ch ->
                ch.name() == name || ch['@gco:isoType'].text() == name
            }
        }
    }

    def htmlOrXmlStart = {
        if (func.isHtmlOutput()) {
            def minimize = ''
            if (env.param("debug").toBool()) {
                minimize = '?minimize=false'
            }
            return """
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8"/>
    <link rel="stylesheet" href="../../static/gn_bootstrap.css$minimize"/>
    <link rel="stylesheet" href="../../static/gn_metadata.css$minimize"/>
    <script src="../../static/lib.js$minimize"></script>
</head>
<body>
"""
        } else {
            return ''
        }
    }

    def htmlOrXmlEnd = {
        def required = """
<script type="text/javascript">
//<![CDATA[
        ${handlers.fileResult("js/std-footer.js", [:])}
//]]>
</script>"""
        if (func.isHtmlOutput()) {
            return required + '</body></html>'
        } else {
            return required
        }
    }

}