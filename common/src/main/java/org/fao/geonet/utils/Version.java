/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parsed version number used to check core-geonetwork and schema plugin compatibility.
 * <p>
 * This class is intended to reflect Maven version numbering conventions for equality and
 * comparison. Maven version numbering is quite complicated and improvements will be
 * required over time.
 *
 * @see <a href="https://octopus.com/blog/maven-versioning-explained">Maven versions explained</a>
 */
public class Version implements Comparable<Version> {
    private static final Pattern PATTERN = Pattern.compile("^(\\d*)?[\\.|-]?(\\d*)?[\\.|-]?(\\d*)?[\\.|-]?(\\d*)?[\\.|-]?(\\S*)?$");

    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final Integer build;
    private final String qualifier;

    /**
     * Build a 0.0.0 version.
     */
    public Version() {
        this("0", "0", "0");
    }

    /**
     * Build a version with major, minor and patch, for example 3.10.7.
     * If any of the parameters can't be parsed as an integer throws a {@link NumberFormatException}.
     *
     * @param major major version number.
     * @param minor minor version number.
     * @param patch patch version number.
     */
    public Version(String major, String minor, String patch) {
        this(major, minor, patch, null, null);
    }

    /**
     * Build a version with major, minor and patch, for example 3.10.7.2-SNAPSHOT
     * If any of the parameters except {@code qualifier} is not null and can't be parsed as
     * an integer throws a {@link NumberFormatException}.
     *
     * @param major     major version number.
     * @param minor     minor version number.
     * @param patch     patch version number.
     * @param build     build version number.
     * @param qualifier the qualifier (part after - character).
     */
    public Version(String major, String minor, String patch, String build, String qualifier) {
        this.major = (major != null && !major.isEmpty()) ? Integer.valueOf(major) : null;
        this.minor = (minor != null && !minor.isEmpty()) ? Integer.valueOf(minor) : null;
        this.patch = (patch != null && !patch.isEmpty()) ? Integer.valueOf(patch) : null;
        this.build = (build != null && !build.isEmpty()) ? Integer.valueOf(build) : null;
        this.qualifier = (qualifier != null && !qualifier.isEmpty()) ? qualifier : null;
    }

    /**
     * Parses a version number following maven major, minor, patch definition.
     * <p>
     * Use of a qualifier such as SNAPSHOT is considered for comparison order only.
     * <p>
     * As an example 2.7.0-SNAPSHOT is returned as major 2, minor 7, patch 0, build null, qualifier SNAPSHOT.
     *
     * @param number The version number to parse
     * @return The version number
     */
    public static Version parseVersionNumber(String number) {
        Matcher parsed = PATTERN.matcher(number);
        if (parsed.find()) {
            String major = parsed.group(1);
            String minor = parsed.group(2);
            String patch = parsed.group(3);
            String build = parsed.group(4);
            String qualifier = parsed.group(5);

            return new Version(major, minor, patch, build, qualifier);
        }
        return new Version(null, null, null, null, number);
    }

    private static final int order(Integer number, String qualifier) {
        return number != null ? number : order(qualifier);
    }

    private static final int order(String qualifier) {
        if (qualifier == null)
            return 0;
        else if ("SNAPSHOT".equalsIgnoreCase(qualifier))
            return Integer.MAX_VALUE;
        else if ("RC".equalsIgnoreCase(qualifier))
            return -1;
        else if ("0".equalsIgnoreCase(qualifier))
            return 0;
        else if ("final".equalsIgnoreCase(qualifier))
            return 0;
        else
            return -2;
    }

    @Override
    public int compareTo(Version o) {
        // numbers sort with snapshot (MAX_VALUE), before numbers, before empty (which sorts to zero).
        int majorCompare = Integer.compare(order(major, qualifier), order(o.major, o.qualifier));
        int minorCompare = Integer.compare(order(minor, qualifier), order(o.minor, o.qualifier));
        int patchCompare = Integer.compare(order(patch, qualifier), order(o.patch, o.qualifier));
        int buildCompare = Integer.compare(order(build, qualifier), order(o.build, o.qualifier));

        // snapshot qualifier if provided sorts ahead of null
        int qualifierCompare = Integer.compare(order(qualifier), order(o.qualifier));

        if (major == null && o.major == null)
            return qualifierCompare;
        else if (majorCompare != 0)
            return majorCompare;

        if (minor == null && o.minor == null)
            return qualifierCompare;
        else if (minorCompare != 0)
            return minorCompare;

        if (patch == null && o.patch == null)
            return qualifierCompare;
        else if (patchCompare != 0)
            return patchCompare;

        if (build == null && o.build == null)
            return qualifierCompare;
        else if (buildCompare != 0)
            return buildCompare;

        return qualifierCompare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Version version = (Version) o;
        return compareTo(version) == 0;
    }

    @Override
    public int hashCode() {
        int placeholder = "SNAPSHOT".equals(qualifier) ? Integer.MAX_VALUE : 0;

        // use of order to match hashcode / equals contract
        return Objects
                .hash(order(major, qualifier),
                        order(minor, qualifier),
                        order(patch, qualifier),
                        order(build, qualifier),
                        order(qualifier)
                );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (major != null) {
            sb.append(major);
        }
        if (minor != null) {
            sb.append(".").append(minor);
        }
        if (patch != null) {
            sb.append(".").append(patch);
        }
        if (build != null) {
            sb.append(".").append(build);
        }
        if (qualifier != null) {
            if (sb.length() != 0) {
                sb.append("-");
            }
            sb.append(qualifier);
        }
        return sb.toString();
    }
}
