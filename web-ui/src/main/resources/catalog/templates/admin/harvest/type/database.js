// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterdatabase = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "database",
            "owner": [""],
            "ownerGroup": [""],
            "ownerUser": [""],
            "site":   {
              "name": "",
              "uuid": "",
              "icon" : "blank.png",
              "account":     {
                "use": true,
                "username" : [],
                "password" : []
              },
              "server": "",
              "port": "",
              "database": "",
              "tableName": "",
              "metadataField": "",
              "databaseType": ""
            },
            "filter": {
              "field": "",
              "value": "",
              "operator": "LIKE"
            },
            "content":   {
              "validate": "NOVALIDATION",
              "batchEdits" : "",
              "translateContent": false,
              "translateContentLangs": "",
              "translateContentFields": ""
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
                + '    <tableName>' + h.site.tableName + '</tableName>'
                + '    <metadataField>' + h.site.metadataField + '</metadataField>'
                + '    <databaseType>' + h.site.databaseType + '</databaseType>'
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>'
                + '      <password>' + h.site.account.password + '</password>'
                + '    </account>'
                + '  </site>'
                + '  <filter>'
                + '    <field>' + h.filter.field + '</field>'
                + '    <value>' + h.filter.value + '</value>'
                + '    <operator>' + h.filter.operator + '</operator>'
                + '  </filter>'
                + '  <options>'
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
                + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '  </options>'
                + '  <content>'
                + '    <validate>' + h.content.validate + '</validate>'
                + '    <batchEdits><![CDATA[' + (h.content.batchEdits == '' ? '[]' : h.content.batchEdits) + ']]></batchEdits>'
                + '    <translateContent>' + _.escape(h.content.translateContent) + '</translateContent>'
                + '    <translateContentLangs>' + _.escape(h.content.translateContentLangs) + '</translateContentLangs>'
                + '    <translateContentFields>' + _.escape(h.content.translateContentFields) + '</translateContentFields>'
                + '  </content>'
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }


};
