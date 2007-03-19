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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.GraphException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sesame.Sesame;
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
import org.openrdf.sesame.repository.local.LocalService;
import org.openrdf.sesame.sail.StatementIterator;

//=============================================================================

public class ThesaurusManagerSesame {

	private Hashtable<String, LocalRepository> repositoryTable = null;

	private Hashtable<String, Thesaurus> thesauriTable = null;

	private LocalService service = null;

	private String thesauriDirectory = null;

	/**
	 * 
	 * @param appPath
	 * @param thesauriRepository
	 * @throws Exception
	 */
	public ThesaurusManagerSesame(String appPath, String thesauriRepository)
			throws Exception {
		// Reccuperation de l'interface Sesame
		service = Sesame.getService();

		File thesauriDir = new File(thesauriRepository);

		if (!thesauriDir.isAbsolute())
			thesauriDir = new File(appPath + thesauriDir);

		thesauriDirectory = thesauriDir.getAbsolutePath();

		initThesauriTable(thesauriDir);
	}

	/**
	 * @param fname
	 * @param type
	 * @param dname
	 * @return
	 */
	public String buildThesaurusFilePath(String fname, String type, String dname) {
		return thesauriDirectory + File.separator + type + File.separator
				+ Geonet.CodeList.THESAURUS + File.separator + dname
				+ File.separator + fname;
	}

	/**
	 * 
	 * @param thesauriDirectory
	 */
	private void initThesauriTable(File thesauriDirectory) {

		repositoryTable = new Hashtable<String, LocalRepository>();
		thesauriTable = new Hashtable<String, Thesaurus>();

		if (thesauriDirectory.isDirectory()) {
			// init of external repositories
			File externalThesauriDirectory = new File(thesauriDirectory,
					Geonet.CodeList.EXTERNAL + File.separator
							+ Geonet.CodeList.THESAURUS);
			if (externalThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = externalThesauriDirectory.listFiles();
				for (int i = 0; i < rdfDataDirectory.length; i++) {
					if (rdfDataDirectory[i].isDirectory()) {
						loadRepositories(rdfDataDirectory[i],
								Geonet.CodeList.EXTERNAL);
					}
				}
			}

			// init of local repositoris
			File localThesauriDirectory = new File(thesauriDirectory,
					Geonet.CodeList.LOCAL + File.separator
							+ Geonet.CodeList.THESAURUS);
			if (localThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = localThesauriDirectory.listFiles();
				for (int i = 0; i < rdfDataDirectory.length; i++) {
					if (rdfDataDirectory[i].isDirectory()) {
						loadRepositories(rdfDataDirectory[i],
								Geonet.CodeList.LOCAL);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param thesauriDirectory
	 */
	private void loadRepositories(File thesauriDirectory, String root) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".rdf");
			}
		};

		String[] rdfDataFile = thesauriDirectory.list(filter);

		for (int i = 0; i < rdfDataFile.length; i++) {

			Thesaurus gst = new Thesaurus(rdfDataFile[i], root,
					thesauriDirectory.getName(), new File(thesauriDirectory,
							rdfDataFile[i]));
			try {
				addThesaurus(gst);
			} catch (Exception e) {
				e.printStackTrace();
				// continue loading
			}
		}
	}

	/**
	 * 
	 * @param gst
	 */
	public void addThesaurus(Thesaurus gst) throws Exception {

		String thesaurusName = gst.getKey();
		if (existsThesaurus(thesaurusName)) {
			throw new Exception("A thesaurus exists with code " + thesaurusName);
		}

		addConfiguredThesaurus(gst);
	}

	/**
	 * TODO TEST
	 * 
	 * @param gst
	 */
	private void addConfiguredThesaurus(Thesaurus gst) throws Exception {

		String thesaurusName = gst.getKey();
		if (existsThesaurus(thesaurusName)) {
			throw new Exception("A thesaurus exists with code " + thesaurusName);
		}

		// boolean inferencing = true;
		// boolean inferencing = false;

		LocalRepository thesaurusRepository;
		try {
			RepositoryConfig repConfig = new RepositoryConfig(gst.getKey());

			SailConfig syncSail = new SailConfig(
					"org.openrdf.sesame.sailimpl.sync.SyncRdfSchemaRepository");
			SailConfig memSail = new org.openrdf.sesame.sailimpl.memory.RdfSchemaRepositoryConfig(
					gst.getFile().getAbsolutePath(), RDFFormat.RDFXML);
			repConfig.addSail(syncSail);
			repConfig.addSail(memSail);
			repConfig.setWorldReadable(true);
			repConfig.setWorldWriteable(true);

			thesaurusRepository = service.createRepository(repConfig);

			// create thesaurus repository
			// thesaurusRepository = service.createRepository(thesaurusName,
			// inferencing);

			// populate thesaurus repository
			// String baseURI = "http://www.w3.org/2004/02/skos/core#";
			// boolean verifyData = false;
			// AdminListener myListener = new StdOutAdminListener();
			// thesaurusRepository.addData(gst.getFile(), baseURI,
			// RDFFormat.RDFXML, verifyData,
			// myListener);

			// put thesaurus in hashtable
			repositoryTable.put(thesaurusName, thesaurusRepository);
			thesauriTable.put(thesaurusName, gst);

		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw e;
		}
		// catch (IOException e) {
		// e.printStackTrace();
		// throw e;
		// } catch (AccessDeniedException e) {
		// e.printStackTrace();
		// throw e;
		// }
	}

	/**
	 * TODO IMPACT SUR LA SUPPRESSION DU REPOSITORY SESAME ?
	 * 
	 * @param name
	 */
	public void remove(String name) {
		thesauriTable.remove(name);
		repositoryTable.remove(name);
	}

	// =============================================================================
	// PUBLIC SERVICES

	public String getThesauriDirectory() {
		return thesauriDirectory;
	}

	public Hashtable<String, LocalRepository> getRepositoryTable() {
		return repositoryTable;
	}

	public Hashtable<String, Thesaurus> getThesauriTable() {
		return thesauriTable;
	}

	public LocalRepository getRepositoryByName(String thesaurusName) {
		return repositoryTable.get(thesaurusName);
	}

	public Thesaurus getThesaurusByName(String thesaurusName) {
		return thesauriTable.get(thesaurusName);
	}

	/**
	 * 
	 * @param query
	 * @param thesaurusRepository
	 * @return
	 * @throws IOException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws AccessDeniedException
	 */
	public QueryResultsTable performRequest(String query,
			LocalRepository thesaurusRepository) throws IOException,
			MalformedQueryException, QueryEvaluationException,
			AccessDeniedException {

		System.out.println("Requete : " + query);

		QueryResultsTable resultsTable = thesaurusRepository.performTableQuery(
				QueryLanguage.SERQL, query);

		printResultsTable(resultsTable);

		return resultsTable;
	}

	/**
	 * 
	 * @param thesaurusRepository
	 * @return
	 * @throws IOException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws AccessDeniedException
	 */
	public Element getAllPrefLabel(String thesaurusRepository)
			throws IOException, MalformedQueryException,
			QueryEvaluationException, AccessDeniedException {

		String query = "SELECT prefLab, note "
				+ " from {} rdf:type {skos:Concept}; "
				+ " skos:prefLabel {prefLab} [skos:scopeNote {note}] "
				+ " where lang(prefLab) like \"fr\""
				+ " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>";

		QueryResultsTable resultsTable = performRequest(query, repositoryTable
				.get(thesaurusRepository));

		printResultsTable(resultsTable);

		Element elDescKeys = resultsTableToXmlKeywords(resultsTable,
				thesaurusRepository);
		Element elThesaName = new Element("thesaName");
		Element elResTitle = new Element("resTitle");
		elResTitle.addContent(thesaurusRepository);
		elThesaName.addContent(elResTitle);
		elDescKeys.addContent(elThesaName);

		return elDescKeys;
	}

	/**
	 * 
	 * @param thesaurusRepository
	 * @param word
	 * @return
	 * @throws IOException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws AccessDeniedException
	 */
	public Element getPrefLabelBeginingWith(String thesaurusRepository,
			String word) throws IOException, MalformedQueryException,
			QueryEvaluationException, AccessDeniedException {

		String query = "SELECT prefLab, note "
				+ " from {} rdf:type {skos:Concept}; "
				+ " skos:prefLabel {prefLab} [skos:scopeNote {note}] "
				+ " where lang(prefLab) like \"fr\" and prefLab like \""
				+ word
				+ "*\" "
				+ " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>";

		QueryResultsTable resultsTable = performRequest(query, repositoryTable
				.get(thesaurusRepository));

		printResultsTable(resultsTable);

		Element elDescKeys = resultsTableToXmlKeywords(resultsTable,
				thesaurusRepository);
		Element elThesaName = new Element("thesaName");
		Element elResTitle = new Element("resTitle");
		elResTitle.addContent(thesaurusRepository);
		elThesaName.addContent(elResTitle);
		elDescKeys.addContent(elThesaName);

		return elDescKeys;
	}

	/**
	 * 
	 * @param thesaurusRepository
	 * @param word
	 * @return
	 * @throws IOException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws AccessDeniedException
	 */
	public Element getPrefLabelIncluding(String thesaurusRepository, String word)
			throws IOException, MalformedQueryException,
			QueryEvaluationException, AccessDeniedException {

		String query = "SELECT prefLab, note "
				+ " from {} rdf:type {skos:Concept}; "
				+ " skos:prefLabel {prefLab} [skos:scopeNote {note}] "
				+ " where lang(prefLab) like \"fr\" and prefLab like \"*"
				+ word
				+ "*\" "
				+ " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>";

		QueryResultsTable resultsTable = performRequest(query, repositoryTable
				.get(thesaurusRepository));

		printResultsTable(resultsTable);

		Element elDescKeys = resultsTableToXmlKeywords(resultsTable,
				thesaurusRepository);
		Element elThesaName = new Element("thesaName");
		Element elResTitle = new Element("resTitle");
		elResTitle.addContent(thesaurusRepository);
		elThesaName.addContent(elResTitle);
		elDescKeys.addContent(elThesaName);

		return elDescKeys;
	}

	/**
	 * 
	 * @param thesaurusRepository
	 * @param word
	 * @return
	 * @throws IOException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws AccessDeniedException
	 */
	public Element getPrefLabel(String thesaurusRepository, String word)
			throws IOException, MalformedQueryException,
			QueryEvaluationException, AccessDeniedException {

		String query = "SELECT prefLab, note "
				+ " from {} rdf:type {skos:Concept}; "
				+ " skos:prefLabel {prefLab} [skos:scopeNote {note}] "
				+ " where lang(prefLab) like \"fr\" and prefLab like \""
				+ word
				+ "\" "
				+ " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>";

		QueryResultsTable resultsTable = performRequest(query, repositoryTable
				.get(thesaurusRepository));

		printResultsTable(resultsTable);

		Element elDescKeys = resultsTableToXmlKeywords(resultsTable,
				thesaurusRepository);
		Element elThesaName = new Element("thesaName");
		Element elResTitle = new Element("resTitle");
		elResTitle.addContent(thesaurusRepository);
		elThesaName.addContent(elResTitle);
		elDescKeys.addContent(elThesaName);

		return elDescKeys;
	}

	/**
	 * 
	 * @param resultsTable
	 */
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

			System.out.println();
		}
		System.out.println("Fin");
	}

	/**
	 * 
	 * @param resultsTable
	 * @param thesaurusRepository
	 * @return
	 */
	private Element resultsTableToXmlKeywords(QueryResultsTable resultsTable,
			String thesaurusRepository) {

		Element elDescKeys = new Element("descKeys");

		int rowCount = resultsTable.getRowCount();

		for (int row = 0; row < rowCount; row++) {
			Element elKeyword = new Element("keyword");

			Value value = resultsTable.getValue(row, 0);
			String sValue = "";
			if (value != null) {
				sValue = value.toString();
			}
			Element elValue = new Element("value");
			elValue.addContent(sValue);

			Value definition = resultsTable.getValue(row, 1);
			String sDefinition = "";
			if (definition != null) {
				sDefinition = definition.toString();
			}
			Element elDefiniton = new Element("definiton");
			elDefiniton.addContent(sDefinition);

			elKeyword.addContent(elValue);
			elKeyword.addContent(elDefiniton);
			elDescKeys.addContent(elKeyword);
		}

		Element elNbResultsTot = new Element("nbresultstot");
		elNbResultsTot.addContent(Integer.toString(rowCount));
		elDescKeys.addContent(elNbResultsTot);

		return elDescKeys;
	}

	/**
	 * @param name
	 * @return
	 */
	public boolean existsThesaurus(String name) {
		return (thesauriTable.get(name) != null);
	}

	/**
	 * TODO ETAPE 3 VOIR SI UTILE par rapport aux updateElement, addElement,
	 * deleteElement ?
	 * 
	 * @param name
	 * @return
	 */
	public boolean updateThesaurus(String thesaurusName, Hashtable htChanges) {
		return false;
	}

	/**
	 * TODO TEST ETAPE 3
	 * 
	 * @param args
	 * @throws GraphException
	 * @throws Exception
	 */
	public boolean addElement(String thesaurusName, String prefLab,
			String altLab, String note) throws GraphException {
		LocalRepository lr = repositoryTable.get(thesaurusName);

		// Graph myGraph = myLocalRepository.getGraph();
		Graph myGraph = new org.openrdf.model.impl.GraphImpl();

		ValueFactory myFactory = myGraph.getValueFactory();

		// definition des namesspaces
		String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
		String namespaceGml = "http://www.opengis.net/gml#";
		// String namespace = "http://geonetwork-opensource.org/regions#";
		String namespace = "http://geosource.org/keyword#";

		// creation du sujet
		URI mySubject = myFactory.createURI(namespace, Long
				.toString((new Date()).getTime()));

		// creation des predicats
		URI skosClass = myFactory.createURI(namespaceSkos, "Concept");
		URI rdfType = myFactory.createURI(org.openrdf.vocabulary.RDF.TYPE);
		URI predicatePrefLabel = myFactory.createURI(namespaceSkos, "prefLabel");
		URI predicateScopeNote = myFactory.createURI(namespaceSkos, "scopeNote");
		
		// pour le sous graphe de l'enveloppe gml
		URI predicateBoundedBy = myFactory.createURI(namespaceGml, "BoundedBy");
		URI predicateEnvelope = myFactory.createURI(namespaceGml, "Envelope");	
		URI predicateSrsName = myFactory.createURI(namespaceGml, "srsName");
		URI srsNameURI = myFactory.createURI("http://www.opengis.net/gml/srs/epsg.xml#epsg:4326");
		BNode gmlNode = myFactory.createBNode();
		URI predicateLowerCorner = myFactory.createURI(namespaceGml, "lowerCorner");
		URI predicateUpperCorner = myFactory.createURI(namespaceGml, "upperCorner");
		
		// creation des objets
		Literal myObject1 = myFactory.createLiteral(prefLab, "fr");
		Literal myObject2 = myFactory.createLiteral(note);
		
		Literal lowerCorner = myFactory.createLiteral("12.23 12.56");		
		Literal upperCorner = myFactory.createLiteral("15.12 16.0");
		
		//preparation du graph
		mySubject.addProperty(rdfType, skosClass); 
		myGraph.add(mySubject, predicatePrefLabel, myObject1);
		myGraph.add(mySubject, predicateScopeNote, myObject2);
		myGraph.add(mySubject, predicateBoundedBy, gmlNode);
		//graph gml		
		gmlNode.addProperty(rdfType, predicateEnvelope);
		myGraph.add(gmlNode, predicateLowerCorner, lowerCorner);
		myGraph.add(gmlNode, predicateUpperCorner, upperCorner);
		myGraph.add(gmlNode, predicateSrsName, srsNameURI);
		
		try {
			lr.addGraph(myGraph);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * TODO ETAPE 3
	 * 
	 * @param args
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 * @throws AccessDeniedException
	 * @throws Exception
	 */
	public boolean updateElement(String thesaurusName, String prefLab,
			String altLab, String note) throws MalformedQueryException,
			QueryEvaluationException, AccessDeniedException {
		// Recuperation du graph du thesaurus
		LocalRepository lr = repositoryTable.get(thesaurusName);
		Graph myGraph = lr.getGraph();

		ValueFactory myFactory = myGraph.getValueFactory();
		String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";

		// Recuperation du sujet (URI du mot clé)
//		URI subject = myFactory
//				.createURI("http://geonetwork-opensource.org/regions#249");
		URI subject = myFactory
		.createURI("http://geosource.org/keyword#1165509663312");
		
		// URI mySubject = myFactory.createURI(id);

		// Preparation des nouveaux statements
		URI predicatePrefLabel = myFactory
				.createURI(namespaceSkos, "prefLabel");
		Literal myObject1 = myFactory.createLiteral(prefLab, "fr");

		URI predicateScopeNote = myFactory
				.createURI(namespaceSkos, "scopeNote");
		Literal myObject2 = myFactory.createLiteral(note,"fr");

		// Suppression des anciens statements
		// prefLabel
		 StatementIterator iter = myGraph.getStatements(subject,
		 predicateScopeNote, null);
		while (iter.hasNext()) {
			Statement st = (Statement) iter.next();
			if (st.getObject() instanceof Literal) {
				Literal litt = (Literal) st.getObject();
				System.out.println(st.getSubject().toString() + " : "
						+ st.getPredicate().getLocalName() + " : "
						+ st.getObject().toString() + " : "
						/*+ litt.getLanguage()*/);
				if (litt.getLanguage()!=null && litt.getLanguage().equals("fr")) {
					System.out.println("a supprimer");
					//myGraph.remove(st);
					//break;
				}
			}
		}
		// iter = myGraph.getStatements(subject, predicatePrefLabel,
		// myFactory.createLiteral("*","fr"));
		// while (iter.hasNext()) {
		// Statement st = (Statement) iter.next();
		// Literal litt = (Literal) st.getObject();
		// System.out.println(st.getSubject().toString() + " : "
		// + st.getPredicate().getLocalName() + " : "
		// + st.getObject().toString() + " : " + litt.getLanguage());
		// if (litt.getLanguage().equals("fr")){
		// myGraph.remove(st);
		// break;
		// }
		// }

		// ajout des nouveaux statements
		// myGraph.add(subject, predicatePrefLabel, myObject1);
		// myGraph.add(subject, predicateScopeNote, myObject2);

		System.out.println("fin!");
		return false;
	}

	/**
	 * TODO TEST ETAPE 3
	 * 
	 * @param args
	 * @throws GraphException
	 * @throws Exception
	 */
	public boolean deleteElement(String thesaurusName, String prefLab,
			String altLab, String note) throws GraphException {
		LocalRepository lr = repositoryTable.get(thesaurusName);

		Graph myGraph = new org.openrdf.model.impl.GraphImpl();

		ValueFactory myFactory = myGraph.getValueFactory();
		String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
		// String namespace = "http://geonetwork-opensource.org/regions#";
		String namespace = "http://geosource.org/keyword#";

		URI mySubject = myFactory.createURI(namespace, Long
				.toString((new Date()).getTime()));

		URI skosClass = myFactory.createURI(namespaceSkos, "Concept");
		URI rdfType = myFactory.createURI(org.openrdf.vocabulary.RDF.TYPE);
		mySubject.addProperty(rdfType, skosClass); // equivalent à :
													// myGraph.add(mySubject,
													// rdfType, skosClass);

		URI myPredicate1 = myFactory.createURI(namespaceSkos, "prefLabel");
		Literal myObject1 = myFactory.createLiteral(prefLab, "fr");
		myGraph.add(mySubject, myPredicate1, myObject1);

		URI myPredicate2 = myFactory.createURI(namespaceSkos, "scopeNote");
		Literal myObject2 = myFactory.createLiteral(note);
		myGraph.add(mySubject, myPredicate2, myObject2);

		// Graph myGraph = new org.openrdf.model.impl.GraphImpl();
		//
		// ValueFactory myFactory = myGraph.getValueFactory();
		// String namespace = "http://www.w3.org/2004/02/skos/core#";
		//
		// org.openrdf.model.URI mySubject = myFactory.createURI(namespace,
		// "Concept");
		//		
		// org.openrdf.model.URI myPredicate1 = myFactory.createURI(namespace,
		// "prefLabel");
		// Literal myObject1 = myFactory.createLiteral(prefLab);
		// myGraph.add(mySubject, myPredicate1, myObject1);
		//		
		// org.openrdf.model.URI myPredicate2 = myFactory.createURI(namespace,
		// "scopeNote");
		// Literal myObject2 = myFactory.createLiteral(note);
		// myGraph.add(mySubject, myPredicate2, myObject2);

		try {
			lr.removeGraph(myGraph);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// =============================================================================

	public static void main(String[] args) throws Exception {
		ThesaurusManagerSesame tm = new ThesaurusManagerSesame("",
				"E:\\workspace3.2\\TestSesame\\res\\codelist\\");
//		 tm.addElement("local.place.regions", "monPays", "le pays de toto", "le pays de toto");
		// tm.deleteElement("local.place.regions", "monPays", "le pays", "voila
		// un pays");
//		tm.updateElement("local.place.regions", "Zimbabwe", "le pays",
//				"voila un pays");
		// tm.getAllPrefLabel("local.place.regions");
		File rdf = new File("E:\\workspace3.2\\TestSesame\\res\\codelist\\local\\thesauri\\place\\toto.rdf");
		Thesaurus thesaurus = new Thesaurus("toto.rdf","local","place",rdf);
		tm.addThesaurus(thesaurus);
		
		tm.getAllPrefLabel("local.place.toto");
		
		tm.addElement("local.place.toto", "monPays", "le pays de toto", "le pays de toto");
		tm.getAllPrefLabel("local.place.toto");		
		System.out.println("fin!!");
	}

}
