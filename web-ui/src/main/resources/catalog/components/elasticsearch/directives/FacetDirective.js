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

  goog.require('gn_facets');

  var module = angular.module('gn_facet_directive', ['gn_facets']);


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

        for (var i = 0; i < this.list.length; i++) {
          this.fLvlCollapse[this.list[i].key] =
            angular.isDefined(this.fLvlCollapse[this.list[i].key]) ?
              this.fLvlCollapse[this.list[i].key] :
              this.list[i].collapsed === true;
        }

        var lastFacet = this.lastUpdatedFacet

        if (this._isFlatTermsFacet(lastFacet) && this.searchCtrl.hasFiltersForKey(lastFacet.key)) {
          this.list.forEach(function (f) {
            if (f.key === lastFacet.key) {
              f.items = lastFacet.items
            }
          }.bind(this))
          this.lastUpdatedFacet = null
        }
      }.bind(this)
    )
  }

  FacetsController.prototype.$onInit = function () {
  }

  FacetsController.prototype.collapseAll = function () {
    for (var i = 0; i < this.list.length; i++) {
      this.fLvlCollapse[this.list[i].key] = true;
    }
  }

  FacetsController.prototype.expandAll = function () {
    for (var i = 0; i < this.list.length; i++) {
      this.fLvlCollapse[this.list[i].key] = false;
    }
  }

  FacetsController.prototype.isVisibleForUser = function (facet) {
    var user = this.$scope.$parent.user;
    if (user && facet.userHasRole && user[facet.userHasRole]) {
      return user[facet.userHasRole]();
    } else {
      return facet.userHasRole ? false : true;
    }
  }

  FacetsController.prototype.loadMoreTerms = function (facet) {
    this.searchCtrl.loadMoreTerms(facet).then(function (terms) {
      angular.merge(facet, terms);
    });
  }

  FacetsController.prototype.filterTerms = function (facet) {
    this.searchCtrl.filterTerms(facet).then(function (terms) {
      angular.merge(facet, terms);
      facet.items = terms.items;
    });
  }

  FacetsController.prototype.onUpdateDateRange = function (facet, from, to) {
    var query_string =  '+' + facet.key + ':[' + moment(from, 'DD-MM-YYYY').toISOString() + ' TO ' +
      moment(to, 'DD-MM-YYYY').toISOString() + ']';
    this.$scope.$digest();
    this.searchCtrl.updateState(facet.path, query_string, true);
  };


  FacetsController.prototype._isFlatTermsFacet = function (facet) {
    return facet && (facet.type === 'terms') && !facet.aggs
  }

  FacetsController.$inject = [
    '$scope'
  ]

  // Define the translation group key matching the facet key
  var facetKeyToTranslationGroupMap = new Map([
    ['isTemplate', 'recordType'],
    ['groupOwner', 'group'],
    ['sourceCatalogue', 'source']
  ]);

  module.filter('facetTranslator', ['$translate', function($translate) {

    return function(input, facetKey) {
      if (!input || angular.isObject(input)) {
        return input;
      }

      // Tree aggregation
      if (input.indexOf && input.indexOf('^') !== -1) {
        return input.split('\^')
                    .map(function(t) {return $translate.instant(t)})
                    .join(' / ');
      }

      // A specific facet key eg. "isHarvested-true"
      var translationId = (facetKeyToTranslationGroupMap.get(facetKey) || facetKey) + '-' + input,
        translation = $translate.instant(translationId);
      if (translation !== translationId) {
        return translation;
      } else {
        // A common translations ?
        translation = $translate.instant(input);
        if (translation != input) {
          return translation;
        }
      }
      return input;
    };
  }]);

  /**
   * Ignore object field suffix
   */
  module.filter('facetKeyTranslator', ['$translate', function($translate) {
    return function(input) {
      return $translate.instant(input.replace(/(.key|.default|.lang{3}[a-z])$/, ''));
    };
  }]);

  module.directive('esFacets', [
   'gnLangs',
    function (gnLangs) {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetsController,
        bindToController: true,
        scope: {
          list: '<esFacets'
        },
        require: {
          searchCtrl: '^^ngSearchForm'
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
  }

  FacetController.prototype.$onInit = function () {
    this.item.collapsed = true;
    if (this.facet.type === 'tree') {
      this.item.path = [this.facet.key, this.item.key];
      this.item.collapsed = !this.searchCtrl.hasChildInSearch(this.item.path);
    } else if (this.facet.type === 'filters'|| this.facet.type === 'histogram') {
      this.item.inverted = this.searchCtrl.isNegativeSearch(this.item.path);
    }
  }

  FacetController.prototype.filter = function (facet, item) {
    var value = !item.inverted;
    if (facet.type === 'terms') {
      facet.include = '';
      if (!item.isNested) {
        this.facetsCtrl.lastUpdatedFacet = facet;
      }
    } else if (facet.type === 'filters' || facet.type === 'histogram') {
      value = item.query_string.query_string.query;
      if(item.inverted) {
        value = '-('+value+')';
      }
    } else if (facet.type === 'tree') {
    }
    this.searchCtrl.updateState(item.path, value);
  }

  FacetController.prototype.isInSearch = function (facet, item) {
    return this.searchCtrl.isInSearch(item.path);
  }

  FacetController.prototype.toggleCollapse = function () {
    this.item.collapsed = !this.item.collapsed;
  }

  FacetController.prototype.toggleInvert = function () {
    var item = this.item;
    item.inverted = !item.inverted;
    this.filter(this.facet, item);
  }

  FacetController.$inject = [
    '$scope'
  ]

  module.directive('esFacet', [
    'gnLangs',
    function (gnLangs) {
      return {
        restrict: 'A',
        controllerAs: 'ctrl',
        controller: FacetController,
        bindToController: true,
        scope: {
          facet: '<esFacet',
          item: '<esFacetItem'
        },
        require: {
          facetsCtrl: '^^esFacets',
          facetCtrl: '?^^esFacet',
          searchCtrl: '^^ngSearchForm'
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
