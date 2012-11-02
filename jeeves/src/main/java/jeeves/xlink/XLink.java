package jeeves.xlink;

import java.net.URL;

import org.jdom.Namespace;

/**
 * XLink allows elements to be inserted into XML documents 
 * in order to create and describe links between resources.
 * 
 * Jeeves support simple XLink. For simple XLink elements:
 * <ul>
 * 	<li>type (Required): simple</li>
 *  <li>href (Optional)</li>
 *  <li>role (Optional)</li>
 *  <li>arcrole (not used)</li>
 *  <li>title (Optional)</li>
 *  <li>show (Optional)</li>
 *  <li>actuate (Optional)</li>
 * </ul>
 * 
 * <p>Attribute <b>xlink:show</b> is optional and could be 
 * <ul>
 *  <li>embed (default if attribute is null or empty): means that the document pointed by the xlink:href attribute
 *  will be loaded by the XLinkHandler. On error, DOCUMENT ME!</li>
 *  <li>replace: means that the document pointed by the xlink:href attribute 
 *  will NOT be loaded. This kind of link is generally used on the XSLT to
 *  present an hyperlink using the xlink:title attribute.</li>
 * </ul>
 * </p>
 * 
 * Is this a better option http://svn.geotools.org/trunk/modules/unsupported/ogc/org.w3.xlink/src/org/w3/xlink/ ?
 * 
 * <p>Attributes <b>xlink:role|arcrole|actuate</b> are optional and not used.
 * 
 * 
 * @author fxprunayre
 *
 */
public class XLink {

    private String href;
	private String title;
	private String role;
	private String type;
	private String show;
	private String actuate;
	
	/**
	 * Prefix generaly used for XLink namespace
	 */
	public static final String NS_PREFIX_XLINK = "xlink";

	public static final String ROLE_EMBED = "embed";
	
	/**
	 * The XLink namespace defined by the specification
	 */
	public static final String NS_XLINK = "http://www.w3.org/1999/xlink";
	
	public static final Namespace NAMESPACE_XLINK = Namespace.getNamespace(NS_PREFIX_XLINK, NS_XLINK);
	
	public static final String HREF = "href";
	public static final String TITLE = "title";
	public static final String TYPE = "type";
	public static final String ROLE = "role";
	public static final String ARCROLE = "arcrole";
	public static final String ACTUATE = "actuate";
	public static final String SHOW = "show";
    public static final String SHOW_REPLACE = "replace";
    public final static String SHOW_EMBED = ROLE_EMBED;
	public static final String LOCAL_PROTOCOL = "local://";
	
	/**
	 * Create a simple XLink element
	 * 
	 * @param href	document URI (relative or absolute)
	 * @param title title of the document
	 * 
	 */
	public XLink (URL href, String title, String role) {
		this.href = href.toString();
		this.title = title;
		this.role = role;
		this.show = ROLE_EMBED;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getShow() {
		return show;
	}

	public void setShow(String show) {
		this.show = show;
	}

	public String getActuate() {
		return actuate;
	}

	public void setActuate(String actuate) {
		this.actuate = actuate;
	}
	
	
	
}
