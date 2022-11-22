//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel;

import static com.google.common.io.Files.getNameWithoutExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.ThesaurusActivation;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.repository.local.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.Maps;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.springframework.beans.factory.annotation.Value;

public class ThesaurusManager implements ThesaurusFinder {

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;

    @Autowired
    private IMetadataUtils iMetadataUtils;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private IsoLanguagesMapper isoLanguagesMapper;

    @Autowired
    private ThesaurusActivationRepository thesaurusActivationRepository;

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    @Autowired
    private AllThesaurus allThesaurus;

    private ConcurrentHashMap<String, Thesaurus> thesauriMap = new ConcurrentHashMap<String, Thesaurus>();
    private LocalService service = null;
    private Path thesauriDirectory = null;
    private boolean initialized = false;

    @Value("${thesaurus.cache.maxsize:50000}")
    private int thesaurusCacheMaxSize = 50000;

    public int getThesaurusCacheMaxSize() {
        return thesaurusCacheMaxSize;
    }

    public void setThesaurusCacheMaxSize(int thesaurusCacheMaxSize) {
        this.thesaurusCacheMaxSize = thesaurusCacheMaxSize;
    }

    /**
     * Initialize ThesaurusManager.
     *
     * @param context ServiceContext used to check when servlet is up only
     */
    public synchronized void init(boolean isTest, ServiceContext context, String thesauriRepository)
        throws Exception {

        if (this.initialized) {
            return;
        }
        this.initialized = true;

        final String siteURL = this.settingManager.getSiteURL(context);
        allThesaurus.init(siteURL);

        // Get Sesame interface
        service = Sesame.getService();

        Path thesauriDir = IO.toPath(thesauriRepository);

        if (!Files.exists(thesauriDir)) {
            thesauriDir = geonetworkDataDirectory.resolveWebResource(thesauriRepository);
        }

        thesauriDir = thesauriDir.toAbsolutePath();
        thesauriDirectory = thesauriDir.toAbsolutePath();

        batchBuildTable(isTest, context, thesauriDir);
    }

    /**
     * Start task to build thesaurus table once the servlet is up.
     *
     * @param synchRun    if false, run the initialization asynchronally
     * @param context     ServiceContext used to check when servlet is up only
     * @param thesauriDir directory containing thesauri
     */
    private void batchBuildTable(boolean synchRun, ServiceContext context, Path thesauriDir) {
        ExecutorService executor = null;
        try {
            Runnable worker = new InitThesauriTableTask(context, thesauriDir);
            if (synchRun) {
                worker.run();
            } else {
                executor = Executors.newFixedThreadPool(1);
                executor.execute(worker);
            }
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    /**
     *
     * @param thesauriDirectory
     */
    private void initThesauriTable(Path thesauriDirectory, ServiceContext context) throws IOException {

        thesauriMap = new ConcurrentHashMap<>();
        Log.info(Geonet.THESAURUS_MAN, "Scanning " + thesauriDirectory);

        if (thesauriDirectory != null && Files.isDirectory(thesauriDirectory)) {
            String[] types = {Geonet.CodeList.EXTERNAL, Geonet.CodeList.LOCAL, Geonet.CodeList.REGISTER};
            for (String type : types) {
                // init of external repositories
                Path externalThesauriDirectory = thesauriDirectory.resolve(type).resolve(Geonet.CodeList.THESAURUS);
                if (Files.isDirectory(externalThesauriDirectory)) {
                    try (DirectoryStream<Path> paths = Files.newDirectoryStream(externalThesauriDirectory, IO.DIRECTORIES_FILTER)) {
                        for (Path aRdfDataDirectory : paths) {
                            loadRepositories(aRdfDataDirectory, type, context);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param thesauriDirectory
     */
    private void loadRepositories(Path thesauriDirectory, String root, ServiceContext context) throws IOException {

        final String siteURL = settingManager.getSiteURL(context);

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(thesauriDirectory, "*.rdf")) {
            for (Path aRdfDataFile : paths) {

                final String rdfFileName = aRdfDataFile.getFileName().toString();
                final String thesaurusDirName = thesauriDirectory.getFileName().toString();

                final Thesaurus gst;
                if (root.equals(Geonet.CodeList.REGISTER)) {
                    if (Log.isDebugEnabled(Geonet.THESAURUS_MAN)) {
                        Log.debug(Geonet.THESAURUS_MAN, "Creating thesaurus : " + aRdfDataFile);
                    }

                    Path outputRdf = thesauriDirectory.resolve(aRdfDataFile);
                    String uuid = getNameWithoutExtension(rdfFileName);
                    try (OutputStream outputRdfStream = Files.newOutputStream(outputRdf)) {
                        getRegisterMetadataAsRdf(uuid, outputRdfStream, context);
                    } catch (Exception e) {
                        Log.error(Geonet.THESAURUS_MAN, "Register thesaurus " + aRdfDataFile + " could not be read/converted from ISO19135 "
                            + "record in catalog - skipping", e);
                        continue;
                    }

                    gst = new Thesaurus(isoLanguagesMapper, rdfFileName, root, thesaurusDirName, outputRdf, siteURL, thesaurusCacheMaxSize);

                } else {
                    gst = new Thesaurus(isoLanguagesMapper, rdfFileName, root, thesaurusDirName, thesauriDirectory.resolve(aRdfDataFile), siteURL, thesaurusCacheMaxSize);
                }

                try {
                    addThesaurus(gst, false);
                } catch (Exception e) {
                    Log.error(Geonet.THESAURUS_MAN, "Error adding thesaurus " + aRdfDataFile + ": " + e.getMessage(), e);
                    // continue loading
                }
            }
        }
    }

    /**
     * Get ISO19135 Register Metadata from catalog and convert to rdf.
     *
     * @param uuid Uuid of register (ISO19135) metadata record describing thesaurus
     * @param os   OutputStream to write rdf to from XSLT conversion
     */
    private void getRegisterMetadataAsRdf(String uuid, OutputStream os, ServiceContext context) throws Exception {
        AbstractMetadata mdInfo = iMetadataUtils.findOneByUuid(uuid);
        Integer id = mdInfo.getId();
        Element md = metadataManager.getMetadata("" + id);
        Processor.detachXLink(md, context);
        final String siteURL = settingManager.getSiteURL(context);
        Element env = Lib.prepareTransformEnv(
            mdInfo.getUuid(),
            mdInfo.getDataInfo().getChangeDate().getDateAndTime(),
            "", siteURL, "");

        //--- transform the metadata with the created env and specified stylesheet
        Element root = new Element("root");
        root.addContent(md);
        root.addContent(env);

        Path styleSheet = metadataSchemaUtils.getSchemaDir("iso19135").resolve("convert").resolve(Geonet.File.EXTRACT_SKOS_FROM_ISO19135);
        Xml.transform(root, styleSheet, os);
    }

    /**
     * Build thesaurus file path according to thesaurus configuration (ie. codelist directory
     * location). If directory does not exist, it will create it.
     *
     * @return the thesaurus file path.
     */
    public Path buildThesaurusFilePath(String fname, String type, String dname) throws IOException {
        Path dirPath = thesauriDirectory.resolve(type).resolve(Geonet.CodeList.THESAURUS).resolve(dname);
        Files.createDirectories(dirPath);
        return dirPath.resolve(fname);
    }

    /**
     * @param writeConceptScheme Write the concept scheme and details
     *                           (title, description, ...) in the graph.
     *                           Used when creating local thesaurus.
     */
    public void addThesaurus(Thesaurus gst, boolean writeConceptScheme) throws Exception {

        String thesaurusName = gst.getKey();

        if (Log.isDebugEnabled(Geonet.THESAURUS_MAN)) {
            Log.debug(Geonet.THESAURUS_MAN, "Adding thesaurus : " + thesaurusName);
        }

        if (existsThesaurus(thesaurusName)) {
            throw new Exception("A thesaurus exists with code " + thesaurusName);
        }

        createThesaurusRepository(gst);
        thesauriMap.put(thesaurusName, gst);

        if (writeConceptScheme) {
            gst.writeConceptScheme(
                gst.getTitle(),
                gst.getDescription(),
                gst.getFname(),
                gst.getDname(),
                gst.getDefaultNamespace());
        }
    }

    public void addOrReloadThesaurus(Thesaurus gst) throws Exception {
        if (thesauriMap.replace(gst.getKey(), gst) != null) {
            service.removeRepository(gst.getKey());
        }

        createThesaurusRepository(gst);
        thesauriMap.put(gst.getKey(), gst);

        if (Log.isDebugEnabled(Geonet.THESAURUS_MAN)) {
            Log.debug(Geonet.THESAURUS_MAN, "Thesaurus " + gst.getKey() + " loaded.");
        }
    }

    /**
     *
     * @param name
     */
    public void remove(String name) {
        service.removeRepository(name);
        thesauriMap.remove(name);
    }

    /**
     *
     * @param gst
     */
    private void createThesaurusRepository(Thesaurus gst) throws Exception {
        LocalRepository thesaurusRepository;
        try {
            RepositoryConfig repConfig = new RepositoryConfig(gst.getKey());

            SailConfig syncSail = new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfSchemaRepository");
            SailConfig memSail = new org.openrdf.sesame.sailimpl.memory.RdfSchemaRepositoryConfig(
                gst.getFile().toAbsolutePath().toString(), RDFFormat.RDFXML);
            repConfig.addSail(syncSail);
            repConfig.addSail(memSail);
            repConfig.setWorldReadable(true);
            repConfig.setWorldWriteable(true);

            thesaurusRepository = service.createRepository(repConfig);

            gst.setRepository(thesaurusRepository);
        } catch (ConfigurationException e) {
            Log.error(Geonet.THESAURUS_MAN, "Create Thesaurus Repository error", e);
            throw e;
        }
    }

    public Path getThesauriDirectory() {
        return thesauriDirectory;
    }

    @Override
    public Map<String, Thesaurus> getThesauriMap() {
        if (this.settingManager.getValueAsBool(Settings.SYSTEM_ENABLE_ALL_THESAURUS)) {
            final HashMap<String, Thesaurus> all = Maps.newHashMap(this.thesauriMap);
            all.put(this.allThesaurus.getKey(), this.allThesaurus);
            return all;
        } else {
            return Collections.unmodifiableMap(thesauriMap);
        }
    }

    @Override
    @Nullable
    public Thesaurus getThesaurusByName(@Nonnull String thesaurusName) {
        return getThesauriMap().get(thesaurusName);
    }

    @Override
    public Thesaurus getThesaurusByConceptScheme(String uri) {
        for (Map.Entry<String, Thesaurus> entry : getThesauriMap().entrySet()) {
            try {
                Thesaurus thesaurus = entry.getValue();
                if (thesaurus.hasConceptScheme(uri)) {
                    return thesaurus;
                }
            } catch (Exception e) {
                Log.error(Geonet.THESAURUS_MAN, "Get Thesaurus By Concept Scheme error", e);
            }
        }

        return null;
    }

    public Thesaurus getThesaurusByTitle(String title) {
        for (Map.Entry<String, Thesaurus> entry : getThesauriMap().entrySet()) {
            try {
                Thesaurus thesaurus = entry.getValue();
                if (thesaurus.getTitle().equals(title)) {
                    return thesaurus;
                }
            } catch (Exception e) {
                Log.error(Geonet.THESAURUS_MAN, "Get thesaurus by title error", e);
            }
        }
        return null;
    }

    /**
     * @param name
     * @return
     */
    @Override
    public boolean existsThesaurus(String name) {
        return (getThesauriMap().get(name) != null);
    }

    /**
     * Create (or update an existing) rdf thesaurus from the specified ISO19135 register record.
     *
     * @param uuid Uuid of iso19135 register metadata record to update thesaurus
     * @param type Type of thesaurus (theme, etc)
     * @return id of thesaurus created/updated
     */
    public String createUpdateThesaurusFromRegister(String uuid, String type, ServiceContext context) throws Exception {
        String root = Geonet.CodeList.REGISTER;

        // check whether we have created a thesaurus for this register already
        String aRdfDataFile = uuid + ".rdf";
        Path thesaurusFile = buildThesaurusFilePath(aRdfDataFile, root, type);
        final String siteURL = settingManager.getSiteURL(context);
        Thesaurus gst = new Thesaurus(isoLanguagesMapper, aRdfDataFile, root, type, thesaurusFile, siteURL, thesaurusCacheMaxSize);

        try (OutputStream outputRdfStream = Files.newOutputStream(thesaurusFile)) {
            getRegisterMetadataAsRdf(uuid, outputRdfStream, context);
        } catch (Exception e) {
            Log.error(Geonet.THESAURUS_MAN, "Register thesaurus " + aRdfDataFile + " could not be read/converted from ISO19135 record in catalog - skipping", e);
        }

        String theKey = gst.getKey();
        gst.retrieveThesaurusTitle();

        addOrReloadThesaurus(gst);

        return theKey;
    }

    /**
     * @return {@link org.jdom.Element}
     */
    public Element buildResultfromThTable(ServiceContext context) throws SQLException, JDOMException, IOException {

        Element elRoot = new Element("thesauri");

        Collection<Thesaurus> e = getThesauriMap().values();
        for (Thesaurus currentTh : e) {
            Element elLoop = new Element("thesaurus");

            Element elKey = new Element("key");
            String key = currentTh.getKey();
            elKey.addContent(key);

            Element elDname = new Element("dname");
            String dname = currentTh.getDname();
            elDname.addContent(dname);

            Element description = new Element("description");
            description.addContent(currentTh.getDescription());

            Element elFname = new Element("filename");
            String fname = currentTh.getFname();
            elFname.addContent(fname);

            Element elTitle = new Element("title");
            String title = currentTh.getTitles(context.getApplicationContext()).get(context.getLanguage());
            if (title == null) {
                title = currentTh.getTitle();
            }
            elTitle.addContent(title);

            //add multilingual titles in to response
            //      "multilingualTitles":     [
            //            { "lang": "fr","title": "Data Usage Scope FR"},
            //            {"lang": "en","title": "Data Usage Scope EN"}
            //      ],
            Element elMultilingualTitles = new Element("multilingualTitles");
            for (Map.Entry<String, String> entry : currentTh.getMultilingualTitles().entrySet()) {
                Element elMultilingualTitle = new Element("multilingualTitle");
                Element elMultilingualTitl_lang = new Element("lang");
                elMultilingualTitl_lang.setText(entry.getKey());
                Element elMultilingualTitle_title = new Element("title");
                elMultilingualTitle_title.setText(entry.getValue());
                elMultilingualTitle.addContent(elMultilingualTitl_lang);
                elMultilingualTitle.addContent(elMultilingualTitle_title);

                elMultilingualTitles.addContent(elMultilingualTitle);
            }

            //add dublin core items to the response
            // "dublinCoreMultilingual": [
            //    { "lang": "fr","tag":"title","value": "Data Usage Scope FR"},
            //    {"lang": "en","tag":"title","value": "Data Usage Scope EN"}
            //]
            Element elDublinCoreMultilingual = new Element("dublinCoreMultilinguals");
            for (Map.Entry<String, Map<String,String>> entryLang : currentTh.getDublinCoreMultilingual().entrySet()) {
                String lang = entryLang.getKey();
                for (Map.Entry<String, String> entryItem : entryLang.getValue().entrySet()) {
                    Element elItem = new Element("dublinCoreMultilingual");
                    Element elLang = new Element("lang");
                    elLang.setText(lang);
                    Element elTag = new Element("tag");
                    elTag.setText(entryItem.getKey());
                    Element elValue = new Element("value");
                    elValue.setText(entryItem.getValue());

                    elItem.addContent(elLang);
                    elItem.addContent(elTag);
                    elItem.addContent(elValue);


                    elDublinCoreMultilingual.addContent(elItem);
                }
            }

            Element elType = new Element("type");
            String type = currentTh.getType();
            elType.addContent(type);

            Element elDate = new Element("date");
            String date = currentTh.getDate();
            elDate.addContent(date);

            Element elUrl = new Element("url");
            String url = currentTh.getDownloadUrl();
            elUrl.addContent(url);

            Element elDefaultURI = new Element("defaultNamespace");
            String defaultURI = currentTh.getDefaultNamespace();
            elDefaultURI.addContent(defaultURI);


            Element elActivated = new Element("activated");

            // By default thesaurus are enabled (if nothing defined in db)
            char activated = Constants.YN_TRUE;
            final Optional<ThesaurusActivation> activation = thesaurusActivationRepository.findById(currentTh.getKey());
            if (activation.isPresent() && !activation.get().isActivated()) {
                activated = Constants.YN_FALSE;
            }
            elActivated.setText("" + activated);

            elLoop.addContent(elKey);
            elLoop.addContent(elDname);
            elLoop.addContent(description);
            elLoop.addContent(elFname);
            elLoop.addContent(elTitle);
            elLoop.addContent(elMultilingualTitles);
            elLoop.addContent(elDublinCoreMultilingual);
            elLoop.addContent(elDate);
            elLoop.addContent(elUrl);
            elLoop.addContent(elDefaultURI);
            elLoop.addContent(elType);
            elLoop.addContent(elActivated);

            elRoot.addContent(elLoop);
        }

        return elRoot;
    }

    /**
     * A Task to build the thesaurus table once the servlet is up.
     *
     * Since <b>thesauri can be metadata records</b> (registers) they can also have xlinks. These
     * may not be resolveable until the servlet is up. Hence we start a thread that waits until the
     * servlet is up before reading the thesauri and creating the thesaurus table.
     */
    final class InitThesauriTableTask implements Runnable {

        private final ServiceContext context;
        private final Path thesauriDir;

        InitThesauriTableTask(ServiceContext context, Path thesauriDir) {
            this.context = context;
            this.thesauriDir = thesauriDir;
        }

        public void run() {
            context.setAsThreadLocal();
            try {
                // poll context to see whether servlet is up yet
                while (!context.isServletInitialized()) {
                    if (Log.isDebugEnabled(Geonet.THESAURUS_MAN)) {
                        Log.debug(Geonet.THESAURUS_MAN, "Waiting for servlet to finish initializing..");
                    }
                    Thread.sleep(10000); // sleep 10 seconds
                }
                try {
                    initThesauriTable(thesauriDir, context);
                } catch (Exception e) {
                    Log.error(Geonet.THESAURUS_MAN, "Error rebuilding thesaurus table : " + e.getMessage() + "\n" + Util.getStackTrace(e));
                }
            } catch (Exception e) {
                Log.debug(Geonet.THESAURUS_MAN, "Thesaurus table rebuilding thread threw exception", e);
            }
        }
    }
}
