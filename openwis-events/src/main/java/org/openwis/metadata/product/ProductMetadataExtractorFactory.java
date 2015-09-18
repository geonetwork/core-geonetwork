package org.openwis.metadata.product;


public final class ProductMetadataExtractorFactory {
    /**
     * Default constructor.
     * Builds a ProductMetadataExtractManagerFactory.
     */
    private ProductMetadataExtractorFactory() {
        super();
    }

    /**
     * Gets an extractor of product metadata according to the metadata schema.
     *
     * @param schema the metadata schema.
     * @return the corresponding extractor.
     */
    public static IProductMetadataExtractor getProductMetadataExtractor(String schema) {
        if ("iso19139".equals(schema)) {
            return new ProductMetadataISO19139Extractor();
        }

        return new ProductMetadataDefaultExtractor();
    }
}
