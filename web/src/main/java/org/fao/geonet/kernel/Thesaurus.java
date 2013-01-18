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
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;
import org.jdom.Namespace;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.GraphException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.sesame.admin.DummyAdminListener;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.sail.StatementIterator;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class Thesaurus {
	private static final String DEFAULT_THESAURUS_NAMESPACE = "http://custom.shared.obj.ch/concept#";

	private String fname;

	private String type;

	private String dname;

	private File thesaurusFile;

	private LocalRepository repository;

    private String title;

    private String date;
    
    private String defaultNamespace;

	private String version;

    private String downloadUrl;

    private String keywordUrl;

	@SuppressWarnings("unused")
	private String name;

	@SuppressWarnings("unused")
	private String description;

	@SuppressWarnings("unused")
	private String source;

	@SuppressWarnings("unused")
	private String langue;

	@SuppressWarnings("unused")
	private String autority;

	private IsoLanguagesMapper isoLanguageMapper;

	/**
	 * @param fname
	 *            file name
	 * @param type
	 * @param dname category/domain name of thesaurus
	 */
	public Thesaurus(IsoLanguagesMapper mapper, String fname, String type, String dname, File thesaurusFile, String siteUrl) {
	    this(mapper, fname, type, dname, thesaurusFile, siteUrl, false);
	}
    public Thesaurus(IsoLanguagesMapper mapper, String fname, String type, String dname, File thesaurusFile, String siteUrl, boolean ignoreMissingError) {
		super();
		this.isoLanguageMapper = mapper;
		this.fname = fname;
		this.type = type;
		this.dname = dname;
		this.thesaurusFile = thesaurusFile; 
		this.downloadUrl = buildDownloadUrl(fname, type, dname, siteUrl);
		this.keywordUrl = buildKeywordUrl(fname, type, dname, siteUrl);
		
        retrieveThesaurusTitle(thesaurusFile, dname + "." + fname, ignoreMissingError);
	}

	
	public Thesaurus(String fname, String type, String dname, File thesaurusFile, String siteUrl) {
		this(null, fname, type, dname, thesaurusFile, siteUrl);
	}
	
    /**
	 * 
	 * @return Thesaurus identifier
	 */
	public String getKey() {
		return buildThesaurusKey(fname, type, dname);
	}
	/**
	 * Get the Domain/category name of the thesaurus 
	 * @return
	 */
	public String getDname() {
		return dname;
	}

	public String getFname() {
		return fname;
	}

	public File getFile() {
		return thesaurusFile;
	}

	public String getType() {
		return type;
	}

    public String getTitle() {
		return title;
	}

    public String getDate() {
		return date;
	}

  public String getDownloadUrl() {
		return downloadUrl;
	}

  public String getKeywordUrl() {
		return keywordUrl;
	}

  public void retrieveThesaurusTitle() {
    retrieveThesaurusTitle(thesaurusFile, dname + "." + fname, false);
  }


	/**
	 * 
	 * @param fname
	 * @param type
	 * @param dname
	 * @return
	 */
	public static String buildThesaurusKey(String fname, String type, String dname) {
		return type + "." + dname + "." + fname.substring(0, fname.indexOf(".rdf"));
	}

	private String buildDownloadUrl(String fname, String type, String dname, String siteUrl) {
		if (type.equals(Geonet.CodeList.REGISTER)) {
			return siteUrl + "/?uuid="+fname.substring(0, fname.indexOf(".rdf"));
		} else {
			return siteUrl + "/thesaurus.download?ref="+Thesaurus.buildThesaurusKey(fname, type, dname);
		}
	}

	private String buildKeywordUrl(String fname, String type, String dname, String siteUrl) {
		return siteUrl + "/xml.keyword.get?thesaurus="+Thesaurus.buildThesaurusKey(fname, type, dname) + "&amp;id="; 
		// needs to have term/concept id tacked onto the end
	}

	public synchronized LocalRepository getRepository() {
		return repository;
	}

	public synchronized Thesaurus setRepository(LocalRepository repository) {
		this.repository = repository;
		return this;
	}

    /**
     * TODO javadoc.
     *
     * @param query
     * @return
     * @throws IOException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws AccessDeniedException
     */
	public synchronized QueryResultsTable performRequest(String query) throws IOException, MalformedQueryException,
            QueryEvaluationException, AccessDeniedException {
        if(Log.isDebugEnabled(Geonet.THESAURUS))
            Log.debug(Geonet.THESAURUS, "Query : " + query);

        //printResultsTable(resultsTable);
		return repository.performTableQuery(QueryLanguage.SERQL, query);
	}

	/**
	 * 
	 * @param resultsTable
	 */
    @SuppressWarnings("unused")    
	private void printResultsTable(QueryResultsTable resultsTable) {
		int rowCount = resultsTable.getRowCount();
		int columnCount = resultsTable.getColumnCount();

		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				Value value = resultsTable.getValue(row, column);

				if (value != null) {
					System.out.print(value.toString());
				} else {
					System.out.print("null");
				}
				System.out.print("\t");
			}
		}
	}

	/**
	 * Add a keyword to the Thesaurus.
	 * 
	 * @param keyword The keyword to add
	 * 
	 * @throws IOException
	 * @throws AccessDeniedException
	 * @throws GraphException
	 */
    public synchronized URI addElement(KeywordBean keyword) throws IOException, AccessDeniedException, GraphException {
        Graph myGraph = new org.openrdf.model.impl.GraphImpl();

        ValueFactory myFactory = myGraph.getValueFactory();

        // Define namespace
        String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
        String namespaceGml = "http://www.opengis.net/gml#";
        String namespace = keyword.getNameSpaceCode();

        if(namespace.equals("#")) {
        	namespace = this.defaultNamespace;
        }
        
        // Create subject
        URI mySubject = myFactory.createURI(namespace, keyword.getRelativeCode());

        URI skosClass = myFactory.createURI(namespaceSkos, "Concept");
        URI rdfType = myFactory.createURI(org.openrdf.vocabulary.RDF.TYPE);
        URI predicatePrefLabel = myFactory
                .createURI(namespaceSkos, "prefLabel");
        URI predicateScopeNote = myFactory
                .createURI(namespaceSkos, "scopeNote");

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
            myGraph.add(mySubject, predicatePrefLabel, valueObj );
            
        }
        Set<Entry<String, String>> definitions = keyword.getDefinitions().entrySet();
        for (Entry<String, String> entry : definitions) {
            String language = toiso639_1_Lang(entry.getKey());
            Value definitionObj = myFactory.createLiteral(entry.getValue(), language);
            myGraph.add(mySubject, predicateScopeNote, definitionObj );
            
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
     * 
     * @param keyword
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws IOException
     * @throws AccessDeniedException
     */
    public synchronized Thesaurus removeElement(KeywordBean keyword) throws MalformedQueryException,
            QueryEvaluationException, IOException, AccessDeniedException {
        String namespace = keyword.getNameSpaceCode();
        String code = keyword.getRelativeCode();

        return removeElement(namespace, code);
    }

    /**
     * Remove keyword from thesaurus.
     * 
     * @param namespace
     * @param code
     * @throws AccessDeniedException
     */
    public synchronized Thesaurus removeElement(String namespace, String code) throws AccessDeniedException {
        Graph myGraph = repository.getGraph();
        ValueFactory myFactory = myGraph.getValueFactory();
        URI subject = myFactory.createURI(namespace, code);
        StatementIterator iter = myGraph.getStatements(subject, null, null);
        while (iter.hasNext()) {
            AtomicReference<Statement> st = new AtomicReference<Statement>(iter.next());
            if (st.get().getObject() instanceof BNode) {
                BNode node = (BNode) st.get().getObject();
                repository.getGraph().remove(node, null, null);
            }
        }
        int removedItems = myGraph.remove(subject, null, null);
        if(Log.isDebugEnabled(Geonet.THESAURUS)) {
        	String msg = "Removed "+removedItems+" elements from thesaurus "+this.title+" with uri: "+subject;
        	Log.debug(Geonet.THESAURUS, msg);
        }
        return this;
    }

    private String toiso639_1_Lang(String lang) {
         String defaultCode = getIsoLanguageMapper().iso639_2_to_iso639_1(Geonet.DEFAULT_LANGUAGE, Geonet.DEFAULT_LANGUAGE.substring(2));
        return getIsoLanguageMapper().iso639_2_to_iso639_1(lang, defaultCode);
     }

	/**
	 * Update the keyword.  If replaceLanguages is true all of the old data will be removed and the new data in the keyword will replace it.
	 * 
	 * @param keyword the bean with the updated information
	 * @param replace If true all of the old data will be removed and the new data in the keyword will replace it.  If false only the data in the
	 * keyword will be updated.  This means only notes and labels that are in the keyword will be updated (for those languages) and the coordinates
	 * will only be updated if they are non-empty strings.
	 * @return 
	 * 
	 * @throws AccessDeniedException
	 * @throws IOException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws GraphException
	 */
	public synchronized URI updateElement(KeywordBean keyword, boolean replace) throws AccessDeniedException, IOException,
	        MalformedQueryException, QueryEvaluationException, GraphException {
	    
        // Get thesaurus graph
        Graph myGraph = repository.getGraph();      
        
        // Set namespace skos and predicates 
        ValueFactory myFactory = myGraph.getValueFactory();
        String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
        URI predicatePrefLabel = myFactory .createURI(namespaceSkos, "prefLabel");
        URI predicateScopeNote = myFactory.createURI(namespaceSkos, "scopeNote");

        // Get subject (URI)
        URI subject = myFactory.createURI(keyword.getNameSpaceCode(),keyword.getRelativeCode());

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
            myGraph.add(subject, predicatePrefLabel, valueObj );
            
        }
        // add updated Definitions/Notes
        Set<Entry<String, String>> definitions = keyword.getDefinitions().entrySet();
        for (Entry<String, String> entry : definitions) {
            String language = toiso639_1_Lang(entry.getKey());
            Value definitionObj = myFactory.createLiteral(entry.getValue(), language);
            myGraph.add(subject, predicateScopeNote, definitionObj );
            
        }

	    // update bbox
        if(replace || !(keyword.getCoordEast() + keyword.getCoordNorth() + keyword.getCoordWest() + keyword.getCoordSouth()).trim().isEmpty()) {
    	    String namespaceGml = "http://www.opengis.net/gml#";
    	    URI predicateBoundedBy = myFactory.createURI(namespaceGml, "BoundedBy");
    	    URI predicateLowerCorner = myFactory.createURI(namespaceGml, "lowerCorner");
    	    URI predicateUpperCorner = myFactory.createURI(namespaceGml, "upperCorner");
    	    
    	    BNode subjectGml = null;
    	    iter = myGraph.getStatements(subject, predicateBoundedBy, null);
    	    while (iter.hasNext()) {
    	        AtomicReference<Statement> st = new AtomicReference<Statement>(iter.next());
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
    	            AtomicReference<Statement> st = new AtomicReference<Statement>(iter.next());
    	            myGraph.remove(st.get());
    	            break;
    	        }
    	        // upperCorner
    	        iter = myGraph.getStatements(subjectGml, predicateUpperCorner, null);
    	        while (true) {
    	            if (!(iter.hasNext())) {
    	                break;
    	            }
    	            AtomicReference<Statement> st = new AtomicReference<Statement>(iter.next());
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
            ArrayList<Statement> toRemove = new ArrayList<Statement>();
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
     * TODO javadoc.
     *
     * @param namespace
     * @param code
     * @return
     * @throws AccessDeniedException
     */
	public synchronized boolean isFreeCode(String namespace, String code) throws AccessDeniedException {
		boolean res = true;				
		Graph myGraph = repository.getGraph();
		ValueFactory myFactory = myGraph.getValueFactory();		
		URI obj = myFactory.createURI(namespace,code);
		Collection<?> statementsCollection = myGraph.getStatementCollection(obj,null,null);
		if (statementsCollection!=null && statementsCollection.size()>0){
			res = false;
		}
		statementsCollection = myGraph.getStatementCollection(null,null,obj);
		if (statementsCollection!=null && statementsCollection.size()>0){
			res = false;
		}				
		return res;
	}
    public Thesaurus updateCode(KeywordBean bean, String newcode) throws AccessDeniedException, IOException {
        return updateCode(bean.getNameSpaceCode(), bean.getRelativeCode(), newcode);
    }

    /**
     * TODO javadoc.
     *
     * @param namespace
     * @param oldcode
     * @param newcode
     * @throws AccessDeniedException
     * @throws IOException
     */
	public synchronized Thesaurus updateCode(String namespace, String oldcode, String newcode) throws AccessDeniedException, IOException {
		Graph myGraph = repository.getGraph();
		
		ValueFactory myFactory = myGraph.getValueFactory();
		
		URI oldobj = myFactory.createURI(namespace,oldcode);
		URI newobj = myFactory.createURI(namespace,newcode);
		StatementIterator iterStSubject = myGraph.getStatements(oldobj,null,null);
		while(iterStSubject.hasNext()){
			AtomicReference<Statement> st = new AtomicReference<Statement>(iterStSubject.next());
			myGraph.add(newobj, st.get().getPredicate(), st.get().getObject());
		}		
		
		StatementIterator iterStObject = myGraph.getStatements(null,null,oldobj);
		while(iterStObject.hasNext()){
			Statement st = iterStObject.next();
			myGraph.add(st.getSubject(), st.getPredicate(), newobj);
		}
		myGraph.remove(oldobj,null,null);
		myGraph.remove(null,null,oldobj);
		return this;
	}

    /**
     * Retrieves the thesaurus title from rdf file.
     *
     * Used to set the thesaurusName and thesaurusDate for keywords
     * @param ignoreMissingError 
     *
     */
    private void retrieveThesaurusTitle(File thesaurusFile, String defaultTitle, boolean ignoreMissingError) {
				// set defaults as in the case of a local thesaurus file, this info
				// may not be present yet
				this.title = defaultTitle;
        this.date = new ISODate().toString();
				this.version = "unknown"; // not really acceptable!

        try {
            Element thesaurusEl = Xml.loadFile(thesaurusFile);

            List<Namespace> theNSs = new ArrayList<Namespace>();
            Namespace rdfNamespace = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			theNSs.add(rdfNamespace);
            theNSs.add(Namespace.getNamespace("skos", "http://www.w3.org/2004/02/skos/core#"));
            theNSs.add(Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/"));
            theNSs.add(Namespace.getNamespace("dcterms", "http://purl.org/dc/terms/"));

            this.defaultNamespace = null;
            Element title = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dc:title", theNSs);
            if (title != null) {
                this.title = title.getValue();
                this.defaultNamespace = title.getParentElement().getAttributeValue("about", rdfNamespace);
            } else {
                this.title = defaultTitle;
            }
            
            try {
            	new java.net.URI(this.defaultNamespace);
            } catch (Exception e) {
            	this.defaultNamespace = DEFAULT_THESAURUS_NAMESPACE;
            }
            
            if(!this.defaultNamespace.endsWith("#")) {
            	this.defaultNamespace += "#";
            }

            Element dateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:issued", theNSs);

            Date thesaususDate = parseThesaurusDate(dateEl);

            if (thesaususDate == null) {
                dateEl = Xml.selectElement(thesaurusEl, "skos:ConceptScheme/dcterms:modified", theNSs);
                thesaususDate = parseThesaurusDate(dateEl);
            }

            if (thesaususDate != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                this.date = df.format(thesaususDate);
            }

        } catch (Exception ex) {
            if(!ignoreMissingError)
                Log.error(Geonet.THESAURUS_MAN, "Error getting thesaurus info: " + ex.getMessage());
        }
    }

    /**
     * Method to parse the thesaurus date value
     *
     * @param dateEl   thesaurus date element
     * @return  Date object representing the thesaurus date value
     */
    private Date parseThesaurusDate(Element dateEl) {
        Date thesaurusDate = null;

        if (dateEl == null) return thesaurusDate;

        String dateVal = dateEl.getText();

        // Try several date formats (date format seem not unified)
        List<SimpleDateFormat> dfList = new ArrayList<SimpleDateFormat>();

        dfList.add(new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"));
        dfList.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        dfList.add(new SimpleDateFormat("yyyy-MM-dd"));

        StringBuffer errorMsg = new StringBuffer("Error parsing the thesaurus date value: ");
        errorMsg.append(dateVal);
        boolean success = false;
        
        for(SimpleDateFormat df: dfList) {
            try {
                thesaurusDate = df.parse(dateVal);
                success = true;
            } catch(Exception ex) {
                // Ignore the exception and try next format
                errorMsg.append("\n  * with format: ");
                errorMsg.append(df.toPattern());
                errorMsg.append(". Error is: ");
                errorMsg.append(ex.getMessage());
            }
        }
        // Report error if no success
        if (!success) {
            errorMsg.append("\nCheck thesaurus date in ");
            errorMsg.append(this.fname);
            Log.error(Geonet.THESAURUS_MAN, errorMsg.toString());
        }
        return thesaurusDate;
    }

		/**
		 * finalize method shuts down the local sesame repository just before an 
		 * unused Thesaurus object is garbage collected - to save resources 
		 *
	   */
		protected void finalize() {
			if (repository != null) {
				repository.shutDown();
			}
		}

        public IsoLanguagesMapper getIsoLanguageMapper() {
            return isoLanguageMapper == null? IsoLanguagesMapper.getInstance() : isoLanguageMapper;
        }

        /**
         * Adds a relation between two keywords.  Both directions in relation will be added   
         * 
         * @param subject the keyword that is related to the other keyword
         * @param related the relation between the two keywords
         * @param relatedSubject
         */
        public synchronized void addRelation(String subject, KeywordRelation related, String relatedSubject) throws AccessDeniedException, IOException,
        MalformedQueryException, QueryEvaluationException, GraphException {
            
            Graph myGraph = repository.getGraph();      
            
             // Set namespace skos and predicates 
             ValueFactory myFactory = myGraph.getValueFactory();
             String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
             URI relationURI = myFactory .createURI(namespaceSkos, related.name);
             URI opposteRelationURI = myFactory .createURI(namespaceSkos, related.opposite().name);
             URI subjectURI = myFactory.createURI(subject);
             URI relatedSubjectURI = myFactory.createURI(relatedSubject);

             myGraph.add(subjectURI, relationURI, relatedSubjectURI);
             myGraph.add(relatedSubjectURI, opposteRelationURI, subjectURI);
        }

        // ------------------------------- Deprecated methods -----------------------------
        /**
         * @deprecated since 2.9.0.  Use {@link #add(KeywordBean)}
         */
        URI addElement(String code, String prefLab, String note, String lang) throws GraphException, IOException,
                AccessDeniedException {

            KeywordBean bean = new KeywordBean(getIsoLanguageMapper())
                .setUriCode(code)
                .setValue(prefLab, lang)
                .setDefinition(note, lang);
            
            return addElement(bean);
        }

        /**
         * @deprecated since 2.9.0 use {@link #add(KeywordBean)}
         */
        URI addElement(String code, String prefLab, String note, String east, String west, String south,
                               String north, String lang) throws IOException, AccessDeniedException, GraphException {
            
            return addElement(new KeywordBean(getIsoLanguageMapper())
                        .setUriCode(code)
                        .setValue(prefLab, lang)
                        .setDefinition(note, lang)
                        .setCoordEast(east)
                        .setCoordNorth(north)
                        .setCoordSouth(south)
                        .setCoordWest(west));
        }


        /**
         * @deprecated since 2.9.0 use {@link #updateElement(KeywordBean)}
         */
        URI updateElement(String namespace, String id, String prefLab, String note, String lang) throws IOException,
                MalformedQueryException, QueryEvaluationException, AccessDeniedException, GraphException {
            KeywordBean keyword = new KeywordBean(getIsoLanguageMapper())
                        .setNamespaceCode(namespace)
                        .setRelativeCode(id)
                        .setValue(prefLab, lang)
                        .setDefinition(note, lang);
            return updateElement(keyword, false);
        }
        /**
         * @deprecated Since 2.9.0 use {@link #updateElement(KeywordBean)}
         */
        URI updateElement(String namespace, String id, String prefLab, String note, String east, String west,
                                  String south, String north, String lang) throws AccessDeniedException, IOException,
                MalformedQueryException, QueryEvaluationException, GraphException {

            KeywordBean bean = new KeywordBean(getIsoLanguageMapper())
                .setNamespaceCode(namespace)
                .setRelativeCode(id)
                .setValue(prefLab, lang)
                .setDefinition(note, lang)
                .setCoordEast(east)
                .setCoordNorth(north)
                .setCoordSouth(south)
                .setCoordWest(west);
            
            return updateElement(bean, true);
        }
		public synchronized void clear() throws IOException, AccessDeniedException {
			AdminListener listener = new DummyAdminListener();
			repository.clear(listener);
		}
        public String getDefaultNamespace() {
            return this.defaultNamespace;
        }

}
