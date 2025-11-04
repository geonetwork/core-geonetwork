// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestersimpleurl = {
  createNew : function() {
    return {
      "@id" : "",
      "@type" : "simpleurl",
      "owner" : [],
      "ownerGroup" : [],
      "ownerUser": [""],
      "site" : {
        "name" : "",
        "uuid" : "",
        "icon" : "blank.png",
        "account" : {
          "use" : false,
          "username" : [],
          "password" : []
        },
        "url" : "http://",
        "apiKeyHeader" : "",
        "apiKey" : "",
        "loopElement" : "",
        "numberOfRecordPath": "",
        "pageSizeParam": "",
        "pageFromParam": "",
        "recordIdPath": "",
        "toISOConversion": ""
      },
      "content" : {
        "validate" : "NOVALIDATION",
        "batchEdits" : "",
        "translateContent": false,
        "translateContentLangs": "",
        "translateContentFields": ""
      },
      "options" : {
        "every" : "0 0 0 ? * *",
        "oneRunOnly" : false,
        "overrideUuid": "SKIP",
        "status" : "active"
      },
      "privileges" : [ {
        "@id" : "1",
        "operation" : [ {
          "@name" : "view"
        }, {
          "@name" : "dynamic"
        } ]
      } ],
      "categories" : [],
      "info" : {
        "lastRun" : [],
        "running" : false
      }
    };
  },
  buildResponseCSWSearch : function($scope) {
    var body = '';
    if ($scope.harvesterSelected.searches) {
      for(var tag in $scope.harvesterSelected.searches[0]) {
        if($scope.harvesterSelected.searches[0].hasOwnProperty(tag)) {
          var value = $scope.harvesterSelected.searches[0][tag].value;
          // Save all values even if empty
          // XML to JSON does not convert single child to Object but Array
          // In that situation, saving only one parameter will make this
          // happen and then search criteria name which is the tag name
          // will be lost.
          //                if (value) {
          body += '<' + tag + '>' + value + '</' + tag + '>';
          //            }
        }
      }
    }
    return '<searches><search>' + body + '</search></searches>';
  },
  buildResponse : function(h, $scope) {
    var body = '<node id="' + h['@id'] + '" '
      + '    type="' + h['@type'] + '">'
      + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>'
      + '  <ownerUser><id>' + h.ownerUser[0] + '</id></ownerUser>'
      + '  <site>'
      + '    <name>' + h.site.name + '</name>'
      + '    <rejectDuplicateResource>' + h.site.rejectDuplicateResource + '</rejectDuplicateResource>'
      + '    <url>' + h.site.url.replace(/&/g, '&amp;') + '</url>'
      + '    <icon>' + h.site.icon + '</icon>'
      + '    <account>'
      + '      <use>' + h.site.account.use + '</use>'
      + '      <username>' + h.site.account.username + '</username>'
      + '      <password>' + h.site.account.password + '</password>'
      + '    </account>'
      + '    <apiKeyHeader>' + h.site.apiKeyHeader + '</apiKeyHeader>'
      + '    <apiKey>' + h.site.apiKey + '</apiKey>'
      + '    <loopElement>' + h.site.loopElement + '</loopElement>'
      + '    <numberOfRecordPath>' + h.site.numberOfRecordPath + '</numberOfRecordPath>'
      + '    <recordIdPath>' + h.site.recordIdPath + '</recordIdPath>'
      + '    <pageFromParam>' + h.site.pageFromParam + '</pageFromParam>'
      + '    <pageSizeParam>' + h.site.pageSizeParam + '</pageSizeParam>'
      + '    <toISOConversion>' + h.site.toISOConversion + '</toISOConversion>'
      + '  </site>'
      + gnHarvestersimpleurl.buildResponseCSWSearch($scope)
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
