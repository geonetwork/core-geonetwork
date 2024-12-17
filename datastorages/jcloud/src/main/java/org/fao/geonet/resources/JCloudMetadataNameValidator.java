/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.resources;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Each JCloud provider has different restrictions on the naming standard used for the metadata property names.
 * This class is used to check the requirement of these headers so that we don't get obscure errors when attempting to set the metadata property names.
 */
public class JCloudMetadataNameValidator {

    public static class ProviderMetadataNamingRules {
        private final String maxLength;
        private final Pattern regex;

        public ProviderMetadataNamingRules(String maxLength, Pattern regex) {
            this.maxLength = maxLength;
            this.regex = regex;
        }

        public String getMaxLength() {
            return maxLength;
        }

        public Pattern getRegex() {
            return regex;
        }
    }

    // Define metadata naming rules for each provider
    private static final Map<String, ProviderMetadataNamingRules> providerMetadataNamingRules = new HashMap<>();
    private static final ProviderMetadataNamingRules defaultRules = new ProviderMetadataNamingRules(
        "255",
        Pattern.compile("^[a-zA-Z0-9._-]{1,255}$")
    );

    // Note: All these patterns have not been tested with all providers and may need adjustments.
    static {
        providerMetadataNamingRules.put("aws-s3", new ProviderMetadataNamingRules(
            "255",
            Pattern.compile("^[a-zA-Z0-9._-]{1,255}$")
        ));

        providerMetadataNamingRules.put("b2", new ProviderMetadataNamingRules(
            "255",
            Pattern.compile("^[a-zA-Z0-9._-]{1,255}$")
        ));

        providerMetadataNamingRules.put("google-cloud-storage", new ProviderMetadataNamingRules(
            "1024",
            Pattern.compile("^[a-zA-Z0-9._-]{1,1024}$")
        ));

        /**
         * Azure blob
         * https://learn.microsoft.com/en-us/rest/api/storageservices/naming-and-referencing-containers--blobs--and-metadata#metadata-names
         * Follows C# naming in lowercase only
         */
        providerMetadataNamingRules.put("azureblob", new ProviderMetadataNamingRules(
            "255",
            Pattern.compile("^[a-z_][a-z0-9_]{0,254}$")
        ));

        providerMetadataNamingRules.put("rackspace-cloudfiles-us", new ProviderMetadataNamingRules(
            "255",
            Pattern.compile("^[a-zA-Z0-9._-]{1,255}$")
        ));

        providerMetadataNamingRules.put("rackspace-cloudfiles-uk", new ProviderMetadataNamingRules(
            "255",
            Pattern.compile("^[a-zA-Z0-9._-]{1,255}$")
        ));
    }

    /**
     * Validates metadata names for each provider.
     *
     * @param provider The name of the provider.
     * @param metadataNames Array of metadata names to validate.
     * @throws IllegalArgumentException if any metadata name is invalid according to the provider's rules.
     */
    public static void validateMetadataNamesForProvider(String provider, String[] metadataNames) throws IllegalArgumentException {
        ProviderMetadataNamingRules rules = providerMetadataNamingRules.getOrDefault(provider.toLowerCase(), defaultRules);

        for (String name : metadataNames) {
            if (StringUtils.hasLength(name) && !isValidMetadataName(name, rules)) {
                throw new IllegalArgumentException(String.format("Invalid metadata name for provider %s: %s", provider, name));
            }
        }
    }

    /**
     * Checks if a single metadata name is valid based on the provider's rules.
     *
     * @param name   The metadata name to check.
     * @param rules  The metadata naming rules for the provider.
     * @return True if the name is valid, false otherwise.
     */
    private static boolean isValidMetadataName(String name, ProviderMetadataNamingRules rules) {
        // Null/Empty property names are allow as it means they will not be used.
        if (!StringUtils.hasLength(name)) {
            return false;
        }

        if (name.length() > Integer.parseInt(rules.getMaxLength())) {
            return false;
        }

        return rules.getRegex().matcher(name).matches();
    }
}
