// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestergeonetwork = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "geonetwork",
            "owner": [""],
            "ownerGroup": [""],
            "site":   {
              "name": "",
              "uuid": "",
              "account":     {
                "use": "false",
                "username": "",
                "password": ""
              },
              "host": [],
              "createRemoteCategory": "false",
              "mefFormatFull": "false",
              "xslfilter": []
            },
            "content":   {
              "validate": "false",
              "importxslt": "none"
            },
            "options":   {
              "every": "0 0 0 ? * *",
              "oneRunOnly": "false",
              "status": ""
            },
            "searches": [],
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
              "running": "false"
            }
          };
    },
    buildResponse : function(h, $scope) {
        var body = '<node id="' + h['@id'] + '" ' 
                + '    type="' + h['@type'] + '">' 
                + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>' 
                + '  <site>' 
                + '    <name>' + h.site.name + '</name>' 
                + '    <host>' + h.site.host + '</host>'
                + '    <createRemoteCategory>' + h.site.createRemoteCategory + '</createRemoteCategory>' 
                + '    <icon>' + h.site.icon + '</icon>' 
                + '    <mefFormatFull>' + h.site.mefFormatFull + '</mefFormatFull>' 
                + '    <xslfilter>' + h.site.xslfilter + '</xslfilter>' 
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username[0] + '</username>' 
                + '      <password>' + h.site.account.password[0] + '</password>' 
                + '    </account>'
                + '  </site>' 
                + '  <searches/>'
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