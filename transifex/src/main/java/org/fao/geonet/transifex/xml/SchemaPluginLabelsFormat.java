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
public class SchemaPluginLabelsFormat implements TranslationFormat {
    XmlFormat format = new XmlFormat();
    private TranslationFileConfig stdConfig;

    @Override
    public TranslationFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties) {
        this.stdConfig = stdConfig;
        format.configure(stdConfig, properties);
        TranslationResolver labelResolver = new TranslationResolver("Label", "element", "label");
        TranslationResolver descriptionResolver = new TranslationResolver("Description", "element", "description");
        TranslationResolver helpResolver = new TranslationResolver("Help", "element", "help");
        TranslationResolver conditionResolver = new TranslationResolver("Condition", "element", "_condition");
        TranslationResolver helperResolver = new TranslationResolver("Helper", "element/helper/option", ".");
        helperResolver.includeTextInKey = false;

        format.getResolvers().add(labelResolver);
        format.getResolvers().add(descriptionResolver);
        format.getResolvers().add(helpResolver);
        format.getResolvers().add(conditionResolver);
        format.getResolvers().add(helperResolver);
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
