package org.fao.geonet.services.rdf;

import jeeves.server.context.ServiceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.*;
import java.nio.file.Path;
import java.util.List;


/**
 * Class to create the rdf output for the public metadata in the catalog.
 *
 * Process the results in pages to avoid memory issues and writes the ouput to a file.
 *
 * @author Jose García
 */
public class RdfOutputManager {
    private final static int PAGE_SIZE = 5;

    private Element thesaurusEl;

    public RdfOutputManager(Element thesaurusEl) {
        this.thesaurusEl = thesaurusEl;
    }

    /**
     * Creates an rdf file with all the public metadata from the catalogue that fits the search criteria.
     *
     * @param context
     * @return Name of the temporal file
     * @throws Exception
     */
    public File createRdfFile(ServiceContext context, RdfSearcher searcher) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        try {
            List results = searcher.search(context);

            Element records = createXsltModel(context);

            // Write results intermediate files:
            //  - recordsFile: File where each metadata is written in DCAT format.
            //  - catalogFile: File where the catalog section (dcat:Catalog) with all dcat:dataset elements is written.
            //                 Required as the results are paginated to avoid memory issues and
            //                 the catalog section goes at the top of the final file, can't be managed all in 1 file

            BufferedWriter outputRecordsFile = null, outputCatalogFile = null, outputRdfFile = null;
            File recordsFile = null, catalogFile = null, rdfFile = null;

            try {
                recordsFile = File.createTempFile("records-", ".rdf");
                outputRecordsFile = new BufferedWriter(new FileWriter(recordsFile));

                catalogFile = File.createTempFile("catalog-", ".rdf");
                outputCatalogFile = new BufferedWriter(new FileWriter(catalogFile));

                Path xslPath = context.getAppPath().resolve(Geonet.Path.XSLT_FOLDER).
                        resolve("services").resolve("dcat").resolve("rdf.xsl");

                int size = results.size();
                int page = 1;

                Log.info(Geonet.GEONETWORK, "DCAT - Processing " + size + " results");

                if (size == 0) {
                    Element recordsRdf = Xml.transform(records, xslPath);
                    writeCatalogResults(outputCatalogFile, recordsRdf, page);
                    writeFileResults(outputRecordsFile, recordsRdf, page);

                } else {
                    for (int i = 0; i < size; i++) {
                        Element mdInfo = (Element) results.get(i);

                        Element info = mdInfo.getChild("info", Edit.NAMESPACE);
                        String id = info.getChildText("id");

                        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
                        Element md = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
                        records.addContent(md.detach());

                        // write results to file when processing the last record of the page or last record of the results
                        if (hasToWriteResults(i, size)) {
                            Log.info(Geonet.GEONETWORK, "DCAT - Processing results from page " + page);

                            // Process the resultset
                            Element recordsRdf = Xml.transform(records, xslPath);

                            writeCatalogResults(outputCatalogFile, recordsRdf, page);

                            // Write results
                            writeFileResults(outputRecordsFile, recordsRdf, page);

                            records = createXsltModel(context);

                            page++;
                        }
                    }

                }

            } finally {
                IOUtils.closeQuietly(outputRecordsFile);
                IOUtils.closeQuietly(outputCatalogFile);
            }


            // Create the final rdf file
            Log.info(Geonet.GEONETWORK, "DCAT - Creating RDF file with results");

            BufferedReader reader1 = null, reader2 = null;
            try {
                rdfFile = File.createTempFile("rdf-", ".rdf");
                outputRdfFile = new BufferedWriter(new FileWriter(rdfFile));

                // File header
                Log.info(Geonet.GEONETWORK, "DCAT - ... Writing file header and dcat:Catalog section");
                writeFileHeader(outputRdfFile);

                // Append catalog records
                reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(catalogFile)));
                IOUtils.copy(reader1, outputRdfFile);

                // Close dcat:Catalog
                outputRdfFile.write("</dcat:Catalog>");
                outputRdfFile.write("\n");

                // Append records file
                Log.info(Geonet.GEONETWORK, "DCAT - ... Writing catalog records");
                reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(recordsFile)));
                IOUtils.copy(reader2, outputRdfFile);

                // File footer
                Log.info(Geonet.GEONETWORK, "DCAT - ... Writing file footer");
                writeFileFooter(outputRdfFile);
            } finally {
                IOUtils.closeQuietly(outputRdfFile);
                IOUtils.closeQuietly(reader1);
                IOUtils.closeQuietly(reader2);
            }

            // Delete temporal files
            FileUtils.deleteQuietly(recordsFile);
            FileUtils.deleteQuietly(catalogFile);

            return rdfFile;

        } finally {
            searcher.close();
        }
    }

    /**
     * Write results at the end of page (100 records per page) or at the end of results.
     *
     * @param pos
     * @param total
     * @return
     */
    private boolean hasToWriteResults(int pos, int total) {
        return (((pos+1) % PAGE_SIZE == 0) || (pos == total - 1));
    }


    /**
     * Writes the catalog results section to a file.
     *
     * @param output
     * @param rdf
     * @param page
     * @throws Exception
     */
    private void writeCatalogResults(BufferedWriter output, Element rdf, int page) throws Exception {
        // First time the catalogFile contains the complete dcat:Catalog section, the following times
        // gets appended the dcat:dataset elements
        if (page > 1) {
            Namespace nsDcat = Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#");
            List<Element> mdDcatDatasets = rdf.getChild("Catalog", nsDcat).getChildren("dataset", nsDcat);

            for (Element mdDcatDataset: mdDcatDatasets) {
                String dataset = Xml.getString(mdDcatDataset);

                // remove namespaces that will be defined in the header
                output.write(removeNamespaces(dataset));
                output.write("\n");
            }

        } else {
            Namespace nsDcat = Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#");
            Element mdDcatCatalog = rdf.getChild("Catalog", nsDcat);

            // remove the dcat:Catalog close element, will be added in the final file
            output.write(removeNamespaces(Xml.getString(mdDcatCatalog)).replace("</dcat:Catalog>", ""));
            output.write("\n");
        }
    }

    /**
     * Writes the metatada rdf to the results file.
     *
     * @param output
     * @param rdf
     * @throws Exception
     */
    private void writeFileResults(BufferedWriter output, Element rdf, int page) throws Exception {
        List<Element> mdDcatList = rdf.getChildren();

        boolean recordsSectionStarted = false;
        for (Element mdDcat: mdDcatList) {
            String elementName = mdDcat.getName();
            // Ignore the catalog section
            if (elementName.equalsIgnoreCase("Catalog")) continue;

            if (elementName.equalsIgnoreCase("CatalogRecord")) recordsSectionStarted = true;

            if (page == 1) {
                // For first results page, write also the Organisation section that is between Catalog
                // and CatalogRecords sections (same in all pages)
                String result = Xml.getString(mdDcat);
                output.write(removeNamespaces(result));
                output.write("\n");
            } else if (recordsSectionStarted) {
                String result = Xml.getString(mdDcat);
                output.write(removeNamespaces(result));
                output.write("\n");
            }
        }
    }

    /**
     * Writes the results file header.
     *
     * @param output
     * @throws Exception
     */
    private void writeFileHeader(BufferedWriter output) throws Exception {
        output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        output.write("\n");
        output.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
                "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" " +
                "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " +
                "xmlns:void=\"http://www.w3.org/TR/void/\" " +
                "xmlns:dcat=\"http://www.w3.org/ns/dcat#\" " +
                "xmlns:dctype=\"http://purl.org/dc/dcmitype/\" " +
                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" " +
                "xmlns:locn=\"http://www.w3.org/ns/locn#\" xmlns:time=\"http://www.w3.org/2006/time#\" " +
                "xmlns:dct=\"http://purl.org/dc/terms/\">");
        output.write("\n");
    }

    /**
     * Writes the results file footer.
     *
     * @param output
     * @throws Exception
     */
    private void writeFileFooter(BufferedWriter output) throws Exception {
        output.write("</rdf:RDF>");
    }

    /**
     * Creates the model element to send to the rdf xslt.
     *
     * @param context
     * @return
     */
    private Element createXsltModel(ServiceContext context) {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        Element modelEl = new Element("root");

        Element serverEl = new Element("server");
        serverEl.addContent(new Element("port").setText(sm.getValue("system/server/port")));
        serverEl.addContent(new Element("host").setText(sm.getValue("system/server/host")));
        serverEl.addContent(new Element("protocol").setText(sm.getValue("system/server/protocol")));

        Element siteEl = new Element("site");
        siteEl.addContent(new Element("name").setText(sm.getValue("system/site/name")));
        siteEl.addContent(new Element("organization").setText(sm.getValue("system/site/organization")));

        Element guiEl = new Element("gui");
        Element systemConfigEl = new Element("systemConfig");
        Element systemEl = new Element("system");
        systemEl.addContent(serverEl);
        systemEl.addContent(siteEl);

        systemConfigEl.addContent(systemEl);

        guiEl.addContent(systemConfigEl);
        guiEl.addContent(new Element("language").setText(context.getLanguage()));
        guiEl.addContent(new Element("url").setText(context.getBaseUrl()));
        guiEl.addContent(new Element("thesaurus").setContent(thesaurusEl.detach()));

        modelEl.addContent(guiEl);

        return modelEl;
    }

    /**
     * Removes namespaces added to the rdf outputs for each metadata. They are already defined in the header.
     *
     * @param xml
     * @throws Exception
     */
    private String removeNamespaces(String xml) {
        return xml.replace("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"", "").
                replace("xmlns:dct=\"http://purl.org/dc/terms/\"", "").
                replace("xmlns:dcat=\"http://www.w3.org/ns/dcat#\"", "").
                replace("xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"", "").
                replace("xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"", "").
                replace("xmlns:void=\"http://www.w3.org/TR/void/\"", "").
                replace("xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"", "").
                replace("xmlns:locn=\"http://www.w3.org/ns/locn#\"", "");
    }
}