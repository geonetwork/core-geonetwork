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
import java.io.IOException;
import java.util.Collection;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.GraphException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.sail.StatementIterator;

public class Thesaurus {
	private String fname;

	private String type;

	private String dname;

	private File thesaurusFile;

	private LocalRepository repository;

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

	/**
	 * @param fname
	 *            file name
	 * @param type
	 * @param dname
	 */
	public Thesaurus(String fname, String type, String dname,
			File thesaurusFile) {
		super();
		this.fname = fname;
		this.type = type;
		this.dname = dname;
		this.thesaurusFile = thesaurusFile; 
		
	}

	/**
	 * 
	 * @return Thesaurus identifier
	 */
	public String getKey() {
		return buildThesaurusKey(fname, type, dname);
	}

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

	/**
	 * 
	 * @param fname
	 * @param type
	 * @param dname
	 * @return
	 */
	public static String buildThesaurusKey(String fname, String type,
			String dname) {
		return type + "." + dname + "."
				+ fname.substring(0, fname.indexOf(".rdf"));
	}

	public LocalRepository getRepository() {
		return repository;
	}

	public void setRepository(LocalRepository repository) {
		this.repository = repository;
	}

	public QueryResultsTable performRequest(String query) throws IOException,
			MalformedQueryException, QueryEvaluationException,
			AccessDeniedException {
		System.out.println("Query : " + query);
		QueryResultsTable resultsTable = repository.performTableQuery(
				QueryLanguage.SERQL, query);
		//printResultsTable(resultsTable);
		return resultsTable;
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
	}

	public URI addElement(String code, String prefLab, String note, String lang)
			throws GraphException, IOException, AccessDeniedException {

		Graph myGraph = new org.openrdf.model.impl.GraphImpl();

		ValueFactory myFactory = myGraph.getValueFactory();
		String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
		//String namespace = "http://geosource.org/keyword#";
		String namespace = "#";

		URI mySubject = myFactory.createURI(namespace, code);

		URI skosClass = myFactory.createURI(namespaceSkos, "Concept");
		URI rdfType = myFactory.createURI(org.openrdf.vocabulary.RDF.TYPE);
		mySubject.addProperty(rdfType, skosClass); 

		URI myPredicate1 = myFactory.createURI(namespaceSkos, "prefLabel");
		Literal myObject1 = myFactory.createLiteral(prefLab, lang);
		myGraph.add(mySubject, myPredicate1, myObject1);

		URI myPredicate2 = myFactory.createURI(namespaceSkos, "scopeNote");
		Literal myObject2 = myFactory.createLiteral(note, lang);
		myGraph.add(mySubject, myPredicate2, myObject2);

		repository.addGraph(myGraph);

		return mySubject;
	}

	public void addElement(String code, String prefLab, String note, 
			String east, String west, String south, String north, 
			String lang)
		throws IOException,
			AccessDeniedException, GraphException {
		Graph myGraph = new org.openrdf.model.impl.GraphImpl();

		ValueFactory myFactory = myGraph.getValueFactory();

		// Define namespace
		String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
		String namespaceGml = "http://www.opengis.net/gml#";
		String namespace = "#";

		// Create subject
		URI mySubject = myFactory.createURI(namespace, code);

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

		Literal myObject1 = myFactory.createLiteral(prefLab, lang);
		Literal myObject2 = myFactory.createLiteral(note, lang);

		Literal lowerCorner = myFactory.createLiteral(west + " " + south);
		Literal upperCorner = myFactory.createLiteral(east + " " + north);

		mySubject.addProperty(rdfType, skosClass);
		myGraph.add(mySubject, predicatePrefLabel, myObject1);
		myGraph.add(mySubject, predicateScopeNote, myObject2);
		myGraph.add(mySubject, predicateBoundedBy, gmlNode);

		gmlNode.addProperty(rdfType, predicateEnvelope);
		myGraph.add(gmlNode, predicateLowerCorner, lowerCorner);
		myGraph.add(gmlNode, predicateUpperCorner, upperCorner);
		myGraph.add(gmlNode, predicateSrsName, srsNameURI);

		repository.addGraph(myGraph);

	}

	public void removeElement(KeywordBean keyword)
			throws MalformedQueryException, QueryEvaluationException,
			IOException, AccessDeniedException {		
		Graph myGraph = repository.getGraph();
		ValueFactory myFactory = myGraph.getValueFactory();
		
		URI subject = myFactory.createURI(keyword.getNameSpaceCode(),keyword.getRelativeCode());
		StatementIterator iter = myGraph.getStatements(subject,null,null);
		while (iter.hasNext()) {
			Statement st = (Statement) iter.next();
			if (st.getObject() instanceof BNode) {
				BNode node = (BNode) st.getObject();
				repository.getGraph().remove(node, null, null);
			}
			System.out.println(st.getSubject().toString() + " : "
					+ st.getPredicate().getLocalName() + " : "
					+ st.getObject().toString());
		}

		myGraph.remove(subject,null,null);
	}

	public URI updateElement(String namespace, String id, String prefLab, String note, String lang)
			throws IOException, MalformedQueryException,
			QueryEvaluationException, AccessDeniedException, GraphException {
		// Get thesaurus graph
		Graph myGraph = repository.getGraph();		
		
		// Set namespace skos and predicates 
		ValueFactory myFactory = myGraph.getValueFactory();
		String namespaceSkos = "http://www.w3.org/2004/02/skos/core#";
		URI predicatePrefLabel = myFactory
				.createURI(namespaceSkos, "prefLabel");
		URI predicateScopeNote = myFactory
				.createURI(namespaceSkos, "scopeNote");

		// Get subject (URI)
		URI subject = myFactory.createURI(namespace,id);

		// Remove old one
		StatementIterator iter = myGraph.getStatements(subject,
				predicatePrefLabel, null);
		while (iter.hasNext()) {
			Statement st = (Statement) iter.next();
			if (st.getObject() instanceof Literal) {
				Literal litt = (Literal) st.getObject();
				if (litt.getLanguage() != null
						&& litt.getLanguage().equals(lang)) {
					// remove
					myGraph.remove(st);
					break;
				}
			}
		}
		// Supp de scopeNote
		iter = myGraph.getStatements(subject, predicateScopeNote, null);
		while (iter.hasNext()) {
			Statement st = (Statement) iter.next();
			if (st.getObject() instanceof Literal) {
				Literal litt = (Literal) st.getObject();
				if (litt.getLanguage() != null
						&& litt.getLanguage().equals(lang)) {
					// Remove
					myGraph.remove(st);
					break;
				}
			}
		}

		Literal litPrefLab = myFactory.createLiteral(prefLab, lang);
		Literal litNote = myFactory.createLiteral(note, lang);

		myGraph.add(subject, predicatePrefLabel, litPrefLab);
		myGraph.add(subject, predicateScopeNote, litNote);

		return subject;
	}

	public void updateElement(String namespace, String id, String prefLab, String note,
			String east, String west, String south, String north, String lang)
			throws AccessDeniedException, IOException, MalformedQueryException,
			QueryEvaluationException, GraphException {

		// update label and definition
		URI subject = updateElement(namespace, id, prefLab, note, lang);

		// update bbox

		Graph myGraph = repository.getGraph();

		ValueFactory myFactory = myGraph.getValueFactory();
		String namespaceGml = "http://www.opengis.net/gml#";
		URI predicateBoundedBy = myFactory.createURI(namespaceGml, "BoundedBy");
		URI predicateLowerCorner = myFactory.createURI(namespaceGml,
				"lowerCorner");
		URI predicateUpperCorner = myFactory.createURI(namespaceGml,
				"upperCorner");

		BNode subjectGml = null;
		StatementIterator iter = myGraph.getStatements(subject,
				predicateBoundedBy, null);
		while (iter.hasNext()) {
			Statement st = (Statement) iter.next();
			if (st.getObject() instanceof BNode) {
				subjectGml = (BNode) st.getObject();
			}
		}
		if (subjectGml != null) {
			// lowerCorner
			iter = myGraph.getStatements(subjectGml, predicateLowerCorner, null);
			while (iter.hasNext()) {
				Statement st = (Statement) iter.next();
				myGraph.remove(st);
				break;
			}
			// upperCorner
			iter = myGraph.getStatements(subjectGml, predicateUpperCorner, null);
			while (iter.hasNext()) {
				Statement st = (Statement) iter.next();
				myGraph.remove(st);
				break;
			}
			// Preparation des nouveaux statements
			Literal lowerCorner = myFactory.createLiteral(west + " " + south);
			Literal upperCorner = myFactory.createLiteral(east + " " + north);

			// ajout des nouveaux statements
			myGraph.add(subjectGml, predicateLowerCorner, lowerCorner);
			myGraph.add(subjectGml, predicateUpperCorner, upperCorner);
		}
	}

	public boolean isFreeCode(String namespace, String code) throws AccessDeniedException {
		boolean res = true;				
		Graph myGraph = repository.getGraph();
		ValueFactory myFactory = myGraph.getValueFactory();		
		URI obj = myFactory.createURI(namespace,code);
		Collection statementsCollection = myGraph.getStatementCollection(obj,null,null);
		if (statementsCollection!=null && statementsCollection.size()>0){
			res = false;
		}
		statementsCollection = myGraph.getStatementCollection(null,null,obj);
		if (statementsCollection!=null && statementsCollection.size()>0){
			res = false;
		}				
		return res;
	}

	public void updateCode(String namespace, String oldcode, String newcode) throws AccessDeniedException, IOException {
		Graph myGraph = repository.getGraph();
		//Graph myTmpGraph = new org.openrdf.model.impl.GraphImpl();
		
		ValueFactory myFactory = myGraph.getValueFactory();
		//ValueFactory myTmpFactory = myTmpGraph.getValueFactory();
		
		URI oldobj = myFactory.createURI(namespace,oldcode);
		URI newobj = myFactory.createURI(namespace,newcode);
		StatementIterator iterStSubject = myGraph.getStatements(oldobj,null,null);
		while(iterStSubject.hasNext()){
			Statement st = (Statement) iterStSubject.next();			
			myGraph.add(newobj, st.getPredicate(), st.getObject());						
		}		
		
		StatementIterator iterStObject = myGraph.getStatements(null,null,oldobj);
		while(iterStObject.hasNext()){
			Statement st = (Statement) iterStObject.next();
			myGraph.add(st.getSubject(), st.getPredicate(), newobj);
		}
		myGraph.remove(oldobj,null,null);
		myGraph.remove(null,null,oldobj);		
		//repository.addGraph(myTmpGraph);
	}
}
