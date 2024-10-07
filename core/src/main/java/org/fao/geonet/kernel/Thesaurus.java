//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Wheres;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.utils.DateUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.openrdf.model.*;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.sesame.admin.DummyAdminListener;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.sail.StatementIterator;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Thesaurus {

    private static final String DEFAULT_THESAURUS_NAMESPACE = "http://custom.shared.obj.ch/concept#";

    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDF_SCHEMA_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    private static final String SKOS_NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

    private static final String DCTERMS_NAMESPACE = "http://purl.org/dc/terms/";

    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";

    private String fname;

    private String type;

    private String dname;

    private Path thesaurusFile;

    private LocalRepository repository;

    private String title;

    private String description;

    private String date;

    private String createdDate;

    private String issuedDate;

    private String modifiedDate;

    private String defaultNamespace;

    private String downloadUrl;

    private String keywordUrl;

    private IsoLanguagesMapper isoLanguageMapper;

    private Map<String, String> multilingualTitles = new Hashtable<>();

    private Map<String, String> multilingualDescriptions = new Hashtable<>();

    // map of lang -> dictionary of values
    //                 key is a dublinCore element (i.e. https://guides.library.ucsc.edu/c.php?g=618773&p=4306386)
    // see #retrieveDublinCore() for example
    private Map<String, Map<String,String>> dublinCoreMultilingual =   new Hashtable<>();

    private Cache<String, Object> THESAURUS_SEARCH_CACHE;


    /**
     * Available for subclasses.
     */
    protected Thesaurus() {
    }

    /**
     * @param fname file name
     * @param dname category/domain name of thesaurus
     * @param thesaurusCacheMaxSize
     */
    public Thesaurus(IsoLanguagesMapper isoLanguageMapper, String fname, String type, String dname, Path thesaurusFile, String siteUrl, int thesaurusCacheMaxSize) {
        this(isoLanguageMapper, fname, null, null, type, dname, thesaurusFile, siteUrl, false, thesaurusCacheMaxSize);
    }

    public Thesaurus(IsoLanguagesMapper isoLanguageMapper, String fname, String tname, String tnamespace, String type, String dname, Path thesaurusFile, String siteUrl, boolean ignoreMissingError, int thesaurusCacheMaxSize) {
        this(isoLanguageMapper, fname, null, null, null, type, dname, thesaurusFile, siteUrl, false, thesaurusCacheMaxSize);
    }

    public Thesaurus(IsoLanguagesMapper isoLanguageMapper, String fname,
                     Map<String, String> multilingualTitles, Map<String, String> multilingualDescriptions,
                     String tnamespace, String type, String dname, Path thesaurusFile, String siteUrl,
                     boolean ignoreMissingError, int thesaurusCacheMaxSize) {
        super();

        THESAURUS_SEARCH_CACHE = CacheBuilder.newBuilder()
            .maximumSize(thesaurusCacheMaxSize)
            .expireAfterAccess(25, TimeUnit.HOURS)
            .build();

        this.isoLanguageMapper = isoLanguageMapper;
        this.fname = fname;
        this.type = type;
        this.dname = dname;
        this.thesaurusFile = thesaurusFile;
        if (!siteUrl.endsWith("/")) {
            siteUrl += '/';
        }
        this.downloadUrl = buildDownloadUrl(fname, type, dname, siteUrl);
        this.keywordUrl = buildKeywordUrl(fname, type, dname, siteUrl);

        this.defaultNamespace = (tnamespace == null ? DEFAULT_THESAURUS_NAMESPACE : tnamespace);

        retrieveThesaurusInformation(thesaurusFile, dname + "." + fname, ignoreMissingError);

        if (multilingualTitles != null) {
            this.multilingualTitles = multilingualTitles;

            if (StringUtils.isBlank(this.title)) {
                this.title = this.multilingualTitles.get(toiso639_1_Lang(Geonet.DEFAULT_LANGUAGE));
            }

            if (StringUtils.isBlank(this.title)) {
                this.title = dname + "." + fname;
            }
        }

        if (multilingualDescriptions != null) {
            this.multilingualDescriptions = multilingualDescriptions;

            if (StringUtils.isBlank(this.description)) {
                this.description = this.multilingualDescriptions.get(toiso639_1_Lang(Geonet.DEFAULT_LANGUAGE));
            }
        }
    }

    /**
     *
     * @param fname
     * @param type
     * @param dname
     * @return
     */
    public static String buildThesaurusKey(String fname, String type, String dname) {
        String name = fname;
        if (name.endsWith(".rdf")) {
            name = name.substring(0, fname.indexOf(".rdf"));
        }
        return type + "." + dname + "." + name;
    }

    public Map<String, String> getMultilingualTitles() {
        return Collections.unmodifiableMap(this.multilingualTitles);
    }

    public Map<String,Map<String, String>> getDublinCoreMultilingual() {
        return Collections.unmodifiableMap(this.dublinCoreMultilingual);
    }

    public Map<String, String> getMultilingualDescriptions() {
        return Collections.unmodifiableMap(this.multilingualDescriptions);
    }

    public void setMultilingualTitles(Map<String, String> multilingualTitles) {
        this.multilingualTitles = multilingualTitles;
    }

    public void setMultilingualDescriptions(Map<String, String> multilingualDescriptions) {
        this.multilingualDescriptions = multilingualDescriptions;
    }

    /**
     * @return Thesaurus identifier
     */
    public String getKey() {
        return buildThesaurusKey(fname, type, dname);
    }

    /**
     * Get the Domain/category name of the thesaurus
     */
    public String getDname() {
        return dname;
    }

    public String getFname() {
        return fname;
    }

    public Path getFile() {
        return thesaurusFile;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    @Nonnull
    public FileTime getLastModifiedTime() {
        FileTime lastModified;
        try {
            lastModified = Files.getLastModifiedTime(getFile());
        } catch (IOException e) {
            lastModified = FileTime.fromMillis(System.currentTimeMillis());
        }

        if (Log.isDebugEnabled(Geonet.THESAURUS)) {
            Log.debug(Geonet.THESAURUS, title + " has lastModified of: " + lastModified.toMillis());
        }

        return lastModified;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getKeywordUrl() {
        return keywordUrl;
    }

    public void retrieveThesaurusInformation() {
        retrieveThesaurusInformation(thesaurusFile, dname + "." + fname, false);
    }

    protected String buildDownloadUrl(String fname, String type, String dname, String siteUrl) {
        if (type.equals(Geonet.CodeList.REGISTER)) {
            return siteUrl + "?uuid=" + fname.substring(0, fname.indexOf(".rdf"));
        } else {
            return siteUrl.substring(0, siteUrl.length() - 4) + "api/registries/vocabularies/" + Thesaurus.buildThesaurusKey(fname, type, dname);
        }
    }

    protected String buildKeywordUrl(String fname, String type, String dname, String siteUrl) {
        return siteUrl + "xml.keyword.get?thesaurus=" + Thesaurus.buildThesaurusKey(fname, type, dname) + "&amp;id=";
        // needs to have term/concept id tacked onto the end
    }

    public synchronized LocalRepository getRepository() {
        return repository;
    }

    public synchronized Thesaurus setRepository(LocalRepository repository) {
        this.repository = repository;
        return this;
    }

    public synchronized Thesaurus initRepository() throws ConfigurationException, IOException {
        RepositoryConfig repConfig = new RepositoryConfig(getKey());

        SailConfig syncSail = new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfSchemaRepository");
        SailConfig memSail = new org.openrdf.sesame.sailimpl.memory.RdfSchemaRepositoryConfig(getFile().toString(),
            RDFFormat.RDFXML);
        repConfig.addSail(syncSail);
        repConfig.addSail(memSail);
        repConfig.setWorldReadable(true);
        repConfig.setWorldWriteable(true);

        LocalRepository thesaurusRepository = Sesame.getService().createRepository(repConfig);
        setRepository(thesaurusRepository);
        return this;
    }

    public synchronized QueryResultsTable performRequest(String query) throws IOException, MalformedQueryException,
        QueryEvaluationException, AccessDeniedException {
        if (Log.isDebugEnabled(Geonet.THESAURUS))
            Log.debug(Geonet.THESAURUS, "Query : " + query);

        return repository.performTableQuery(QueryLanguage.SERQL, query);
    }

    public boolean hasConceptScheme(String uri) {

        String query = "SELECT conceptScheme"
            + " FROM {conceptScheme} rdf:type {skos:ConceptScheme}"
            + " WHERE conceptScheme = <" + uri + ">"
            + " USING NAMESPACE skos = <http://www.w3.org/2004/02/skos/core#>";

        try {
            return performRequest(query).getRowCount() > 0;
        } catch (Exception e) {
            Log.error(Geonet.THESAURUS_MAN,
                String.format("Error retrieving concept scheme for %s. Error is: %s", thesaurusFile, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    public List<String> getConceptSchemes() {

        String query = "SELECT conceptScheme"
            + " FROM {conceptScheme} rdf:type {skos:ConceptScheme}"
            + " USING NAMESPACE skos = <http://www.w3.org/2004/02/skos/core#>";

        try {
            List<String> ret = new ArrayList<>();
            QueryResultsTable table = performRequest(query);
            for (int i = 0; i < table.getRowCount(); i++) {
                Value value = table.getValue(i, 0);
                ret.add(value.toString());
            }
            return ret;
        } catch (Exception e) {
            Log.error(Geonet.THESAURUS_MAN, String.format(
                "Error retrieving concept schemes for %s. Error is: %s", thesaurusFile, e.getMessage()));
            return Collections.emptyList();
        }
    }

    /**
     * Add a keyword to the Thesaurus.
     *
     * @param keyword The keyword to add
     */
    public synchronized URI addElement(KeywordBean keyword) throws IOException, AccessDeniedException, GraphException {
        THESAURUS_SEARCH_CACHE.invalidateAll();
        Graph myGraph = new org.openrdf.model.impl.GraphImpl();

        ValueFactory myFactory = myGraph.getValueFactory();

        // Define namespace
        String namespaceGml = "http://www.opengis.net/gml#";

        // Create subject
        URI mySubject = myFactory.createURI(keyword.getUriCode());

        URI skosClass = myFactory.createURI(SKOS_NAMESPACE, "Concept");
        URI rdfType = myFactory.createURI(org.openrdf.vocabulary.RDF.TYPE);
        URI predicatePrefLabel = myFactory
            .createURI(SKOS_NAMESPACE, "prefLabel");
        URI predicateScopeNote = myFactory
            .createURI(SKOS_NAMESPACE, "scopeNote");

        URI predicateBoundedBy = myFactory.createURI(namespaceGml, "BoundedBy");
        URI predicateEnvelope = myFactory.createURI(namespaceGml, "Envelope");
        URI predicateSrsName = myFactory.createURI(namespaceGml, "srsName");
        URI srsNameURI = myFactory
            .createURI("http://www.opengis.net/gml/srs/epsg.xml#epsg:4326");
        BNode gmlNode = myFactory.createBNode();
        URI predicateLowerCorner = myFactory.createURI(namespaceGml,
            "lowerCorner");
        URI predicateUpperCorner = myFactory.createURI(namespaceGml,
            "upperCorner");

        Literal lowerCorner = myFactory.createLiteral(keyword.getCoordWest() + " " + keyword.getCoordSouth());
        Literal upperCorner = myFactory.createLiteral(keyword.getCoordEast() + " " + keyword.getCoordNorth());

        mySubject.addProperty(rdfType, skosClass);
        Set<Entry<String, String>> values = keyword.getValues().entrySet();
        for (Entry<String, String> entry : values) {
            String language = toiso639_1_Lang(entry.getKey());
            Value valueObj = myFactory.createLiteral(entry.getValue(), language);
            myGraph.add(mySubject, predicatePrefLabel, valueObj);

        }
        Set<Entry<String, String>> definitions = keyword.getDefinitions().entrySet();
        for (Entry<String, String> entry : definitions) {
            String language = toiso639_1_Lang(entry.getKey());
            Value definitionObj = myFactory.createLiteral(entry.getValue(), language);
            myGraph.add(mySubject, predicateScopeNote, definitionObj);

        }
        myGraph.add(mySubject, predicateBoundedBy, gmlNode);

        gmlNode.addProperty(rdfType, predicateEnvelope);
        myGraph.add(gmlNode, predicateLowerCorner, lowerCorner);
        myGraph.add(gmlNode, predicateUpperCorner, upperCorner);
        myGraph.add(gmlNode, predicateSrsName, srsNameURI);

        repository.addGraph(myGraph);
        return mySubject;
    }

    /**
     * Remove keyword from thesaurus.
     */
    public synchronized Thesaurus removeElement(KeywordBean keyword) throws AccessDeniedException {
        String namespace = keyword.getNameSpaceCode();
        String code = keyword.getRelativeCode();

        return removeElement(namespace, code);
    }

    /**
     * Remove keyword from thesaurus.
     */
    public synchronized Thesaurus removeElement(String namespace, String code) throws AccessDeniedException {
        Graph myGraph = repository.getGraph();
        ValueFactory myFactory = myGraph.getValueFactory();
        URI subject = myFactory.createURI(namespace, code);

        return removeElement(myGraph, subject);
    }

    /**
     * Remove keyword from thesaurus.
     */
    public synchronized Thesaurus removeElement(String uri) throws AccessDeniedException {
        Graph myGraph = repository.getGraph();
        ValueFactory myFactory = myGraph.getValueFactory();
        URI subject = myFactory.createURI(uri);

        return removeElement(myGraph, subject);
    }

    private Thesaurus removeElement(Graph myGraph, URI subject)
        throws AccessDeniedException {
        StatementIterator iter = myGraph.getStatements(subject, null, null);
        while (iter.hasNext()) {
            AtomicReference<Statement> st = new AtomicReference<Statement>(iter.next());
            if (st.get().getObject() instanceof BNode) {
                BNode node = (BNode) st.get().getObject();
                repository.getGraph().remove(node, null, null);
            }
        }
        int removedItems = myGraph.remove(subject, null, null);
        if (Log.isDebugEnabled(Geonet.THESAURUS)) {
            String msg = "Removed " + removedItems + " elements from thesaurus " + this.title + " with uri: " + subject;
            Log.debug(Geonet.THESAURUS, msg);
        }
        return this;
    }

    private String toiso639_1_Lang(String lang) {
        String defaultCode = getIsoLanguageMapper().iso639_2_to_iso639_1(
            Geonet.DEFAULT_LANGUAGE,
            Geonet.DEFAULT_LANGUAGE.substring(0, 2));
        return getIsoLanguageMapper().iso639_2_to_iso639_1(lang, defaultCode);
    }

    /**
     * Update the keyword.  If replaceLanguages is true all of the old data will be removed and the
     * new data in the keyword will replace it.
     *
     * @param keyword the bean with the updated information
     * @param replace If true all of the old data will be removed and the new data in the keyword
     *                will replace it.  If false only the data in the keyword will be updated.  This
     *                means only notes and labels that are in the keyword will be updated (for those
     *                languages) and the coordinates will only be updated if they are non-empty
     *                strings.
     */
    public synchronized URI updateElement(KeywordBean keyword, boolean replace) throws AccessDeniedException {
        THESAURUS_SEARCH_CACHE.invalidateAll();

        // Get thesaurus graph
        Graph myGraph = repository.getGraph();

        // Set namespace skos and predicates
        ValueFactory myFactory = myGraph.getValueFactory();
        URI predicatePrefLabel = myFactory.createURI(SKOS_NAMESPACE, "prefLabel");
        URI predicateScopeNote = myFactory.createURI(SKOS_NAMESPACE, "scopeNote");

        // Get subject (URI)
        URI subject = myFactory.createURI(keyword.getUriCode());

        // Remove old labels
        StatementIterator iter = myGraph.getStatements(subject, predicatePrefLabel, null);
        removeMatchingLiterals(replace, myGraph, iter, keyword.getValues().keySet());

        // remove old scopeNote
        iter = myGraph.getStatements(subject, predicateScopeNote, null);
        removeMatchingLiterals(replace, myGraph, iter, keyword.getDefinitions().keySet());

        // add updated Labels
        Set<Entry<String, String>> values = keyword.getValues().entrySet();
        for (Entry<String, String> entry : values) {
            String language = toiso639_1_Lang(entry.getKey());
            Value valueObj = myFactory.createLiteral(entry.getValue(), language);
            myGraph.add(subject, predicatePrefLabel, valueObj);

        }
        // add updated Definitions/Notes
        Set<Entry<String, String>> definitions = keyword.getDefinitions().entrySet();
        for (Entry<String, String> entry : definitions) {
            String language = toiso639_1_Lang(entry.getKey());
            Value definitionObj = myFactory.createLiteral(entry.getValue(), language);
            myGraph.add(subject, predicateScopeNote, definitionObj);

        }

        // update bbox
        if (replace || !(keyword.getCoordEast() + keyword.getCoordNorth() + keyword.getCoordWest() + keyword.getCoordSouth()).trim().isEmpty()) {
            String namespaceGml = "http://www.opengis.net/gml#";
            URI predicateBoundedBy = myFactory.createURI(namespaceGml, "BoundedBy");
            URI predicateLowerCorner = myFactory.createURI(namespaceGml, "lowerCorner");
            URI predicateUpperCorner = myFactory.createURI(namespaceGml, "upperCorner");

            BNode subjectGml = null;
            iter = myGraph.getStatements(subject, predicateBoundedBy, null);
            while (iter.hasNext()) {
                AtomicReference<Statement> st = new AtomicReference<>(iter.next());
                if (st.get().getObject() instanceof BNode) {
                    subjectGml = (BNode) st.get().getObject();
                }
            }
            if (subjectGml != null) {
                // lowerCorner
                iter = myGraph.getStatements(subjectGml, predicateLowerCorner, null);
                while (true) {
                    if (!(iter.hasNext())) {
                        break;
                    }
                    AtomicReference<Statement> st = new AtomicReference<>(iter.next());
                    myGraph.remove(st.get());
                    break;
                }
                // upperCorner
                iter = myGraph.getStatements(subjectGml, predicateUpperCorner, null);
                while (true) {
                    if (!(iter.hasNext())) {
                        break;
                    }
                    AtomicReference<Statement> st = new AtomicReference<>(iter.next());
                    myGraph.remove(st.get());
                    break;
                }
                // create the new statements
                Literal lowerCorner = myFactory.createLiteral(keyword.getCoordWest() + " " + keyword.getCoordSouth());
                Literal upperCorner = myFactory.createLiteral(keyword.getCoordEast() + " " + keyword.getCoordNorth());

                // Add the new statements
                myGraph.add(subjectGml, predicateLowerCorner, lowerCorner);
                myGraph.add(subjectGml, predicateUpperCorner, upperCorner);
            }
        }

        return subject;
    }


    private void removeMatchingLiterals(boolean replace, Graph myGraph, StatementIterator iter, Set<String> valueLanguages) {
        try {
            ArrayList<Statement> toRemove = new ArrayList<>();
            while (iter.hasNext()) {
                Statement st = iter.next();
                if (st.getObject() instanceof Literal) {
                    Literal litt = (Literal) st.getObject();
                    String lang = getIsoLanguageMapper().iso639_1_to_iso639_2(litt.getLanguage(), Geonet.DEFAULT_LANGUAGE);
                    if (replace || valueLanguages.contains(lang)) {
                        // remove
                        toRemove.add(st);
                    }
                }
            }

            for (Statement statement : toRemove) {
                myGraph.remove(statement);
            }
        } finally {
            iter.close();
        }
    }

    /**
     * Check if code is already used by a thesaurus concept.
     *
     * @param namespace Use null, to check a concept identifier not based on thesaurus namespace
     * @param code      The concept identifier
     */
    public synchronized boolean isFreeCode(String namespace, String code) throws AccessDeniedException {
        boolean res = true;
        Graph myGraph = repository.getGraph();
        ValueFactory myFactory = myGraph.getValueFactory();
        URI obj = namespace == null ? myFactory.createURI(code) : myFactory.createURI(namespace, code);
        Collection<?> statementsCollection = myGraph.getStatementCollection(obj, null, null);
        if (statementsCollection != null && !statementsCollection.isEmpty()) {
            res = false;
        }
        statementsCollection = myGraph.getStatementCollection(null, null, obj);
        if (statementsCollection != null && !statementsCollection.isEmpty()) {
            res = false;
        }
        return res;
    }

    public Thesaurus updateCode(KeywordBean bean, String newcode) throws AccessDeniedException, IOException {
        return updateCode(bean.getNameSpaceCode(), bean.getRelativeCode(), newcode);
    }

    /**
     * Update concept code by creating URI from namespace and code. This is recommended when
     * thesaurus concept identifiers contains # eg. http://vocab.nerc.ac.uk/collection/P07/current#CFV13N44
     */
    public synchronized Thesaurus updateCode(String namespace, String oldcode, String newcode) throws AccessDeniedException {
        Graph myGraph = repository.getGraph();

        ValueFactory myFactory = myGraph.getValueFactory();

        URI oldobj = myFactory.createURI(namespace, oldcode);
        URI newobj = myFactory.createURI(namespace, newcode);

        return updateElementCode(myGraph, oldobj, newobj);

    }

    /**
     * Update concept code using its URI. This is recommended when concept identifier may not be
     * based on thesaurus namespace and does not contains #.
     *
     * eg. http://vocab.nerc.ac.uk/collection/P07/current/CFV13N44/
     */
    public synchronized Thesaurus updateCodeByURI(String olduri, String newuri) throws AccessDeniedException {
        Graph myGraph = repository.getGraph();

        ValueFactory myFactory = myGraph.getValueFactory();

        URI oldobj = myFactory.createURI(olduri);
        URI newobj = myFactory.createURI(newuri);

        return updateElementCode(myGraph, oldobj, newobj);
    }

    private Thesaurus updateElementCode(Graph myGraph, URI oldobj, URI newobj) {
        StatementIterator iterStSubject = myGraph.getStatements(oldobj, null, null);
        while (iterStSubject.hasNext()) {
            AtomicReference<Statement> st = new AtomicReference<>(iterStSubject.next());
            myGraph.add(newobj, st.get().getPredicate(), st.get().getObject());
        }

        StatementIterator iterStObject = myGraph.getStatements(null, null, oldobj);
        while (iterStObject.hasNext()) {
            Statement st = iterStObject.next();
            myGraph.add(st.getSubject(), st.getPredicate(), newobj);
        }
        myGraph.remove(oldobj, null, null);
        myGraph.remove(null, null, oldobj);
        return this;
    }


    public void writeConceptScheme(String thesaurusTitle, String namespace) throws IOException, AccessDeniedException, GraphException {
        Graph myGraph = new org.openrdf.model.impl.GraphImpl();
        writeConceptScheme(myGraph, thesaurusTitle, null, null, null, null, null, namespace);
        repository.addGraph(myGraph);
    }

    /**
     * Set the information about a new thesaurus and save the graph to the repository.
     */

    public void createConceptScheme(String thesaurusTitle,
                                    Map<String, String> multilingualTitles,
                                    String thesaurusDescription,
                                    Map<String, String> multilingualDescriptions,
                                    String identifier,
                                    String type,
                                    String namespace) throws IOException, AccessDeniedException, GraphException {
        Graph myGraph = new org.openrdf.model.impl.GraphImpl();

        writeConceptScheme(myGraph,
            thesaurusTitle,
            multilingualTitles,
            thesaurusDescription,
            multilingualDescriptions,
            identifier,
            type,
            namespace);

        repository.addGraph(myGraph);
    }


    /**
     * Set the information about an existing thesaurus.
     */
    public void updateConceptScheme(String thesaurusTitle,
                                    Map<String, String> multilingualTitles,
                                    String thesaurusDescription,
                                    Map<String, String> multilingualDescriptions,
                                    String identifier,
                                    String type,
                                    String namespace) throws AccessDeniedException, GraphException {
        Graph myGraph = repository.getGraph();
        removeElement(getConceptSchemes().get(0));

        writeConceptScheme(myGraph,
            thesaurusTitle,
            multilingualTitles,
            thesaurusDescription,
            multilingualDescriptions,
            identifier,
            type,
            namespace);
    }

    public void writeConceptScheme(Graph myGraph, String thesaurusTitle,
                                   Map<String, String> multilingualTitles,
                                   String thesaurusDescription,
                                   Map<String, String> multilingualDescriptions,
                                   String identifier,
                                   String type,
                                   String namespace) throws GraphException {

        ValueFactory myFactory = myGraph.getValueFactory();

        URI mySubject = myFactory.createURI(namespace);
        URI skosClass = myFactory.createURI(SKOS_NAMESPACE, "ConceptScheme");
        URI rdfType = myFactory.createURI(org.openrdf.vocabulary.RDF.TYPE);
        mySubject.addProperty(rdfType, skosClass);

        URI titleURI = myFactory.createURI(DC_NAMESPACE, "title");

        boolean addTitleElement = true;
        if (multilingualTitles != null) {
            for (Entry<String, String> entrySet : multilingualTitles.entrySet()) {
                if (StringUtils.isNotEmpty(entrySet.getValue())) {
                    String language = toiso639_1_Lang(entrySet.getKey());
                    Value valueObj = myFactory.createLiteral(entrySet.getValue(), language);
                    myGraph.add(mySubject, titleURI, valueObj);

                    addTitleElement = false;
                }
            }
        }

        if (addTitleElement) {
            addElement("title", thesaurusTitle, myGraph, myFactory, mySubject);
        }


        boolean addDescriptionElement = true;
        URI descriptionURI = myFactory.createURI(DC_NAMESPACE, "description");

        if (multilingualDescriptions != null) {
            for (Entry<String, String> entrySet : multilingualDescriptions.entrySet()) {
                if (StringUtils.isNotEmpty(entrySet.getValue())) {
                    String language = toiso639_1_Lang(entrySet.getKey());
                    Value valueObj = myFactory.createLiteral(entrySet.getValue(), language);
                    myGraph.add(mySubject, descriptionURI, valueObj);

                    addDescriptionElement = false;
                }
            }
        }

        if (addDescriptionElement) {
            addElement("description", thesaurusDescription, myGraph, myFactory, mySubject);
        }

        addElement("identifier", identifier, myGraph, myFactory, mySubject);
        addElement("type", type, myGraph, myFactory, mySubject);
    }





    private void addElement(String name, String value, Graph myGraph, ValueFactory myFactory, URI mySubject) {
        if (StringUtils.isNotEmpty(value)) {
            URI uri = myFactory.createURI(DC_NAMESPACE, name);
            Value object = myFactory.createLiteral(value);
            myGraph.add(mySubject, uri, object);
        }
    }

    //   <skos:ConceptScheme rdf:about="http://www.thesaurus.gc.ca/#CoreSubjectThesaurus">
    //      <dc:title>main title</dc:title>
    //      <dc:title xml:lang="en">title en</dc:title>
    //      <dc:title xml:lang="fr">title fr</dc:title>
    //      <dc:publisher xml:lang="en">publisher en</dc:publisher>
    //      <dc:publisher xml:lang="fr">publisher fr</dc:publisher>
    //    <dc:description></dc:description>
    //  </skos:ConceptScheme>
    //
    // This will setup the dublinCoreMultilingual like so;
    //
    //dublinCoreMultilingual := {
    //     "en" ->  {
    //                  "title" -> "title en",
    //                  "publisher" -> "publisher en"
    //              }
    //     "fr" ->  {
    //                  "title" -> "title fr",
    //                  "publisher" -> "publisher fr"
    //              }
    //  }
    //
    // note - only looks at language-specified elements
    //
    private void retrieveDublinCore(Element thesaurusEl) {
        List<Namespace> theNSs = getThesaurusNamespaces();

        Namespace xmlNS = Namespace.getNamespace("xml","http://www.w3.org/XML/1998/namespace");
        try {
            List<Element> multiLingualTitles = (List<Element>) Xml.selectNodes(thesaurusEl,
                "skos:ConceptScheme/dc:*[@xml:lang]|skos:ConceptScheme/dcterms:*[@xml:lang]", theNSs);
            dublinCoreMultilingual.clear();
            for (Element el: multiLingualTitles) {
                String lang = isoLanguageMapper.iso639_2_to_iso639_1(el.getAttribute("lang", xmlNS).getValue());
                String value = el.getTextTrim();
                String name = el.getName();
                if (!dublinCoreMultilingual.containsKey(lang)) {
                    dublinCoreMultilingual.put(lang,new HashMap<>());
                }
                dublinCoreMultilingual.get(lang).put(name,value);
            }
        } catch (Exception e) {
            Log.warning(Geonet.THESAURUS,"error extracting multilingual dublin core items from thesaurus",e);
        }
    }

    //    <rdf:RDF>
    //    <skos:ConceptScheme rdf:about="http://www.thesaurus.gc.ca/#CoreSubjectThesaurus">
    //          <dc:title>Main GN Title</dc:title>
    //          <dc:title xml:lang="en">English Version (en)</dc:title>
    //          <dc:title xml:lang="fr">French Version (fr)</dc:title>
    //          <dc:description></dc:description>
    //     </skos:ConceptScheme>
    //
    // This will setup;
    //  {
    //      "en": "English Version (en)",
    //      "fr": "French Version (fr)"
    //  }
    private void retrieveMultiLingualTitles(Element thesaurusEl) {
        try {
            String xpathTitles = "skos:ConceptScheme/dc:title[@xml:lang]" +
                "|skos:ConceptScheme/dcterms:title[@xml:lang]" +
                "|skos:ConceptScheme/rdfs:label[@xml:lang]" +
                "|skos:ConceptScheme/skos:prefLabel[@xml:lang]" +
                "|rdf:Description[rdf:type/@rdf:resource = 'http://www.w3.org/2004/02/skos/core#ConceptScheme']/dc:title[@xml:lang]";
            multilingualTitles.clear();
            multilingualTitles.putAll(retrieveMultilingualField(thesaurusEl, xpathTitles));
        } catch (Exception e) {
            Log.warning(Geonet.THESAURUS,"error extracting multilingual titles from thesaurus",e);
        }
    }

    private void retrieveMultiLingualDescriptions(Element thesaurusEl) {
        try {
            String xpathDescriptions = "skos:ConceptScheme/dc:description[@xml:lang]|skos:ConceptScheme/dcterms:description[@xml:lang]|rdf:Description[rdf:type/@rdf:resource = 'http://www.w3.org/2004/02/skos/core#ConceptScheme']/dc:description[@xml:lang]";
            multilingualDescriptions.clear();
            multilingualDescriptions.putAll(retrieveMultilingualField(thesaurusEl, xpathDescriptions));
        } catch (Exception e) {
            Log.warning(Geonet.THESAURUS,"error extracting multilingual descriptions from thesaurus",e);
        }
    }

    private Map<String, String> retrieveMultilingualField(Element thesaurusEl, String xpath) throws JDOMException {
        List<Namespace> theNSs = getThesaurusNamespaces();

        Namespace xmlNS = Namespace.getNamespace("xml","http://www.w3.org/XML/1998/namespace");

        Map<String, String> multilingualValues = new HashMap<>();
        List<Element> multilingualValuesEl = (List<Element>) Xml.selectNodes(thesaurusEl,
            xpath, theNSs);
        for (Element el: multilingualValuesEl) {
            String lang = isoLanguageMapper.iso639_2_to_iso639_1(el.getAttribute("lang", xmlNS).getValue());
            String titleValue = el.getTextTrim();
            multilingualValues.put(lang, titleValue);
        }

        return multilingualValues;
    }

    /**
     * Retrieves the thesaurus information from rdf file.
     *
     * Used to set the thesaurusName and thesaurusDate for keywords.
     */
    private void retrieveThesaurusInformation(Path thesaurusFile, String defaultTitle, boolean ignoreMissingError) {
        if (!Files.exists(thesaurusFile)) return;

        // set defaults as in the case of a local thesaurus file, this info
        // may not be present yet
        this.title = defaultTitle;

        try {
            Element thesaurusEl = Xml.loadFile(thesaurusFile);

            List<Namespace> theNSs = getThesaurusNamespaces();

            this.defaultNamespace = null;
            retrieveMultiLingualTitles(thesaurusEl);
            retrieveDublinCore(thesaurusEl);

            Element titleEl = Xml.selectElement(thesaurusEl,
                "skos:ConceptScheme/dc:title|skos:ConceptScheme/dcterms:title" +
                    "|skos:ConceptScheme/rdfs:label|skos:ConceptScheme/skos:prefLabel" +
                    "|skos:Collection/dc:title|skos:Collection/dcterms:title" +
                    "|rdf:Description/dc:title|rdf:Description/dcterms:title", theNSs);

            if (titleEl != null) {
                this.title = titleEl.getValue();
                this.defaultNamespace = titleEl
                    .getParentElement()
                    .getAttributeValue("about", Namespace.getNamespace("rdf", RDF_NAMESPACE));
            } else {
                this.title = defaultTitle;
                this.defaultNamespace = DEFAULT_THESAURUS_NAMESPACE;
            }

            Element descriptionEl = Xml.selectElement(thesaurusEl,
                "skos:ConceptScheme/dc:description|skos:ConceptScheme/dcterms:description|" +
                    "skos:Collection/dc:description|skos:Collection/dcterms:description|" +
                    "rdf:Description/dc:description|rdf:Description/dcterms:description", theNSs);

            this.description = descriptionEl != null ? descriptionEl.getValue() : "";

            retrieveMultiLingualDescriptions(thesaurusEl);

            try {
                new java.net.URI(this.defaultNamespace);
            } catch (Exception e) {
                this.defaultNamespace = DEFAULT_THESAURUS_NAMESPACE;
            }

            Element issuedDateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:issued", theNSs);
            this.issuedDate = issuedDateEl==null? "": issuedDateEl.getText();

            Element modifiedDateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:modified", theNSs);
            this.modifiedDate = modifiedDateEl==null? "": modifiedDateEl.getText();

            Element createdDateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:created", theNSs);
            this.createdDate = createdDateEl==null? "": createdDateEl.getText();

            // Default date
            Element dateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:issued|skos:Collection/dc:date", theNSs);
            Date thesaususDate = parseThesaurusDate(dateEl);

            if (thesaususDate == null) {
                dateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:modified", theNSs);
                thesaususDate = parseThesaurusDate(dateEl);
            }

            if (thesaususDate != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                this.date = df.format(thesaususDate);
            }

            FileTime lastModifiedTime = null;
            if (this.date != null) {
                try {
                    lastModifiedTime = FileTime.fromMillis(DateUtil.parseBasicOrFullDateTime(this.date).toInstant().toEpochMilli());
                } catch (Exception e) {
                    Log.warning(Geonet.THESAURUS, "Unable to parse " + this.date + " into an actual java.util.Date object", e);
                }
            }

            if (lastModifiedTime == null) {
                try {
                    lastModifiedTime = Files.getLastModifiedTime(thesaurusFile);
                } catch (IOException e) {
                    lastModifiedTime = FileTime.fromMillis(new Date().getTime());
                }
                if (this.date == null) {
                    this.date = new ISODate(lastModifiedTime.toMillis(), true).toString();
                }
            }

            if (Log.isDebugEnabled(Geonet.THESAURUS_MAN)) {
                Log.debug(Geonet.THESAURUS_MAN, String.format(
                    "Thesaurus information: %s (%s)", this.title, this.date));
            }
        } catch (Exception ex) {
            if (!ignoreMissingError)
                Log.error(Geonet.THESAURUS_MAN, String.format(
                    "Error getting thesaurus info for %s. Error is: %s", thesaurusFile, ex.getMessage()));
        }
    }

    /**
     * Method to parse the thesaurus date value
     *
     * @param dateEl thesaurus date element
     * @return Date object representing the thesaurus date value
     */
    private Date parseThesaurusDate(Element dateEl) {
        Date thesaurusDate = null;

        if (dateEl == null) return thesaurusDate;

        String dateVal = dateEl.getText();

        // Try several date formats (date format seem not unified)
        List<SimpleDateFormat> dfList = new ArrayList<>();

        dfList.add(new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"));
        dfList.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        dfList.add(new SimpleDateFormat("yyyy-MM-dd"));
        dfList.add(new SimpleDateFormat("yyyy-MM"));
        dfList.add(new SimpleDateFormat("yyyy"));

        StringBuffer errorMsg = new StringBuffer("Error parsing the thesaurus date value: ");
        errorMsg.append(dateVal);

        for (SimpleDateFormat df : dfList) {
            try {
                thesaurusDate = df.parse(dateVal);
                return thesaurusDate;
            } catch (Exception ex) {
                // Ignore the exception and try next format
                errorMsg.append("\n  * with format: ");
                errorMsg.append(df.toPattern());
                errorMsg.append(". Error is: ");
                errorMsg.append(ex.getMessage());
            }
        }
        // Report error if no success
        errorMsg.append("\nCheck thesaurus date in ");
        errorMsg.append(this.fname);
        Log.error(Geonet.THESAURUS_MAN, errorMsg.toString());
        return thesaurusDate;
    }

    /**
     * finalize method shuts down the local sesame repository just before an unused Thesaurus object
     * is garbage collected - to save resources
     */
    protected void finalize() {
        if (repository != null) {
            repository.shutDown();
        }
    }

    public IsoLanguagesMapper getIsoLanguageMapper() {
        return isoLanguageMapper;
    }

    /**
     * Adds a relation between two keywords.  Both directions in relation will be added
     *
     * @param subject the keyword that is related to the other keyword
     * @param related the relation between the two keywords
     */
    public synchronized void addRelation(String subject, KeywordRelation related, String relatedSubject) throws AccessDeniedException {
        THESAURUS_SEARCH_CACHE.invalidateAll();

        Graph myGraph = repository.getGraph();

        // Set namespace skos and predicates
        ValueFactory myFactory = myGraph.getValueFactory();
        URI relationURI = myFactory.createURI(SKOS_NAMESPACE, related.name);
        URI opposteRelationURI = myFactory.createURI(SKOS_NAMESPACE, related.opposite().name);
        URI subjectURI = myFactory.createURI(subject);
        URI relatedSubjectURI = myFactory.createURI(relatedSubject);

        myGraph.add(subjectURI, relationURI, relatedSubjectURI);
        myGraph.add(relatedSubjectURI, opposteRelationURI, subjectURI);
    }

    /**
     * Gets a keyword using its id
     *
     * @param uri the keyword to retrieve
     * @return keyword
     */
    public KeywordBean getKeyword(String uri, String... languages) {
        String cacheKey = "getKeyword" + uri + String.join("", languages);
        Object cacheValue = THESAURUS_SEARCH_CACHE.getIfPresent(cacheKey);
        if (cacheValue != null) {
            return (KeywordBean) cacheValue;
        }

        List<KeywordBean> keywords;

        try {
            Query<KeywordBean> query = QueryBuilder
                .keywordQueryBuilder(getIsoLanguageMapper(), languages)
                .where(Wheres.ID(uri))
                .build();

            keywords = query.execute(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (keywords.isEmpty()) {
            throw new TermNotFoundException(getTermNotFoundMessage(uri));
        }

        KeywordBean keywordBean = keywords.get(0);
        THESAURUS_SEARCH_CACHE.put(cacheKey, keywordBean);
        return keywordBean;
    }

    /**
     * Gets top concepts/keywords from the thesaurus
     *
     * @return keywords
     */
    public List<KeywordBean> getTopConcepts(String... languages) {
        List<KeywordBean> keywords;

        try {
            Query<KeywordBean> query = QueryBuilder
                .keywordQueryBuilder(getIsoLanguageMapper(), languages)
                .select(Selectors.TOPCONCEPTS, true)
                .build();

            keywords = query.execute(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (keywords.isEmpty()) {
            throw new TermNotFoundException("No top concepts/keywords in file " + thesaurusFile);
        }

        return keywords;
    }

    private String getTermNotFoundMessage(String searchValue) {
        return "Could not find " + searchValue + " in file " + thesaurusFile;
    }

    /**
     * Thesaurus has keyword
     *
     * @param uri the keyword to check
     * @return boolean
     */
    public boolean hasKeyword(String uri) {
        try {
            getKeyword(uri);
        } catch (TermNotFoundException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets broader keywords
     *
     * @param uri the keyword whose broader terms should be retrieved
     * @return keywords
     */

    public List<KeywordBean> getBroader(String uri, String... languages) {
        return getRelated(uri, KeywordRelation.NARROWER, languages);
    }

    public List<KeywordBean> getNarrower(String uri, String... languages) {
        return getRelated(uri, KeywordRelation.BROADER, languages);
    }

    /**
     * Has broader keywords
     *
     * @param uri the keyword to check for broader terms
     * @return keywords
     */

    public boolean hasBroader(String uri) {
        return !getRelated(uri, KeywordRelation.NARROWER).isEmpty();
    }

    /**
     * Gets related keywords
     *
     * @param uri the keyword whose related terms should be retrieved
     * @return keyword
     */
    public List<KeywordBean> getRelated(String uri, KeywordRelation request, String... languages) {
        Query<KeywordBean> query = QueryBuilder
            .keywordQueryBuilder(getIsoLanguageMapper(), languages)
            .select(Selectors.related(uri, request), true)
            .build();

        try {
            return query.execute(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether there is a keyword for a label
     *
     * @param label    the preferred label of the keyword
     * @param langCode the language of the label
     * @return boolean
     */
    public boolean hasKeywordWithLabel(String label, String langCode) {
        try {
            getKeywordWithLabel(label, langCode);
        } catch (TermNotFoundException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets a keyword using its label.
     *
     * @param label    the preferred label of the keyword
     * @param langCode the language of the label
     * @return keyword
     */
    public KeywordBean getKeywordWithLabel(String label, String langCode) {
        Query<KeywordBean> query = QueryBuilder
            .keywordQueryBuilder(getIsoLanguageMapper(), langCode)
            .where(Wheres.prefLabel(langCode, label))
            .build();

        List<KeywordBean> matchingKeywords;

        try {
            matchingKeywords = query.execute(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (matchingKeywords.isEmpty()) {
            throw new TermNotFoundException(label);
        }

        return matchingKeywords.get(0);
    }

    public synchronized void clear() throws IOException, AccessDeniedException {
        AdminListener listener = new DummyAdminListener();
        repository.clear(listener);
    }

    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    public Map<String, String> getTitles(ApplicationContext context) throws JDOMException, IOException {
        return LangUtils.translate(context, getKey());
    }

    public List <String> getKeywordHierarchy(String keywordLabel, String langCode) {
        String cacheKey = "getKeywordHierarchy" + keywordLabel + langCode;
        Object cacheValue = THESAURUS_SEARCH_CACHE.getIfPresent(cacheKey);
        if (cacheValue != null) {
            return (List<String>) cacheValue;
        }
        boolean isUri = keywordLabel.startsWith("http");
        KeywordBean term =
            isUri
                ? this.getKeyword(keywordLabel, langCode)
                : this.getKeywordWithLabel(keywordLabel, langCode);

        List<ArrayList <KeywordBean>> result = this.classify(term, langCode);

        List <String> hierarchies = new ArrayList<>();
        for ( List <KeywordBean> hierachy : result) {
            String path = hierachy.stream()
                .map(k -> isUri ? k.getUriCode() : k.getPreferredLabel(langCode))
                .collect(Collectors.joining("^"));
            hierarchies.add(path);
        }
        THESAURUS_SEARCH_CACHE.put(cacheKey, hierarchies);
        return hierarchies;
    }

    public List<ArrayList <KeywordBean>> classify(KeywordBean term, String langCode) {

        List<ArrayList <KeywordBean>> result = new ArrayList<>();
        if (this.hasBroader(term.getUriCode())) {
            result.addAll(classifyTermWithBroaderTerms(term, langCode));
        } else {
            result.add(classifyTermWithNoBroaderTerms(term));
        }
        return result;
    }

    private List<ArrayList <KeywordBean>> classifyTermWithBroaderTerms(KeywordBean term, String langCode) {
        List<ArrayList <KeywordBean>> result = new ArrayList<>();
        for (ArrayList <KeywordBean> stringToBroaderTerm : classifyBroaderTerms(term, langCode)) {
            stringToBroaderTerm.add(term);
            result.add(stringToBroaderTerm);
        }
        return result;
    }

    private List<ArrayList <KeywordBean>> classifyBroaderTerms(KeywordBean term, String langCode) {
        List<ArrayList<KeywordBean>> result = new ArrayList<>();
        List<KeywordBean> narrowerList = this.getNarrower(term.getUriCode(), langCode);
        for (KeywordBean broaderTerm : this.getBroader(term.getUriCode(), langCode)) {
            // Avoid loop eg. http://www.eionet.europa.eu/gemet/concept/1462
            if (!narrowerList.contains(broaderTerm)) {
                result.addAll(this.classify(broaderTerm, langCode));
            }
        }
        return result;
    }

    private ArrayList <KeywordBean> classifyTermWithNoBroaderTerms(KeywordBean term) {
        ArrayList <KeywordBean> list = new ArrayList <>();
        list.add(term);
        return list;
    }


    private List<Namespace> getThesaurusNamespaces() {
        List<Namespace> theNSs = new ArrayList<>();
        theNSs.add(Namespace.getNamespace("rdf", RDF_NAMESPACE));
        theNSs.add(Namespace.getNamespace("rdfs", RDF_SCHEMA_NAMESPACE));
        theNSs.add(Namespace.getNamespace("skos", SKOS_NAMESPACE));
        theNSs.add(Namespace.getNamespace("dc", DC_NAMESPACE));
        theNSs.add(Namespace.getNamespace("dcterms", DCTERMS_NAMESPACE));

        return theNSs;
    }
}
