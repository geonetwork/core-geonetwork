package jeeves.server.overrides;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.utils.Log;
import jeeves.utils.XPath;
import jeeves.utils.Xml;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.jdom.*;
import org.jdom.filter.Filter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletContext;
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
 * <p/>
 * The order of resolution is:
 * <ol>
 * <li>System property with key:  <em>{servlet.getServletContext().getServletContextName()}.jeeves.configuration.overrides.file</em></li>
 * <li>Servlet init parameter with key:  <em>jeeves.configuration.overrides.file</em></li>
 * <li>System property with key:  <em>jeeves.configuration.overrides.file</em></li>
 * <li>Servlet <em>context</em> init parameters with key:  <em>jeeves.configuration.overrides.file</em></li>
 * </ol>
 * 
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
     <!-- properties allow some properties to be defined that will be substituted -->
     <!-- into text or attributes where ${property} is the substitution pattern -->
     <!-- The properties can reference other properties -->
     <properties>
         <enabled>true</enabled>
         <dir>xml</dir>
         <aparam>overridden</aparam>
     </properties>
     <!-- A regular expression for matching the file affected. -->
     <file name=".*WEB-INF/config\.xml">
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
     <file name=".*WEB-INF/config2\.xml">
         <replaceText xpath="default/language">de</replaceText>
     </file>
     <!-- a normal file tag is for updating XML configuration files -->
     <!-- textFile tags are for updating normal text files like sql files -->
     <textFile name="test-sql.sql">
     	<!-- each line in the text file is matched against the linePattern attribute and the new value is used for substitution -->
     	<update linePattern="(.*) Relations">$1 NewRelations</update>
     	<update linePattern="(.*)relatedId(.*)">$1${aparam}$2</update>
     </textFile>
     <!-- configure the spring aspects of geonetwork -->
     <spring>
         <!-- import a complete spring xml file -->
         <import file="./config-spring-overrides.xml"/>
         <!-- declare a file as a spring properties override file: See http://static.springsource.org/spring/docs/3.0.x/api/org/springframework/beans/factory/config/PropertyOverrideConfigurer.html -->
         <propertyOverrides file="./config-property-overrides.properties" />
         <!-- set a property on one bean to reference another bean -->
         <set bean="beanName" property="propertyName" ref="otherBeanName"/>
         <!-- add a references to a bean to a property on another bean.  This assumes the property is a collection -->
         <add bean="beanName" property="propertyName" ref="otherBeanName"/>
      </spring>
 </overrides>
 * ]]></pre>
 * 
 * 	 A original proposal about the overrides are at:
 *   <a href="http://trac.osgeo.org/geonetwork/wiki/ConfigOverride">http://trac.osgeo.org/geonetwork/wiki/ConfigOverride</a>
 *   The API has changed slightly since it was written but the principals remain the same 

 */
public class ConfigurationOverrides {


    private static final String CONFIG_OVERRIDES_FILENAME = "config-overrides.xml";

	enum Updates {
        REPLACEATT,
        REPLACEXML,
        ADDXML,
        REMOVEXML,
        REPLACETEXT;
    }

    static String LOGFILE_XPATH = "logging/logFile";
    public static String OVERRIDES_KEY = "jeeves.configuration.overrides.file";
    private static String ATTNAME_ATTR_NAME = "attName";
    private static String VALUE_ATTR_NAME = "value";
    private static String XPATH_ATTR_NAME = "xpath";
    private static String FILE_NODE_NAME = "file";
    private static String TEXT_FILE_NODE_NAME = "textFile";
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
    private static final List<Namespace> WEB_XML_NS = new ArrayList<Namespace>();
    static {
        WEB_XML_NS.add(Namespace.getNamespace("http://java.sun.com/xml/ns/j2ee"));
    }
    /**
     * Update the logging configuration so that it uses the configuration defined in the overrides rather than the defaults
     * 
     * @param context the servlet context that is loaded (maybe null.  If null appPath is used to resolve configuration files like: /WEB-INF/configuration-overrides.xml
     * @param appPath The path to the webapplication root.  If servlet is null (and therefore getResource cannot be used, this path is used to file files)
     */
    public static void updateLoggingAsAccordingToOverrides(ServletContext context, String appPath) throws JDOMException, IOException {
        String resource = lookupOverrideParameter(context, appPath);

        ServletResourceLoader loader = new ServletResourceLoader(context,appPath);
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

    /**
     * 
     *
     * @param configFile the path to the configuration file that has been loaded (and is the configRoot).  This is used to identify overrides
     * @param context
     *@param appPath The path to the webapplication root.  If servlet is null (and therefore getResource cannot be used, this path is used to file files)
     * @param configElement the root element of the configuration (obtained by loading configFile)    @throws JDOMException
     * @throws IOException
     */
    public static void updateWithOverrides(String configFile, ServletContext context, String appPath, Element configElement) throws JDOMException, IOException {
        String resource = lookupOverrideParameter(context,appPath);

        ServletResourceLoader loader = new ServletResourceLoader(context,appPath);
        updateConfig(loader,resource,configFile, configElement);
    }

    private static void updateConfig(ResourceLoader loader, String overridesResource, String configFilePath, Element configRoot) throws JDOMException, IOException {
        Element overrides = loader.loadXmlResource(overridesResource);
        if (overrides == null) {
            return;
        }
        Properties properties = loadProperties(overrides);
        List<Element> files = overrides.getChildren(FILE_NODE_NAME);
        for (Element file : files) {
            String expectedfileName = file.getAttributeValue(FILE_NAME_ATT_NAME);

            if (Pattern.matches(expectedfileName, configFilePath)) {
            	Log.info(Log.JEEVES, "Overrides being applied to configuration file: " + expectedfileName);

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
        // Register all namespaces which may be required to solve xpath
        ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
        for (Iterator iterator = configRoot.getAdditionalNamespaces().iterator(); iterator.hasNext();) {
            Namespace ns = (Namespace) iterator.next();
            namespaces.add(ns);
        }
        
        List<?> objects = Xml.selectNodes(configRoot, xpath, namespaces);
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

    static String updatePropertiesInText(Properties properties, String value) {
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

    private static String lookupOverrideParameter(ServletContext context,String appPath) throws JDOMException, IOException {
        String resource;
        if(context == null) {
            resource = lookupOverrideParamFromAppPath(appPath);
        } else {
            resource = lookupOverrideParamFromServlet(context);
        }
        return resource;
    }

    private static String lookupOverrideParamFromAppPath(String appPath) throws JDOMException, IOException {
        File webInf = new File(appPath,"WEB-INF");
		File webxmlFile = new File(webInf,"web.xml");
        String resource = null;
        if(webxmlFile.exists()) {
            Element webXML = Xml.loadFile(webxmlFile);
            Namespace namespace = webXML.getNamespace();
            String webappName = webXML.getChildTextTrim("display-name", namespace);

            if(webappName == null || webappName.isEmpty()) {
                webappName = "geonetwork";
            }
            resource = System.getProperty(webappName+"."+OVERRIDES_KEY);
            
        }
        if (resource == null || resource.trim().isEmpty()) {
        	resource = lookupOverrideParamFromConfigFile(new File(webInf,CONFIG_OVERRIDES_FILENAME).toURI().toURL());
        }
        if (resource == null || resource.trim().isEmpty()) {
            resource = System.getProperty(OVERRIDES_KEY);
        }
        return resource;
    }

    @SuppressWarnings("unchecked")
	private static String lookupOverrideParamFromConfigFile(URL url) throws IOException, JDOMException {
    	try {
			Element config = Xml.loadFile(url);
			StringBuilder builder = new StringBuilder();
			for(Element elem : (List<Element>)config.getChildren("override")) {
				if(builder.length() > 0) {
					builder.append(',');
				}
				builder.append(elem.getTextTrim());
			}
			return builder.toString();
    	} catch (FileNotFoundException e) {
    		return null;
    	}
	}

	private static String lookupOverrideParamFromServlet(ServletContext context) throws IOException, JDOMException {
        String resource;
        resource = System.getProperty(context.getServletContextName()+"."+OVERRIDES_KEY);
        if (resource == null || resource.trim().isEmpty()) {
            resource = lookupOverrideParamFromConfigFile(context.getResource("/WEB-INF/" + CONFIG_OVERRIDES_FILENAME));
        }
        if (resource == null || resource.trim().isEmpty()) {
            resource = System.getProperty(OVERRIDES_KEY);
        }
        if (resource == null || resource.trim().isEmpty()) {
            resource = context.getInitParameter(OVERRIDES_KEY);
        }
        return resource;
    }

    public static abstract class ResourceLoader {
        protected InputStream loadInputStream(String resource) throws IOException {
            File file = resolveFile(resource);
            if(file == null) {
                return fallbackInputStream(resource);
            } else {
                return new FileInputStream(file);
            }
        }
        protected abstract File resolveFile(String resource) throws IOException;
        protected abstract InputStream fallbackInputStream(String resource) throws IOException;
        protected String resolveImportFileName(String importResource, String baseResource) {
            String resolved = resolveRelative(importResource, baseResource, File.separator);
            if(resolved.equals(importResource)) {
                return resolveRelative(importResource, baseResource, "/");
            } else {
                return resolved;
            }
        }

        protected String resolveRelative(String importResource, String baseResource, String sep) {
            String baseDir = baseResource;
            int lastSep = baseDir.lastIndexOf(sep);
            if(lastSep > -1) {
                baseDir = baseDir.substring(0,lastSep);
            }
            String back = ".." + sep;
            if(importResource.startsWith(back)) {
                return baseDir+sep+".."+sep+importResource;
            } else if(importResource.startsWith(back.substring(1))){
                return baseDir+sep+importResource;
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

        public final Element loadXmlResource(String resource) throws IOException {
        	Element loadedResource = null;
        	if(resource == null) {
        	    return null;
        	}
        	String[] resources = resource.split(",");
            for (String string : resources) {
                if (!string.trim().isEmpty()) {
                    InputStream in = loadInputStream(string);
                    if (in != null) {
                        try {
                            try {
                                Element element = Xml.loadStream(in);
                                Element loaded = resolveImports(element, string);
                                if (loadedResource == null) {
                                    loadedResource = loaded;
                                } else {
                                    mergeElements(loadedResource, loaded);
                                }
                            } catch (JDOMException e) {
                                throw new IOException(e);
                            }
                        } finally {
                            in.close();
                        }
                    } else {
                        if(Log.isDebugEnabled(Log.JEEVES))
                            Log.debug(Log.JEEVES, "Unable to load Configuration Override resource: " + string);
                    }
                }
            }
            return loadedResource;
        }

        private Element resolveImports(Element baseElement, String baseResource) throws JDOMException, IOException {
            List<Element> imports = new ArrayList<Element>(baseElement.getChildren("import"));

            for (Element anImport : imports) {
                anImport.detach();
                String file = anImport.getAttributeValue("file");

                Element importedXml = loadXmlResource(resolveImportFileName(file, baseResource));
                mergeElements(baseElement, importedXml);
                
            }
            return baseElement;
        }
		private void mergeElements(Element baseElement, Element importedXml) throws JDOMException {
            List<Element> children = new ArrayList<Element>(importedXml.getChildren());
            for (Element toMerge : children) {
                toMerge.detach();
                if (toMerge.getName().equalsIgnoreCase("file")) {
                    String xpath = "file[@name = '" + toMerge.getAttributeValue("name") + "']";
                    merge(baseElement, toMerge, xpath, false);
                } else if (toMerge.getName().equalsIgnoreCase("properties")) {
                    merge(baseElement, toMerge, toMerge.getName(), true);
                } else if (toMerge.getName().equalsIgnoreCase("textFile")) {
                    String xpath = "textFile[@name = '" + toMerge.getAttributeValue("name") + "']";
                    merge(baseElement, toMerge, xpath, false);
                } else {
                    merge(baseElement, toMerge, toMerge.getName(), false);
                }
            }
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
                    Element e = (Element) c.detach();
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
        private final ServletContext context;
        private String appPath;

        ServletResourceLoader(ServletContext context, String appPath) {
            this.context = context;
            this.appPath = appPath;
        }

        @Override
        protected File resolveFile(String resource) throws IOException {
            File file = null;
            File testPath = new File(resource);

            if(testPath.exists()) {
                file = testPath;
            }
            if(file ==null && context!=null) {
                String path = context.getRealPath(resource);
                if(path != null) {
                    testPath = new File(path);
                    if(testPath.exists()) {
                        file = testPath;
                    }
                }
            }
            if(file == null && appPath != null) {
                File testFile = new File(appPath, resource);
                if (testFile.exists()) {
                    file = testFile;
                }
            }

            if(file == null) {
                URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
                if (url != null) {
                    File testFile = new File(url.getFile());
                    if (testFile.exists()) {
                        file = testFile;
                    } else {
                        testFile = new File(url.getPath());
                        if (testFile.exists()) {
                            file = testFile;
                        }
                    }
                }
            }
            return file;
        }

        @Override
        protected InputStream fallbackInputStream(String resource) throws IOException {

            if (resource != null) {
                InputStream in;
                try {
                    // try resource as a url
                    in = new URL(resource).openStream();
                } catch (MalformedURLException e) {
                    URL url;
                    try {
                        if(context != null) {
                            // try to get resource from the servlet context if servlet is non-null
                            url = context.getResource(resource);
                        } else {
                            // fall back to appPath is servlet is null 
                            File appBasedFile = new File(appPath,resource);
                            if(appBasedFile.exists()) {
                                url = appBasedFile.toURI().toURL();
                            } else {
                                url = null;
                            }
                       }
                    } catch (MalformedURLException e2) {
                        url = null;
                    }
                    if (url == null) {
                        File file = new File(resource);
                        if (file.exists()) {
                            in = new FileInputStream(file);
                        } else {
                        	file = new File(resource.replace('/', '\\'));
                        	if(file.exists()) {
                        		in = new FileInputStream(file);
                        	} else {
                        		throw new IllegalArgumentException("The resource file " + resource + " is not a file and not a web resource: " + url + ".  Perhaps a leading / was forgotten?");
                        	}
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

    /**
     * Load an XML file and applies the overrides to the file
     * 
     *
     * @param servletRelativePath file to load.  It is assumed to be with the servlet and is assumed to start with /.
     * @param context the ServletContext to use for locating the file
     * @return
     */
    public static Element loadXmlFileAndUpdate(String servletRelativePath, ServletContext context) {
        String appPath = context.getContextPath();
        Element elem = null;
        try {
            elem = Xml.loadFile(context.getRealPath(servletRelativePath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            updateWithOverrides(servletRelativePath, context, appPath, elem);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(Log.JEEVES, "Unable to read overrides for config.xml: "+e);
        }
        return elem;
    }


    /**
     * Loads a text file, compares each line to the textFile elements in the overrides file and if a match is found replaces that line with the line in the overrides
     * 
     * Examples of use are to be able to update server.props using overrides and the sql data,create, etc... files with overrides so that the files can contain the defaults
     * and the specifics for a particular platform can be configured using overrides
     * 
     * @param configFilePath The path to the files to be loaded and overriden.  IE /WEB-INF/server.prop
     * @param contex the servlet context that is loaded (maybe null.  If null appPath is used to resolve configuration files like: /WEB-INF/configuration-overrides.xml
     * @param appPath The path to the webapplication root.  If servlet is null (and therefore getResource cannot be used, this path is used to file files)
     * @param reader a buffered reader opened to the file to be loaded.
     * 
     * @return the array of lines in the file.
     * @throws JDOMException 
     */
	public static List<String> loadTextFileAndUpdate(String configFilePath, ServletContext context, String appPath, BufferedReader reader) throws IOException {
	    ServletResourceLoader loader = new ServletResourceLoader(context, appPath);
	    
        try {
            String resource = lookupOverrideParameter(context, appPath);
            return loadFileAndUpdate(loader, resource, configFilePath, reader);
        } catch (JDOMException e) {
            return loadFileAndUpdate(loader, null, configFilePath, reader);
        }

	}

    private static List<String> loadFileAndUpdate(ResourceLoader loader, String overridesResource, String configFilePath,
            BufferedReader reader) throws IOException {
        Element overrides = loader.loadXmlResource(overridesResource);
		HashMap<Pattern, String> matches = new HashMap<Pattern, String>();

		Properties properties = new Properties();
		if (overrides != null) {
			properties = loadProperties(overrides);
			List<Element> files = overrides.getChildren(TEXT_FILE_NODE_NAME);

			for (Element file : files) {
				String expectedfileName = file
						.getAttributeValue(FILE_NAME_ATT_NAME);

				if (Pattern.matches(expectedfileName, configFilePath)) {
					List<Element> updates = file.getChildren("update");
					for (Element element : updates) {
						matches.put(Pattern.compile(element
								.getAttributeValue("linePattern")), element
								.getTextTrim());
					}
				}
			}
		}

		ArrayList<String> al = new ArrayList<String>();

		String line = reader.readLine();
		try {
			while (line != null) {
				for (Map.Entry<Pattern, String> entry : matches.entrySet()) {
					Matcher matcher = entry.getKey().matcher(line);
					if (matcher.matches()) {
						String value = updatePropertiesInText(properties,
								entry.getValue());
						line = matcher.replaceFirst(value);
						break;
					}
				}
				al.add(line);
				line = reader.readLine();
			}
			return al;
		} finally {
			reader.close();
		}
	}

    @SuppressWarnings("unchecked")
    public static void importSpringConfigurations(XmlBeanDefinitionReader reader, ConfigurableBeanFactory beanFactory, ServletContext servletContext, String appPath) throws JDOMException, IOException {
        String overridesResource = lookupOverrideParameter(servletContext, appPath);

        ResourceLoader loader = new ServletResourceLoader(servletContext, appPath);
        
        Element overrides = loader.loadXmlResource(overridesResource);
        if (overrides == null) {
            return;
        }

        Properties properties = loadProperties(overrides);

        for(Element e: (List<Element>) overrides.getChildren("spring")) {
            for (Element element : (List<Element>) e.getChildren("import")) {

                String importFile = element.getAttributeValue("file");
                importFile = updatePropertiesInText(properties, importFile);
                
                Log.info(Log.JEEVES, "ConfigurationOverrides: importing spring file into application context: "+importFile);
                File file = loader.resolveFile(importFile);
                if(file != null) {
                    Resource inputSource = new FileSystemResource(file);
                    reader.loadBeanDefinitions(inputSource);
                } else {
                    InputStream inputStream = loader.loadInputStream(importFile);
                    try {
                        Resource inputSource = new InputStreamResource(inputStream);
                        reader.loadBeanDefinitions(inputSource);
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
            }
        }

        
    }

    @SuppressWarnings("unchecked")
    public static void applyNonImportSpringOverides(JeevesApplicationContext jeevesApplicationContext, ServletContext servletContext,
            String appPath) throws JDOMException, IOException {

        String overridesResource = lookupOverrideParameter(servletContext, appPath);

        ResourceLoader loader = new ServletResourceLoader(servletContext, appPath);
        
        Element overrides = loader.loadXmlResource(overridesResource);
        if (overrides == null) {
            return;
        }
        
        Properties properties = loadProperties(overrides);
        List<Element> updateEls = new ArrayList<Element>();
        List<Element> spring = new ArrayList<Element>(overrides.getChildren("spring"));
        for (Element el: spring) {
            for (Element element : (List<Element>) el.getChildren()) {
                if(!element.getName().equals("import")) {
                    updateEls.add(element);
                }
                
            }
        }
        new SpringPropertyOverrides(updateEls, properties).applyOverrides(jeevesApplicationContext);
    }

}
