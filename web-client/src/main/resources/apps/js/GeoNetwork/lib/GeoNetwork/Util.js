/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
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
Ext.namespace('GeoNetwork');

GeoNetwork.Lang = {};

GeoNetwork.Util = {
    defaultLocale: 'en',
    /**
     * Supported GeoNetwork GUI languages
     */
    locales: [
            ['ar', 'عربي', 'ara'], 
            ['ca', 'Català', 'cat'], 
            ['cn', '中文', 'chi'], 
            ['de', 'Deutsch', 'ger'], 
            ['en', 'English', 'eng'], 
            ['es', 'Español', 'spa'], 
            ['fr', 'Français', 'fre'], 
            ['nl', 'Nederlands', 'dut'], 
            ['no', 'Norsk', 'nor'],
            ['pt', 'Рortuguês', 'por'], 
            ['ru', 'Русский', 'rus']
    ],
    
    /**
     * Set OpenLayers lang and load ext required lang files
     */
    setLang: function(lang, baseUrl){
        lang = lang || GeoNetwork.Util.defaultLocale;
        OpenLayers.Lang.setCode(lang);
        var s = document.createElement("script");
        s.type = 'text/javascript';
        s.src = baseUrl + "/js/ext/src/locale/ext-lang-" + lang + ".js";
        document.getElementsByTagName("head")[0].appendChild(s);
    },
    /**
     * Return a valid language code if translation is available.
     */
    getCatalogueLang: function(lang){
        var i;
        for (i = 0; i < GeoNetwork.Util.locales.length; i++) {
            if (GeoNetwork.Util.locales[i][0] === lang) {
                return GeoNetwork.Util.locales[i][0];
            }
        }
        return GeoNetwork.Util.defaultLocale;
    },
    getISO3LangCode: function(lang){
        var i;
        for (i = 0; i < GeoNetwork.Util.locales.length; i++) {
            if (GeoNetwork.Util.locales[i][0] === lang) {
                return GeoNetwork.Util.locales[i][2];
            }
        }
        return 'eng';
    },
    getParameters: function(url){
        var parameters = OpenLayers.Util.getParameters(url);
        if (OpenLayers.String.contains(url, '#')) {
            var start = url.indexOf('#') + 1;
            var end = url.length;
            var paramsString = url.substring(start, end);
            
            var pairs = paramsString.split(/[\/]/);
            for (var i = 0, len = pairs.length; i < len; ++i) {
                var keyValue = pairs[i].split('=');
                var key = keyValue[0];
                var value = keyValue[1] || '';
                parameters[key] = value;
            }
        }
        return parameters;
    },
    getBaseUrl: function(url){
        return url.substring(0, url.indexOf('?') || url.indexOf('#') || url.length);
    }
};
