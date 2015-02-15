(function() {
  goog.provide('gn_thesaurus_directive');

  var module = angular.module('gn_thesaurus_directive', []);

  /**
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
       'gnEditorXMLService', 'gnCurrentEdit',
       function($timeout,
               gnThesaurusService, gnEditor,
               gnEditorXMLService, gnCurrentEdit) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnThesaurusSelector',
             elementName: '@',
             elementRef: '@',
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
             var restrictTo =
                 scope.include ? (
                     scope.include.indexOf(',') !== -1 ?
                      scope.include.split(',') : [scope.include]) : [];

             scope.allowFreeTextKeywords =
             (attrs.allowFreeTextKeywords === undefined) ||
             (attrs.allowFreeTextKeywords == 'true');

             // TODO: Remove from list existing thesaurus
             // in the record ?
             gnThesaurusService.getAll().then(
             function(listOfThesaurus) {
               // TODO: Sort them
               if (restrictTo.length > 0) {
                 var filteredList = [];
                 angular.forEach(listOfThesaurus, function(thesaurus) {
                   if ($.inArray(thesaurus.getKey(), restrictTo) !== -1) {
                     filteredList.push(thesaurus);
                   }
                 });
                 scope.thesaurus = filteredList;
               } else {
                 scope.thesaurus = listOfThesaurus;
               }
             });

             scope.add = function() {
               gnEditor.add(gnCurrentEdit.id,
               scope.elementRef, scope.elementName, scope.domId, 'before');
             };

             scope.addThesaurus = function(thesaurusIdentifier) {
               if (scope.selectorOnly) {
                 scope.$parent.thesaurusKey = scope.thesaurusKey =
                         thesaurusIdentifier;
               } else {
                 gnThesaurusService
                         .getXML(thesaurusIdentifier,  null,
                                 attrs.transformation).then(
                         function(data) {
                   // Add the fragment to the form
                   scope.snippet = data;
                   scope.snippetRef = gnEditor.
                                 buildXMLFieldName(
                                   scope.elementRef,
                                   scope.elementName);


                   $timeout(function() {
                     // Save the metadata and refresh the form
                     gnEditor.save(gnCurrentEdit.id, true);
                   });

                     });
               }
               return false;
             };
           }
         };
       }]);


  /**
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
      ['$timeout', '$translate',
       'gnThesaurusService', 'gnEditor',
       'Keyword',
       function($timeout, $translate,
               gnThesaurusService, gnEditor, Keyword) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnKeywordSelector',
             elementRef: '@',
             thesaurusKey: '@',
             keywords: '@',
             transformations: '@',
             currentTransformation: '@',
             lang: '@',
             textgroupOnly: '@',

             // Max number of tags allowed. Use 1 to restrict to only
             // on keyword.
             maxTags: '@'
           },
           templateUrl: '../../catalog/components/thesaurus/' +
           'partials/keywordselector.html',
           link: function(scope, element, attrs) {

             scope.max = gnThesaurusService.DEFAULT_NUMBER_OF_RESULTS;
             scope.filter = null;
             scope.results = null;
             scope.snippet = null;
             scope.isInitialized = false;
             scope.elementRefBackup = scope.elementRef;
             scope.invalidKeywordMatch = false;
             scope.selected = [];
             scope.initialKeywords = scope.keywords ?
             scope.keywords.split(',') : [];
             scope.transformationLists =
             scope.transformations.indexOf(',') !== -1 ?
             scope.transformations.split(',') : [scope.transformations];
             scope.maxTagsLabel = scope.maxTags || '∞';

             //Get langs of metadata
             var langs = [];
             for (var p in JSON.parse(scope.lang)) {
               langs.push(p);
             }
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
                   gnThesaurusService.getKeywords(keyword,
                   scope.thesaurusKey, 1, 2).then(function(listOfKeywords) {
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
               // Only applies to multiselect mode
               scope.$watch('filter', search);
             };


             // Init typeahead and tag input
             var initTagsInput = function() {
               var id = '#tagsinput_' + scope.elementRef;
               $timeout(function() {
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
                 scope.thesaurusKey, scope.max)
                  .then(function(listOfKeywords) {

                   var field = $(id).tagsinput('input');
                   field.attr('placeholder', $translate('searchKeyword'));

                   var keywordsAutocompleter =
                   gnThesaurusService.getKeywordAutocompleter({
                     thesaurusKey: scope.thesaurusKey,
                     dataToExclude: scope.selected
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
                     angular.copy($(this).tagsinput('items'), scope.selected);
                     getSnippet();
                   });

                   // When clicking the element trigger input
                   // to show autocompletion list.
                   // https://github.com/twitter/typeahead.js/issues/798
                   field.on('typeahead:opened', function() {
                     var initial = field.val(),
                     ev = $.Event('keydown');
                     ev.keyCode = ev.which = 40;
                     field.trigger(ev);
                     if (field.val() != initial) {
                       field.val('');
                     }
                     return true;
                   });
                 });
               });
             };

             var checkState = function() {
               if (scope.isInitialized && !scope.invalidKeywordMatch) {
                 getSnippet();

                 scope.$watch('results', getSnippet);
                 scope.$watch('selected', getSnippet);

                 if (scope.mode === 'tagsinput') {
                   initTagsInput();
                 }
               } else if (scope.invalidKeywordMatch) {
                 // invalidate element ref to not trigger
                 // an update of the record with an invalid
                 // state ie. keywords not loaded properly
                 scope.elementRef = '';
               }
             };


             var search = function() {
               gnThesaurusService.getKeywords(scope.filter,
               scope.thesaurusKey, scope.max)
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
             }
           }
         };
       }]);


  /**
     * @ngdoc directive
     * @name gn_fields_directive.directive:gnKeywordPicker
     * @function
     *
     * @description
     * Provide simple keyword search.
     *
     * We can't transclude input (http://plnkr.co/edit/R2O2ixWA1QJUsVcUHl0N)
     */
  module.directive('gnKeywordPicker', [
    'gnThesaurusService', '$compile', '$translate',
    function(gnThesaurusService, $compile, $translate) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          scope.thesaurusKey = attrs.thesaurusKey ||  '';
          scope.max = gnThesaurusService.DEFAULT_NUMBER_OF_RESULTS;
          var initialized = false;

          // Create an input group around the element
          // with a thesaurus selector on the right.
          var addThesaurusSelectorOnElement = function() {
            var inputGroup = angular.
                element('<div class="input-group"></div>');
            var dropDown = angular.
                element('<div class="input-group-btn"></div>');
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
            element.attr('placeholder', $translate('searchOrTypeKeyword'));

            // Thesaurus selector is not added if the key is defined
            // by configuration
            if (!initialized && !attrs.thesaurusKey) {
              addThesaurusSelectorOnElement(element);
            }
            var keywordsAutocompleter =
                gnThesaurusService.getKeywordAutocompleter({
                  thesaurusKey: scope.thesaurusKey
                });

            // Init typeahead
            element.typeahead({
              minLength: 0,
              highlight: true
              // template: '<p>{{label}}</p>'
              // TODO: could be nice to have definition
            }, {
              name: 'keyword',
              displayKey: 'label',
              source: keywordsAutocompleter.ttAdapter()
              // templates: {
              // header: '<h4>' + scope.thesaurusKey + '</h4>'
              // }
            });

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
        }
      };
    }]);
})();
