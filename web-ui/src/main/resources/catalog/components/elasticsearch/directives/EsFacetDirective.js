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


  var FacetsController = function() {

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
          list: '<esFacets'
        },
        templateUrl: function(elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facets.html';
        },
        link: function(scope, element, attrs) {

          scope.fLvlCollapse = {};
          scope.collapseAll = function() {
            angular.forEach(scope.fLvlCollapse, function(v, k) {
              scope.fLvlCollapse[k] = true;
            });
          };
          scope.expandAll = function() {
            angular.forEach(scope.fLvlCollapse, function(v, k) {
              scope.fLvlCollapse[k] = false;
            });
          };

          var hasOverridenConfig =
            angular.isArray(scope.facetList) &&
            scope.facetList.length > 0;
          scope.getLabel = function(facet) {
            if (hasOverridenConfig) {
              for (var i = 0; i < scope.facetList.length; i++) {
                var f = scope.facetList[i];
                if (facet['@name'] === f.key) {
                  var newLabel = f.labels && f.labels[gnLangs.getCurrent()];
                  if (newLabel) {
                    return facet['@label'] = newLabel;
                  }
                }
              }
            }
            return facet['@label'];
          };

          scope.tabs = null;
          scope.activeTab = null;
          scope.isTabMode = angular.isDefined(attrs['tabField']);
          scope.initialValues = null;
          if (scope.tabField) {
            // Init the facet to use for tabs the first time
            // search is triggered
            scope.$watch('dimension', function(n, o) {
              if (n !== o && scope.dimension.length > 0) {
                angular.forEach(scope.dimension, function(value) {
                  if (value['@name'] === scope.tabField) {
                    if (scope.tabs == null) {
                      scope.initialValues = value.category;
                    } else {
                      angular.extend(value.category, scope.initialValues);
                    }
                    scope.tabs = value;
                    scope.categoryKey = scope.getLabel(scope.tabs);
                    angular.forEach(scope.tabs.category, function(c) {
                      c.isSelected = scope.activeTab === c;
                    });
                  }
                });
              }
            });


            scope.buildPath = function(category, $event) {
              return gnFacetConfigService.buildPath(scope.path, category);
            };

            scope.buildLabel = gnFacetConfigService.buildLabel;

            scope.filter = function(category, $event) {
              category.isSelected = !category.isSelected;
              if (category.isSelected) {
                scope.activeTab = category;
              } else {
                scope.activeTab = null;
              }
              angular.forEach(scope.tabs.category, function(c) {
                c.isSelected = scope.activeTab === c;
              });
              gnFacetConfigService.filter(scope, category, category.isSelected);
            };

            scope.isOnDrillDownPath = function(category, $event) {
              return gnFacetConfigService.isOnDrillDownPath(scope, category);
            };

            scope.isInFilter = function(category, $event) {
              return gnFacetConfigService.isInFilter(scope, category);
            };
          }

          scope.isDisplayed = function(facet) {
            if (hasOverridenConfig) {
              // Check if the facet should be displayed
              for (var i = 0; i < scope.facetList.length; i++) {
                var f = scope.facetList[i];
                if (facet['@name'] === f.key) {
                  var newLabel = f.labels && f.labels[gnLangs.getCurrent()];
                  if (newLabel) {
                    facet['@label'] = newLabel;
                  }
                  return true;
                }
              }
            } else {
              return true;
            }
            return false;
          };

          // Facet is collapsed if not in current search criteria
          scope.isFacetsCollapse = function(facetKey) {
            return !angular.isDefined(scope.params[facetKey]);
          };

          // Load facet configuration to know which index field
          // correspond to which dimension.
          gnFacetConfigService.loadConfig(scope.facetType).
          then(function(data) {
            scope.facetConfig = {
              config: data,
              map:  {}
            };

            angular.forEach(scope.facetConfig.config, function(key) {
              scope.facetConfig.map[key.label] = key.name;
            });
          });
        }
      };
    }]);

  var FacetController = function() {

  };


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
        templateUrl: function (elem, attrs) {
          return attrs.template || '../../catalog/components/elasticsearch/directives/' +
            'partials/facet.html';
        },
        link: function (scope, element, attrs) {

        }
      };
    }]);

})();
