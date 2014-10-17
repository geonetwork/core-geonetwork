package iso19139

public class Handlers {
    def handlers;
    def f
    Matchers matchers
    Functions isofunc
    common.Handlers commonHandlers

    public Handlers(handlers, f) {
        this.handlers = handlers
        this.f = f
        isofunc = new Functions(handlers: handlers, f:f)
        matchers =  new Matchers(handlers: handlers, f:f)
        commonHandlers = new common.Handlers(handlers, f)
    }

    def addDefaultHandlers() {
        handlers.add matchers.isTextEl, isoTextEl
        handlers.add matchers.isUrlEl, isoUrlEl
        handlers.add matchers.isCodeListEl, isoCodeListEl
        handlers.add select: matchers.isContainerEl, processChildren: true, priority: -1, isoEntry
        commonHandlers.addDefaultStartAndEndHandlers()
    }

    def isoTextEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.label(el))
                dd(isofunc.isoText(el))
            }
        }
    }

    def isoUrlEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.label(el))
                dd(el.'gmd:Url'.text())
            }
        }
    }

    def isoCodeListEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.label(el))
                dd(el['@codeListValue'].text())
            }
        }
    }

    def isoEntry = { el, childData ->
        if (!childData.isEmpty()) {
            return handlers.fileResult('html/entry.html', [label: f.label(el), childData: childData])
        }
        return null
    }
}