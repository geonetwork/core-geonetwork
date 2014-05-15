(function() {
  goog.provide('inspire_contact_directive');
  goog.require('inspire_multilingual_text_directive');
  goog.require('inspire_get_shared_users_factory');

  var module = angular.module('inspire_contact_directive', ['inspire_multilingual_text_directive', 'inspire_get_shared_users_factory']);

  module.controller('InspireContactController', [
    '$scope', 'inspireGetSharedUsersFactory',
    function($scope, inspireGetSharedUsersFactory) {
      $scope.linkToOtherContact = function() {
        var userId = $scope.selectedSharedUser.id;
        inspireGetSharedUsersFactory.loadDetails($scope.url, userId).then($scope.updateContact);
      };

      $scope.updateContact = function(newContact) {
        $scope.$parent.contactUnderEdit.id = newContact ? newContact.id : '';
        $scope.$parent.contactUnderEdit.name = newContact ? newContact.name : '';
        $scope.$parent.contactUnderEdit.surname = newContact ? newContact.surname : '';
        $scope.$parent.contactUnderEdit.email = newContact ? newContact.email : '';

        var role = newContact ? newContact.role : $scope.$parent.contactUnderEdit.role;
        $scope.$parent.contactUnderEdit.role = role ? role : $scope.data.roleOptions[0];
        $scope.$parent.contactUnderEdit.organization = newContact ? newContact.organization : '';
        $scope.$parent.contactUnderEdit.validated = newContact ? newContact.validated : false;

        var modal = $('#editContactModal');
        modal.modal('hide');
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
        contact: '=',
        roleOptions: '=',
        languages: '=',
        mainLang: '=',
        editContact: '&'
      },
      restrict: 'A',
      replace: 'true',
      template: '<div class="form-group">' +
        '<label class="col-xs-3 control-label"><span data-translate="">{{title}}</span>: </label>' +
        '<div class="col-xs-9">' +
        '<div class="form-group">' +
        '<div class="col-xs-6">' +
        '<input ng-disabled="contact.validated" id="contactName" class="form-control" ng-model="contact.name" placeholder="{{\'name\' | translate}}"></select>' +
        '</div>' +
        '<div class="col-xs-6">' +
        '<input ng-disabled="contact.validated" id="contactSurname" class="form-control" ng-model="contact.surname" placeholder="{{\'surname\' | translate}}"></select>' +
        '</div>' +
        '</div>' +
        '<div class="form-group">' +
        '<div class="col-xs-12">' +
        '<input ng-disabled="contact.validated" id="contactEmail" class="form-control" ng-model="contact.email" placeholder="{{\'email\' | translate}}"></select>' +
        '</div>' +
        '</div>' +
        '<div class="form-group">' +
        '<div class="col-xs-12">' +
        '<select ng-disabled="contact.validated" id="contactRole" class="form-control" ng-model="contact.role"' +
        'data-ng-options="role for role in roleOptions"></select>' +
        '</div>' +
        '</div>' +
        '<div class="form-group">' +
        '<div class="col-xs-12">' +
        '<div data-ng-disabled="contact.validated" data-inspire-multilingual-text data-field="contact.organization" ' +
        '     data-placeholder="{{\'organization\' | translate}}" data-rows="1" data-main-lang="mainLang" data-languages="languages"/>' +
        '<button type="button" class="btn btn-default" data-ng-click="editContact()" ><i class="fa fa-edit" style="padding-right: 5px"></i>{{\'modify\' | translate }}</button>' +
        '</div>' +
        '</div>' +
        '</div></div>'
    };
  });
})();
