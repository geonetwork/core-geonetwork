(function() {
  goog.provide('gn_openwis_monitoring_controller');

  var module = angular.module('gn_openwis_monitoring_controller', []);

  module.controller('GnOpenwisMonitoringController', [
      '$scope',
      '$routeParams',
      '$http',
      function($scope, $routeParams, $http) {
        $scope.options = [
            {
              id : 0,
              name : 'catalogStatistics'
            }, {
              id : 1,
              name : 'dataStatistics'
            }, {
              id : 2,
              name : 'cacheContents'
            }, {
              id : 3,
              name : 'cacheStatistics'
            }, {
              id : 4,
              name : 'globalReports'
            }, {
              id : 5,
              name : 'ingestedDataStatistics'
            }, {
              id : 6,
              name : 'recentEvents'
            }
        ];

        $scope.isSimple = true;

        $http.get($scope.url + 'xml.info?type=groups&_content_type=json')
            .success(function(data) {
              $scope.groups = [];
              $.each(data.group, function(index, group) {
                $scope.groups.push({
                  id : group["@id"],
                  name : group["name"]
                });
              });
            }).error(function(data) {
              $scope.groups = [
                {
                  id : 0,
                  name : 'error retrieving groups'
                }
              ];
            });

        $scope.monitorType = $scope.options[0].id;

        $scope.data = [];

        $scope.isGroupDisabled = function() {
          return $scope.options[$scope.monitorType].id == 1
              || $scope.options[$scope.monitorType].id > 4;
        }

        $scope.isMaxRecordsDisabled = function() {
          return $scope.options[$scope.monitorType].id == 0
              || $scope.options[$scope.monitorType].id == 2
              || $scope.options[$scope.monitorType].id == 3
              || $scope.options[$scope.monitorType].id == 4;
        }

        $scope.updateData = function() {

          $http.get(
              $scope.url + 'openwis.monitoring.get?monitorType='
                  + $scope.options[$scope.monitorType].name).success(
              function(data) {

                if (data && data.data && $.isArray(data.data)) {
                  $scope.data = data.data;
                  $scope.isSimple = false;
                } else {
                  $scope.isSimple = true;
                  $scope.data = data;
                }

                // $("#graphic").empty();
                //
                // if (!$scope.isSimple) {
                // var chart = nv.models.multiBarChart().reduceXTicks(true)
                // .rotateLabels(0).showControls(true);
                // chart.xAxis.tickFormat(d3.format(',f'));
                //
                // chart.yAxis.tickFormat(d3.format(',.1f'));
                //
                // d3.select('#graphic').datum({values:
                // $scope.data}).call(chart);
                // }
              }).error(function(data) {
            // TODO
          });
        }
      }
  ]);

})();
