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
    this.facetService_ = $injector.get('gnESFacet');
    this.searchClient_ = $injector.get('gnESClient');
    this.searchService_ = $injector.get('gnESService');

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
    this.state_ = {
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
  SearchManager.prototype.trigger = function() {
    this.state_.loading = true;
    this.state_.loadingFacets = true;
    var params = this.searchService_.generateEsRequest(this.state_);

    this.searchClient_.search(params, '1234').then(function(response) {
      this.state_.loading = false;
      this.state_.loadingFacets = false;
      this.state_.results = response.records;
      this.state_.facets = response.facets;
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
    this.state_.from = from;
    this.state_.to = to;
    this.trigger();
  };

  /**
   * Set a sorting criteria, and whether the sort is reversed. This will
   * trigger a search.
   * @param {string} criteria
   * @param {bool} [reverse]
   */
  SearchManager.prototype.setSortBy = function(criteria, reverse) {
    this.state_.sortBy = criteria;
    this.state_.sortByReversed = !!reverse;
    this.trigger();
  };

  /**
   * Reset the search params.
   */
  SearchManager.prototype.reset = function() {
    this.state_.any = '';
    this.state_.params = {};
  };

  /**
   * Returns whether a search is running.
   * @returns {boolean}
   */
  SearchManager.prototype.isLoading = function() {
    return this.state_.loading;
  };

  /**
   * Returns whether search results are currently present in the state.
   * @returns {boolean}
   */
  SearchManager.prototype.hasResults = function() {
    return this.state_.results.length > 0;
  };

  /**
   * Returns the results array. Note: the reference will change
   * when new results are received.
   * @returns {Array<Object>}
   */
  SearchManager.prototype.getResults = function() {
    return this.state_.results;
  };

  /**
   * Returns the facets array. Note: the reference will change
   * when new facets are received.
   * @returns {Array<Object>}
   */
  SearchManager.prototype.getFacets = function() {
    return this.state_.facets;
  };

  /**
   * Returns the params dictionary.
   * @returns {Array<Object>}
   */
  SearchManager.prototype.getParams = function() {
    return this.state_.params;
  };

  /**
   * full text search property, to be used with ngModel
   */
  Object.defineProperty(SearchManager.prototype, 'fullText', {
    get: function() {
      return this.state_.any;
    },
    set: function(text) {
      this.state_.any = text || '';
    }
  });


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
