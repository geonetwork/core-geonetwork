// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterwebdav = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "webdav",
            "owner" : [],
            "ownerGroup" : [],
            "ownerUser": [""],
            "site" : {
                "name" : "",
                "uuid" : "",
                "url" : "http://",
                "account" : {
                    "use" : false,
                    "username" : [],
                    "password" : []
                },
                "icon" : "blank.png"
            },
            "content" : {
                "validate" : "NOVALIDATION",
                "importxslt" : "none",
                "translateContent": false,
                "translateContentLangs": "",
                "translateContentFields": ""
            },
            "options" : {
                "every" : "0 0 0 ? * *",
                "oneRunOnly" : false,
                "status" : "active",
                "recurse" : true,
                "overrideUuid" : "SKIP",
                "subtype" : "waf"
            },
            "ifRecordExistAppendPrivileges": false,
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
                + '  </site>'
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>'
                + '      <password>' + h.site.account.password + '</password>'
                + '    </account>'
                + '  <options>'
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
                + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '    <recurse>' + h.options.recurse + '</recurse>'
                + '    <subtype>' + h.options.subtype + '</subtype>'
                + '  </options>'
                + '  <content>'
                + '    <validate>' + h.content.validate + '</validate>'
                + '    <importxslt>' + h.content.importxslt + '</importxslt>'
                + '    <translateContent>' + _.escape(h.content.translateContent) + '</translateContent>'
                + '    <translateContentLangs>' + _.escape(h.content.translateContentLangs) + '</translateContentLangs>'
                + '    <translateContentFields>' + _.escape(h.content.translateContentFields) + '</translateContentFields>'
                + '  </content>'
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};
