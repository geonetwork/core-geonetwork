package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.base.Optional;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.TransformationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates nodes that import other templates into the current template.
 *
 * @author Jesse on 12/3/2014.
 */
@Component
public class TNodeFactoryInclude extends TNodeFactoryByAttName {
    private static final String INCLUDE = "include";
    private static final String REPLACE = "include-replace";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SystemInfo info;

    protected TNodeFactoryInclude() {
        super(INCLUDE);
    }

    public TNodeFactoryInclude(SystemInfo info) {
        super(INCLUDE);
        this.info = info;
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        Attributes filteredAtts = new FilteredAttributes(attributes, INCLUDE, REPLACE);
        String templatePath = getValue(attributes, INCLUDE);
        String replaceAtt = getValue(attributes, REPLACE);
        boolean replace = false;
        if (replaceAtt != null) {
            replace = replaceAtt.isEmpty() || replaceAtt.equalsIgnoreCase("y") || replaceAtt.equalsIgnoreCase("true")
                      || replaceAtt.equalsIgnoreCase("yes");
        }
        return new TNodeInclude(info, qName, filteredAtts, templatePath, replace);
    }

    private class TNodeInclude extends TNode {

        private final String templatePath;
        private final boolean replace;

        public TNodeInclude(SystemInfo info, String qName, Attributes attributes, String templatePath, boolean replace)
                throws IOException {
            super(info, qName, attributes);
            this.replace = replace;
            this.templatePath = templatePath;
            if (!replace) {
                this.addChild(new TNodeInclude(info, qName, attributes, templatePath, true));
            }
        }

        @Override
        protected Optional<String> canRender(TRenderContext context) {
            return Optional.absent();
        }

        @Override
        public void render(TRenderContext context) throws IOException {
            if (replace) {
                final Handlers handlers = TransformationContext.getContext().getHandlers();
                final FileResult fileResult = handlers.fileResult(this.templatePath, context.getModel(true));
                context.append(fileResult.toString());
            } else {
                super.render(context);
            }
        }


    }
}
