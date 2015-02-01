(function() {
  goog.provide('gn_dashboard_status_controller');


  var module = angular.module('gn_dashboard_status_controller',
      []);


  /**
   *
   */
  module.controller('GnDashboardStatusController', [
    '$scope', '$routeParams', '$http', 'gnSearchManagerService',
    function($scope, $routeParams, $http, gnSearchManagerService) {
      $scope.healthy = undefined;

      $http.get('../../criticalhealthcheck').success(function(data) {
        $scope.healthy = true;
        $scope.healthcheck = data;
      }).error(function(data) {
        $scope.healthy = false;
        $scope.healthcheck = data;
      });

      $scope.indexMessages = function(md) {
        if (angular.isArray(md.idxMsg)) {
          return md.idxMsg;
        }

        return [md.idxMsg];
      };
      $scope.indexMessageTitle = function (errorMsg) {
        if (errorMsg === undefined) {
          return "Empty error message";
        }
        return errorMsg.split("|")[0];
      };
      $scope.indexMessageReason = function (errorMsg) {
        if (errorMsg === undefined) {
          return "Empty error message";
        }
        return errorMsg.split("|")[1];
      };
      $scope.rawIndexMessageDetail = function (errorMsg) {
        if (errorMsg === undefined) {
          return "Empty error message";
        }
        return errorMsg.split("|")[2]
      };
      $scope.indexMessageDetail = function (errorMsg) {
        var maxLine = 80,
            indentPattern = /(\s*).*/,
            detail = $scope.rawIndexMessageDetail(errorMsg);

        if (detail.trim() == '') {
          return '';
        }

        var lines = detail.split("\n");

        detail = "";

        var nextSpace = function (line) {
          for (var j = maxLine; j < line.length; j++) {
            if (' ' === line.charAt(j)) {
              return j;
            }
          }
          return line.length;
        };

        for (var i = 0; i < lines.length; i++) {
          var line = lines[i];
          var indent = indentPattern.exec(line)[1] + "    ";
          while(line.length > maxLine) {
            var ns = nextSpace(line);
            detail += line.substring(0, ns) + "\n";
            line = indent + line.substring(ns);
          }
          detail += line + "\n";
        }
        return detail;
      };
      $scope.searchObj = {
        params: {
          _indexingError: 1,
          sortBy: 'changeDate'
        }
      };
    }]);

})();
