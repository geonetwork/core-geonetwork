// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterthredds = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "thredds",
            "owner": [""],
            "ownerGroup": [""],
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
                "icon" : "blank.png"
            },
            "content" : {
            },
            "options" : {
                "every" : "0 0 0 ? * *",
                "oneRunOnly" : false,
                "overrideUuid" : "SKIP",
                "status" : "active",
                "lang" : "eng",
                "topic" : "oceans",
                "createServiceMd" : true,
                "outputSchema" : "iso19139",
                "datasetTitle" : "",
                "datasetAbstract" : "",
                "serviceCategory" : "",
                "datasetCategory" : ""
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
                + '    <createServiceMd>' + h.options.createServiceMd + '</createServiceMd>'
                + '    <outputSchema>' + h.options.outputSchema + '</outputSchema>'
                + '    <datasetTitle>' + h.options.datasetTitle + '</datasetTitle>'
                + '    <datasetAbstract>' + h.options.datasetAbstract + '</datasetAbstract>'
                + '    <serviceCategory>' + h.options.serviceCategory + '</serviceCategory>'
                + '    <datasetCategory>' + h.options.datasetCategory + '</datasetCategory>'
                + '  </options>'
                + '  <content>'
                + '  </content>'
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};
