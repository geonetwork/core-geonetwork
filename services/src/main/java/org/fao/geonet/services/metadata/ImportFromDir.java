//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata;

import com.google.common.collect.Lists;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// FIXME: this class could be moved to DataManager


/**
 * Import all metadata found inside a given directory.
 */
public class ImportFromDir extends NotInReadOnlyModeService {
    //--------------------------------------------------------------------------

    private static final String CONFIG_FILE = "import-config.xml";
    private Path stylePath;
    private Map<String, Exception> exceptions = new HashMap<String, Exception>();
    private boolean failOnError;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.stylePath = appPath.resolve(Geonet.Path.IMPORT_STYLESHEETS);
    }

    //--------------------------------------------------------------------------
    //---
    //--- API
    //---
    //--------------------------------------------------------------------------

    /**
     * util.import service allow to import metadata in batch mode using 2 different mode: <ul>
     * <li>Search for all xml file in given directory and load them</li> <li>Search for a file named
     * import-config.xml in given directory and the import as described in that file.</li> </ul>
     *
     * Fail on error parameter is available for directory batch mode. If true, import stop on first
     * error.
     *
     * Return the number of record inserted and the list of exceptions.
     */
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String dir = Util.getParam(params, Params.DIR);
        failOnError = Util.getParam(params, Params.FAIL_ON_ERROR, "off").equals("on");
        Path file = IO.toPath(dir).resolve(CONFIG_FILE);

        //-----------------------------------------------------------------------

        long start = System.currentTimeMillis();

        int result;

        if (Files.exists(file)) {
            result = configImport(params, context, file);
        } else {
            result = standardImport(params, context);
        }

        long end = System.currentTimeMillis();
        long duration = (end - start) / 1000;

        context.info("Import time is :" + duration + " secs");

        Element response = new Element("response")
            .addContent(new Element("records").setText("" + result))
            .addContent(new Element("time").setText("" + duration));

        if (exceptions.size() > 0) {
            Element ex = new Element("exceptions").setAttribute("count", "" + exceptions.size());
            for (Map.Entry<String, Exception> e : exceptions.entrySet()) {
                Element exceptionInfo = new Element("exception");
                exceptionInfo.setAttribute("file", e.getKey());
                if (e.getValue() instanceof SchematronValidationErrorEx) {
                    exceptionInfo.addContent((Element)
                        ((SchematronValidationErrorEx) e.getValue()).getObject());
                } else if (e instanceof XSDValidationErrorEx) {
                    exceptionInfo.addContent((Element)
                        ((XSDValidationErrorEx) e.getValue()).getObject());
                } else {
                    exceptionInfo.setText(e.getValue().getMessage());
                }
                ex.addContent(exceptionInfo);
            }
            response.addContent(ex);
        }

        return response;
    }

    //--------------------------------------------------------------------------
    //---
    //--- ImportMetadataReindexer class (used in standardImport)
    //---
    //--------------------------------------------------------------------------

    private int standardImport(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        String dir = Util.getParam(params, Params.DIR);
        boolean recurse = Util.getParam(params, Params.RECURSE, "off").equals("on");

        final List<Path> files = Lists.newArrayList();
        final MEFLib.MefOrXmlFileFilter predicate = new MEFLib.MefOrXmlFileFilter();
        if (recurse) {
            Files.walkFileTree(IO.toPath(dir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (predicate.accept(file)) {
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(IO.toPath(dir), predicate)) {
                for (Path file : paths) {
                    files.add(file);
                }
            }
        }

        if (files.size() == 0)
            throw new Exception("No XML or MEF file found in " + dir);

        ImportMetadataReindexer r = new ImportMetadataReindexer(dm, params, context, files, stylePath, failOnError);
        r.process();
        exceptions = r.getExceptions();

        return files.size();
    }

    private int configImport(Element params, ServiceContext context, Path configFile) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        ImportConfig config = new ImportConfig(configFile, context);

        String dir = Util.getParam(params, Params.DIR);
        String group = Util.getParam(params, Params.GROUP);
        String style = Util.getParam(params, Params.STYLESHEET);

        boolean validate = Util.getParam(params, Params.VALIDATE, "off").equals("on");

        int counter = 0;
        ArrayList<ImportInfo> alImport = new ArrayList<ImportInfo>();

        try (DirectoryStream<Path> sites = Files.newDirectoryStream(IO.toPath(dir))) {
            for (Path site : sites) {
                if (context.isDebugEnabled())
                    context.debug("Scanning site : " + site);

                try (DirectoryStream<Path> categs = Files.newDirectoryStream(site)) {
                    for (Path categ : categs) {
                        if (context.isDebugEnabled()) {
                            context.debug("   Scanning category : " + categ);
                        }

                        try (DirectoryStream<Path> paths = Files.newDirectoryStream(categ, new MEFLib.MefOrXmlFileFilter())) {
                            for (Path file : paths) {
                                Element xml = Xml.loadFile(file);

                                if (!style.equals("_none_")) {
                                    FilePathChecker.verify(style);

                                    xml = Xml.transform(xml, stylePath.resolve(style));
                                }

                                String category = config.mapCategory(categ.toString());
                                String schema = config.mapSchema(categ.toString());

                                if (validate)
                                    dm.validate(schema, xml);

                                alImport.add(new ImportInfo(schema, category, xml));
                                counter++;
                            }
                        }
                    }
                }

            }
        }

        //--- step 2 : insert metadata

        for (ImportInfo ii : alImport)
            insert(ii.schema, ii.xml, group, context, ii.category);

        return counter;
    }

    private void insert(String schema, Element xml, String group, ServiceContext context,
                        String category) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        //-----------------------------------------------------------------------
        //--- if the uuid does not exist we generate it

        String uuid = dm.extractUUID(schema, xml);

        if (uuid.length() == 0)
            uuid = UUID.randomUUID().toString();

        if (category.equals("_none_")) category = null;
        //-----------------------------------------------------------------------
        //--- insert metadata into the system

        String docType = null, title = null, createDate = null, changeDate = null;
        boolean ufo = true, indexImmediate = true;
        String isTemplate = "n";
        dm.insertMetadata(context, schema, xml, uuid, context.getUserSession().getUserIdAsInt(), group, gc.getBean(SettingManager
                .class).getSiteId(),
            isTemplate, docType, category, createDate, changeDate, ufo, indexImmediate);

    }

    //--------------------------------------------------------------------------
    //---
    //--- Standard import
    //---
    //--------------------------------------------------------------------------

    public static final class ImportCallable implements Callable<HashMap<String, Exception>> {
        private final List<Path> files;
        private final int beginIndex, count;
        private final Element params;
        private final Path stylePath;
        private final boolean failOnError;
        private final ServiceContext context;
        private final int userId;
        private final String userName;
        private final Profile userProfile;

        ImportCallable(List<Path> files, int beginIndex, int count, Element params, ServiceContext context, Path stylePath, boolean failOnError) {
            this.files = files;
            this.beginIndex = beginIndex;
            this.count = count;
            this.params = params;
            this.context = context;
            this.stylePath = stylePath;
            this.failOnError = failOnError;
            this.userId = Integer.valueOf(this.context.getUserSession().getUserId());
            this.userName = this.context.getUserSession().getUsername();
            this.userProfile = this.context.getUserSession().getProfile();
        }

        private void login() {
            UserSession session = new UserSession();
            session.loginAs(new User().setUsername(this.userName).setId(this.userId).setProfile(this.userProfile));
            this.context.setUserSession(session);
        }

        public HashMap<String, Exception> call() throws Exception {
            HashMap<String, Exception> exceptions = new HashMap<String, Exception>();

            login();

            for (Path file : files) {
                try {
                    MEFLib.doImport(params, context, file, stylePath);
                } catch (Exception e) {
                    if (failOnError)
                        throw e;

                    exceptions.put(file.toString(), e);
                }
            }
            return exceptions;
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Config import
    //---
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    public class ImportMetadataReindexer extends MetadataIndexerProcessor {
        Element params;
        List<Path> files;
        Path stylePath;
        ServiceContext context;
        HashMap<String, Exception> exceptions = new HashMap<String, Exception>();


        public ImportMetadataReindexer(DataManager dm, Element params, ServiceContext context, List<Path> fileList, Path stylePath,
                                       boolean failOnError) {
            super(dm);
            this.params = params;
            this.context = context;
            this.files = fileList;
            this.stylePath = stylePath;
        }

        public void process() throws Exception {
            int threadCount = ThreadUtils.getNumberOfThreads();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            int perThread;
            if (files.size() < threadCount) perThread = files.size();
            else perThread = files.size() / threadCount;
            int index = 0;

            List<Future<HashMap<String, Exception>>> submitList =
                new ArrayList<Future<HashMap<String, Exception>>>();
            while (index < files.size()) {
                int start = index;
                int count = Math.min(perThread, files.size() - start);
                // create threads to process this chunk of files
                Callable<HashMap<String, Exception>> worker =
                    new ImportCallable(files, start, count, params, context, stylePath, failOnError);
                Future<HashMap<String, Exception>> submit = executor.submit(worker);
                submitList.add(submit);
                index += count;
            }

            for (Future<HashMap<String, Exception>> future : submitList) {
                try {
                    exceptions.putAll(future.get());
                } catch (InterruptedException e) {
                    Log.error(Geonet.GEONETWORK, "ImportMetadataReindexer error: " + e.getMessage(), e);
                } catch (ExecutionException e) {
                    Log.error(Geonet.GEONETWORK, "ImportMetadataReindexer error: " + e.getMessage(), e);
                }
            }
            executor.shutdown();
        }

        public Map<String, Exception> getExceptions() {
            return exceptions;
        }
    }

}

//=============================================================================

class ImportInfo {
    public String schema;
    public String category;
    public Element xml;

    //--------------------------------------------------------------------------

    public ImportInfo(String schema, String category, Element xml) {
        this.schema = schema;
        this.category = category;
        this.xml = xml;
    }
}

//=============================================================================

class ImportConfig {
    private static final String CATEGORY_MAPPING = "categoryMapping";
    private static final String SCHEMA_MAPPING = "schemaMapping";

    private static final String MAPPING = "mapping";
    private static final String DEFAULT = "default";

    private static final String ATTR_DIR = "dir";
    private static final String ATTR_TO = "to";

    //--------------------------------------------------------------------------

    private Map<String, String> htCategId = new HashMap<String, String>();
    private Map<String, String> htCategMapping = new HashMap<String, String>();
    private Map<String, String> htSchemaMapping = new Hashtable<String, String>();

    private String defaultCateg;
    private String defaultSchema;

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public ImportConfig(Path configFile, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        Element config = Xml.loadFile(configFile);

        fillCategIds(context);

        mapCategor(config.getChild(CATEGORY_MAPPING));
        mapSchemas(config.getChild(SCHEMA_MAPPING), dm);
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public String mapCategory(String catDir) {
        String mapping = htCategMapping.get(catDir);

        if (mapping == null)
            mapping = defaultCateg;

        return mapping;
    }

    //--------------------------------------------------------------------------

    public String mapSchema(String catDir) {
        String mapping = htSchemaMapping.get(catDir);

        if (mapping == null)
            mapping = defaultSchema;

        return mapping;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private void fillCategIds(ServiceContext context) throws Exception {
        final List<MetadataCategory> metadataCategories = context.getBean(MetadataCategoryRepository.class).findAll();

        for (MetadataCategory record : metadataCategories) {
            String id = "" + record.getId();
            String name = record.getName();

            htCategId.put(name, id);
        }
    }

    //--------------------------------------------------------------------------

    private void mapCategor(Element categMapping) {
        @SuppressWarnings("unchecked")
        List<Element> list = categMapping.getChildren(MAPPING);

        for (Element el : list) {
            String dir = el.getAttributeValue(ATTR_DIR);
            String to = el.getAttributeValue(ATTR_TO);

            String categId = htCategId.get(to);

            if (categId == null)
                throw new IllegalArgumentException("Category not found : " + to);

            htCategMapping.put(dir, categId);
        }

        String defaultTo = categMapping.getChild(DEFAULT).getAttributeValue(ATTR_TO);
        String defaultId = htCategId.get(defaultTo);

        if (defaultId == null)
            throw new IllegalArgumentException("Default category not found : " + defaultTo);

        defaultCateg = defaultTo;
    }

    //--------------------------------------------------------------------------

    private void mapSchemas(Element schemaMapping, DataManager dm) {
        @SuppressWarnings("unchecked")
        List<Element> list = schemaMapping.getChildren(MAPPING);

        for (Element el : list) {
            String dir = el.getAttributeValue(ATTR_DIR);
            String to = el.getAttributeValue(ATTR_TO);

            if (!dm.existsSchema(to))
                throw new IllegalArgumentException("Schema not found : " + to);

            htSchemaMapping.put(dir, to);
        }

        String defaultTo = schemaMapping.getChild(DEFAULT).getAttributeValue(ATTR_TO);

        if (!dm.existsSchema(defaultTo))
            throw new IllegalArgumentException("Default schema not found : " + defaultTo);

        defaultSchema = defaultTo;
    }
}
