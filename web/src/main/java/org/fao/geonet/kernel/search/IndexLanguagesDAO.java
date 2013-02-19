package org.fao.geonet.kernel.search;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;
import org.fao.geonet.kernel.setting.domain.IndexLanguage;
import org.fao.geonet.util.spring.CollectionUtils;
import org.jdom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Persistency of index languages. This class purely exist so it is not DataManager.
 *
 */
public class IndexLanguagesDAO {

    public Set<IndexLanguage> retrieveIndexLanguages(Dbms dbms) throws Exception {
        String query = "SELECT languageName, selected FROM IndexLanguages";
        List<Element> results = dbms.select(query).getChildren();
        for(Element r : results) {
            System.out.println("\n\n** retrieved language: " + Xml.getString(r));
        }
        Set<IndexLanguage> languages = new HashSet<IndexLanguage>();
        for(Element result : results) {
            IndexLanguage language = new IndexLanguage();
            language.setName(result.getChildText("languagename"));
            language.setSelected(result.getChildText("selected").equals("y"));
            languages.add(language);
        }
        return languages;
    }

    public Set<IndexLanguage> retrieveSelectedIndexLanguages(Dbms dbms) throws Exception {
        String query = "SELECT languageName, selected FROM IndexLanguages where selected = 'y'";
        List<Element> results = dbms.select(query).getChildren();
        Set<IndexLanguage> languages = new HashSet<IndexLanguage>();
        for(Element result : results) {
            IndexLanguage language = new IndexLanguage();
            language.setName(result.getChildText("languagename"));
            language.setSelected(true);
            languages.add(language);
        }
        return languages;
    }

    public void saveIndexLanguages(Set<IndexLanguage> languages, Dbms dbms) throws Exception {
       for(IndexLanguage language : languages) {
           // check if exists in db
           String query = "SELECT * FROM IndexLanguages WHERE languageName = ?";
           List<Element> results = dbms.select(query, language.getName()).getChildren();
           // does not yet exist: insert
           if(CollectionUtils.isEmpty(results)) {
               query = "INSERT INTO IndexLanguages (languageName, selected) VALUES (?, ?)";
               dbms.execute(query, language.getName(), language.isSelected() ? "y" : "n" );
           }
           // already exists:  update
           else {
               query = "UPDATE IndexLanguages SET selected=? WHERE languageName=?";
               dbms.execute(query, language.isSelected() ? "y" : "n", language.getName());
           }
           dbms.commit();
       }
    }

}
