(function() {
  goog.provide('gn_dashboard_search_stat_controller');

  var module = angular.module('gn_dashboard_search_stat_controller',
      []);



  /**
   *
   */
  module.controller('GnDashboardSearchStatController', [
    '$scope', '$routeParams', '$http', '$translate', '$sce',
    function($scope, $routeParams, $http, $translate, $sce) {

      $scope.statistics = {md: {}, search: {
        mainSearchStatistics: {},
        terms: {},
        services: []
      }};

      // By default, provide search terms for this service
      $scope.termsForSearchService = 'q';
      $scope.currentField = 'any';

      // The first search date
      $scope.dateMin = null;
      // The last search date
      $scope.dateMax = null;

      // The beginning of the temporal range
      $scope.dateFrom = null;
      // The end of the temporal range
      $scope.dateTo = null;
      $scope.graphicType = 'DAY';



      function getMainSearchStat() {
        // Get core statistics for q service
        $http.get('statistics-search?_content_type=json&service=q')
        .success(function(data) {
              $scope.statistics.search.mainSearchStatistics.q =
                  (data === 'null' ? null : data);
            }).error(function(data) {
              // TODO
            });

        // Get core statistics for csw service
        $http.get('statistics-search?_content_type=json&service=csw')
        .success(function(data) {
              $scope.statistics.search.mainSearchStatistics.csw =
                  (data === 'null' ? null : data);
            }).error(function(data) {
              // TODO
            });

        $http.get('statistics-search-ip?_content_type=json')
        .success(function(data) {
              $scope.statistics.search.ip = data;
            }).error(function(data) {
              // TODO
            });
      };

      function getSearchStatByDate() {
        var byType = true;

        // Search by date statistics
        $http.get($scope.url +
                  'statistics-search-by-date?_content_type=json&' +
                  'dateFrom=' +
                    ($scope.dateFrom === null ? 'NULL' :
                      moment($scope.dateFrom).format('YYYY-MM-DD')) +
                  '&dateTo=' +
                    ($scope.dateTo === null ? 'NULL' :
                      moment($scope.dateTo).format('YYYY-MM-DD')) +
                  '&graphicType=' + $scope.graphicType +
                  '&byType=' + byType).success(function(data) {
          // Get min/max range
          $scope.dateMin = data.dateMin.substring(0, 10);
          $scope.dateMax = data.dateMax.substring(0, 10);

          // No date defined, get min and max of search range
          // and init dateFrom and dateTo to query full time range
          if ($scope.dateFrom === null) {
            $scope.dateFrom = new Date($scope.dateMin);
            $scope.dateTo = new Date($scope.dateMax);
            return;
          }

          if (!data.requests) {
            return;
          }

          // Format the data for multi bar chart
          $scope.statistics.search.temporal = [];

          // Retrieve all possible labels for all series
          // nvd3 require to have all values for all series
          // (with 0 when no data)
          // to stack series properly
          // TODO : Add all possible dates between min / max
          var xValues = [];
          for (var i = 0; i < data.requests.length; i++) {
            var r = data.requests[i];
            if (r.record) {
              if ($.isArray(r.record)) {
                for (var j = 0; j < r.record.length; j++) {
                  xValues.push(r.record[j].reqdate);
                }
              } else {
                xValues.push(r.record.reqdate);
                // Jeeves JSON output one xml child as an object and
                // n children as array of object. Convert all to array
                r.record = [r.record];
              }
            }
          }
          xValues.sort();

          // Build an array with all values for each series
          for (var i = 0; i < data.requests.length; i++) {
            var records = data.requests[i].record;
            if (records && records.length > 0) {
              var values = [];
              for (var j = 0; j < xValues.length; j++) {
                var valueFoundForSeries = false;
                for (var k = 0; k < records.length; k++) {
                  var record = records[k];
                  if (record.reqdate == xValues[j]) {
                    valueFoundForSeries = true;
                    values.push(record);
                    // The value exist for the serie - exit
                    k = records.length;
                  }
                }

                // Add a zero value if no data available for current x value
                if (!valueFoundForSeries) {
                  values.push({reqdate: xValues[j], number: 0});
                }
              }

              $scope.statistics.search.temporal.push({
                key: $translate(data.requests[i]['@service']),
                values: values
              });
            }
          }

          // No data
          if ($scope.statistics.search.temporal.length === 0) {
            return;
          }

          nv.addGraph(function() {
            var chart = nv.models.multiBarChart()
                      .x(function(d) { return d.reqdate; })
                      .y(function(d) { return parseInt(d.number); })
                      .stacked(true);
            //                      .rotateLabels(-90)


            chart.reduceXTicks(false).staggerLabels(true);

            chart.yAxis
                  .axisLabel('Number of requests')
                  .tickFormat(d3.format('.f'));

            d3.select('#gn-stat-search-timeline')
                    .datum($scope.statistics.search.temporal)
                    .transition().duration(500)
                    .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
          });
        }).error(function(data) {
          // TODO
        });
      };

      function getSearchStatByService() {
        // Search by service type statistics
        $http.get('statistics-search-by-service-type?_content_type=json')
        .success(function(data) {
              var total = 0;
              for (var i in data) {
                data[i].nbsearch = parseInt(data[i].nbsearch);
                total += data[i].nbsearch;
              }
              $scope.statistics.search.byServiceType = data;
              nv.addGraph(function() {
                var chart = nv.models.pieChart()
                         .x(function(d) { return $translate(d.service) })
                         .y(function(d) { return d.nbsearch})
                         .values(function(d) { return d})
                         .tooltips(true)
                         .tooltipContent(function(key, y, e, graph) {
                      // TODO : %age should be relative to
                      // the current set of displayed values
                      return '<h3>' + key + '</h3>' +
                             '<p>' + parseInt(y).toFixed() + ' ' +
                             $translate('searches') + ' (' +
                             (y / total * 100).toFixed() + '%)</p>';
                    })
                         .showLabels(true);

                d3.select('#gn-stat-search-by-service')
                       .datum([data])
                       .transition().duration(1200)
                       .call(chart);

                return chart;
              });
            }).error(function(data) {
              // TODO
            });
      };

      function getSearchStatForFieldsAndTerms() {
        $http.get('statistics-search-fields?_content_type=json&')
        .success(function(data) {
              $scope.statistics.search.fields = data;

              // Retrieve the list of search services
              for (var i in data) {
                var value = data[i].service;
                if ($scope.statistics.search.services.indexOf(value) === -1) {
                  $scope.statistics.search.services.push(value);
                }
              }
              $scope.statistics.search.services.sort();
            }).error(function(data) {
              // TODO
            });

        $scope.viewTermsForField = function(field, service) {
          $scope.currentField = field;
          $http.get('statistics-search-terms?_content_type=json&' +
                  'field=' + field +
                  '&service=' + service)
                  .success(function(data) {
                $scope.statistics.search.terms.any = data;
              }).error(function(data) {
                // TODO
              });
        };
        $scope.viewTermsForField($scope.currentField,
            $scope.termsForSearchService);
      }

      $scope.searchStatisticExport = function() {
        $http.get('statistics-search-export?tableToExport=requests')
        .success(function(data) {
              $scope.requestsExport = $sce.trustAsHtml(data);
            }).error(function(data) {
              // TODO
            });
        $http.get('statistics-search-export?tableToExport=params')
        .success(function(data) {
              $scope.paramsExport = $sce.trustAsHtml(data);
            }).error(function(data) {
              // TODO
            });
      };


      // Refresh graph when model change
      $scope.$watch('graphicType', getSearchStatByDate);
      $scope.$watch('dateFrom', getSearchStatByDate);
      $scope.$watch('dateTo', getSearchStatByDate);

      getMainSearchStat();
      getSearchStatByDate();
      getSearchStatByService();
      getSearchStatForFieldsAndTerms();


      // Status
    }]);

})();
