package iso19139

public class Matchers {
    def handlers;
    def f

    def isUrlEl = {!it.'gmd:URL'.text().isEmpty()}
    def isCodeListEl = {!it['@codeListValue'].text().isEmpty()}

    def isTextEl = {el ->
        !el.'gco:CharacterString'.text().isEmpty() ||
                !el.'gmd:PT_FreeText'.'gmd:textGroup'.'gmd:LocalisedCharacterString'.text().isEmpty()
    }

    def isContainerEl = {el ->
        !isTextEl(el) && !isUrlEl(el) && !isCodeListEl(el)
    }
}
