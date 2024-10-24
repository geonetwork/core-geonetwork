// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestergeoPREST = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "geoPREST",
            "owner": [""],
            "ownerGroup": [""],
            "ownerUser": [""],
            "site":   {
              "name": "",
              "uuid": "",
              "icon" : "blank.png",
              "account":     {
                "use": false,
                "username": "",
                "password": ""
              },
              "baseUrl": "http://"
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
                "freeText": ""
              }],
            "privileges": [{
              "@id": "1",
              "operation":     [
                {"@name": "view"},
                {"@name": "dynamic"}
              ]
            }],
            "info":   {
              "lastRun": [],
              "lastRunSuccess" : [],
              "elapsedTime" : [],
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
                + '    <baseUrl>' + h.site.baseUrl.replace(/&/g, '&amp;') + '</baseUrl>'
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
                + '    <importxslt>' + _.escape(h.content.importxslt) + '</importxslt>'
                + '  </content>'
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};
