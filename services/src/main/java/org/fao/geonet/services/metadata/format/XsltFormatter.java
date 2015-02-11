package org.fao.geonet.services.metadata.format;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.fao.geonet.services.metadata.format.SchemaLocalizations.loadSchemaLocalizations;

/**
 * Strategy for formatting using an xslt based formatter.
 *
 * <p>
 *     Note: to include files from the formatter dir you can use
 *     <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
 *     and it will be replaced with the path to the formatter dir
 *     using the URI resolver.
 * </p>
 *
 * <p>
 *     Note: For a formatter to retrieve a request parameter
 *     an xsl:param should be defined with the name of the URL parameter
 *     eg. <xsl:param name="view"/>
 * </p>
 *
 * @author Jesse on 10/15/2014.
 * @author Francois on 06/01/2015: Add request parameters transfert to XSLT
 *  and metadata info.
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
        // TODO: It could be easier to put the metadata
        // record in a metadata tag so it will be easier to pick
        // it up with xpath whatever the standard is.
        root.addContent(fparams.metadata);

        // Add metadata information (ie. harvested, categories, schema, dates, ...)
        Element info = fparams.metadataInfo.asXml();
        // metadataInfo contains the XML in data which is not needed
        info.removeChild("data");
        root.addContent(new Element("info")
                .addContent(info));

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

        // Create a map of request parameters to be passed to the XSL transformation
        // For a formatter to retrieve a request parameter
        // an xsl:param should be defined
        // eg. <xsl:param name="view"/>
        Map<String, Object> requestParameters = new HashMap<String, Object>();
        Iterator<String> iterator = fparams.servletRequest.getParameterMap().keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            requestParameters.put(key, fparams.servletRequest.getParameterMap().get(key));
        }
        Element transformed = Xml.transform(root, fparams.viewFile, requestParameters);

        Element response = new Element("metadata");
        response.addContent(transformed);
        return Xml.getString(response);
    }

}
