(function() {
  'use strict';
  goog.provide('inspire_contact_directive');
  goog.require('inspire_multilingual_text_directive');
  goog.require('inspire_get_shared_users_factory');

  var module = angular.module('inspire_contact_directive', ['inspire_multilingual_text_directive', 'inspire_get_shared_users_factory']);

  module.controller('InspireContactController', [
    '$scope', 'inspireGetSharedUsersFactory', '$translate',
    function($scope, inspireGetSharedUsersFactory, $translate) {
      $scope.linkToOtherContact = function() {
        var userId = $scope.selectedSharedUser.id;
        inspireGetSharedUsersFactory.loadDetails($scope.url, userId).then($scope.updateContact);
      };

      $scope.updateContact = function(newContact) {
        if (confirm($translate('overwriteContactConfirmation'))) {
          $scope.$parent.contactUnderEdit.id = newContact ? newContact.id : '';
          $scope.$parent.contactUnderEdit.name = newContact ? newContact.name : '';
          $scope.$parent.contactUnderEdit.surname = newContact ? newContact.surname : '';
          $scope.$parent.contactUnderEdit.email = newContact ? newContact.email : '';

          var role = newContact ? newContact.role : $scope.$parent.contactUnderEdit.role;
          $scope.$parent.contactUnderEdit.role = role || $scope.data.roleOptions[0];
          $scope.$parent.contactUnderEdit.organization = newContact ? newContact.organization : '';
          $scope.$parent.contactUnderEdit.validated = newContact ? newContact.validated : false;

          var modal = $('#editContactModal');
          modal.modal('hide');
        }
      };
      inspireGetSharedUsersFactory.loadAll($scope.url).then(function(sharedUsers) {
        $scope.sharedUsers = sharedUsers;
        $scope.$parent.selectedSharedUser = {};
      });
      $scope.setSharedUser = function(user) {
        $scope.$parent.selectedSharedUser = user;
      };
    }]);

  module.directive('inspireContact', function() {
    return {
      scope: {
        title: '@',
        showTitle: '@',
        contact: '=',
        roleOptions: '=',
        languages: '=',
        mainLang: '=',
        validationClass: '@',
        editContact: '&'
      },
      restrict: 'A',
      replace: 'true',
      link: function(scope) {
        scope.validationClassString = function(model) {
          if (model && model.length > 0) {
            return '';
          }

          return scope.validationClass;
        };
        scope.validationEmail = function() {
          var email = scope.contact.email;
          if (/\S+@\S+\.\S+/.test(email)) {
            return '';
          }
          return scope.validationClass;
        };
      },
      templateUrl: '../../catalog/components/edit/inspire/partials/contact.html'
    };
  });
}());
