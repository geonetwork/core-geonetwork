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
  goog.provide('gn_facets_config_service');


  var module = angular.module('gn_facets_config_service', []);

  /**
   * TODO: Translate indexkey/facetkey
   */
  module.factory('gnFacetConfigService', [
    'gnHttp',
    function(gnHttp) {

      var loadConfig = function(summaryType) {
        return gnHttp.callService('facetConfig', {}, {
          cache: true
        }).then(function(data) {
          if (data.status != 200) {
            return;
          }
          if (!data.data.hasOwnProperty(summaryType)) {
            alert('ERROR: The config-summary.xml file does ' +
                "not declare a summary type of: '" + summaryType + "'");
          }
          return data.data[summaryType];
        });
      };

      function filter(scope, category, checked) {
        if (!scope.facetConfig) {
          return; // Facet configuration not yet loaded.
        }


        // Extract facet.q info
        if (angular.isUndefined(scope.params['facet.q'])) {
          scope.params['facet.q'] = '';
        }
        var facetQParam = scope.params['facet.q'];
        var dimensionList =
            facetQParam.split('&');
        var categoryList = [];
        $.each(dimensionList, function(idx) {
          // Dimension filter contains the dimension name first
          // and then the drilldown path. User may uncheck
          // an element in the middle of the path. In such case
          // only activate the parent node.
          var dimensionFilter = dimensionList[idx].split('/');

          // Dimension but not in that category path. Add filter.
          if (dimensionFilter[1] &&
              dimensionFilter[0] !=
              scope.facetConfig.map[scope.categoryKey]) {
            categoryList.push({
              dimension: dimensionFilter[0],
              value: dimensionFilter.slice(1, dimensionFilter.length)
            });
          } else if (dimensionFilter[1] &&
              dimensionFilter.length > 2 &&
              dimensionFilter[0] ==
              scope.facetConfig.map[scope.categoryKey]) {

            var filteredElementInPath =
                $.inArray(
                encodeURIComponent(category['@value']),
                dimensionFilter);
            // Restrict the path to its parent
            if (filteredElementInPath !== -1) {
              categoryList.push({
                dimension: scope.categoryKey,
                value: dimensionFilter.
                    slice(1, filteredElementInPath).
                    join('/')
              });
            }
          }
        });
        // Add or remove new category
        if (checked) {
          categoryList.push({
            dimension: scope.categoryKey,
            value: category.path
          });
        } else {

        }

        // Build facet.q
        facetQParam = '';
        $.each(categoryList, function(idx) {
          if (categoryList[idx].value) {
            facetQParam = facetQParam +
                (scope.facetConfig.map[categoryList[idx].dimension] ||
                categoryList[idx].dimension) +
                '/' +
                categoryList[idx].value +
                (idx < categoryList.length - 1 ? '&' : '');
          }
        });
        scope.params['facet.q'] = facetQParam;

        var preserveGeometrySearch = true;
        scope.$emit('resetSearch', scope.params, preserveGeometrySearch);
      }


      buildPath = function(path, category) {
        category.path =
            (path === undefined ? '' : path + '/') +
            encodeURIComponent(category['@value']);
        return category.path;
      };

      /**
       * Adds a new attribute called 'label' instead
       * of '@label' for orderBy
       * @param {Object} category
       * @return {boolean|*}
       */
      buildLabel = function(category) {
        category.label = category['@label'];
        return category.label;
      };



      /**
       * Check that current category is already used
       * in current filter.
       * @param {Object} category
       * @return {boolean|*}
       */
      isOnDrillDownPath = function(scope, category) {
        // Is selected if the category value is defined in the
        // facet.q parameter (ie. combination of
        // dim1/dim1val/dim1val2&dim2/dim2Val...).
        category.isSelected =
            angular.isUndefined(scope.params['facet.q']) ?
            false :
            ($.inArray(
            encodeURIComponent(category['@value']),
            scope.params['facet.q'].split(/&|\//)) !== -1 ||
            $.inArray(
            category['@value'],
            scope.params['facet.q'].split(/&|\//)) !== -1);
        return category.isSelected;
      };

      function isInFilter(scope, category) {
        if (!scope.facetConfig) {
          return false;
          // Facet configuration not yet loaded
        }

        var facetQParam = scope.params['facet.q'];
        if (facetQParam === undefined) {
          return false;
        }
        var dimensionList =
            facetQParam.split('&');
        var categoryList = [];
        for (var i = 0; i < dimensionList.length; i++) {
          var dimensionFilter = dimensionList[i].split('/');
          if (dimensionFilter[0] ==
              scope.facetConfig.map[scope.categoryKey] &&
              ($.inArray(
              encodeURIComponent(category['@value']),
              scope.params['facet.q'].split(/&|\//)) !== -1 ||
              $.inArray(
              category['@value'],
              scope.params['facet.q'].split(/&|\//)) !== -1)) {
            return true;
          }
        }
        return false;
      };

      return {
        loadConfig: loadConfig,
        filter: filter,
        buildPath: buildPath,
        buildLabel: buildLabel,
        isInFilter: isInFilter,
        isOnDrillDownPath: isOnDrillDownPath
      };
    }
  ]);
})();
