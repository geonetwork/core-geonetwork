(function() {
  goog.provide('gn_openwis_productmetadata_controller');

  var module = angular.module('gn_openwis_productmetadata_controller', ['datatables', 'datatables.fixedcolumns']);

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
          .withOption('iDisplayLength', 25)
          .withOption('scrollX', '100%')
          .withOption('scrollCollapse', true)
          .withOption('autoWidth', false)
          .withFixedColumns({
            leftColumns: 0,
            rightColumns: 1
          })
          .withPaginationType('full_numbers');

        $scope.dtColumns = [
          DTColumnBuilder.newColumn('metadataUrn').withOption('name', '_uuid'),
          DTColumnBuilder.newColumn('metadataTitle').withOption('name', '_title'),
          DTColumnBuilder.newColumn('metadataCategory').withOption('name', '_cat'),
          DTColumnBuilder.newColumn('originator').withOption('name', '_originator'),
          DTColumnBuilder.newColumn('process').withOption('name', '_process'),
          DTColumnBuilder.newColumn('gtsCategory').withOption('name', '_gtsCategory'),
          DTColumnBuilder.newColumn('fncPattern').withOption('name', '_fncPattern'),
          DTColumnBuilder.newColumn('dataPolicy').withOption('name', '_dataPolicy'),
          DTColumnBuilder.newColumn('priority').withOption('name', '_priority'),
          DTColumnBuilder.newColumn('localDataResource').withOption('name', '_localDataResource'),
          DTColumnBuilder.newColumn('actions').renderWith(function(data, type, full) {
            return "<div class=\"dropdown\" style=\"width:130px\">\n" +
              "  <button class=\"btn btn-primary dropdown-toggle\" type=\"button\" data-toggle=\"dropdown\">Actions" +
              "  <span class=\"caret\"></span></button>" +
              "  <ul class=\"dropdown-menu\" style=\"min-width:100px\">" +
              "    <li><a class=\"fa fa-eye\" href=\"catalog.search#/metadata/" + full.metadataUrn + "\">View</a></li>" +
              "    <li><a class=\"fa fa-edit\" onclick=\"angular.element(this).scope().edit('" + full.metadataUrn + "')\">Edit</a></li>" +
              "    <li><a class=\"fa fa-edit\" href=\"catalog.edit#/metadata/" + full.metadataUrn + "\">Edit metadata</a></li>" +
              "    <li><a class=\"fa fa-copy\" href=\"catalog.edit#/create?from=" + full.metadataUrn + "\">Duplicate</a></li>" +
              "    <li><a class=\"fa fa-eye\" href=\"#\">Remove</a></li>" +
              "  </ul>" +
              "</div>";
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
