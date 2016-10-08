/**
 * floatThead wrapper for AngularJS
 * @version v0.1.0 - 2015-10-25
 * @link https://github.com/brandon-barker/angular-floatThead
 * @author Brandon Barker
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
(function () {
  'use strict';

  angular
    .module('floatThead', [])
    .directive('floatThead', ['$timeout', '$log', floatThead]);

  function floatThead($timeout, $log) {
    // Usage:
    // Specify float-thead on any table element and optionally pass through a floatThead options object to initialize the library.
    // Optionally specify ng-model to have the directive watch any objects for changes and call 'reflow' on floatThead.
    // You can also manually trigger a reflow by triggering an event on the table element called 'update', eg: jQuery('.table').trigger('update');
    var directive = {
      require: '?ngModel',
      scope: {
        floatThead: '=?',
        floatTheadEnabled: '=?'
      },
      controller: ['$scope', '$element', '$attrs', function ($scope, $element, $attrs) {
        // default float-thead-enabled to true if not present
        if (!$attrs.hasOwnProperty('floatTheadEnabled')) {
          $scope.floatTheadEnabled = $attrs.floatTheadEnabled = true;
        }
        
        // default to empty object
        if (!$attrs.hasOwnProperty('floatThead') || $attrs.floatThead === '') {
          $scope.floatThead = $attrs.floatThead = {};
        }
      }],
      link: link,
      restrict: 'A'
    };
    return directive;

    function link($scope, $element, $attrs, ngModel) {
      $scope.$watch('floatTheadEnabled', function (newVal) {
        if (newVal === true) {
          if (angular.isObject($scope.floatThead)) {
            jQuery($element).floatThead($scope.floatThead);
          }
        } else {
          jQuery($element).floatThead('destroy');
        }
      });
      
      $scope.$watch('floatThead', function (newVal, oldVal) {
        if (newVal === oldVal || !$scope.floatTheadEnabled) {
          return;
        }
        
        // first destroy it so we can recreate with new options
        jQuery($element).floatThead('destroy');
        if (angular.isObject(newVal)) {
          jQuery($element).floatThead(newVal);
        }
      }, true);

      if (ngModel) {
        // hook the model $formatters to get notified when anything changes so we can reflow
        ngModel.$formatters.push(function () {
          // only reflow if enabled
          if ($scope.floatTheadEnabled && angular.isObject($scope.floatThead)) {
            // give time for rerender before reflow
            $timeout(function() {
              jQuery($element).floatThead('reflow');
            });
          }
        });
      } else {
        $log.info('floatThead: ngModel not provided!');
      }

      $element.bind('update', function () {
        $timeout(function () {
          jQuery($element).floatThead('reflow');
        }, 0);
      });

      $element.bind('$destroy', function () {
        jQuery($element).floatThead('destroy');
      });
    }
  }
})();
