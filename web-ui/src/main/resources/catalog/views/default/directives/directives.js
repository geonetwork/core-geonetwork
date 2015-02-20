(function() {

  goog.provide('gn_search_default_directive');

  var module = angular.module('gn_search_default_directive', []);

  module.directive('gnInfoList', ['gnMdView',
    function(gnMdView) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
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

  module.directive('gnAttributeTableRenderer', ['gnMdView',
    function(gnMdView) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
        'partials/attributetable.html',
        scope: {
          attributeTable: '=gnAttributeTableRenderer'
        }
      };
    }
  ]);

  module.directive('gnMdActionsMenu', ['gnMetadataActions',
    function(gnMetadataActions) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gnMdActionsMenu);
          
          scope.$watch(attrs.gnMdActionsMenu, function(a) {
            scope.md = a;
          });
          
          scope.isWorkflowEnabled = function(md) {
            var st = md.status;
            var res = st && 
                  //Status is unknown
                  (!isNaN(st) && st != '0');
            
            //What if it is an array: gmd:MD_ProgressCode
            if(!res && Array.isArray(st)) {
              angular.forEach(st, function(s) {
                if(!isNaN(s) && s != '0') {
                  res = true;
                }
              });
            }
            
            return res;

          };
        }
      };
    }
  ]);

  module.directive('gnPeriodChooser', [
    function() {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
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
              scope.dateTo = today.format(scope.format);
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
            scope.dateTo = today.add(1, 'day').format(scope.format);
          };
        }
      };
    }
  ]);
})();
