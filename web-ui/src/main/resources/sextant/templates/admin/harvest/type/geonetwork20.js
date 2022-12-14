// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestergeonetwork20 = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "geonetwork20",
            "owner": [""],
            "ownerGroup": [""],
            "ownerUser": [""],
            "site":   {
              "name": "",
              "uuid": "",
              "account":     {
                "use": false,
                "username": "",
                "password": ""
              },
              "host": [],
              "createRemoteCategory": false,
              "mefFormatFull": false,
              "xslfilter": []
            },
            "content":   {
              "validate": "NOVALIDATION",
              "importxslt": "none"
            },
            "options":   {
              "every": "0 0 0 ? * *",
              "oneRunOnly": false,
              "overrideUuid" : "SKIP",
              "status": ""
            },
            "searches": [{
                "freeText": "",
                "title": "",
                "abstract": "",
                "keywords": "",
                "digital": "",
                "hardcopy": "",
                "siteId": ""
              }],
            "privileges": [{
              "@id": "1",
              "operation":     [
                {"@name": "view"},
                {"@name": "dynamic"}
              ]
            }],
            "groupsCopyPolicy": [],
            "info":   {
              "lastRun": [],
              "running": false
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
                + '    <host>' + h.site.host.replace(/&/g, '&amp;') + '</host>'
                + '    <icon>' + h.site.icon + '</icon>'
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>'
                + '      <password>' + h.site.account.password + '</password>'
                + '    </account>'
                + '  </site>'
                + '  <searches>'
                + '    <search>'
                + '      <freeText>' + h.searches[0].freeText + '</freeText>'
                + '      <title>' + (h.searches[0].title || '') + '</title>'
                + '      <abstract>' + (h.searches[0]['abstract'] || '') + '</abstract>'
                + '      <keywords>' + (h.searches[0].keywords || '') + '</keywords>'
                + '      <digital>' + (h.searches[0].digital || '') + '</digital>'
                + '      <hardcopy>' + (h.searches[0].hardcopy || '') + '</hardcopy>'
                + '      <siteId>' + (h.searches[0].siteId || '') + '</siteId>'
                + '    </search>'
                + '  </searches>'
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
