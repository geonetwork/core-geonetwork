package org.fao.geonet.transifex.xml;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.sf.json.JSONObject;
import org.eclipse.core.runtime.Assert;
import org.fao.geonet.transifex.Layout;
import org.fao.geonet.transifex.TransifexReadyFile;
import org.fao.geonet.transifex.TranslationFileConfig;
import org.fao.geonet.transifex.TranslationFormat;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The very flexible Xml translation file format of geonetwork. By default it is assumed that the XML file is a tree with the
 * xpath being the key and the value being the text in the element.  However a single XML file can have multiple keys and values.
 * See the example configuration for and example
 * <p>
 * Example configuration:
 * <pre><code>
 *   &lt;configuration>
 *       <files>
 *           <file>
 *               &lt;id>iso19139-labels&lt;/id>
 *               &lt;name>Iso19139 Schema labels&lt;/name>
 *               &lt;categories>
 *                   &lt;category>iso19139&lt;/category>
 *                   &lt;category>schema-plugin&lt;/category>
 *                   &lt;category>labels&lt;/category>
 *               &lt;/categories>
 *               &lt;format-class>xml.XmlFormat&lt;/format-class>
 *               &lt;parameters>
 *                    &lt;resolver.1.name>name&lt;/resolver.1.name>
 *                    &lt;resolver.1.keyXPath>&#42;//entry/key&lt;/resolver.1.keyXPath>
 *                    &lt;resolver.1.valueXPath>../value/text()&lt;/resolver.1.valueXPath>
 *                    &lt;resolver.2.name>description&lt;/resolver.2.name>
 *                    &lt;resolver.2.keyXPath>&#42;//entry/key&lt;/resolver.2.keyXPath>
 *                    &lt;resolver.2.valueXPath>../description/text()&lt;/resolver.2.valueXPath>
 *               &lt;/parameters>
 *           </file>
 *       </files>
 *   &lt;/configuration>
 * </code></pre>
 * </p>
 *
 * @author Jesse on 6/18/2015.
 */
public class XmlFormat implements TranslationFormat {
    private static final Pattern ELEMENT_NAME_EXTRACTOR = Pattern.compile("([^\\[]+)(\\[(.+)\\])?");
    private static final Pattern ELEMENT_ATT_EXTRACTOR = Pattern.compile("( and )?([^=,]+)='([^']+)'");
    public static final String TEXT_EL_PREFIX = "node()[text()='";
    List<TranslationResolver> resolvers = Lists.newArrayList();
    private TranslationFileConfig stdConfig;

    @Override
    public XmlFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties) {
        this.stdConfig = stdConfig;
        return this;
    }

    @Override
    public List<TransifexReadyFile> toTransifex(String translationFile) throws Exception {
        List<TransifexReadyFile> files = Lists.newArrayList();
        Path path = Paths.get(translationFile);
        if (Files.exists(path)) {
            Element element = Xml.loadFile(path);
            for (TranslationResolver resolver : resolvers) {
                JSONObject translations = new JSONObject();
                @SuppressWarnings("unchecked")
                List<Element> nodes = (List<Element>) Xml.selectNodes(element, resolver.keyElem);
                for (Element node : nodes) {
                    String key = createId(node, resolver.includeTextInKey);
                    String value;
                    if (resolver.valueXPath.equals("copy-of")) {
                        StringBuilder valueBuilder = new StringBuilder();
                        for (Object o : node.getContent()) {
                            if (o instanceof Element) {
                                Element elem = (Element) o;
                                valueBuilder.append(Xml.getString(elem));
                            } else if (o instanceof Text) {
                                Text text = (Text) o;
                                valueBuilder.append(text.getText());
                            }  else if (o instanceof Comment) {
                                Comment comment = (Comment) o;
                                valueBuilder.append("<!--").append(comment.getText()).append("-->");
                            } else {
                                valueBuilder.append(o.toString());
                            }
                        }
                        value = valueBuilder.toString();
                    } else {
                        value = Xml.selectString(node, resolver.valueXPath);
                    }
                    if (value != null && !value.trim().isEmpty()) {
                        translations.put(key, value);
                    }
                }
                if (translations.size() > 0) {
                    String resourceId = stdConfig.id + "-" + resolver.name.toLowerCase();
                    String transifexName = stdConfig.name + " " + resolver.name + "s";
                    files.add(new TransifexReadyFile(resourceId, transifexName, translations.toString(2), stdConfig.categories));
                }
            }
        }
        return files;
    }

    @VisibleForTesting
    String createId(Element node, boolean includeTextInKey) {
        StringBuilder id = new StringBuilder();
        if (includeTextInKey && !node.getTextTrim().isEmpty()) {
            id.append("node()[text()='").append(node.getTextTrim()).append("']");
        }

        while (node != null) {
            if (id.length() > 0) {
                id.insert(0, "/");
            }
            StringBuilder atts = new StringBuilder("[");
            //noinspection unchecked
            for (Attribute attribute : (List<Attribute>) node.getAttributes()) {
                if (attribute.getNamespace().getURI().equals("http://www.w3.org/2001/XMLSchema-instance") ||
                    attribute.getNamespace().getURI().equals("http://www.w3.org/1999/xlink")) {
                    continue;
                }
                if (atts.length() > 1) {
                    atts.append(" and ");
                }
                atts.append(attribute.getName()).append("='").append(attribute.getValue()).append("'");
            }
            if (atts.length() > 1) {
                atts.append(']');
                id.insert(0, atts);
            }
            id.insert(0, node.getName());
            node = node.getParentElement();
        }
        return id.toString();
    }

    @Override
    public String toGeonetwork(List<TransifexReadyFile> fromTransifex) throws IOException, JDOMException {
        Element root = null;
        for (TransifexReadyFile transifexReadyFile : fromTransifex) {
            String typeId = transifexReadyFile.resourceId.substring(transifexReadyFile.resourceId.lastIndexOf("-") + 1);
            TranslationResolver type = findType(typeId);
            JSONObject properties = JSONObject.fromObject(transifexReadyFile.data);
            @SuppressWarnings("unchecked")
            Set<String> keySet = properties.keySet();

            for (String key : keySet) {
                String[] sections = key.split("/");
                String value = properties.getString(key);
                if (root == null) {
                    root = createElement(sections[0]);
                }
                addElements(root, sections, 1, type, value);

            }
        }
        return root == null ? "<translations/>" : Xml.getString(root);
    }

    private TranslationResolver findType(String typeId) {
        for (TranslationResolver resolver : resolvers) {
            if (resolver.name.toLowerCase().equals(typeId)) {
                return resolver;
            }
        }
        throw new IllegalArgumentException("No resolver with type id: " + typeId);
    }

    private Element createElement(String section) {
        Matcher elMatcher = ELEMENT_NAME_EXTRACTOR.matcher(section);
        Assert.isTrue(elMatcher.find());
        String elName = elMatcher.group(1);
        Element element = new Element(elName);
        if (elMatcher.groupCount() == 3 && elMatcher.group(3) != null) {
            Matcher attMatcher = ELEMENT_ATT_EXTRACTOR.matcher(elMatcher.group(3));
            while(attMatcher.find()) {
                element.setAttribute(attMatcher.group(2), attMatcher.group(3));
            }
        }
        return element;
    }

    private void addElements(Element element, String[] sections, int startSectionIdx, TranslationResolver type, String value) throws
            IOException, JDOMException {
        BestMatch bestMatch = findBestMatch(element, sections, startSectionIdx);
        Element keyEl = addElement(bestMatch.element, sections, bestMatch.startIndex, null);
        addElement(keyEl, type.valueXPath.split("/"), 0, value);
    }

    BestMatch findBestMatch(final Element element, final String[] sections, final int startSectionIdx) {
        int i = startSectionIdx;
        Element bestMatch = element;
        while(i < sections.length) {
            Element child = findChild(sections[i], bestMatch);

            if (child == null) {
                break;
            }

            bestMatch = child;
            i++;
        }
        return new BestMatch(i, bestMatch);
    }

    private Element findChild(String section, Element element) {
        Matcher elMatcher = ELEMENT_NAME_EXTRACTOR.matcher(section);
        Assert.isTrue(elMatcher.find());
        String elName = elMatcher.group(1);
        @SuppressWarnings("unchecked")
        List<Element> children = element.getChildren(elName);
        for (Element child : children) {
            boolean allRequiredAttributes = true;
            int matchedAttributes = 0;
            if (elMatcher.groupCount() == 3 && elMatcher.group(3) != null) {
                Matcher attMatcher = ELEMENT_ATT_EXTRACTOR.matcher(elMatcher.group(3));
                while(attMatcher.find()) {
                    String key = attMatcher.group(2);
                    String value = attMatcher.group(3);

                    if (child.getAttribute(key) != null && child.getAttributeValue(key).equals(value)) {
                        matchedAttributes++;
                    } else {
                        allRequiredAttributes = false;
                        break;
                    }
                }
            }
            if (allRequiredAttributes && child.getAttributes().size() == matchedAttributes) {
                return child;
            }
        }
        return null;
    }

    private Element addElement(Element element, String[] sections, int startSectionIdx, String value) throws IOException, JDOMException {
        Element leaf = element;
        for (int i = startSectionIdx; i < sections.length; i++) {
            if (sections[i].equals("copy-of")) {
                // continue
            } else if (sections[i].equals("..")) {
                leaf = leaf.getParentElement();
            } else if (sections[i].startsWith("@")) {
                if (i != sections.length - 1) {
                    throw new AssertionError(Joiner.on('/').join(sections) + " should only have an attribute as the last element in path");
                }
            } else {
                if (sections[i].equals(".")) {
                    // do nothing
                } else if (sections[i].startsWith(TEXT_EL_PREFIX)) {
                    String text = sections[i].substring(TEXT_EL_PREFIX.length(), sections[i].length() - 2);
                    leaf.setText(text);
                } else {
                    Element next = createElement(sections[i]);
                    leaf.addContent(next);
                    leaf = next;
                }
            }
        }
        if (value != null) {
            String lastSegment = sections[sections.length - 1];
            if (lastSegment.equals("copy-of")) {
                leaf.addContent(Xml.loadString("<x>" + value + "</x>", false).cloneContent());
            } else if (lastSegment.startsWith("@")) {
                leaf.setAttribute(lastSegment.substring(1), value);
            } else {
                leaf.setText(value);
            }
        }

        return leaf;
    }

    @Override
    public Layout getDefaultLayout() {
        return Layout.DIR;
    }

    public List<TranslationResolver> getResolvers() {
        return resolvers;
    }

    private static final class BestMatch {
        final int startIndex;
        final Element element;

        private BestMatch(int startIndex, Element element) {
            this.startIndex = startIndex;
            this.element = element;
        }
    }
}
