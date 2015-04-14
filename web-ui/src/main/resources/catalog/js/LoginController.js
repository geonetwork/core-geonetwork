(function() {

  goog.provide('gn_login_controller');


  goog.require('gn_catalog_service');
  goog.require('gn_utility');

  var module = angular.module('gn_login_controller', [
    'gn_utility',
    'gn_catalog_service'
  ]);

  /**
   *    Take care of login action, reset and update password.
   */
  module.controller('GnLoginController',
      ['$scope', '$http', '$rootScope', '$translate',
       '$location', '$window', '$timeout',
       'gnUtilityService', 'gnConfig',
       function($scope, $http, $rootScope, $translate, 
           $location, $window, $timeout,
               gnUtilityService, gnConfig) {
          $scope.formAction = '../../j_spring_security_check#' +
         $location.path();
          $scope.registrationStatus = null;
          $scope.passwordReminderStatus = null;
          $scope.sendPassword = false;
          $scope.password = null;
          $scope.passwordCheck = null;
          $scope.userToRemind = null;
          $scope.changeKey = null;
          $scope.passwordUpdated = false;

          $scope.redirectUrl = gnUtilityService.getUrlParameter('redirect');
          $scope.signinFailure = gnUtilityService.getUrlParameter('failure');
          $scope.gnConfig = gnConfig;

          function initForm() {
           if ($window.location.pathname.indexOf('new.password') !== -1) {
             // Retrieve username from URL parameter
             $scope.userToRemind = gnUtilityService.getUrlParameter('username');
             $scope.changeKey = gnUtilityService.getUrlParameter('changeKey');
           }
          }

          // TODO: https://github.com/angular/angular.js/issues/1460
          // Browser autofill does not work properly
          $timeout(function() {
            $('input[data-ng-model], select[data-ng-model]').each(function() {
              angular.element(this).controller('ngModel')
                .$setViewValue($(this).val());
            });
          }, 300);

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
