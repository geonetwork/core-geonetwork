(function() {
  goog.provide('gn_inspire_editor');

  goog.require('gn');
  goog.require('inspire_contact_directive');
  goog.require('inspire_multilingual_text_directive');
  goog.require('inspire_get_shared_users_factory');
  goog.require('inspire_get_keywords_factory');
  goog.require('inspire_date_picker_directive');

  goog.require('inspire_mock_full_metadata_factory');

  var module = angular.module('gn_inspire_editor',
    ['gn', 'inspire_contact_directive', 'inspire_multilingual_text_directive', 'inspire_metadata_factory',
      'inspire_get_shared_users_factory', 'inspire_get_keywords_factory', 'inspire_date_picker_directive']);

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
    '$scope', 'inspireMetadataLoader', 'inspireGetSharedUsersFactory', '$translate',
    function($scope, inspireMetadataLoader, inspireGetSharedUsersFactory, $translate) {
      window.onbeforeunload = function() {
        return $translate('beforeUnloadEditor');
      };
      $scope.languages = ['ger', 'fre', 'ita', 'eng'];

      var params = window.location.search;
      var mdId = params.substring(params.indexOf("id=") + 3);
      var indexOfAmp = mdId.indexOf('&');
      if (indexOfAmp > -1) {
        mdId = mdId.substring(0, indexOfAmp);
      }

      inspireMetadataLoader($scope.url, mdId).then(function (data) {
        $scope.data = data;
      });

      $scope.$watch("data.language", function (newVal, oldVal) {
        var langs =  $scope.data.otherLanguages;
        var i = langs.indexOf(oldVal);
        if (i > -1) {
          langs.splice(i, 1);
        }
        if (langs.indexOf(newVal) < 0) {
          langs.push(newVal);
        }
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
        $scope.selectedSharedUser = {};
        var modal = $('#editContactModal');
        modal.modal('show');
      };

      $scope.editKeyword = function(keyword) {
        $scope.keywordUnderEdit = keyword;
        $scope.selectedKeyword = {};
        var modal = $('#editKeywordModal');
        modal.modal('show');
      };
      $scope.deleteKeyword = function(keyword) {
        var keywords = $scope.data.identification.descriptiveKeyword;
        keywords.splice(keywords.indexOf(keyword), 1);
      };
  }]);


  module.controller('InspireKeywordController', [
    '$scope', 'inspireGetKeywordsFactory',
    function($scope, inspireGetKeywordsFactory) {
      $scope.keywords = {
        data: {},
        service: {}
      };
      inspireGetKeywordsFactory($scope.url, 'external.theme.inspire-theme').then (function (keywords) {
        $scope.keywords.data = keywords;
      });
      inspireGetKeywordsFactory($scope.url, 'external.theme.inspire-service-taxonomy').then (function (keywords) {
        $scope.keywords.service = keywords;
      });

      $scope.selectKeyword = function(keyword) {
        $scope.$parent.selectedKeyword = keyword;
      };

      $scope.linkToOtherKeyword = function() {
        var keyword = $scope.$parent.selectedKeyword;
        $scope.keywordUnderEdit.uri = keyword.uri;
        $scope.keywordUnderEdit.words = keyword.words;
        var modal = $('#editKeywordModal');
        modal.modal('hide');
      }
    }]);

})();
