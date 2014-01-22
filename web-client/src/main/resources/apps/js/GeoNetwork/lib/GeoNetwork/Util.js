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

//Required for IE6, 7, 8
//https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Function/bind
if (!Function.prototype.bind) {
     Function.prototype.bind = function (oThis) {
         if (typeof this !== "function") {
           // closest thing possible to the ECMAScript 5 internal IsCallable function
           throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
         }
      
         var aArgs = Array.prototype.slice.call(arguments, 1), 
             fToBind = this, 
             fNOP = function () {},
             fBound = function () {
               return fToBind.apply(this instanceof fNOP && oThis
                                      ? this
                                      : oThis,
                                    aArgs.concat(Array.prototype.slice.call(arguments)));
             };
      
         fNOP.prototype = this.prototype;
         fBound.prototype = new fNOP();
      
         return fBound;
    };
}

GeoNetwork.Lang = {};

GeoNetwork.Util = {
    defaultLocale: 'eng',
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
            ['it', 'Italiano', 'ita'], 
            ['nl', 'Nederlands', 'dut'], 
            ['no', 'Norsk', 'nor'],
            ['pl', 'Polski', 'pol'], 
            ['pt', 'Рortuguês', 'por'], 
            ['ru', 'Русский', 'rus'],
            ['fi', 'Suomeksi', 'fin'],
            ['tr', 'Türkçe', 'tur']
                       
    ],
    /** api: method[setLang] 
     *  :param lang: String ISO 3 letters code
     *  :param baseUrl: String Base URL use to load Ext loc files
     *
     *  Set OpenLayers lang and load ext required lang files
     */
    setLang: function(lang, baseUrl){
        lang = lang || GeoNetwork.Util.defaultLocale;
        // translate to ISO2 language code
        var openlayerLang = this.getISO2LangCode(lang);

        OpenLayers.Lang.setCode(openlayerLang);
        var s = document.createElement("script");
        s.type = 'text/javascript';
        s.src = baseUrl + "/js/ext/src/locale/ext-lang-" + openlayerLang + ".js";
        document.getElementsByTagName("head")[0].appendChild(s);
    },
    /** api: method[setLang] 
     *  :param lang: String ISO 3 letters code
     *  
     *  
     *  Return a valid language code if translation is available.
     *  Catalogue use ISO639-2 code.
     */
    getCatalogueLang: function(lang){
        var i;
        for (i = 0; i < GeoNetwork.Util.locales.length; i++) {
            if (GeoNetwork.Util.locales[i][0] === lang) {
                return GeoNetwork.Util.locales[i][2];
            }
        }
        return 'eng';
    },
    /** api: method[setLang] 
     *  :param lang: String ISO 3 letters code
     *  
     *  Return ISO2 language code (Used by OpenLayers lang and before GeoNetwork 2.7.0)
     *  for corresponding ISO639-2 language code.
     */
    getISO2LangCode: function(lang){
        var i;
        for (i = 0; i < GeoNetwork.Util.locales.length; i++) {
            if (GeoNetwork.Util.locales[i][2] === lang) {
                return GeoNetwork.Util.locales[i][0];
            }
        }
        return 'en';
    },
    /** api: method[getParameters] 
     *  :param url: String URL to parse
     *  
     *  Get list of URL parameters including anchor
     */
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
        if (url.indexOf('?') > 0) {
          return url.substring(0, url.indexOf('?'));
        } else if (url.indexOf('#') > 0) {
          return url.substring(0, url.indexOf('#'));
        } else {
          return url;
        }
    },
    /** api: method[protocolToCSS] 
     *  
     *  Provide a mapping between various GeoNetwork protocol and mime types and the CSS icon class. Return a generic class if we don't have a mapping.
     */
    protocolToCSS: function(type, useDownload) {
      var typesAndProtocols = {
        'application/vnd.ogc.wms_xml': 'addLayerIcon',
        'application/vnd.ogc.wmc': 'addLayerIcon',
        'OGC:WMS': 'addLayer',
        'OGC:WMS-1.1.1-http-get-map': 'addLayerIcon',
        'OGC:WMS-1.3.0-http-get-map': 'addLayerIcon',
        'ACCESS MAP VIEWER': 'addLayer',
        'application/vnd.google-earth.kml+xml': 'md-mn-kml',
        'application/zip': 'md-mn-zip',
        'WWW:DOWNLOAD-1.0-http--download': 'md-mn-download',
        'application/x-compressed': 'md-mn-zip',
        'text/html': 'md-mn-www',
        'text/plain': 'md-mn-www',
        'bookmark': 'md-mn-bookmark'
      };

      var defaultCSS = 'md-mn-www';
      if (useDownload) defaultCSS = 'md-mn-download';
      return typesAndProtocols[type] || defaultCSS;
    },
    // TODO : add function to compute color map
    defaultColorMap: [
                       "#2205fd", 
                       "#28bc03", 
                       "#bc3303", 
                       "#e4ff04", 
                       "#ff04a0", 
                       "#a6ff96", 
                       "#408d5d", 
                       "#7d253e", 
                       "#2ce37e", 
                       "#10008c", 
                       "#ff9e05", 
                       "#ff7b5d", 
                       "#ff0000", 
                       "#00FF00"],
   /** api: method[generateColorMap] 
    *  :param classes: integer Number of classes
    *  
    *   Return a random color map
    */
   generateColorMap: function (classes) {
        var colors = [];
        for (var i = 0; i < classes; i++) {
            // http://paulirish.com/2009/random-hex-color-code-snippets/
            colors[i] = '#'+('00000'+(Math.random()*(1<<24)|0).toString(16)).slice(-6);
        }
        return colors;
    },
    /** api: method[updateHeadInfo] 
     *  :param info: Object with the following properties
     *  
     *   - title: a title properties to be use as document title
     *   
     *   - meta: an Object of element to be added 
     *  as meta tags.
     *  
     *   - tagsToRemove: an Object of META name to remove (default is subject, author, keywords).
     *  
     */
    updateHeadInfo: function (info) {
        if (info && info.title) {
            document.title = info.title;
        }
        GeoNetwork.Util.removeMetaTags(info.tagsToRemove || {'subject': true, 'author': true, 'keywords': true});
        
        if (info) {
            for (var key in info.meta) {
                if (info.meta.hasOwnProperty(key)) {
                    var values = info.meta[key];
                    Ext.each(values, function (item) {
                        GeoNetwork.Util.addMetaTag(key, item);
                    });
                }
            }
        }
    },
    /** api: method[addMetaTag] 
     *  :param name: the name of the META tag
     *  :param content: the content of the META tag
     *  
     *  Add a META tag with name and content to the HEAD.
     *  
     */
    addMetaTag: function (name, content) {
        var meta = document.createElement('meta');
        meta.name = name;
        meta.content = content;
        document.getElementsByTagName('head')[0].appendChild(meta);
    },
    /** api: method[removeMetaTags] 
     *  :param tagToRemove: Object with the list of tag to remove
     *  
     *  Remove all META tags from HEAD which names match one of the
     *  tag to remove.
     *  
     */
    removeMetaTags: function (tagsToRemove) {
        var metas = Ext.DomQuery.jsSelect('head > meta');
        Ext.each(metas, function (meta) {
            var name = meta.getAttribute('name');
            if (tagsToRemove[name]) {
                Ext.get(meta).remove();
            }
        });
    },
    /** api: method[buildPermalinkMenu] 
     *  :param l: String or Function If String the link is added as is, if a function
     *  the function is called on 'show' event
     *  :param scope: Object The scope on which the l function is called.
     *  
     *  Create a permalink menu which is updated on show.
     *  
     *  TODO : maybe move on widget package - this is GUI related?
     *  
     *   Return Ext.menu.Menu
     */
    buildPermalinkMenu: function (l, scope) {
        var menu = new Ext.menu.Menu();
        var permalinkMenu = new Ext.menu.TextItem({text: '<input/><br/><a>&nbsp;</a>'});
        menu.add(
                '<b class="menu-title">' + OpenLayers.i18n('permalinkInfo') + '</b>',
                permalinkMenu
            );
        // update link when item is displayed
        var updatePermalink = function() {
            var link = l;
            if (typeof(l) == 'function') {
                link = l.apply(scope);
            }
            var id = 'permalink-' + permalinkMenu.getId();
            permalinkMenu.update('<input id="' + id + '" value="' + link + '"/>'
                + '</br>'
                + '<a href="' + link + '">Link</a>', 
                true, 
                // Select permalink input for user to easily copy/paste link
                function() {
                    // On IE8, select() on an element scroll to top of page, why ?
                    if (!Ext.isIE8) {
                        // update callback is not really called after update
                        // so add a short timeout TODO
                        setTimeout(function(){
                            var e = Ext.get(id);
                            if (e) {
                                e.dom.select();
                            }
                        }, 100);
                    }
            });
            
        };
        // onstatechange does not work because the menu item may be not be rendered
        //this.permalinkProvider.on('statechange', onStatechange, this.permalinkMenu);
        menu.on('show', updatePermalink, scope);
        return new Ext.Button({
            iconCls: 'linkIcon',
            menu: menu
        });
    }
};
