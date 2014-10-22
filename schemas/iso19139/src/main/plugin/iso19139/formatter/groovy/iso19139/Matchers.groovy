package iso19139

public class Matchers {
    def handlers;
    def f
    def env

    def isUrlEl = {!it.'gmd:URL'.text().isEmpty()}
    def isCodeListEl = {!it['@codeListValue'].text().isEmpty()}
    def hasCodeListChild = {it.children().size() == 1 && it.children().any{!it['@codeListValue'].text().isEmpty()}}

    def isTextEl = {el ->
        !el.'gco:CharacterString'.text().isEmpty() ||
                !el.'gmd:PT_FreeText'.'gmd:textGroup'.'gmd:LocalisedCharacterString'.text().isEmpty()
    }

    def isContainerEl = {el ->
        !isTextEl(el) && !isUrlEl(el) && !isCodeListEl(el) && !el.children().isEmpty() && !hasCodeListChild(el)
    }
    def isRespParty = { el ->
        !el.'gmd:CI_ResponsibleParty'.isEmpty() || el.'gmd:CI_ResponsibleParty'['@gco:isoType'].text() == 'gmd:CI_ResponsibleParty'
    }
}
