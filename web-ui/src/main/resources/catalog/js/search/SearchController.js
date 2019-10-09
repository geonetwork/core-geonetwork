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


  goog.provide('gn_search_controller');


  goog.require('gn_catalog_service');
  goog.require('gn_searchsuggestion_service');
  goog.require('gn_static_pages');
  goog.require('gn_usersearches');

  var module = angular.module('gn_search_controller', [
    'ui.bootstrap.typeahead',
    'gn_searchsuggestion_service',
    'gn_catalog_service',
    'gn_static_pages',
    'gn_usersearches'
  ]);

  /**
   * Main search controller attached to the first element of the
   * included html file from the base-layout.xsl output.
   */
  module.controller('GnSearchController', [
    '$scope',
    '$q',
    '$http',
    'suggestService',
    'gnAlertService',
    'gnSearchSettings',
    'gnGlobalSettings',
    'gnConfig',
    'orderByFilter',
    function($scope, $q, $http, suggestService, gnAlertService,
             gnSearchSettings, gnGlobalSettings, gnConfig, orderByFilter) {

      /** Object to be shared through directives and controllers */
      $scope.searchObj = {
        params: {},
        permalink: true,
        sortbyValues: gnSearchSettings.sortbyValues,
        sortbyDefault: gnSearchSettings.sortbyDefault,
        hitsperpageValues: gnSearchSettings.hitsperpageValues
      };

      $scope.isUserFeedbackEnabled = false;

      statusSystemRating = gnConfig[gnConfig.key.isRatingUserFeedbackEnabled];
      if (statusSystemRating == 'advanced') {
        $scope.isUserFeedbackEnabled = true;
      }

      $scope.isUserSearchesEnabled = gnGlobalSettings.gnCfg.mods.search.usersearches.enabled;
      $scope.displayFeaturedSearchesPanel =
        gnGlobalSettings.gnCfg.mods.search.usersearches.displayFeaturedSearchesPanel;

      $scope.ise  = false;

      /** Facets configuration */
      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType;

      /* Pagination configuration */
      $scope.paginationInfo = gnSearchSettings.paginationInfo;

      /* Default result view template */
      $scope.resultTemplate = gnSearchSettings.resultTemplate ||
          gnSearchSettings.resultViewTpls[0].tplUrl;
      /* Default advanced search form template */
      $scope.advancedSearchTemplate = gnSearchSettings.advancedSearchTemplate ||
        '../../catalog/views/default/templates/advancedSearchForm/defaultAdvancedSearchForm.html';

      $scope.getAnySuggestions = function(val) {
        return suggestService.getAnySuggestions(val);
      };

      $scope.keywordsOptions = {
        mode: 'remote',
        remote: {
          url: suggestService.getUrl('QUERY', 'keyword', 'STARTSWITHFIRST'),
          filter: suggestService.bhFilter,
          wildcard: 'QUERY'
        }
      };

      $scope.orgNameOptions = {
        mode: 'remote',
        remote: {
          url: suggestService.getUrl('QUERY', 'orgName', 'STARTSWITHFIRST'),
          filter: suggestService.bhFilter,
          wildcard: 'QUERY'
        }
      };

      $scope.categoriesOptions = {
        mode: 'prefetch',
        promise: (function() {
          var defer = $q.defer();
          $http.get('../api/tags', {cache: true}).
              success(function(data) {
                var res = [];
                for (var i = 0; i < data.length; i++) {
                  res.push({
                    id: data[i].name,
                    name: data[i].label.eng
                  });
                }
                res = orderByFilter(res,'name',false);
                defer.resolve(res);
              });
          return defer.promise;
        })()
      };

      $scope.sourcesOptions = {
        mode: 'prefetch',
        promise: (function() {
          var defer = $q.defer();
          $http.get('../api/sources', {cache: true}).
              success(function(a) {
                var res = [];
                for (var i = 0; i < a.length; i++) {
                  res.push({
                    id: a[i].id,
                    name: a[i].name
                  });
                }
                defer.resolve(res);
              });
          return defer.promise;
        })()
      };

      /**
       * Keep a reference on main cat scope
       * @return {*}
       */
      $scope.getCatScope = function() {return $scope};
    }]);
})();
