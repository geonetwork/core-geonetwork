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
          name: 'Jesse',
          surname: 'Eichar',
          email: 'jesse.eichar@camptocamp.com',
          organization: "camptocamp SA",
          role: 'pointOfContact'
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
            name: 'Florent',
            surname: 'Gravin',
            email: 'florent.gravin@camptocamp.com',
            organization: "camptocamp SA",
            role: 'owner'
          },
          keyword: 'building'
        }
      };
      $scope.editLang = $scope.data.language;
      $scope.$watch("data.language", function (newVal, oldVal) {
        var langs =  $scope.data.otherLanguages;
        var i = langs.indexOf(oldVal);
        if (i > -1) {
          langs.splice(i, 1);
        }
        langs.push(newVal);
        $scope.editLang = newVal;
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
      }
  }]);

  module.directive('contact', function() {
    return {
      scope: {
        title: '@',
        contact: '=',
        editContact: '&'
      },
      restrict: 'A',
      replace: 'true',
      template: '<div class="form-group">' +
        '<label class="col-xs-3 control-label"><span data-translate="">{{title}}</span>: </label>' +
          '<div class="col-xs-8">' +
            '<p class="form-control-static">{{contact.name}} {{contact.surname}}</p>' +
            '<p class="form-control-static">{{contact.email}}</p>' +
            '<p class="form-control-static">{{contact.role}}</p>' +
            '<p class="form-control-static">{{contact.organization}}</p>' +
            '<button type="button" class="btn btn-default" data-ng-click="editContact()" data-translate="">modify</button>' +
        '</div></div></form>'
    };
  });
  module.directive('multilingualText', function() {
    return {
      scope: {
        title: '@',
        rows: '@',
        languages: '=',
        field: '='
      },
      restrict: 'A',
      replace: 'true',
      template: '<div class="form-group">' +
        '<label class="col-xs-3 control-label"><span data-translate="">{{title}}</span>: </label>' +
          '<div class="col-xs-8">' +
            '<p class="form-control-static">{{contact.name}} {{contact.surname}}</p>' +
            '<p class="form-control-static">{{contact.email}}</p>' +
            '<p class="form-control-static">{{contact.role}}</p>' +
            '<p class="form-control-static">{{contact.organization}}</p>' +
            '<button type="button" class="btn btn-default" data-ng-click="editContact()" data-translate="">modify</button>' +
        '</div></div></form>'
    };
  });

})();
