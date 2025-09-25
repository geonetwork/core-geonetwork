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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A report class that stores validation messages from schematron validation.
 *
 * <p>This class captures validation messages produced when metadata is validated
 * against schematron rules. Messages are organized by pattern title, allowing
 * for structured reporting of validation issues.</p>
 *
 * <p>Each instance of SchematronValidationReport is associated with a specific
 * schematron rule set and its requirement level (REQUIRED for errors or
 * REPORT_ONLY for warnings).</p>
 */
public class SchematronValidationReport{
    /**
     * Placeholder used when a pattern title is not provided.
     */
    private static final String NO_PATTERN_TITLE_PLACEHOLDER = "NO_PATTERN_TITLE";

    /**
     * Information about the schematron rule set that generated this report.
     */
    private final String schematron;

    /**
     * Map of validation messages organized by pattern title.
     * For each pattern title, there can be multiple validation messages.
     */
    private final Map<String, List<String>> ruleMessages = new HashMap<>();

    /**
     * Creates a new SchematronValidationReport for a specific schematron rule set.
     *
     * @param schematronName The name of the schematron rule set
     */
    public SchematronValidationReport(String schematronName) {
        this.schematron = schematronName;
    }

    /**
     * Gets all validation messages in this report, organized by pattern title.
     *
     * @return Map of validation messages where keys are pattern titles and
     *         values are lists of validation message strings
     */
    public Map<String, List<String>> getMessages() { return ruleMessages; }

    /**
     * Gets information about the schematron rule set that generated this report.
     *
     * @return Schematron object containing the name and requirement level
     */
    public String getSchematron() {
        return schematron;
    }

    /**
     * Adds a validation message to the report for a specific pattern.
     *
     * @param patternTitle The title of the pattern that triggered the validation message,
     *        or null if no specific pattern is associated
     * @param message The validation message text
     */
    public void addMessage(String patternTitle, String message) {
        if (patternTitle == null) {
            patternTitle = NO_PATTERN_TITLE_PLACEHOLDER;
        }
        // Initialize the list if it doesn't exist
        ruleMessages.computeIfAbsent(patternTitle, k -> new ArrayList<>()).add(message);
    }
}
