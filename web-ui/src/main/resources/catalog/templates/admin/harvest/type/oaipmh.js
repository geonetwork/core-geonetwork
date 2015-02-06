// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesteroaipmh = {
    createNew : function() {
        return {
            "@id": "",
            "@type": "oaipmh",
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
              "url": "",
              "icon" : "blank.gif"
            },
            "options":   {
              "every": "0 0 0 ? * *",
              "oneRunOnly": false,
              "status": ""
            },
            "searches": [{
                "from": "",
                "until": "",
                "prefix": "",
                "set": ""
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
                + '    <url>' + h.site.url.replace(/&/g, '&amp;') + '</url>'
                + '    <icon>' + h.site.icon + '</icon>' 
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>' 
                + '      <password>' + h.site.account.password + '</password>' 
                + '    </account>'
                + '  </site>' 
                + '  <searches>'
                + '    <search>'
                + '      <from>' + (h.searches[0].from || '') + '</from>'
                + '      <until>' + (h.searches[0].until || '') + '</until>'
                + '      <set>' + (h.searches[0].set || '') + '</set>'
                + '      <prefix>' + (h.searches[0].prefix || '') + '</prefix>'
                + '    </search>'
                + '  </searches>'
                + '  <options>' 
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>' 
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '  </options>' 
                + '  <content>'
                + '  </content>' 
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};