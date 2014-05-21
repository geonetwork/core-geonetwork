(function() {
  goog.provide('gn_contact_us_controller');

  goog.require('gn_contactus_directive');

  var module = angular.module('gn_contact_us_controller',
      ['gn_contactus_directive']);

  module.constant('$LOCALES', ['core']);

  /**
   *
   */
  module.controller('GnContactUsController',
      ['$scope', 'gnConfig',
       function($scope, gnConfig) {
         $scope.gnConfig = gnConfig;
       }]);

})();
