/**
 * @ngdoc overview
 * @name ng-skos
 * @description
 *
 * The main module <b>ngSKOS</b> contains several directives and services to
 * handle SKOS data. See the [API reference](#api) for module documentation.
 */
(function() {
  goog.provide('ngSkos');

  goog.require('ngSkos_browser_directive');
  goog.require('ngSkos_concept_directive');
  goog.require('ngSkos_label_directive');

  angular.module('ngSkos', [
    'ngSkos_browser_directive',
    'ngSkos_concept_directive',
    'ngSkos_label_directive'
  ]);
})();
