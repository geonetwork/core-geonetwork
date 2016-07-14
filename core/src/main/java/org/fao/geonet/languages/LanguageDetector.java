//===	Copyright (C) 2012 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================
package org.fao.geonet.languages;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;

import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.nio.file.Path;

/**
 * Automatic language detector using http://code.google.com/p/language-detection/.
 *
 * @author heikki doeleman
 */
public class LanguageDetector {

    private static LanguageDetector instance;

    private static boolean profilesLoaded = false;
    private static boolean languageLevelSupported = false;
    private static String upgradeMessage;

    static {
        String javaVersion = System.getProperty("java.version");
        // java < 1.6 not supported
        if (StringUtils.isNotEmpty(javaVersion) && javaVersion.startsWith("1.5")) {
            upgradeMessage = "You are running on Java " + javaVersion + ", auto-detecting languages is disabled. Upgrade to at least 1.6.";
            Log.warning(Geonet.LANGUAGEDETECTOR, LanguageDetector.upgradeMessage);
        } else {
            LanguageDetector.languageLevelSupported = true;
        }
    }

    private LanguageDetector() {
    }

    /**
     * TODO javadoc.
     *
     * @return instance
     * @throws Exception hmm
     */
    public static synchronized LanguageDetector getInstance() throws Exception {
        if (!LanguageDetector.languageLevelSupported) {
            throw new Exception(LanguageDetector.upgradeMessage);
        }
        if (instance == null) {
            instance = new LanguageDetector();
        }
        return instance;
    }

    /**
     * Creates mapping to ISO 639-2 for all languages supported by this language detector.
     *
     * @param path path to profiles directory
     * @throws Exception hmm
     */
    public static void init(Path path) throws Exception {
        if (!LanguageDetector.languageLevelSupported) {
            throw new Exception(LanguageDetector.upgradeMessage);
        }
        if (!LanguageDetector.profilesLoaded) {
            //
            // initialize DetectorFactory. NOTE this can only happen once, otherwise an exception is thrown.
            //
            DetectorFactory.loadProfile(path.toFile());
            LanguageDetector.profilesLoaded = true;
        }
    }

    /**
     * Helps ensure singleton-ness.
     *
     * @return nothing
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Detects language of input string.
     *
     * @param input text to analyze
     * @return iso 639-2 code of detected language
     * @throws Exception hmm
     */
    public String detect(ServiceContext srvContext, String input) throws Exception {
        if (!LanguageDetector.languageLevelSupported) {
            throw new Exception(LanguageDetector.upgradeMessage);
        }
        Detector detector = DetectorFactory.create();
        detector.append(input);
        String detectedLanguage = detector.detect();
        // this is to deal with zh-cn and zh-tw in languageprofiles. All other files have a 639-1 2-char filename.
        if (detectedLanguage.length() > 2) {
            detectedLanguage = detectedLanguage.substring(0, 2);
        }
        String iso639_2 = srvContext.getBean(IsoLanguagesMapper.class).iso639_1_to_iso639_2(detectedLanguage);
        Log.debug(Geonet.LANGUAGEDETECTOR,
            "detected language: " + iso639_2 +
                " for text:" + input);

        return iso639_2;
    }
}
