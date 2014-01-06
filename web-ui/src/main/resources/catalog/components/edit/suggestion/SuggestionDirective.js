(function() {
  goog.provide('gn_suggestion_directive');

  /**
   * Provide directives for suggestions of the 
   * edited metadata.
   *
   * - gnSuggestionList
   */
  angular.module('gn_suggestion_directive', [])
  .directive('gnSuggestionList', ['gnSuggestion',
        function(gnSuggestion) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/suggestion/' +
                'partials/list.html',
            scope: {},
            link: function(scope, element, attrs) {
              gnSuggestion.load().success(function(data){
                scope.suggestions = data;
              });
            }
          };
        }]);
})();
