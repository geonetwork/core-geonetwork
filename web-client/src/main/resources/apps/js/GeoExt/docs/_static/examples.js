Ext.onReady(function() {
    var blocks = Ext.select("div.exampleblock");
    var loc = window.location.href;
    var exbase = "../examples/"
    if (/^http:\/\/(www\.)?geoext.org\/examples.html/.test(loc)) {
        exbase = "http://api.geoext.org/" + docversion + "/examples/";
    } else if (/^http:\/\/dev.geoext.org\/docs\/examples.html/.test(loc)) {
        exbase = "http://dev.geoext.org/trunk/geoext/examples/";
    }
    blocks.each(function(el) {
        el.wrap({
            tag: "a", 
            href: el.first().id.replace(
                /^example-(.*)/, 
                exbase + "$1.html"
            ),
            cls: "examplelink",
            target: "_blank"
        });
    });
});