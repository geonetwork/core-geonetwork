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
  var FacetsController = function ($scope) {
    this.fLvlCollapse = {}
    this.currentFacet
    this.$scope = $scope

    $scope.$watch(
      function () {
        return this.list
      }.bind(this),
      function (newValue) {
        if (!newValue) return
        var lastFacet = this.lastUpdatedFacet
        if (this._isFlatTermsFacet(lastFacet) && Object.keys(this.state[lastFacet.name]).length) {
          this.list.forEach(function (f) {
            if (f.name === lastFacet.name) {
              f.items = lastFacet.items
            }
          }.bind(this))
          this.lastUpdatedFacet = null
        }
      }.bind(this)
    )
  }

  FacetsController.prototype.$onInit = function () {
    this.state = this.lucene.facets || {}
  }

  FacetsController.prototype.collapseAll = function () {
    angular.forEach(this.fLvlCollapse, function (v, k) {
      this.fLvlCollapse[k] = true
    }.bind(this))
  }
  FacetsController.prototype.expandAll = function () {
    angular.forEach(this.fLvlCollapse, function (v, k) {
      this.fLvlCollapse[k] = false
    }.bind(this))
  }

  FacetsController.prototype.updateSearch = function () {
    this.lucene.facets = this.state
    this.$scope.$parent.triggerSearch();
  }

  FacetsController.prototype._isFlatTermsFacet = function (facet) {
    return facet && (facet.type === 'terms') && !facet.aggs
  }

  FacetsController.$inject = [
    '$scope'
  ]

  module.directive('esFacets', [
    'gnFacetConfigService', 'gnLangs',
    function (gnFacetConfigService, gnLangs) {
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
        templateUrl: function (elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facets.html'
        },
        link: function (scope, element, attrs) {
        }
      }
    }])


  /**
   * One facet block
   * @param $scope
   * @constructor
   */
  var FacetController = function ($scope) {
    this.$scope = $scope

    $scope.$on('beforeSearchReset', function () {
      this._resetState()
    }.bind(this))
  }

  FacetController.prototype.$onInit = function () {
    this.parentCtrl = this.facetCtrl || this.facetsCtrl
    this._initState()
  }

  FacetController.prototype.filter = function (facet, item) {
    if (this.state[item.name]) {
      delete this.state[item.name]
    } else {
      var itemState
      if (facet.type === 'terms') {
        itemState = true

        if (!item.isNested) {
          this.facetsCtrl.lastUpdatedFacet = facet
        }
      } else if (facet.type === 'filters') {
        itemState = item.query_string.query_string.query
      }
      this.state[item.name] = itemState
    }
    this.facetsCtrl.updateSearch()
  }

  FacetController.prototype.isInSearch = function (facet, item) {
    return this.state.hasOwnProperty(item.name) && !angular.isObject(this.state[item.name])
  }

  FacetController.prototype._initState = function () {
    if( this.parentName) {
      this.parentCtrl.state[this.parentName] = this.parentCtrl.state[this.parentName] || {}
      this.parentCtrl.state[this.parentName][this.facet.name] = this.state = this.parentCtrl.state[this.parentName][this.facet.name] || {}
    } else {
      this.parentCtrl.state[this.facet.name] = this.state = this.parentCtrl.state[this.facet.name] || {}
    }
  }

  FacetController.prototype._resetState = function () {
    if( this.parentName) {
      this.parentCtrl.state[this.parentName] = {}
      this.parentCtrl.state[this.parentName][this.facet.name] =  this.state = {}
    } else {
      this.parentCtrl.state[this.facet.name] = this.state = {}
    }
  }

  FacetController.$inject = [
    '$scope'
  ]

  module.directive('esFacet', [
    'gnFacetConfigService', 'gnLangs',
    function (gnFacetConfigService, gnLangs) {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetController,
        bindToController: true,
        scope: {
          facet: '<esFacet',
          parentName: '<esFacetParentName'
        },
        require: {
          facetsCtrl: '^^esFacets',
          facetCtrl: '?^^esFacet'
        },
        templateUrl: function (elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facet.html'
        },
        link: function (scope, element, attrs) {
        }
      }
    }])

})()
