(function() {
  goog.provide('gn_pagination_directive');

  var module = angular.module('gn_pagination_directive', []);

  module.directive('gnPagination', ['hotkeys', '$translate',
                                    function(hotkeys, $translate) {

      return {
        restrict: 'A',
        replace: true,
        require: '^ngSearchForm',
        scope: {
          config: '=gnPagination',
          values: '=hitsValues'
        },
        templateUrl: '../../catalog/components/search/pagination/partials/' +
            'pagination.html',
        link: function(scope, element, attrs, controller) {

          // Init config from default and eventual given one
          var defaultConfig = {
            pages: -1,
            currentPage: 1,
            hitsPerPage: 3
          };
          angular.extend(defaultConfig, scope.config);
          scope.config = defaultConfig;
          delete defaultConfig;

          /**
           * If an object {paginationInfo} is defined inside the
           * SearchFormController, then add from and to  params
           * to the search.
           */
          var getPaginationParams = function() {
            var pageOptions = scope.config;
            return {
              from: (pageOptions.currentPage - 1) * pageOptions.hitsPerPage + 1,
              to: pageOptions.currentPage * pageOptions.hitsPerPage
            };
          };
          controller.getPaginationParams = getPaginationParams;

          scope.updateSearch = function(hitsPerPage) {
            if (hitsPerPage) {
              scope.config.hitsPerPage = hitsPerPage;
            }
            controller.updateSearchParams(getPaginationParams());
            controller.triggerSearch(true);
          };

          scope.previous = function() {
            if (scope.config.currentPage > 1) {
              scope.config.currentPage -= 1;
              scope.updateSearch();
            }
          };
          scope.next = function() {
            if (scope.config.currentPage < scope.config.pages) {
              scope.config.currentPage += 1;
              scope.updateSearch();
            }
          };
          scope.first = function() {
            scope.config.currentPage = 1;
            scope.updateSearch();
          };
          scope.last = function() {
            scope.config.currentPage = scope.config.pages;
            scope.updateSearch();
          };
          controller.activatePagination();

          hotkeys.bindTo(scope)
            .add({
                combo: 'ctrl+left',
                description: $translate('hotkeyFirstPage'),
                callback: scope.first
              }).add({
                combo: 'left',
                description: $translate('hotkeyPreviousPage'),
                callback: scope.previous
              }).add({
                combo: 'right',
                description: $translate('hotkeyNextPage'),
                callback: scope.next
              }).add({
                combo: 'ctrl+right',
                description: $translate('hotkeyLastPage'),
                callback: scope.last
              });

        }
      };
    }]);
})();
