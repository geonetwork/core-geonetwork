(function() {

  goog.provide('gn_selection_directive');

  var module = angular.module('gn_selection_directive', []);

  module.directive('gnSelectionWidget', [ 'gnHttp', function(gnHttp) {

    return {
      restrict: 'A',
      scope: true,
      templateUrl: '../../catalog/components/search/searchmanager/partials/' +
      'selection-widget.html',
      link: function(scope, element, attrs) {

        var ckb = $(element).find('.md-checkbox');
        var watchers = [];

        // initial state
        gnHttp.callService('mdSelect', {}).success(function(res) {
          scope.searchResults.selectedCount = parseInt(res[0], 10);
        });

        var updateCkb = function(records) {
          var checked = true;
          records.forEach(function(md){
            checked = checked && md['geonet:info'].selected;
          });
          ckb[0].checked = checked;
        };

        // set checkbox state on page change
        scope.$watchCollection('searchResults.records', function(records){
          var w;
          while(w = watchers.pop()) { w(); }
          updateCkb(records);
          records.forEach(function(record, i) {
            watchers.push(scope.$watch(
              'searchResults.records['+i+']["geonet:info"].selected',
              function() { updateCkb(scope.searchResults.records); }
            ));
          });
        });

        ckb.on('click', function() {
          scope.$apply(function(){
            scope.selectAllInPage(ckb[0].checked);
          });
        });

        scope.selectAllInPage = function(selected) {
          scope.searchResults.records.forEach(function(record) {
            record['geonet:info'].selected = selected;
            gnHttp.callService('mdSelect', {
              selected: selected ? 'add' : 'remove',
              id: record.getUuid()
            }).success(function(res) {
              var fn = (selected) ? Math.max : Math.min;
              scope.searchResults.selectedCount = fn(
                scope.searchResults.selectedCount,
                parseInt(res[0], 10));
            });
          });
        };

        scope.selectAll = function() {
          gnHttp.callService('mdSelect', {
            selected: 'add-all'
          }).success(function(res) {
            scope.searchResults.selectedCount = parseInt(res[0], 10);
            scope.searchResults.records.forEach(function(record) {
              record['geonet:info'].selected = true;
            });
          });
        };

        scope.unSelectAll = function() {
          gnHttp.callService('mdSelect', {
            selected: 'remove-all'
          }).success(function(res) {
            scope.searchResults.selectedCount = parseInt(res[0], 10);
            scope.searchResults.records.forEach(function(record) {
              record['geonet:info'].selected = false;
            });
          });
        };

      }
    };

  }]);

  module.directive('gnSelectionMd', [ 'gnHttp', function(gnHttp) {

    return {
      restrict: 'A',
      link: function(scope, element, attrs) {

        scope.change = function() {
          gnHttp.callService('mdSelect', {
            selected: element[0].checked ? 'add' : 'remove',
            id: scope.md.getUuid()
          }).success(function(res) {
            scope.searchResults.selectedCount = parseInt(res[0], 10);
          });
        };

      }
    };

  }]);


})();
