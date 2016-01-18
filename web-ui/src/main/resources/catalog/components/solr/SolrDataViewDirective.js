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
           }
         };
       }]);

  module.directive('gnDataTable',
      ['$http', '$translate',
       function($http, $translate) {

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
             scope.$watch('q', function(newValue, oldValue) {
               if (newValue !== oldValue) {
                 if (scope.q === undefined) {
                   table.bootstrapTable('destroy');
                 } else {
                   // TODO could be better to refreshOptions
                   table.bootstrapTable('destroy');
                   scope.url = scope.q.replace('rows=0', 'rows=1');
                   $http.get(scope.url).then(function(response) {
                     var columns = [];
                     $.each(response.data.response.docs[0], function(key) {
                       if ($.inArray(key, scope.excludeCols) === -1) {
                         columns.push({
                           field: key,
                           // TODO: Add feature catalogue translator
                           title: $translate(key)
                         });
                       }
                     });
                     table.bootstrapTable({
                       url: scope.url.replace('rows=1', ''),
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
                       totalRows: response.data.response.numFound,
                       pageSize: pageList[0],
                       pageList: pageList
                     });
                   });
                 }
               }
             });
           }
         };
       }]);


})();
