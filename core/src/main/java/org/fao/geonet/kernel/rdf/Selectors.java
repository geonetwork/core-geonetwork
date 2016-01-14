package org.fao.geonet.kernel.rdf;

import static java.text.MessageFormat.format;

import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Namespace;

/**
 * Represents selecting an element in a RDF query.  Normally there is a "variable" and a "path"
 * 
 * @author jeichar
 */
public class Selectors {
    public static final Namespace GML_NAMESPACE = Namespace.getNamespace("gml","http://www.opengis.net/gml#");
    public static final Namespace SKOS_NAMESPACE = Namespace.getNamespace("skos","http://www.w3.org/2004/02/skos/core#");
    public static final Selector ID = new Selector("id", "{id} rdf:type {skos:Concept}", SKOS_NAMESPACE);
		public static final Selector TOPCONCEPTS = new Selector("", "{id} rdf:type {skos:Concept}, {cs} rdf:type {skos:ConceptScheme}, {cs} skos:hasTopConcept {id}", SKOS_NAMESPACE);
    public static final Selector EMPTY = new Selector("", "", SKOS_NAMESPACE);
    public static final Selector PREF_LABEL = new Selector("prefLabel", "{id} skos:prefLabel {prefLabel}", SKOS_NAMESPACE);
    public static Selector prefLabel(String lang, IsoLanguagesMapper mapper) {
        String varName = lang+LABEL_POSTFIX;
        String twoCodelang = mapper.iso639_2_to_iso639_1(lang, lang.substring(0,2));
        return new Selector(varName, "{id} skos:prefLabel {"+varName+"}", SKOS_NAMESPACE).where(Wheres.ilike("lang("+varName+")", twoCodelang));
    }
    public static final Selector NOTE = new Selector("note", "{id} skos:scopeNote {note}", SKOS_NAMESPACE);
    public static Selector note(String lang, IsoLanguagesMapper mapper) {
        String varName = lang+NOTE_POSTFIX;
        String twoCodelang = mapper.iso639_2_to_iso639_1(lang, lang.substring(0,2));
        return new Selector(varName, "{id} skos:scopeNote {"+varName+"}", SKOS_NAMESPACE).where(Wheres.ilike("lang("+varName+")", twoCodelang));
    }
	public static final Selector LOWER_CORNER = new Selector("lowc", "{id} gml:BoundedBy {} gml:lowerCorner {lowc}", GML_NAMESPACE);
	public static final Selector UPPER_CORNER = new Selector("uppc", "{id} gml:BoundedBy {} gml:upperCorner {uppc}", GML_NAMESPACE);
	public static final Selector SRS_NAME = new Selector("srsName", "{id} gml:BoundedBy {} gml:srsName {srsName}", GML_NAMESPACE);
	public static final Selector BROADER = new Selector("broader", "{id} skos:broader {broader}", SKOS_NAMESPACE);
	/**
	 * Add a column with the language of another column.
	 *
	 * For example: languages(Selectors.PREF_LABEL)
	 *
	 * @param columnSelector the column that this selector is based on
	 * 
	 * @return a selector
	 */
	public static Selector languages(Selector columnSelector) {
	    return new Selector(format("lang({0}) as \"language\"", columnSelector.id), ID.getPath()+", "+columnSelector.getPath(), Selectors.SKOS_NAMESPACE);
	}

	public static final String LABEL_POSTFIX = "_prefLabel";
    public static final String NOTE_POSTFIX = "_note";
    public static Selector related(String id, KeywordRelation request) {
        return new Selector("", "{id} skos:"+request+" {b}", SKOS_NAMESPACE).where(Wheres.like("b", "*"+id));
    }
}
