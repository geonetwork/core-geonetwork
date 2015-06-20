package org.fao.geonet.transifex.xml;

import org.fao.geonet.transifex.Layout;
import org.fao.geonet.transifex.TransifexReadyFile;
import org.fao.geonet.transifex.TranslationFileConfig;
import org.fao.geonet.transifex.TranslationFormat;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Strategy for translating the schema plugins labels files
 *
 * @author Jesse on 6/18/2015.
 */
public class SchemaPluginCodelistFormat implements TranslationFormat {
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

        format.getResolvers().add(labelResolver);
        format.getResolvers().add(descriptionResolver);
        format.getResolvers().add(helpResolver);
        format.getResolvers().add(conditionResolver);
        return this;
    }

    @Override
    public List<TransifexReadyFile> toTransifex(String translationFile) throws Exception {
        return format.toTransifex(Paths.get(translationFile).resolve("eng/" + stdConfig.fileName).toString());
    }

    @Override
    public String toGeonetwork(List<TransifexReadyFile> fromTransifex) {
        return format.toGeonetwork(fromTransifex);
    }

    @Override
    public Layout getDefaultLayout() {
        return Layout.DIR;
    }
}
