(function() {
  goog.provide('gn_thesaurus_directive');

  var module = angular.module('gn_thesaurus_directive', []);

  /**
     *
     *
     */
  module.directive('gnThesaurusSelector',
      ['$http', '$rootScope', '$timeout',
       'gnThesaurusService', 'gnMetadataManagerService',
       function($http, $rootScope, $timeout,
       gnThesaurusService, gnMetadataManagerService) {

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
             scope.thesaurus = {};
             scope.snippet = null;
             scope.snippetRef = null;

             // TODO: Remove from list existing thesaurus
             // in the record ?
             gnThesaurusService.getThesaurusList().then(
             function(data) {
               // TODO: Sort them
               scope.thesaurus = data;
             });

             scope.add = function() {
               $rootScope.$broadcast('AddElement',
                   scope.elementRef, scope.elementName, scope.domId, 'before');
             };

             scope.addThesaurus = function(thesaurusIdentifier) {
               gnThesaurusService
               .getThesaurusSnippet(thesaurusIdentifier).then(
               function(data) {
                 // Add the fragment to the form
                 scope.snippet = '<' + scope.elementName +
                 " xmlns:gmd='http://www.isotc211.org/2005/gmd'>" +
                 data + '</' + scope.elementName + '>';
                 scope.snippetRef = '_X' + scope.elementRef +
                 '_' + scope.elementName.replace(':', 'COLON');

                 // FIXME : time out may not be the best options.
                 // We need to have the form updated before saving
                 // edits
                 $timeout(function() {
                   // Save the metadata and refresh the form
                   $rootScope.$broadcast('SaveEdits', true);
                 }, 200);

               });
               return false;
             };
           }
         };
       }]);


  /**
     *
     *
     */
  module.directive('gnKeywordSelector',
      ['$http', '$rootScope', '$timeout',
       'gnThesaurusService', 'gnMetadataManagerService',
       function($http, $rootScope, $timeout,
       gnThesaurusService, gnMetadataManagerService) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnKeywordSelector',
             elementRef: '@',
             thesaurusName: '@',
             thesaurusKey: '@',
             keywords: '@',
             transformations: '@',
             currentTransformation: '@'
           },
           templateUrl: '../../catalog/components/thesaurus/' +
           'partials/keywordselector.html',
           link: function(scope, element, attrs) {

             scope.max = 200;
             scope.filter = null;
             scope.results = null;
             scope.snippet = null;
             scope.selected = [];
             scope.currentSelectionLeft = [];
             scope.currentSelectionRight = [];
             scope.initialKeywords = scope.keywords ?
                 scope.keywords.split(',') : [];
             var sortOnSelection = true;

             // Check initial keywords are available in the thesaurus

             var sort = function(a, b) {
               if (a.value['#text'].toLowerCase() <
               b.value['#text'].toLowerCase()) {
                 return -1;
               }
               if (a.value['#text'].toLowerCase() >
               b.value['#text'].toLowerCase()) {
                 return 1;
               }
               return 0;
             };

             var init = function() {
               // Check that all initial keywords are in the thesaurus
               angular.forEach(scope.initialKeywords, function(keyword) {
                 // One keyword only and exact match search
                 gnThesaurusService.getKeywords(keyword,
                 scope.thesaurusKey, 1, 2).then(function(data) {
                   scope.selected.push(data[0]);
                 });
               });

               // Get the matching XML snippet for the initial set of keywords
               getSnippet();

               // Then register search filter change
               scope.$watch('filter', search);
             };

             var search = function() {
               gnThesaurusService.getKeywords(scope.filter,
               scope.thesaurusKey, scope.max)
              .then(function(data) {
                 // Remove from search already selected keywords
                 scope.results = $.grep(data, function(n) {
                   var alreadySelected = true;
                   if (scope.selected.length !== 0) {
                     alreadySelected = $.grep(scope.selected, function(s) {
                       return s.value['#text'] === n.value['#text'];
                     }).length === 0;
                   }
                   return alreadySelected;
                 });
               });
             };

             var getKeywordIds = function() {
               var ids = [];
               angular.forEach(scope.selected, function(k) {
                 ids.push(k.uri);
               });
               return ids;
             };

             var getSnippet = function() {
               gnThesaurusService
              .getThesaurusSnippet(scope.thesaurusKey,
               getKeywordIds(), scope.transformation).then(
               function(data) {
                 scope.snippet = data;
               });
             };

             /**
             * Select a single element or the list of currently
             * selected element.
             */
             scope.select = function(k) {
               var elementsToAdd = [];
               if (!k) {
                 angular.forEach(scope.currentSelectionLeft, function(value) {
                   elementsToAdd.push($.grep(scope.results, function(n) {
                     return n.value['#text'] === value;
                   })[0]);
                 });
               } else {
                 elementsToAdd.push(k);
               }

               angular.forEach(elementsToAdd, function(k) {
                 scope.selected.push(k);
                 scope.results = $.grep(scope.results, function(n) {
                   return n !== k;
                 });
               });

               if (sortOnSelection) {
                 scope.selected.sort(sort);
               }

               getSnippet();
             };


             scope.unselect = function(k) {
               var elementsToRemove = k ?
                   [k.value['#text']] : scope.currentSelectionRight;
               scope.selected = $.grep(scope.selected, function(n) {
                 var toUnselect =
                 $.inArray(n.value['#text'], elementsToRemove) !== -1;
                 if (toUnselect) {
                   scope.results.push(n);
                 }
                 return !toUnselect;
               });

               if (sortOnSelection) {
                 scope.results.sort(sort);
               }

               getSnippet();
             };

             if (scope.thesaurusKey) {
               init();
             }
           }
         };
       }]);
})();
