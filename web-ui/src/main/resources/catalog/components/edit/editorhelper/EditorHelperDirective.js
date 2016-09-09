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
  goog.provide('gn_editor_helper');

  var module = angular.module('gn_editor_helper', []);

  /**
   * @ngdoc directive
   * @name gn_editor_helper.directive:gnEditorHelper
   * @restrict A
   *
   * @description
   * Create a widget to handle a list of suggestion to help editor
   * to populate a field. Suggestions are list of values defined
   * in labels.xml for each schema.
   *
   */
  module.directive('gnEditorHelper', [
    function() {

      return {
        restrict: 'A',
        replace: false,
        transclude: false,
        scope: {
          mode: '@gnEditorHelper',
          ref: '@',
          type: '@',
          relatedElement: '@',
          relatedAttr: '@',
          tooltip: '@',
          multilingualField: '@'
        },
        templateUrl: '../../catalog/components/edit/editorhelper/partials/' +
            'editorhelper.html',
        link: function(scope, element, attrs) {
          // Retrieve the target field by name (general case)
          // or by id (template mode field).
          var field = document.gnEditor[scope.ref] || $('#' + scope.ref).get(O),
              relatedAttributeField = document.gnEditor[scope.relatedAttr],
              relatedElementField = document.gnEditor[scope.relatedElement],
              initialValue = field.value;

          // Function to properly set the target field value
          var populateField = function(field, value) {
            if (field && value !== undefined) {
              field.value = field.type === 'number' ? parseFloat(value) : value;
              $(field).change();
              // gn-check add class on keyup event.
              $(field).keyup();
            }
          };


          // Load the config from the textarea containing the helpers
          scope.config =
              angular.fromJson($('#' + scope.ref + '_config')[0].value);

          // If only one option, convert to an array
          if (!$.isArray(scope.config.option)) {
            scope.config.option = [scope.config.option];
          }
          if (angular.isArray(scope.config)) {
            scope.config.option = scope.config;
          }

          // Check if current value is one of the suggestion
          var isInList = false;
          angular.forEach(scope.config.option, function(opt) {
            if (opt !== undefined && opt['@value'] === initialValue) {
              isInList = true;
            }
          });
          if (!isInList) {
            scope.otherValue = {'@value' : initialValue};
          } else {
            scope.otherValue = {'@value' : ''};
          }

          // Set the initial value
          scope.config.selected = {};
          scope.config.value =
              field.type === 'number' ? parseFloat(field.value) : field.value;
          scope.config.layout =
              scope.mode && scope.mode.indexOf('radio') !== -1 ?
              'radio' : scope.mode;

          scope.selectOther = function() {
            $('#otherValue_' + scope.ref).focus();
          };
          scope.selectOtherRadio = function() {
            $('#otherValueRadio_' + scope.ref).prop('checked', true);
          };
          scope.updateWithOtherValue = function() {
            field.value = scope.otherValue['@value'];
            $(field).change();
          };
          scope.select = function(value) {
            field.value = value['@value'];
            $(field).change();
          };

          // On change event update the related element(s)
          // which is sent by the form
          scope.$watch('config.selected', function() {
            var option = scope.config.selected;
            if (option && option['@value']) {
              // Set the current value to the selected option if not empty
              scope.config.value =
                  field.type === 'number' ?
                  parseFloat(option['@value']) : option['@value'];
              populateField(relatedAttributeField, option['@title'] || '');
              populateField(relatedElementField, option['@title'] || '');
            }
          });

          // When field value change, update the main element
          // value
          scope.$watch('config.value', function() {
            populateField(field, scope.config.value);
          });
        }
      };
    }]);
})();
