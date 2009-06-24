package org.fao.geonet.test;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Objects of this clas represent a path to an element,
 * attribute or text node in a document.
 * <p/>
 * This class includes methods to create a path based on
 * an element and methods to retrieve the Element or text
 * value that a path leads to from a document.
 * TODO: this may be done (partially) with XPath.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class Path
{

	private List elements;

	private Path()
	{
		elements = new ArrayList();
	}

	/**
	 * Create a Path to the given Element from the root of
	 * the document that the element belongs to.
	 *
	 * @param elm the Element to create a Path to.
	 * @return a new Path to the given Element.
	 */
	public static Path createPathToElement(Element elm)
	{
		Path path = new Path();
		do
		{
			PathElement pe = new PathElement(elm);
			path.elements.add(0, pe);
		}
		while ((elm = (Element) elm.getParent()) != null);
		log("Created path: " + path.toString());
		return path;
	}

	/**
	 * Create a Path to the given attribute of the given element.
	 *
	 * @param elm
	 * @param attr
	 * @return the created Path
	 */
	public static Path createPathToAttribute(Element elm, String attr)
	{
		Path path = createPathToElement(elm);
		PathElement pe = new PathElement(attr);
		path.elements.add(pe);
		log("Created path: " + path.toString());
		return path;
	}

	/**
	 * Create a path to the text node of the given element.
	 *
	 * @param elm
	 * @return
	 */
	public static Path createPathToText(Element elm)
	{
		Path path = createPathToElement(elm);
		PathElement pe = new PathElement();
		path.elements.add(pe);
		log("Created path: " + path.toString());
		return path;
	}

	/**
	 * Get the value (attribute value or text node) that this Path refers to,
	 * using the given element as the context. Return null if this path leads
	 * nowhere in the given element or if this Path leads to an element.
	 *
	 * @param elm the context element.
	 * @return the attribute value or text
	 */
	public String findValue(Element elm)
	{
		log("Looking for " + toString() + " in element " + elm.getName());
		if (elements.size() == 0)
		{
			return null;
		} else if (((PathElement) elements.get(0)).matches(elm))
		{
			if (elements.size() == 1)
			{
				return null; // not a value.
			} else
			{
				for (int i = 1; i < elements.size() - 1; i++)
				{
					PathElement pe = (PathElement) elements.get(i);
					elm = pe.findElement(elm);
					if (elm == null)
					{
						return null;
					}
				}
				PathElement pe = (PathElement) elements.get(elements.size() - 1);
				return pe.findValue(elm);
			}
		} else
		{
			return null;
		}
	}

	/**
	 * Get the element that this Path refers to, using the given element as context.
	 * Return null of no such element can be found or if this path points to
	 * an attribute or a text node.
	 *
	 * @param elm the context of the search.
	 * @return the Element that this Path points to.
	 */
	public Element findElement(Element elm)
	{
		log("Looking for " + toString() + " in element " + elm.getName());
		if (elements.size() == 0)
		{
			return null;
		} else if (((PathElement) elements.get(0)).matches(elm))
		{
			if (elements.size() == 1)
			{
				return elm;
			} else
			{
				for (int i = 1; i < elements.size(); i++)
				{
					PathElement pe = (PathElement) elements.get(i);
					elm = pe.findElement(elm);
					if (elm == null)
					{
						break;
					}
				}
				return elm;
			}
		} else
		{
			return null;
		}
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		toString(buf);
		return buf.toString();
	}

	void toString(StringBuffer buf)
	{
		for (Iterator iter = elements.iterator(); iter.hasNext();)
		{
			PathElement element = (PathElement) iter.next();
			element.toString(buf);
			if (element == elements.get(0) && !element.isRoot())
			{
				buf.deleteCharAt(0);
			}
		}
	}

	private static void log(String message)
	{
		//System.out.println(message);
	}

}

