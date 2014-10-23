package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

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
    private Set<String> compiledXslt = Sets.newConcurrentHashSet();
    final Charset charset = Charset.forName(Constants.ENCODING);

    @Autowired
    GeonetworkDataDirectory dataDirectory;

    public String format(FormatterParams fparams) throws Exception {

        final String viewFilePath = fparams.viewFile.getPath();
        if (fparams.isDevMode() || !this.compiledXslt.contains(viewFilePath)) {
            compileFunctionsFile(fparams.viewFile, viewFilePath);
        }

        String lang = fparams.config.getLang(fparams.context.getLanguage());
        Iterable<SchemaLocalization> localization = fparams.format.getSchemaLocalizations(fparams.context).values();

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
        Element transformed = Xml.transform(root, fparams.viewFile.getAbsolutePath());

        Element response = new Element("metadata");
        response.addContent(transformed);
        return Xml.getString(response);
    }


    private synchronized void compileFunctionsFile(File viewXslFile,
                                                   String canonicalPath) throws IOException, JDOMException {
        this.compiledXslt.add(canonicalPath);

        final String baseName = Files.getNameWithoutExtension(viewXslFile.getName());
        final File lastUpdateFile = new File(viewXslFile.getParentFile(), baseName + ".lastUpdate");
        if (lastUpdateFile.lastModified() < viewXslFile.lastModified()) {
            final String xml = Files.toString(viewXslFile, charset);

            String updated = xml.replace("@@formatterDir@@", this.dataDirectory.getFormatterDir().toURI().toString());

            Files.write(updated, viewXslFile, charset);
            Files.touch(lastUpdateFile);
        }
    }
}
