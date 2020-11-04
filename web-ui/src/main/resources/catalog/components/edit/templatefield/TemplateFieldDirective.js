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
  goog.provide('gn_template_field_directive');

  var module = angular.module('gn_template_field_directive',
      ['pascalprecht.translate']);
  module.directive('gnTemplateFieldAddButton',
      ['gnEditor', 'gnCurrentEdit', '$rootScope', '$translate',
       function(gnEditor, gnCurrentEdit, $rootScope, $translate) {

         return {
           restrict: 'A',
           replace: true,
           scope: {
             id: '@gnTemplateFieldAddButton'
           },
           link: function(scope, element, attrs) {
             var textarea = $(element).parent()
                 .find('textarea[name=' + scope.id + ']'),
             hasChoice =
                 angular.isDefined($(element).attr('data-has-choice'));

             // Unregister this textarea to the form
             // It will be only submitted if user click the add button
             textarea.removeAttr('name');

             scope.addFromTemplate = function() {
               textarea.attr('name', scope.id);

               // Save and refreshform
               gnEditor.save(gnCurrentEdit.id, true).then(function() {
                  // success. Do nothing
               }, function(rejectedValue) {
                  $rootScope.$broadcast('StatusUpdated', {
                   title: $translate.instant('runServiceError'),
                   error: rejectedValue,
                   timeout: 0,
                   type: 'danger'
                  });
               });
             };

             var chooseTemplate = function(id) {
               textarea.val($(element).parent()
               .find('textarea#' + id + '-value').val());

               scope.addFromTemplate();
             };

             if (!hasChoice) {
               // Register click event on main button
               // which will add snippet from the single
               // textarea
               $(element).click(scope.addFromTemplate);
             } else {
               // Register click on each choices
               var choices = $(element)
               .find('ul > li > a');
               choices.each(function(idx, e) {
                 var id = $(e).attr('id');
                 $(e).click(function() {
                   chooseTemplate(id);
                 });
               });
             }
           }
         };
       }]);

  /**
   * The template field directive managed a custom field which
   * is based on an XML snippet to be sent in the form with some
   * string to be replace from related inputs.
   *
   * This allows to edit a complex XML structure with simple
   * form fields. eg. a date of creation where only the date field
   * is displayed to the end user and the creation codelist value
   * is in the XML template for the field.
   */
  module.directive('gnTemplateField', ['$http', '$rootScope', '$timeout',
    function($http, $rootScope, $timeout) {

      return {
        restrict: 'A',
        replace: false,
        transclude: false,
        scope: {
          id: '@gnTemplateField',
          keys: '@',
          values: '@',
          notSetCheck: '@'
        },
        link: function(scope, element, attrs) {
          var xmlSnippetTemplate = element[0].innerHTML;
          var separator = '$$$';
          var fields = scope.keys && scope.keys.split(separator);
          var values = scope.values && scope.values.split(separator);

          // Replace all occurence of {{fieldname}} by its value
          var generateSnippet = function() {
            var xmlSnippet = xmlSnippetTemplate, isOneFieldDefined = false;

            angular.forEach(fields, function(fieldName) {
              var field = $('#' + scope.id + '_' + fieldName);
              var value = '';
              if (field.attr('type') === 'checkbox') {
                value = field.is(':checked') ? 'true' : 'false';
              } else {
                value = field.val() || '';
              }

              if (value !== '') {
                xmlSnippet = xmlSnippet.replace(
                    '{{' + fieldName + '}}',
                    value.replace(/\&/g, '&amp;amp;')
                    .replace(/\"/g, '&quot;'));

                // If one value is defined the field
                // is defined
                isOneFieldDefined = true;
              } else {
                xmlSnippet = xmlSnippet.replace(
                    '{{' + fieldName + '}}',
                    '');
              }
            });

            // Usually when a template field is link to a
            // gnTemplateFieldAddButton directive, the keys
            // is empty.
            if (scope.keys === undefined || scope.keys === '') {
              isOneFieldDefined = true;
            }

            // Reset the snippet if no match were found
            // which means that no fields have values
            if (isOneFieldDefined) {
              element[0].innerHTML = xmlSnippet;
            } else {
              element[0].innerHTML = '';
            }
          };
          var init = function() {
            // Initialize all values
            angular.forEach(values, function(value, key) {
              var selector = '#' + scope.id + '_' + fields[key];
              if ($(selector).attr('type') === 'checkbox') {
                $(selector).attr('checked', value === 'true');
              } else {
                $(selector).val(value);
              }
            });

            // Register change event on each fields to be
            // replaced in the XML snippet.
            angular.forEach(fields, function(value) {
              $('#' + scope.id + '_' + value).change(function() {
                generateSnippet();
              });
            });


            // If template element is not existing in the metadata
            var unsetCheckbox = $('#gn-template-unset-' + scope.notSetCheck);
            if (unsetCheckbox[0] !== undefined) {
              // Reset the template
              element[0].innerHTML = '';
              // When checkbox is checked generate default
              // snippet.
              unsetCheckbox.change(function() {
                $('#' + scope.notSetCheck).toggleClass('hidden');
                if (unsetCheckbox[0].checked) {
                  element[0].innerHTML = '';
                } else {
                  generateSnippet();
                }
              });
            } else {
              generateSnippet();
            }
          };
          $timeout(function() {
            init();
          });
        }
      };
    }]);
})();
