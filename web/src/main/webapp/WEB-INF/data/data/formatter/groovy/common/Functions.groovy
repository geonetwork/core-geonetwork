package common

import org.fao.geonet.services.metadata.format.FormatType

public class Functions {
    org.fao.geonet.services.metadata.format.groovy.Handlers handlers;
    def f
    def env

    def isHtmlOutput = {
        env.formatType == FormatType.html || env.formatType == FormatType.pdf ||
                env.formatType == FormatType.testpdf
    }

    /**
     * Creates the default html for a label -> text pair.  This is the element for primitive/simple data.
     * This does not return a function it is returns the actual html and thus can be used within handlers/functions to
     * directly get the html
     */
    def textEl(label, text) {
        return handlers.fileResult("html/text-el.html", ["label": label, "text" : text])
    }

    def textColEl(content, cols) {
        return '<div class="col-md-' + cols + '">' + content + '</div>'
    }

}