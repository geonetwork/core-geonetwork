(function() {
  'use strict';
  goog.provide('placeholder_directive');

  var module = angular.module('placeholder_directive', []);

  module.directive('placeholderInput', function() {
    return {
      scope: {
        rows: '@',
        placeholderPlacementClass: '@',
        disabled: '@',
        placeholder: '@',
        validationClass: '@',
        parent: '=',
        field: '@'
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      link: function ($scope) {
        $scope.selectInput = function ($event) {
          $event.stopPropagation();
          var inputEl = $($event.target.parentElement).find("input");
          if (inputEl.length > 0) {
            inputEl.focus();
          }
        };
      },
      templateUrl: '../../catalog/components/edit/inspire/partials/placeholder.html'
    };
  });

}());
