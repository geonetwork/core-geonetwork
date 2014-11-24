package org.fao.geonet.kernel.search;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

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
            
            Path schemaDir = schemaManager.getSchemaDir(schema);
            addCodeLists(codeListName, _codeList, schemaDir.resolve("loc").resolve(langCode).resolve("codelists.xml"));
        }
        return _codeList;
    }

    public static String cacheKey(final String langCode, final String codeListName) {
        return "codelist:"+langCode+":"+codeListName;
    }

    @SuppressWarnings("unchecked")
    private void addCodeLists(String codeListName, Map<String, String> codeList, Path file) throws IOException, JDOMException {
        if (Files.exists(file)) {
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