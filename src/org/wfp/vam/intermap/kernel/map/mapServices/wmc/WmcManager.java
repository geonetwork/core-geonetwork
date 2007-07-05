/**
 * This class represent a Web Map Context 1.0.0
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc;

import java.util.*;

import org.jdom.*;

import jeeves.utils.*;

import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;

public class WmcManager
{
	private Element context;
	private BoundingBox bb;
	private List lServices;
	private Iterator i; // lServices iterator

	public WmcManager(Element context) {
		this.context = context;

		Namespace ns = org.jdom.Namespace.getNamespace("http://www.opengis.net/context");

		System.out.println("\n\ncontext:\n" + Xml.getString(context));

		System.out.println("\n\nLayerList\n\n" + context.getChild("LayerList", ns)); // DEBUG

		List t = context.getChildren(); // DEBUG
		for (Iterator i = t.iterator(); i.hasNext(); System.out.println((Element)i.next())); // DEBUG

		lServices = context.getChild("LayerList", ns).getChildren("Layer", ns);
		i = lServices.iterator();
	}

	/**
	 * Returns the BoundingBox defined in the context. For now assumes that
	 * the coordinates are in degrees.
	 *
	 * @param    context             an Element
	 *
	 * @return   a BoundingBox
	 *
	 */
	public BoundingBox getBoundingBox(Element context) {
		Element elBb = context.getChild("BoundingBox");

		// for now we suppose that the BoundingBox coordinates are in degrees...
		int minx = Integer.parseInt(elBb.getAttributeValue("minx"));
		int miny = Integer.parseInt(elBb.getAttributeValue("miny"));
		int maxx = Integer.parseInt(elBb.getAttributeValue("maxx"));
		int maxy = Integer.parseInt(elBb.getAttributeValue("maxy"));

		return new BoundingBox(maxx, minx, maxx, minx);
	}

	public boolean hasNextLayer() { return i.hasNext(); }

	public Element nextLayer() { return (Element)i.next(); }

}

