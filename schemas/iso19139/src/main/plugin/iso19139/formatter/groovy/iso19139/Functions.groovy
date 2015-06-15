package iso19139

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
        return commonHandlers.func.textEl(f.nodeLabel(node), text)
    }
    /**
     * A shortcut for: commonHandlers.func.textEl(node), text))
     * @return
     */
    def isoUrlEl(node, href, text) {
        return commonHandlers.func.urlEl(f.nodeLabel(node), href, text)
    }
}