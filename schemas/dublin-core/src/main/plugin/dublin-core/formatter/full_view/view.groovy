/*
 * The view.groovy script is a groovy script which must configure the handlers object
 * (org.fao.geonet.services.metadata.format.groovy.Handlers).  To script has
 * the following variables bound before execution:
 *
 * - handlers - an org.fao.geonet.services.metadata.format.groovy.Handlers object
 * - f - an org.fao.geonet.services.metadata.format.groovy.Functions object
 * - env - an org.fao.geonet.services.metadata.format.groovy.Environment object.
 *         *IMPORTANT* this object can only be used during process time. When this
 *                     script is executed the org.fao.geonet.services.metadata.format.groovy.Transformer
 *                     object is created but not executed.  The transformer is cached so that the
 *                     groovy processing only needs to be executed once.
 */

/*
def dcHandlers = new common.Handlers(handlers, f, env)
handlers.add select:"simpledc", {
    el ->
        def replacements = [
                title: el.'dc:title'.text(),
                creator: el.'dc:creator'.text()
        ]
        handlers.fileResult("html/main.html", replacements)
}
*/

def dcHandlers = new common.Handlers(handlers, f, env)
dcHandlers.addDefaultStartAndEndHandlers()

handlers.add select:'dc:title', priority:10, { el ->
    // Don't need a return because last expression of a function is
    // always returned in groovy
    """<h1>
         ${el.text()}
       </h1>"""
}

handlers.add name:"allText",
        select:{!it.name().equals("simpledc") && !it.text().isEmpty()},
        { el ->
            dcHandlers.func.textEl f.nodeLabel(el), el.text()
        }

handlers.add name: 'container el', select: "simpledc", priority: -1, dcHandlers.entryEl(f.&nodeLabel)

handlers.sort name: 'container sorter', select: 'simpledc', {
    el1, el2 ->
        if(el1.name() == 'dc:title') return -1
        else if(el2.name() == 'dc:title') return 1
        else return el1.name().compareTo(el2.name())
}