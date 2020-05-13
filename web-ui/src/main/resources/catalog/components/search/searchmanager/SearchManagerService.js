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
  goog.provide('gn_search_manager_service');

  var module = angular.module('gn_search_manager_service', []);

  module.factory('gnSearchManagerService', [
    'gnUtilityService',
    '$q',
    '$rootScope',
    '$http',
    'gnHttp',
    function(gnUtilityService, $q, $rootScope,
             $http, gnHttp) {
      /**
       * Utility to format a search response. JSON response
       * when containing one element will not make an array.
       * Tidy the JSON to be always the same if one or more
       * elements.
       */
      var format = function(data) {
        // Retrieve facet and add name as property and remove @count
        var facets = {}, dimension = [], results = -1,
            listOfArrayFields = ['image', 'link',
              'format', 'keyword', 'otherConstr',
              'Constraints', 'SecurityConstraints'];

        // When using summaryOnly=true, the facet is the root element
        if (data[0] && data[0]['@count']) {
          data.summary = data[0];
          results = data[0]['@count'];
        }

        // Cleaning facets
        for (var facet in data.summary) {
          if (facet == 'dimension') {
            dimension = gnUtilityService.traverse(
                data.summary.dimension,
                gnUtilityService.formatObjectPropertyAsArray,
                'category');
          } else if (facet != '@count' && facet != '@type') {
            facets[facet] = data.summary[facet];
            facets[facet].name = facet;
          } else if (facet == '@count') {
            // Number of results
            results = data.summary[facet];
          }
        }

        if (data.metadata) {
          // Retrieve metadata
          for (var i = 0; i < data.metadata.length ||
              (!$.isArray(data.metadata) && i < 1); i++) {
            var metadata =
                $.isArray(data.metadata) ? data.metadata[i] : data.metadata;

            // Fix all fields which are arrays and are returned as string
            // when only one value returned.
            for (var property in metadata) {
              if (metadata.hasOwnProperty(property) &&
                  listOfArrayFields.indexOf(property) != -1 &&
                  typeof metadata[property] === 'string') {
                metadata[property] = [metadata[property]];
              }
            }

            // Parse selected to boolean
            metadata['geonet:info'].selected =
                metadata['geonet:info'].selected == 'true';
          }
        }

        var records = [];
        if (data.metadata && data.metadata.length) {
          records = data.metadata; // results is an array
        } else if (data.metadata) {
          records = [data.metadata]; // only one result
        }

        return {
          facet: facets,
          dimension: dimension,
          count: results,
          metadata: records
        };

      };

      /**
       * Link together records, filter and a pager.
       * Return the search function to invoke.
       *
       * <code>
       *        $scope.records = {};
       *        $scope.filter = {};
       *
       *        // Pager config
       *        $scope.pagination = {
       *          pages: -1,
       *          currentPage: 0,
       *          hitsPerPage: 20
       *        };
       *
       *        // Register the search results, filter and pager
       *        // and get the search function back
       *        searchFn = gnSearchManagerService.register({
       *          records: 'records',
       *          filter: 'filter',
       *          pager: 'pagination'
       *          //              error: function () {console.log('error');},
       *          //              success: function () {console.log('succ');}
       *        }, $scope);
       *
       *        // Update search filter and reset page
       *        $scope.search = function(e) {
       *          $filter = {any: (e ? e.target.value : '')};
       *          $scope.pagination.currentPage = 0;
       *          searchFn();
       *        };
       *
       *        // When the current page change trigger the search
       *        $scope.$watch('pagination.currentPage', function() {
       *          $scope.search();
       *        });
       *        </code>
       */
      var register = function(config, scope) {

        var searchFn = function() {
          var pageOptions = scope[config.pager], filter = '';

          scope[config.filter] && $.each(scope[config.filter],
              function(key, value) {
                filter += '&' + key + '=' + value;
              });
          search('q?_content_type=json&fast=index' +
              '&bucket=' + scope.searchResults.selectionBucket +
              filter +
              '&from=' + (pageOptions.currentPage *
              pageOptions.hitsPerPage + 1) +
              '&to=' + ((pageOptions.currentPage + 1) *
              pageOptions.hitsPerPage), config.error)
              .then(function(data) {
                scope[config.records] = data;
                pageOptions.count = parseInt(data.count);
                pageOptions.pages = Math.round(
                    data.count /
                    pageOptions.hitsPerPage, 0);
                config.success && config.success(data);
              });
        };
        return searchFn;
      };

      /**
       * Run a search.
       */
      var search = function(url, error) {
        var defer = $q.defer();
        $http.get(url).
            success(function(data, status) {
              defer.resolve(format(data));
            }).
            error(function(data, status) {
              defer.reject(error);
            });
        return defer.promise;
      };

      // TODO: remove search call to use params instead
      // of url and use gnSearch only (then rename it to search)
      var gnSearch = function(params, error, internal) {
        var defer = $q.defer();
        gnHttp.callService(internal ? 'internalSearch' : 'search',
            params).
            success(function(data, status) {
              defer.resolve(format(data));
            }).
            error(function(data, status) {
              defer.reject(error);
            });
        return defer.promise;
      };

      var indexSetOfRecords = function(params) {
        var defer = $q.defer();
        var defaultParams = {
          fast: 'index',
          summaryOnly: 'true'
        };
        angular.extend(params, defaultParams);

        gnSearch(params).then(function(data) {
          if (parseInt(data.count) > 0) {
            selectAll().then(function() {
              index(false, true).then(function(data) {
                defer.resolve(data);
              });
            });
          } else {
            defer.reject('No records to index');
          }
        }, function(reason) {
          defer.reject('error: ' + reason);
        });
        return defer.promise;
      };
      var index = function(reset, fromSelection) {
        var defer = $q.defer();
        var url = 'admin.index.rebuildxlinks?reset=';
        url += reset ? 'yes' : 'no';
        url += '&fromSelection=';
        url += fromSelection ? 'yes' : 'no';

        $http.get(url).
            success(function(data, status) {
              defer.resolve(data);
            }).
            error(function(data, status) {
              defer.reject(error);
            });
        return defer.promise;
      };
      var selected = function(bucket) {
        return $http.get('../api/selections/' + (bucket || 'metadata'));
      };
      var select = function(uuid, bucket) {
        return $http.put('../api/selections/' + (bucket || 'metadata'), null, {
          params: {
            uuid: uuid
          }
        });
      };
      var unselect = function(uuid, bucket) {
        return $http.delete('../api/selections/' + (bucket || 'metadata'),
            {
              params: {
                uuid: uuid
              }
            });
      };
      var selectAll = function(bucket) {
        return $http.put('../api/selections/' + (bucket || 'metadata'));
      };
      var selectNone = function(bucket) {
        return $http.delete('../api/selections/' + (bucket || 'metadata'));
      };

      return {
        search: search,
        format: format,
        gnSearch: gnSearch,
        register: register,
        selected: selected,
        select: select,
        unselect: unselect,
        selectAll: selectAll,
        selectNone: selectNone,
        indexSetOfRecords: indexSetOfRecords
      };
    }
  ]);

})();
