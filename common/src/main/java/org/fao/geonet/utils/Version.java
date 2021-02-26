package org.fao.geonet.utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parsed version number used to check core-geonetwork and schema plugin compatibility.
 *
 * This class is intended to reflect maven version numbering conventions for equality and
 * comparison. Maven version numbering is quite complicated and improvements will be
 * required over time.
 *
 * @see <a href="https://octopus.com/blog/maven-versioning-explained">Maven versions explained</a>
 */
public class Version implements Comparable<Version> {
    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final Integer build;
    private final String qualifer;

    private static final Pattern PATTERN = Pattern.compile("^(\\d*)?[\\.|-]?(\\d*)?[\\.|-]?(\\d*)?[\\.|-]??(\\d*)?[\\.|-]?(\\S*)?$");

    /**
     * Parses a version number following maven major, minor, patch definition.
     *
     * Use of a qualifier such as SNAPSHOT is considered for comparison order only.
     *
     * As an example 2.7.0-SNAPSHOT is returned as major 2, minor 7, patch 0, build null, qualifier SNAPSHOT.
     *
     * @param number The version number to parse
     * @return The version number
     */
    public static Version parseVersionNumber(String number) {
        Matcher parsed = PATTERN.matcher(number);
        if( parsed.find()){
            String major = parsed.group(1);
            String minor = parsed.group(2);
            String patch = parsed.group(3);
            String build = parsed.group(4);
            String qualifer = parsed.group(5);

            return new Version(major,minor,patch,build,qualifer);
        }
        return new Version(null,null,null,null,number);
    }

    public Version() {
        this("0", "0", "0");
    }

    public Version(String major, String minor, String patch) {
        this(major,minor,patch,null,null);
    }
    public Version(String major, String minor, String patch, String build, String qualifier) {
        this.major = (major != null && !major.isEmpty()) ? Integer.valueOf(major) :  null;
        this.minor = (minor != null && !minor.isEmpty())? Integer.valueOf(minor) :  null;
        this.patch = (patch != null && !patch.isEmpty())? Integer.valueOf(patch) :  null;
        this.build = (build != null && !build.isEmpty())? Integer.valueOf(build) :  null;
        this.qualifer = (qualifier != null && !qualifier.isEmpty())? qualifier :  null;
    }


    /** Qualifer if present sorts higher than a null*/
    private static final Comparator<String> qualiferFirst = Comparator
        .nullsFirst(String::compareTo);

    /** Qualifier placeholder of null sort higher than a default placeholder of 0 */
    private static final Comparator<Integer> snapshotFirst = Comparator
        .nullsFirst(Integer::compareTo);

    private static final int placeholder( Integer number, String qualifier){
        return number != null ? number : "SNAPSHOT".equals(qualifier) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public int compareTo(Version o) {
        // numbers sort with snapshot (MAX_VALUE), before numbers, before empty (which sorts to zero).
        int majorCompare = snapshotFirst.compare( placeholder(major,qualifer), placeholder(o.major,o.qualifer));
        int minorCompare = snapshotFirst.compare( placeholder(minor,qualifer), placeholder(o.minor,o.qualifer));
        int patchCompare = snapshotFirst.compare( placeholder(patch,qualifer), placeholder(o.patch,o.qualifer));
        int buildCompare = snapshotFirst.compare( placeholder(build,qualifer), placeholder(o.build,o.qualifer));

        // snapshot qualifier if provided sorts ahead of null
        int qualiferCompare = qualiferFirst.compare( qualifer, o.qualifer );

        if( major == null && o.major == null)
            return qualiferCompare;
        else if ( majorCompare != 0 )
            return majorCompare;

        if( minor == null && o.minor == null)
            return qualiferCompare;
        else if ( minorCompare != 0 )
            return minorCompare;

        if( patch == null && o.patch == null)
            return qualiferCompare;
        else if ( patchCompare != 0 )
            return patchCompare;

        if( build == null && o.build == null)
            return qualiferCompare;
        else if ( buildCompare != 0 )
            return buildCompare;

        return qualiferCompare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;

        // numbers sort with snapshot (MAX_VALUE), before numbers, before empty (which sorts to zero).
        int majorCompare = snapshotFirst.compare( placeholder(major,qualifer), placeholder(version.major,version.qualifer));
        int minorCompare = snapshotFirst.compare( placeholder(minor,qualifer), placeholder(version.minor,version.qualifer));
        int patchCompare = snapshotFirst.compare( placeholder(patch,qualifer), placeholder(version.patch,version.qualifer));
        int buildCompare = snapshotFirst.compare( placeholder(build,qualifer), placeholder(version.build,version.qualifer));
        int qualiferCompare = qualiferFirst.compare( qualifer, version.qualifer );

        return majorCompare == 0 && minorCompare == 0 && patchCompare == 0 && buildCompare == 0 && qualiferCompare == 0;
    }

    @Override
    public int hashCode() {
        int placeholder =  "SNAPSHOT".equals(qualifer) ? Integer.MAX_VALUE : 0;

        // use of placeholder to match hashcode / equals contract
        return Objects.hash(
            placeholder(major,qualifer),
            placeholder(minor,qualifer),
            placeholder(patch,qualifer),
            placeholder(build,qualifer),
            qualifer);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        if( major != null){
            sb.append(major);
        }
        if( minor != null){
            sb.append(".").append(minor);
        }
        if( patch != null){
            sb.append(".").append(patch);
        }
        if( build != null){
            sb.append(".").append(build);
        }
        if( qualifer != null){
            if( sb.length() != 0 ){
                sb.append("-");
            }
            sb.append(qualifer);
        }
        return sb.toString();
    }

}
