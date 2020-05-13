// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterogcwxs = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "ogcwxs",
            "owner" : [],
            "ownerGroup" : [],
            "ownerUser": [""],
            "site" : {
                "name" : "",
                "uuid" : "",
                "account" : {
                    "use" : false,
                    "username" : [],
                    "password" : []
                },
                "url" : "http://",
                "ogctype" : "WMS1.3.0",
                "icon" : "blank.png"
            },
            "content" : {
                "validate" : "NOVALIDATION",
                "importxslt" : "none"
            },
            "options" : {
                "every" : "0 0 0 ? * *",
                "oneRunOnly" : false,
                "status" : "active",
                "lang" : "eng",
                "topic" : "",
                "createThumbnails" : "",
                "serviceTemplateUuid" : "",
                "datasetTemplateUuid" : "",
                "useLayer" : "",
                "useLayerMd" : "",
                "datasetCategory" : "",
                "overrideUuid" : "SKIP",
                "outputSchema" : "iso19139"
            },
            "privileges" : [ {
                "@id" : "1",
                "operation" : [ {
                    "@name" : "view"
                }, {
                    "@name" : "dynamic"
                } ]
            } ],
            "categories" : [{'@id': ''}],
            "info" : {
                "lastRun" : [],
                "running" : false
            }
        };
    },
    buildResponse : function(h, $scope) {
        var body = '<node id="' + h['@id'] + '" '
                + '    type="' + h['@type'] + '">'
                + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>'
                + '  <ownerUser><id>' + h.ownerUser[0] + '</id></ownerUser>'
                + '  <site>'
                + '    <name>' + h.site.name + '</name>'
                + '    <ogctype>' + h.site.ogctype + '</ogctype>'
                + '    <url>' + h.site.url.replace(/&/g, '&amp;') + '</url>'
                + '    <icon>' + h.site.icon + '</icon>'
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>'
                + '      <password>' + h.site.account.password + '</password>'
                + '    </account>'
                + '  </site>'
                + '  <options>'
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
                + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '    <lang>' + h.options.lang + '</lang>'
                + '    <topic>' + h.options.topic + '</topic>'
                + '    <createThumbnails>' + h.options.createThumbnails + '</createThumbnails>'
                + '    <serviceTemplateUuid>' + h.options.serviceTemplateUuid + '</serviceTemplateUuid>'
                + '    <datasetTemplateUuid>' + h.options.datasetTemplateUuid + '</datasetTemplateUuid>'
                + '    <useLayer>' + h.options.useLayer + '</useLayer>'
                + '    <useLayerMd>' + h.options.useLayerMd + '</useLayerMd>'
                + '    <datasetCategory>' + h.options.datasetCategory + '</datasetCategory>'
                + '    <outputSchema>' + h.options.outputSchema + '</outputSchema>'
                + '  </options>'
                + '  <content>'
                + '    <validate>' + h.content.validate + '</validate>'
                + '    <importxslt>' + h.content.importxslt + '</importxslt>'
                + '  </content>'
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};
