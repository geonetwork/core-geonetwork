(function() {
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
          $scope.$parent.contactUnderEdit.role = role ? role : $scope.data.roleOptions[0];
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
          } else {
            return scope.validationClass;
          }
        };
      },
      template: '<div class="form-group" >' +
        '<label data-ng-if="showTitle" class="col-xs-3 control-label"><span data-translate="">{{title}}</span>: </label>' +
        '<div data-ng-class="showTitle ? \'\' : \'col-xs-offset-3\'" class="col-xs-9">' +
        '<div class="form-group" data-ng-class="validationClassString(contact.name)">' +
        '<div class="col-xs-6">' +
        '<input ng-disabled="contact.validated" id="contactName" class="form-control" ng-model="contact.name" placeholder="{{\'name\' | translate}}"></select>' +
        '</div>' +
        '<div class="col-xs-6" data-ng-class="validationClassString(contact.surname)">' +
        '<input ng-disabled="contact.validated" id="contactSurname" class="form-control" ng-model="contact.surname" placeholder="{{\'surname\' | translate}}"></select>' +
        '</div>' +
        '</div>' +
        '<div class="form-group">' +
        '<div class="col-xs-12" data-ng-class="validationClassString(contact.email)">' +
        '<input ng-disabled="contact.validated" id="contactEmail" class="form-control" ng-model="contact.email" placeholder="{{\'email\' | translate}}"></select>' +
        '</div>' +
        '</div>' +
        '<div class="form-group">' +
        '<div class="col-xs-12" data-ng-class="validationClassString(contact.role)">' +
        '<select ng-disabled="contact.validated" id="contactRole" class="form-control" ng-model="contact.role"' +
        'data-ng-options="opt.name as opt.title for opt in roleOptions"></select>' +
        '</div>' +
        '</div>' +
        '<div class="form-group">' +
        '<div class="col-xs-12">' +
        '<div data-ng-disabled="contact.validated" data-inspire-multilingual-text data-field="contact.organization" ' +
        'validation-class="{{validationClass}}"' +
        '     data-placeholder="{{\'organization\' | translate}}" data-rows="1" data-main-lang="mainLang" data-languages="languages"/>' +
//        '<button type="button" class="btn btn-default" data-ng-click="editContact()" ><i class="fa fa-edit" style="padding-right: 5px"></i>{{\'modify\' | translate }}</button>' +
        '</div>' +
        '</div>' +
        '</div></div>'
    };
  });
})();
