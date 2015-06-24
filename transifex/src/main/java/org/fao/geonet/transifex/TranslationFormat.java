package org.fao.geonet.transifex;

import java.util.List;
import java.util.Map;

/**
 * Represents a translation file format.
 *
 * @author Jesse on 6/18/2015.
 */
public interface TranslationFormat {
    TranslationFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties);
    List<TransifexReadyFile> toTransifex(String translationFile) throws Exception;
    String toGeonetwork(List<TransifexReadyFile> fromTransifex) throws Exception;
    Layout getDefaultLayout();
}
