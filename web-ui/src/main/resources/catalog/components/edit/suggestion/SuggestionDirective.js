(function() {
  goog.provide('gn_suggestion_directive');

  /**
   * Provide directives for suggestions of the
   * edited metadata.
   *
   * - gnSuggestionList
   */
  angular.module('gn_suggestion_directive', [])
  .directive('gnSuggestionList', ['gnSuggestion', 'gnCurrentEdit',
        function(gnSuggestion, gnCurrentEdit) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/suggestion/' +
                'partials/list.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.gnSuggestion = gnSuggestion;
              scope.gnCurrentEdit = gnCurrentEdit;
              scope.suggestions = [];
              scope.loading = false;

              scope.load = function() {
                scope.loading = true;
                scope.suggestions = [];
                gnSuggestion.load(scope.$parent.lang || 'eng').
                    success(function(data) {
                      scope.loading = false;
                      if (data && !angular.isString(data)) {
                        scope.suggestions = data;
                      }
                      else {
                        scope.suggestions = [];
                      }
                    });
              };

              // Reload suggestions list when a directive requires it
              scope.$watch('gnSuggestion.reload', function() {
                if (scope.gnSuggestion.reload) {
                  scope.load();
                  scope.gnSuggestion.reload = false;
                }
              });

              // When saving is done, refresh validation report
              // scope.$watch('gnCurrentEdit.saving', function(newValue) {
              //   if (newValue === false) {
              //     scope.load();
              //   }
              // });
            }
          };
        }])
  .directive('gnRunSuggestion', ['gnSuggestion',
        function(gnSuggestion) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/suggestion/' +
                'partials/runprocess.html',
            link: function(scope, element, attrs) {
              scope.gnSuggestion = gnSuggestion;
              // Indicate if processing is running
              scope.processing = false;
              // Indicate if one process is complete
              scope.processed = false;
              /**
               * Init form parameters.
               * This function is registered to be called on each
               * suggestion click in the suggestions list.
               */
              var initParams = function() {
                scope.params = {};
                scope.currentSuggestion = gnSuggestion.getCurrent();
                var p = scope.currentSuggestion.params;
                for (key in p) {
                  scope.params[key] = p[key].defaultValue;
                }
              };

              scope.runProcess = function() {
                scope.processing = true;
                gnSuggestion.runProcess(
                    gnSuggestion.getCurrent()['@process'],
                    scope.params).then(function() {
                  scope.processing = false;
                  scope.processed = true;
                });
              };
              gnSuggestion.register(initParams);
            }
          };
        }]);
})();
