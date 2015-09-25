package org.openwis.metadata.product;

import org.fao.geonet.domain.Metadata;
import org.openwis.products.client.ProductMetadata;
import org.openwis.products.client.UpdateFrequency;

/**
 * Interface to implement by product metadata extractor classes.
 *
 */
public interface IProductMetadataExtractor {
    String extractFncPattern(Metadata metadata) throws Exception;

    String extractOriginator(Metadata metadata) throws Exception;

    String extractTitle(Metadata metadata) throws Exception;

    String extractLocalDataSource(Metadata metadata) throws Exception;

    UpdateFrequency extractUpdateFrequency(Metadata metadata) throws Exception;

    /**
     * Description goes here.
     * @param metadata
     * @return
     */
    String extractFileExtension(Metadata metadata) throws Exception;

    /**
     * Description goes here.
     * @param metadata
     * @param pm
     */
    void extractGTSCategoryGTSPriorityAndDataPolicy(Metadata metadata, ProductMetadata pm) throws Exception;

    /**
     * Default priority.
     */
    static final Integer DEFAULT_PRIORITY = 3;

    /**
     * Default originator.
     */
    static final String DEFAULT_ORIGINATOR = "RTH focal point";

    /**
     * Max string length for Fnc Pattern.
     */
    static final int MAX_LENGTH_FNC_PATTERN = 1024;

    /**
     * Max string length for Local Datasource.
     */
    static final int MAX_LENGTH_LOCAL_DATASOURCE = 255;

    /**
     * Max string length for Originator.
     */
    static final int MAX_LENGTH_ORIGINATOR = 255;

    /**
     * Max string length for Title.
     */
    static final int MAX_LENGTH_TITLE = 255;

    /**
     * The GTS Category Unknown.
     */
    static final String GTS_CATEGORY_NONE = "Unknown";

    /**
     * The GTS Category Essential.
     */
    static final String GTS_CATEGORY_ESSENTIAL = "WMO\\\\s*Essential";

    /**
     * The GTS Category Additional.
     */
    static final String GTS_CATEGORY_ADDITIONAL = "WMO\\\\s*Additional";

    /**
     * The GTS priority.
     */
    static final String GTS_PRIORITY = "GTS\\\\s*Priority\\\\s*(\\\\d)";


    /**
     * The default additional data policy.
     */
    static final String DEFAULT_ADDITIONAL_DATAPOLICY = "additional-default";


    /**
     * The Public Data policy.
     */
    static final String PUBLIC_DATAPOLICY = "public";

    /**
     * The Unknown Data policy.
     */
    static final String UNKNOWN_DATAPOLICY = "unknown";

    /**
     * Max string length for GTS Category
     */
    static final int MAX_LENGTH_GTS_CATEGORY = 255;

    /**
     * The pattern applied on URN to determine if FNC Pattern should be ignored
     */
    static final String URN_PATTERN_FOR_IGNORED_FNC_PATTERN = "^urn:x-wmo:md:int.wmo.wis::\\\\D\\\\D\\\\D\\\\D\\\\d\\\\d\\\\D\\\\D\\\\D\\\\D$";

}
