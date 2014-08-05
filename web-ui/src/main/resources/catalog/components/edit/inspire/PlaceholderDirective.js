(function() {
  'use strict';
  goog.provide('placeholder_directive');

  var module = angular.module('placeholder_directive', []);

  module.directive('placeholderInput', function() {
    return {
      scope: {
        rows: '@',
        index: '@',
        disabled: '@',
        placeholder: '@',
        validationClass: '@',
        parent: '=',
        field: '@'
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      templateUrl: '../../catalog/components/edit/inspire/partials/placeholder.html'
    };
  });

}());
