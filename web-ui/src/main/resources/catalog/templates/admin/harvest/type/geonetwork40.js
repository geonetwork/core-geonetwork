// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestergeonetwork40 = {
  createNew : function() {
    return {
      "@id": "",
      "@type": "geonetwork40",
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
        "node": "srv",
        "useChangeDateForUpdate": false,
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
        "overrideUuid": "SKIP",
        "status": ""
      },
      "searches": [{
        "freeText": "",
        "title": "",
        "abstract": "",
        "keywords": "",
        "source": {
          "uuid": [],
          "name": []
        }
      }],
      "ifRecordExistAppendPrivileges": false,
      "privileges": [{
        "@id": "1",
        "operation":     [
          {"@name": "view"},
          {"@name": "dynamic"}
        ]
      }],
      "categories" : [{'@id': ''}],
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
      + '    <node>' + h.site.node + '</node>'
      + '    <useChangeDateForUpdate>' + h.site.useChangeDateForUpdate + '</useChangeDateForUpdate>'
      + '    <createRemoteCategory>' + h.site.createRemoteCategory + '</createRemoteCategory>'
      + '    <icon>' + h.site.icon + '</icon>'
      + '    <mefFormatFull>' + h.site.mefFormatFull + '</mefFormatFull>'
      + '    <xslfilter>'
      + (h.site.xslfilter[0] ? h.site.xslfilter.replace(/&/g, '&amp;') : '')
      + '</xslfilter>'
      + '    <account>'
      + '      <use>' + h.site.account.use + '</use>'
      + '      <username>' + h.site.account.username + '</username>'
      + '      <password>' + h.site.account.password + '</password>'
      + '    </account>'
      + '  </site>'
      + '  <searches>'
      + '    <search>'
      + '      <freeText>' + ((h.searches[0] && h.searches[0].freeText) || '') + '</freeText>'
      + '      <title>' + ((h.searches[0] && h.searches[0].title) || '') + '</title>'
      + '      <abstract>' + ((h.searches[0] && h.searches[0]['abstract']) || '') + '</abstract>'
      + '      <keywords>' + ((h.searches[0] && h.searches[0].keywords) || '') + '</keywords>'
      + '      <source>'
      + '        <uuid>' + ((h.searches[0] && h.searches[0].source.uuid) || '') + '</uuid>'
      + '        <name/>'
      + '      </source>'
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
