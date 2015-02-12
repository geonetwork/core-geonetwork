// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterz3950Config = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "z3950Config",
            "owner": [""],
            "ownerGroup": [""],
            "site":   {
              "name": "",
              "translations": {},
              "uuid": "",
              "account":     {
                "use": false,
                "username": "",
                "password": ""
              },
              "host": "",
              "port": "2100"
            },
            "content":   {
            },
            "options":   {
              "every": "0 0 0 ? * *",
              "oneRunOnly": false,
              "status": "",
              "clearConfig": false
            },
            "searches": [{
                "freeText": "",
                "title": "",
                "abstract": "",
                "keywords": "",
                "category": "z3950Servers"
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
                + '  <site>' 
                + '    <name>' + h.site.name + '</name>'
                + $scope.buildTranslations(h)
                + '    <host>' + h.site.host + '</host>'
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
                + '      <category>z3950Servers</category>'
                + '    </search>'
                + '  </searches>'
                + '  <options>' 
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>' 
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '    <clearConfig>' + h.options.clearConfig + '</clearConfig>' 
                + '  </options>' 
                + '  <content>'
                + '  </content>' 
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};