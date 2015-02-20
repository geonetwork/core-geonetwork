(function() {
  goog.provide('gn_multilingual_field_directive');

  var module = angular.module('gn_multilingual_field_directive', []);

  /**
   * Decorate a set of multilingual inputs or textareas with:
   * * a button to switch from language selector to a display
   * all languages mode
   * * a list of language to display the matching input.
   *
   * On initialization, a language label is inserted before each
   * fields.
   *
   * It also set direction attribute for RTL language.
   *
   */
  module.directive('gnMultilingualField', ['$timeout', '$translate',
    function($timeout, $translate) {

      return {
        restrict: 'A',
        transclude: true,
        templateUrl: '../../catalog/components/edit/' +
            'multilingualfield/partials/multilingualfield.html',
        scope: {
          mainLanguage: '@',
          expanded: '@'
        },
        link: function(scope, element, attrs) {
          // Only inputs and textarea could be multilingual fields
          var formFieldsSelector = 'input,textarea';

          // Some input should be displayed in Right-To-Left direction
          var rtlLanguages = ['ara'];

          // Get languages from attributes (could be grab from the
          // form field ? FIXME)
          scope.languages = angular.fromJson(attrs.gnMultilingualField);
          scope.currentLanguage = scope.mainLanguage;
          scope.mainLangInOtherLang =
              angular.isDefined(scope.languages[scope.mainLanguage]);
          /**
           * Get the 3 letter code set in codeListValue
           * from a lang identifier eg. "EN"
           */
          function getISO3Code(langId) {
            var langCode = null;
            if (langId === scope.mainLanguage) {
              return scope.mainLanguage;
            }
            angular.forEach(scope.languages,
                function(key, value) {
                  if (key === '#' + langId) {
                    langCode = value;
                    return;
                  }
                }
            );
            return langCode;
          }

          $timeout(function() {
            scope.expanded = scope.expanded === 'true';

            $(element).find(formFieldsSelector).each(function() {
              var langId = $(this).attr('lang');

              // FIXME : should not be the id but the ISO3Code
              if (langId) {
                // Add the language label
                $(this).before('<span class="label label-primary">' +
                    $translate(getISO3Code(langId)) + '</span>');

                // Set the direction attribute
                if ($.inArray(langId, rtlLanguages) !== -1) {
                  $(this).attr('dir', 'rtl');
                }
              }
            });

            // By default, do not expand fields
            scope.displayAllLanguages(scope.expanded);
          });

          scope.switchToLanguage = function(langId) {
            scope.currentLanguage = langId.replace('#', '');
            $(element).find(formFieldsSelector).each(function() {
              if ($(this).attr('lang') === scope.currentLanguage ||
                  ($(this).attr('lang') === scope.mainLanguage &&
                  scope.currentLanguage === '')) {
                $(this).removeClass('hidden').focus();
              } else {
                $(this).addClass('hidden');
              }
            });
          };

          var setLabel = function(key) {
            scope.languageSwitchLabel = $translate(key);
            scope.languageSwitchHelp = $translate(key + '-help');
          };

          scope.displayAllLanguages = function(force) {
            scope.expanded =
                force !== undefined ? force : !scope.expanded;

            $(element).find(formFieldsSelector).each(function() {
              if (scope.expanded) {
                setLabel('oneLanguage');
                $(this).prev('span').removeClass('hidden');
                $(this).removeClass('hidden').focus();
              } else {
                setLabel('allLanguage');
                $(this).prev('span').addClass('hidden');
                if ($(this).attr('lang') !== scope.mainLanguage) {
                  $(this).addClass('hidden');
                } else {
                  scope.currentLanguage = scope.mainLanguage;
                  $(this).removeClass('hidden');
                }
              }
            });
          };
        }
      };
    }]);
})();
