(function() {
  goog.provide('gn_md_editor_service');

  var module = angular.module('gn_md_editor_service', []);


  module.provider('gnMdEditorService',
      function() {
        this.$get = [
          '$q',
          '$rootScope',
          '$http',
          'gnUrlUtils',
          function($q, $rootScope, $http, gnUrlUtils, Keyword, editor) {
            return {

            };
          }];
      });
})();
