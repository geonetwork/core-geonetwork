(function() {
  goog.provide('gn_fields');




  goog.require('gn_editor_helper_directive');
  goog.require('gn_field_duration_directive');
  goog.require('gn_template_field_directive');

  angular.module('gn_fields', [
    'gn_field_duration_directive',
    'gn_editor_helper_directive',
    'gn_template_field_directive'
  ]);
})();
