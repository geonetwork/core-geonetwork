Ext.namespace('GeoNetwork');
Ext.namespace('GeoNetwork.Bootstrap');

GeoNetwork.Bootstrap.run = function() {
    for(var p in GeoNetwork.Settings.bootstrap) {
       var element = document.createElement("input");
       element.setAttribute("type", 'hidden');
       element.setAttribute("value", GeoNetwork.Settings.bootstrap[p]);
       element.setAttribute("id", p);

       Ext.getBody().dom.appendChild(element);
    }
}