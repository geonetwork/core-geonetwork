package jeeves.xlink;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import jeeves.JeevesJCS;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

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
public class Processor {

	private static TreeSet<Long> failures = new TreeSet<Long>();

	private static int MAX_FAILURES = 50;

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

	//--------------------------------------------------------------------------
  /**
    * Resolve all XLinks of the input XML document.
    */
	public static Element processXLink(Element xml) {
		searchXLink(xml, ACTION_RESOLVE);
		searchLocalXLink(xml, ACTION_RESOLVE);
		return xml;
	}

	//--------------------------------------------------------------------------
  /**
    * Uncache all XLinks child of the input XML document.
    */
	public static Element uncacheXLink(Element xml) {
		searchXLink(xml, ACTION_UNCACHE);
		return xml;
	}

	//--------------------------------------------------------------------------
  /**
    * Remove all XLinks child of the input XML document.
    */
	public static Element removeXLink(Element xml) {
		searchXLink(xml, ACTION_REMOVE);
		searchLocalXLink(xml, ACTION_REMOVE);
		return xml;
	}

	//--------------------------------------------------------------------------
  /**
    * Detach all XLinks child of the input XML document.
    */
	public static Element detachXLink(Element xml) {
		searchXLink(xml, ACTION_DETACH);
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
	public static Element resolveXLink(String uri) throws IOException, JDOMException, CacheException {
		String idSearch = null;
		return resolveXLink(uri, idSearch);
	}

	//--------------------------------------------------------------------------
	/** Resolves an xlink */
	public static Element resolveXLink(String uri, String idSearch) throws IOException, JDOMException, CacheException {

		cleanFailures();
		if (failures.size()>MAX_FAILURES) {
			throw new RuntimeException("There have been "+failures.size()+" timeouts resolving xlinks in the last "+ELAPSE_TIME+" ms");
		}

		JeevesJCS xlinkCache = JeevesJCS.getInstance(XLINK_JCS);
		Element remoteFragment = (Element) xlinkCache.get(uri.toLowerCase());

		if (remoteFragment == null) {
			Log.info(Log.XLINK_PROCESSOR, "cache MISS on "+uri.toLowerCase());
			
			try {
				URL url = new URL(uri.replaceAll("&amp;", "&"));
				
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(1000);
			
				BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
				try {
					remoteFragment = Xml.loadStream(in);
					Log.debug(Log.XLINK_PROCESSOR,"Read:\n"+Xml.getString(remoteFragment));
				} finally {
					in.close();
				}
			} catch (Exception e) {	// MalformedURLException, IOException
				synchronized(Processor.class) {
					failures.add (System.currentTimeMillis());
				}
				Log.error(Log.XLINK_PROCESSOR,"Failed on " + uri 
						+ " with exception message " + e.getMessage());
			}
			
			if (remoteFragment != null) {
				xlinkCache.put(uri.toLowerCase(), remoteFragment);
				Log.debug(Log.XLINK_PROCESSOR,"cache miss for "+uri);
			} else {
				return null;
			}	
					
		} else {
			Log.info(Log.XLINK_PROCESSOR, "cache HIT on "+uri.toLowerCase());
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
				Log.debug(Log.XLINK_PROCESSOR,"Failed to search for remote fragment using " + xpath + ", error" + e.getMessage());
				return null;
			}
		} else {
			res = (Element)remoteFragment.clone();
		}
		Log.debug(Log.XLINK_PROCESSOR,"Read:"+Xml.getString(res));
		return res;
	}

	//--------------------------------------------------------------------------
	/** Uncaches an xlink */
	public static void uncacheXLinkUri(String uri) throws CacheException {
		JeevesJCS xlinkCache = JeevesJCS.getInstance(XLINK_JCS);
		Element theXLink = (Element)xlinkCache.get(uri.toLowerCase());
		if (theXLink == null) {
			Log.error(Log.XLINK_PROCESSOR,"Uri "+uri+" wasn't there");
		} else {
			xlinkCache.remove(uri);
			Log.error(Log.XLINK_PROCESSOR,"Uri "+uri+" was removed from cache");
		}
	}

	//--------------------------------------------------------------------------
	// Private methods
	//--------------------------------------------------------------------------

  /**
    * Utility to return all XLinks child of the input XML document that match
		* specified XPath.
    */
	private static List<Attribute> getXLinksWithXPath(Element md, String xpath) {
		List<Namespace> theNss = new ArrayList();
		theNss.add(XLink.NAMESPACE_XLINK);
		List<Attribute> xlinks = new ArrayList();
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
    *
    */
	private static void searchXLink(Element md, String action) {
		List<Attribute> xlinks = getXLinksWithXPath(md, "*//@xlink:href");
				
		Log.debug(Log.XLINK_PROCESSOR, "returned "+xlinks.size()+" elements");

		// process remote xlinks, skip local xlinks for later
		for (Attribute xlink : xlinks) {
			String hrefUri = xlink.getValue();
			Log.debug(Log.XLINK_PROCESSOR, "will resolve href '"+hrefUri+"'");
			String idSearch = null;
			int hash = hrefUri.indexOf('#');
			if (hash > 0 && hash != hrefUri.length()-1) {
				idSearch = hrefUri.substring(hash+1);
				hrefUri = hrefUri.substring(0, hash);
			}

			if (hash != 0) { // skip local xlinks eg. xlink:href="#details"
				doXLink(hrefUri, idSearch, xlink, action);
			}
		}
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
     */
	private static void searchLocalXLink(Element md, String action) {
		List<Attribute> xlinks = getXLinksWithXPath(md, "*//@xlink:href[starts-with(.,'#')]");
				
		Log.debug(Log.XLINK_PROCESSOR, "local xlink search returned "+xlinks.size()+" elements");

		// now all remote fragments have been added, process local xlinks (uncached)
		for (Attribute xlink : xlinks) {
			Element element = xlink.getParent(); 
			if (action.equals(ACTION_REMOVE)) {
				element.removeContent();
			} else {
				String idSearch = xlink.getValue().substring(1);
				Log.debug(Log.XLINK_PROCESSOR, "process local xlink '"+idSearch+"'");
				Element localFragment = null;
				try {
					localFragment = Xml.selectElement(md, "*//*[@id='"+idSearch+"']");
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
				}
			}
			cleanXLinkAttributes(element, action);
		}
	}

	//--------------------------------------------------------------------------
	private static void doXLink(String hrefUri, String idSearch, Attribute xlink, String action) { 
		Element element = xlink.getParent();

        // Don't process XLink for operatesOn
        List<String> excludedXlinkElements = new ArrayList<String>();
        excludedXlinkElements.add("operatesOn");

        if (excludedXlinkElements.contains(element.getName())) {
           return;
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
						Element remoteFragment = resolveXLink(hrefUri, idSearch);
						
						// Not resolved in cache or using href
						if (remoteFragment == null)
							return;
						
						searchXLink(remoteFragment, action);
						
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
