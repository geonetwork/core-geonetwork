(function() {
  'use strict';
  goog.provide('gn_inspire_editor');

  goog.require('gn_language_switcher');
  goog.require('inspire_contact_directive');
  goog.require('inspire_multilingual_text_directive');
  goog.require('inspire_get_shared_users_factory');
  goog.require('inspire_get_keywords_factory');
  goog.require('inspire_get_extents_factory');
  goog.require('inspire_date_picker_directive');
  goog.require('inspire-metadata-loader');
  goog.require('inspire_ie9_select');
  goog.require('inspire_get_shared_formats_factory');

  angular.module('gn', []);

  var module = angular.module('gn_inspire_editor', ['gn_language_switcher', 'pascalprecht.translate', 'inspire_contact_directive',
    'inspire_multilingual_text_directive', 'inspire_metadata_factory', 'inspire_get_shared_users_factory', 'inspire_get_keywords_factory',
    'inspire_get_extents_factory', 'inspire_date_picker_directive', 'inspire_ie9_select', 'inspire_get_shared_formats_factory']);

  module.factory('localeLoader', ['$http', '$q', function($http, $q) {
    return function(options) {
      var allPromises = [];
      angular.forEach(options.locales, function(value, index) {
        var langUrl = options.prefix +
          options.key + '-' + value + options.suffix;

        var deferredInst = $q.defer();
        allPromises.push(deferredInst.promise);

        $http({
          method: 'GET',
          url: langUrl
        }).success(function(data, status, header, config) {
          deferredInst.resolve(data);
        }).error(function(data, status, header, config) {
          deferredInst.reject(options.key);
        });
      });

      // Finally, create a single promise containing all the promises
      // for each app module:
      var deferred = $q.all(allPromises);
      return deferred;
    };
  }]);

  module.config(['$translateProvider',
    function($translateProvider) {
      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.translations(lang, translationJson);

      $translateProvider.preferredLanguage(lang);

      moment.lang(lang);
    }]);

  module.filter('translateLang', function () {
    var translations = {
      "eng" : "English",
      "en" : "English",
      "fre" : "Français",
      "fr" : "Français",
      "ger" : "Deutsch",
      "ge" : "Deutsch",
      "de" : "Deutsch",
      "deu" : "Deutsch",
      "ita" : "Italiano",
      "it" : "Italiano",
      "roh" : "Rumantsch",
      "rm" : "Rumantsch"
    };
    return function (input) {
      var translation = translations[angular.lowercase(input)];
      return translation ? translation : input;
    };
  });


  module.controller('GnInspireController', [
    '$scope', 'inspireMetadataLoader', 'translateLangFilter', '$translate', '$http',
    function($scope, inspireMetadataLoader, translateLangFilter, $translate, $http) {
      $scope.metadataIcon = "";
      $scope.base = "../../catalog/";
      $scope.url = "";
      $scope.lang = location.href.split('/')[5].substring(0, 3) || 'eng';
      $scope.langs = {ger: 'ger', fre:'fre', ita:'ita', eng:'eng'};
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
      $scope.emptyContact = $scope.data.contact[0];

      $http.get($scope.url + "q@json?fast=index&from=1&to=1&sortBy=relevance&_id=" + mdId).success(function(data){
        var metadata, logoId;
        if (data.metadata) {
          if (data.metadata[0]) {
            metadata = data.metadata[0];
          } else {
            metadata = data.metadata;
          }
          if (metadata.groupLogoUuid) {
            logoId = metadata.groupLogoUuid;
          }
          if (!logoId && metadata.catalog && metadata.catalog.length == 2) {
            logoId = metadata.catalog[1];
          }

          if (!logoId) {
            logoId = metadata.source;
          }
        }

        if (logoId) {
          $scope.metadataIcon = "/geonetwork/images/logos/"+logoId+".gif";
        }
      }).error(function(err){
        if (waitDialog) {
          waitDialog.modal('hide');
        }
        alert(err);
      });

      $scope.translateLanguage = function(lang) {
        return function (lang) {
          return translateLangFilter(lang);
        };
      };
      var legalNames = ["dataset", "series", "service"];
      $scope.hierarchyLevelFilter = function () {
        return function(input) {
          var result = [];
          angular.forEach(input, function (val) {
            if (legalNames.indexOf(val.name) > -1) {
              result.push(val);
            }
          });
          return result;
        };
      };
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
      $scope.$watch("data.language", function(lang) {
        if ($scope.data.otherLanguages.indexOf(lang) < 0) {
          $scope.data.otherLanguages.push(lang);
        }
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
      $scope.refSysTitle = function() {
        return function (refSys) {
          if (refSys[$scope.lang]) {
            return refSys[$scope.lang];
          } else {
            for (var lang in refSys) {
              if (refSys.hasOwnProperty(lang)) {
                return refSys[lang];
              }

              return "";
            }
          }
        }
      };
      $scope.refSysValidationErrorClass = '';
      var addEmptyRefSys = function () {
        var j;
        var refSys = {
          code: {},
          options: [{}]
        };
        for (j = 0; j < $scope.data.refSysOptions.length; j++) {
          refSys.options.push({
            "eng": $scope.data.refSysOptions[j],
            "fre": $scope.data.refSysOptions[j],
            "ger": $scope.data.refSysOptions[j],
            "ita": $scope.data.refSysOptions[j],
            "roh": $scope.data.refSysOptions[j]
          });
        }
        $scope.data.refSys.push(refSys);
      };
      var removeEmptyRefSys = function () {
        var i;
        for (i = $scope.data.refSys.length - 1; i > -1; i--) {
          if ($scope.data.refSys[i].ref === undefined && ($scope.data.refSys[i].code === undefined ||
              $scope.data.refSys[i].code.eng === undefined)) {
            $scope.data.refSys.splice(i, 1);
            return;
          }
        }
      };
      $scope.$watch("data.refSys", function () {
          var refSys = $scope.data.refSys;
          var i, doAdd = $scope.data.refSysOptions !== undefined;
          var refSysClass = $scope.validationErrorClass;

          for (i = 0; i < refSys.length; i++) {
            if (refSys[i].code) {
              if (refSys[i].code[$scope.lang] && $scope.data.refSysOptions.indexOf(refSys[i].code[$scope.lang]) > -1) {
                refSysClass = '';
                removeEmptyRefSys();
                doAdd = false;
              }

              if (refSys[i].ref === undefined || !$scope.refSysTitle()(refSys[i].code)) {
                doAdd = false;
              }
            }
          }
          $scope.refSysValidationErrorClass = refSysClass;
          if (doAdd) {
            addEmptyRefSys();
          }
      }, true);
      $scope.$watch("data.refSysOptions", function () {
        var i,j;
        for (i = 0; i < $scope.data.refSys.length; i++ ){
          $scope.data.refSys[i].options = [$scope.data.refSys[i].code];
          for (j = 0; j < $scope.data.refSysOptions.length; j++) {
            $scope.data.refSys[i].options.push({
              "eng": $scope.data.refSysOptions[j],
              "fre": $scope.data.refSysOptions[j],
              "ger": $scope.data.refSysOptions[j],
              "ita": $scope.data.refSysOptions[j],
              "roh": $scope.data.refSysOptions[j]
            });
          }
        }
      });
      $scope.saveMetadata = function(editTab, finish) {
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
        delete dataClone.allConformanceReports;
        delete dataClone.couplingTypeOptions;
        delete dataClone.dcpListOptions;
        delete dataClone.refSysOptions;
        for (var i = 0; i < dataClone.refSys; i++) {
          delete dataClone.refSys[i].options;
        }

        var data = encodeURIComponent(JSON.stringify(dataClone));
        var finalData = 'id=' + mdId + '&data=' + data;
        if (editTab) {
          finalData = 'validate=false&'+finalData;
        }
        if (finish) {
          finalData = 'finish=false&'+finalData;
        }
        return $http({
          method: 'POST',
          url: $scope.url + "inspire.edit.save@json",
          data: finalData,
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
          if (typeof data[0] === 'string') {

            if (waitDialog) {
              waitDialog.modal('hide');
            }

            if (data[0] !== 'ok') {
              var dialogContentEl = $('#errorDialogContent');
              dialogContentEl.text('');
              dialogContentEl.append(data);
              $('#errorDialog').modal();
              return;
            }
          }

          if (editTab) {
            allowUnload = true;
            window.location.href = 'metadata.edit?id=' + mdId + '&currTab=' + editTab;
          } else {
            angular.copy(data[0], $scope.data);
            if (waitDialog) {
              waitDialog.modal('hide');
            }
          }
        }).error(function (data) {
          if (waitDialog) {
            waitDialog.modal('hide');
          }
          alert($translate("unexpectedSaveError")  + data);
        });
      };

      $scope.stopEditing = function() {
        allowUnload = true;
        window.location.href = 'metadata.show?id=' + mdId;
      };

      $scope.saveMetadataAndExit = function (){
        $scope.saveMetadata(undefined, true).success(function(){
          $scope.stopEditing();
        });
      };

  }]);


  module.controller('InspireKeywordController', [
    '$scope', 'inspireGetKeywordsFactory',
    function($scope, inspireGetKeywordsFactory) {

      var dataThesaurus = 'external.theme.inspire-theme';
      var serviceThesaurus = 'external.theme.inspire-service-taxonomy';

      $scope.editKeyword = function(keyword) {
        $scope.keywordUnderEdit = keyword;
        $scope.selectedKeyword = {};
        var modal = $('#editKeywordModal');
        modal.modal('show');
      };

      var isEmpty = function (keyword) {
        return keyword.thesaurus === '' && keyword.code === -1;
      };

      $scope.deleteKeyword = function(keyword) {
        var k, i, keywords = $scope.data.identification.descriptiveKeywords;
        keywords.splice(keywords.indexOf(keyword), 1);

        for (i = 0; i < keywords.length; i++) {
          k = keywords[i];
          if (k.thesaurus === dataThesaurus || k.thesaurus === serviceThesaurus || isEmpty(k)) {
            return;
          }
        }

        keywords.push({
          code: -1,
          thesaurus: '',
          words: {}
        });
      };

      $scope.keywords = {
        data: {},
        service: {}
      };

      inspireGetKeywordsFactory($scope.url, dataThesaurus).then (function (keywords) {
        $scope.keywords.data = keywords;
      });
      inspireGetKeywordsFactory($scope.url, serviceThesaurus).then (function (keywords) {
        $scope.keywords.service = keywords;
      });

      $scope.selectKeyword = function(keyword) {
        $scope.selectedKeyword = keyword;
      };

      $scope.linkToOtherKeyword = function() {
        var thesaurus = $scope.data.identification.type === 'data' ? dataThesaurus : serviceThesaurus;

        var keyword = $scope.selectedKeyword;
        $scope.keywordUnderEdit.code = keyword.code;
        $scope.keywordUnderEdit.words = keyword.words;
        $scope.keywordUnderEdit.thesaurus = thesaurus;
        var modal = $('#editKeywordModal');
        modal.modal('hide');
      };
      $scope.validationCls = '';
      $scope.validateKeywords = function(){
        var keyword, i, keywords, thesaurus, valid = false;
        keywords = $scope.data.identification.descriptiveKeywords;

        thesaurus = $scope.data.identification.type === 'data' ? dataThesaurus : serviceThesaurus;

        for (i = 0; i < keywords.length; i++) {
          keyword = keywords[i];

          if (keyword.code && keyword.code.length > 0 && keyword.thesaurus === thesaurus) {
            valid = true;
          }
        }

        $scope.validationCls = valid ? '' : $scope.validationErrorClass;
      };

      $scope.$watch('data.identification.descriptiveKeywords', $scope.validateKeywords, true);
    }]);


  module.controller('InspireFormatController', [
    '$scope', 'inspireGetFormatsFactory',
    function($scope, inspireGetFormatsFactory) {
      $scope.allFormats = {validated: [], nonValidated: []};

      inspireGetFormatsFactory.loadAll($scope.url).then(function(formats) {
        $scope.allFormats.validated = formats.validated;
        $scope.allFormats.nonValidated = formats.nonValidated;
      });

      $scope.editFormat = function(format) {
        $scope.formatUnderEdit = format;
        $scope.selectedFormat = {};
        var modal = $('#editFormatModal');
        modal.modal('show');
      };

      $scope.deleteFormat = function(format) {
        var k, i, formats = $scope.data.distributionFormats;
        if (formats.length > 1) {
          formats.splice(formats.indexOf(format), 1);
        } else {
          formats[0].id = "";
          formats[0].name = "";
          formats[0].version = "";
          formats[0].validated = false;
        }
      };

      $scope.selectFormat = function(format) {
        $scope.selectedFormat = format;
      };

      $scope.linkToOtherFormat = function() {
        var format = $scope.selectedFormat;
        angular.copy(format, $scope.formatUnderEdit);

        var modal = $('#editFormatModal');
        modal.modal('hide');
      };

      $scope.validationCls = '';
      $scope.validateFormats = function(){
        var format, i, formats, thesaurus, valid = false;
        formats = $scope.data.distributionFormats;

        for (i = 0; i < formats.length; i++) {
          format = formats[i];

          if (format.id) {
            valid = true;
          }
        }

        $scope.validationCls = valid ? '' : $scope.validationErrorClass;
      };

      $scope.$watch('data.distributionFormats', $scope.validateFormats, true);
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
        var legal, i, count = 0;
        var allConstraints = [$scope.data.constraints.legal, $scope.data.constraints.generic, $scope.data.constraints.security];

        angular.forEach(allConstraints, function (constraints) {
          for (i = 0; i < constraints.length; i++) {
            legal = constraints[i];
            if (legal[propertyName]) {
              count += legal[propertyName].length;
            }
          }

        });
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

      var hasNonEmptyOtherConstraintInConstraint = function(legalConstraint) {
        var x;
        for (x = 0; x < legalConstraint.otherConstraints.length; x++) {
          if (legalConstraint.otherConstraints[x][$scope.data.language] !== '') {
            return true;
          }
        }
        return false;
      };
      $scope.hasNonEmptyOtherConstraint = function() {
        var x;
        for (x = 0; x < $scope.data.constraints.legal.length; x++) {
          if (hasNonEmptyOtherConstraintInConstraint($scope.data.constraints.legal[x])) {
            return true;
          }
        }
        return false;
      };

      $scope.validityClass = {
          otherConstraints: ''
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
      $scope.$watchCollection('data.constraints.generic', function() {
        $scope.propertyCount.updateAccessConstraints();
        $scope.propertyCount.updateUseConstraints();
        $scope.propertyCount.updateUseLimitations();
      });
      $scope.$watchCollection('data.constraints.security', function() {
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
        var otherConstraint;
        var hasOtherRestrictions = $scope.hasOtherRestrictions($scope.legal);
        if ($scope.legal.otherConstraints.length === 0) {
          otherConstraint = {};
          otherConstraint[$scope.data.language] = '';
          $scope.legal.otherConstraints.push(otherConstraint);
        }
        $scope.validityClass.otherConstraints = !hasOtherRestrictions || $scope.hasNonEmptyOtherConstraint() ? '' : $scope.validationErrorClass;
      });
  }]);
  module.controller('InspireUseConstraintController', [
    '$scope', function($scope) {

      $scope.$watch('useConstraint', function() {
        var otherConstraint;
        if ($scope.legal.otherConstraints.length === 0) {
          otherConstraint = {};
          otherConstraint[$scope.data.language] = '';
          $scope.legal.otherConstraints.push(otherConstraint);
        }
      });
  }]);
  module.controller('InspireOtherConstraintController', [
    '$scope', function($scope) {

      $scope.$watch('other', function() {
        var hasOtherRestrictions = $scope.hasOtherRestrictions($scope.legal);
        $scope.validityClass.otherConstraints = !hasOtherRestrictions || $scope.hasNonEmptyOtherConstraint() ? '' : $scope.validationErrorClass;
      }, true);
      $scope.deleteOtherConstraint = function (legal, constraint) {
        $scope.deleteFromArray(legal.otherConstraints, constraint);
      };
  }]);

  module.controller('InspireConformityController', [
    '$scope', '$translate', function($scope, $translate) {

      $scope.validationClassObject = function(model) {
        var elem, i;
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
      $scope.updateSelectedConformity = function() {
        return function (report) {
          var i = null;
          for (i in report) {
            if (report.hasOwnProperty(i)) {
              $scope.data.conformity[i] = angular.copy(report[i]);
            }
          }
        };
      };
      $scope.updateConformanceResultTitle = function(mainLang) {
        return function (desc) {
          if (desc.title[mainLang]) {
            return desc.title[mainLang];
          }
          if (desc.title[$scope.data.language]) {
            return desc.title[$scope.data.language];
          }
          if (desc.title.length > 0) {
            return desc.title[0];
          }
          return desc.conformanceResultRef;
        };
      };
  }]);

  module.controller('InspireLinkController', [
    '$scope', '$http', function($scope, $http) {

      $scope.$watch('link', function(newVal) {
        var lang, url;
        var errorHandler = function (msg,errorCode) {
          if (errorCode === 404) {
            $scope.isValidURL = false;
          }
        };
        $scope.isValidURL = undefined;

        for (lang in newVal.localizedURL) {
          if (newVal.localizedURL.hasOwnProperty(lang) &&
              $scope.data.otherLanguages.indexOf(lang) > -1) {
            if ($scope.isValidURL === undefined) {
              $scope.isValidURL = true;
            }
            url = newVal.localizedURL[lang];
            if (!/\S+/.test(url)) {
              if (lang === $scope.data.language) {
                $scope.isValidURL = false;
              }
            } else {
              $http.head("../../proxy?url=" + encodeURIComponent(url)).error(errorHandler);
            }
          } else {
            delete newVal.localizedURL[lang];
          }
        }
      }, true);
      $scope.deleteLink = function(link) {
        delete link.localizedURL;
      };
  }]);

  module.filter('hideNonCheTopicCategories', function () {
    var acceptable = {
      environment: true,
      geoscientificInformation: true,
      planningCadastre: true,
      imageryBaseMapsEarthCover: true,
      utilitiesCommunication: true
    };
    return function (input) {
      var i, filtered = [];
      for(i = 0; i < input.length; i++) {
        if (!acceptable.hasOwnProperty(input[i])) {
          filtered.push(i);
        }
      }
      return filtered;
    };
  });

}());

