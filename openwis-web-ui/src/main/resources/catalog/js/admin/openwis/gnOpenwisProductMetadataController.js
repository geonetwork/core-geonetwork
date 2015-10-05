(function() {
  goog.provide('gn_openwis_productmetadata_controller');

  var module = angular.module('gn_openwis_productmetadata_controller', ['datatables']);

  module.controller('GnOpenwisProductMetadataController', [
      '$scope',
      '$routeParams',
      '$http',
      '$rootScope',
      'DTOptionsBuilder',
      'DTColumnBuilder',
      function($scope, $routeParams, $http, $rootScope, DTOptionsBuilder, DTColumnBuilder) {

        $scope.dtOptions = DTOptionsBuilder.newOptions()
          .withOption('ajax', {
            // Either you specify the AjaxDataProp here
            // dataSrc: 'data',
            url:  $scope.url + 'openwis.productmetadata.search',
            type: 'GET'
          })
          .withDataProp('data')
          .withOption('processing', true)
          .withOption('serverSide', true)
          .withPaginationType('full_numbers');

        $scope.dtColumns = [
          DTColumnBuilder.newColumn('metadataUrn'),
          DTColumnBuilder.newColumn('metadataTitle'),
          DTColumnBuilder.newColumn('metadataCategory'),
          DTColumnBuilder.newColumn('originator'),
          DTColumnBuilder.newColumn('process'),
          DTColumnBuilder.newColumn('gtsCategory'),
          DTColumnBuilder.newColumn('fncPattern'),
          DTColumnBuilder.newColumn('dataPolicy'),
          DTColumnBuilder.newColumn('localDataResource'),
          DTColumnBuilder.newColumn('actions').renderWith(function(data, type, full) {
            return "<button class=\"btn btn-default\" onclick=\"angular.element(this).scope().edit('"+full.metadataUrn+"')\">Edit</button>";
          })
        ];

        $scope.edit = function(metadataUrn) {
          $http({
            url : $scope.url + 'openwis.productmetadata.get',
            method : 'GET',
            params : {urn: metadataUrn}
          }).success(function(data) {
            $scope.product = data;

            $("#editProductMetadata").modal();
          }).error(function(data) {
            console.log(data);
            $scope.updateData();
            $rootScope.$broadcast('StatusUpdated', {
              title : 'Error',
              msg : 'Error getting product metadata details. Please reload.',
              type : 'danger'
            });
          });
        };

      }
  ]);

  module.controller('GnOpenwisProductMetadataModalController', function($scope,
                                                                  $http, $rootScope) {
    $scope.ok = function() {
      $http({
        url : $scope.url + 'openwis.productmetadata.set',
        method : 'POST',
        params : $scope.product
      }).success(function(data) {
        $scope.updateData();
        $("#editProductMetadata").modal('hide');
      }).error(function(data) {
        console.log(data);
        $scope.updateData();
        $rootScope.$broadcast('StatusUpdated', {
          title : 'Error',
          msg : 'Error saving product metadadata details. Please try again.',
          type : 'danger'
        });
      });
    };

    $scope.cancel = function() {
      $("#editProductMetadata").modal('hide');
    };
  });

})();
