// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterwfsfeatures = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "wfsfeatures",
            "owner" : [],
            "ownerGroup" : [],
            "site" : {
                "name" : "",
                "translations": {},
                "uuid" : "",
                "account" : {
                    "use" : false,
                    "username" : [],
                    "password" : []
                },
                "url" : "http://",
                "icon" : "blank.gif"
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
                "query" : "",
                "stylesheet" : "",
                "streamFeatures" : false,
                "createSubtemplates" : false,
                "templateId" : "",
                "recordsCategory": "",
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
                + '  <site>' 
                + '    <name>' + h.site.name + '</name>'
                + $scope.buildTranslations(h)
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
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '    <lang>' + h.options.lang + '</lang>' 
                + '    <query>' + h.options.query + '</query>'
                + '    <stylesheet>' + h.options.stylesheet + '</stylesheet>' 
                + '    <streamFeatures>' + h.options.streamFeatures + '</streamFeatures>' 
                + '    <createSubtemplates>' + h.options.createSubtemplates + '</createSubtemplates>' 
                + '    <templateId>' + h.options.templateId + '</templateId>'
                + '    <outputSchema>' + h.options.outputSchema + '</outputSchema>' 
                + '    <recordsCategory>' + h.options.recordsCategory + '</recordsCategory>' 
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