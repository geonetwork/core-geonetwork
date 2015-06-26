package jeeves.xlink;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;
import org.apache.jcs.access.exception.CacheException;
import org.fao.geonet.JeevesJCS;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Process XML document having XLinks to resolve, remove and detach fragments.
 *
 * TODO : Define when to empty the cache ? and how to clean all or only one
 * fragments in the cache ?
 *
 * @author pvalsecchi
 * @author fxprunayre
 * @author sppigot
 */
public final class Processor {

    private static TreeSet<Long> failures = new TreeSet<Long>();

    private static final int MAX_FAILURES = 50;

    private static final long ELAPSE_TIME = 30000;

    /**
     * Action to specify to remove all children off elements having an XLink.
     */
    private static final String ACTION_REMOVE = "remove";

    /**
     * Action to specify to resolve all XLinks.
     */
    private static final String ACTION_RESOLVE = "resolve";

    /**
     * Action to specify to uncache all XLinks.
     */
    private static final String ACTION_UNCACHE = "uncache";

    /**
     * Action to specify to resolve and remove all XLinks.
     */
    private static final String ACTION_DETACH = "detach";

    public static final String XLINK_JCS = "xlink";

    private static CopyOnWriteArraySet<URIMapper> uriMapper = new CopyOnWriteArraySet<URIMapper>();

    /**
     * Default constructor.
     * Builds a Processor.
     */
    private Processor() {}

    /**
     * Resolve all XLinks of the input XML document.
     *
     * @return All set of all the xlinks that failed to resolve.
     */
    public static Set<String> processXLink(Element xml, ServiceContext srvContext) {
        Set<String> errors = Sets.newHashSet();
        errors.addAll(searchXLink(xml, ACTION_RESOLVE, srvContext));
        errors.addAll(searchLocalXLink(xml, ACTION_RESOLVE));
        return errors;
    }

    //--------------------------------------------------------------------------
    /**
     * Uncache all XLinks child of the input XML document.
     */
    public static Element uncacheXLink(Element xml) {
        searchXLink(xml, ACTION_UNCACHE, null);
        return xml;
    }

    //--------------------------------------------------------------------------
    /**
     * Remove all XLinks child of the input XML document.
     */
    public static Element removeXLink(Element xml) {
        searchXLink(xml, ACTION_REMOVE, null);
        searchLocalXLink(xml, ACTION_REMOVE);
        return xml;
    }

    //--------------------------------------------------------------------------
    /**
     * Resolve XLinks in document and remove the xlink attributes.
     */
    public static Element detachXLink(Element xml, ServiceContext context) {
        searchXLink(xml, ACTION_DETACH, context);
        searchLocalXLink(xml, ACTION_DETACH);
        return xml;
    }

    //--------------------------------------------------------------------------
    /**
     * Return all XLinks child of the input XML document.
     */
    public static List<Attribute> getXLinks(Element md) {
        return getXLinksWithXPath(md, "*//@xlink:href");
    }

    //--------------------------------------------------------------------------
    /**
     * Remove an XLink from the cache.
     */
    public static void removeFromCache(String xlinkUri) throws CacheException {

        JeevesJCS xlinkCache = JeevesJCS.getInstance(XLINK_JCS);
        if (xlinkCache.get(xlinkUri)!=null) {
            xlinkCache.remove(xlinkUri);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Clear the cache.
     */
    public static void clearCache() throws CacheException {
        JeevesJCS.getInstance(XLINK_JCS).clear();
    }

    //--------------------------------------------------------------------------
    /**
     * Add an XLink to the cache.
     */
    public static void addXLinkToCache(String uri, Element fragment) throws CacheException {
        JeevesJCS xlinkCache = JeevesJCS.getInstance(XLINK_JCS);
        Element cachedFragment = (Element) xlinkCache.get(uri.toLowerCase());
        if (cachedFragment == null) {
            xlinkCache.put(uri.toLowerCase(), fragment);
        }
    }

    //--------------------------------------------------------------------------
    /** Resolves an xlink */
    public static Element resolveXLink(String uri, ServiceContext srvContext) throws IOException, JDOMException, CacheException {
        String idSearch = null;
        return resolveXLink(uri, idSearch, srvContext);
    }

    //--------------------------------------------------------------------------
    /** Resolves an xlink*/
    public static synchronized Element resolveXLink(String uri, String idSearch, ServiceContext srvContext) throws IOException, JDOMException, CacheException {

        cleanFailures();
        if (failures.size()>MAX_FAILURES) {
            throw new RuntimeException("There have been "+failures.size()+" timeouts resolving xlinks in the last "+ELAPSE_TIME+" ms");
        }
        Element remoteFragment = null;
        try {
            if(uri.startsWith(XLink.LOCAL_PROTOCOL)) {
                LocalServiceRequest request = LocalServiceRequest.create(uri.replaceAll("&amp;", "&"));
                request.setDebug(false);
                if(request.getLanguage() == null) {
                    request.setLanguage(srvContext.getLanguage());
                }
                request.setInputMethod(InputMethod.GET);
                remoteFragment = srvContext.execute(request);
            } else {
                uri = uri.replaceAll("&+","&");
                String mappedURI = mapURI(uri);

                JeevesJCS xlinkCache = JeevesJCS.getInstance(XLINK_JCS);
                remoteFragment = (Element) xlinkCache.getFromGroup(uri.toLowerCase(), mappedURI);
                if (remoteFragment == null) {
                    Log.info(Log.XLINK_PROCESSOR, "cache MISS on "+uri.toLowerCase());
                    URL url = new URL(uri.replaceAll("&amp;", "&"));

                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(1000);

                    BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                    try {
                        remoteFragment = Xml.loadStream(in);
                        if(Log.isDebugEnabled(Log.XLINK_PROCESSOR))
                            Log.debug(Log.XLINK_PROCESSOR,"Read:\n"+Xml.getString(remoteFragment));
                    } finally {
                        in.close();
                    }
                } else {
                    Log.debug(Log.XLINK_PROCESSOR, "cache HIT on "+uri.toLowerCase());
                }

                if (remoteFragment != null && !remoteFragment.getName().equalsIgnoreCase("error")) {
                    xlinkCache.putInGroup(uri.toLowerCase(), mappedURI, remoteFragment);
                    if(Log.isDebugEnabled(Log.XLINK_PROCESSOR))
                        Log.debug(Log.XLINK_PROCESSOR,"cache miss for "+uri);
                } else {
                    return null;
                }

            }
        } catch (Exception e) {	// MalformedURLException, IOException
            synchronized(Processor.class) {
                failures.add (System.currentTimeMillis());
            }

            Log.error(Log.XLINK_PROCESSOR,"Failed on " + uri, e);
        }

        // search for and return only the xml fragment that has @id=idSearch

        Element res = null;
        if (idSearch != null) {
            String xpath = "*//*[@id='" + idSearch + "']";
            try {
                res = Xml.selectElement(remoteFragment, xpath);
                if (res != null) {
                    res = (Element)res.clone();
                    res.removeAttribute("id");
                }
            } catch (Exception e) {
                Log.warning(Log.XLINK_PROCESSOR,"Failed to search for remote fragment using " + xpath + ", error" + e.getMessage());
                return null;
            }
        } else {
            if (remoteFragment == null) {
                return null;
            } else {
                res = (Element) remoteFragment.clone();
            }
        }
        if(Log.isDebugEnabled(Log.XLINK_PROCESSOR))
            Log.debug(Log.XLINK_PROCESSOR,"Read:"+Xml.getString(res));
        return res;
    }

    public static String mapURI( String uri ) {
        uri = uri.replaceAll("&+","&").toLowerCase();
        for(URIMapper mapper: uriMapper) {
            uri = mapper.map(uri);
        }
        return uri;
    }

    public static void addUriMapper(URIMapper mapper) {
        uriMapper.add(mapper);
    }
    public static void removeUriMapper(URIMapper mapper) {
        uriMapper.add(mapper);
    }

    //--------------------------------------------------------------------------
    /** Uncaches an xlink */
    public static void uncacheXLinkUri(String uri) throws CacheException {
        JeevesJCS xlinkCache = JeevesJCS.getInstance(XLINK_JCS);
        String mappedURI = mapURI(uri);
        Set groupKeys = xlinkCache.getGroupKeys(mappedURI);
        if(groupKeys==null || groupKeys.isEmpty()) {
            xlinkCache.remove(uri);
        } else {
            for( Object key : groupKeys ) {
                xlinkCache.remove(key, mappedURI);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    /**
     * Utility to return all XLinks child of the input XML document that match
     * specified XPath.
     */
    @SuppressWarnings("unchecked")
    private static List<Attribute> getXLinksWithXPath(Element md, String xpath) {
        List<Namespace> theNss = new ArrayList<Namespace>();
        theNss.add(XLink.NAMESPACE_XLINK);
        List<Attribute> xlinks = new ArrayList<Attribute>();
        try {
            xlinks = (List<Attribute>) Xml.selectNodes(md, xpath, theNss);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(Log.XLINK_PROCESSOR, e.getMessage());
        }
        return xlinks;
    }

    //--------------------------------------------------------------------------
    /**
     * Search for Remote XLinks in XML document. Load and cache remote resource
     * if needed.
     * <p/>
     * TODO : Maybe don't wait to much to load a remote resource. Add timeout
     * param?
     *
     * @param action
     *            Define what to do with XLink ({@link #ACTION_DETACH,
     *            #ACTION_REMOVE, #ACTION_RESOLVE}).
     * @return All set of all the xlinks that failed to resolve.
     *
     */
    private static Set<String> searchXLink(Element md, String action, ServiceContext srvContext) {
        List<Attribute> xlinks = getXLinksWithXPath(md, "*//@xlink:href");

        if(Log.isDebugEnabled(Log.XLINK_PROCESSOR)) Log.debug(Log.XLINK_PROCESSOR, "returned "+xlinks.size()+" elements");

        Set<String> errors = Sets.newHashSet();
        // process remote xlinks, skip local xlinks for later
        for (Attribute xlink : xlinks) {
            String hrefUri = xlink.getValue();
            if(Log.isDebugEnabled(Log.XLINK_PROCESSOR)) Log.debug(Log.XLINK_PROCESSOR, "will resolve href '"+hrefUri+"'");
            String idSearch = null;
            int hash = hrefUri.indexOf('#');
            if (hash > 0 && hash != hrefUri.length()-1) {
                idSearch = hrefUri.substring(hash+1);
                hrefUri = hrefUri.substring(0, hash);
            }

            if (hash != 0) { // skip local xlinks eg. xlink:href="#details"
                String error = doXLink(hrefUri, idSearch, xlink, action, srvContext);
                if (error != null) {
                    errors.add(error);
                }
            }
        }

        return errors;
    }

    //--------------------------------------------------------------------------
    /**
     * Search for Local XLinks in XML document. eg. xlink:href="#details"
     * <p/>
     * TODO : cache local fragments to avoid calling same xpath. 
     *
     * @param action
     *            Define what to do with XLink ({@link #ACTION_DETACH,
     *            #ACTION_REMOVE, #ACTION_RESOLVE}).
     *
     * @return All set of all the xlinks that failed to resolve.
     */
    private static Set<String> searchLocalXLink(Element md, String action) {
        List<Attribute> xlinks = getXLinksWithXPath(md, "*//@xlink:href[starts-with(.,'#')]");

        if(Log.isDebugEnabled(Log.XLINK_PROCESSOR))
            Log.debug(Log.XLINK_PROCESSOR, "local xlink search returned "+xlinks.size()+" elements");

        Set<String> errors = Sets.newHashSet();
        // now all remote fragments have been added, process local xlinks (uncached)
        Map<String,Element> localIds = new HashMap<String,Element>();
        for (Attribute xlink : xlinks) {
            Element element = xlink.getParent();
            if (action.equals(ACTION_REMOVE)) {
                element.getParentElement().removeAttribute("show", XLink.NAMESPACE_XLINK);
                element.removeContent();
            } else {
                String idSearch = xlink.getValue().substring(1);
                if(Log.isDebugEnabled(Log.XLINK_PROCESSOR))
                    Log.debug(Log.XLINK_PROCESSOR, "process local xlink '"+idSearch+"'");
                Element localFragment = localIds.get(idSearch);
                try {
                    if (localFragment == null) {
                        localFragment = Xml.selectElement(md, "*//*[@id='" + idSearch + "']");
                        localIds.put(idSearch,localFragment);
                    }

                    // -- avoid recursivity if an xlink:href #ID is a descendant of the localFragment

                    // Should work in XPath v2. Failed with JDOM :
                    // localFragment = Xml.selectElement(md, "*//*[@id='" + idSearch + "' "
                    //  		+ "and count(descendant::*[@xlink:href='#" + idSearch + "'])=0]");
                    List<Attribute> subXlinks = getXLinksWithXPath(localFragment, "*//@xlink:href[.='#" + idSearch + "']");
                    if (subXlinks.size()!=0) {
                        Log.warning(Log.XLINK_PROCESSOR, "found a fragment " + Xml.getString(localFragment) + " containing "
                                                         + subXlinks.size() + " reference(s) to itself. Id: " + idSearch);
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(Log.XLINK_PROCESSOR, "Failed to look up localxlink "+idSearch+": "+e.getMessage());
                }
                if (localFragment != null) {
                    localFragment = (Element)localFragment.clone();
                    localFragment.removeAttribute("id");
                    // replace children of this element with the fragment
                    element.removeContent();
                    element.addContent(localFragment);
                } else {
                    errors.add(xlink.getValue());
                }
            }
            cleanXLinkAttributes(element, action);
        }
        return errors;
    }

    //--------------------------------------------------------------------------

    /**
     * Resolve the xlink of an element and if resolved correctly, updates the xlink.
     *
     * Returns null if the XLINK was correctly resolved, or if there was a failure, returns the xlink that was not resolved.
     */
    private static String doXLink(String hrefUri, String idSearch, Attribute xlink, String action, ServiceContext srvContext) {
        Element element = xlink.getParent();

        // Don't process XLink for operatesOn
        List<String> excludedXlinkElements = new ArrayList<String>();
        excludedXlinkElements.add("operatesOn");
        excludedXlinkElements.add("featureCatalogueCitation");
        excludedXlinkElements.add("Anchor");
        excludedXlinkElements.add("source");
        // TODO: Add configuration file

        if (excludedXlinkElements.contains(element.getName())) {
            return null;
        }

        if (!hrefUri.equals("")) {
            String show = element.getAttributeValue(XLink.SHOW, XLink.NAMESPACE_XLINK);
            if (show == null || show.equals("")) show = XLink.SHOW_EMBED;
            if (show.equalsIgnoreCase(XLink.SHOW_EMBED) || show.equalsIgnoreCase(XLink.SHOW_REPLACE)) {
                if (action.equals(ACTION_REMOVE)) {
                    element.removeContent();
                } else if (action.equals(ACTION_UNCACHE)) {
                    try {
                        uncacheXLinkUri(hrefUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.error(Log.XLINK_PROCESSOR,"Uncaching failed: "+e.getMessage());
                    }
                } else {
                    try {
                        Element remoteFragment = resolveXLink(hrefUri, idSearch, srvContext);

                        // Not resolved in cache or using href
                        if (remoteFragment == null)
                            return hrefUri;

                        searchXLink(remoteFragment, action, srvContext);

                        if (show.equalsIgnoreCase(XLink.SHOW_REPLACE)) {
                            // replace this element with the fragment
                            if (!action.equals(ACTION_DETACH) && show.equalsIgnoreCase(XLink.SHOW_REPLACE)) {
                                remoteFragment.setAttribute((Attribute)xlink.clone());
                                remoteFragment.setAttribute(new Attribute(XLink.SHOW, XLink.SHOW_REPLACE, XLink.NAMESPACE_XLINK));
                            }
                            Element parent = element.getParentElement();
                            int index = parent.indexOf(element);
                            parent.setContent(index,remoteFragment);
                        } else { // show = XLink.SHOW_EMBED
                            // replace children of this element with the fragment
                            element.removeContent();
                            element.addContent(remoteFragment);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.error(Log.XLINK_PROCESSOR,"doXLink "+action+" failed: "+e.getMessage());
                    }
                }
                cleanXLinkAttributes(element, action);
            } else {
                Log.error(Log.XLINK_PROCESSOR,"Invalid xlink:show attribute '"+show+"'");
            }
        }

        return null;
    }

    //--------------------------------------------------------------------------
    private static void cleanXLinkAttributes(Element element, String action) {
        // Clean all XLink related attributes
        if (action.equals(ACTION_DETACH)) {
            element.removeAttribute(XLink.HREF, XLink.NAMESPACE_XLINK);
            element.removeAttribute(XLink.ROLE, XLink.NAMESPACE_XLINK);
            element.removeAttribute(XLink.TITLE, XLink.NAMESPACE_XLINK);
        }
    }

    //--------------------------------------------------------------------------
    private synchronized static void cleanFailures() {
        long now = System.currentTimeMillis();

        for (Iterator<Long> iter = failures.iterator(); iter.hasNext();) {
            long next = iter.next();
            if (now-next > ELAPSE_TIME) {
                iter.remove();
            } else {
                break;
            }
        }
    }

}
