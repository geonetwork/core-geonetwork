(function() {
  goog.provide('gn_keyword_selector');

  var module = angular.module('gn_keyword_selector', []);

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
           'keywordselector/partials/' +
           'keywordselector.html',
           link: function(scope, element, attrs) {

             console.log(scope);
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
