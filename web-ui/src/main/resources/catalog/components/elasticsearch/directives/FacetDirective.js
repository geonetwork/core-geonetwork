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

(function () {
  goog.provide('gn_facet_directive')

  var module = angular.module('gn_facet_directive', [])


  /**
   * All facet panel
   * @constructor
   */
  var FacetsController = function ($scope, gnSearchManagerService) {
    this.searchManager = gnSearchManagerService.getSearchManager(this.searchName);

    this.fLvlCollapse = {};
    this.$scope = $scope;

    $scope.$watch(
      function () {
        return this.list
      }.bind(this),
      function (newValue) {
        if (!newValue) return;
        var lastFacet = this.lastUpdatedFacet;

        if (this._isFlatTermsFacet(lastFacet) && this.searchManager.hasFiltersForKey(lastFacet.key)) {
          this.list.forEach(function (f) {
            if (f.key === lastFacet.key) {
              f.items = lastFacet.items
            }
          }.bind(this));
          this.lastUpdatedFacet = null
        }
      }.bind(this)
    )
  };

  FacetsController.prototype.$onInit = function () {
  };

  FacetsController.prototype.collapseAll = function () {
    for (var i = 0; i < this.list.length; i++) {
      this.fLvlCollapse[this.list[i].key] = true;
    }
  };
  FacetsController.prototype.expandAll = function () {
    for (var i = 0; i < this.list.length; i++) {
      this.fLvlCollapse[this.list[i].key] = false;
    }
  };


  FacetsController.prototype._isFlatTermsFacet = function (facet) {
    return facet && (facet.type === 'terms') && !facet.aggs
  };

  FacetsController.$inject = [
    '$scope',
    'gnSearchManagerService'
  ];

  module.directive('esFacets', [
   'gnLangs',
    function (gnLangs) {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetsController,
        bindToController: true,
        scope: {
          list: '<esFacets',
          sParams: '<params',
          type: '<facetType',
          searchName: '@esFacetSearchName'
        },
        templateUrl: function (elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facets.html'
        },
        link: function (scope, element, attrs) {
        }
      }
    }]);


  /**
   * One facet block
   * @param $scope
   * @param gnSearchManagerService
   * @constructor
   */
  var FacetController = function ($scope, gnSearchManagerService) {
    this.$scope = $scope;
    this.searchManager = gnSearchManagerService.getSearchManager(this.searchName);
  };

  FacetController.prototype.$onInit = function () {
    this.item.collapsed = true;
    if (this.facet.type === 'tree') {
      this.item.path = [this.facet.key, this.item.key];
    }
  };

  FacetController.prototype.filter = function (facet, item) {
    var value;
    if (facet.type === 'terms') {
      value = true
      if (!item.isNested) {
        this.facetsCtrl.lastUpdatedFacet = facet
      }
    } else if (facet.type === 'filters') {
      value = item.query_string.query_string.query
    } else if (facet.type === 'tree') {
      value = true;
    }
    this.searchManager.toggleParam(item.path, value);
    this.searchManager.trigger();
  };

  FacetController.prototype.isInSearch = function (facet, item) {
    return this.searchManager.isInSearch(item.path)
  };

  FacetController.prototype.toggleCollapse = function () {
    this.item.collapsed = !this.item.collapsed;
  };

  FacetController.$inject = [
    '$scope',
    'gnSearchManagerService'
  ];

  module.directive('esFacet', [
    function () {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetController,
        bindToController: true,
        scope: {
          facet: '<esFacet',
          item: '<esFacetItem',
          searchName: '@esFacetSearchName'
        },
        require: {
          facetsCtrl: '^^esFacets',
          facetCtrl: '?^^esFacet',
        },
        templateUrl: function (elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facet.html'
        },
        link: function (scope, element, attrs) {
        }
      }
    }])

})();
