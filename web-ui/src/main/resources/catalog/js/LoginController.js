(function() {
  goog.provide('gn_login_controller');

  var module = angular.module('gn_login_controller', []);

  module.constant('$LOCALES', ['core']);

  /**
   *
   */
  module.controller('GnLoginController',
      ['$scope', '$http', '$rootScope', '$translate',
       function($scope, $http,$rootScope, $translate) {

         /**
               * Register user
               */
         $scope.register = function(formId) {
           $http.get('create.account@json?' + $(formId).serialize())
          .success(function(data) {
             $scope.registrationStatus = data;

           })
          .error(function(data) {

             $rootScope.$broadcast('StatusUpdated', {
               title: $translate('registrationError'),
               error: data,
               timeout: 0,
               type: 'danger'});
           });
         };
       }]);

})();
