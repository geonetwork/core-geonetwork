(function() {
  goog.provide('gn_harvest_report_controller');


  var module = angular.module('gn_harvest_report_controller',
      []);

  /**
   * GnHarvestReportController provides management interface
   * for report on harvesters.
   */
  module.controller('GnHarvestReportController', [
    '$scope',
    function($scope) {
      $scope.csvReport = function(event) {
        var json = [];
        var table = document.getElementById('harvestReport');
        var names = [];
        for (var i = 0, row; row = table.rows[i]; i++) {
          var obj = {};
          for (var j = 0, col; col = row.cells[j]; j++) {
            if (i == 0) {
              names[j] = col.innerText;
            } else {
              obj[names[j]] = col.innerText;
            }
          }
          if (i != 0) {
            json[i - 1] = obj;
          }
        }

        $scope.csvExport(json, event);
      };
    }]);
})();
