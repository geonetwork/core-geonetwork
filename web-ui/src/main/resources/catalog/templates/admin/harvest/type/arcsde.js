// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterarcsde = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "arcsde",
            "owner": [""],
            "ownerGroup": [""],
            "site":   {
              "name": "",
              "translations": {},
              "uuid": "",
              "account":     {
                "use": true,
                "username": "",
                "password": ""
              },
              "server": "",
              "port": "",
              "username": "",
              "password": "",
              "database": ""
            },
            "content":   {
              "validate": "NOVALIDATION"
            },
            "options":   {
              "every": "0 0 0 ? * *",
              "oneRunOnly": false,
              "status": ""
            },
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
                + '  <site>' 
                + '    <name>' + h.site.name + '</name>' 
                + $scope.buildTranslations(h)
                + '    <server>' + h.site.server + '</server>'
                + '    <port>' + h.site.port + '</port>' 
                + '    <icon>' + h.site.icon + '</icon>' 
                + '    <database>' + h.site.database + '</database>' 
                + '    <username>' + h.site.account.username + '</username>' 
                + '    <password>' + h.site.account.password + '</password>' 
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
                + '  </content>' 
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};