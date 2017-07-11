package common

import org.fao.geonet.api.records.formatters.FormatType
import org.fao.geonet.api.records.formatters.groovy.Environment
import org.nibor.autolink.*;

public class Functions {
    org.fao.geonet.api.records.formatters.groovy.Handlers handlers;
    def org.fao.geonet.api.records.formatters.groovy.Functions f
    def Environment env

    def isHtmlOutput = {
        env.formatType == FormatType.html || env.formatType == FormatType.pdf ||
                env.formatType == FormatType.testpdf
    }

    def isPDFOutput = {
        env.formatType == FormatType.pdf || env.formatType == FormatType.testpdf
    }

    /**
     * Creates the default html for a label -> text pair.  This is the element for primitive/simple data.
     * This does not return a function it is returns the actual html and thus can be used within handlers/functions to
     * directly get the html
     */
    def textEl(label, text) {
        return handlers.fileResult("html/text-el.html", ["label": label, "text" : text])
    }

    /**
     * Same as textEl but doesn't escape the XML
     */
    def wikiTextEl(label, text) {
        return handlers.fileResult("html/wikitext-el.html", ["label": label, "text" : text])
    }

    /**
     * Creates the default html for a label -> text pair.  This is the element for primitive/simple data.
     * This does not return a function it is returns the actual html and thus can be used within handlers/functions to
     * directly get the html
     */
    def urlEl(label, href, text) {
        return handlers.fileResult("html/url-el.html", ["label": label, "href" : href, "text" :
                text.length() > 50 ? (text.substring(0, 50) + "...") : text])
    }

    def textColEl(content, cols) {
        return '<div class="col-md-' + cols + '">' + content + '</div>'
    }

    def urlToHtml(content) {
        LinkExtractor linkExtractor = LinkExtractor.builder()
                .linkTypes(EnumSet.of(LinkType.URL)) // limit to URLs
                .build();

        Iterable<LinkSpan> links = linkExtractor.extractLinks(content);
        String result = Autolink.renderLinks(content, links, new LinkRenderer()  {
            void render(LinkSpan link, CharSequence text, StringBuilder sb) {
                sb.append("<a target=\"_blank\" href=\"");
                sb.append(text, link.getBeginIndex(), link.getEndIndex());
                sb.append("\">");
                sb.append(text, link.getBeginIndex(), link.getEndIndex());
                sb.append("</a>");
            }
        });
        return result
    }

}
