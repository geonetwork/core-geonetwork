package org.fao.geonet.kernel.reusable;

import static java.lang.String.format;
import static org.junit.Assert.*;

import java.io.IOException;

import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

public class UtilsTest {

	String deFrKeywordTemplate = "<gmd:MD_Keywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"><gmd:keyword xsi:type=\"gmd:PT_FreeText_PropertyType\"><gmd:PT_FreeText><gmd:textGroup><gmd:LocalisedCharacterString locale=\"%s\">%s</gmd:LocalisedCharacterString></gmd:textGroup><gmd:textGroup><gmd:LocalisedCharacterString locale=\"%s\">%s</gmd:LocalisedCharacterString></gmd:textGroup></gmd:PT_FreeText></gmd:keyword></gmd:MD_Keywords>";
	@Test
	public void testEqualElem() throws IOException, JDOMException {
		Element frDeKeyword = Xml.loadString(format(deFrKeywordTemplate,"#FR","Neuchatel", "#DE","Neuenburg"),false);
		Element deFrKeyword = Xml.loadString(format(deFrKeywordTemplate,"#DE","Neuenburg", "#FR","Neuchatel"),false);
		Element duplicateTranslations = Xml.loadString(format(deFrKeywordTemplate,"#FR","Neuchatel", "#FR","Neuchatel"),false);
		Element wrongTranslations = Xml.loadString(format(deFrKeywordTemplate,"#FR","Neuenburg", "#DE","Neuchatel"),false);

		assertTrue(Utils.equalElem(frDeKeyword, deFrKeyword));
		assertTrue(Utils.equalElem(deFrKeyword, deFrKeyword));
		assertFalse(Utils.equalElem(deFrKeyword, duplicateTranslations));
		assertFalse(Utils.equalElem(duplicateTranslations, deFrKeyword));
		assertFalse(Utils.equalElem(deFrKeyword, wrongTranslations));
	}

	@Test
	public void testEqualAtts() throws IOException, JDOMException {
		Element elem1 = Xml.loadString("<gmd:MD_Keywords locale=\"#FR\" xsi:type=\"gmd:PT_FreeText_PropertyType\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"></gmd:MD_Keywords>",false);
		Element elem2 = Xml.loadString("<gmd:MD_Keywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" locale=\"#FR\"></gmd:MD_Keywords>",false);
		Element elem3 = Xml.loadString("<gmd:MD_Keywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" locale=\"#DE\"></gmd:MD_Keywords>",false);
		Element elem4 = Xml.loadString("<gmd:MD_Keywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"></gmd:MD_Keywords>",false);
		Element elem5 = Xml.loadString("<gmd:MD_Keywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" skos:locale=\"#FR\"></gmd:MD_Keywords>",false);
		Element elem6 = Xml.loadString("<gmd:MD_Keywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" ll=\"#FR\"></gmd:MD_Keywords>",false);
		
		assertTrue(Utils.equalAtts(elem1, elem2));
		assertFalse(Utils.equalAtts(elem1, elem3));
		assertFalse(Utils.equalAtts(elem1, elem4));
		assertFalse(Utils.equalAtts(elem1, elem5));
		assertFalse(Utils.equalAtts(elem1, elem6));
	}

}
