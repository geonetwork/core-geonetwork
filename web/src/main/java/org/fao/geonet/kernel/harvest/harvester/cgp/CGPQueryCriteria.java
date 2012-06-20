package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class to construct query criteria from expression(s).
 * <p/>
 * See KOGIS / GeoTask: geocat.ch Gateway Protocol protocol spec.
 *
 * @author Just van den Broecke
 */
public class CGPQueryCriteria
{

	/**
	 * Construct criteria.
	 */
	public CGPQueryCriteria()
	{
	}

	/**
	 * Add any expression.
	 *
	 * @param attribute GM03 attribute name
	 * @param operator  operator
	 * @param value     GM03 attribute value
	 */
	public CGPQueryCriteria addExpression(String attribute, String operator, Object value)
	{
		Element expressionElm = new Element("expression", CGPRequest.NAMESPACE_GCQ);
		addTextChild(expressionElm, "attribute", attribute);
		addTextChild(expressionElm, "operator", operator);
		if (value instanceof String)
		{
			addTextChild(expressionElm, "value", (String) value);
		} else if (value instanceof Element)
		{
			addElementChild(expressionElm, "value", (Element) value);
		}
		expressionElms.add(expressionElm);
		return this;
	}

	/**
	 * Add expression "bounding box".
	 *
	 * @param lonWest  westbound longitude
	 * @param latSouth southbound latitude
	 * @param lonEast  eastbound longitude
	 * @param latNorth northbound latitude
	 */
	public CGPQueryCriteria addBBoxExpression(String lonWest, String latSouth, String lonEast, String latNorth)
	{
		/*
	   <gcq:attribute>/MD_Metadata/identificationInfo/extent/geographicElement/polygon</gcq:attribute>
		<gcq:operator>WITHIN</gcq:operator>
		<gcq:value>
			<gml:Polygon srsName="EPSG:4326" xmlns:gml="http://www.geocat.ch/2003/05/gateway/GML">
				<gml:exteriorRing>
					<gml:pos>5.9643 47.7721</gml:pos>
					<gml:pos>10.5589 47.7721</gml:pos>
					<gml:pos>10.5589 45.8195</gml:pos>
					<gml:pos>5.9643 45.8195</gml:pos>
					<gml:pos>5.9643 47.7721</gml:pos>
				</gml:exteriorRing>
			</gml:Polygon>
		</gcq:value>

		 */

		Element polygonElm = new Element("Polygon", CGPRequest.NAMESPACE_GML);
		polygonElm.setAttribute("srsName", "EPSG:4326");
		Element exteriorRingElm = new Element("exteriorRing", CGPRequest.NAMESPACE_GML);
		addTextChild(exteriorRingElm, "pos", lonWest + " " + latNorth, CGPRequest.NAMESPACE_GML);
		addTextChild(exteriorRingElm, "pos", lonEast + " " + latNorth, CGPRequest.NAMESPACE_GML);
		addTextChild(exteriorRingElm, "pos", lonEast + " " + latSouth, CGPRequest.NAMESPACE_GML);
		addTextChild(exteriorRingElm, "pos", lonWest + " " + latSouth, CGPRequest.NAMESPACE_GML);
		addTextChild(exteriorRingElm, "pos", lonWest + " " + latNorth, CGPRequest.NAMESPACE_GML);
		polygonElm.addContent(exteriorRingElm);

		return addExpression("/MD_Metadata/identificationInfo/extent/geographicElement/polygon", "WITHIN", polygonElm);
	}

	/**
	 * Add expression "before specified date".
	 *
	 * @param dateStr a date string formatted  "yyyy-MM-ddThh:mm:ss" or "yyyy-MM-dd"
	 */
	public CGPQueryCriteria addBeforeDateExpression(String dateStr)
	{
		return addDateExpression("lt", dateStr);
	}

	/**
	 * Add expression "before far in the future date".
	 */
	public CGPQueryCriteria addBeforeDistantDateExpression()
	{
		return addBeforeDateExpression("2020-01-01");
	}

	/**
	 * Add expression "after specified date".
	 *
	 * @param dateStr a date string formatted  "yyyy-MM-dd'T'hh:mm:ss" or "yyyy-MM-dd"
	 */
	public CGPQueryCriteria addAfterDateExpression(String dateStr)
	{
		return addDateExpression("gt", dateStr);
	}

	/**
	 * Add dateStamp expression with operator and specified date.
	 *
	 * @param operator a date operator like "gt" or "lt"
	 * @param dateStr  a date string formatted  "yyyy-MM-dd'T'hh:mm:ss" or "yyyy-MM-dd"
	 */
	public CGPQueryCriteria addDateExpression(String operator, String dateStr)
	{
		if (dateStr == null || dateStr.length() == 0)
		{
			return this;
		}

		if (dateStr.length() == 10)
		{
			// Only date no time, e.g. 2009-02-27
			dateStr += "T00:00:00";
		}
		return addExpression("/MD_Metadata/dateStamp", operator, dateStr);
	}

	/**
	 * Add expression for wildcard text.
	 *
	 * @param value free text value (without %)
	 */
	public CGPQueryCriteria addFreeTextExpression(String value)
	{
		if (value == null || value.length() == 0)
		{
			return this;
		}

		return addExpression("/MD_Metadata/*", "like", "%" + value + "%");
	}

	/**
	 * Util method to add Element child to an Element.
	 *
	 * @param parentElm parent element
	 * @param name      child element tag name
	 * @param value     child Element Element content value
	 * @return parent Element with added text child
	 */
	private static Element addElementChild(Element parentElm, String name, Element value)
	{
		Element childElm = new Element(name, CGPRequest.NAMESPACE_GCQ);
		childElm.addContent(value);
		return parentElm.addContent(childElm);
	}

	/**
	 * Util method to add text child to an Element.
	 *
	 * @param parentElm parent element
	 * @param name      pchild element tag name
	 * @param value     child Element text content value
	 * @return parent Element with added text child
	 */
	private static Element addTextChild(Element parentElm, String name, String value)
	{
		return addTextChild(parentElm, name, value, CGPRequest.NAMESPACE_GCQ);
	}

	/**
	 * Util method to add text child to an Element.
	 *
	 * @param parentElm parent element
	 * @param name      pchild element tag name
	 * @param value     child Element text content value
	 * @param namespace namespace to use for child element
	 * @return parent Element with added text child
	 */
	private static Element addTextChild(Element parentElm, String name, String value, Namespace namespace)
	{
		Element textChildElm = new Element(name, namespace);
		textChildElm.setText(value);
		return parentElm.addContent(textChildElm);
	}

	/**
	 * Convert expression Element list to valid CGP Query criteria Element.
	 *
	 * @return CGP Query criteria Element
	 */
	public Element toElement()
	{
		Element criteriaElm = new Element("criteria", CGPRequest.NAMESPACE_GCQ);
		Element expressionElm;
		for (int i = 0; i < expressionElms.size(); i++)
		{
			expressionElm = expressionElms.get(i);
			if (i == 0)
			{
				// First expression is always added directly to parent criteria Element
				criteriaElm.addContent(expressionElm);
			} else
			{
				// We have more than one expressions.
				// Add concatenated expression, using AND-ing.
				Element concatExpressionElm = new Element("concatenatedExpression", CGPRequest.NAMESPACE_GCQ);
				addTextChild(concatExpressionElm, "concatenationOperator", "and");
				concatExpressionElm.addContent(expressionElm);
				criteriaElm.addContent(concatExpressionElm);
			}
		}
		return criteriaElm;
	}

	/**
	 * Convert expression Element list to valid CGP Query stringed Element.
	 *
	 * @return CGP Query criteria Element as String
	 */
	public String toString()
	{
		return Xml.getString(toElement());
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	/**
	 * List of query expression Elements.
	 */
	private List<Element> expressionElms = new ArrayList(2);
}
