(function() {
  goog.provide('gn_language_switcher_directive');

  var module = angular.module('gn_language_switcher_directive',
      ['pascalprecht.translate']);

  module.directive('gnLanguageSwitcher', ['$translate',
    function($translate) {

      return {
        restrict: 'A',
        replace: false,
        transclude: true,
        scope: {
          langs: '=langs',
          lang: '=gnLanguageSwitcher'
        },
        template:
            '<select class="form-control" ng-model="lang" ' +
            'ng-options="key as (key | translate) for (key, value) in ' +
            'langs">' +
                '</select>',
        link: function(scope, element, attrs) {
          scope.$watch('lang', function(value) {
            var url = location.href.split('/');
            if (value !== url[5]) {
              url[5] = value;  // Use ISO3 code
              // if (window.history.pushState) {
              //     // Update translate with no page reload
              //     // And adding an history state to update browser URL
              //     $translate.uses(scope.langs[value]); // Use ISO2 code
              //     window.history.pushState(null, null, url.join('/'));
              // } else {
              // trigger a reload
              location.href = url.join('/');
              // }
              if (moment) {
                moment.lang(scope.langs[value]);
              }
            }
          });
        }
      };
    }]);
})();
