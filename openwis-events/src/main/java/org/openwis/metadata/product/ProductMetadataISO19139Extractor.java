package org.openwis.metadata.product;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.utils.Xml;
import org.jdom.Namespace;
import org.openwis.products.client.ProductMetadata;
import org.openwis.products.client.RecurrentScale;
import org.openwis.products.client.RecurrentUpdateFrequency;
import org.openwis.products.client.UpdateFrequency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Extracts the product information from a metadata document.
 *
 */
public class ProductMetadataISO19139Extractor implements IProductMetadataExtractor {
    private static List<Namespace> nsListGMDGCO = new ArrayList<Namespace>();
    private static List<Namespace> nsListGMDGMX = new ArrayList<Namespace>();

    private static final List<String> TEXT_EXTENSION = Arrays.asList("1", "B", "C", "D", "F", "G",
            "K", "N", "S", "U", "V", "W", "X");

    private static final List<String> GRIB_EXTENSION = Arrays.asList("H", "O", "Y");

    private static final List<String> BUFR_EXTENSION = Arrays.asList("I", "J");

    private static final List<String> IMG_EXTENSION = Arrays.asList("P", "Q");

    private static final List<String> SATIMG_EXTENSION = Arrays.asList("E");

    private static final List<String> SATDATA_EXTENSION = Arrays.asList("T");

    public ProductMetadataISO19139Extractor() {
        super();

        nsListGMDGCO = new ArrayList<Namespace>();
        nsListGMDGCO.add(Geonet.Namespaces.GMD);
        nsListGMDGCO.add(Geonet.Namespaces.GCO);

        nsListGMDGMX = new ArrayList<Namespace>();
        nsListGMDGCO.add(Geonet.Namespaces.GMD);
        nsListGMDGMX.add(Geonet.Namespaces.GMX);
    }


    @Override
    public String extractFncPattern(Metadata metadata) throws Exception {
        String fncPattern;
        final String xpath = "gmd:describes/gmx:MX_DataSet/gmx:dataFile/gmx:MX_DataFile/gmx:fileName/gmx:FileName";
        fncPattern = Xml.selectString(metadata.getXmlData(false), xpath, nsListGMDGMX);
        fncPattern = StringUtils.abbreviate(fncPattern, MAX_LENGTH_FNC_PATTERN);

        //Log.info(Geonet.EXTRACT_PRODUCT_METADATA, "Extracted FNC Pattern: " + fncPattern);

        return StringUtils.isBlank(fncPattern) ? null : fncPattern;
    }

    @Override
    public String extractOriginator(Metadata metadata) throws Exception {
        final String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";

        String originator = Xml.selectString(metadata.getXmlData(false), xpath, nsListGMDGCO);
        originator = StringUtils.abbreviate(originator, MAX_LENGTH_ORIGINATOR);

        //Log.info(Geonet.EXTRACT_PRODUCT_METADATA,
        //        MessageFormat.format("Extracted Originator: {0}", originator));

        return originator;
    }

    @Override
    public String extractTitle(Metadata metadata) throws Exception {
        final String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/"
                + "gmd:CI_Citation/gmd:title/gco:CharacterString";
        String title = Xml.selectString(metadata.getXmlData(false), xpath, nsListGMDGCO);
        title = StringUtils.abbreviate(title, MAX_LENGTH_TITLE);

        //Log.info(Geonet.EXTRACT_PRODUCT_METADATA,
        //        MessageFormat.format("Extracted Title: {0}", title));

        return title;
    }

    @Override
    public String extractLocalDataSource(Metadata metadata) throws Exception {
        final String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:environmentDescription/gco:CharacterString";
        String localDS = Xml.selectString(metadata.getXmlData(false), xpath, nsListGMDGCO);
        localDS = StringUtils.abbreviate(localDS, MAX_LENGTH_LOCAL_DATASOURCE);

        //Log.info(Geonet.EXTRACT_PRODUCT_METADATA,
        //        MessageFormat.format("Extracted Local Data Source: {0}", localDS));

        return localDS;
    }

    @Override
    public UpdateFrequency extractUpdateFrequency(Metadata metadata) throws Exception {
        String updateFrequency;
        final String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/"
                + "gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/"
                + "gmd:MD_MaintenanceFrequencyCode/@codeListValue";

        // TODO: Review this code, updateFrequency is not used

        // FIXME create an update frequency object ...
        updateFrequency = Xml.selectString(metadata.getXmlData(false), xpath, nsListGMDGCO);
        //Log.debug(Geonet.EXTRACT_PRODUCT_METADATA, "Extracted update frequency (ignored): "
        //        + updateFrequency);

        // FIXME the recurrentUpdateFrequency should have a period also ...
        RecurrentUpdateFrequency recurrentUpdateFrequency = new RecurrentUpdateFrequency();
        recurrentUpdateFrequency.setRecurrentScale(RecurrentScale.HOUR);
        recurrentUpdateFrequency.setRecurrentPeriod(1);

        return recurrentUpdateFrequency;

    }

    @Override
    public String extractFileExtension(Metadata metadata) throws Exception {
        String result = null;

        // Try to guess file extension from T1 of TTAAiiCCCC
        String urn = metadata.getUuid();
        String ttaaii = urn.substring(urn.lastIndexOf("::") + 2);
        String t1 = ttaaii.substring(0, 1).toUpperCase();

        if (TEXT_EXTENSION.contains(t1)) {
            //text
            result = "txt";
        } else if (GRIB_EXTENSION.contains(t1)) {
            //grib
            result = "grib";
        } else if (BUFR_EXTENSION.contains(t1)) {
            //bufr
            result = "bufr";
        } else if (IMG_EXTENSION.contains(t1)) {
            //img
            result = null;
        } else if (SATIMG_EXTENSION.contains(t1)) {
            //sat Img
            result = null;
        } else if (SATDATA_EXTENSION.contains(t1)) {
            //Sat Data
            result = null;
        }

        // If no result, try to extract info from metadata
        if (result == null) {
            String xpath = "gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString";
            String distributionFormat = Xml.selectString(metadata.getXmlData(false), xpath, nsListGMDGCO);
            if (StringUtils.isNotBlank(distributionFormat)) {
                result = distributionFormat;
            }
        }

        //TODO: Review this code
        // In any case, filter file extensions to keep only valid FNC extension
        /*if (result != null && !ACCEPTED_FILE_EXTENSIONS.contains(result)) {
            Log.info(Geonet.EXTRACT_PRODUCT_METADATA, "Ignored file extension: " + result);
            result = null;
        }*/

        return result;
    }

    @Override
    public void extractGTSCategoryGTSPriorityAndDataPolicy(Metadata metadata, ProductMetadata pm) throws Exception {
        // TODO: Review code

       /* List<String> xpathList = OpenwisMetadataPortalConfig.getList(ConfigurationConstants.EXTRACT_XPATH);
        List<Element> useLimitationElts = new ArrayList<Element>();
        for (String xpath : xpathList) {
            useLimitationElts.addAll((List<Element>) Xml.selectNodes(metadata.getData(), xpath,
                    nsListGMDGCO));
        }

        if (useLimitationElts.isEmpty()) {
            assignUnkownDataPolicy(pm);
            return;
        }

        String otherDP = null; // track other dp (than essential and additional)
        boolean isGlobal = false;
        for (Element useLimitationEl : useLimitationElts) {
            String useLimitationStr = useLimitationEl.getText();

            // Try to get GTS category
            if (Pattern.matches(GTS_CATEGORY_ESSENTIAL, useLimitationStr)
                    || Pattern.matches(GTS_CATEGORY_ADDITIONAL, useLimitationStr)) {
                pm.setGtsCategory(useLimitationStr);
                // Extract the datapolicy
                pm.setDataPolicy(extractDatapolicy(useLimitationStr, metadata.getData()));

                Log.info(Geonet.EXTRACT_PRODUCT_METADATA, MessageFormat.format(
                        "Extracted GTS Category: {0} - Data Policy: {1}", useLimitationStr,
                        pm.getDataPolicy()));
                isGlobal = true;
            } else if (Pattern.matches(GTS_PRIORITY, useLimitationStr)) {
                pm.setPriority(extractGtsPriority(useLimitationStr));
            } else {
                // will keep the last other dp...
                otherDP = useLimitationStr;
            }
        }

        if (pm.getGtsCategory() != null
                && Pattern.matches(GTS_CATEGORY_ADDITIONAL, pm.getGtsCategory()) && otherDP != null) {
            // Try to apply another DP for additional product
            //Log.info(Geonet.EXTRACT_PRODUCT_METADATA,
            //        MessageFormat.format("Possible Data Policy for Additional product: {0}", otherDP));
            pm.setDataPolicy(otherDP);
        } else if (StringUtils.isBlank(pm.getGtsCategory())) {
            // Custom GTS category / data policy name
            if (otherDP == null) {
                // no other dp specified (only GTS priority was specified)
                assignUnkownDataPolicy(pm);
            } else {
                otherDP = StringUtils.abbreviate(otherDP, MAX_LENGTH_GTS_CATEGORY);
                Log.info(Geonet.EXTRACT_PRODUCT_METADATA, MessageFormat.format(
                        "Possible value for GTS Category and Data Policy: {0}", otherDP));
                pm.setDataPolicy(otherDP);
                pm.setGtsCategory(otherDP);
            }
        }

        checkFNCPattern(pm, isGlobal);*/
    }

    /**
     * Check if FNC Pattern should be ignored, which occurs in the following cases:
     * - the md is not Global
     * - the md URN matches a given regexp
     */
    private void checkFNCPattern(ProductMetadata pm, boolean isGlobal) {
        // TODO: Review code

        /*if (pm.getFncPattern() == null) {
            return;
        }
        if (!isGlobal) {
            //Log.info(Geonet.EXTRACT_PRODUCT_METADATA, "FNC Pattern ignored for non Global product");
            pm.setFncPattern(null);
        } else {
            // Check URN matches exclude pattern
            if (Pattern.matches(URN_PATTERN_FOR_IGNORED_FNC_PATTERN, pm.getUrn())) {
                Log.info(Geonet.EXTRACT_PRODUCT_METADATA, "FNC Pattern ignored because of URN exclude pattern");
                pm.setFncPattern(null);
            }
        }*/
    }
}
