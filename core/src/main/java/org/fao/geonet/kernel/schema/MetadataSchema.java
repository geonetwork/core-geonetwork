/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.schema;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaGroupId;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.editorconfig.Editor;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;


//==============================================================================

@JsonPropertyOrder({
    "name", "titles", "descriptions", "standardUrl", "targetNamespace", "namespaces",
    "readwriteUUID", "schematronRules"
})
public class MetadataSchema {
    public static final String SCHEMATRON_DIR = "schematron";
    private static final String XSL_FILE_EXTENSION = ".xsl";
    private static final String SCH_FILE_EXTENSION = ".sch";
    private static final String SCHEMATRON_RULE_FILE_PREFIX = "schematron-rules";
    private Map<String, List<String>> hmElements = new HashMap<>();
    private Map<String, List<List<String>>> hmRestric = new HashMap<>();
    private Map<String, MetadataType> hmTypes = new HashMap<>();
    private Map<String, List<String>> hmSubs = new HashMap<>();
    private Map<String, String> hmSubsLink = new HashMap<>();
    private Map<String, Namespace> hmNameSpaces = new HashMap<>();
    private Map<String, Namespace> hmPrefixes = new HashMap<>();
    private Map<String, MetadataSchemaOperationFilter> hmOperationFilters =
        new HashMap<>();
    private String schemaName;
    private Path schemaDir;
    private String standardUrl;
    private String version;
    private String appMinorVersionSupported;
    private String appMajorVersionSupported;
    private Map<String, String> titles = new HashMap<>();
    private Map<String, String> descriptions = new HashMap<>();
    private String primeNS;
    private String[] schematronRules;
    private boolean canEdit = false;
    private boolean readwriteUUID = false;
    private List<Element> rootAppInfoElements;

    private SchematronRepository schemaRepo;
    private SchematronCriteriaGroupRepository criteriaGroupRepository;
    private SchemaPlugin schemaPlugin;

    private String dependsOn;


    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    MetadataSchema(SchematronRepository schemaRepo, SchematronCriteriaGroupRepository criteriaGroupRepository) {
        schemaName = "UNKNOWN";
        this.schemaRepo = schemaRepo;
        this.criteriaGroupRepository = criteriaGroupRepository;
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public boolean canEdit() {
        return canEdit;
    }

    //---------------------------------------------------------------------------

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    //---------------------------------------------------------------------------

    public String getName() {
        return schemaName;
    }

    //---------------------------------------------------------------------------

    public void setName(String inName) {
        schemaName = inName;
        this.schemaPlugin = SchemaManager.getSchemaPlugin(schemaName);
    }

    @JsonIgnore
    public Editor getConfigEditor() {
        Path metadataSchemaConfig =
            getSchemaDir().resolve("layout").resolve("config-editor.xml");
        if (metadataSchemaConfig.toFile().exists()) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Editor.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                return (Editor) unmarshaller.unmarshal(metadataSchemaConfig.toFile());
            } catch (JAXBException e) {
                Log.error(Geonet.SCHEMA_MANAGER, " Get config editor. Error is " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Get schema directory
     */
    @JsonIgnore
    public Path getSchemaDir() {
        return schemaDir;
    }

    /**
     * Set schema directory
     */
    public void setSchemaDir(Path schemaDir) {
        this.schemaDir = schemaDir;
    }

    //---------------------------------------------------------------------------
    @JsonProperty(value = "targetNamespace")
    public String getPrimeNS() {
        return primeNS;
    }

    //---------------------------------------------------------------------------

    public void setPrimeNS(String theNS) {
        primeNS = theNS;
    }

    //---------------------------------------------------------------------------

    public MetadataType getTypeInfo(String type) {
        Logger.log();
        if (hmTypes.get(type) == null) return new MetadataType();
        else return hmTypes.get(type);
    }

    //---------------------------------------------------------------------------

    public String getElementType(String elem, String parent) throws Exception {
        // two cases here - if we have just one element (or a substitute) with
        // this name then return its type

        Logger.log();
        List<String> childType = hmElements.get(elem);
        if (childType == null) {
            // Check and see whether we can substitute another element from the
            // substitution link
            String oldelem = elem;
            elem = hmSubsLink.get(elem);
            Logger.log();
            childType = hmElements.get(elem);
            if (childType == null) {
                Log.warning(Geonet.SCHEMA_MANAGER, "ERROR: Mismatch between schema and xml: No type for 'element' : "
                    + oldelem + " with parent " + parent + ". Returning xs:string");
                return "xs:string";
            }
        }
        if (childType.size() == 1) return childType.get(0);

        Logger.log();
        // OTHERWISE get the type by examining the parent:
        // for each parent with that name parent
        // 1. retrieve its mdt
        List<String> exType = hmElements.get(parent);
        if (exType == null) return "xs:string";
        for (String type : exType) {
            // 2. search that mdt for the element names elem
            MetadataType mdt = getTypeInfo(type);
            for (int k = 0; k < mdt.getElementCount(); k++) {
                String elemTest = mdt.getElementAt(k);
                // 3. return the type name of that element
                if (elem.equals(elemTest)) {
                    return mdt.getElementTypeAt(k);
                }
            }
        }

        Logger.log();
        return null;
    }

    //---------------------------------------------------------------------------

    /**
     * A simple type is a type that has no children and no attributes (but can have restrictions on
     * its value)
     */

    public boolean isSimpleElement(String elem, String parent) throws Exception {
        String type = getElementType(elem, parent);
        return type != null && !hmTypes.containsKey(type);
    }

    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------

    public List<String> getElementValues(String elem, String parent) throws Exception {

        String type = getElementType(elem, parent);
        String restricName = elem;
        if (type != null) restricName = restricName + "+" + type;

        // two cases here - if we have just one element with this name
        // then return its values
        List<List<String>> childValues = hmRestric.get(restricName);
        if (childValues == null) return null;
        if (childValues.size() == 1) return childValues.get(0);

        // OTHERWISE we don't know what to do so return the first one anyway! This
        // should not happen....
        Logger.log();
        return childValues.get(0);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Package protected API methods
    //---
    //---------------------------------------------------------------------------

    void addElement(String name, String type, List<String> alValues, List<String> alSubs, String subLink) {
        // first just add the subs - because these are for global elements we
        // never have a clash because global elements are all in the same scope
        // and are thus unique
        if (alSubs != null && !alSubs.isEmpty()) hmSubs.put(name, alSubs);
        if (subLink != null && StringUtils.isNotBlank(subLink)) hmSubsLink.put(name, subLink);

        List<String> exType = hmElements.get(name);

        // it's already there but the type has been added already
        if (exType != null && exType.contains(type)) return;

        // it's already there but doesn't have this type
        if (exType != null && !(exType.contains(type))) {
            Logger.log();


            // it's not there so add a new list
        } else {
            exType = new ArrayList<>();
            hmElements.put(name, exType);
        }
        exType.add(type);

        String restricName = name;
        if (type != null) restricName = name + "+" + type;

        // it's already there
        List<List<String>> exValues = hmRestric.get(restricName);
        if (exValues != null) {
            Logger.log();

            // it's not there so add a new list of lists
        } else {
            exValues = new ArrayList<>();
            hmRestric.put(restricName, exValues);
        }
        exValues.add(alValues);
    }

    //---------------------------------------------------------------------------

    public void addType(String name, MetadataType mdt) {
        mdt.setName(name);
        hmTypes.put(name, mdt);
    }

    //---------------------------------------------------------------------------

    public void addNS(String targetNSPrefix, String targetNSUri) {

        Namespace ns = Namespace.getNamespace(targetNSPrefix, targetNSUri);
        hmNameSpaces.put(targetNSPrefix, ns);
        hmPrefixes.put(targetNSUri, ns);
    }

    //---------------------------------------------------------------------------
    @JsonIgnore
    public String getNS(String targetNSPrefix) {
        Namespace ns = hmNameSpaces.get(targetNSPrefix);
        if (ns != null) {
            return ns.getURI();
        } else {
            return null;
        }
    }


    /**
     * Return the list of namespaces for the schema.
     */
    @JsonIgnore
    public List<Namespace> getNamespaces() {
        List<Namespace> list = new ArrayList<>(hmNameSpaces.size());
        for (Namespace ns : hmNameSpaces.values()) {
            list.add(ns);
        }
        return list;
    }

    //---------------------------------------------------------------------------

    public String getPrefix(String theNSUri) {
        Namespace ns = hmPrefixes.get(theNSUri);
        if (ns != null) {
            return ns.getPrefix();
        } else {
            return null;
        }
    }

    //---------------------------------------------------------------------------
    @JsonIgnore
    public List<Namespace> getSchemaNS() {
        return new ArrayList<>(hmPrefixes.values());
    }

    @JsonProperty(value = "namespaces")
    public Map<String, String> getSchemaNSWithPrefix() {
        Map<String, String> mapNs = new HashMap<>();
        List<Namespace> schemaNsList = getSchemaNS();

        for (Namespace ns : schemaNsList) {
            mapNs.put(ns.getPrefix(), ns.getURI());
        }
        return mapNs;
    }

    public void buildchematronRules(Path basePath) {
        Path schematronResourceDir = basePath.resolve("WEB-INF").resolve("classes").resolve(SCHEMATRON_DIR);
        Path schemaSchematronDir = schemaDir.resolve(SCHEMATRON_DIR);
        Path schematronCompilationFile = schematronResourceDir.resolve("iso_svrl_for_xslt2.xsl");
        Path schematronExpandFile = schematronResourceDir.resolve("iso_abstract_expand.xsl");

        if(Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
            Log.debug(Geonet.SCHEMA_MANAGER, "     Schematron compilation for schema " + schemaName);
            Log.debug(Geonet.SCHEMA_MANAGER, "          - compiling with " + schematronCompilationFile);
            Log.debug(Geonet.SCHEMA_MANAGER, "          - rules location is " + schemaSchematronDir);
        }

        if (Files.exists(schemaSchematronDir)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(schemaSchematronDir, "*.sch")) {
                for (Path rule : paths) {
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
                        Log.debug(Geonet.SCHEMA_MANAGER, "                - rule " + rule);
                    }

                    // Compile all schematron rules
                    final String xslPath = rule.toAbsolutePath().toString().replace(SCH_FILE_EXTENSION, XSL_FILE_EXTENSION);
                    Path schematronXslFilePath = rule.getFileSystem().getPath(xslPath);

                    try (OutputStream schematronXsl = Files.newOutputStream(schematronXslFilePath)) {
                        Element schematronRule = Xml.loadFile(schemaSchematronDir.resolve(rule));
                        // Expand schematron abstract rules
                        Element schematronExpandXml = Xml.transform(schematronRule, schematronExpandFile);
                        Xml.transform(schematronExpandXml, schematronCompilationFile, schematronXsl);
                    } catch (FileNotFoundException e) {
                        Log.error(Geonet.SCHEMA_MANAGER, "     Schematron rule file not found " + schematronXslFilePath
                            + ". Error is " + e.getMessage(), e);
                    } catch (Exception e) {
                        Log.error(Geonet.SCHEMA_MANAGER, "     Schematron rule compilation failed for " + schematronXslFilePath
                            + ". Error is " + e.getMessage(), e);
                    }

                }
            } catch (IOException e) {
                Log.error(Geonet.SCHEMA_MANAGER, "     Schematron rule file not found " + schemaSchematronDir
                    + ". Error is " + e.getMessage(), e);
            }
        }
    }

    /**
     * Compile and register all schematron rules available for current schema. Schematron rules
     * files are in schema schematron directory and start with "schematron-rules" prefix.
     */
    public void loadSchematronRules(Path basePath) {
        // Compile schema schematron rules
        buildchematronRules(basePath);

        List<String> saSchemas = Lists.newArrayList();

        final Path schematronDir = schemaDir.resolve(SCHEMATRON_DIR);
        if (Files.exists(schematronDir)) {

            Map<String, Schematron> existing = Maps.newHashMap();

            for (Schematron schematron : schemaRepo.findAllBySchemaName(schemaName)) {
                existing.put(schematron.getRuleName(), schematron);
            }

            try (DirectoryStream<Path> schematronFiles = Files.newDirectoryStream(schematronDir, new SchematronReportRulesFilter())) {
                for (Path schematronFile : schematronFiles) {
                    final String schematronFileName = schematronFile.getFileName().toString();
                    saSchemas.add(schematronFileName);

                    org.fao.geonet.domain.Schematron schematron = new org.fao.geonet.domain.Schematron();
                    schematron.setSchemaName(schemaName);
                    schematron.setFile(schematronFileName);
                    schematron.setDisplayPriority(0);

                    //if schematron not already exists
                    if (existing.containsKey(schematron.getRuleName())) {
                        if (!Files.exists(schematronDir.resolve(schematron.getFile()))) {
                            schematron.setFile(schematronFileName);
                            schemaRepo.saveAndFlush(schematron);
                        }
                    } else {
                        schematron.getLabelTranslations().put(Geonet.DEFAULT_LANGUAGE, schematron.getRuleName());
                        schemaRepo.saveAndFlush(schematron);

                        final SchematronCriteriaGroup schematronCriteriaGroup = new SchematronCriteriaGroup();
                        schematronCriteriaGroup.setId(new SchematronCriteriaGroupId("*Generated*", schematron.getId()));
                        schematronCriteriaGroup.setRequirement(schematron.getDefaultRequirement());

                        SchematronCriteria criteria = new SchematronCriteria();
                        criteria.setValue("_ignored_");
                        criteria.setType(SchematronCriteriaType.ALWAYS_ACCEPT);

                        schematronCriteriaGroup.addCriteria(criteria);

                        criteriaGroupRepository.saveAndFlush(schematronCriteriaGroup);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            setSchematronPriorities();
        }

        setSchematronRules(saSchemas.toArray(new String[saSchemas.size()]));
    }

    private void setSchematronPriorities() {
        List<Schematron> schematronList = schemaRepo.findAllBySchemaName(schemaName);

        Collections.sort(schematronList, Schematron.DISPLAY_PRIORITY_COMPARATOR);

        List<Schematron> updated = Lists.newArrayList();
        for (int i = 0; i < schematronList.size(); i++) {
            Schematron schematron = schematronList.get(i);
            if (schematron.getDisplayPriority() != i) {
                schematron.setDisplayPriority(i);
                updated.add(schematron);
            }
        }
        this.schemaRepo.saveAll(updated);
    }

    public void setOperationFilters(Map<String, MetadataSchemaOperationFilter> operationFilters) {
        this.hmOperationFilters = operationFilters;
    }

    /**
     * Get the XPath filter for the reserved operation.
     *
     * @return The XPath to select element to filter or null
     */
    public MetadataSchemaOperationFilter getOperationFilter(ReservedOperation operation) {
        return hmOperationFilters.get(operation.name());
    }

    public MetadataSchemaOperationFilter getOperationFilter(String operation) {
        return hmOperationFilters.get(operation);
    }

    @JsonIgnore
    public SchemaPlugin getSchemaPlugin() {
        return schemaPlugin;
    }

    /**
     * Return the list of schematron rules to applied for this schema
     */
    public String[] getSchematronRules() {
        if (schematronRules != null) {
            return this.schematronRules.clone();
        } else {
            return new String[]{};
        }
    }

    private void setSchematronRules(String[] schematronRules) {
        if (schematronRules != null) {
            this.schematronRules = schematronRules.clone();
        }
    }

    public void setRootAppInfoElements(List<Element> rootAppInfoElements) {
        this.rootAppInfoElements = rootAppInfoElements;
    }

    // -- this info for profile detection methods
    @JsonIgnore
    public List<Element> getSchemaAppInfoElements() {
        return rootAppInfoElements;
    }

    /**
     * true if schema requires to synch the uuid column schema info with the uuid in the metadata
     * record (updated on editing or in UFO).
     */
    public boolean isReadwriteUUID() {
        return readwriteUUID;
    }

    public void setReadwriteUUID(boolean readwriteUUID) {
        this.readwriteUUID = readwriteUUID;
    }

    public String getStandardUrl() {
        return standardUrl;
    }

    public void setStandardUrl(String standardUrl) {
        this.standardUrl = standardUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppMinorVersionSupported() {
        return appMinorVersionSupported;
    }

    public void setAppMinorVersionSupported(String appMinorVersionSupported) {
        this.appMinorVersionSupported = appMinorVersionSupported;
    }

    public String getAppMajorVersionSupported() {
        return appMajorVersionSupported;
    }

    public void setAppMajorVersionSupported(String appMajorVersionSupported) {
        this.appMajorVersionSupported = appMajorVersionSupported;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String depends) {
        this.dependsOn = depends;
    }

    public Map<String, String> getTitles() {
        return titles;
    }

    public void setTitles(Map<String, String> titles) {
        this.titles = titles;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Schematron rules filename is like "schematron-rules-iso.xsl
     */
    private static class SchematronReportRulesFilter implements DirectoryStream.Filter<Path> {
        public boolean accept(Path entry) {
            String filename = entry.getFileName().toString();
            return filename.startsWith(SCHEMATRON_RULE_FILE_PREFIX) && filename.endsWith(XSL_FILE_EXTENSION);
        }
    }

    /**
     * Query XML document with one of the saved query
     * to retrieve a simple string value.
     *
     * @param savedQuery {@link SavedQuery}
     * @param xml
     */
    public String queryString(String savedQuery, Element xml) throws ResourceNotFoundException, JDOMException {
        SavedQuery query = schemaPlugin.getSavedQuery(savedQuery);
        if (query == null) {
            throw new ResourceNotFoundException(String.format(
                "Saved query '%s' for schema '%s' not found. Available queries are '%s'.",
                savedQuery, getName(),
                schemaPlugin.getSavedQueries().stream().map(SavedQuery::getId).collect(Collectors.joining(", "))));
        }

        String xpath = query.getXpath();
        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
            Log.error(Geonet.SCHEMA_MANAGER, String.format(
                "Saved query XPath: %s", xpath));
        }

        return Xml.selectString(xml,
            xpath,
            getNamespaces());
    }
}
