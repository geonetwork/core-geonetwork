(function() {

  goog.provide('gn_search_default_directive');

  var module = angular.module('gn_search_default_directive', []);

  module.directive('gnInfoList', ['gnMdView',
    function(gnMdView) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/js/custom/default/' +
            'partials/infolist.html',
        link: function linkFn(scope, element, attr) {
          scope.showMore = function(isDisplay) {
            var div = $('#gn-info-list' + this.md.getUuid());
            $(div.children()[isDisplay ? 0 : 1]).addClass('hidden');
            $(div.children()[isDisplay ? 1 : 0]).removeClass('hidden');
          };
          scope.go = function(uuid) {
            gnMdView(index, md, records);
            gnMdView.setLocationUuid(uuid);
          };
        }
      };
    }
  ]);


  module.directive('gnMdActionsMenu', ['gnMetadataActions',
    function(gnMetadataActions) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/js/custom/default/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gnMetadataActions);
        }
      };
    }
  ]);

  module.directive('gnPeriodChooser', [
    function() {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/js/custom/default/' +
            'partials/periodchooser.html',
        scope: {
          label: '@gnPeriodChooser',
          dateFrom: '=',
          dateTo: '='
        },
        link: function linkFn(scope, element, attr) {
          var today = moment();
          scope.format = 'YYYY-MM-DD';
          scope.options = ['today', 'yesterday', 'thisWeek', 'thisMonth',
            'last3Months', 'last6Months', 'thisYear'];
          scope.setPeriod = function(option) {
            if (option === 'today') {
              var date = today.format(scope.format);
              scope.dateFrom = date;
            } else if (option === 'yesterday') {
              var date = today.clone().subtract(1, 'day')
                .format(scope.format);
              scope.dateFrom = date;
              scope.dateTo = date;
              return;
            } else if (option === 'thisWeek') {
              scope.dateFrom = today.clone().startOf('week')
                .format(scope.format);
            } else if (option === 'thisMonth') {
              scope.dateFrom = today.clone().startOf('month')
                .format(scope.format);
            } else if (option === 'last3Months') {
              scope.dateFrom = today.clone().startOf('month').
                  subtract(3, 'month').format(scope.format);
            } else if (option === 'last6Months') {
              scope.dateFrom = today.clone().startOf('month').
                  subtract(6, 'month').format(scope.format);
            } else if (option === 'thisYear') {
              scope.dateFrom = today.clone().startOf('year')
                .format(scope.format);
            }
            scope.dateTo = today.format(scope.format);
          };
        }
      };
    }
  ]);
  module.directive('gnTimeFilter', ['$timeout',
    function($timeout) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/js/custom/default/' +
            'partials/timefilter.html',
        link: function linkFn(scope, element, attr) {
          var container = $(element).find('svg');
          scope.listOfSelectedYears = [];
          scope.unselectYear = function(year) {
            scope.listOfSelectedYears.splice(
                scope.listOfSelectedYears.indexOf(year), 1
            );
            scope.selectYear();
          };
          scope.selectYear = function(year) {
            $timeout(function() {
              if (year) {
                scope.listOfSelectedYears.push(year);
              }
              angular.extend(scope.searchObj.params, {
                createDateYear: scope.listOfSelectedYears.join(' or ')
              });
            });
          };
          scope.$watchCollection('searchObj', function() {
            if (scope.searchObj.params.createDateYear) {

            } else {
              scope.listOfSelectedYears = [];
            }
          });
          scope.$watch('searchInfo', function() {
            scope.listOfYears = scope.searchInfo.facet &&
                scope.searchInfo.facet.createDateYears;
            if (scope.listOfYears) {
              var data = [];
              $.each(scope.listOfYears, function(index, value) {
                if (value['@count']) {
                  var count = parseInt(value['@count']);
                  data.push({label: value['@name'], count: count});
                }
              });
              function compare(a, b) {
                if (a.label < b.label)
                  return -1;
                if (a.label > b.label)
                  return 1;
                return 0;
              }
              data.sort(compare);

              nv.addGraph(function() {
                var chart = nv.models.discreteBarChart()
                  .x(function(d) { return d.label })
                  .y(function(d) { return d.count })
                  .staggerLabels(true)
                  .tooltips(false);
                //.showLabels(true);
                //d3.select("svg")
                d3.select(container.get(0))
                  .datum([{values: data}])
                  .call(chart);

                nv.utils.windowResize(chart.update);
                chart.discretebar.dispatch.on('elementClick', function(e) {
                  scope.selectYear(e.point.label);
                });
                chart.discretebar.dispatch.on('legendClick', function(e) {
                  scope.selectYear(e.point.label);
                });
                return chart;
              });
            }
          });
        }
      };
    }
  ]);
})();
