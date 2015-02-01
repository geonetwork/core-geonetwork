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
      $scope.indexMessageTitle = function (md) {
        return md.idxMsg.split("|")[0];
      };
      $scope.indexMessageReason = function (md) {
        return md.idxMsg.split("|")[1];
      };
      $scope.indexMessageDetail = function (md) {
        var maxLine = 80,
            indentPattern = /(\s*).*/,
            detail = md.idxMsg.split("|")[2],
            lines = detail.split("\n");

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
