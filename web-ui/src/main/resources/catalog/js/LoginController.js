(function() {
  goog.provide('gn_login_controller');

  var module = angular.module('gn_login_controller', []);

  module.constant('$LOCALES', ['core']);

  /**
   *    Take care of login action, reset and update password.
   */
  module.controller('GnLoginController',
      ['$scope', '$http', '$rootScope', '$translate', '$location', '$window',
       'gnUtilityService',
       function($scope, $http, $rootScope, $translate, $location, $window,
               gnUtilityService) {
          $scope.registrationStatus = null;
          $scope.passwordReminderStatus = null;
          $scope.sendPassword = false;
          $scope.password = null;
          $scope.passwordCheck = null;
          $scope.userToRemind = null;
          $scope.changeKey = null;
          $scope.passwordUpdated = false;

          $scope.redirectUrl = gnUtilityService.getUrlParameter('redirect');

          function initForm() {
           if ($window.location.pathname.indexOf('new.password') !== -1) {
             // Retrieve username from URL parameter
             $scope.userToRemind = gnUtilityService.getUrlParameter('username');
             $scope.changeKey = gnUtilityService.getUrlParameter('changeKey');
           }
          }

          $scope.setSendPassword = function(value) {
           $scope.sendPassword = value;
           $('#username').focus();
          };
         /**
          * Register user. An email will be sent to the new
          * user and another to the catalog admin if a profile
          * higher than registered user is requested.
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
         /**
          * Remind user password.
          */
         $scope.remindMyPassword = function(formId) {
           $http.get('password.reminder@json?' + $(formId).serialize())
            .success(function(data) {
             $scope.passwordReminderStatus = data;
             $scope.sendPassword = false;
           })
            .error(function(data) {
             $rootScope.$broadcast('StatusUpdated', {
               title: $translate('passwordReminderError'),
               error: data,
               timeout: 0,
               type: 'danger'});
           });
         };

         /**
          * Change user password.
          */
         $scope.updatePassword = function(formId) {
           $http.get('password.change@json?' + $(formId).serialize())
            .success(function(data) {
             if (data == 'null') {
               $scope.passwordUpdated = true;
             }
           })
            .error(function(data) {

             $rootScope.$broadcast('StatusUpdated', {
               title: $translate('passwordUpdateError'),
               error: data,
               timeout: 0,
               type: 'danger'});
           });
         };

         $scope.nodeChangeRedirect = function(redirectTo) {
           $http.get('../../j_spring_security_logout')
              .success(function(data) {
                  window.location.href = redirectTo;
           });
         };

         initForm();

       }]);

})();
