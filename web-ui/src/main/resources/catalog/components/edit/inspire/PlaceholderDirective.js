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
        field: '='
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      link: function($scope) {
            console.log($scope.field);
      },
      templateUrl: '../../catalog/components/edit/inspire/partials/placeholder.html'
    };
  });

}());
