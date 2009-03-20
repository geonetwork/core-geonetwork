package org.fao.geonet.kernel.mef;

import java.io.File;

import jeeves.exceptions.BadFormatEx;
import jeeves.utils.Xml;

import org.jdom.Element;

public class XMLFileVisitor implements FileVisitor {
	
	// --------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// --------------------------------------------------------------------------

	public void visit(File mefFile, MEFVisitor v) throws Exception {
		Element info = handleXml(mefFile, v);
	}

	// --------------------------------------------------------------------------

	public Element handleXml(File mefFile, MEFVisitor v)
			throws Exception {
				
		Element md = null;
		Element info = null;

		md = Xml.loadFile(mefFile);
		info = new Element("info");

		if (md == null)
			throw new BadFormatEx("Missing xml metadata file .");

		v.handleMetadata(md);
		v.handleInfo(info);

		return info;
	}
	
	public void handleBin(File mefFile, MEFVisitor v, Element info) throws Exception {}
}
