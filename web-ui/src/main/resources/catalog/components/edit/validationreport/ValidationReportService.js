(function() {
  'use strict';
  goog.provide('gn_validation_service');

  var module = angular.module('gn_validation_service', [
  ]);

  module.factory('gnValidation', [
    'gnHttp',
    'gnEditor',
    'gnCurrentEdit',
    '$q',
    function(gnHttp, gnEditor, gnCurrentEdit, $q) {
      return {

        get: function() {
          var defer = $q.defer();

          gnHttp.callService('getValidation', {
            id: gnCurrentEdit.id
          }).success(function(data) {
            // Empty response return null string.
            var response = data !== 'null' ? data : null;
            defer.resolve(response);
          });
          return defer.promise;
        }
      };
    }]);
})();
