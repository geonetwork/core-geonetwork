(function() {
  goog.provide('gn_inspire_editor');

  goog.require('gn');

  var module = angular.module('gn_inspire_editor', ['gn']);

  // Define the translation files to load
  module.constant('$LOCALES', ['core', 'editor', 'inspire']);

  module.config(['$translateProvider', '$LOCALES',
    function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);

  module.controller('GnInspireController', [
    '$scope', '$http', '$q', '$rootScope', '$translate',
    function($scope, $http, $q, $rootScope, $translate) {
      $scope.languages = ['ger', 'fre', 'ita', 'eng'];
      $scope.data = {
        roleOptions: ['pointOfContact', 'owner', 'custodian'],
        language: "eng",
        characterSet: "UTF8",
        hierarchyLevel: "Dataset",
        hierarchyLevelOptions: [
          'Attribute',
          'AttributeType',
          'Dataset'
        ],
        contact: {
          id: '2',
          name: 'Jesse',
          surname: 'Eichar',
          email: 'jesse.eichar@camptocamp.com',
          organization: "camptocamp SA",
          role: 'pointOfContact',
          validated: true
        },
        otherLanguages: ['eng', 'ger'],
        identification: {
          type: 'data',
          title: {eng: 'Title',fre: 'Titre'},
          date: '12-12-2008',
          dateType: 'creation',
          dateTypeOptions: ['creation', 'publication', 'revision'],
          citationIdentifier: 'identifier',
          abstract: {fre: 'Abstract'},
          pointOfContact:  {
            id: '1',
            name: 'Florent',
            surname: 'Gravin',
            email: 'florent.gravin@camptocamp.com',
            organization: "camptocamp SA",
            role: 'owner',
            validated: false
          },
          keyword: 'building'
        }
      };
      $scope.$watch("data.language", function (newVal, oldVal) {
        var langs =  $scope.data.otherLanguages;
        var i = langs.indexOf(oldVal);
        if (i > -1) {
          langs.splice(i, 1);
        }
        langs.push(newVal);
      });
      $scope.$watchCollection("data.otherLanguages", function() {
        var langs =  $scope.data.otherLanguages;
        langs.sort(function(a,b) {
          if (a == $scope.data.language) {
            return -1;
          }
          if (b == $scope.data.language) {
            return 1;
          }
          return $scope.languages.indexOf(a) - $scope.languages.indexOf(b);
        });

      });
      $scope.isOtherLanguage = function (lang) {
        var langs =  $scope.data.otherLanguages;
        var i = 0;
        for (i = 0; i < langs.length; i++) {
          if (lang === langs[i]) {
            return true;
          }
        }
      };
      $scope.toggleLanguage = function (lang) {
        var langs =  $scope.data.otherLanguages;
        if (lang !== $scope.data.language) {
          var i = langs.indexOf(lang);
          if (i > -1) {
            langs.splice(i, 1);
          } else {
            langs.push(lang);
          }
        }
      };
      $scope.editContact = function(title, contact) {
        $scope.contactUnderEdit = contact;
        $scope.contactUnderEdit.title = title;
        var modal = $('#editContactModal');
        modal.modal('show');
      };
      $scope.updateContact = function(newContact) {
        $scope.contactUnderEdit.id = newContact ? newContact.id : '';
        $scope.contactUnderEdit.name = newContact ? newContact.name : '';
        $scope.contactUnderEdit.surname = newContact ? newContact.surname : '';
        $scope.contactUnderEdit.email = newContact ? newContact.email : '';
        $scope.contactUnderEdit.role = newContact ? newContact.role : $scope.data.roleOptions[0];
        $scope.contactUnderEdit.organization = newContact ? newContact.organization : '';
        $scope.contactUnderEdit.validated = newContact ? newContact.validated : false;
        var modal = $('#editContactModal');
        modal.modal('hide');
      }
  }]);

  module.directive('contact', function() {
    return {
      scope: {
        title: '@',
        contact: '=',
        roleOptions: '=',
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
        '<input ng-disabled="contact.validated" id="contactOrganization" class="form-control" ng-model="contact.organization" placeholder="{{\'organization\' | translate}}">' +
        '</div>' +
        '</div>' +
        '<button type="button" class="btn btn-default" data-ng-click="editContact()" data-translate="">modify</button>' +
        '</div></div>'
    };
  });
  module.directive('multilingualText', function() {
    return {
      scope: {
        title: '@',
        rows: '@',
        languages: '=',
        mainLang: '=',
        field: '='
      },
      restrict: 'A',
      replace: 'true',
      link: function($scope) {
        $scope.editLang = $scope.mainLang;
        $scope.setEditLang = function(lang) {
          $scope.editLang = lang;
        }
      },
      template: '<div class="form-group">' +
        '<label for="title" class="col-xs-3 control-label" ><span data-translate="">{{title}}</span>: </label>' +
        '<div class="col-xs-9">' +
        '<textarea rows="{{rows}}" id="title" class="form-control col-xs-12" data-ng-repeat="lang in languages" data-ng-model="field[lang]" data-ng-show="editLang === lang || editLang === \'all\'" placeholder="{{lang | translate}}" />' +
        '<ul class="nav nav-pills">' +
        '<li data-ng-class="lang === editLang ? \'active\' : \'\'" data-ng-repeat="lang in languages" data-ng-hide="editLang === \'all\'"> ' +
        '<a data-ng-click="setEditLang(lang)">{{lang | translate}}</a>' +
        '</li>' +
        '<li>' +
        '<a data-ng-click="editLang === \'all\' ? setEditLang(mainLang) : setEditLang(\'all\')">' +
        '{{editLang === \'all\' ? \'collapse\' : \'all\' | translate}}</a>' +
        '</li>' +
        '</ul>' +
        '</div></div>'
    };
  });

})();
