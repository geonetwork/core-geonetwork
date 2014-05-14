(function() {
  goog.provide('inspire_contact_directive');
  goog.require('inspire_multilingual_text_directive');

  var module = angular.module('inspire_contact_directive', ['inspire_multilingual_text_directive']);

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
        '<div class="col-xs-8">' +
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
        '</div>' +
        '</div>' +
        '<button type="button" class="btn btn-default" data-ng-click="editContact()" data-translate="">modify</button>' +
        '</div></div>'
    };
  });
})();
