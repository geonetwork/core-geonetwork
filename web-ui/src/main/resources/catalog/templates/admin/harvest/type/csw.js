// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestercsw = {
  createNew : function() {
    return {
      "@id" : "",
      "@type" : "csw",
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
        "capabilitiesUrl" : "http://",
        "xpathFilter" : "",
        "rejectDuplicateResource" : false,
        "xslfilter": [],
        "outputSchema": "http://www.isotc211.org/2005/gmd",
        "sortBy": "identifier:A",
        "queryScope": "local",
        "hopCount": 2
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
      "ifRecordExistAppendPrivileges": false,
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
  buildResponseCSWBBOXFilter : function($scope) {
    var body = '';

    if ($scope.harvesterSelected.bboxFilter) {

      if (!isNaN($scope.harvesterSelected.bboxFilter['bbox-xmin']) &&
          !isNaN($scope.harvesterSelected.bboxFilter['bbox-ymin']) &&
          !isNaN($scope.harvesterSelected.bboxFilter['bbox-xmax']) &&
          !isNaN($scope.harvesterSelected.bboxFilter['bbox-ymax'])) {
        body += '<bboxFilter>' +
          '<bbox-xmin>' + $scope.harvesterSelected.bboxFilter['bbox-xmin'] + '</bbox-xmin>' +
          '<bbox-ymin>' + $scope.harvesterSelected.bboxFilter['bbox-ymin'] + '</bbox-ymin>' +
          '<bbox-xmax>' + $scope.harvesterSelected.bboxFilter['bbox-xmax'] + '</bbox-xmax>' +
          '<bbox-ymax>' + $scope.harvesterSelected.bboxFilter['bbox-ymax'] + '</bbox-ymax>' +
          '</bboxFilter>';
      }
    }

    return body;
  },
  buildResponseCSWFilter : function($scope) {
    var body = '';
    if ($scope.harvesterSelected.filters) {
      for (var i = 0; i < $scope.harvesterSelected.filters.length; i++){
        var filter = $scope.harvesterSelected.filters[i];

        body += '<filter>' +
          '<field>' + filter.field + '</field>' +
          '<operator>' + filter.operator + '</operator>' +
          '<value>' + filter.value + '</value>' +
          '<condition>' + filter.condition + '</condition>' +
          '</filter>';
      }
    }

    return '<filters>' + body + '</filters>';
  },
  buildResponse : function(h, $scope) {
    var body = '<node id="' + h['@id'] + '" '
      + '    type="' + h['@type'] + '">'
      + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>'
      + '  <ownerUser><id>' + h.ownerUser[0] + '</id></ownerUser>'
      + '  <site>'
      + '    <name>' + h.site.name + '</name>'
      + '    <rejectDuplicateResource>' + h.site.rejectDuplicateResource + '</rejectDuplicateResource>'
      + '    <capabilitiesUrl>' + h.site.capabilitiesUrl.replace(/&/g, '&amp;') + '</capabilitiesUrl>'
      + '    <icon>' + h.site.icon + '</icon>'
      + '    <account>'
      + '      <use>' + h.site.account.use + '</use>'
      + '      <username>' + h.site.account.username + '</username>'
      + '      <password>' + h.site.account.password + '</password>'
      + '    </account>'
      + '    <xpathFilter>' + h.site.xpathFilter + '</xpathFilter>'
      + '    <xslfilter>' + _.escape(h.site.xslfilter) + '</xslfilter>'
      + '    <outputSchema>' + h.site.outputSchema + '</outputSchema>'
      + '    <sortBy>' + h.site.sortBy + '</sortBy>'
      + '    <queryScope>' + h.site.queryScope + '</queryScope>'
      + '    <hopCount>' + h.site.hopCount + '</hopCount>'
      + '  </site>'
      + gnHarvestercsw.buildResponseCSWBBOXFilter($scope)
      + gnHarvestercsw.buildResponseCSWFilter($scope)
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
