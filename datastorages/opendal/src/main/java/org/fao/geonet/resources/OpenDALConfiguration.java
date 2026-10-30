/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.resources;

import org.apache.opendal.AsyncOperator;
import org.apache.opendal.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class OpenDALConfiguration {
    private static final Logger log = LoggerFactory.getLogger(OpenDALConfiguration.class);

    private AsyncOperator asyncOperator;
    private Operator operator;
    private String scheme = "fs";
    private long presignExpirySeconds = 0;
    private Map<String, String> options = new HashMap<>();

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setOptions(Map<String, String> options) {
        if (options != null) {
            options.values().removeIf(value -> value == null || value.isEmpty());
            this.options.putAll(options);
        }
    }

    /**
     * Validity of the presigned URLs used to download attachments directly from the storage.
     * 0 (default) disables presigned URLs and attachments are served by GeoNetwork.
     */
    public void setPresignExpirySeconds(String presignExpirySeconds) {
        if (presignExpirySeconds != null && !presignExpirySeconds.isEmpty()) {
            this.presignExpirySeconds = Long.parseLong(presignExpirySeconds);
        }
    }

    @PostConstruct
    public void init() {
        log.info("Initializing OpenDAL operator with scheme: {}", scheme);
        // Presign is only available in the async API, the blocking operator shares the same service
        this.asyncOperator = AsyncOperator.of(scheme, options);
        this.operator = asyncOperator.blocking();
        if (presignExpirySeconds > 0 && !asyncOperator.info.fullCapability.presignRead) {
            log.warn("OpenDAL scheme {} does not support presigned URLs, attachments will be served by GeoNetwork.", scheme);
        }
    }

    @PreDestroy
    public void destroy() {
        if (operator != null) {
            operator.close();
        }
        if (asyncOperator != null) {
            asyncOperator.close();
        }
    }

    /**
     * @return a presigned URL to read the object at path, or null if presigned URLs are disabled or not supported.
     */
    public String presignRead(String path) {
        if (presignExpirySeconds <= 0 || !asyncOperator.info.fullCapability.presignRead) {
            return null;
        }
        return asyncOperator.presignRead(path, Duration.ofSeconds(presignExpirySeconds)).join().getUri();
    }

    @Nonnull
    public Operator getOperator() {
        if (operator == null) {
            throw new IllegalStateException("OpenDAL Operator not initialized");
        }
        return operator;
    }
}
