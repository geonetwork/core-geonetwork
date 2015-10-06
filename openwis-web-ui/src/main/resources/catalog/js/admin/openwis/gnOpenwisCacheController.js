(function() {
  goog.provide('gn_openwis_cache_controller');

  var module = angular.module('gn_openwis_cache_controller', [
    'datatables'
  ]);

  module
      .controller(
          'GnOpenwisCacheController',
          [
              '$scope',
              '$routeParams',
              '$http',
              '$rootScope',
              'DTOptionsBuilder',
              'DTColumnBuilder',
              function($scope, $routeParams, $http, $rootScope,
                  DTOptionsBuilder, DTColumnBuilder) {

                $scope.dtOptions = DTOptionsBuilder.newOptions().withOption(
                    'ajax', {
                      url : $scope.url + 'openwis.cache.search',
                      data : function(data) {
                        $scope.planify(data);
                      },
                      type : 'GET'
                    }).withDataProp('data').withOption('processing', true)
                    .withOption('serverSide', true).withPaginationType(
                        'full_numbers');

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('filename').withOption('name',
                        'filename'),
                        DTColumnBuilder.newColumn('urn'),
                    DTColumnBuilder.newColumn('checksum').withOption('name',
                        'checksum'),
                    DTColumnBuilder.newColumn('origin').withOption('name',
                        'received_from_gts'),
                    DTColumnBuilder.newColumn('insertion_date').withOption(
                        'name', 'insertion_date'),
                    DTColumnBuilder
                        .newColumn('actions')
                        .renderWith(
                            function(data, type, full) {
                              return "<button class=\"btn btn-default\" data-ng-click=\"alert('wa')\">View</button>";
                            })
                ];

                $scope.planify = function(data) {
                  for (var i = 0; i < data.columns.length; i++) {
                    column = data.columns[i];
                    column.searchRegex = column.search.regex;
                    column.searchValue = column.search.value;
                    delete (column.search);
                  }
                }

              }
          ]);
})();
