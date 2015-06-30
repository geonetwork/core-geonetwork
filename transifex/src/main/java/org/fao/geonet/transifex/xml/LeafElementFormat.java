package org.fao.geonet.transifex.xml;

import org.fao.geonet.transifex.Layout;
import org.fao.geonet.transifex.TransifexReadyFile;
import org.fao.geonet.transifex.TranslationFileConfig;
import org.fao.geonet.transifex.TranslationFormat;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Strategy for translating the schema plugins labels files
 *
 * @author Jesse on 6/18/2015.
 */
public class LeafElementFormat implements TranslationFormat {
    XmlFormat format = new XmlFormat();
    private TranslationFileConfig stdConfig;

    @Override
    public TranslationFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties) {
        this.stdConfig = stdConfig;
        format.configure(stdConfig, properties);
        TranslationResolver labelResolver = new TranslationResolver("String", "*[normalize-space(text()) != ''] | *//*[normalize-space(text()) != '']", ".");
        labelResolver.includeTextInKey = false;
        format.getResolvers().add(labelResolver);
        return this;
    }

    @Override
    public List<TransifexReadyFile> toTransifex(String translationFile) throws Exception {
        return format.toTransifex(Paths.get(translationFile).resolve("eng/" + stdConfig.fileName).toString());
    }

    @Override
    public String toGeonetwork(List<TransifexReadyFile> fromTransifex) throws IOException, JDOMException {
        return format.toGeonetwork(fromTransifex);
    }

    @Override
    public Layout getDefaultLayout() {
        return Layout.DIR;
    }
}
