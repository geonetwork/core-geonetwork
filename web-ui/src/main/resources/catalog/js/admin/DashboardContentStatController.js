(function() {
  goog.provide('gn_dashboard_content_stat_controller');

  var module = angular.module('gn_dashboard_content_stat_controller',
      []);



  /**
   * Provides statistics on the catalog content.
   *
   * TODO: Move NVD3 calls to specific module
   */
  module.controller('GnDashboardContentStatController', [
    '$scope', '$routeParams', '$http', '$translate',
    function($scope, $routeParams, $http, $translate) {

      $scope.statistics = {md: {}};
      $scope.hits = 10;
      $scope.currentIndicator = null;
      $scope.currentIndicatorData = null;

      /**
       * Refresh graph on currently selected facet
       */
      $scope.$watch('currentIndicator', function() {
        // No data

        if ($scope.currentIndicator === null) {
          return;
        }

        // Format results
        var data = [];
        var total = 0;
        $.each($scope.currentIndicator, function(index, value) {
          var count = parseInt(value['@count']);
          data.push({label: value['@name'], count: count});
          total += count;
        });

        nv.addGraph(function() {
          var chart = nv.models.pieChart()
                       .x(function(d) { return d.label})
              // TODO : Need to translate facet labels ?
                       .y(function(d) { return d.count})
                       .values(function(d) { return d})
                       .tooltips(true)
                       .tooltipContent(function(key, y, e, graph) {
                // TODO : %age should be relative to
                // the current set of displayed values
                var value = d3.format('.0f')(y.replace(',', ''));
                return '<h3>' + key + '</h3>' +
                    '<p>' + value + ' ' +
                    $translate('records') + ' (' +
                    (value / total * 100).toFixed() + '%)</p>';
              })
                       .showLabels(true);

          d3.select('#gn-stat-md-by-facet')
                     .datum([data])
                     .transition().duration(1200)
                     .call(chart);

          return chart;
        });
      });

      function getMainStat() {

        $scope.statistics.md.popularity = {
          sortBy: 'popularity'
        };
        $scope.statistics.md.rating = {
          sortBy: '_rating',
          _rating: '1 or 2 or 3 or 4 or 5'
        };

        $scope.paginationInfo = {
          pages: -1,
          currentPage: 1,
          hitsPerPage: 10
        };

        $http.get('statistics-content?_content_type=json')
        .success(function(data) {
              $scope.statistics.md.mainStatistics = data;
            }).error(function(data) {
              // TODO
            });
      };

      function getMetadataStat(by, isTemplate) {
        isTemplate = isTemplate || 'n';
        // Search by service type statistics
        $http.get('statistics-content-metadata?_content_type=json?' +
                'by=' + by +
                '&isTemplate=' + encodeURIComponent(isTemplate))
                  .success(function(data) {

              if (data == 'null') { // Null response returned
                // TODO : Add no data message
                return;
              }
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
      getMetadataStat('harvested');
      getMetadataStat('category');
      getMetadataStat('status');
      getMetadataStat('validity');
      getMetadataStat('owner');
      getMetadataStat('groupowner');

    }]);

  module.filter('mdRated', function() {
    return function(input) {
      var ret = [];
      if (angular.isArray(input)) {
        for (var i = 0; i < input.length; ++i) {
          if (input[i].rating > 0) {
            ret.push(input[i]);
          }
        }
      }
      return ret;
    }
  });

  module.controller('GnDashboardContentStatControllerPopularity', [
    '$scope',
    function($scope) {
      $scope.searchObj = {
        permalink: false,
        params: $scope.statistics.md.popularity
      };
    }]);
  module.controller('GnDashboardContentStatControllerRating', [
    '$scope',
    function($scope) {
      $scope.searchObj = {
        permalink: false,
        params: $scope.statistics.md.rating
      };
    }]);

})();
