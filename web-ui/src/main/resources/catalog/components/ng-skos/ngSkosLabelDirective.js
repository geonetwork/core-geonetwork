/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

/**
 * @ngdoc directive
 * @name ng-skos.directive:skosLabel
 * @restrict A
 * @description
 *
 * Displays the preferred label of a concept.
 * Changes on the preferred label(s) are reflected in the display.
 *
 * ## Source code
 *
 * The most recent [source
 * code](https://github.com/gbv/ng-skos/blob/master/src/directives/skosLabel.js)
 * of this directive is available at GitHub.
 *
 * @param {string} skos-label Assignable angular expression with
 *      [concept](http://gbv.github.io/jskos/jskos.html#concepts)
 *      data to bind to.
 * @param {string} lang optional language. If not specified, an arbitrary
 *      preferred label is selected. Future versions of this directive may
 *      use more elaborated heuristics to select an alternative language.
 *
 * @example
 <example module="myApp">
  <file name="index.html">
    <div ng-controller="myController">
      <dl>
        <dt>en</dt>
        <dd><span skos-label="sampleConcept" lang="en"/></dd>
        <dt>de</dt>
        <dd><span skos-label="sampleConcept" lang="de"/></dd>
        <dt><input type="text" ng-model="lang2"/></dt>
        <dd><span skos-label="sampleConcept" lang="{{lang2}}"/></dd>
      </dl>
      <pre>{{sampleConcept}}</pre>
    </div>
  </file>
  <file name="script.js">
    angular.module('myApp',['ngSKOS']);

    function myController($scope) {
        $scope.sampleConcept = {
            prefLabel: {
                en: "example",
                de: "Beispiel",
            },
        };
        $scope.lang2 = "fr";
    }
  </file>
</example>
 */

(function() {
  goog.provide('ngSkos_label_directive');

  var module = angular.module('ngSkos_label_directive', []);

  module.directive('skosLabel', function() {
    return {
      restrict: 'A',
      scope: {
        concept: '=skosLabel'
      },
      template: '{{concept.prefLabel[language] ? ' +
          '(concept.prefLabel[language] == "topConcepts" ? ' +
          '(concept.prefLabel[language] | translate) : ' +
              'concept.prefLabel[language])' +
              ' : "???"}}',
      link: function(scope, element, attrs) {

        function updateLanguage(language) {
          scope.language = language ? language : attrs.lang;

          language = scope.concept ?
              selectLanguage(scope.concept.prefLabel, scope.language) : '';

          if (language != scope.language) {
            scope.language = language;
          }
        }

        function selectLanguage(labels, language) {
          if (angular.isObject(labels)) {
            if (language && labels[language]) {
              return language;
            } else {
              for (language in labels) {
                return language;
              }
            }
          }
        }

        // update if lang attribute changed (also called once at initialization)
        attrs.$observe('lang', updateLanguage);

        // update with same language if prefLabels changed
        scope.$watch('concept.prefLabel', function(value) {
          updateLanguage();
        }, true);
      }
    };
  });

})();
