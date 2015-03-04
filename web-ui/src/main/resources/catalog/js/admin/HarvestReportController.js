(function() {
  goog.provide('gn_harvest_report_controller');

  
  var module = angular.module('gn_harvest_report_controller',
      []);
  
  /**
   * GnHarvestReportController provides management interface
   * for report on harvesters.
   *
   */
  module.controller('GnHarvestReportController', [
	'$scope', '$http', '$translate', '$injector', '$rootScope', 'gnUtilityService',
	function($scope, $http, $translate, $injector, $rootScope) {
//		$scope.isLoadingHarvester = false;
//		$scope.harvesterTypes = {};
//	    $scope.harvesters = null;
	      
//		function loadHarvesters() {
//	        $scope.isLoadingHarvester = true;
//	        $scope.harvesters = null;
//	        return $http.get('admin.harvester.list?_content_type=json').success(function(data) {
//	          if (data != 'null') {
//	            $scope.harvesters = data;
//	            gnUtilityService.parseBoolean($scope.harvesters);
//	          }
//	          $scope.isLoadingHarvester = false;
//	        }).error(function(data) {
//	          // TODO
//	          $scope.isLoadingHarvester = false;
//	        });
//	      }
		/*
		function loadHarvesterTypes() {
	        $http.get('admin.harvester.info?_content_type=json&type=harvesterTypes',
	            {cache: true})
	          .success(function(data) {
	              angular.forEach(data[0], function(value) {
	                $scope.harvesterTypes[value] = {
	                  label: value,
	                  text: $translate('harvester-' + value)
	                };
	                $.getScript('../../catalog/templates/admin/harvest/type/' +
	                    value + '.js')
	                .done(function(script, textStatus) {
	                      $scope.$apply(function() {
	                        $scope.harvesterTypes[value].loaded = true;
	                      });
	                      // FIXME: could we make those harvester specific
	                      // function a controller
	                    })
	                .fail(function(jqxhr, settings, exception) {
	                      $scope.harvesterTypes[value].loaded = false;
	                    });
	              });
	            }).error(function(data) {
	              // TODO
	            });
	      }
	      */
		
//		if ($scope.harverters == null ) {
//			$scope.$parent.loadHarvesters();
//		}
//		if ($scope.harvesterTypes == {}) {
//			loadHarvesterTypes();
//		}
	}]);
})();
