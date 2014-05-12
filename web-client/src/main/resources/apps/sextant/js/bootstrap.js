Ext.namespace('GeoNetwork');
Ext.namespace('GeoNetwork.Bootstrap');

GeoNetwork.Bootstrap.run = function() {
    for(p in GeoNetwork.Settings.bootsrap) {
       var element = document.createElement("input");
       element.setAttribute("type", 'hidden');
       element.setAttribute("value", GeoNetwork.Settings.bootsrap[p]);
       element.setAttribute("id", p);

       Ext.getBody().dom.appendChild(element);
    }
}