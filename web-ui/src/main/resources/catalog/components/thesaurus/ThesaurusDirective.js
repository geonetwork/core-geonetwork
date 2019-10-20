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
  goog.provide('gn_thesaurus_directive');

  var module = angular.module('gn_thesaurus_directive',
      ['pascalprecht.translate']);

  /**
   * @ngdoc directive
   * @name gn_thesaurus.directive:gnThesaurusSelector
   *
   * @description
   * The thesaurus selector is available in 2 modes:
   *
   * One is composed of a drop down list
   * of thesaurus available in the catalog. On selection,
   * an empty XML fragment is requested and added to the form
   * before the editor is saved and refreshed. This mode
   * should be used in the metadata editor.
   *
   * When selectorOnly attribute is set, then only a dropdown
   * containing the list of thesaurus is displayed. In this
   * mode, the property thesaurusKey in the scope of the parent
   * is modified when a thesaurus is selected.
   *
   */
  module.directive('gnThesaurusSelector',
      ['$timeout',
       'gnThesaurusService', 'gnEditor',
       'gnEditorXMLService', 'gnCurrentEdit', '$rootScope', '$translate',
       function($timeout,
                gnThesaurusService, gnEditor,
                gnEditorXMLService, gnCurrentEdit, $rootScope, $translate) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnThesaurusSelector',
             elementName: '@',
             elementRef: '@',
             freekeywordElementName: '@',
             freekeywordElementRef: '@',
             domId: '@',
             selectorOnly: '@',
             transformation: '@',
             // Comma separated values of thesaurus keys
             include: '@'
           },
           templateUrl: '../../catalog/components/thesaurus/' +
           'partials/thesaurusselector.html',
           link: function(scope, element, attrs) {
             scope.thesaurus = null;
             scope.thesaurusKey = null;
             scope.snippet = null;
             scope.snippetRef = null;
             var restrictionList = scope.include ? (
             scope.include.indexOf(',') !== -1 ?
             scope.include.split(',') : [scope.include]) : [];

             var includeThesaurus = [];
             var excludeThesaurus = [];
             for (var i = 0; i < restrictionList.length; i++) {
               var t = restrictionList[i];
               if (t.indexOf('-') === 0) {
                 excludeThesaurus.push(t.substring(1));
               } else {
                 includeThesaurus.push(t);
               }
             }


             scope.allowFreeTextKeywords =
             (attrs.allowFreeTextKeywords === undefined) ||
             (attrs.allowFreeTextKeywords == 'true');

             // TODO: Remove from list existing thesaurus
             // in the record ?
             gnThesaurusService.getAll().then(
             function(listOfThesaurus) {
               scope.thesaurus = [];
               angular.forEach(listOfThesaurus, function(thesaurus) {
                 if (excludeThesaurus.length > 0 &&
                 $.inArray(thesaurus.getKey(), excludeThesaurus) !== -1) {

                 } else if (includeThesaurus.length == 0 || (
                 includeThesaurus.length > 0 &&
                 $.inArray(thesaurus.getKey(), includeThesaurus) !== -1)) {
                   scope.thesaurus.push(thesaurus);
                 }
               });
             });

             scope.add = function() {
               return gnEditor.add(gnCurrentEdit.id,
                   scope.freekeywordElementRef || scope.elementRef,
                 scope.freekeywordElementName || scope.elementName,
                        scope.domId, 'before').then(function() {
                 gnEditor.save(gnCurrentEdit.id, true);
               });
             };

             scope.addThesaurus = function(thesaurusIdentifier) {
               if (scope.selectorOnly) {
                 scope.$parent.thesaurusKey = scope.thesaurusKey =
                 thesaurusIdentifier;
               } else {
                 gnCurrentEdit.working = true;
                 return gnThesaurusService
                  .getXML(thesaurusIdentifier, null,
                 attrs.transformation).then(
                 function(data) {
                   // Add the fragment to the form
                   scope.snippet = data;
                   scope.snippetRef = gnEditor.buildXMLFieldName(
                   scope.elementRef,
                   scope.elementName);


                   $timeout(function() {
                     // Save the metadata and refresh the form
                     gnEditor.save(gnCurrentEdit.id, true).then(function() {
                       // success. Nothing to do.
                     }, function(rejectedValue) {
                       $rootScope.$broadcast('StatusUpdated', {
                         title: $translate.instant('runServiceError'),
                         error: rejectedValue,
                         timeout: 0,
                         type: 'danger'
                       });
                     });
                   });
                 });
               }
               return false;
             };
           }
         };
       }]);


  /**
   * @ngdoc directive
   * @name gn_thesaurus.directive:gnKeywordSelector
   *
   * @description
   * The thesaurus selector is available in 2 modes:
   * The keyword selector could be of 2 types:
   * 1) composed of an input with autocompletion. Each tags
   * added to the input
   *
   * 2) 2 selection lists: one with the thesaurus search
   * response, the other with the selection.
   *
   * Each time a keyword is selected, the server is
   * requested to provide the corresponding snippet
   * for the thesaurus.
   *
   * TODO: explain transformation
   */
  module.directive('gnKeywordSelector',
      ['$compile', '$timeout', '$translate',
       'gnThesaurusService', 'gnEditor',
       'Keyword', 'gnLangs',
       function($compile, $timeout, $translate,
                gnThesaurusService, gnEditor, Keyword, gnLangs) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             elementRef: '@',
             thesaurusKey: '@',
             keywords: '@',
             transformations: '@',
             currentTransformation: '@',
             lang: '@',
             orderById: '@',
             textgroupOnly: '@',

             // Max number of tags allowed. Use 1 to restrict to only
             // on keyword.
             maxTags: '@',
             thesaurusTitle: '@'
           },
           templateUrl: '../../catalog/components/thesaurus/' +
           'partials/keywordselector.html',
           link: function(scope, element, attrs) {
             $compile(element.contents())(scope);
             // pick up skos browser directive with compiler

             scope.max = gnThesaurusService.DEFAULT_NUMBER_OF_RESULTS;
             scope.filter = null;
             scope.results = null;
             scope.snippet = null;
             scope.isInitialized = false;
             scope.elementRefBackup = scope.elementRef;
             scope.invalidKeywordMatch = false;
             scope.selected = [];
             scope.initialKeywords = [];
             if (scope.keywords) {
               var buffer = '';
               for (var i = 0; i < scope.keywords.length; i++) {
                 var next = scope.keywords.charAt(i);
                 if (next !== ',') {
                   buffer += next;
                 } else if (i === scope.keywords.length - 1 ||
                 scope.keywords.charAt(i + 1) === ',') {
                   buffer += next;
                   i++;
                 } else {
                   scope.initialKeywords.push(buffer);
                   buffer = '';
                 }
               }
               if (buffer.length > 0) {
                 scope.initialKeywords.push(buffer);
               }
             }
             scope.transformationLists =
             scope.transformations.indexOf(',') !== -1 ?
             scope.transformations.split(',') : [scope.transformations];
             scope.maxTagsLabel = scope.maxTags || '∞';

             //Get langs of metadata
             var langs = [];
             for (var p in JSON.parse(scope.lang)) {
               langs.push(p);
             }
             scope.mainLang = langs[0];
             scope.langs = langs.join(',');

             // Check initial keywords are available in the thesaurus
             scope.sortKeyword = function(a, b) {
               if (a.getLabel().toLowerCase() <
               b.getLabel().toLowerCase()) {
                 return -1;
               }
               if (a.getLabel().toLowerCase() >
               b.getLabel().toLowerCase()) {
                 return 1;
               }
               return 0;
             };
             scope.resetKeywords = function() {
               scope.selected = [];
               scope.elementRef = scope.elementRefBackup;
               scope.invalidKeywordMatch = false;
               checkState();
             };


             var init = function() {

               // Nothing to load - init done
               scope.isInitialized = scope.initialKeywords.length === 0;

               // If no keyword, set the default transformation
               if (
               $.inArray(scope.currentTransformation,
               scope.transformationLists) === -1 &&
               scope.initialKeywords.length === 0) {

                 scope.setTransformation(scope.transformationLists[0]);
               }
               if (scope.isInitialized) {
                 checkState();
               } else {

                 // Check that all initial keywords are in the thesaurus
                 var counter = 0;
                 angular.forEach(scope.initialKeywords, function(keyword) {
                   // One keyword only and exact match search
                   // in current editor language.
                   gnThesaurusService.getKeywords(keyword,
                   scope.thesaurusKey, gnLangs.current, 1, 'MATCH')
                   .then(function(listOfKeywords) {
                     counter++;

                     listOfKeywords[0] &&
                     scope.selected.push(listOfKeywords[0]);
                     // Init done when all keywords are selected
                     if (counter === scope.initialKeywords.length) {
                       scope.isInitialized = true;
                       scope.invalidKeywordMatch =
                       scope.selected.length !== scope.initialKeywords.length;

                       // Get the matching XML snippet for
                       // the initial set of keywords
                       // once the loaded keywords are all selected.
                       checkState();
                     }
                   });
                 });
               }

               // Then register search filter change
               scope.$watch('filter', search);
             };

             // Used by skos-browser to add keywords from the
             // skos hierarchy to the current list of tags
             scope.addThesaurusConcept = function(uri, text) {
               var textArr = [];
               textArr['#text'] = text;
               var k = {
                 uri: uri,
                 value: textArr
               };
               var keyword = new Keyword(k);

               var thisId = '#tagsinput_' + scope.elementRef;
               // Add to tags
               $(thisId).tagsinput('add', keyword);

               // Update selection and snippet
               angular.copy($(thisId).tagsinput('items'), scope.selected);
               getSnippet(); // FIXME: should not be necessary
               // as there is a watch on it ?

               // Clear typeahead
               $(thisId).tagsinput('input').typeahead('val', '');
             };

             // Init typeahead and tag input
             var initTagsInput = function() {
               var id = '#tagsinput_' + scope.elementRef;
               $timeout(function() {
                 try {
                   $(id).tagsinput({
                     itemValue: 'label',
                     itemText: 'label',
                     maxTags: scope.maxTags
                   });

                   // Add selection to the list of tags
                   angular.forEach(scope.selected, function(keyword) {
                     $(id).tagsinput('add', keyword);
                   });

                   // Load all keywords from thesaurus on startup
                   gnThesaurusService.getKeywords('',
                   scope.thesaurusKey, gnLangs.current, scope.max)
                    .then(function(listOfKeywords) {

                     var field = $(id).tagsinput('input');
                     field.attr('placeholder',
                     $translate.instant('searchKeyword'));

                     var keywordsAutocompleter =
                     gnThesaurusService.getKeywordAutocompleter({
                       thesaurusKey: scope.thesaurusKey,
                       dataToExclude: scope.selected,
                       lang: gnLangs.current,
                       orderById: scope.orderById
                     });

                     // Init typeahead
                     field.typeahead({
                       minLength: 0,
                       highlight: true
                       // template: '<p>{{label}}</p>'
                       // TODO: could be nice to have definition
                     }, {
                       name: 'keyword',
                       displayKey: 'label',
                       limit: Infinity,
                       source: keywordsAutocompleter.ttAdapter()
                     }).bind('typeahead:selected',
                     $.proxy(function(obj, keyword) {
                       // Add to tags
                       this.tagsinput('add', keyword);

                       // Update selection and snippet
                       angular.copy(this.tagsinput('items'), scope.selected);
                       getSnippet(); // FIXME: should not be necessary
                       // as there is a watch on it ?

                       // Clear typeahead
                       this.tagsinput('input').typeahead('val', '');
                     }, $(id))
                     );

                     $(id).on('itemRemoved', function() {
                       angular.copy($(this)
                       .tagsinput('items'), scope.selected);
                       getSnippet();
                     });
                   });
                 } catch (e) {
                   console.warn('No tagsinput for ' + id +
                   ', error: ' + e.message);
                 }
               });
             };

             var checkState = function() {
               if (scope.isInitialized && !scope.invalidKeywordMatch) {
                 getSnippet();

                 scope.$watch('results', getSnippet);
                 scope.$watch('selected', getSnippet);

                 initTagsInput();
               } else if (scope.invalidKeywordMatch) {
                 // invalidate element ref to not trigger
                 // an update of the record with an invalid
                 // state ie. keywords not loaded properly
                 scope.elementRef = '';
               }
             };


             var search = function() {
               gnThesaurusService.getKeywords(scope.filter,
               scope.thesaurusKey, gnLangs.current, scope.max)
                .then(function(listOfKeywords) {
                 // Remove from search already selected keywords
                 scope.results = $.grep(listOfKeywords, function(n) {
                   var alreadySelected = true;
                   if (scope.selected.length !== 0) {
                     alreadySelected = $.grep(scope.selected, function(s) {
                       return s.getLabel() === n.getLabel();
                     }).length === 0;
                   }
                   return alreadySelected;
                 });
               });
             };
             scope.setTransformation = function(t) {
               $timeout(function() {
                 scope.currentTransformation = t;
                 getSnippet();
               });
               return false;
             };
             scope.isCurrent = function(t) {
               return t === scope.currentTransformation;
             };
             var getKeywordIds = function() {
               var ids = [];
               angular.forEach(scope.selected, function(k) {
                 ids.push(k.getId());
               });
               return ids;
             };

             var getSnippet = function() {
               gnThesaurusService
                .getXML(scope.thesaurusKey,
               getKeywordIds(), scope.currentTransformation, scope.langs,
                   scope.textgroupOnly).then(
               function(data) {
                 scope.snippet = data;
               });
             };

             if (scope.thesaurusKey) {
               init();
               gnThesaurusService.getTopConcept(scope.thesaurusKey).then(
               function(c) {
                 scope.concept = c;
               });
             }
           }
         };
       }]);


  /**
   * @ngdoc directive
   * @name gn_thesaurus.directive:gnKeywordPicker
   * @function
   *
   * @description
   * Provide simple keyword search.
   *
   * We can't transclude input (http://plnkr.co/edit/R2O2ixWA1QJUsVcUHl0N)
   */
  module.directive('gnKeywordPicker', [
    'gnThesaurusService', '$compile', '$translate', 'gnCurrentEdit',
    function(gnThesaurusService, $compile, $translate, gnCurrentEdit) {
      return {
        restrict: 'A',
        scope: {
        },
        link: function(scope, element, attrs) {
          scope.thesaurusKey = attrs.thesaurusKey || '';
          scope.orderById = attrs.orderById || 'false';
          scope.max = gnThesaurusService.DEFAULT_NUMBER_OF_RESULTS;



          var displayDefinition = attrs.displayDefinition || '';
          var numberOfSuggestions = attrs.numberOfSuggestions || 20;
          var initialized = false;

          // Create an input group around the element
          // with a thesaurus selector on the right.
          var addThesaurusSelectorOnElement = function() {
            var inputGroup = angular.element(
                '<div class="input-group"></div>');
            var dropDown = angular.element(
                '<div class="input-group-btn"></div>');
            // Thesaurus selector is a directive
            var thesaurusSel = '<span data-gn-thesaurus-selector="" ' +
                'data-selector-only="true"></span>';

            var input = element.replaceWith(inputGroup);
            inputGroup.append(input);
            inputGroup.append(dropDown);
            // Compile before insertion
            dropDown.append($compile(thesaurusSel)(scope));
          };


          var init = function() {
            // Get list of available thesaurus (if not defined
            // by scope)
            element.typeahead('destroy');
            element.attr('placeholder',
                $translate.instant('searchOrTypeKeyword'));

            // Thesaurus selector is not added if the key is defined
            // by configuration
            if (!initialized && !attrs.thesaurusKey) {
              addThesaurusSelectorOnElement(element);
            }
            var searchLanguage = gnCurrentEdit.allLanguages.code2iso['#' + attrs.lang] ||
              gnCurrentEdit.mdLanguage ||
              scope.lang;
            var keywordsAutocompleter =
                gnThesaurusService.getKeywordAutocompleter({
                  thesaurusKey: scope.thesaurusKey,
                  lang: searchLanguage,
                  outputLang: gnCurrentEdit.allLanguages && gnCurrentEdit.allLanguages.iso ?
                         gnCurrentEdit.allLanguages.iso.join(',') :
                         (gnCurrentEdit.mdLanguage || scope.lang),
                  orderById: scope.orderById
                });


            // Multilingual support
            // In multilingual mode, update all inputs
            // based on information returned by thesaurus service
            // (there is one input per language with a specific
            // field name)
            var isMultilingualMode =
              $(element).closest('div[data-gn-multilingual-field]').size() === 1;

            // When concept id attribute is set, then an extra input field is used.
            // Eg. in ISO schema, an Anchor element is used.
            // In such case, an xlink:href attribute store the concept id.
            // By default, such an attribute is identified in the form by
            // the parent element id + '_' + attribute name
            if (angular.isDefined(attrs.thesaurusConceptIdAttribute)) {
              scope.conceptIdElementName =
                // In multilingual mode, the ref to the CharacterString is known using the id
                (isMultilingualMode ? '_' + attrs.id.replace('gn-field-', '') : attrs.name) +
                '_' + attrs.thesaurusConceptIdAttribute;

              // Check that the element does not exist already in the form
              // Could be in the case it was already encoded.
              var input = element.parent().parent().find('[name=' + scope.conceptIdElementName + ']');

              var insertionPoint = isMultilingualMode ?
                element.closest('div[data-gn-multilingual-field]').find('div.well') : element;

              var isFirstMultilingualElement = insertionPoint.siblings('div.gn-keyword-picker-concept-id').length === 0;

              if ((!isMultilingualMode && input.length === 0) ||
                // Add an extra form element to store the value
                // If multilingual, only one field is added to the first input
                // eg. in ISO19139, the xlink:href attribute is part of the
                // CharacterString and not to the children
                (isMultilingualMode && isFirstMultilingualElement && input.length === 0)) {

                var conceptIdElement =  angular.element(
                  '<div class="well well-sm gn-keyword-picker-concept-id row">' +
                  '  <div class="form-group">' +
                  '    <label class="col-sm-4"><i class="fa fa-link fa-fw"/><span data-translate>URL</span></label>' +
                  '    <div class="col-sm-6"><input name="' + scope.conceptIdElementName + '" ' +
                  '       class="gn-field-link form-control"/>' +
                  '    </div>' +
                  '    <div class="col-sm-2"><a class="btn btn-link" title="{{\'resetUrl\' | translate}}" data-ng-click="resetUrl()"><i class="fa fa-times text-danger"/></a></div>' +
                  '  </div>' +
                  '</div>');
                insertionPoint[isMultilingualMode ? 'before' : 'after'](
                  $compile(conceptIdElement)(scope));
              }
            }

            // Init typeahead
            element.typeahead({
              minLength: 0,
              highlight: true
            }, {
              name: 'keyword',
              limit: numberOfSuggestions,
              source: keywordsAutocompleter.ttAdapter(),
              displayKey: function (data) {
                 return data.props.values[searchLanguage] || data.props.value;
              },
              templates: {
                suggestion: function (data) {
                  var def = data.props.definitions[searchLanguage];
                  var text = '<p>' + data.props.values[searchLanguage] + '';
                  if (displayDefinition && def != '') {
                    text += ' - <i>' + def + '</i>';
                  }
                  return text + '</p>';
                }
               // header: '<h4>' + scope.thesaurusKey + '</h4>'
              }
            }).bind('typeahead:selected',
              $.proxy(function(obj, keyword) {
                var inputs = $(obj.currentTarget).parent().parent().find('input.tt-input');
                if (isMultilingualMode && inputs.size() > 0) {
                  for (var i = 0; i < inputs.size(); i ++) {
                    var input = inputs.get(i);
                    var lang = input.getAttribute('lang');
                    var value = keyword.props.values[gnCurrentEdit.allLanguages.code2iso['#' + lang]];
                    if (value) {
                      $(input).typeahead('val', value);
                    }
                    // If no value for the language, value is not set.
                  }
                } else {
                  $(obj.currentTarget).typeahead('val', keyword.label);
                }

                if(scope.conceptIdElementName) {
                  var keywordKey = keyword.props.uri;
                  // This directive may depend on others and populate
                  // the same target input field for the attribute.
                  // Use a search instead of a scope element to cope with init order.

                  var insertionPoint = isMultilingualMode ?
                    element.closest('div[data-gn-multilingual-field]') : element.parent().parent();
                  var input = insertionPoint.find('[name=' + scope.conceptIdElementName + ']');
                  input.val(keywordKey);
                }
              }, $(element))
            );


            scope.resetUrl = function () {
              if(scope.conceptIdElementName) {
                var insertionPoint = isMultilingualMode ?
                  element.closest('div[data-gn-multilingual-field]') : element;
                var input = insertionPoint.find('[name=' + scope.conceptIdElementName + ']');
                input.val('');
              }
            };

            // When clicking the element trigger input
            // to show autocompletion list.
            // https://github.com/twitter/typeahead.js/issues/798
            element.on('typeahead:opened', function() {
              var initial = element.val(),
                  ev = $.Event('keydown');
              ev.keyCode = ev.which = 40;
              element.trigger(ev);
              if (element.val() != initial) {
                element.val('');
              }
              return true;
            });
            initialized = true;
          };

          init();

          scope.$watch('thesaurusKey', function(newValue) {
            init();
          });

          element.on('$destroy', function () {
            if (scope.conceptIdElement) {
              scope.conceptIdElement.remove();
            }
          });
        }
      };
    }]);
})();
