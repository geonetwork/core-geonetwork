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

  var SearchManager = function($injector) {
    this.facetService = $injector.get('gnESFacet');
    this.searchClient = $injector.get('gnESClient');
    this.searchService = $injector.get('gnESService');

    /**
     * Describes the full search state including query params, facets and results
     * @type {Object}
     * @property {number} from
     * @property {number} to
     * @property {string} sortBy
     * @property {boolean} sortByReversed
     * @property {string} any
     * @property {Object} params
     * @property {Array<Object>} results
     * @property {boolean} loading
     * @property {Array<Object>} facets
     * @property {boolean} loadingFacets
     * @property {string} geometry Geometry expressed in WKT
     * @property {string} geometryRelation Either intersects or overlaps
     */
    this.state = {
      from: 0,
      to: 10,
      sortBy: 'relevance',
      sortByReversed: false,
      any: '',
      params: {},
      results: [],
      loading: false,
      facets: [],
      loadingFacets: false
    };

  };

  /**
   * Manually trigger a search, updating the state in the process.
   */
  SearchManager.prototype.triggerSearch = function() {
    this.state.loading = true;
    this.state.loadingFacets = true;
    var params = this.searchService.generateEsRequest(this.state);

    this.searchClient.search(params, '1234').then(function(result) {
      this.state.loading = false;
      this.state.loadingFacets = false;
      console.log(result);
    }.bind(this), function(error) {
      console.error('The search failed', error);
    }.bind(this));
  };

  /**
   * Set the current pagination. This will trigger a search.
   * @param {number} from
   * @param {number} to
   */
  SearchManager.prototype.setPagination = function(from, to) {
    this.state.from = from;
    this.state.to = to;
    this.triggerSearch();
  };

  /**
   * Set a sorting criteria, and whether the sort is reversed. This will
   * trigger a search.
   * @param {string} criteria
   * @param {bool} [reverse]
   */
  SearchManager.prototype.setSortBy = function(criteria, reverse) {
    this.state.sortBy = criteria;
    this.state.sortByReversed = !!reverse;
    this.triggerSearch();
  };

  /**
   * Reset the search params.
   */
  SearchManager.prototype.resetSearch = function() {
    this.state.any = '';
    this.state.params = {};
  };

  /**
   * Change the full text search criteria, or clear it if no arguments
   * passed.
   * @param {string} text
   */
  SearchManager.prototype.setFullTextSearch = function(text) {
    this.state.any = text || '';
  };

  /**
   * Returns the full text search criteria.
   * @returns {string}
   */
  SearchManager.prototype.getFullTextSearch = function() {
    return this.state.any;
  };


  module.factory('gnSearchManagerService', [
    '$injector',
    function($injector) {
      var searches = {};

      return {
        getSearchManager: function(searchName) {
          if (!searches[searchName]) {
            searches[searchName] = new SearchManager($injector);
          }
          return searches[searchName];
        }
      };
    }
  ]);

})();
