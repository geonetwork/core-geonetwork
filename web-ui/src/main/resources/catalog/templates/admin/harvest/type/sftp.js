// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestersftp = {
  createNew: function () {
    return {
      "@id": "",
      "@type": "sftp",
      "owner": [],
      "ownerGroup": [],
      "ownerUser": [""],
      "site": {
        "name": "",
        "uuid": "",
        "server": "",
        "account": {
          "username": [],
          "password": []
        },
        "icon": "blank.png",
        "xslfilter": []
      },
      "content": {
        "validate": "NOVALIDATION",
        "importxslt": "none",
        "translateContent": false,
        "translateContentLangs": "",
        "translateContentFields": ""
      },
      "options": {
        "every": "0 0 0 ? * *",
        "oneRunOnly": false,
        "status": "active",
        "recurse": true,
        "overrideUuid": "SKIP",
        "folder": "",
        "useAuthKey": true,
        "typeAuthKey": "RSA"
      },
      "ifRecordExistAppendPrivileges": false,
      "privileges": [{
        "@id": "1",
        "operation": [{
          "@name": "view"
        }, {
          "@name": "dynamic"
        }]
      }],
      "categories": [{'@id': ''}],
      "info": {
        "lastRun": [],
        "running": false
      }
    };
  },
  buildResponse: function (h, $scope) {
    var body = '<node id="' + h['@id'] + '" '
      + '    type="' + h['@type'] + '">'
      + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>'
      + '  <ownerUser><id>' + h.ownerUser[0] + '</id></ownerUser>'
      + '  <site>'
      + '    <name>' + h.site.name + '</name>'
      + '    <server>' + h.site.server + '</server>'
      + '    <port>' + h.site.port + '</port>'
      + '    <icon>' + h.site.icon + '</icon>'
      + '    <xslfilter>'
      + (h.site.xslfilter[0] ? h.site.xslfilter.replace(/&/g, '&amp;') : '')
      + '</xslfilter>'
      + '  </site>'
      + '    <account>'
      + '      <use>' + h.site.account.use + '</use>'
      + '      <username>' + h.site.account.username + '</username>'
      + '      <password>' + h.site.account.password + '</password>'
      + '    </account>'
      + '  <options>'
      + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
      + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
      + '    <every>' + h.options.every + '</every>'
      + '    <status>' + h.options.status + '</status>'
      + '    <folder>' + h.options.folder + '</folder>'
      + '    <recurse>' + h.options.recurse + '</recurse>'
      + '    <useAuthKey>' + h.options.useAuthKey + '</useAuthKey>'
      + '    <publicKey>' + h.options.publicKey + '</publicKey>'
      + '    <typeAuthKey>' + h.options.typeAuthKey + '</typeAuthKey>'
      + '  </options>'
      + '  <content>'
      + '    <validate>' + h.content.validate + '</validate>'
      + '    <importxslt>' + h.content.importxslt + '</importxslt>'
      + '    <translateContent>' + _.escape(h.content.translateContent) + '</translateContent>'
      + '    <translateContentLangs>' + _.escape(h.content.translateContentLangs) + '</translateContentLangs>'
      + '    <translateContentFields>' + _.escape(h.content.translateContentFields) + '</translateContentFields>'
      + '  </content>'
      + $scope.buildResponseGroup(h)
      + $scope.buildResponseCategory(h) + '</node>';
    return body;
  }
};
