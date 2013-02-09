package org.fao.geonet.kernel.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jeeves.utils.Xml;

import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;
import org.jdom.JDOMException;

public final class CodeListCacheLoader implements Callable<Map<String, String>> {
    private final String langCode;
    private final String codeListName;
    private final SchemaManager schemaManager;

    public CodeListCacheLoader(String langCode, String codeListName, SchemaManager schemaManager) {
        this.langCode = langCode;
        this.codeListName = codeListName;
        this.schemaManager = schemaManager;
    }

    @Override
    public Map<String, String> call() throws Exception {
        Map<String, String> _codeList = new HashMap<String, String>();
        Set<String> schemas = schemaManager.getSchemas();
        for (String schema : schemas) {
            
            String schemaDir = schemaManager.getSchemaDir(schema);
            addCodeLists(codeListName, _codeList, new File(schemaDir + "/loc/" + langCode + "/codelists.xml"));
        }
        return _codeList;
    }

    public static String cacheKey(final String langCode, final String codeListName) {
        return "codelist:"+langCode+":"+codeListName;
    }

    @SuppressWarnings("unchecked")
    private void addCodeLists(String codeListName, Map<String, String> codeList, File file) throws IOException, JDOMException {
        if (file.exists()) {
            Element xmlDoc = Xml.loadFile(file);

            List<Element> codelists = xmlDoc.getChildren("codelist");
            for (Element element : codelists) {
                if (element.getAttributeValue("name").equals(codeListName)) {
                    List<Element> entries = element.getChildren("entry");
                    for (Element entry : entries) {
                        codeList.put(entry.getChildText("code"), entry.getChildText("label"));
                    }
                    break;
                }
            }
        }
    }

}