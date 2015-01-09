(function() {

  goog.provide('gn_selection_directive');

  var module = angular.module('gn_selection_directive', []);

  module.directive('gnSelectionWidget', [
    '$translate', 'hotkeys',
    'gnHttp', 'gnMetadataActions',
    function($translate, hotkeys,
             gnHttp, gnMetadataActions) {

      return {
        restrict: 'A',
        scope: true,
        templateUrl: '../../catalog/components/search/resultsview/partials/' +
            'selection-widget.html',
        link: function(scope, element, attrs) {

          var watchers = [];
          scope.checkAll = true;
          scope.mdService = gnMetadataActions;

          // initial state
          gnHttp.callService('mdSelect', {}).success(function(res) {
            scope.searchResults.selectedCount = parseInt(res[0], 10);
          });

          var updateCkb = function(records) {
            var checked = true;
            records.forEach(function(md) {
              checked = checked && md['geonet:info'].selected;
            });
          };

          // set checkbox state on page change
          scope.$watchCollection('searchResults.records', function(records) {
            var w;
            while (w = watchers.pop()) { w(); }
            updateCkb(records);
            records.forEach(function(record, i) {
              watchers.push(scope.$watch(
                  'searchResults.records[' + i + ']["geonet:info"].selected',
                  function() { updateCkb(scope.searchResults.records); }
                  ));
            });
          });

          scope.select = function() {
            scope.checkAll = !scope.checkAll;
            if (scope.checkAll) {
              scope.selectAll();
            } else {
              scope.unSelectAll();
            }
          };
          scope.getIcon = function() {
            if (scope.searchResults.selectedCount === 0) {
              return 'fa-square-o';
            } else if (scope.searchResults.selectedCount ==
                scope.searchResults.count) {
              return 'fa-check-square-o';
            } else {
              return 'fa-minus-square-o';
            }
          };

          scope.selectAllInPage = function(selected) {
            var params = {
              selected: selected ? 'add' : 'remove',
              id: []
            };
            scope.searchResults.records.forEach(function(record) {
              params.id.push(record.getUuid());
              record['geonet:info'].selected = selected;
            });

            gnHttp.callService('mdSelect', params, {
              method: 'POST'
            }).success(function(res) {
              scope.searchResults.selectedCount = parseInt(res[0], 10);
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
          hotkeys.bindTo(scope)
            .add({
                combo: 'a',
                description: $translate('hotkeySelectAll'),
                callback: scope.selectAll
              }).add({
                combo: 'p',
                description: $translate('hotkeySelectAllInPage'),
                callback: function() {
                  scope.selectAllInPage(true);
                }
              }).add({
                combo: 'n',
                description: $translate('hotkeyUnSelectAll'),
                callback: scope.unSelectAll
              });

          scope.$on('mdSelectAll', scope.selectAll);
          scope.$on('mdSelectNone', scope.unSelectAll);

        }
      };

    }]);

  module.directive('gnSelectionMd', ['gnHttp', function(gnHttp) {

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
