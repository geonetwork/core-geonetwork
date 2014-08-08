(function() {
  'use strict';
  goog.provide('inspire_ie9_select');

  var module = angular.module('inspire_ie9_select', []);

  module.directive('ie9Select', ['$translate', '$timeout', function($translate, $timeout) {
    return {
      scope: {
        field: "=",
        options: '=',
        defaultValue: '@',
        change: '&',
        value: '@',
        groupBy: '@',
        title: '@',
        titleFunc: '&',
        translateTitle: '@'
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      link: function($scope) {
        $scope.showEmptyOption = true;
        $scope.groups = [];
        $scope.showEmptyOption = true;
        var updateShowEmptyOption = function () {
          $scope.showEmptyOption = (!$scope.field || angular.equals($scope.field, {})) && $scope.defaultValue === undefined;
        };
        var isSelected = function (option) {
          if ((!$scope.value && option === $scope.field) || ($scope.value && option[$scope.value] === $scope.field)) {
            return true;
          }

          if ($scope.defaultValue) {
            var i = parseInt($scope.defaultValue);
            if (i < 0) {
              i = $scope.options.length + i;
              if (i < 0) {
                throw new Error($scope.defaultValue + " is out of range: " + $scope.options.length + " -- " + $scope.title);
              }
              return option === $scope.options[i];
            }
          }
        };
        var getTitle = function (option) {
          var title = option;
          if ($scope.title) {
            title = option[$scope.title];
          } else if ($scope.titleFunc()) {
            title = $scope.titleFunc()(option);
          }
          if ($scope.translateTitle === 'true') {
            title = $translate(title);
          }

          return title;
        };
        $scope.$watch('options', function(){
          var k, groupTitle, opt, group, finalOpt,
            i = 0,
            groups = [],
            groupsMap = {},
            options = $scope.options;

          updateShowEmptyOption();

          for (k in options) {
            if (options.hasOwnProperty(k)) {
              opt = options[k];
              groupTitle = $scope.groupBy ? opt[$scope.groupBy] : '';
              group = groupsMap[groupTitle];
              finalOpt = {
                value: i,
                actual: opt,
                isSelected: isSelected(opt),
                title: getTitle(opt)
              };
              if (group) {
                group.opts.push(finalOpt);
              } else {
                group = {
                  title: groupTitle,
                  opts: [finalOpt]
                };
                groups.push(group);
                groupsMap[groupTitle] = group;
              }
              i++;
            }
          }

          $scope.groups = groups;
        });

        var lastField = $scope.field;
        $scope.$watch('field', function(newVal, oldVal){
          if (newVal !== oldVal && newVal !== lastField) {
            angular.forEach($scope.groups, function(grp){
              angular.forEach(grp.opts, function (opt) {
                opt.isSelected = isSelected(opt.actual);
              });
            });
          }
          updateShowEmptyOption();
        });

        $scope.updateField = function (newVal, $event) {
          $event.stopPropagation();
          $timeout(function() {
            var newFieldVal = newVal;
            if (newFieldVal) {
              if ($scope.value) {
                newFieldVal = newVal[$scope.value];
              }
            } else {
              newFieldVal = '';
            }

            if (newFieldVal !== $scope.field) {
              if (typeof newFieldVal === 'object') {
                angular.copy(newFieldVal, $scope.field);
              } else {
                $scope.field = newFieldVal;
              }
              lastField = $scope.field;
              if ($scope.change) {
                $scope.change();
              }
            }

          });
         };

      },
      templateUrl: '../../catalog/components/edit/inspire/partials/select.html'
    };
  }]);

}());
