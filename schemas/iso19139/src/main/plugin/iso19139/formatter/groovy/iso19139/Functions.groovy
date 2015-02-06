package iso19139

import com.google.common.xml.XmlEscapers

public class Functions {
    def handlers;
    def f
    def env
    def commonHandlers

    def isoUrlText = { el ->
        el.'gmd:URL'.text()
    }
    def isoText = { el ->
        def uiCode = '#'+env.lang2.toUpperCase()
        def locStrings = el.'**'.findAll{ it.name() == 'gmd:LocalisedCharacterString' && !it.text().isEmpty()}
        def ptEl = locStrings.find{it.'@locale' == uiCode}
        if (ptEl != null) return ptEl.text()
        def charString = el.'**'.findAll {it.name() == 'gco:CharacterString' && !it.text().isEmpty()}
        if (!charString.isEmpty()) return charString[0].text()
        if (!locStrings.isEmpty()) return locStrings[0].text()
        ""
    }
    /**
     * A shortcut for: commonHandlers.func.textEl(node), text))
     * @return
     */
    def isoTextEl(node, text) {
        return commonHandlers.func.textEl(f.nodeLabel(node), XmlEscapers.xmlContentEscaper().escape(text))
    }
}