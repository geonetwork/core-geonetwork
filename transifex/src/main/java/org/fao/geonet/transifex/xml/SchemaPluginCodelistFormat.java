package org.fao.geonet.transifex.xml;

import com.google.common.collect.Lists;
import net.sf.json.JSONObject;
import org.fao.geonet.transifex.Layout;
import org.fao.geonet.transifex.TransifexReadyFile;
import org.fao.geonet.transifex.TranslationFileConfig;
import org.fao.geonet.transifex.TranslationFormat;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Strategy for translating the schema plugins labels files
 *
 * @author Jesse on 6/18/2015.
 */
public class SchemaPluginCodelistFormat implements TranslationFormat {
    public static final String LABEL = "label";
    public static final String DESCRIPTION = "description";
    private TranslationFileConfig stdConfig;

    @Override
    public TranslationFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties) {
        this.stdConfig = stdConfig;
        return this;
    }

    @Override
    public List<TransifexReadyFile> toTransifex(String translationFile) throws Exception {
        List<TransifexReadyFile> files = Lists.newArrayList();
        Path path = Paths.get(translationFile).resolve("eng").resolve(stdConfig.fileName);
        if (Files.exists(path)) {
            Element element = Xml.loadFile(path);
            @SuppressWarnings("unchecked")
            List<Element> objects = (List<Element>) Xml.selectNodes(element, "*//entry");
            JSONObject json = new JSONObject();
            for (Element object : objects) {
                String key = object.getParentElement().getAttributeValue("name") + "/" + object.getChild("code").getTextTrim();
                Element label = object.getChild(LABEL);
                Element description = object.getChild(DESCRIPTION);
                if (label != null && !label.getTextTrim().isEmpty()) {
                    json.put(key + "/" + LABEL, label.getTextTrim());
                }
                if (description != null && !description.getTextTrim().isEmpty()) {
                    json.put(key + "/" + DESCRIPTION, description.getTextTrim());
                }
            }
            if (json.size() > 0) {
                files.add(new TransifexReadyFile(stdConfig.id, stdConfig.name, json.toString(), stdConfig.categories));
            }
        }
        return files;
    }

    @Override
    public String toGeonetwork(List<TransifexReadyFile> fromTransifex) throws JDOMException {
        if (fromTransifex.size() == 1) {
            Element codelists = new Element("codelists");
            JSONObject jsonObject = JSONObject.fromObject(fromTransifex.get(0).data);
            Set set = jsonObject.keySet();
            for (Object jsonKey : set) {
                String[] parts = ((String) jsonKey).split("/");
                String codelist = parts[0];
                String key = parts[1];
                String type = parts[2];
                String value = jsonObject.getString((String) jsonKey);
                Element codelistElement = Xml.selectElement(codelists, "codelist[@name='" + codelist + "']");
                if (codelistElement == null) {
                    codelistElement = new Element("codelist").setAttribute("name", codelist);
                    codelists.addContent(codelistElement);
                }

                Element entryElement = Xml.selectElement(codelistElement, "entry[code/text() = '" + key + "']");
                if (entryElement == null) {
                    entryElement = new Element("entry").addContent(new Element("code").setText(key));
                    codelistElement.addContent(entryElement);
                }

                entryElement.addContent(new Element(type).setText(value));
            }

            return Xml.getString(codelists);
        }
        return null;
    }

    @Override
    public Layout getDefaultLayout() {
        return Layout.DIR;
    }
}
