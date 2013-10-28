// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterfilesystem = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "filesystem",
            "owner" : [],
            "ownerGroup" : [],
            "site" : {
                "name" : "",
                "uuid" : "",
                "directory" : "/filesystem/path",
                "recurse" : "true",
                "nodelete" : "true",
                "checkFileLastModifiedForUpdate" : "true",
                "icon" : "blank.gif"
            },
            "content" : {
                "validate" : "false",
                "importxslt" : "none"
            },
            "options" : {
                "every" : "0 0 0 ? * *",
                "oneRunOnly" : "false",
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
                "running" : "false"
            }
        };
    },
    buildResponse : function(h, $scope) {
        var body = '<node id="' + h['@id'] + '" ' 
                + '    type="' + h['@type'] + '">' 
                + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>' 
                + '  <site>' 
                + '    <name>' + h.site.name + '</name>' 
                + '    <recurse>' + h.site.recurse + '</recurse>'
                + '    <nodelete>' + h.site.nodelete + '</nodelete>'
                + '    <checkFileLastModifiedForUpdate>' + h.site.checkFileLastModifiedForUpdate + '</checkFileLastModifiedForUpdate>'
                + '    <directory>' + h.site.directory + '</directory>'
                + '    <icon>' + h.site.icon + '</icon>'
                + '  </site>' 
                + '  <options>' 
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>' 
                + '    <every>' + h.options.every + '</every>' 
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