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

(function() {
  goog.provide('gn_validation_report_directive');

  goog.require('gn_utility');


  /**
   */
  angular.module('gn_validation_report_directive', [
    'gn_utility'
  ])
      .directive('gnValidationReport', [
        'gnValidation', 'gnCurrentEdit', '$location',
        function(gnValidation, gnCurrentEdit, $location) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/validationreport/' +
                'partials/validationreport.html',
            scope: {},
            link: function(scope) {
              scope.showErrors = false;
              scope.showSuccess = false;
              scope.alwaysOnTop = false;
              scope.gnCurrentEdit = gnCurrentEdit;
              scope.loading = false;
              scope.ruleTypes = [];

              scope.load = function() {
                scope.numberOfRules = 0;
                scope.ruleTypes = [];
                scope.hasRequiredErrors = false;
                scope.hasErrors = false;
                scope.hasSuccess = false;
                scope.loading = true;

                gnValidation.get().then(function(response) {
                  var optional = [];
                  var ruleTypes = response.data.report;
                  angular.forEach(ruleTypes, function(ruleType) {
                    if (ruleType.requirement !== 'REQUIRED') {
                      optional.push(ruleType);
                    } else {
                      scope.ruleTypes.push(ruleType);
                    }

                    ruleType.expanded = false;

                    scope.hasRequiredErrors = scope.hasRequiredErrors ||
                        (ruleType.requirement === 'REQUIRED' &&
                        ruleType.error > 0);
                    scope.hasErrors = scope.hasErrors || ruleType.error > 0;
                    angular.forEach(ruleType.patterns, function(pat) {
                      scope.numberOfRules +=
                          pat.rules ? pat.rules.length : 0;
                    });
                  });

                  scope.ruleTypes = scope.ruleTypes.concat(optional);
                  scope.hasSuccess = scope.ruleTypes.length > 0;
                  scope.loading = false;
                }).finally(function() {
                  scope.loading = false;
                });
              };

              scope.scrollTo = function(ref) {
                $location.search('scrollTo', '#gn-el-' + ref.split('_')[1]);
              };

              scope.labelImportanceClass = function(type) {
                if (type.error === 0) {
                  return 'label-success';
                } else if (type.requirement === 'REQUIRED') {
                  return 'label-danger';
                }
                return 'label-info';
              };

              scope.toggleShowErrors = function() {
                scope.showErrors = !scope.showErrors;
              };
              scope.toggleShowSuccess = function() {
                scope.showSuccess = !scope.showSuccess;
              };
              scope.getClass = function(type) {
                if (scope.numberOfRules > 0) {
                  if (type === 'icon') {
                    return scope.hasRequiredErrors ?
                        'fa-thumbs-o-down' : 'fa-thumbs-o-up';
                  }
                  return scope.hasRequiredErrors ?
                      'panel-danger' : 'panel-success';
                }
                if (type === 'icon') {
                  return 'fa-check';
                } else {
                  return '';
                }
              };

              // When saving is done, refresh validation report
              scope.$watch('gnCurrentEdit.saving', function(newValue) {
                if (newValue === false && gnCurrentEdit.showValidationErrors) {
                  scope.load();
                }
              });
            }
          };
        }]);
}());
