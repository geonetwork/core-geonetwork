(function() {
  goog.provide('gn_thesaurus_directive');

  var module = angular.module('gn_thesaurus_directive', []);

  /**
   * The thesaurus selector is composed of a drop down list
   * of thesaurus available in the catalog. On selection,
   * an empty XML fragment is requested and added to the form
   * before the editor is saved and refreshed.
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
             domId: '@'
           },
           templateUrl: '../../catalog/components/thesaurus/' +
           'partials/thesaurusselector.html',
           link: function(scope, element, attrs) {
             scope.thesaurus = null;
             scope.snippet = null;
             scope.snippetRef = null;

             // TODO: Remove from list existing thesaurus
             // in the record ?
             gnThesaurusService.getAll().then(
             function(listOfThesaurus) {
               // TODO: Sort them
               scope.thesaurus = listOfThesaurus;
             });

             scope.add = function() {
               gnEditor.add(gnCurrentEdit.id,
               scope.elementRef, scope.elementName, scope.domId, 'before');
             };

             scope.addThesaurus = function(thesaurusIdentifier) {
               gnThesaurusService
                .getXML(thesaurusIdentifier).then(
               function(data) {
                 // Add the fragment to the form
                 scope.snippet = gnEditorXMLService.
                 buildXML(scope.elementName, data);
                 scope.snippetRef = gnEditor.
                 buildXMLFieldName(scope.elementRef, scope.elementName);


                 $timeout(function() {
                   // Save the metadata and refresh the form
                   gnEditor.save(gnCurrentEdit.id, true);
                 });

               });
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
      ['$timeout',
       'gnThesaurusService', 'gnEditor',
       'Keyword',
       function($timeout,
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
                   field.typeahead({
                     valueKey: 'label',
                     local: listOfKeywords,
                     // Then filter on typing
                     remote: {
                       wildcard: 'QUERY',
                       url: gnThesaurusService.getKeywordsSearchUrl('QUERY',
                       scope.thesaurusKey, scope.max),
                       filter: gnThesaurusService.parseKeywordsResponse
                     },
                     minLength: 0,
                     limit: gnThesaurusService.DEFAULT_NUMBER_OF_SUGGESTIONS
                     // template: '<p>{{label}}</p>'
                     // TODO: could be nice to have definition
                   }).bind('typeahead:selected',
                   $.proxy(function(obj, keyword) {
                     // Add to tags
                     this.tagsinput('add', keyword);

                     // Update selection and snippet
                     scope.selected = this.tagsinput('items');
                     getSnippet(); // FIXME: should not be necessary
                     // as there is a watch on it ?

                     // Clear typeahead
                     this.tagsinput('input').typeahead('setQuery', '');
                   }, $(id))
                   );

                   $(id).on('itemRemoved', function() {
                     scope.selected = $(this).tagsinput('items');
                     getSnippet();
                   });

                   // Display full list when input is clicked
                   // TODO: add config for that
                   // From http://stackoverflow.com/questions/18768401/
                   //   typeahead-js-displaying-all-prefetched-datums
                   $(element).find('input.tt-query')
                      .on('click', function() {
                     var $input = $(this);

                     // these are all expected to be objects
                     // so falsey check is fine
                     if (!$input.data() || !$input.data().ttView ||
                     !$input.data().ttView.datasets ||
                     !$input.data().ttView.dropdownView ||
                     !$input.data().ttView.inputView) {
                       return;
                     }

                     var ttView = $input.data().ttView;

                     var toggleAttribute = $input.attr('data-toggled');

                     if (!toggleAttribute || toggleAttribute === 'off') {
                       $input.attr('data-toggled', 'on');

                       $input.typeahead('setQuery', '');

                       if ($.isArray(ttView.datasets) &&
                       ttView.datasets.length > 0) {
                         // only pulling the first dataset for this hack
                         var fullSuggestionList = [];
                         // renderSuggestions expects a
                         // suggestions array not an object
                         $.each(ttView.datasets[0].itemHash, function(i, item) {
                           fullSuggestionList.push(item);
                         });

                         ttView.dropdownView.renderSuggestions(
                         ttView.datasets[0], fullSuggestionList);
                         ttView.inputView.setHintValue('');
                         ttView.dropdownView.open();
                       }
                     }
                     else if (toggleAttribute === 'on') {
                       $input.attr('data-toggled', 'off');
                       ttView.dropdownView.close();
                     }
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
               scope.currentTransformation = t;
               getSnippet();
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
               getKeywordIds(), scope.currentTransformation).then(
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
})();
