/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.processing.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.util.*;

/**
 * A report about metadata validation processing.
 *
 * <p>This class extends MetadataProcessingReport to provide specific functionality for tracking
 * validation results of metadata records. It maintains collections of valid metadata, invalid metadata,
 * and metadata with warnings, along with their associated validation messages.</p>
 *
 * <p>The report can handle both schema (XSD) validation errors and schematron validation errors/warnings.</p>
 */
@JsonPropertyOrder({
    "uuid", "metadata", "validMetadata", "invalidMetadata", "metadataWithWarnings",
    "validationErrors", "validationWarnings", "numberOfRecords", "numberOfRecordsProcessed", "numberOfValidRecords",
    "numberOfInvalidRecords", "numberOfRecordsWithValidationWarnings", "numberOfNullRecords",
    "numberOfRecordsNotEditable", "startIsoDateTime", "endIsoDateTime", "ellapsedTimeInSeconds",
    "totalTimeInSeconds", "running", "type"
})
// Exclude unused properties from JSON serialization
@JsonIgnoreProperties({
    "errors", "infos", "metadataErrors", "metadataInfos", "numberOfRecordsUnchanged",
    "numberOfRecordsWithErrors", "numberOfRecordNotFound"
})
public class MetadataValidationProcessingReport extends MetadataProcessingReport {

    /**
     * Map of valid metadata records keyed by metadata ID.
     * Contains metadata that passed all validation checks.
     */
    protected Map<Integer, InfoReport> validMetadata = new HashMap<>();

    /**
     * Map of invalid metadata records keyed by metadata ID.
     * Contains metadata that failed one or more required validation checks.
     */
    protected Map<Integer, InfoReport> invalidMetadata = new HashMap<>();

    /**
     * Map of metadata records with warnings keyed by metadata ID.
     * Contains metadata that failed one or more non-required validation checks.
     */
    protected Map<Integer, InfoReport> metadataWithWarnings = new HashMap<>();

    /**
     * Map of validation errors for metadata records keyed by metadata ID.
     * Each metadata record can have multiple SchematronValidationReports containing errors.
     */
    protected Map<Integer, List<SchematronValidationReport>> metadataErrors = new HashMap<>();

    /**
     * Map of validation warnings for metadata records keyed by metadata ID.
     * Each metadata record can have multiple SchematronValidationReports containing warnings.
     */
    protected Map<Integer, List<SchematronValidationReport>> metadataWarnings = new HashMap<>();

    /**
     * Default constructor that initializes the validation processing report.
     */
    public MetadataValidationProcessingReport() {
        super();
    }

    /**
     * Gets the map of valid metadata records.
     *
     * @return Map of valid metadata records keyed by metadata ID
     */
    public Map<Integer, InfoReport> getValidMetadata() { return validMetadata; }

    /**
     * Gets the map of invalid metadata records.
     *
     * @return Map of invalid metadata records keyed by metadata ID
     */
    public Map<Integer, InfoReport> getInvalidMetadata() { return invalidMetadata; }

    /**
     * Gets the map of metadata records that have validation warnings.
     *
     * @return Map of metadata records with warnings keyed by metadata ID
     */
    public Map<Integer, InfoReport> getMetadataWithWarnings() { return metadataWithWarnings; }

    /**
     * Gets the map of validation errors for metadata records.
     *
     * @return Map of validation errors for metadata records keyed by metadata ID
     */
    public Map<Integer, List<SchematronValidationReport>> getValidationErrors() {
        return metadataErrors;
    }

    /**
     * Gets the map of validation warnings for metadata records.
     *
     * @return Map of validation warnings for metadata records keyed by metadata ID
     */
    public Map<Integer, List<SchematronValidationReport>> getValidationWarnings() {
        return metadataWarnings;
    }

    /**
     * Gets the number of valid metadata records in the report.
     *
     * @return The count of metadata records that passed all validation checks
     */
    public synchronized int getNumberOfValidRecords() {
        return this.validMetadata.size();
    }

    /**
     * Gets the number of invalid metadata records in the report.
     *
     * @return The count of metadata records that failed validation
     */
    public synchronized int getNumberOfInvalidRecords() {
        return this.invalidMetadata.size();
    }

    /**
     * Gets the number of metadata records with validation warnings.
     *
     * @return The count of metadata records that have validation warnings
     */
    public synchronized int getNumberOfRecordsWithValidationWarnings() {
        return this.metadataWithWarnings.size();
    }

    /**
     * Adds a metadata record to the list of valid metadata.
     *
     * @param metadataId The ID of the metadata record
     * @param metadataUUID The UUID of the metadata record
     * @param draft Whether the metadata record is a draft
     * @param approved Whether the metadata record is approved
     */
    public synchronized void addValidMetadata(int metadataId, String metadataUUID, boolean draft, boolean approved) {
        InfoReport infoReport = new InfoReport("Is valid");
        infoReport.setUuid(metadataUUID);
        infoReport.setDraft(draft);
        infoReport.setApproved(approved);
        this.validMetadata.put(metadataId, infoReport);
    }

    /**
     * Convenience method that adds a metadata record to the list of valid metadata.
     *
     * @param metadata The metadata record object
     */
    public void addValidMetadata(AbstractMetadata metadata) {
        addValidMetadata(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()));
    }

    /**
     * Adds a metadata record to the list of invalid metadata.
     * This is used when a metadata record fails one or more required validation checks.
     *
     * @param metadataId The ID of the metadata record
     * @param metadataUUID The UUID of the metadata record
     * @param draft Whether the metadata record is a draft
     * @param approved Whether the metadata record is approved
     */
    public synchronized void addInvalidMetadata(int metadataId, String metadataUUID, boolean draft, boolean approved) {
        InfoReport infoReport = new InfoReport("Is invalid");
        infoReport.setUuid(metadataUUID);
        infoReport.setDraft(draft);
        infoReport.setApproved(approved);
        this.invalidMetadata.put(metadataId, infoReport);
    }

    /**
     * Convenience method that adds a metadata record to the list of invalid metadata.
     *
     * @param metadata The metadata record object
     */
    public void addInvalidMetadata(AbstractMetadata metadata) {
        addInvalidMetadata(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()));
    }

    /**
     * Adds a metadata record to the list of metadata with warnings.
     * This is used when a metadata record passes all required validation checks
     * but has one or more non-required validation issues (warnings).
     *
     * @param metadataId The ID of the metadata record
     * @param metadataUUID The UUID of the metadata record
     * @param draft Whether the metadata record is a draft
     * @param approved Whether the metadata record is approved
     */
    public synchronized void addMetadataWithWarnings(int metadataId, String metadataUUID, boolean draft, boolean approved) {
        InfoReport infoReport = new InfoReport("Has warnings");
        infoReport.setUuid(metadataUUID);
        infoReport.setDraft(draft);
        infoReport.setApproved(approved);
        this.metadataWithWarnings.put(metadataId, infoReport);
    }

    /**
     * Convenience method that adds a metadata record to the list of metadata with warnings.
     *
     * @param metadata The metadata record object
     */
    public void addMetadataWithWarnings(AbstractMetadata metadata) {
        addMetadataWithWarnings(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()));
    }

    /**
     * Adds a validation message (error or warning) to the report for a specific metadata record.
     * This method is responsible for tracking validation messages and updating the metadata status
     * (invalid or with warnings) based on the schematron requirement level.
     *
     * @param metadataId The ID of the metadata record
     * @param metadataUUID The UUID of the metadata record
     * @param draft Whether the metadata record is a draft
     * @param approved Whether the metadata record is approved
     * @param message The validation message text
     * @param patternTitle The title of the schematron pattern that triggered the message
     * @param schematronName The name of the schematron rule set that generated the message
     * @param schematronRequirement The requirement level of the schematron rule (REQUIRED for errors, REPORT_ONLY for warnings)
     */
    public synchronized void addValidationMessage(int metadataId, String metadataUUID, boolean draft, boolean approved,
                                                  String message, String patternTitle, String schematronName, SchematronRequirement schematronRequirement) {

        Map<Integer, List<SchematronValidationReport>> targetMap =
            (schematronRequirement == SchematronRequirement.REQUIRED) ? metadataErrors : metadataWarnings;

        List<SchematronValidationReport> schematronValidationReports =
                targetMap.computeIfAbsent(metadataId, k -> new ArrayList<>());

        SchematronValidationReport schematronValidationReport = schematronValidationReports
                .stream()
                .filter(svr -> schematronName.equals(svr.getSchematron()))
                .findFirst()
                .orElseGet(() -> {
                    SchematronValidationReport newSchematronValidationReport = new SchematronValidationReport(schematronName);
                    schematronValidationReports.add(newSchematronValidationReport);
                    return newSchematronValidationReport;
                });

        schematronValidationReport.addMessage(patternTitle, message);

        if (schematronRequirement == SchematronRequirement.REQUIRED && !invalidMetadata.containsKey(metadataId)) {
            addInvalidMetadata(metadataId, metadataUUID, draft, approved);
        } else if (schematronRequirement == SchematronRequirement.REPORT_ONLY && !metadataWithWarnings.containsKey(metadataId)) {
            addMetadataWithWarnings(metadataId, metadataUUID, draft, approved);
        }
    }

    /**
     * Convenience method that adds a validation message for an AbstractMetadata object.
     * Delegates to the more detailed addValidationMessage method after extracting metadata properties.
     *
     * @param metadata The metadata record object
     * @param message The validation message text
     * @param patternTitle The title of the schematron pattern that triggered the message
     * @param schematronName The name of the schematron rule set that generated the message
     * @param schematronRequirement The requirement level of the schematron rule (REQUIRED for errors, REPORT_ONLY for warnings)
     */
    public void addValidationMessage(AbstractMetadata metadata, String message, String patternTitle, String schematronName, SchematronRequirement schematronRequirement) {
        addValidationMessage(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()), message, patternTitle, schematronName, schematronRequirement);
    }

    /**
     * Processes an XML validation report for a metadata record and extracts all validation messages
     * that match the specified requirement level.
     *
     * <p>This method handles both XSD schema validation errors and schematron validation messages.
     * It first restructures the schematron report to have a more hierarchical structure, then
     * extracts validation messages and adds them to the report.</p>
     *
     * @param record The metadata record being validated
     * @param schemaTronReport The XML validation report containing validation results
     * @param requirement The requirement level to process (REQUIRED for errors, REPORT_ONLY for warnings)
     * @throws JDOMException If there is an error processing the XML validation report
     */
    public synchronized void addAllReportsMatchingRequirement(AbstractMetadata record, Element schemaTronReport, SchematronRequirement requirement) throws JDOMException {
        List<Namespace> namespaces = new ArrayList<>();
        namespaces.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
        namespaces.add(Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl"));

        if (SchematronRequirement.REQUIRED.equals(requirement)) {
            // Extract all the xsd errors
            List<?> xsdErrors = Xml.selectNodes(
                schemaTronReport,
                "geonet:xsderrors/geonet:error/geonet:message[normalize-space(.) != '']",
                namespaces);

            // Add each xsd error to the report
            for (Object xsdError : xsdErrors) {
                String message = Xml.selectString((Element) xsdError, "normalize-space(.)", namespaces);

                this.addValidationMessage(
                    record,
                    message,
                    null,
                    "XSD",
                    SchematronRequirement.REQUIRED);
            }
        }

        List<?> schematronReports = Xml.selectNodes(
            schemaTronReport,
            String.format(
                "geonet:schematronerrors/geonet:report[@geonet:required = '%s']",
                requirement.toString()
            ),
            namespaces);

        for (Object schematronReportObject : schematronReports) {
            Element schematronReport = (Element) schematronReportObject;
            String schematronName = schematronReport.getAttributeValue("rule", namespaces.get(0)); // Get the geonet:rule attribute

            List<?> patterns = Xml.selectNodes(
                schematronReport,
                "svrl:schematron-output/svrl:active-pattern",
                namespaces
            );

            for (Object patternObject : patterns) {
                Element patternElem = (Element) patternObject;
                String patternTitle = patternElem.getAttributeValue("name");

                List<?> messages = Xml.selectNodes(
                    patternElem,
                    "svrl:fired-rule/svrl:failed-assert/svrl:text[normalize-space(.) != '']",
                    namespaces
                );

                for (Object messageObject : messages) {
                    String message = Xml.selectString((Element) messageObject, "normalize-space(.)", namespaces);

                    this.addValidationMessage(
                        record,
                        message,
                        patternTitle,
                        schematronName,
                        requirement);
                }
            }

            List<?> messages = Xml.selectNodes(schematronReport,
                "geonet:schematronVerificationError[normalize-space(.) != '']",
                namespaces);

            for (Object messageObject : messages) {
                String message = Xml.selectString((Element) messageObject, "normalize-space(.)", namespaces);

                this.addValidationMessage(
                    record,
                    message,
                    null,
                    schematronName,
                    SchematronRequirement.REQUIRED);
            }
        }
    }
}
