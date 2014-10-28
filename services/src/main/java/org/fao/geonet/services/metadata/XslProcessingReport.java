//=============================================================================
//===  Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import jeeves.constants.Jeeves;
import jeeves.services.ReadWriteController;

import org.fao.geonet.Util;
import org.fao.geonet.domain.ISODate;
import org.jdom.Element;
import org.opengis.annotation.XmlElement;

/**
 * Batch processing report containing information about the current process and
 * the metadata records affected.
 * 
 * @author francois
 * 
 */
@XmlRootElement(name = "response")
@XmlType(propOrder = { "processId", "startDate", "reportDate", "running",
		"totalRecords", "processedRecords", "nullRecords", "done",
		"notProcessFound", "notOwner", "notFound", "metadataErrorReport" })
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlSeeAlso(ListErrorReport.class)
public class XslProcessingReport {

	@XmlAttribute(name = "process")
	protected final String processId;
	@XmlAttribute
	protected final ISODate startDate;

	/**
	 * The total number of records to process
	 */
	protected int totalRecords = 0;
	/**
	 * The number of records processed when the report was generated
	 */
	@XmlAttribute
	protected int processedRecords = 0;
	/**
	 * The number of records when a null metadata identifier is processed (may
	 * happen when a record is in the selection but was deleted after the
	 * selection)
	 */
	@XmlAttribute
	protected int nullRecords = 0;

	/**
	 * The list of record identifiers successfully processed
	 */
	protected Set<Integer> metadata = new HashSet<Integer>();

	/**
	 * The list of record identifiers not found (may be deleted)
	 */
	protected Set<Integer> notFound = new HashSet<Integer>();

	/**
	 * The list of records identifiers the user who starts the process is not
	 * allowed to edit
	 */
	protected Set<Integer> notEditable = new HashSet<Integer>();

	/**
	 * The list of record identifiers for which the schema does not provide
	 * process with that process id
	 */
	protected Set<Integer> noProcessFound = new HashSet<Integer>();
	/**
	 * The list of records with error and exception details
	 */
	protected Map<Integer, Exception> metadataErrors = new HashMap<Integer, Exception>();

	/**
	 * Initialize a report and its start date.
	 * 
	 * @param processId
	 */
	public XslProcessingReport(String processId) {
		this.processId = processId;
		this.startDate = new ISODate();
	}

	public XslProcessingReport() {
		this.processId = null;
		this.startDate = new ISODate();
	}

	@XmlTransient
	protected synchronized boolean isProcessing() {
		return totalRecords != processedRecords;
	}

	@XmlAttribute
	public String getReportDate() {
		return new ISODate().toString();
	}

	@XmlAttribute
	public String getRunning() {
		return Boolean.toString(isProcessing());
	}

	/**
	 * Return report as XML
	 * 
	 * @return
	 */
	public synchronized Element toXml() {
		Element xmlReport = new Element(Jeeves.Elem.RESPONSE);

		xmlReport.setAttribute("process", processId);

		xmlReport.addContent(new Element("done").setText(metadata.size() + ""));
		xmlReport.addContent(new Element("notProcessFound")
				.setText(noProcessFound.size() + ""));
		xmlReport.addContent(new Element("notOwner").setText(notEditable.size()
				+ ""));
		xmlReport.addContent(new Element("notFound").setText(notFound.size()
				+ ""));

		xmlReport.setAttribute("startDate", startDate.toString());
		xmlReport.setAttribute("reportDate", new ISODate().toString());
		xmlReport.setAttribute("running", String.valueOf(isProcessing()));
		xmlReport.setAttribute("totalRecords", totalRecords + "");
		xmlReport.setAttribute("processedRecords", processedRecords + "");
		xmlReport.setAttribute("nullRecords", nullRecords + "");

		Element mdErrorReport = new Element("metadataErrorReport");
		for (Entry<Integer, Exception> e : metadataErrors.entrySet()) {
			Element info = new Element("metadata");
			info.setAttribute("id", e.getKey() + "");
			info.addContent(new Element("message").setText(e.getValue()
					.getMessage()));
			info.addContent(new Element("stack").setText(Util.getStackTrace(e
					.getValue())));
			mdErrorReport.addContent(info);
		}
		xmlReport.addContent(mdErrorReport);

		return xmlReport;
	}

	@XmlAnyElement(lax = true)
	public ListErrorReport getMetadataErrorReport() {

		return new ListErrorReport(this.metadataErrors);
	}

	public void setMetadataErrorReport(ListErrorReport map) {
		// no
	}

	public Integer getDone() {
		return metadata.size();
	}

	public void setDone(Integer done) {
		// no
	}

	public Integer getNotProcessFound() {
		return noProcessFound.size();
	}

	public void setNotProcessFound(Integer i) {
		// no
	}

	public Integer getNotOwner() {
		return notEditable.size();
	}

	public void setNotOwner(Integer i) {
		// no
	}

	public Integer getNotFound() {
		return notFound.size();
	}

	public void setNotFound(Integer i) {
		// no
	}

	public synchronized int getNullRecords() {
		return nullRecords;
	}

	public synchronized void incrementNullRecords() {
		this.nullRecords++;
	}

	public synchronized String getProcessId() {
		return processId;
	}

	public synchronized ISODate getStartDate() {
		return startDate.clone();
	}

	@XmlAttribute
	public synchronized int getTotalRecords() {
		return totalRecords;
	}

	public synchronized void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public synchronized int getProcessedRecords() {
		return processedRecords;
	}

	public synchronized void incrementProcessedRecords() {
		this.processedRecords++;
	}

	public synchronized void addMetadataId(int metadataId) {
		this.metadata.add(metadataId);
	}

	public synchronized void addNotFoundMetadataId(int metadataId) {
		this.notFound.add(metadataId);
	}

	public synchronized int getNotFoundMetadataCount() {
		return this.notFound.size();
	}

	public synchronized void addNotEditableMetadataId(int metadataId) {
		this.notEditable.add(metadataId);
	}

	public synchronized int getNotEditableMetadataCount() {
		return this.notEditable.size();
	}

	public synchronized void addNoProcessFoundMetadataId(int metadataId) {
		this.noProcessFound.add(metadataId);
	}

	public synchronized int getNoProcessFoundCount() {
		return this.noProcessFound.size();
	}

	public synchronized void addMetadataError(int metadataId, Exception error) {
		this.metadataErrors.put(metadataId, error);
	}

}

@XmlRootElement(name="metadataErrorReport")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlSeeAlso(ErrorReport.class)
class ListErrorReport {

	protected Map<Integer, Exception> metadataErrors = new HashMap<Integer, Exception>();

	public ListErrorReport() {

	}

	public ListErrorReport(Map<Integer, Exception> metadataErrors) {
		this.metadataErrors = metadataErrors;
	}

	@XmlAnyElement(lax = true)
	public java.util.List<JAXBElement<? extends ErrorReport>> getMetadataErrorReport() {

		java.util.List<JAXBElement<? extends ErrorReport>> res = new LinkedList<JAXBElement<? extends ErrorReport>>();
		for (Entry<Integer, Exception> e : metadataErrors.entrySet()) {
			ErrorReport error = new ErrorReport(e.getKey(), e.getValue());
			res.add(new JAXBElement<ErrorReport>(new QName("metadata"),
					ErrorReport.class, error));
		}
		
		return res;
	}

	public void setMetadataErrorReport(
			List<JAXBElement<? extends ErrorReport>> map) {
		// no
	}
}

@XmlElement(value = "metadata")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
class ErrorReport {

	private String id;
	private String message;
	private String stack;

	public ErrorReport() {

	}

	public ErrorReport(Integer id, Exception e) {

		this.id = id.toString();
		this.message = e.getMessage();
		this.stack = Util.getStackTrace(e);
	}

	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStack() {
		return stack;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}
}