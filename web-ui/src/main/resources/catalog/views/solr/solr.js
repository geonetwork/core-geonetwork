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

  goog.provide('gn_search_solr');

  goog.require('angularUtils.directives.dirPagination');

  goog.require('gn_search');
  goog.require('gn_solr_result');

  var module = angular.module('gn_search_solr', [
    'angularUtils.directives.dirPagination',
    'gn_search',
    'gn_solr_result',
  ]);

  module.config([ 'paginationTemplateProvider', function(paginationTemplateProvider) {
    paginationTemplateProvider.setPath('../../catalog/views/solr/dirPagination.tpl.html');
  }]);

  module.controller('GnSearchSolrController', [

    '$scope',
    '$element',
    '$timeout',
    'gnSolrRequestManager',
    'gnFeaturesTableManager',

    function($scope, $element, $timeout, gnSolrRequestManager, gnFeaturesTableManager) {

      let viewConfig = {
        center : [280274.03240585705, 6053178.654789996],
        zoom   : 2
      };

      $scope.searchMap = new ol.Map({
        controls : [],
        view     : new ol.View(viewConfig),
        layers   : [
          new ol.layer.Tile({ source: new ol.source.OSM() })
        ]
      });

      $scope.collection = new ol.Collection();

      let map = $scope.searchMap;
      let layer         = new ol.layer.Vector({
        map    : map,
        source : new ol.source.Vector({
          features        : $scope.collection,
          useSpatialIndex : false
        })
      });
      map.addLayer(layer);

      $scope.collection.on('add', function(e) {
        map.getView().fit(e.element.getGeometry().getExtent(), map.getSize(), { minResolution: 2.3 });
        $scope.feature = e.element;
      });
      $scope.collection.on('remove', function(e) {
        map.getView().fit(
          map.getView().getProjection().getExtent(),
          map.getSize()
        );
        $scope.feature = undefined;
      });

      window.map = map;
      $scope.solrObject = gnSolrRequestManager.register('Default', 'facets');

      $scope.solrObject.init({});

      $scope.anyField = '';

      $scope.pager = {
        page         : 1,
        itemsPerPage : 10
      };

      let solrParams = {
        'facet'       : true,
        'facet.field' : [ 'resourceType' ],
        start         : ($scope.pager.page - 1) * $scope.pager.itemsPerPage,
        rows          : $scope.pager.itemsPerPage
      };


      $scope.search = function(resourceType) {

        if (resourceType) {
          $scope.active  = (resourceType == 'all') ? undefined : resourceType;
        }
        $scope.pager.page = 1;

        let qParams = {
          any: $scope.anyField,
          params: {}
        };
        if ($scope.active) {
          let values           = {}
          values[resourceType] = true
          console.log(values);
          qParams.params.resourceType = {
            type   : 'field',
            values : values
          };
        }

        $scope.loading = true;
        $scope.promise = $scope.solrObject.searchWithFacets(
          qParams, solrParams
        );


      };

      $scope.reset = function() {
        $scope.anyField   = '';
        $scope.pager = {
          page         : 1,
          itemsPerPage : 10
        };
        $scope.active = undefined;
        $scope.loading = true;
        $scope.solrObject.resetSearch(solrParams);
      };

      $scope.solrObject.on('search', function(resp) {
        let page           = (resp.solrData.response.start / $scope.pager.itemsPerPage) + 1;
        $scope.loading     = false;
        $scope.pager.count = resp.count;
        $scope.facets      = resp.facets;
        $scope.results     = resp.records;
      });

      $scope.search();

      $scope.pageChanged = function(pageNumber) {
        $scope.loading = true;
        $scope.pager.page = pageNumber;
        console.log('pagechanged : ' + pageNumber)
        $scope.solrObject.affineSearch({}, {
          start: (pageNumber - 1) * $scope.pager.itemsPerPage
        });
        $scope.feature = undefined;
      };


      $scope.validProperty = function(key) {
        return key.substr(0,3) == 'ft_';
      }

  }]);

})();
