(function() {
  goog.provide('gn_dashboard_content_stat_controller');

  var module = angular.module('gn_dashboard_content_stat_controller',
      []);



  /**
   *
   */
  module.controller('GnDashboardContentStatController', [
    '$scope', '$routeParams', '$http', '$translate',
    function($scope, $routeParams, $http, $translate) {

      $scope.statistics = {md: {}};
      $scope.hits = 10;

      function getMainStat() {
        $http.get($scope.url + 'statistics-content@json')
        .success(function(data) {
              $scope.statistics.md.mainStatistics = data;
            }).error(function(data) {
              // TODO
            });

        $http.get($scope.url + 'q@json?fast=index&' +
                'sortBy=popularity&from=1&to=' + $scope.hits)
                .success(function(data) {
              $scope.statistics.md.popularity = data.metadata;
            }).error(function(data) {
              // TODO
            });

        $http.get($scope.url + 'q@json?fast=index&' +
                'sortBy=rating&from=1&to=' + $scope.hits)
        .success(function(data) {
              $scope.statistics.md.rating = data.metadata;
            }).error(function(data) {
              // TODO
            });
      };

      function getMetadataStat(by, isTemplate) {
        isTemplate = isTemplate || 'n';
        // Search by service type statistics
        $http.get($scope.url + 'statistics-content-metadata@json?' +
                'by=' + by +
                '&isTemplate=' + encodeURIComponent(isTemplate))
                  .success(function(data) {
              var total = 0;
              for (var i in data) {
                data[i].total = parseInt(data[i].total);
                total += data[i].total;
              }
              $scope.statistics.md[by] = data;
              nv.addGraph(function() {
                var chart = nv.models.pieChart()
                         .x(function(d) { return $translate(d.label) })
                         .y(function(d) { return d.total})
                         .values(function(d) { return d})
                         .tooltips(true)
                         .tooltipContent(function(key, y, e, graph) {
                      // TODO : %age should be relative to
                      // the current set of displayed values
                      return '<h3>' + key + '</h3>' +
                             '<p>' + parseInt(y).toFixed() + ' ' +
                             $translate('records') + ' (' +
                             (y / total * 100).toFixed() + '%)</p>';
                    })
                         .showLabels(true);

                d3.select('#gn-stat-md-by-' + by)
                       .datum([data])
                       .transition().duration(1200)
                       .call(chart);

                return chart;
              });
            }).error(function(data) {
              // TODO
            });
      };

      getMainStat();

      getMetadataStat('schema');
      getMetadataStat('template', '%');
      getMetadataStat('source');
      getMetadataStat('harvest');
      getMetadataStat('category');
      getMetadataStat('status');
      getMetadataStat('validity');
      getMetadataStat('owner');
      getMetadataStat('groupowner');

    }]);

})();
