package org.fao.geonet.services.gm03;

import org.apache.commons.lang.ArrayUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ISO19139CHEtoGM03small extends ISO19139CHEtoGM03Base {
    private static final String GML = "http://www.geocat.ch/2003/05/gateway/GML";

    public ISO19139CHEtoGM03small(File schemaLocation, String xslFilename) throws SAXException, TransformerConfigurationException {
        super(schemaLocation, xslFilename);
    }

    protected boolean wantIntermediate() {
        return false;
    }

    protected void flatten(Document source) throws FlattenerException {
    }

    protected void removeDuplicates(Document doc) throws FlattenerException {
    }

    protected String getGmlCoordinateNs() {
        return null;
    }

    protected Element createCoordinate(Document doc, String[] vals) {
        Element coordNode = doc.createElementNS(GML, "GML:pos");
        StringBuffer coords = new StringBuffer();
        for (int k = 0; k < vals.length; k++) {
            String val = vals[k];
            if(k>0) {
                coords.append(" ");
            }
            coords.append(val);
        }
        coordNode.setTextContent(coords.toString());
        return coordNode;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, FlattenerException, TransformerException {
        final String xslFilename = args[0];
        final String schemaFilename = args[1];
        final String[] xmlFilenames = (String[]) ArrayUtils.subarray(args, 2, args.length);

        File schemaLocation = null;
        if (!schemaFilename.equalsIgnoreCase("no")) {
            schemaLocation = new File(schemaFilename);
        }
        ISO19139CHEtoGM03small converter = new ISO19139CHEtoGM03small(schemaLocation, xslFilename);

        converter.convert(xmlFilenames, "ISO19139CHEtoGM03small.main");
    }
}