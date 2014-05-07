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
    '$scope', '$http', '$rootScope', '$translate',
    'gnConfig', 'gnSearchManagerService',
    function($scope, $http, $rootScope, $translate,
             gnConfig, gnSearchManagerService) {

      $scope.gnConfig = gnConfig;
      /**
       * Type of relations in SKOS thesaurus
       */
      var relationTypes = ['broader', 'narrower', 'related'];

      /**
       * The list of thesaurus
       */
      $scope.thesaurus = {};
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

      $scope.recordsRelatedToThesaurus = 0;
      /**
       * The type of thesaurus import. Could be new, file or url.
       */
      $scope.importAs = null;

      var defaultMaxNumberOfKeywords = 50,
          creatingThesaurus = false, // Keyword creation in progress ?
          creatingKeyword = false, // Thesaurus creation in progress ?
          selectedKeywordOldId = null; // Keyword id before starting editing

      /**
       * Select a thesaurus and search its keywords.
       */
      $scope.selectThesaurus = function(t) {
        creatingThesaurus = false;
        $scope.thesaurusSelected = t;
        $scope.thesaurusSelectedActivated = t.activated == 'y';
        searchThesaurusKeyword();
      };


      /**
       * Search thesaurus keyword based on filter and max number
       */
      searchThesaurusKeyword = function() {
        if ($scope.thesaurusSelected) {
          $scope.recordsRelatedToThesaurus = 0;
          $http.get('keywords@json?pNewSearch=true&pTypeSearch=1' +
              '&pThesauri=' + $scope.thesaurusSelected.key +
                      '&pMode=searchBox' +
                      '&maxResults=' +
                      ($scope.maxNumberOfKeywords ||
                              defaultMaxNumberOfKeywords) +
                      '&pKeyword=' + (encodeURI($scope.keywordFilter) || '*')
          ).success(function(data) {
            $scope.keywords = data[0];
            gnSearchManagerService.gnSearch({
              summaryOnly: 'true',
              thesaurusIdentifier: $scope.thesaurusSelected.key}).
                then(function(results) {
                  $scope.recordsRelatedToThesaurus = parseInt(results.count);
                });
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
                title: $translate('thesaurusCreationError'),
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
          title: $translate('thesaurusUploadError'),
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
       * Remove a thesaurus from the catalog thesaurus repository
       */
      $scope.deleteThesaurus = function() {
        $http.get('thesaurus.remove?ref=' +
                  $scope.thesaurusSelected.key)
          .success(function(data) {
              $scope.thesaurusSelected = null;
              loadThesaurus();
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('thesaurusDeleteError'),
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
        $http.get('thesaurus.activate@json?' +
                'ref=' + $scope.thesaurusSelected.key +
                '&activated=' +
                    ($scope.thesaurusSelectedActivated ? 'n' : 'y')
        ).success(function(data) {
          // TODO
        });
      };

      $scope.reindexRecords = function() {
        gnSearchManagerService.indexSetOfRecords({
          thesaurusIdentifier: $scope.thesaurusSelected.key}).
            then(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('indexingRecordsRelatedToTheThesaurus'),
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
          $http.get('thesaurus.keyword.links@json?' +
              'request=' + value +
                      '&thesaurus=' + $scope.thesaurusSelected.key +
                      '&id=' + encodeURIComponent(k.uri))
              .success(function(data) {
                $scope.keywordSelectedRelation[value] = data.descKeys;
              })
              .error();
        });
      };

      /**
       * Edit an existing keyword, open the modal, search relations
       */
      $scope.editKeyword = function(k) {
        $scope.keywordSelected = k;
        selectedKeywordOldId = k.uri;
        creatingKeyword = false;
        $('#keywordModal').modal();
        searchRelation(k);
      };

      /**
       * Edit a new keyword and open the modal
       */
      $scope.addKeyword = function(k) {
        creatingKeyword = true;
        $scope.keywordSuggestedUri = '';
        $scope.keywordSelected = {
          'uri': $scope.thesaurusSelected.defaultNamespace + '#',
          'value': {'@language': $scope.lang, '#text': ''},
          'definition': {'@language': $scope.lang},
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
      buildKeyword = function() {
        var geoxml = $scope.isPlaceType() ?
            '<west>' + $scope.keywordSelected.geo.west + '</west>' +
            '<south>' + $scope.keywordSelected.geo.south + '</south>' +
            '<east>' + $scope.keywordSelected.geo.east + '</east>' +
            '<north>' + $scope.keywordSelected.geo.north + '</north>' : '';

        var xml = '<request><newid>' + $scope.keywordSelected.uri + '</newid>' +
            '<refType>' + $scope.thesaurusSelected.dname + '</refType>' +
            '<definition>' + $scope.keywordSelected.definition['#text'] +
                '</definition>' +
            '<namespace>' + $scope.thesaurusSelected.defaultNamespace +
                '</namespace>' +
            '<ref>' + $scope.thesaurusSelected.key + '</ref>' +
            '<oldid>' + selectedKeywordOldId + '</oldid>' +
            '<lang>' + $scope.lang + '</lang>' +
            '<label>' + $scope.keywordSelected.value['#text'] + '</label>' +
            geoxml +
            '</request>';

        return xml;
      };

      /**
       * Create the keyword in the thesaurus
       */
      $scope.createKeyword = function() {
        $http.post('thesaurus.keyword.add', buildKeyword(), {
          headers: {'Content-type': 'application/xml'}
        })
          .success(function(data) {
              $scope.keywordSelected = null;
              $('#keywordModal').modal('hide');
              searchThesaurusKeyword();
              creatingKeyword = false;
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('keywordCreationError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Update the keyword in the thesaurus
       */
      $scope.updateKeyword = function() {
        $http.post('thesaurus.keyword.update', buildKeyword(), {
          headers: {'Content-type': 'application/xml'}
        })
          .success(function(data) {
              $scope.keywordSelected = null;
              $('#keywordModal').modal('hide');
              searchThesaurusKeyword();
              selectedKeywordOldId = null;
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('keywordUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
     * Remove a keyword
     */
      $scope.deleteKeyword = function(k) {
        $scope.keywordSelected = k;
        $http.get('thesaurus.keyword.remove?pThesaurus=' + k.thesaurus.key +
                  '&id=' + encodeURIComponent(k.uri))
          .success(function(data) {
              searchThesaurusKeyword();
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('keywordDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Build a keyword identifier based on thesaurus
       * namespace and keyword label
       */
      $scope.computeKeywordId = function() {
        $scope.keywordSuggestedUri =
            $scope.thesaurusSelected.defaultNamespace +
            '#' +
            $scope.keywordSelected.value['#text'].replace(/[^\d\w]/gi, '');
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
       * When updating number of keywords, refresh keyword list
       */
      $scope.$watch('maxNumberOfKeywords', function() {
        searchThesaurusKeyword();
      });


      /**
       * When updating the keyword filter, refresh keyword list
       */
      $scope.$watch('keywordFilter', function() {
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
            title: $translate('thesaurusListError'),
            error: data,
            timeout: 0,
            type: 'danger'});
        });
      }

      loadThesaurus();

    }]);

})();
