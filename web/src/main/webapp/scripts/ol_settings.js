Ext.BLANK_IMAGE_URL = Env.url + '/scripts/ext/resources/images/default/s.gif';
OpenLayers.DOTS_PER_INCH = 90.71;
OpenLayers.ProxyHost = Env.url + '/proxy?url=';
OpenLayers.ImgPath = Env.url + '/scripts/openlayers/img/'
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

// Define a constant with the base url to the MapFish web service.
mapfish.SERVER_BASE_URL = Env.url + '/'; //'../../';

// Remove pink background when a tile fails to load
OpenLayers.Util.onImageLoadErrorColor = "transparent";

/**
 * Translates 639-2 code (Env.lang) to Openlayers lang code (usually 639-1 code)
 *
 */
function getOpenlayersLangCode() {
    switch(Env.lang) {
        case"cat":a="ca";
            break;
        case"dut":a="nl";
            break;
        case"eng":a="en";
            break;
        case"fin":a="fi";
            break;
        case"fre":a="fr";
            break;
        case"ger":a="de";
            break;
        case"ita":a="it";
            break;
        case"spa":a="es";
            break;
        case"nor":a="no";
            break;
        case"rus":a="ru";
            break;
        case"por":a="pt";
            break;
        case"chi":a="cn";
            break;
        default:a="en"
    }

    return a;
}

if (Env.lang) {
    OpenLayers.Lang.setCode(getOpenlayersLangCode());
    var s = document.createElement("script");
    s.type = 'text/javascript';
    s.src = Env.url + "/scripts/ext/locale/ext-lang-"+getOpenlayersLangCode()+".js";
    document.getElementsByTagName("head")[0].appendChild(s);
} else {
    OpenLayers.Lang.setCode(GeoNetwork.defaultLocale);
    var s = document.createElement("script");
    s.type = 'text/javascript';
    s.src = Env.url + "/scripts/ext/locale/ext-lang-"+GeoNetwork.defaultLocale+".js";
    document.getElementsByTagName("head")[0].appendChild(s);
}

OpenLayers.Util.onImageLoadError = function() {
    this._attempts = (this._attempts) ? (this._attempts + 1) : 1;
    if(this._attempts <= OpenLayers.IMAGE_RELOAD_ATTEMPTS) {
        this.src = this.src;
    } else {
        this.style.backgroundColor = OpenLayers.Util.onImageLoadErrorColor;
        this.style.display = "none";
    }
};




