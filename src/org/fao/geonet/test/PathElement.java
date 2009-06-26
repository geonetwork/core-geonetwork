package org.fao.geonet.test;

import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Iterator;
import java.util.List;

/**
 * A PathElement is an element of a Path.
 * <p/>
 * TODO: this may be done partially with XPath.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */

public class PathElement
{

	public static final int ATTRIBUTE = 1;
	public static final int ELEMENT = 2;
	public static final int TEXT = 3;

	/**
	 * The type.
	 */
	private int type;
	/**
	 * The tag name or attribute name.
	 */
	private String name;
	private Namespace ns;
	/**
	 * The index of an element amoung siblings with the same name.
	 */
	private int index;
	/**
	 * Whether this PathElement represents a root element.
	 */
	private boolean isRoot;

	/**
	 * Create a PathElement representing an Element.
	 *
	 * @param anElement the Element to represent.
	 */
	PathElement(Element anElement)
	{
		type = ELEMENT;
		name = anElement.getName();
		ns = anElement.getNamespace();

		if (anElement.getParent() == null)
		{
			isRoot = true;
		} else
		{
			// find the index of this element among other elements of the same name.
			for (Iterator iter = ((Element) anElement.getParent()).getChildren().iterator(); iter.hasNext();)
			{
				Element element = (Element) iter.next();
				if (element.getName().equals(name))
				{
					if (element != anElement)
					{
						index++;
					} else
					{
						break;
					}
				}
			}
		}

	}

	/**
	 * Create a PathElement representing an attribute.
	 *
	 * @param attr the attribute name.
	 */
	PathElement(String attr)
	{
		type = ATTRIBUTE;
		name = attr;
	}

	/**
	 * Create a PathElement representing a text node.
	 */
	PathElement()
	{
		type = TEXT;
	}

	/**
	 * Determine whether the given Element matches this PathElement.
	 *
	 * @param elm the Element to scrutinize.
	 * @return true if the Element matches this PathElement, false otherwise.
	 */
	public boolean matches(Element elm)
	{
		if (type != ELEMENT || !elm.getName().equals(name))
		{
			return false;
		}

		if (elm.getParent() == null)
		{
			return isRoot;
		} else
		{
			int count = 0;
			for (Iterator iter = ((Element) elm.getParent()).getChildren().iterator(); iter.hasNext();)
			{
				Element element = (Element) iter.next();
				if (element.getName().equals(name))
				{
					if (count == index)
					{
						return true;
					} else
					{
						count++;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Find the Element that this PathElement points to in the context
	 * of the given Element.
	 *
	 * @param context the Element to search in.
	 * @return a child element of the context element that matches this PathElement.
	 */
	public Element findElement(Element context)
	{
		if (type != ELEMENT)
		{
			return null;
		}

		List children = context.getChildren(name);
		if (ns != null) {
			children = context.getChildren(name, ns);
		}

		if (children.size() > index)
		{
			return (Element) children.get(index);
		}
		return null;
	}

	/**
	 * Find the attribute or text value that this PathElement points to.
	 *
	 * @param context the Element to search in.
	 * @return the text value or an attribute value of the context node if
	 *         it matches this PathElement.
	 */
	public String findValue(Element context)
	{
		if (type == ELEMENT)
		{
			return null;
		}

		if (type == TEXT)
		{
			return context.getText();
		}

		if (type == ATTRIBUTE)
		{
			return context.getAttributeValue(name);
		}

		return null;
	}


	/**
	 * @return Returns the index.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @return Returns the isRoot.
	 */
	public boolean isRoot()
	{
		return isRoot;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		toString(buf);
		return buf.toString();
	}

	void toString(StringBuffer buf)
	{
		if (type == ELEMENT)
		{
			buf.append('/');
			buf.append(name);
		}
		if (type == ATTRIBUTE)
		{
			buf.append("@");
			buf.append(name);
		}
		if (type == TEXT)
		{
			buf.append("/text()");
		}
	}
}

