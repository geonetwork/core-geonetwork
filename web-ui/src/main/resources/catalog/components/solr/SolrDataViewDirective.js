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
               'id', '_version_',
               'featureTypeId', 'docType'];
             scope.setLayer = function(l) {
               scope.currentLayer = l;
             };
             scope.map.getLayers().on('remove', function(e) {
               if (e.element == scope.currentLayer) {
                 scope.setLayer(null);
               }
             });
           }
         };
       }]);

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
                       title: $translate(field.label)
                     });
                   }
                 });

                 solrObject.on('search', function(event) {
                   table.bootstrapTable('destroy');
                   table.bootstrapTable({
                     url: solrUrl.replace('rows=0', ''),
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

               }
               else {
                 table.bootstrapTable('destroy');
               }
             });
           }
         };
       }]);


})();
