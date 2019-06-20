/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
          expanded: '@',
          currentLanguage: '=?'
        },
        link: function(scope, element, attrs) {
          // Only inputs and textarea could be multilingual fields
          var formFieldsSelector =
              'div[data-ng-transclude] > input.form-control,' +
              'div[data-ng-transclude] > textarea.form-control,' +
              // + selector for field using directive eg. gn-keyword-picker
              'div[data-ng-transclude] > span > input.form-control,' +
              'div[data-ng-transclude] > span > textarea.form-control';

          // Some input should be displayed in Right-To-Left direction
          var rtlLanguages = ['AR'];

          // Get languages from attributes (could be grab from the
          // form field ? FIXME)
          scope.languages = angular.fromJson(attrs.gnMultilingualField);
          var mainLanguage = scope.mainLanguage;
          // Have to map the main language to one of the languages in the inputs
          if (angular.isDefined(scope.languages[mainLanguage])) {
            mainLanguage = scope.languages[mainLanguage].substring(1);
          } else {
            $(element).find(formFieldsSelector).each(function() {
              var lang = $(this).attr('lang');
              if (angular.isDefined(scope.languages[lang])) {
                mainLanguage = scope.languages[lang].substring(1);
              }
            });
          }

          if (!mainLanguage) {
            // when there is a gco:CharacterString and there is no
            // PT_FreeText with the same language
            // then the scope.languages map has an entry mainLanguage -> #
            // but the problem is that the input element will have the 'lang'
            // attribute to be eng (not empty string).  So
            // we need to update the map and main language to be '#' +
            // scope.mainLanguage so that all the looks ups
            // can be done correctly.
            mainLanguage = scope.mainLanguage;
            scope.languages[mainLanguage] = '#' + mainLanguage;
          }

          scope.hasData = {};

          scope.currentLanguage = mainLanguage;

          /**
           * Get the 3 letter code set in codeListValue
           * from a lang identifier eg. "EN"
           */
          function getISO3Code(langId) {
            var langCode = null;
            angular.forEach(scope.languages,
                function(key, value) {
                  if (key === '#' + langId) {
                    langCode = value;
                  }
                }
            );
            return langCode;
          }

          $timeout(function() {
            scope.expanded = scope.expanded === 'true';

            $(element).find(formFieldsSelector).each(function() {
              var inputEl = $(this);
              var langId = inputEl.attr('lang');

              // FIXME : should not be the id but the ISO3Code
              if (langId) {
                // Add the language label
                inputEl.before('<span class="label label-primary">' +
                    $translate.instant(getISO3Code(langId)) + '</span>');

                // Set the direction attribute
                if ($.inArray(langId, rtlLanguages) !== -1) {
                  inputEl.attr('dir', 'rtl');
                }

                var setNoDataClass = function() {
                  var code = ('#' + langId);
                  scope.hasData[code] = inputEl.val().trim().length > 0;
                };

                inputEl.on('keyup', setNoDataClass);

                setNoDataClass();
              }
            });

            // By default, do not expand fields
            scope.displayAllLanguages(scope.expanded);
          });

          scope.switchToLanguage = function(langId) {
            scope.currentLanguage = langId.replace('#', '');
            $(element).find(formFieldsSelector).each(function() {
              if ($(this).attr('lang') === scope.currentLanguage ||
                  ($(this).attr('lang') === mainLanguage &&
                  scope.currentLanguage === '')) {
                $(this).removeClass('hidden').focus();
              } else {
                $(this).addClass('hidden');
              }
            });
          };

          var setLabel = function(key) {
            scope.languageSwitchLabel = $translate.instant(key);
            scope.languageSwitchHelp = $translate.instant(key + '-help');
          };

          scope.displayAllLanguages = function(force, focus) {
            scope.expanded =
                force !== undefined ? force : !scope.expanded;

            $(element).find(formFieldsSelector).each(function() {
              if (scope.expanded) {
                setLabel('oneLanguage');
                $(this).prev('span').removeClass('hidden');
                var el = $(this).removeClass('hidden');
                if (focus) {
                  el.focus();
                }
              } else {
                setLabel('allLanguage');
                $(this).prev('span').addClass('hidden');

                if ($(this).attr('lang') !== mainLanguage) {
                  $(this).addClass('hidden');
                } else {
                  scope.currentLanguage = mainLanguage;
                  var el = $(this).removeClass('hidden');
                  if (focus) {
                    el.focus();
                  }
                }
              }
            });
          };
        }
      };
    }]);
})();
