(function() {
  goog.provide('gn_openwis_admin_subscription_controller');

  var module = angular.module('gn_openwis_admin_subscription_controller', [
      'datatables', 'datatables.fixedcolumns', 'datatables.columnfilter'
  ]);

  module
      .controller(
          'gnOpenwisAdminSubscriptionController',
          [
              '$scope',
              '$routeParams',
              '$http',
              '$rootScope',
              'DTOptionsBuilder',
              'DTColumnBuilder',
              '$http',
              function($scope, $routeParams, $http, $rootScope,
                  DTOptionsBuilder, DTColumnBuilder, $http) {

                $scope.myself = $("*[name='myself']").val();

                $scope.dtInstance = {},

                $scope.groups = [];
                $scope.lang = location.href.split('/')[5].substring(0, 3)
                    || 'eng';

                $http({
                  url : $scope.url + 'xml.info?type=groups&_content_type=json',
                  method : 'GET'
                }).success(function(data) {
                  $scope.groups = data.group;
                });

                $scope.dtOptions = DTOptionsBuilder.newOptions().withOption(
                    "sAjaxSource",
                    $scope.url + 'openwis.subscription.search?myself='
                        + $scope.myself).withDataProp('data').withOption(
                    'processing', true).withOption('serverSide', true)
                    .withOption('iDisplayLength', 25).withOption('scrollX',
                        '100%').withOption('scrollCollapse', true).withOption(
                        'autoWidth', false).withOption('bFilter', false)
                    .withPaginationType('full_numbers').withFixedColumns({
                      leftColumns : 0,
                      rightColumns : 1
                    }).withColumnFilter({
                      aoColumns : [
                          null, null, null, null, null, null, null, null
                      ]
                    });

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('id').withOption('name', 'id')
                        .notVisible(),
                    DTColumnBuilder.newColumn('title').withOption('name',
                        'title'),
                    DTColumnBuilder.newColumn('user')
                        .withOption('name', 'user'),
                    DTColumnBuilder.newColumn('urn').withOption('name', 'urn'),
                    DTColumnBuilder.newColumn('status').withOption('name',
                        'status'),
                    DTColumnBuilder.newColumn('backup').withOption('name',
                        'backup'),
                    DTColumnBuilder.newColumn('starting_date').withOption(
                        'name', 'starting_date'),
                    DTColumnBuilder
                        .newColumn('actions')
                        .renderWith(
                            function(data, type, full) {
                              return "<div style=\"width:120px\">"
                                  + "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.search#/metadata/"
                                  + full.metadataUrn
                                  + "\" title=\"View\"><i class=\"fa fa-eye\"></i></a>"
                                  + "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().edit('"
                                  + full.metadataUrn
                                  + "')\" title=\"Edit product info\"><i class=\"fa fa-edit\"></i></a>"
                                  + "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.edit#/metadata/"
                                  + full.metadataId
                                  + "\" title=\"Edit metadata\"><i class=\"fa fa-pencil\"></i></a>"
                                  + "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.edit#/create?from="
                                  + full.metadataId
                                  + "\"  title=\"Duplicate\"><i class=\"fa fa-copy\"></i></a>"
                                  + "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().delete('"
                                  + full.metadataId
                                  + "')\" title=\"Remove\"><i class=\"fa fa-times text-danger\"></i></a>"
                                  + "</div>";
                            })
                ];

                $scope.updateData = function() {
                  // Refresh the table
                  $scope.dtOptions.sAjaxSource = $scope.url
                      + 'openwis.subscription.search?myself=' + $scope.myself
                      + '&group=' + $scope.group;
                  if ($scope.dtInstance.reloadData) {
                    $scope.dtInstance.reloadData();
                  }
                };

                $scope.$watch('group', $scope.updateData);
              }

          ]);
})();
