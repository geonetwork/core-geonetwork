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
  var FacetsController = function() {
    this.fLvlCollapse = {};
  };

  FacetsController.prototype.$onInit = function() {
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
  };

  FacetController.prototype.filter = function(facet, item) {
    var params = this.facetsCtrl.sParams;
    var value = params[facet.name];
    if(value) {
      if(!angular.isArray(value)) {
        params[facet.name] = [value];
      }
    }
    else {
      params[facet.name] = [];
    }
    params[facet.name].push(item.name);
    this.$scope.$emit('resetSearch', params);
  };

  FacetController.prototype.isInSearch = function(facet, item) {
      var p = this.facetsCtrl.sParams[facet.name];
      if (angular.isString(p)) {
        p = [p];
      }
      return p && p.indexOf(item.name) >= 0;
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
