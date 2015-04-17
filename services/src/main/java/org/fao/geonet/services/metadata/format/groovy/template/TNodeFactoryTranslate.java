package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.SystemInfo;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates fmt-translate nodes.
 *
 * A translate node take the text content element and find a translation.  depending on the value of the
 * fmt-translate attribute the translation is resolved using the {@link org.fao.geonet.services.metadata.format.groovy.Functions}
 * translate methods.
 *
 * Examples:
 * <pre><code>
 * &lt;div fmt-translate="">testString&lt;/div>
 * &lt;div fmt-translate="default">testString&lt;/div>
 * &lt;div fmt-translate=">file">testString&lt;/div>
 * &lt;div fmt-translate="default>file">testString&lt;/div>
 * &lt;div fmt-translate="codelist>name">testString&lt;/div>
 * &lt;div fmt-translate="codelist>desc>context1">testString&lt;/div>
 * &lt;div fmt-translate="codelist>desc>context2">testString&lt;/div>
 * &lt;div fmt-translate="node>name>context">testString&lt;/div>
 * &lt;div fmt-translate="node>desc">testString&lt;/div>
 * </code></pre>
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryTranslate extends TNodeFactoryByAttName {
    public static final String TRANSLATE = "translate";

    public TNodeFactoryTranslate() {
        super(TRANSLATE, null);
    }

    public TNodeFactoryTranslate(SystemInfo info, TextContentParser textContentParser) {
        super(TRANSLATE, info);
        this.textContentParser = textContentParser;
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, TRANSLATE);
        final AttributesFiltered attributesFiltered = new AttributesFiltered(attributes, TRANSLATE);
        return new TNodeTranslate(SystemInfo.getInfo(this.testingInfo), textContentParser, qName, attributesFiltered, value);
    }

}
