package jeeves.server;

import jeeves.server.sources.http.JeevesServlet;
import jeeves.utils.Log;
import jeeves.utils.XPath;
import jeeves.utils.Xml;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.jdom.*;
import org.jdom.filter.Filter;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class assists JeevesEngine by allowing certain configurations to be overridden.
 * <p/>
 * <p/>
 * The idea is to allow configurations to have seconds overridden for a specific server deployment.
 * A common scenario is to have test and production instances with different configurations.  In both configurations
 * 90% of the configuration is the same but certain parts need to be updated.
 * <p/>
 * This class allows an override file to be specified as a system property or a servlet init parameter:
 * <em>jeeves.configuration.overrides.file</em>.
 * The configuration in the override file will override settings in the "standard" loaded configuration
 * files.
 * <p/>
 * The overrides file can be a file (relative to the servlet base) or a URL
 * <p/>
 * Note:  When writing the xpath the root not should not be in the xpath and the path should not
 * start with a /.
 * <p/>
 * The override configuration structure is an XML file as follows:
 * <pre><[[CDATA[
 * <overrides>
    <!-- import values.  The imported values are put at top of sections -->
    <import file="./imported-config-overrides.xml" />
    <!-- The logging section allows logging configuration files to be specified -->
    <!-- At the moment only property based configuration files are supported -->
    <!-- There may be multiple configuration files -->
    <!-- The properties are loaded in order and subsequent files will override -->
    <!-- properties previously loaded.  This allows one to have shared configuration in -->
    <!-- one log file and more specific configuration in other files -->
    <logging>
        <logFile>logfile1.properties</logFile>
        <logFile>logfile2.properties</logFile>
    </logging>
     <!-- properties allow some properties to be defined that will be substituted -->
     <!-- into text or attributes where ${property} is the substitution pattern -->
     <!-- The properties can reference other properties -->
     <properties>
         <enabled>true</enabled>
         <dir>xml</dir>
         <aparam>overridden</aparam>
     </properties>
     <!-- In this version only the file name is considered not the path.  -->
     <!-- So case conf1/config.xml and conf2/config.xml cannot be handled -->
     <file name="config.xml">
         <!-- This example will update the file attribute of the xml element with the name attribute 'countries' -->
         <replaceAtt xpath="default/gui/xml[@name = 'countries']" attName="file" value="${dir}/europeanCountries.xml"/>
         <!-- if there is no value then the attribute is removed -->
         <replaceAtt xpath="default/gui" attName="removeAtt"/>
         <!-- If the attribute does not exist it is added -->
         <replaceAtt xpath="default/gui" attName="newAtt" value="newValue"/>

         <!-- This example will replace all the xml in resources with the contained xml -->
         <replaceXML xpath="resources">
           <resource enabled="${enabled}">
             <name>main-db</name>
             <provider>jeeves.resources.dbms.DbmsPool</provider>
              <config>
                  <user>admin</user>
                  <password>admin</password>
                  <driver>oracle.jdbc.driver.OracleDriver</driver>
                  <!-- ${host} will be updated to be local host -->
                  <url>jdbc:oracle:thin:@${host}:1521:fs</url>
                  <poolSize>10</poolSize>
              </config>
           </resource>
         </replaceXML>
         <!-- This example simple replaces the text of an element -->
         <replaceText xpath="default/language">${lang}</replaceText>
         <!-- This examples shows how only the text is replaced not the nodes -->
         <replaceText xpath="default/gui">ExtraText</replaceText>
         <!-- append xml as a child to a section (If xpath == "" then that indicates the root of the document),
              this case adds nodes to the root document -->
         <addXML xpath=""><newNode/></addXML>
         <!-- append xml as a child to a section, this case adds nodes to the root document -->
         <addXML xpath="default/gui"><newNode2/></addXML>
         <!-- remove a single node -->
         <removeXML xpath="default/gui/xml[@name = countries2]"/>
     </file>
     <file name="config2.xml">
         <replaceText xpath="default/language">de</replaceText>
     </file>
 </overrides>
 * ]]></pre>
 */
class ConfigurationOverrides {


    enum Updates {
        REPLACEATT,
        REPLACEXML,
        ADDXML,
        REMOVEXML,
        REPLACETEXT;
    }

    static String LOGFILE_XPATH = "logging/logFile";
    private static String OVERRIDES_KEY = "jeeves.configuration.overrides.file";
    private static String ATTNAME_ATTR_NAME = "attName";
    private static String VALUE_ATTR_NAME = "value";
    private static String XPATH_ATTR_NAME = "xpath";
    private static String FILE_NODE_NAME = "file";
    private static String FILE_NAME_ATT_NAME = "name";
    private static Pattern PROP_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
    private static final Filter ELEMENTS_FILTER = new Filter() {
        public boolean matches(Object obj) {
            return obj instanceof Element;
        }
    };
    private static final Filter TEXTS_FILTER = new Filter() {
        public boolean matches(Object obj) {
            return obj instanceof Text;
        }
    };


    public static void updateLoggingAsAccordingToOverrides(JeevesServlet servlet) throws JDOMException, IOException {
        String resource = lookupOverrideParameter(servlet);

        ServletResourceLoader loader = new ServletResourceLoader(servlet);
        Element xml = loader.loadXmlResource(resource);
        if (xml != null) {
            doUpdateLogging(xml, loader);
        }
    }

    /**
     * This method is default visibility for testing
     */
    static void doUpdateLogging(Element overrides, ResourceLoader loader) throws JDOMException, IOException {
        List<?> logOverides = Xml.selectNodes(overrides, LOGFILE_XPATH);
        Properties p = new Properties();
        for (Object logOveride : logOverides) {
            String value = ((Content) logOveride).getValue();
            InputStream in = loader.loadInputStream(value);
            if (in != null) {
                try {
                    p.load(in);
                } finally {
                    in.close();
                }
            } else {
                throw new IllegalArgumentException("log configuration file " + value + " was not found");
            }
        }
        if (logOverides.size() > 0) {
            LoggerRepository loggerRepo = Logger.getRootLogger().getLoggerRepository();
            loggerRepo.resetConfiguration();
            PropertyConfigurator configurator = new PropertyConfigurator();
            configurator.doConfigure(p, loggerRepo);
        }
    }

    public static void updateWithOverrides(String configFile, JeevesServlet servlet, Element configRoot) throws JDOMException, IOException {
        String resource = lookupOverrideParameter(servlet);

        ServletResourceLoader loader = new ServletResourceLoader(servlet);
        updateConfig(loader,resource,new File(configFile).getName(), configRoot);
    }

    /**
     * default visibility so that unit tests can be written against it
     */
    static void updateConfig(ResourceLoader loader, String overridesResource, String fileName, Element configRoot) throws JDOMException, IOException {
        Element overrides = loader.loadXmlResource(overridesResource);
        if (overrides == null) {
            return;
        }
        Properties properties = loadProperties(overrides);
        List<Element> files = overrides.getChildren(FILE_NODE_NAME);
        for (Element file : files) {
            String expectedfileName = file.getAttributeValue(FILE_NAME_ATT_NAME);
            if (expectedfileName.equals(fileName)) {
                Log.info(Log.JEEVES, "Overrides being applied to configuration file: " + fileName);

                List<Element> elements = file.getChildren();
                for (Element element : elements) {
                    switch (Updates.valueOf(element.getName().toUpperCase())) {
                        case ADDXML:
                            addXml(properties, element, configRoot);
                            break;
                        case REMOVEXML:
                            removeXml(properties, element, configRoot);
                            break;
                        case REPLACEXML:
                            replaceXml(properties, element, configRoot);
                            break;
                        case REPLACEATT:
                            replaceAtts(properties, element, configRoot);
                            break;
                        case REPLACETEXT:
                            replaceText(properties, element, configRoot);
                            break;
                        default:
                            throw new IllegalArgumentException(element.getName() + " is not a recognized update tag");
                    }
                }
            }
        }
    }

    private static void removeXml(Properties properties, Element elem, Element configRoot) throws JDOMException {
        String xpath = getXPath(elem);
        List<Content> matches = xpathLookup(configRoot, xpath);
        info("Removing xml elements: " + xpath);

        for (Content match : matches) {
            match.detach();
        }
    }

    private static void addXml(Properties properties, Element elem, Element configRoot) throws JDOMException {

        List<Content> newXml = updateProperties(properties, elem);
        String xpath = getXPath(elem);
        info("Adding xml elements to " + xpath);
        debug("Elements added are:" + Xml.getString(new Element("toAdd").addContent(newXml)));
        for (Content content : newXml) {
            content.detach();
        }

        if (xpath.trim().equals("")) {
            debug("Adding to root element");
            configRoot.addContent(newXml);
        } else {
            List<Content> matches = xpathLookup(configRoot, xpath);
            for (Content match : matches) {
                if (match instanceof Element) {
                    Element element = (Element) match;
                    debug("Adding xml to " + XPath.getXPath(element));
                    element.addContent(newXml);
                } else {
                    throw new IllegalArgumentException("the xpath of an Add XML overrides must select elements only");
                }
            }
        }
    }

    private static void replaceXml(Properties properties, Element elem, Element configRoot) throws JDOMException {
        String xpath = getXPath(elem);
        List<Content> matches = xpathLookup(configRoot, xpath);
        List<Content> newXml = updateProperties(properties, elem);
        info("Replacing child xml elements of " + xpath);
        debug("New elements are:" + Xml.getString(new Element("toAdd").addContent(newXml)));
        for (Content content : newXml) {
            content.detach();
        }
        for (Content toUpdate : matches) {
            if (toUpdate instanceof Element) {
                Element element = (Element) toUpdate;
                debug("replacingXML of " + XPath.getXPath(element));
                element.setContent(newXml);
            } else {
                throw new IllegalArgumentException("the xpath of an Replace XML override must select elements only");
            }

        }
    }

    private static void replaceText(Properties properties, Element elem, Element configRoot) throws JDOMException {
        String xpath = getXPath(elem);
        String text = updatePropertiesInText(properties, elem.getText());
        info("Replacing text of " + xpath);
        debug("New text is:" + text);

        List<Content> matches = xpathLookup(configRoot, xpath);
        for (Content toUpdate : matches) {
            if (toUpdate instanceof Element) {
                Element element = (Element) toUpdate;
                debug("replacing Text of " + XPath.getXPath(element));
                List<Text> textContent = toList(element.getDescendants(TEXTS_FILTER));

                if (textContent.size() > 0) {
                    for (int i = 0; i < textContent.size(); i++) {
                        Text text1 = textContent.get(i);
                        if (i == 0 && text.length() > 0) {
                            text1.setText(text);
                        } else {
                            text1.detach();
                        }
                    }
                } else {
                    element.addContent(text);
                }
            } else {
                throw new IllegalArgumentException("the xpath of an Replace Text override must select elements only");
            }

        }
    }

    private static void replaceAtts(Properties properties, Element elem, Element configRoot) throws JDOMException {
        String xpath = getXPath(elem);
        List<Content> matches = xpathLookup(configRoot, xpath);
        String attName = getCaseInsensitiveAttValue(elem, ATTNAME_ATTR_NAME, true);
        String newValue = updatePropertiesInText(properties, getCaseInsensitiveAttValue(elem, VALUE_ATTR_NAME, false));

        info("Replacing attribute " + attName + " of node " + xpath);
        debug("New attribute is:" + newValue);


        for (Content toUpdate : matches) {
            if (toUpdate instanceof Element) {
                Element element = (Element) toUpdate;
                debug("Updating attibute of node " + XPath.getXPath(element));
                if (newValue == null) {
                    element.removeAttribute(attName);
                } else {
                    element.setAttribute(attName, newValue);
                }
            } else {
                throw new IllegalArgumentException("the xpath of an Replace Attribute override must select elements only");
            }
        }
    }

    private static List<Content> xpathLookup(Element configRoot, String xpath) throws JDOMException {
        List<?> objects = Xml.selectNodes(configRoot, xpath);
        List<Content> elements = new ArrayList<Content>();

        for (Object object : objects) {
            if (object instanceof Content) {
                elements.add((Element) object);
            } else {
                throw new Error("How can this not be a content element");
            }
        }
        return elements;
    }

    private static Properties loadProperties(Element overrides) {
        Properties properties = new Properties();
        List<Element> pElem = overrides.getChildren("properties");
        for (Element element : pElem) {
            List<Element> props = element.getChildren();
            for (Element prop : props) {
                String key = prop.getName();
                String value = prop.getTextTrim();
                properties.put(key, value);
            }
        }

        while (!resolve(properties)) ;
        return properties;
    }

    private static String getXPath(Element elem) {
        return getCaseInsensitiveAttValue(elem, XPATH_ATTR_NAME, true);
    }

    private static String getCaseInsensitiveAttValue(Element elem, String name, boolean exceptionOnFailure) {
        List<Attribute> atts = elem.getAttributes();
        for (Attribute att : atts) {
            if (att.getName().equalsIgnoreCase(name)) {
                return att.getValue();
            }
        }
        if (exceptionOnFailure)
            throw new AssertionError(elem.getName() + " does not have a '" + name + "' attribute");
        else
            return null;
    }

    private static List<Content> updateProperties(Properties properties, Element elem) {
        Element clone = (Element) elem.clone();
        Iterator<Element> iter = clone.getDescendants(ELEMENTS_FILTER);

        List<Element> elems = toList(iter);

        for (Element next : elems) {
            List<Attribute> atts = next.getAttributes();
            for (Attribute att : atts) {
                if (!att.getName().equalsIgnoreCase(XPATH_ATTR_NAME)) {
                    String updatedValue = updatePropertiesInText(properties, att.getValue());
                    att.setValue(updatedValue);
                }
            }
        }
        Iterator<Text> iter2 = clone.getDescendants(TEXTS_FILTER);

        List<Text> textNodes = toList(iter2);

        for (Text text : textNodes) {
            String updatedText = updatePropertiesInText(properties, text.getText());
            text.setText(updatedText);
        }

        List<Content> newXml = new ArrayList<Content>(clone.getChildren());
        for (Content content : newXml) {
            content.detach();
        }

        return newXml;
    }

    private static List toList(Iterator iter) {
        ArrayList elems = new ArrayList();
        while (iter.hasNext()) {
            elems.add(iter.next());
        }
        return elems;
    }

    private static String updatePropertiesInText(Properties properties, String value) {
        if (value == null) {
            return null;
        }
        String updatedValue = value;
        Matcher matcher = PROP_PATTERN.matcher(updatedValue);
        while (matcher.find()) {
            String propKey = matcher.group(1);
            String propValue = properties.getProperty(propKey);

            if (propValue == null) {
                throw new IllegalArgumentException("Found a reference to a variable: " + propKey + " which is not a valid property.  Check the spelling");
            }
            String dataToReplace = matcher.group(0);
            updatedValue = updatedValue.replace(dataToReplace, propValue);
        }
        return updatedValue;
    }

    private static boolean resolve(Properties properties) {
        boolean finishedResolving = true;
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            Matcher matcher = PROP_PATTERN.matcher(value);

            if (matcher.find())
                finishedResolving = false;

            String updatedValue = updatePropertiesInText(properties, value);
            properties.put(key, updatedValue);
        }
        return finishedResolving;
    }

    private static void info(String msg) {
        Log.info(Log.JEEVES, msg);
    }

    private static void debug(String msg) {
        Log.debug(Log.JEEVES, msg);
    }

    private static String lookupOverrideParameter(JeevesServlet servlet) {
        String resource = System.getProperty(OVERRIDES_KEY);
        if (resource == null) {
            resource = servlet.getInitParameter(OVERRIDES_KEY);
        }
        if (resource == null) {
            resource = servlet.getServletContext().getInitParameter(OVERRIDES_KEY);
        }
        return resource;
    }

    public static abstract class ResourceLoader {
        protected abstract InputStream loadInputStream(String resource) throws JDOMException, IOException;
        protected String resolveImportFileName(String importResource, String baseResource) {
            String resolved = resolveRelative(importResource, baseResource, File.separator);
            if(resolved.equals(importResource)) {
                return resolveRelative(importResource, baseResource, "/");
            } else {
                return resolved;
            }
        }

        protected String resolveRelative(String importResource, String baseResource, String sep) {
            String back = ".." + sep;
            if(importResource.startsWith(back)) {
                return baseResource+sep+".."+sep+importResource;
            } else if(importResource.startsWith(back.substring(1))){
                return baseResource+sep+".."+sep+importResource;
            } else {
                return importResource;
            }
        }

        public final String loadStringResource(String resource) throws JDOMException, IOException {
            InputStream in = loadInputStream(resource);
            if (in != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder data = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        data.append(line);
                    }
                    return data.toString();
                } finally {
                    in.close();
                }
            } else {
                return null;
            }
        }

        public final Element loadXmlResource(String resource) throws JDOMException, IOException {
            InputStream in = loadInputStream(resource);
            if (in != null) {
                try {
                    return resolveImports(Xml.loadStream(in), resource);
                } finally {
                    in.close();
                }
            } else {
                return null;
            }
        }

        private Element resolveImports(Element baseElement, String baseResource) throws JDOMException, IOException {
            List<Element> imports = new ArrayList<Element>(baseElement.getChildren("import"));

            for (Element anImport : imports) {
                anImport.detach();
                String file = anImport.getAttributeValue("file");

                Element importedXml = loadXmlResource(resolveImportFileName(file, baseResource));
                List<Element> children = new ArrayList<Element>(importedXml.getChildren());
                for (Element toMerge : children) {
                    toMerge.detach();
                    if(toMerge.getName().equalsIgnoreCase("file")) {
                        String xpath = "file[@name = '" + toMerge.getAttributeValue("name") + "']";
                        merge(baseElement, toMerge, xpath, false);
                    } else if(toMerge.getName().equalsIgnoreCase("properties")) {
                        merge(baseElement, toMerge, toMerge.getName(), true);
                    } else {
                        merge(baseElement, toMerge, toMerge.getName(), false);
                    }
                }
            }
            return baseElement;
        }

        private void merge(Element baseElement, Element toMerge, String xpath, boolean overrideImports ) throws JDOMException {
            Element mergeTarget = Xml.selectElement(baseElement, xpath);
            if(mergeTarget != null) {
                Collection<Content> contentToAdd = detach(toMerge.getContent());
                if(overrideImports) {
                    contentToAdd = filterOutExistingElements(mergeTarget, contentToAdd);
                }
                mergeTarget.addContent(0,contentToAdd);
            } else {
                baseElement.addContent(toMerge);
            }
        }

        private Collection<Content> filterOutExistingElements(Element mergeTarget, Collection<Content> contentToAdd) {
            ArrayList<Content> toAdd = new ArrayList<Content>();
            for (Content c : contentToAdd)
            {
                if(c instanceof Element) {
                    Element e = (Element) c;
                    if(mergeTarget.getChild(e.getName()) == null) {
                        toAdd.add(e);
                    }
                } else {
                    toAdd.add(c);
                }
            }
            contentToAdd = toAdd;
            return contentToAdd;
        }

        private Collection<Content> detach(List content) {
            ArrayList<Content> al = new ArrayList<Content>(content);
            for (Content o : al) {
                o.detach();
            }
            return al;
        }
    }

    static class ServletResourceLoader extends ResourceLoader {
        private final JeevesServlet servlet;

        ServletResourceLoader(JeevesServlet servlet) {
            this.servlet = servlet;
        }

        protected InputStream loadInputStream(String resource) throws JDOMException, IOException {

            if (resource != null) {
                InputStream in;
                try {
                    in = new URL(resource).openStream();
                } catch (MalformedURLException e) {
                    URL url;
                    try {
                        url = servlet.getServletContext().getResource(resource);
                    } catch (MalformedURLException e2) {
                        url = null;
                    }
                    if (url == null) {
                        File file = new File(resource);
                        if (file.isAbsolute() && file.exists()) {
                            in = new FileInputStream(file);
                        } else {
                            throw new IllegalArgumentException("The resource file " + resource + " is not a file and not a web resource: " + url + ".  Perhaps a leading / was forgotten?");
                        }
                    } else {
                        in = url.openStream();
                    }
                }
                return in;
            }
            return null;
        }
    }

}
