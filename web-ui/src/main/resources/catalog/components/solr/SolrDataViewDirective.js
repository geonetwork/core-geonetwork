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
  goog.provide('gn_solr_data_view_directive');

  var module = angular.module('gn_solr_data_view_directive', []);

  module.directive('gnDataFilterView',
      [
       function() {

         return {
           restrict: 'A',
           scope: {
             map: '=gnDataFilterView'
           },
           templateUrl: '../../catalog/components/solr/' +
           'partials/datafilterview.html',
           link: function(scope, element, attrs) {
             scope.currentLayer = null;
             scope.layers = scope.map.getLayers().getArray();
             scope.excludeCols = [
               'id', '_version_', 'featureTypeId', 'docType'
             ];
             scope.setLayer = function(l) {
               scope.currentLayer = l;
             };
             scope.getLabel = function(layer) {
               return layer.get('label');
             };
             scope.set = function() {
               if (scope.layer) {
                 scope.currentLayer = scope.layer;
               }
             };
             scope.map.getLayers().on('remove', function(e) {
               if (e.element == scope.currentLayer) {
                 scope.setLayer(null);
               }
             });
           }
         };
       }]);

  module.filter('overlay', function() {
    return function(input) {
      if (!input) { return; }
      return input.filter(function(l) {
        return l.background != true && !(l instanceof ol.layer.Vector);
      });
    };
  });

  module.directive('gnDataTable',
      ['$http', '$translate', 'gnSolrRequestManager',
       function($http, $translate, gnSolrRequestManager) {

         return {
           restrict: 'A',
           replace: true,
           scope:  {
             q: '=gnDataTable',
             excludeCols: '='
           },
           templateUrl: '../../catalog/components/solr/' +
           'partials/datatable.html',
           link: function(scope, element, attrs) {
             var pageList = [5, 10, 50, 100];
             var table = element.find('table');
             scope.url = null;

             var solrObject = gnSolrRequestManager.register(
                 attrs['gnDataTableSolrType'],
                 attrs['gnDataTableSolrName']);

             scope.$watch(function() {
               return solrObject.baseUrl;
             }, function(solrUrl, oldValue) {
               if (solrUrl) {
                 var columns = [],
                     fields = solrObject.filteredDocTypeFieldsInfo;

                 fields.forEach(function(field) {
                   if ($.inArray(field.idxName, scope.excludeCols) === -1) {
                     columns.push({
                       field: field.idxName,
                       title: $translate.instant(field.label)
                     });
                   }
                 });

                 // TODO: Should use the solObject to get table of results
                 // and do paging/sorting instead of re-running the query
                 solrObject.on('search', function(event) {
                   var url = solrUrl.replace('rows=0', '');
                   if (url.indexOf('&q=') === -1) {
                     url += '&q=*:*';
                   }
                   table.bootstrapTable('destroy');
                   table.bootstrapTable({
                     url: url,
                     queryParams: function(p) {
                       return {
                         rows: p.limit,
                         start: p.offset
                       };
                     },
                     //data: scope.data.response.docs,
                     responseHandler: function(res) {
                       return {
                         total: res.response.numFound,
                         rows: res.response.docs
                       };
                     },
                     columns: columns,
                     pagination: true,
                     sidePagination: 'server',
                     totalRows: solrObject.totalCount,
                     pageSize: pageList[0],
                     pageList: pageList
                   });
                 });

               } else {
                 table.bootstrapTable('destroy');
               }
             });
           }
         };
       }]);


})();
