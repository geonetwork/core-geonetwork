(function() {
  goog.provide('gn_fields_directive');

  goog.require('gn_metadata_manager_service');

  var module = angular.module('gn_fields_directive',
      []);

  /**
   * Note: ng-model and angular checks could not be applied to
   * the editor form as it would require to init the model
   * from the form content using ng-init for example.
   */
  module.directive('gnCheck',
      function() {
        return {
          restrict: 'A',
          link: function(scope, element, attrs) {

            // Required attribute
            if (attrs.required) {
              element.keyup(function() {
                if ($(this).get(0).value == '') {
                  $(attrs.gnCheck).addClass('has-error');
                } else {
                  $(attrs.gnCheck).removeClass('has-error');
                }
              });
              element.keyup();
            }
          }
        };
      });

  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnCountryPicker
   * @function
   * 
   * @description
   * Use the region API to retrieve the list of
   * Country.
   *
   * TODO: This could be used in other places
   * probably. Move to another common or language module ?
   */
  module.directive('gnCountryPicker', ['gnHttp',
    function(gnHttp) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          gnHttp.callService('country', {}, {
            cache: true
          }).success(function(response) {
            var data = response.region;

            // Compute default name and add a
            // tokens element which is used for filter
            angular.forEach(data, function(country) {
              country.tokens = [];
              angular.forEach(country.label, function(label) {
                country.tokens.push(label);
              });
              country.name = country.label[scope.lang];
            });

            $(element).typeahead({
              name: 'countries',
              valueKey: 'name',
              local: data,
              minLength: 0,
              limit: 30
            });
          });
        }
      };
    }]);
  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnLanguagePicker
   * @function
   * 
   * @description
   * Use the lang service to retrieve the list of
   * ISO language available and provide autocompletion
   * for the input field with that directive attached.
   *
   * TODO: This could be used in other places
   * like admin > harvesting > OGC WxS
   * probably. Move to another common or language module ?
   */
  module.directive('gnLanguagePicker', ['gnHttp',
    function(gnHttp) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          gnHttp.callService('lang', {}, {
            cache: true
          }).success(function(data) {
            // Compute default name and add a
            // tokens element which is used for filter
            angular.forEach(data, function(lang) {
              var defaultName = lang.label['eng'];
              lang.name = lang.label[scope.lang] || defaultName;
              lang.tokens = [lang.name, lang.code, defaultName];
            });

            $(element).typeahead({
              name: 'isoLanguages',
              valueKey: 'code',
              template: function(datum) {
                return '<p>' + datum.name + ' (' + datum.code + ')</p>';
              },
              local: data,
              minLength: 0,
              limit: 30
            });
          });
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnFieldTooltip
   * @function
   * 
   * @description
   * Initialized field label or fieldset legend tooltip
   * based on the tooltip configuration.
   * 
   * @param {string} gnFieldTooltip The tooltip configuration
   *  which identified the schema, the element name and optionnaly the
   *  element parent name and XPath. eg. 'iso19139|gmd:fileIdentifier'.
   *  
   * @param {string} placement Tooltip placement. Default to 'bottom' 
   * See {@link http://getbootstrap.com/javascript/#tooltips}
   * 
   * 
   * @example
  <example>
    <file name="index.html">
      <label for="gn-field-3" 
         data-gn-field-tooltip="iso19139|gmd:fileIdentifier|gmd:MD_Metadata|
         /gmd:MD_Metadata/gmd:fileIdentifier"
         data-placement="left">File identifier</label>
   </file>
   </example>
   */
  module.directive('gnFieldTooltip',
      ['gnSchemaManagerService',
       function(gnSchemaManagerService) {
         return {
           restrict: 'A',
           link: function(scope, element, attrs) {
             var isInitialized = false;
             var initTooltip = function() {
               if (!isInitialized) {
                 // Retrieve field information (there is a cache)
                 gnSchemaManagerService
                  .getElementInfo(attrs.gnFieldTooltip).then(function(data) {
                   var info = data[0];
                   if (info.description && info.description.length > 0) {
                     // Initialize tooltip when description returned
                     // TODO: Create some kind of template
                     element.tooltip({
                       title: info.description,
                       placement: attrs.placement || 'bottom'
                     });
                     element.tooltip('show');
                     isInitialized = true;
                   }
                 });
               }
             };

             // On hover trigger the tooltip init
             element.hover(function() {
               initTooltip();
             });
           }
         };
       }]);

  /**
   * Move an element up or down. If direction
   * is not defined, direction is down.
   */
  module.directive('gnEditorControlMove', ['gnEditor',
    function(gnEditor) {
      return {
        restrict: 'A',
        scope: {
          ref: '@gnEditorControlMove',
          domelementToMove: '@',
          direction: '@'
        },
        link: function(scope, element, attrs) {
          $(element).click(function() {
            gnEditor.move(scope.ref, scope.direction || 'down',
                scope.domelementToMove);
          });
        }
      };
    }]);
})();
