// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterz3950 = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "z3950",
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
                "query" : "",
                "repositories" : [{
                    "@id": ""
                  }],
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
                "topic" : "",
                "createThumbnails" : true,
                "useLayer" : true,
                "useLayerMd" : true,
                "datasetCategory" : "",
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
                + '    <query>' + h.site.query + '</query>'
                + '    <repositories><repository id="' + h.site.repositories[0]['@id'] + '"/></repositories>'
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