package org.fao.geonet.services.metadata.format;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static org.fao.geonet.services.metadata.format.SchemaLocalizations.loadSchemaLocalizations;

/**
 * Strategy for formatting using an xslt based formatter.
 *
 * <p>
 *     Note: to include files from the formatter dir you can use @@formatterDir@@ and it will be replaced with the
 *     path to the formatter dir.
 * </p>
 *
 * @author Jesse on 10/15/2014.
 */
@Component
public class XsltFormatter implements FormatterImpl {
    @Autowired
    GeonetworkDataDirectory dataDirectory;

    public String format(FormatterParams fparams) throws Exception {

        String lang = fparams.config.getLang(fparams.context.getLanguage());

        Element root = new Element("root");

        root.addContent(new Element("lang").setText(fparams.context.getLanguage()));
        root.addContent(new Element("url").setText(fparams.url));
        root.addContent(new Element("locUrl").setText(fparams.getLocUrl()));

        root.addContent(new Element("resourceUrl").setText(fparams.getResourceUrl()));
        root.addContent(fparams.metadata);
        root.addContent(fparams.format.getPluginLocResources(fparams.context, fparams.formatDir, lang));
        if (fparams.config.loadStrings()) {
            root.addContent(fparams.format.getStrings(fparams.context.getAppPath(), lang));
        }

        Element schemas = new Element("schemas");
        root.addContent(schemas);

        List<String> schemasToLoadList = fparams.config.listOfSchemasToLoad();

        String schemasToLoad = fparams.config.schemasToLoad();
        if (!"none".equalsIgnoreCase(schemasToLoad)) {
            SchemaManager schemaManager = fparams.context.getBean(SchemaManager.class);
            Collection<SchemaLocalization> localization = loadSchemaLocalizations(fparams.context.getApplicationContext(),
                    schemaManager).values();

            for (SchemaLocalization schemaLocalization : localization) {
                String currentSchema = schemaLocalization.schema.trim();
                if ("all".equalsIgnoreCase(schemasToLoad) || schemasToLoadList.contains(currentSchema.toLowerCase())) {
                    Element schemaEl = new Element(currentSchema);
                    schemas.addContent(schemaEl);

                    schemaEl.addContent((Element) schemaLocalization.getLabels(fparams.context.getLanguage()).clone());
                    schemaEl.addContent((Element) schemaLocalization.getCodelists(fparams.context.getLanguage()).clone());
                    schemaEl.addContent((Element) schemaLocalization.getStrings(fparams.context.getLanguage()).clone());
                }
            }
        }
        if (!"false".equalsIgnoreCase(fparams.param("debug", "false"))) {
            return Xml.getString(root);
        }
        Element transformed = Xml.transform(root, fparams.viewFile);

        Element response = new Element("metadata");
        response.addContent(transformed);
        return Xml.getString(response);
    }

}
