(function() {
  goog.provide('gn_openwis_monitoring_controller');

  var module = angular.module('gn_openwis_monitoring_controller', []);

  module
      .controller(
          'GnOpenwisMonitoringController',
          [
              '$scope',
              '$routeParams',
              '$http',
              'DTOptionsBuilder',
              'DTColumnBuilder',
              function($scope, $routeParams, $http, DTOptionsBuilder,
                  DTColumnBuilder) {

                $scope.monitorType = 0;

                $scope.options = [
                    {
                      id : 0,
                      name : 'ingestedDataStatistics',
                      cols: [0, 1]
                    }, {
                      id : 1,
                      name : 'disseminatedDataStatistics',
                      cols: [0, 1, 2, 3, 4, 5]
                    }, {
                      id : 2,
                      name : 'exchangedDataStatistics',
                      cols: [0, 1, 7, 8]
                    }, {
                      id : 3,
                      name : 'replicatedDataStatistics',
                      cols: [0, 1, 8]
                    }
                ];

                $scope.planify = function(data) {
                  for (var i = 0; i < data.columns.length; i++) {
                    column = data.columns[i];
                    column.searchRegex = column.search.regex;
                    column.searchValue = column.search.value;
                    delete (column.search);
                  }
                };

                $scope.dtInstance = {},

                $scope.dtOptions = DTOptionsBuilder.newOptions().withOption(
                    "sAjaxSource",
                    $scope.url + 'openwis.monitoring.get?monitorType='
                        + $scope.options[$scope.monitorType].name)
                    .withDataProp('data').withOption('processing', true)
                    .withOption('serverSide', true).withOption(
                        'iDisplayLength', 25).withOption('scrollX', '100%')
                    .withOption('scrollCollapse', true).withOption('autoWidth',
                        false).withPaginationType('full_numbers');

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('date')
                        .withOption('name', 'date'),
                    DTColumnBuilder.newColumn('size')
                        .withOption('name', 'size'),
                    DTColumnBuilder.newColumn('dissToolNbFiles').withOption(
                        'name', 'dissToolNbFiles'),
                    DTColumnBuilder.newColumn('userId').withOption('name',
                        'userId'),
                    DTColumnBuilder.newColumn('dissToolSize').withOption(
                        'name', 'dissToolSize'),
                    DTColumnBuilder.newColumn('nbFiles').withOption('name',
                        'nbFiles'),
                    DTColumnBuilder.newColumn('totalSize'),
                    DTColumnBuilder.newColumn('nbMetadata'),
                    DTColumnBuilder.newColumn('source').withOption('name',
                        'source')
                ];


                $scope.updateData = function() {
                  $scope.dtOptions.sAjaxSource = $scope.url
                      + 'openwis.monitoring.get?monitorType='
                      + $scope.options[$scope.monitorType].name;

                 for(i = 0; i < $scope.dtColumns.length; i++) {
                   $scope.dtColumns[i].visible = false;
                 }
                 
                 for(i = 0; i < $scope.options[$scope.monitorType].cols.length; i++) {
                   $scope.dtColumns[$scope.options[$scope.monitorType].cols[i]].visible = true;
                 }
                 
                 if($scope.dtInstance && $scope.dtInstance.dataTable) {
                   $scope.dtInstance.dataTable.fnAdjustColumnSizing();
                 }
                }
                
                $(document).on( 'init.dt.dtr', function (e, settings, json) {
                  $scope.updateData();                  
                });
              }
          ]);

})();
