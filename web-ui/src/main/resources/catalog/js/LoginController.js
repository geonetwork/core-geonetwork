(function() {
  goog.provide('gn_login_controller');

  var module = angular.module('gn_login_controller', []);

  module.constant('$LOCALES', ['core']);

  /**
   *
   */
  module.controller('GnLoginController', ['$scope', function($scope) {
    $scope.initForm = function() {
      $('#inputUsername').focus();
    };
  }]);

})();
