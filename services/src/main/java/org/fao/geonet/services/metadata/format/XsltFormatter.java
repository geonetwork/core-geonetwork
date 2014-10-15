package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.constants.Geonet.StagingProfile.DEVELOPMENT;

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
public class XsltFormatter {
    private Set<String> compiledXslt = Sets.newConcurrentHashSet();
    final Charset charset = Charset.forName(Constants.ENCODING);

    @Autowired
    SettingManager settingManager;
    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    @Qualifier("stagingProfile")
    String stagingProfile;

    public Element format(FormatterParams params) throws Exception {

        final String canonicalPath = params.viewFile.getCanonicalPath();
        if (DEVELOPMENT.equals(stagingProfile) || !this.compiledXslt.contains(canonicalPath)) {
            compileFunctionsFile(params.viewFile, canonicalPath);
        }

        String lang = params.config.getLang(params.context.getLanguage());
        List<SchemaLocalization> localization = params.format.getLabels(params.context, lang);

        Element root = new Element("root");

        String url = settingManager.getSiteURL(params.context);

        root.addContent(new Element("lang").setText(params.context.getLanguage()));
        root.addContent(new Element("url").setText(url));
        String locUrl = url + "/" + params.context.getNodeId() + "/" + params.context.getLanguage() + "/";
        root.addContent(new Element("locUrl").setText(locUrl));
        String xslid = Util.getParam(params.params, "xsl", null);
        String resourceUrl = locUrl + "/metadata.formatter.resource?" + Params.SCHEMA + "=" + params.schema + "&" +
                             Params.ID + "=" + xslid + "&" + Params.FNAME + "=";
        root.addContent(new Element("resourceUrl").setText(resourceUrl));
        root.addContent(params.metadata);
        root.addContent(params.format.getResources(params.context, params.formatDir, lang));
        if (params.config.loadStrings()) {
            root.addContent(params.format.getStrings(params.context.getAppPath(), lang));
        }

        Element schemas = new Element("schemas");
        root.addContent(schemas);

        List<String> schemasToLoadList = params.config.listOfSchemasToLoad();

        String schemasToLoad = params.config.schemasToLoad();
        if (!"none".equalsIgnoreCase(schemasToLoad)) {
            for (SchemaLocalization schemaLocalization : localization) {
                String currentSchema = schemaLocalization.schema.trim();
                if ("all".equalsIgnoreCase(schemasToLoad) || schemasToLoadList.contains(currentSchema.toLowerCase())) {
                    Element schemaEl = new Element(currentSchema);
                    schemas.addContent(schemaEl);

                    schemaEl.addContent((Element) schemaLocalization.labels.clone());
                    schemaEl.addContent((Element) schemaLocalization.codelists.clone());
                    schemaEl.addContent((Element) schemaLocalization.strings.clone());
                }
            }
        }
        if (Util.getParam(params.params, "debug", false)) {
            return root;
        }
        Element transformed = Xml.transform(root, params.viewFile.getAbsolutePath());

        Element response = new Element("metadata");
        response.addContent(transformed);
        return response;
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
