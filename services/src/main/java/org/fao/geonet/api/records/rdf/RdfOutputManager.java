/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.rdf;

import jeeves.server.context.ServiceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.nio.charset.Charset;
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
    private int PAGE_SIZE = 10;

    private Element thesaurusEl;

    public RdfOutputManager(Element thesaurusEl) {
        this.thesaurusEl = thesaurusEl;
    }

    public RdfOutputManager(Element thesaurusEl, int pageSize) {
        this.thesaurusEl = thesaurusEl;
        PAGE_SIZE = pageSize;
    	
    }
    
    /**
     * Creates an rdf file with all the public metadata from the catalogue that fits the search
     * criteria.
     *
     * @return Name of the temporal file
     */
    public File createRdfFile(ServiceContext context, RdfSearcher searcher) throws Exception {
    	try {
        List<Element> results = searcher.search(context);
    	return createRdfFile(context,results,1,"");
    	}
    	 finally {
             searcher.close();
         }
    }    

    /**
     * Creates an rdf file with all the public metadata from the catalogue that fits the search
     * criteria.
     *
     * @param context
     * @param results
     * @param page
     * @param pagingInformation paging information
     * @return
     * @throws Exception
     */
    public File createRdfFile(ServiceContext context, List<Element> results, int page, String pagingInformation) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dm = applicationContext.getBean(DataManager.class);
        //SettingManager sm = applicationContext.getBean(SettingManager.class);

        try {
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
                outputRecordsFile = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(recordsFile), Charset.forName("UTF-8")));

                catalogFile = File.createTempFile("catalog-", ".rdf");
                outputCatalogFile = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(catalogFile), Charset.forName("UTF-8")));

                Path xslPath = context.getAppPath().resolve(Geonet.Path.XSLT_FOLDER).
                    resolve("services").resolve("dcat").resolve("rdf.xsl");

                int size = results.size();

                Log.info(Geonet.GEONETWORK, "DCAT - Processing " + size + " results");

                if (size == 0) {
                    Element recordsRdf = Xml.transform(records, xslPath);
                    writeCatalogResults(outputCatalogFile, recordsRdf, page, pagingInformation);
                    writeFileResults(outputRecordsFile, recordsRdf, page);

                } else {
                    for (int i = 0; i < size; i++) {
                        Element mdInfo = results.get(i);

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

                            writeCatalogResults(outputCatalogFile, recordsRdf, page, pagingInformation);

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
                outputRdfFile = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(rdfFile), Charset.forName("UTF-8")));


                // File header
                Log.info(Geonet.GEONETWORK, "DCAT - ... Writing file header and dcat:Catalog section");
                writeFileHeader(outputRdfFile);

                // Append catalog records
                reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(catalogFile), Charset.forName("UTF-8")));
                IOUtils.copy(reader1, outputRdfFile);

                // Append records file
                Log.info(Geonet.GEONETWORK, "DCAT - ... Writing catalog records");
                reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(recordsFile), Charset.forName("UTF-8")));
                IOUtils.copy(reader2, outputRdfFile);
                
                // Close dcat:Catalog
                outputRdfFile.write("</dcat:Catalog>");
                outputRdfFile.write("\n");

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
        }
    }

    /**
     * Write results at the end of page (100 records per page) or at the end of results.
     */
    private boolean hasToWriteResults(int pos, int total) {
        return (((pos + 1) % PAGE_SIZE == 0) || (pos == total - 1));
    }


    /**
     * Writes the catalog results section to a file.
     */
    private void writeCatalogResults(BufferedWriter output, Element rdf, int page, String pagingInformation) throws Exception {
        // First time the catalogFile contains the complete dcat:Catalog section, the following times
        // gets appended the dcat:dataset elements
        if (page > 1) {
            Namespace nsDcat = Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#");
            List<Element> mdDcatDatasets = rdf.getChild("Catalog", nsDcat).getChildren("dataset", nsDcat);

            for (Element mdDcatDataset : mdDcatDatasets) {
                String dataset = Xml.getString(mdDcatDataset);

                // remove namespaces that will be defined in the header
                output.write(removeNamespaces(dataset));
                output.write("\n");
            }

        } else {
        	output.write(pagingInformation);
            Namespace nsDcat = Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#");
            output.write("\n");
            Element mdDcatCatalog = rdf.getChild("Catalog", nsDcat);

            // remove the dcat:Catalog close element, will be added in the final file
            output.write(removeNamespaces(Xml.getString(mdDcatCatalog)).replace("</dcat:Catalog>", ""));
            output.write("\n");
        }
    }

    /**
     * Writes the metatada rdf to the results file.
     */
    private void writeFileResults(BufferedWriter output, Element rdf, int page) throws Exception {
        List<Element> mdDcatList = rdf.getChildren();

        boolean recordsSectionStarted = false;
        for (Element mdDcat : mdDcatList) {
            String elementName = mdDcat.getName();
            // Ignore the catalog section
            if (elementName.equalsIgnoreCase("Catalog")) continue;

            if (elementName.equalsIgnoreCase("CatalogRecord")) recordsSectionStarted = true;

            if (page == 1) {
                // For first results page, write also the Organisation section that is between Catalog
                // and CatalogRecords sections (same in all pages)
            	output.write("<dcat:dataset>");
                output.write("\n");
                String result = Xml.getString(mdDcat);
                output.write(removeNamespaces(result));
                output.write("</dcat:dataset>");
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
     */
    private void writeFileFooter(BufferedWriter output) throws Exception {
        output.write("</rdf:RDF>");
    }

    /**
     * Creates the model element to send to the rdf xslt.
     */
    private Element createXsltModel(ServiceContext context) {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        Element modelEl = new Element("root");

        Element serverEl = new Element("server");
        serverEl.addContent(new Element("port").setText(sm.getValue(Settings.SYSTEM_SERVER_PORT)));
        serverEl.addContent(new Element("host").setText(sm.getValue(Settings.SYSTEM_SERVER_HOST)));
        serverEl.addContent(new Element("protocol").setText(sm.getValue(Settings.SYSTEM_SERVER_PROTOCOL)));

        Element siteEl = new Element("site");
        siteEl.addContent(new Element("siteId").setText(sm.getValue(Settings.SYSTEM_SITE_SITE_ID_PATH)));
        siteEl.addContent(new Element("name").setText(sm.getValue(Settings.SYSTEM_SITE_NAME_PATH)));
        siteEl.addContent(new Element("organization").setText(sm.getValue(Settings.SYSTEM_SITE_ORGANIZATION)));

        Element metadataEl = new Element("metadata");
        metadataEl.addContent(new Element("resourceIdentifierPrefix").setText(sm.getValue(Settings.SYSTEM_RESOURCE_PREFIX)));

        Element guiEl = new Element("gui");

        Element systemConfigEl = new Element("systemConfig");
        systemConfigEl.addContent(metadataEl);

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
     * Removes namespaces added to the rdf outputs for each metadata. They are already defined in
     * the header.
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
