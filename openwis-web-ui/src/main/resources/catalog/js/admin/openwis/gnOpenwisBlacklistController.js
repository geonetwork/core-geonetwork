(function() {
  goog.provide('gn_openwis_blacklist_controller');

  var module = angular.module('gn_openwis_blacklist_controller', []);

  module
      .controller(
          'GnOpenwisBlacklistController',
          [
              '$scope',
              '$routeParams',
              '$http',
              '$rootScope',
              'DTOptionsBuilder',
              'DTColumnBuilder',
              function($scope, $routeParams, $http, $rootScope,
                  DTOptionsBuilder, DTColumnBuilder) {

                $scope.planify = function(data) {
                  for (var i = 0; i < data.columns.length; i++) {
                    column = data.columns[i];
                    column.searchRegex = column.search.regex;
                    column.searchValue = column.search.value;
                    delete (column.search);
                  }
                };

                $scope.dtInstance = {}, $scope.dtOptions = DTOptionsBuilder
                    .newOptions().withOption('ajax', {
                      url : $scope.url + 'openwis.blacklisting.search',
                      data : function(data) {
                        $scope.planify(data);
                      },
                      type : 'GET'
                    }).withDataProp('data').withOption('processing', true)
                    .withOption('serverSide', true).withPaginationType(
                        'full_numbers');

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('user')
                        .withOption('name', 'user'),
                    DTColumnBuilder.newColumn('nbDisseminationWarnThreshold')
                        .withOption('name', 'nbDisseminationWarnThreshold'),
                    DTColumnBuilder.newColumn('volDisseminationWarnThreshold')
                        .withOption('name', 'volDisseminationWarnThreshold'),
                    DTColumnBuilder.newColumn(
                        'nbDisseminationBlacklistThreshold').withOption('name',
                        'nbDisseminationBlacklistThreshold'),
                    DTColumnBuilder.newColumn(
                        'volDisseminationBlacklistThreshold').withOption(
                        'name', 'volDisseminationBlacklistThreshold'),
                    DTColumnBuilder.newColumn('status').withOption('name',
                        'status'),
                    DTColumnBuilder
                        .newColumn('actions')
                        .renderWith(
                            function(data, type, full) {
                              return "<a class=\"btn btn-link\" target=\"_blank\" onclick='"
                                  + "angular.element(this).scope().edit("
                                  + JSON.stringify(full)
                                  + ")"
                                  + "'><i class=\"fa fa-eye\"></i></a>";
                            })
                ];

                $scope.changeBlacklist = function() {
                  $scope.element.status = "NOT_BLACKLISTED_BY_ADMIN";
                  if ($scope.element.isBlacklisted) {
                    $scope.element.status = "BLACKLISTED_BY_ADMIN";
                  }
                }

                $scope.edit = function(element) {
                  $scope.element = element;
                  $http({
                    url : $scope.url + 'openwis.blacklisting.isBlacklisted',
                    method : 'POST',
                    params : $scope.element
                  }).success(function(data) {
                    $scope.element.isBlacklisted = data;
                    $("#editBlackList").modal();
                  }).error(function(data) {
                    $rootScope.$broadcast('StatusUpdated', {
                      title : 'Error',
                      msg : 'Error getting user details. Please reload.',
                      type : 'danger'
                    });
                  });
                };
              }
          ]);

  module.controller('GnOpenwisBlacklistModalController', function($scope,
      $http, $rootScope) {
    $scope.ok = function() {
      $http({
        url : $scope.url + 'openwis.blacklisting.set',
        method : 'POST',
        params : $scope.element
      }).success(function(data) {
        if ($scope.dtInstance.dataTable) {
          $scope.dtInstance.dataTable._fnDraw()
        }
        $("#editBlackList").modal('hide');
      }).error(function(data) {
        if ($scope.dtInstance.dataTable) {
          $scope.dtInstance.dataTable._fnDraw()
        }
        $rootScope.$broadcast('StatusUpdated', {
          title : 'Error',
          msg : 'Error saving user details. Please try again.',
          type : 'danger'
        });
      });
    };

    $scope.cancel = function() {
      if ($scope.dtInstance.dataTable) {
        $scope.dtInstance.dataTable._fnDraw()
      }
      $("#editBlackList").modal('hide');
    };
  });

})();
