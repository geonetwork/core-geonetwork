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
                    .withOption('scrollCollapse', true).withOption('autoWidth',
                        true).withOption('bFilter', false).withPaginationType(
                        'full_numbers');

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

                              var susres = "<a class=\"btn btn-link\" target=\"_blank\" onclick=\"angular.element(this).scope().resume("
                                  + full.id
                                  + ")\"  title=\"Resume\"><i class=\"fa fa-play\"></i></a>";

                              if (full.status == 'ACTIVE') {
                                susres = "<a class=\"btn btn-link\" target=\"_blank\" onclick=\"angular.element(this).scope().suspend("
                                    + full.id
                                    + ")\"  title=\"Suspend\"><i class=\"fa fa-pause\"></i></a>";
                              }

                              return "<div style=\"width:120px\">"
                                  + "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.search#/metadata/"
                                  + full.urn
                                  + "\" title=\"View Product\"><i class=\"fa fa-eye\"></i></a>"
                                  + "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().edit('"
                                  + full.id
                                  + "')\" title=\"Edit subscription\"><i class=\"fa fa-edit\"></i></a>"
                                  + susres
                                  + "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().discard('"
                                  + full.id
                                  + "')\" title=\"Discard subscription\"><i class=\"fa fa-times text-danger\">"
                                  + "</i></a></div>";
                            })
                ];

                $scope.updateData = function() {
                  // Refresh the table
                  $scope.dtOptions.sAjaxSource = $scope.url
                      + 'openwis.subscription.search?myself=' + $scope.myself
                      + '&group=' + $scope.group;
                  if ($scope.dtInstance.dataTable) {
                    $scope.dtInstance.dataTable._fnDraw()
                  }
                };

                $scope.$watch('group', $scope.updateData);

                $scope.suspend = function(id) {
                  $http(
                      {
                        url : $scope.url
                            + 'openwis.subscription.suspend?subscriptionId='
                            + id,
                        method : 'GET'
                      }).success(function(data) {
                    console.log(data);
                    $scope.updateData();
                  });
                };

                $scope.resume = function(id) {
                  $http(
                      {
                        url : $scope.url
                            + 'openwis.subscription.resume?subscriptionId='
                            + id,
                        method : 'GET'
                      }).success(function(data) {
                    console.log(data);
                    $scope.updateData();
                  });
                };

                $scope.edit = function(id) {
                  $http(
                      {
                        url : $scope.url
                            + 'openwis.subscription.get?subscriptionId=' + id,
                        method : 'GET'
                      }).success(function(data) {
                    console.log(data);
                    $scope.updateData();
                  });
                };

                $scope.discard = function(id) {
                  if (window.confirm('Are you sure you want to delete?')) {
                    $http(
                        {
                          url : $scope.url
                              + 'openwis.subscription.discard?subscriptionId='
                              + id,
                          method : 'GET'
                        }).success(function(data) {
                      $scope.updateData();
                    });
                  }
                }
              }

          ]);
})();
