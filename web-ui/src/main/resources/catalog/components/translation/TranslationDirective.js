(function() {
  goog.provide('gn_translation_directive');

  var module = angular.module('gn_translation_directive', [
    'pascalprecht.translate'
  ]);

  module.directive('gnTranslationSelector',
      ['$translate', function($translate) {
        return {
          restrict: 'A',
          replace: true,
           scope: {
             options: '=gnTranslationSelectorOptions'
           },
           template:
               '<select ng-model="lang" ' +
                   'ng-options="l.value as l.label for l in ' +
                       'options.langs">' +
               '</select>',
          link: function(scope, element, attrs) {
            scope.$watch('lang', function(value) {
              $translate.uses(value).then(angular.noop, function(lang) {
                // failed to load lang from server, fallback to default code.
                scope.lang = scope.options.fallbackCode;
              });
            });
            
            scope.lang = $translate.preferredLanguage();
          }
        };
      }]);
})();
