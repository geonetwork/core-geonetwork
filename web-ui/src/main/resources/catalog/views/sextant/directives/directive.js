(function() {
  goog.provide('sxt_directives');

  goog.require('sxt_categorytree');
  goog.require('sxt_layertree');
  goog.require('sxt_viewer_directive');
  goog.require('sxt_tabswitcher');
  goog.require('sxt_graticule');

  var module = angular.module('sxt_directives', [
      'sxt_categorytree',
      'sxt_layertree',
      'sxt_viewer_directive',
      'sxt_graticule',
      'sxt_tabswitcher'
  ]);
})();
