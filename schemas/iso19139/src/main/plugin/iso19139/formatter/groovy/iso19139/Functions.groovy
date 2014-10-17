package iso19139

public class Functions {
    def handlers;
    def f

    def isoText = { el ->
        def uiCode = '#'+f.lang2.toUpperCase()
        def locStrings = el.'**'.findAll{ it.name == 'gmd:LocalisedCharacterString'}
        def ptEl = locStrings.find{it.'@locale' == uiCode}
        if (ptEl != null) return ptEl.text()
        if (el.'gco:CharacterString') return el.'gco:CharacterString'.text()
        if (!locStrings.isEmpty) return locStrings[0].text()
        ""
    }

}