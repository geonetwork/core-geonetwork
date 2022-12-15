// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvestercsw2 = {
  createNew : function() {
    return {
      "@id" : "",
      "@type" : "csw2",
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
        "outputSchema": "http://www.isotc211.org/2005/gmd",
        "queryScope": "local",
        "hopCount": 2
      },
      "options" : {
        "every" : "0 0 0 ? * *",
        "oneRunOnly" : false,
        "overrideUuid": "SKIP",
        "status" : "inactive",
        "remoteHarvesterNestedServices": false,
        "numberOfRecordsPerRequest": 20,
        "errorConfigNextRecordsNotZero": false,
        "errorConfigNextRecordsBadValue": true,
        "errorConfigFewerRecordsThanRequested": true,
        "errorConfigTotalRecordsChanged": true,
        "errorConfigMaxPercentTotalRecordsChangedAllowed": 5,
        "errorConfigDuplicatedUuids": true,
        "processQueueType": "auto",
        "doNotSort": false,
        "processID": "",
        "executeLinkChecker": true
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
      "info" : {
        "lastRun" : [],
        "lastRunSuccess" : [],
        "elapsedTime" : [],
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
      + '    <capabilitiesUrl>' + h.site.capabilitiesUrl.replace(/&/g, '&amp;') + '</capabilitiesUrl>'
      + '    <icon>' + h.site.icon + '</icon>'
      + '    <account>'
      + '      <use>' + h.site.account.use + '</use>'
      + '      <username>' + h.site.account.username + '</username>'
      + '      <password>' + h.site.account.password + '</password>'
      + '    </account>'
      + '    <outputSchema>' + h.site.outputSchema + '</outputSchema>'
      + '    <queryScope>' + h.site.queryScope + '</queryScope>'
      + '    <hopCount>' + h.site.hopCount + '</hopCount>'
      + '  </site>'
      + '  <rawFilter>' + h.rawFilter + '</rawFilter>'
      + gnHarvestercsw.buildResponseCSWBBOXFilter($scope)
      + gnHarvestercsw.buildResponseCSWFilter($scope)
      + '  <options>'
      + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>'
      + '    <overrideUuid>' + h.options.overrideUuid + '</overrideUuid>'
      + '    <every>' + h.options.every + '</every>'
      + '    <status>' + h.options.status + '</status>'
      + '    <remoteHarvesterNestedServices>' + h.options.remoteHarvesterNestedServices + '</remoteHarvesterNestedServices>'
      + '    <numberOfRecordsPerRequest>' + h.options.numberOfRecordsPerRequest + '</numberOfRecordsPerRequest>'
      + '    <errorConfigNextRecordsNotZero>' + h.options.errorConfigNextRecordsNotZero + '</errorConfigNextRecordsNotZero>'
      + '    <errorConfigNextRecordsBadValue>' + h.options.errorConfigNextRecordsBadValue + '</errorConfigNextRecordsBadValue>'
      + '    <errorConfigFewerRecordsThanRequested>' + h.options.errorConfigFewerRecordsThanRequested + '</errorConfigFewerRecordsThanRequested>'
      + '    <errorConfigTotalRecordsChanged>' + h.options.errorConfigTotalRecordsChanged + '</errorConfigTotalRecordsChanged>'
      + '    <errorConfigMaxPercentTotalRecordsChangedAllowed>' + h.options.errorConfigMaxPercentTotalRecordsChangedAllowed + '</errorConfigMaxPercentTotalRecordsChangedAllowed>'
      + '    <errorConfigDuplicatedUuids>' + h.options.errorConfigDuplicatedUuids + '</errorConfigDuplicatedUuids>'
      + '    <processQueueType>' + h.options.processQueueType + '</processQueueType>'
      + '    <doNotSort>' + h.options.doNotSort + '</doNotSort>'
      + '    <processID>' + h.options.processID + '</processID>'
      + '    <executeLinkChecker>' + h.options.executeLinkChecker + '</executeLinkChecker>'
      + '  </options>'
      + $scope.buildResponseGroup(h) + '</node>';

    console.log(body);

    return body;
  }
};
