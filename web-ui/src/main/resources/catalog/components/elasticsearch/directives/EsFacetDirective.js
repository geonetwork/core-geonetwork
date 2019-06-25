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
  goog.provide('gn_es_facet_directive');

  var module = angular.module('gn_es_facet_directive', []);


  /**
   * All facet panel
   * @constructor
   */
  var FacetsController = function($scope) {
    this.fLvlCollapse = {};
    this.currentFacet;
    this.$scope = $scope;

    $scope.$watch(
      function() { return this.list}.bind(this),
      function(newValue) {
        if(!newValue) return;
        if( this.lastUpdatedFacet && this.state[this.lastUpdatedFacet.name].length) {
          this.list.forEach(function(f) {
            if(f.name === this.lastUpdatedFacet.name) {
              f.items = this.lastUpdatedFacet.items
            }
          }.bind(this))
          this.lastUpdatedFacet = null
        }
      }.bind(this)
    )
  };

  FacetsController.prototype.$onInit = function() {
    this.state = this.lucene.facets || {}
  };

  FacetsController.prototype.collapseAll = function() {
    angular.forEach(this.fLvlCollapse, function(v, k) {
      this.fLvlCollapse[k] = true;
    }.bind(this));
  };
  FacetsController.prototype.expandAll = function() {
    angular.forEach(this.fLvlCollapse, function(v, k) {
      this.fLvlCollapse[k] = false;
    }.bind(this));
  };

  FacetsController.prototype.updateSearch = function() {
    this.lucene.facets = this.state
    this.$scope.$emit('resetSearch', this.sParams);
  };

  FacetsController.$inject = [
    '$scope'
  ];

  module.directive('esFacets', [
    'gnFacetConfigService', 'gnLangs',
    function(gnFacetConfigService, gnLangs) {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetsController,
        bindToController: true,
        scope: {
          list: '<esFacets',
          sParams: '<params',
          lucene: '<',
          type: '<facetType'
        },
        templateUrl: function(elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facets.html';
        },
        link: function(scope, element, attrs) {
        }
      };
    }]);


  /**
   * One facet block
   * @param $scope
   * @constructor
   */
  var FacetController = function($scope) {
    this.$scope = $scope;
  };

  FacetController.prototype.$onInit = function() {
    this.facetsCtrl.state[this.facet.name] = this.state = this.facetsCtrl.state[this.facet.name] || [];
  };

  FacetController.prototype.filter = function(facet, item) {
    var index = this.state.indexOf(item.name);
    if(index > -1 ) {
      this.state.splice(index, 1)
    } else {
      this.state.push(item.name)
    }
    this.facetsCtrl.lastUpdatedFacet = facet;
    this.facetsCtrl.updateSearch();
  };

  FacetController.prototype.isInSearch = function(facet, item) {
    return (this.facetsCtrl.state[facet.name].indexOf(item.name) >= 0)
  };

  FacetController.$inject = [
    '$scope'
  ];

  module.directive('esFacet', [
    'gnFacetConfigService', 'gnLangs',
    function(gnFacetConfigService, gnLangs) {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetController,
        bindToController: true,
        scope: {
          facet: '<esFacet'
        },
        require: {
          facetsCtrl: '^^esFacets'
        },
        templateUrl: function (elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facet.html';
        },
        link: function (scope, element, attrs) {
        }
      };
    }]);

})();
