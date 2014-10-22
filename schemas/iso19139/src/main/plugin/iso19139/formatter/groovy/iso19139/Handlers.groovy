package iso19139

public class Handlers {
    def handlers;
    def f
    def env
    Matchers matchers
    Functions isofunc
    common.Handlers commonHandlers

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        isofunc = new Functions(handlers: handlers, f:f, env:env)
        matchers =  new Matchers(handlers: handlers, f:f, env:env)
        commonHandlers = new common.Handlers(handlers, f, env)
    }

    def addDefaultHandlers() {
        handlers.add matchers.isTextEl, isoTextEl
        handlers.add matchers.isUrlEl, isoUrlEl
        handlers.add matchers.isCodeListEl, isoCodeListEl
        handlers.add matchers.hasCodeListChild, applyToChild(isoCodeListEl, '*')
        handlers.add matchers.isRespParty, respPartyEl
        handlers.add matchers.isContactInfo, contactInfoEl
        handlers.add matchers.isAddress, addressEl

        handlers.add select: matchers.isContainerEl, processChildren: true, priority: -1, isoEntryEl
        commonHandlers.addDefaultStartAndEndHandlers()

        handlers.sort 'gmd:MD_Metadata', {el1, el2 ->
            def v1 = matchers.isContainerEl(el1.el) ? 1 : -1;
            def v2 = matchers.isContainerEl(el2.el) ? 1 : -1;
            return v1 - v2
        }
    }

    def applyToChild(handlerFunc, name) {
        {el ->
            def children = el[name]
            if (children.size() == 1) {
                return handlerFunc(children[0])
            } else {
                throw new IllegalStateException("There is supposed to be only a single child when this method is called")
            }
        }
    }
    def nonEmpty(handlerFunc) {
        {el ->
            if (!el.text().isEmpty()) {
                return handlerFunc(el)
            }
        }
    }
    def isoTextEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.nodeLabel(el))
                dd(isofunc.isoText(el))
            }
        }
    }

    def isoUrlEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.nodeLabel(el))
                dd(el.'gmd:Url'.text())
            }
        }
    }

    def isoCodeListEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.nodeLabel(el))
                dd(el['@codeListValue'].text())
            }
        }
    }

    def isoEntryEl = { el, childData ->
        if (!childData.isEmpty()) {
            return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: childData])
        }
        return null
    }

    /**
     * El must be a parent of gmd:CI_ResponsibleParty
     */
    def respPartyEl = {el ->
        def party = el.'gmd:CI_ResponsibleParty'

        def childrenToProcess = []
        childrenToProcess.addAll(party.'gmd:individualName')
        childrenToProcess.addAll(party.'gmd:organisationName')
        childrenToProcess.addAll(party.'gmd:positionName')
        childrenToProcess.addAll(party.'gmd:role')
        childrenToProcess.addAll(party.'gmd:contactInfo'.'gmd:CI_Contact')
        def contactData = handlers.processElement( childrenToProcess )

        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData])
    }

    /**
     * el must be a parent of gmd:CI_Contact
     */
    def contactInfoEl = {el ->
        def contactData = []
        def text = nonEmpty(isoTextEl)
        def phone = el.'gmd:phone'.'gmd:CI_Telephone'

        phone.'gmd:voice'.each {contactData.add(text(it))}
        phone.'gmd:facsimile'.each {contactData.add(text(it))}

        contactData = contactData.findAll{it != null}
        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData.join("\n")])
    }

    /**
     * el must be a parent of gmd:CI_Contact
     */
    def addressEl = {el ->
        def addresses = el.'gmd:address'.'gmd:CI_Address'
        def contactData = []
        def text = nonEmpty(isoTextEl)

        addresses.'gmd:deliveryPoint'.each {contactData.add(text(it))}

        contactData = contactData.findAll{it != null}
        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData.join("\n")])
    }

}