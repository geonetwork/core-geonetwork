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
        "rejectDuplicateResource" : false,
        "xslfilter": [],
        "outputSchema": "",
        "queryScope": "local",
        "hopCount": 2
      },
      "content" : {
        "validate" : "NOVALIDATION"
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
      + '    <capabilitiesUrl>' + h.site.capabilitiesUrl.replace(/&/g, '&amp;') + '</capabilitiesUrl>'
      + '    <icon>' + h.site.icon + '</icon>'
      + '    <account>'
      + '      <use>' + h.site.account.use + '</use>'
      + '      <username>' + h.site.account.username + '</username>'
      + '      <password>' + h.site.account.password + '</password>'
      + '    </account>'
      + '    <xslfilter>' + h.site.xslfilter + '</xslfilter>'
      + '    <outputSchema>' + h.site.outputSchema + '</outputSchema>'
      + '    <queryScope>' + h.site.queryScope + '</queryScope>'
      + '    <hopCount>' + h.site.hopCount + '</hopCount>'
      + '  </site>'
      + gnHarvestercsw.buildResponseCSWSearch($scope)
      + '  <options>'
      + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
      + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
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
