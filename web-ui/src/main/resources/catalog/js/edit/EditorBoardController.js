(function() {
  goog.provide('gn_editorboard_controller');



  goog.require('gn_mdactions_service');
  goog.require('gn_search');
  goog.require('gn_search_form_controller');

  var module = angular.module('gn_editorboard_controller',
      ['gn_search', 'gn_search_form_controller', 'gn_mdactions_service']);


  module.controller('GnEditorBoardSearchController', [
    '$scope',
    '$location',
    '$rootScope',
    '$translate',
    '$q',
    'gnSearchSettings',
    'gnMetadataActions',
    function($scope, $location, $rootScope, $translate, $q,
        gnSearchSettings, gnMetadataActions) {
      $scope.onlyMyRecord = false;

      var defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        params: {
          sortBy: 'changeDate',
          // TODO manage subtemplate from this UI
          //_isTemplate: 'y or n or s',
          _isTemplate: 'y or n',
          resultType: $scope.facetsSummaryType,
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, defaultSearchObj);

      $scope.toggleOnlyMyRecord = function() {
        $scope.onlyMyRecord = !$scope.onlyMyRecord;
      };

      var setOwner = function() {
        $scope.searchObj.params['_owner'] = $scope.user.id;
      };
      var unsetOwner = function() {
        delete $scope.searchObj.params['_owner'];
      };
      $scope.$watch('onlyMyRecord', function(value) {
        if (!$scope.searchObj) {
          return;
        }

        value ? setOwner() : unsetOwner();
      });

      $scope.deleteRecord = function(md) {
        var deferred = $q.defer();

        gnMetadataActions.deleteMd(md, $scope.searchObj.params).
            then(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('metadataRemoved',
                    {title: md.title || md.defaultTitle}),
                timeout: 2
              });
              deferred.resolve(data);
            }, function(reason) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate(reason.data.error.message),
                timeout: 0,
                type: 'danger'
              });
              deferred.reject(reason);
            });

        return deferred.promise;
      };
    }
  ]);
  module.controller('GnEditorBoardController', [
    '$scope',
    '$location',
    'gnSearchSettings',
    function($scope, $location, gnSearchSettings) {

      gnSearchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/editor.html',
        tooltip: 'List',
        icon: 'fa-list'
      }];

      gnSearchSettings.resultTemplate =
          gnSearchSettings.resultViewTpls[0].tplUrl;

      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType = 'manager';

      gnSearchSettings.sortbyValues = [{
        sortBy: 'relevance',
        sortOrder: ''
      }, {
        sortBy: 'changeDate',
        sortOrder: ''
      }, {
        sortBy: 'title',
        sortOrder: 'reverse'
      }];

      gnSearchSettings.hitsperpageValues = [20, 50, 100];

      gnSearchSettings.paginationInfo = {
        hitsPerPage: gnSearchSettings.hitsperpageValues[0]
      };

    }
  ]);
})();
