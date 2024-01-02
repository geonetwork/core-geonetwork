// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterarcsde = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "arcsde",
            "owner": [""],
            "ownerGroup": [""],
            "ownerUser": [""],
            "site":   {
              "name": "",
              "uuid": "",
              "icon" : "blank.png",
              "account":     {
                "use": true,
                "username": "",
                "password": ""
              },
              "server": "",
              "port": "",
              "username": "",
              "password": "",
              "database": "",
              "version": "9",
              "connectionType": "ARCSDE",
              "databaseType": ""
            },
            "content":   {
              "validate": "NOVALIDATION"
            },
            "options":   {
              "every": "0 0 0 ? * *",
              "oneRunOnly": false,
              "overrideUuid": "SKIP",
              "status": ""
            },
            "privileges": [{
              "@id": "1",
              "operation":     [
                {"@name": "view"},
                {"@name": "dynamic"}
              ]
            }],
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
                + '    <server>' + h.site.server + '</server>'
                + '    <port>' + h.site.port + '</port>'
                + '    <icon>' + h.site.icon + '</icon>'
                + '    <database>' + h.site.database + '</database>'
                + '    <username>' + h.site.account.username + '</username>'
                + '    <password>' + h.site.account.password + '</password>'
                + '    <version>' + h.site.version + '</version>'
                + '    <connectionType>' + h.site.connectionType + '</connectionType>'
                + '    <databaseType>' + h.site.databaseType + '</databaseType>'
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
                + '  </options>'
                + '  <content>'
                + '    <validate>' + h.content.validate + '</validate>'
                + '  </content>'
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }


};
