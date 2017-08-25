/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_thesaurus_controller');

  var module = angular.module('gn_thesaurus_controller',
      ['blueimp.fileupload']);


  /**
   * GnThesaurusController provides managment of thesaurus
   * by adding new local thesaurus, uploading from SKOS file or by URL.
   *
   * Keyword can be searched and added to local thesaurus.
   *
   * Limitations:
   *  * Only keyword in the GUI language will be displayed
   * and edited. TODO
   *  * Keyword relation can't be modified.
   *  * Thesaurus properties once created can't be modified
   *
   */
  module.controller('GnThesaurusController', [
    '$scope',
    '$http',
    '$rootScope',
    '$translate',
    'gnConfig',
    'gnSearchManagerService',
    'gnUtilityService',
    'gnGlobalSettings',
    function($scope,
        $http,
        $rootScope,
        $translate,
        gnConfig,
        gnSearchManagerService,
        gnUtilityService,
        gnGlobalSettings) {

      $scope.gnConfig = gnConfig;
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

      /**
       * Type of relations in SKOS thesaurus
       */
      var relationTypes = ['broader', 'narrower', 'related'];

      /**
       * The list of thesaurus
       */
      $scope.thesaurus = [];
      /**
       * The currently selected thesaurus
       */
      $scope.thesaurusSelected = null;
      /**
       * Is the selected thesaurus activated or not.
       */
      $scope.thesaurusSelectedActivated = false;
      /**
       * A suggested namespace for new thesaurus based
       * on other thesaurus properties
       */
      $scope.thesaurusSuggestedNs = '';
      /**
       * The thesaurus URL to upload
       */
      $scope.thesaurusUrl = '';


      /**
       * The current list of keywords
       */
      $scope.keywords = {};
      /**
       * The selected keyword
       */
      $scope.keywordSelected = null;
      /**
       * The list of relation for the selected keyword
       */
      $scope.keywordSelectedRelation = {};
      /**
       * A suggestion for the new keyword identifier
       * based on keyword properties
       */
      $scope.keywordSuggestedUri = '';
      /**
       * The current keyword filter
       */
      $scope.keywordFilter = '';

      $scope.maxNumberOfKeywords = 50;
      $scope.availableResultCounts = [50, 100, 200, 500, 1000];
      $scope.setResultsCount = function(count) {
        $scope.maxNumberOfKeywords = count;
      };

      $scope.recordsRelatedToThesaurus = 0;

      /**
       * Language switch for keyword list
       */
      $scope.currentLangShown = $scope.lang;
      $scope.availableLangs = gnConfig['ui.config'].mods.header.languages;
      $scope.switchLang = function(lang3) {
        $scope.currentLangShown = lang3;
        $scope.currentKeywordEditor = lang3;
      };

      /**
       * Language switch for keyword editor
       */
      $scope.currentKeywordEditorLang = $scope.lang;
      $scope.switchKeywordEditorLang = function(lang3) {
        $scope.currentKeywordEditorLang = lang3;
      };

      /**
       * The type of thesaurus import. Could be new, file or url.
       */
      $scope.importAs = null;

      var defaultMaxNumberOfKeywords = 50,
          creatingThesaurus = false, // Keyword creation in progress ?
          creatingKeyword = false, // Thesaurus creation in progress ?
          selectedKeywordOldId = null; // Keyword id before starting editing

      $scope.searching = false;

      /**
       * Select a thesaurus and search its keywords.
       */
      $scope.selectThesaurus = function(t) {
        creatingThesaurus = false;
        $scope.thesaurusSelected = t;
        $scope.thesaurusSelectedActivated = t.activated == 'y';

        $('#keywordFilter').focus();
        searchThesaurusKeyword();
      };


      /**
       * Search thesaurus keyword based on filter and max number
       */
      searchThesaurusKeyword = function() {
        $scope.searching = true;
        if ($scope.thesaurusSelected) {
          // list of ui languages; we want the keyword info in all these
          // put the current lang first to be used for sorting
          var langsList = [$scope.currentLangShown];
          Object.keys($scope.availableLangs).forEach(function(lang3) {
            if (langsList.indexOf(lang3) === -1) {
              langsList.push(lang3);
            }
          });

          $scope.recordsRelatedToThesaurus = 0;
          $http.get('../api/registries/vocabularies/search?type=CONTAINS' +
              '&thesaurus=' + $scope.thesaurusSelected.key +
              '&uri=*' +
              encodeURIComponent($scope.keywordFilter) + '*' +
              '&rows=' +
              ($scope.maxNumberOfKeywords ||
                      defaultMaxNumberOfKeywords) +
              '&q=' + (encodeURI($scope.keywordFilter) || '*') +
              '&pLang=' + langsList.join(',')
          ).success(function(data) {
            $scope.keywords = data;
            gnSearchManagerService.gnSearch({
              summaryOnly: 'true',
              thesaurusIdentifier: $scope.thesaurusSelected.key}).
                then(function(results) {
                  $scope.recordsRelatedToThesaurus = parseInt(results.count);
                });
          }).finally(function() {
            $scope.searching = false;
          });
        }
      };

      /**
       * Add a new local thesaurus and open the modal
       */
      $scope.addThesaurus = function(type) {
        creatingThesaurus = true;

        $scope.thesaurusImportType = 'theme';
        $scope.importAs = type;
        $scope.thesaurusSelected = {
          title: '',
          filename: '',
          defaultNamespace: 'http://www.mysite.org/thesaurus',
          dname: 'theme',
          type: 'local'
        };

        $('#thesaurusModal').modal();
        $('#thesaurusModal').on('shown.bs.modal', function() {
          var id = $scope.importAs === 'new' ? '#gn-thesaurus-title' :
              ($scope.importAs === 'file' ? '#gn-thesaurus-file' :
                  '#gn-thesaurus-url');
          $(id).focus();
        });
      };

      /**
       * Build a namespace based on page location, thesaurus type
       * and filename. eg. http://localhost:8080/thesaurus/theme/commune
       */
      $scope.computeThesaurusNs = function() {
        $scope.thesaurusSuggestedNs =
            location.origin +
            '/thesaurus/' + $scope.thesaurusSelected.dname + '/' +
            $scope.thesaurusSelected.filename.replace(/[^\d\w]/gi, '');
      };

      /**
       * Use the suggested namespace for the new thesaurus
       */
      $scope.useSuggestedNs = function() {
        $scope.thesaurusSelected.defaultNamespace = $scope.thesaurusSuggestedNs;
      };

      /**
       * Create the thesaurus in the catalog, close the modal on success
       * and refresh the list.
       */
      $scope.createThesaurus = function() {
        var xml = '<request>' +
            '<tname>' + $scope.thesaurusSelected.title + '</tname>' +
            '<fname>' + $scope.thesaurusSelected.filename + '</fname>' +
            '<tns>' + $scope.thesaurusSelected.defaultNamespace + '</tns>' +
            '<dname>' + $scope.thesaurusSelected.dname + '</dname>' +
            '<type>local</type></request>';
        $http.post('thesaurus.update', xml, {
          headers: {'Content-type': 'application/xml'}
        })
            .success(function(data) {
              $scope.thesaurusSelected = null;
              $('#thesaurusModal').modal('hide');
              loadThesaurus();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('thesaurusCreationError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Thesaurus uploaded with success, close the modal,
       * refresh the list.
       */
      uploadThesaurusDone = function(data) {
        $scope.clear($scope.queue);
        $scope.thesaurusUrl = '';

        $('#thesaurusModal').modal('hide');

        loadThesaurus();
      };

      /**
       * Thesaurus uploaded with error, broadcast it.
       */
      uploadThesaurusError = function(e, data) {
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate.instant('thesaurusUploadError'),
          error: data.jqXHR.responseJSON,
          timeout: 0,
          type: 'danger'});
      };

      /**
       * Thesaurus upload options.
       */
      $scope.thesaurusUploadOptions = {
        autoUpload: false,
        //        TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
        done: uploadThesaurusDone,
        fail: uploadThesaurusError
      };

      /**
       * Upload the file or URL in the catalog thesaurus repository
       */
      $scope.importThesaurus = function(formId) {
        // Uploading
        $(formId)[0].enctype = ($scope.importAs === 'file' ?
            'multipart/form-data' : '');
        if ($scope.importAs === 'file') {
          $scope.submit();
        } else {
          $http.get('thesaurus.upload?' + $(formId).serialize())
              .success(uploadThesaurusDone)
              .error(function(data) {
                uploadThesaurusError(null, data);
              });
        }
      };

      /**
       * Ask for confirmation to delete the thesaurus
       */
      $scope.deleteThesaurus = function(e) {
        $scope.delEntryId = $scope.thesaurusSelected.key;
        $('#gn-confirm-delete').modal('show');
      };

      /**
       * Remove a thesaurus from the catalog thesaurus repository
       * (this is done after a confirm dialog)
       */
      $scope.confirmDeleteThesaurus = function() {
        $http.get('thesaurus.remove?ref=' +
                  $scope.delEntryId)
            .success(function(data) {
              $scope.thesaurusSelected = null;
              $scope.delEntryId = null;
              loadThesaurus();
            })
            .error(function(data) {
              $scope.delEntryId = null;
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('thesaurusDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Activate a thesaurus in order to be able to
       * use it in the metadata editor.
       */
      $scope.enableThesaurus = function() {
        $http.get('thesaurus.enable?_content_type=json' +
                '&ref=' + $scope.thesaurusSelected.key +
                '&activated=' +
                    ($scope.thesaurusSelected.activated == 'y' ? 'n' : 'y')
        ).success(function(data) {
          $scope.thesaurusSelected.activated = data.activated;
        });
      };

      $scope.reindexRecords = function() {
        gnSearchManagerService.indexSetOfRecords({
          thesaurusIdentifier: $scope.thesaurusSelected.key}).
            then(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title:
                    $translate.instant('indexingRecordsRelatedToTheThesaurus'),
                timeout: 2
              });
            });
      };

      /**
       * Thesaurus creation on going
       */
      $scope.isNew = function() {
        return creatingThesaurus;
      };
      /**
       * Keyword creation on going
       */
      $scope.isNewKeyword = function() {
        return creatingKeyword;
      };

      /**
       * Search relation for each types for the current keyword
       */
      searchRelation = function(k) {
        $scope.keywordSelectedRelation = {};
        $.each(relationTypes, function(index, value) {
          $http.get('thesaurus.keyword.links?_content_type=json&' +
              'request=' + value +
                      '&thesaurus=' + $scope.thesaurusSelected.key +
                      '&id=' + encodeURIComponent(k.uri))
              .success(function(data) {
                $scope.keywordSelectedRelation[value] = data.descKeys;
              });
        });
      };

      /**
       * Edit an existing keyword, open the modal, search relations
       */
      $scope.editKeyword = function(k) {
        // reset keyword editor lang to list lanugage
        $scope.currentKeywordEditorLang = $scope.currentLangShown;

        $scope.keywordSelected = angular.copy(k);
        $scope.keywordSelected.oldId = $scope.keywordSelected.uri;
        creatingKeyword = false;
        $('#keywordModal').modal();
        searchRelation($scope.keywordSelected);
      };

      /**
       * Edit a new keyword and open the modal
       */
      $scope.addKeyword = function() {
        // reset keyword editor lang to list lanugage
        $scope.currentKeywordEditorLang = $scope.currentLangShown;

        creatingKeyword = true;
        $scope.keywordSuggestedUri = '';
        $scope.keywordSelected = {
          'uri': $scope.thesaurusSelected.defaultNamespace +
              ($scope.thesaurusSelected.defaultNamespace.indexOf('#') === -1 ?
              '#' : '') +
              gnUtilityService.randomUuid(),
          'value': '',
          'values': {},
          'definition': '',
          'definitions': { },
          'defaultLang': $scope.lang
        };
        if ($scope.isPlaceType()) {
          $scope.keywordSelected.geo = {
            west: '',
            south: '',
            east: '',
            north: ''
          };
        }
        $('#keywordModal').modal();
      };

      /**
       * Build keyword POST body message
       */
      buildKeyword = function(keywordObject) {
        var geoxml = $scope.isPlaceType() ?
            '<west>' + keywordObject.geo.west + '</west>' +
            '<south>' + keywordObject.geo.south + '</south>' +
            '<east>' + keywordObject.geo.east + '</east>' +
            '<north>' + keywordObject.geo.north + '</north>' : '';

        // build localized values xml
        var localizedValues = '';
        Object.keys(keywordObject.values).forEach(function(lang3) {
          localizedValues +=
              '<loc_' + lang3 + '_label>' +
              keywordObject.values[lang3] +
              '</loc_' + lang3 + '_label>';
        });

        // build localized definitions xml
        var localizedDefinitions = '';
        Object.keys(keywordObject.definitions).forEach(function(lang3) {
          localizedDefinitions +=
              '<loc_' + lang3 + '_definition>' +
              keywordObject.definitions[lang3] +
              '</loc_' + lang3 + '_definition>';
        });

        var xml = '<request><newid>' + keywordObject.uri + '</newid>' +
            '<refType>' + $scope.thesaurusSelected.dname + '</refType>' +
            '<namespace>' + $scope.thesaurusSelected.defaultNamespace +
                '</namespace>' +
            '<ref>' + $scope.thesaurusSelected.key + '</ref>' +
            '<oldid>' +
            (keywordObject.oldId || keywordObject.uri) + '</oldid>' +
            localizedValues +
            localizedDefinitions +
            '</request>';

        return xml;
      };

      /**
       * Create the keyword in the thesaurus
       */
      $scope.createKeyword = function() {
        $http.post(
            'thesaurus.keyword.add?_content_type=json',
            buildKeyword($scope.keywordSelected),
            { headers: {'Content-type': 'application/xml'} }
        )
            .success(function(data) {
              var response = data[0];
              if (response && response['@message']) {
                var statusConfig = {
                  title: $translate.instant('keywordCreationError'),
                  msg: response['@message'],
                  timeout: 0,
                  type: 'danger'
                };
                $rootScope.$broadcast('StatusUpdated', statusConfig);
              } else {
                $scope.keywordSelected = null;
                $('#keywordModal').modal('hide');
                searchThesaurusKeyword();
                creatingKeyword = false;
              }
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('keywordCreationError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Update the keyword in the thesaurus
       */
      $scope.updateKeyword = function() {
        $http.post('thesaurus.keyword.update',
            buildKeyword($scope.keywordSelected),
            { headers: {'Content-type': 'application/xml'} }
        )
            .success(function(data) {
              $scope.keywordSelected = null;
              $('#keywordModal').modal('hide');
              searchThesaurusKeyword();
              selectedKeywordOldId = null;
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('keywordUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
     * Remove a keyword
     */
      $scope.deleteKeyword = function(k) {
        $scope.keywordToDelete = k;
        $('#gn-confirm-delete-keyword').modal('show');
      };
      $scope.confirmDeleteKeyword = function() {
        var k = $scope.keywordToDelete;
        $http.get('thesaurus.keyword.remove?pThesaurus=' + k.thesaurusKey +
            '&id=' + encodeURIComponent(k.uri))
            .success(function(data) {
              searchThesaurusKeyword();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('keywordDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            })
            .finally(function() {
              $scope.keywordToDelete = null;
            });
      };

      /**
       * Build a keyword identifier based on thesaurus
       * namespace and keyword label
       */
      $scope.computeKeywordId = function() {
        var defaultLabel = $scope.keywordSelected.values[
            $scope.keywordSelected.defaultLang] || '';
        $scope.keywordSuggestedUri =
            $scope.thesaurusSelected.defaultNamespace +
            ($scope.thesaurusSelected.defaultNamespace.indexOf('#') === -1 ?
            '#' : '') +
            defaultLabel.replace(/[^\d\w]/gi, '');
      };

      /**
       * Use the suggestion for the keyword uri
       */
      $scope.useSuggestedUri = function() {
        $scope.keywordSelected.uri = $scope.keywordSuggestedUri;
      };

      /**
       * Is the thesaurus a place type thesaurus
       */
      $scope.isPlaceType = function() {
        if ($scope.thesaurusSelected) {
          return $scope.thesaurusSelected.key &&
              $scope.thesaurusSelected.key.indexOf('.place.') !== -1;
        } else {
          return false;
        }
      };

      /**
       * Is the thesaurus external (ie. readonly keywords).
       */
      $scope.isExternal = function() {
        if ($scope.thesaurusSelected) {
          return $scope.thesaurusSelected.type === 'external';
        } else {
          return false;
        }
      };

      /**
       * When updating keyword search params: refresh list
       */
      $scope.$watch(function() {
        return $scope.maxNumberOfKeywords + '##' +
            $scope.keywordFilter + '##' +
            $scope.currentLangShown;
      }, function() {
        searchThesaurusKeyword();
      });

      /**
       * Load the list of thesaurus from the server
       */
      function loadThesaurus() {
        $http.get('thesaurus@json').success(function(data) {
          $scope.thesaurus = data[0];
        }).error(function(data) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate.instant('thesaurusListError'),
            error: data,
            timeout: 0,
            type: 'danger'});
        });
      }

      loadThesaurus();

    }]);

})();
