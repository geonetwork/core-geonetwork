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
            "searches": [{
                "freeText": "",
                "title": "",
                "abstract": "",
                "keywords": "",
                "digital": "",
                "hardcopy": "",
                "anyField": "",
                "anyValue": "",
                "source": {
                  "uuid": [],
                  "name": []
                }
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
                + '    <host>' + h.site.host.replace(/&/g, '&amp;') + '</host>'
                + '    <createRemoteCategory>' + h.site.createRemoteCategory + '</createRemoteCategory>' 
                + '    <icon>' + h.site.icon + '</icon>' 
                + '    <mefFormatFull>' + h.site.mefFormatFull + '</mefFormatFull>' 
                + '    <xslfilter>' + h.site.xslfilter + '</xslfilter>' 
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
                + '      <anyField>' + (h.searches[0].anyField || '') + '</anyField>'
                + '      <anyValue>' + (h.searches[0].anyValue || '') + '</anyValue>'
                + '      <source>'
                + '        <uuid>' + (h.searches[0].source.uuid || '') + '</uuid>'
                + '        <name/>'
                + '      </source>'
                + '    </search>'
                + '  </searches>'
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