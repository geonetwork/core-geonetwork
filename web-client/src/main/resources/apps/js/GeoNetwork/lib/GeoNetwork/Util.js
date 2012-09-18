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
            ['pt', 'Рortuguês', 'por'], 
            ['ru', 'Русский', 'rus']
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

        // Update templates with new language texts
        new GeoNetwork.Templates().refreshTemplates();

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
        return url.substring(0, url.indexOf('?') || url.indexOf('#') || url.length);
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

    findContainerId: function(node) {
	  if(node == null) {
		  console.log('container id not found');
		  return null;
	  }
	  if(node.parentNode.tagName == 'DIV' && node.parentNode.id) {
	  	console.log('found container id: ' + node.parentNode.id);
	  	return node.parentNode.id;
	  }
	  return GeoNetwork.Util.findContainerId(node.parentNode);
    },
    
    getTopLeft: function (elm) {

		var x, y = 0;
		
		//set x to elm’s offsetLeft
		x = elm.offsetLeft;
		
		
		//set y to elm’s offsetTop
		y = elm.offsetTop;
		
		
		//set elm to its offsetParent
		elm = elm.offsetParent;
		
		
		//use while loop to check if elm is null
		// if not then add current elm’s offsetLeft to x
		//offsetTop to y and set elm to its offsetParent
		
		while(elm != null)
		{
		
		x = parseInt(x) + parseInt(elm.offsetLeft);
		y = parseInt(y) + parseInt(elm.offsetTop);
		elm = elm.offsetParent;
		}
		
		//here is interesting thing
		//it return Object with two properties
		//Top and Left
		
		return {Top:y, Left: x};
		
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
