// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterfilesystem = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "filesystem",
            "owner" : [],
            "ownerGroup" : [],
            "ownerUser": [""],
            "site" : {
                "name" : "",
                "uuid" : "",
                "directory" : "/filesystem/path",
                "recurse" : true,
                "nodelete" : true,
                "checkFileLastModifiedForUpdate" : true,
                "recordType" : 'n',
                "icon" : "blank.png",
                "beforeScript": ""
            },
            "content" : {
                "validate" : "NOVALIDATION",
                "importxslt" : "none"
            },
            "options" : {
                "every" : "0 0 0 ? * *",
                "oneRunOnly" : false,
                "overrideUuid" : "SKIP",
                "status" : "active"
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
                + '    <recurse>' + h.site.recurse + '</recurse>'
                + '    <nodelete>' + h.site.nodelete + '</nodelete>'
                + '    <checkFileLastModifiedForUpdate>' + h.site.checkFileLastModifiedForUpdate + '</checkFileLastModifiedForUpdate>'
                + '    <directory>' + h.site.directory + '</directory>'
                + '    <recordType>' + h.site.recordType + '</recordType>'
                + '    <icon>' + h.site.icon + '</icon>'
                + '    <beforeScript>' + h.site.beforeScript + '</beforeScript>'
                + '  </site>'
                + '  <options>'
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
                + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
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
