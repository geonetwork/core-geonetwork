// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterwebdav = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "webdav",
            "owner" : [],
            "ownerGroup" : [],
            "site" : {
                "name" : "",
                "translations": {},
                "uuid" : "",
                "url" : "http://",
                "account" : {
                    "use" : false,
                    "username" : [],
                    "password" : []
                },
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
                "recurse" : true,
                "subtype" : "waf"
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
                + '  </site>' 
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>' 
                + '      <password>' + h.site.account.password + '</password>' 
                + '    </account>'
                + '  <options>' 
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>' 
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '    <recurse>' + h.options.recurse + '</recurse>'
                + '    <subtype>' + h.options.subtype + '</subtype>'
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