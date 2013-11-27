(function() {
  goog.provide('gn_fields_directive');

  goog.require('gn_metadata_manager_service');

  var module = angular.module('gn_fields_directive',
      []);

  /**
   * Provide check field utilities.
   *
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
})();
