(function() {
  goog.provide('gn_harvest_controller');





  goog.require('gn_harvest_report_controller');
  goog.require('gn_harvest_settings_controller');
  goog.require('gn_harvester');

  var module = angular.module('gn_harvest_controller',
      ['gn_harvest_settings_controller',
       'gn_harvest_report_controller', 'gn_harvester']);


  /**
   *
   */
  module.controller('GnHarvestController', [
    '$scope', '$http', 'gnUtilityService',
    function($scope, $http, gnUtilityService) {
      $scope.isLoadingHarvester = false;
      $scope.harvesters = null;

      $scope.pageMenu = {
        folder: 'harvest/',
        defaultTab: 'harvest-settings',
        tabs:
            [{
              type: 'harvest-settings',
              label: 'harvester',
              icon: 'fa-dashboard',
              href: '#/harvest/harvest-settings'
            },{
              type: 'harvest-report',
              label: 'report',
              icon: 'fa-th',
              href: '#/harvest/harvest-report'
            }]
      };

      $scope.loadHarvesters = function() {
        $scope.isLoadingHarvester = true;
        $scope.harvesters = null;
        return $http.get('admin.harvester.list?_content_type=json&id=-1').
            success(
            function(data) {
              if (data != 'null') {
                $scope.harvesters = data;
                gnUtilityService.parseBoolean($scope.harvesters);
                pollHarvesterStatus();
              }
              $scope.isLoadingHarvester = false;
            }).error(function(data) {
              // TODO
              $scope.isLoadingHarvester = false;
            });
      };

      var getRunningHarvesterIds = function() {
        var runningHarvesters = [];
        for (var i = 0; $scope.harvesters &&
            i < $scope.harvesters.length; i++) {
          var h = $scope.harvesters[i];
          if (h.info.running) {
            runningHarvesters.push(h['@id']);
          }
        }

        return runningHarvesters;
      };
      var isPolling = false;
      var pollHarvesterStatus = function() {
        if (isPolling) {
          return;
        }
        var runningHarvesters = getRunningHarvesterIds();
        if (runningHarvesters.length == 0) {
          return;
        }
        isPolling = true;

        $http.get('admin.harvester.list?onlyInfo=true&_content_type=json&id=' +
            runningHarvesters.join('&id=')).success(
            function(data) {
              isPolling = false;
              if (data != 'null') {
                if (!angular.isArray(data)) {
                  data = [data];
                }
                var harvesterIndex = {};
                angular.forEach($scope.harvesters, function(oldH) {
                  harvesterIndex[oldH['@id']] = oldH;
                });

                for (var i = 0; i < data.length; i++) {
                  var h = data[i];
                  gnUtilityService.parseBoolean(h.info);
                  var old = harvesterIndex[h['@id']];
                  if (old && !angular.equals(old.info, h.info)) {
                    old.info = h.info;
                  }
                  if (old && !angular.equals(old.error, h.error)) {
                    old.error = h.error;
                  }
                }

                setTimeout(pollHarvesterStatus, 5000);
              }
            }).error(function(data) {
          isPolling = false;
        });
      };

      $scope.refreshHarvester = function() {
        $scope.loadHarvesters().then(function() {
          if ($scope.harvesterSelected) {
            // Select the clone
            angular.forEach($scope.harvesters, function(h) {
              if (h['@id'] === $scope.harvesterSelected['@id']) {
                $scope.selectHarvester(h);
              }
            });
          }
        });
      };

      $scope.loadHarvesters();
    }]);
})();
