(function() {
  goog.provide('gn_template_field_directive');

  var module = angular.module('gn_template_field_directive', []);
  module.directive('gnTemplateFieldAddButton', ['gnEditor', 'gnCurrentEdit',
    function(gnEditor, gnCurrentEdit) {

      return {
        restrict: 'A',
        replace: true,
        scope: {
          id: '@gnTemplateFieldAddButton'
        },
        link: function(scope, element, attrs) {
          var textarea = $(element).parent()
            .find('textarea[name=' + scope.id + ']');
          // Unregister this textarea to the form
          // It will be only submitted if user click the add button
          textarea.removeAttr('name');

          scope.addFromTemplate = function() {
            textarea.attr('name', scope.id);

            // Save and refreshform
            gnEditor.save(gnCurrentEdit.id, true);
          };

          $(element).click(scope.addFromTemplate);
        }
      };
    }]),
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
  module.directive('gnTemplateField', ['$http', '$rootScope',
    function($http, $rootScope) {

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
            var xmlSnippet = xmlSnippetTemplate, updated = false;

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
              } else {
                xmlSnippet = xmlSnippet.replace(
                    '{{' + fieldName + '}}',
                    '');
              }
              updated = true;
            });

            // Usually when a template field is link to a
            // gnTemplateFieldAddButton directive, the keys
            // is empty.
            if (scope.keys === undefined) {
              updated = true;
            }

            // Reset the snippet if no match were found TODO
            // which means that no value is defined
            element[0].innerHTML = '';
            if (updated) {
              element[0].innerHTML = xmlSnippet;
            }
          };

          var init = function() {
            // Register change event on each fields to be
            // replaced in the XML snippet.
            angular.forEach(fields, function(value) {
              $('#' + scope.id + '_' + value).change(function() {
                generateSnippet();
              });
            });

            // Initialize all values
            angular.forEach(values, function(value, key) {
              var selector = '#' + scope.id + '_' + fields[key];
              if ($(selector).attr('type') === 'checkbox') {
                $(selector).attr('checked', value === 'true');
              } else {
                $(selector).val(value);
              }
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

          init();
        }
      };
    }]);
})();
