//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.setting;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.fao.geonet.ApplicationContextHolder;

import java.util.Calendar;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;
import static org.fao.geonet.kernel.setting.SettingManager.isPortRequired;

//=============================================================================
public class SettingInfo {

    public String getSiteName() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        return settingManager.getSiteName();
    }

    //---------------------------------------------------------------------------

    /**
     * Return a string like 'http://HOST[:PORT]'
     */
    public String getSiteUrl() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        String protocol = settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        return getSiteUrl(protocol.equalsIgnoreCase("https"));
    }

    /**
     * Return a string like 'http://HOST[:PORT]'
     */
    public String getSiteUrl(boolean secureUrl) {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        String protocol;
        Integer port;
            String host = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
            Integer secureport = toIntOrNull(Settings.SYSTEM_SERVER_SECURE_PORT);
            Integer insecureport = toIntOrNull(Settings.SYSTEM_SERVER_PORT);
        if (secureUrl) {
            protocol = "https";
            if (secureport == null && insecureport == null) {
                port = 443;
            } else if (secureport != null) {
                port = secureport;
            } else {
                protocol = "http";
                port = insecureport;
            }
        } else {
            protocol = "http";
            if (secureport == null && insecureport == null) {
                port = 80;
            } else if (insecureport != null) {
                port = insecureport;
            } else {
                protocol = "https";
                port = secureport;
            }
        }

        StringBuffer sb = new StringBuffer(protocol + "://");

        sb.append(host);

		if (isPortRequired(protocol, port + "")) {
			sb.append(":");
			sb.append(port);
		}

        return sb.toString();
    }

    //---------------------------------------------------------------------------

    private Integer toIntOrNull(String key) {
        try {
            SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
            return Integer.parseInt(settingManager.getValue(key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getSelectionMaxRecords() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
    String value = settingManager.getValue(Settings.SYSTEM_SELECTIONMANAGER_MAXRECORDS);
        if (value == null) value = "10000";
        return value;
    }

    /**
     * Whether to use auto detection of the language used in search terms.
     */
    public boolean getAutoDetect() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_AUTODETECT_ENABLE);
        if (value == null) {
            return false;
        } else {
            return value.equals("true");
        }
    }

    /**
     * Whether search results should be only in the requested language.
     */
    public SearchRequestLanguage getRequestedLanguageOnly() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_REQUESTED_LANGUAGE_ONLY);
        return SearchRequestLanguage.find(value);
    }

    /**
     * Whether search results should be sorted with the requested language on top.
     */
    public boolean getRequestedLanguageOnTop() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_REQUESTEDLANGUAGE_SORTED);
        if (value == null) {
            return false;
        } else {
            return value.equals("true");
        }
    }

    public boolean getLuceneIndexOptimizerSchedulerEnabled() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
    String value = settingManager.getValue(Settings.SYSTEM_INDEXOPTIMIZER_ENABLE);
        if (value == null) return false;
        else return value.equals("true");
    }

    public boolean isXLinkResolverEnabled() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE);
        if (value == null) return false;
        else return value.equals("true");
    }

    //---------------------------------------------------------------------------

    public boolean isSearchStatsEnabled() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_SEARCHSTATS);
        if (value == null) return false;
        else return value.equals("true");
    }

    //---------------------------------------------------------------------------

    public Calendar getLuceneIndexOptimizerSchedulerAt() throws IllegalArgumentException {
        Calendar calendar = Calendar.getInstance();
        try {
            SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

            calendar.set(0, 0, 0,
                Integer.parseInt(settingManager.getValue(Settings.SYSTEM_INDEXOPTIMIZER_HOUR)),
                Integer.parseInt(settingManager.getValue(Settings.SYSTEM_INDEXOPTIMIZER_MIN)) ,
                Integer.parseInt(settingManager.getValue(Settings.SYSTEM_INDEXOPTIMIZER_SEC)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed parsing schedule at info from settings: " + e.getMessage());
        }
        return calendar;
    }

    //---------------------------------------------------------------------------

    public int getLuceneIndexOptimizerSchedulerInterval() throws IllegalArgumentException {
        int result = -1;
        try {
            SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

            int day = Integer.parseInt(settingManager.getValue("system/indexoptimizer/interval/day"));
            int hour = Integer.parseInt(settingManager.getValue("system/indexoptimizer/interval/hour"));
            int min = Integer.parseInt(settingManager.getValue("system/indexoptimizer/interval/min"));
            result = (day * 24 * 60) + (hour * 60) + min;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed parsing scheduler interval from settings: " + e.getMessage());
        }
        return result;
    }

    //---------------------------------------------------------------------------

    public String getFeedbackEmail() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        return settingManager.getValue("system/feedback/email");
    }

    //---------------------------------------------------------------------------

    public boolean getInspireEnabled() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        return settingManager.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
    }

    //---------------------------------------------------------------------------

    public char[] getAnalyzerIgnoreChars() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String ignoreChars = settingManager.getValue(Settings.SYSTEM_LUCENE_IGNORECHARS);
        if (ignoreChars == null || ignoreChars.length() == 0) {
            return null;
        }
        return ignoreChars.toCharArray();
    }


    public enum SearchRequestLanguage {
        OFF("off", null, null),
        PREFER_LOCALE("prefer_locale", "_locale", SHOULD),
        ONLY_LOCALE("only_locale", "_locale", MUST),
        PREFER_DOC_LOCALE("prefer_docLocale", "_docLocale", SHOULD),
        ONLY_DOC_LOCALE("only_docLocale", "_docLocale", MUST),
        PREFER_UI_LOCALE("prefer_ui_locale", "_locale", SHOULD),
        ONLY_UI_LOCALE("only_ui_locale", "_locale", MUST),
        PREFER_UI_DOC_LOCALE("prefer_ui_docLocale", "_docLocale", SHOULD),
        ONLY_UI_DOC_LOCALE("only_ui_docLocale", "_docLocale", MUST);

        public final String databaseValue;
        public final String fieldName;
        private final BooleanClause.Occur occur;

        SearchRequestLanguage(String databaseValue, String fieldName, BooleanClause.Occur occur) {
            this.databaseValue = databaseValue;
            this.fieldName = fieldName;
            this.occur = occur;
        }

        public static SearchRequestLanguage find(String value) {
            for (SearchRequestLanguage enumValue : values()) {
                if (enumValue.databaseValue.equals(value)) {
                    return enumValue;
                }
            }

            return OFF;
        }

        public void addQuery(BooleanQuery query, String langCode) {
            if (fieldName != null) {
                query.add(new TermQuery(new Term(fieldName, langCode)), occur);
            }

        }
    }

}

//=============================================================================

