(function() {
  'use strict';
  goog.provide('gn_inspire_editor');

  goog.require('gn');
  goog.require('inspire_contact_directive');
  goog.require('inspire_multilingual_text_directive');
  goog.require('inspire_get_shared_users_factory');
  goog.require('inspire_get_keywords_factory');
  goog.require('inspire_get_extents_factory');
  goog.require('inspire_date_picker_directive');
  goog.require('inspire-metadata-loader');

  var module = angular.module('gn_inspire_editor',
    [ 'gn', 'inspire_contact_directive', 'inspire_multilingual_text_directive', 'inspire_metadata_factory',
      'inspire_get_shared_users_factory', 'inspire_get_keywords_factory', 'inspire_get_extents_factory',
      'inspire_date_picker_directive']);

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
    '$scope', 'inspireMetadataLoader', '$translate', '$http',
    function($scope, inspireMetadataLoader, $translate, $http) {
      var allowUnload = false;
      window.onbeforeunload = function() {
        if (!allowUnload) {
          return $translate('beforeUnloadEditor');
        }
      };
      $scope.languages = ['ger', 'fre', 'ita', 'eng', 'roh'];

      var params = window.location.search;
      var mdId = params.substring(params.indexOf("id=") + 3);
      var indexOfAmp = mdId.indexOf('&');
      if (indexOfAmp > -1) {
        mdId = mdId.substring(0, indexOfAmp);
      }
      $scope.mdId = mdId;

      $scope.data = inspireMetadataLoader($scope.lang, $scope.url, mdId);
      $scope.validationErrorClass = 'has-error';
      $scope.validationClassString = function(model) {
        if (model && model.length > 0) {
          return '';
        }
        return $scope.validationErrorClass;
      };
      $scope.validationClassArray = function(model, property) {
        var cls, elem, i;
        for (i = 0; i < model.length; i++) {
          elem = model[i];
          cls = $scope.validationClassString(elem[property]);
          if (cls) {
            return cls;
          }
        }
        return '';
      };
      $scope.$watchCollection("data.otherLanguages", function(langs) {
        if (langs.length === 0) {
          if ($scope.data.mainLang) {
            langs.push($scope.data.mainLang);
          } else {
            langs.push($scope.languages[0]);
          }
        }
        langs.sort(function(a,b) {
          if (a === $scope.data.language) {
            return -1;
          }
          if (b === $scope.data.language) {
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
      $scope.deleteFromArray = function(model, elemToRemove) {
        var i = model.indexOf(elemToRemove);
        if (i > -1) {
          model.splice(i, 1);
        }
      };

      $scope.saveMetadata = function(editTab) {
        var waitDialog = $('#pleaseWaitDialog');
        if (waitDialog) {
          waitDialog.find('h2').text($translate('saveInProgress'));
          waitDialog.modal();
        }
        var dataClone = angular.copy($scope.data);
        delete dataClone.roleOptions;
        delete dataClone.dateTypeOptions;
        delete dataClone.hierarchyLevelOptions;
        delete dataClone.topicCategoryOptions;
        delete dataClone.constraintOptions;
        delete dataClone.serviceTypeOptions;
        delete dataClone.metadataTypeOptions;
        delete dataClone.scopeCodeOptions;
        delete dataClone.conformityTitleOptions;

        var data = JSON.stringify(dataClone);
        return $http({
          method: 'POST',
          url: $scope.url + "inspire.edit.save",
          data: 'id=' + mdId + '&data=' + data,
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
          if (!/.*<ok\s*\/\s*>.*/ig.test(data)) {

            if (waitDialog) {
              waitDialog.modal('hide');
            }

            if (/.*<body\s*.*/ig.test(data)) {
              data = '<div>' + $translate("unexpectedSaveError") + '</div>';
            }

            var dialogContentEl = $('#errorDialogContent');
            dialogContentEl.text('');
            dialogContentEl.append(data);
            $('#errorDialog').modal();
          }
          if (editTab) {
            allowUnload = true;
            window.location.href = 'metadata.edit?id=' + mdId + '&currTab=' + editTab;
          } else {
            if (waitDialog) {
              waitDialog.modal('hide');
            }
          }
        }).error(function (data) {
          if (waitDialog) {
            waitDialog.modal('hide');
          }
          alert($translate("saveError")  + data);
        });
      };

      $scope.stopEditing = function() {
        allowUnload = true;
        window.location.href = 'metadata.show?id=' + mdId;
      };

      $scope.saveMetadataAndExit = function (){
        $scope.saveMetadata().success(function(){
          $scope.stopEditing();
        });
      };

  }]);


  module.controller('InspireKeywordController', [
    '$scope', 'inspireGetKeywordsFactory',
    function($scope, inspireGetKeywordsFactory) {
      $scope.editKeyword = function(keyword) {
        $scope.keywordUnderEdit = keyword;
        $scope.selectedKeyword = {};
        var modal = $('#editKeywordModal');
        modal.modal('show');
      };
      $scope.deleteKeyword = function(keyword) {
        var keywords = $scope.data.identification.descriptiveKeywords;
        keywords.splice(keywords.indexOf(keyword), 1);
      };

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
        $scope.selectedKeyword = keyword;
      };

      $scope.linkToOtherKeyword = function() {
        var keyword = $scope.selectedKeyword;
        $scope.keywordUnderEdit.code = keyword.code;
        $scope.keywordUnderEdit.words = keyword.words;
        var modal = $('#editKeywordModal');
        modal.modal('hide');
      };
      $scope.validationCls = '';
      $scope.validateKeywords = function(){
        var i, keywords, valid = false;
        keywords = $scope.data.identification.descriptiveKeywords;

        for (i = 0; i < keywords.length; i++) {
          if (keywords[i].code && keywords[i].code.length > 0) {
            valid = true;
          }
        }

        $scope.validationCls = valid ? '' : $scope.validationErrorClass;
      };

      $scope.$watch('data.identification.descriptiveKeywords', $scope.validateKeywords, true);
    }]);


  module.controller('InspireExtentController', [
    '$scope', 'inspireGetExtentsFactory',
    function($scope, inspireGetExtentsFactory) {
      $scope.loadingExtents = false;
      $scope.extentImgSrc = function (width, extent) {
        return $scope.url + 'region.getmap.png?mapsrs=EPSG:21781&background=geocat&width=' + width + '&id=' + extent.geom;
      };
      $scope.editExtent = function(extent) {
        $scope.extentUnderEdit = extent;
        $scope.selectedExtent = {};
        var modal = $('#editExtentModal');
        modal.modal('show');
      };

      $scope.selectExtent = function (extent) {
        $scope.selectedExtent = extent;
      };

      $scope.searchExtents = function (query) {
        inspireGetExtentsFactory($scope, $scope.url, query).then (function (extents){
          $scope.extents = extents;
        });

        $scope.validationCls = '';
        $scope.linkToOtherExtent = function () {
          $scope.extentUnderEdit.geom = $scope.selectedExtent.geom;
          $scope.extentUnderEdit.description = $scope.selectedExtent.description;
          var modal = $('#editExtentModal');
          modal.modal('hide');
        };
        $scope.validateExtents = function(){
          var i, extents, valid = false;
          extents = $scope.data.identification.extents;

          for (i = 0; i < extents.length; i++) {
            if (extents[i].geom && extents[i].geom.length > 0) {
              valid = true;
            }
          }

          $scope.validationCls = valid ? '' : $scope.validationErrorClass;
        };

        $scope.validateExtents();

      };
    }]);

  module.controller('InspireConstraintsController', [
    '$scope', function($scope) {
      var countProperties = function(propertyName) {
        var legal, i, count;
        var legalConstraints = $scope.data.constraints.legal;

        count = 0;
        for (i = 0; i < legalConstraints.length; i++) {
          legal = legalConstraints[i];
          if (legal[propertyName]) {
            count += legal[propertyName].length;
          }
        }
        return count;
      };

      $scope.propertyCount = {
        accessConstraints: 0,
        useConstraints: 0,
        useLimitations: 0,
        updateAccessConstraints: function() {
          $scope.propertyCount.accessConstraints = countProperties('accessConstraints');
          if ($scope.propertyCount.accessConstraints === 0) {
            $scope.data.constraints.legal[0].accessConstraints = [''];
            $scope.propertyCount.accessConstraints = 1;
          }
        },
        updateUseConstraints: function() {
          $scope.propertyCount.useConstraints = countProperties('useConstraints');
          if ($scope.propertyCount.useConstraints === 0) {
            $scope.data.constraints.legal[0].useConstraints = [''];
            $scope.propertyCount.useConstraints = 1;
          }
        },
        updateUseLimitations: function() {
          $scope.propertyCount.useLimitations = countProperties('useLimitations');
          if ($scope.propertyCount.useLimitations === 0) {
            $scope.data.constraints.legal[0].useLimitations = [{}];
            $scope.propertyCount.useLimitations = 1;
          }
        }
      };

      $scope.$watchCollection('data.constraints.legal', function(newValue) {
        if (newValue.length === 0) {
          newValue.push({
            accessConstraints: [''],
            useConstraints: [''],
            useLimitations: [{}],
            otherConstraints: [],
            legislationConstraints: []
          });
        }

        $scope.propertyCount.updateAccessConstraints();
        $scope.propertyCount.updateUseConstraints();
        $scope.propertyCount.updateUseLimitations();
      });
      $scope.hasOtherRestrictions = function(legalConstraint) {
        var x;
        for (x = 0; x < legalConstraint.accessConstraints.length; x++) {
          if (legalConstraint.accessConstraints[x] === 'otherRestrictions') {
            return true;
          }
        }
        return false;
      };
    }]);

  module.controller('InspireAccessConstraintController', [
    '$scope', function($scope) {

      $scope.$watch('accessConstraint', function() {
        var i, j, otherConstraint, toRemove, remove;
        var hasOtherRestrictions = $scope.hasOtherRestrictions($scope.legal);
        if (hasOtherRestrictions && $scope.legal.otherConstraints.length === 0) {
          otherConstraint = {};
          otherConstraint[$scope.data.language] = '';
          $scope.legal.otherConstraints.push(otherConstraint);
        } else if (!hasOtherRestrictions) {
          toRemove = [];
          for (i = 0; i < $scope.legal.otherConstraints.length; i++) {
            otherConstraint = $scope.legal.otherConstraints[i];
            remove = otherConstraint[$scope.data.language].trim().length === 0;
            for (j = 0; j < $scope.data.otherLanguages.length; j++) {
              if (otherConstraint[$scope.data.language].trim().length > 0) {
                remove = false;
                break;
              }
            }

            if (remove) {
              toRemove.push(otherConstraint);
            }
          }

          for (i = 0; i < toRemove.length; i++) {
            $scope.deleteFromArray($scope.legal.otherConstraints, toRemove[i]);
          }
        }
      });
  }]);

  module.controller('InspireConformityController', [
    '$scope', '$translate', function($scope, $translate) {

      $scope.validationClassObject = function(model) {
        var cls, elem, i;
        for (i in model) {
          if (model.hasOwnProperty(i)) {
            elem = model[i];

            if (elem.length > 0) {
              return '';
            }
          }
        }
        return $scope.validationErrorClass;
      };

      $scope.passOptions = [{
        val: 'true',
        name: $translate('passConformity')
      },{
        val: 'false',
        name: $translate('failConformity')
      }];

      $scope.$watch('data.conformity.title', function(newVal) {
        var i, option, lang;
        for (i = 0; i < $scope.data.conformityTitleOptions.length; i++) {
          option = $scope.data.conformityTitleOptions[i];
          if (option === newVal) {
            return;
          }

          for(lang in newVal) {
            if (newVal.hasOwnProperty(lang) && option[lang] === newVal[lang]) {
              $scope.data.conformity.title = option;
              return;
            }
          }
        }
      });

  }]);


}());
