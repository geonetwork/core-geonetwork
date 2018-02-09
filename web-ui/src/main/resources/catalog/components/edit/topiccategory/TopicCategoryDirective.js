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
  goog.provide('gn_topiccategory_directive');

  var module = angular.module('gn_topiccategory_directive',
      ['pascalprecht.translate']);


  /**
   * @ngdoc directive
   * @name gn_topiccategory.directive:gnTopiccategorySelector
   *
   * @description
   * The topic categories selector is composed of an
   * input with autocompletion. Each tags added to
   * the input.
   *
   */
  module.directive('gnTopiccategorySelector',
      ['$compile', '$filter', '$timeout', '$translate',
       'gnTopicCategoryService', 'gnEditor',
       'TopicCategory', 'gnLangs',
       function($compile, $filter, $timeout, $translate,
                gnTopicCategoryService, gnEditor, TopicCategory, gnLangs) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             values: '@gnTopiccategorySelector',
             label: '@label',
             ref: '@ref'
           },
           templateUrl: '../../catalog/components/edit/topiccategory/partials/' +
           'topiccategory.html',
           link: function(scope, element, attrs) {
              console.log('gnTopicCategorySelector');

             scope.max = gnTopicCategoryService.DEFAULT_NUMBER_OF_RESULTS;
             scope.filter = null;
             scope.results = null;
             scope.snippet = null;
             scope.isInitialized = false;
             scope.refBackup = scope.ref;
             scope.invalidTopicCategoryMatch = false;
             scope.selected = [];
             scope.initialTopicCategories = [];

             if (scope.values) {
               scope.initialTopicCategories = scope.values.split(',');
             }

             scope.maxTagsLabel = scope.maxTags || '∞';

             scope.resetTopicCategories = function() {
               scope.selected = [];
               scope.ref = scope.refBackup;
               scope.invalidTopicCategoryMatch = false;
               checkState();
             };

             scope.buildFinalSnippet = function() {
               var snippet = "";

               if (scope.snippets) {
                 for(var i = 0; i < scope.snippets.length; i++) {
                   snippet += "<gn_create>" +
                     scope.snippets[i] + "</gn_create>";
                 }
               }

               return "<gn_multiple><gn_delete></gn_delete>" +
                 snippet + "</gn_multiple>";
             };


             var init = function() {

               // Nothing to load - init done
               scope.isInitialized = scope.initialTopicCategories.length === 0;

               if (scope.isInitialized) {
                 checkState();
               } else {

                 // Check that all initial keywords are in the thesaurus
                 var counter = 0;
                 gnTopicCategoryService.getTopicCategories()
                 .then(function(listOfTopicCategories) {
                   angular.forEach(scope.initialTopicCategories, function(topicCategory) {
                     var existingTopicCategory = null;
                     for (var i = 0; i < listOfTopicCategories.length; i++) {
                       if (listOfTopicCategories[i].getId() == topicCategory) {
                         existingTopicCategory = listOfTopicCategories[i];
                       }
                     }

                     if (existingTopicCategory) {
                       counter++;
                       scope.selected.push(existingTopicCategory);
                     }
                   });

                   // Init done when all keywords are selected
                   if (counter === scope.initialTopicCategories.length) {
                     scope.isInitialized = true;
                     scope.invalidTopicCategoryMatch =
                         scope.selected.length !== scope.initialTopicCategories.length;

                     // Get the matching XML snippet for
                     // the initial set of keywords
                     // once the loaded keywords are all selected.
                     checkState();
                   }
                 });
               }

               // Then register search filter change
               scope.$watch('filter', search);
             };

             // Init typeahead and tag input
             var initTagsInput = function() {
               var id = '#tagsinput_' + scope.ref;
               $timeout(function() {
                 try {
                   $(id).tagsinput({
                     itemValue: 'label',
                     itemText: 'label',
                     maxTags: scope.maxTags
                   });

                   // Add selection to the list of tags
                   angular.forEach(scope.selected, function(topicCategory) {
                     $(id).tagsinput('add', topicCategory);
                   });

                   // Load all keywords from codelist on startup
                   gnTopicCategoryService.getTopicCategories()
                    .then(function(listOfTopicCategories) {

                     var field = $(id).tagsinput('input');
                     field.attr('placeholder',
                     $translate.instant('searchTopiccategory'));

                     var topicCategoriesAutocompleter =
                     gnTopicCategoryService.getTopicCategoryAutocompleter({
                       dataToExclude: scope.selected,
                       lang: gnLangs.current
                     });

                     // Init typeahead
                     field.typeahead({
                       minLength: 0,
                       highlight: true
                       // template: '<p>{{label}}</p>'
                       // TODO: could be nice to have definition
                     }, {
                       name: 'topiccategory',
                       displayKey: 'label',
                       source: topicCategoriesAutocompleter.ttAdapter()
                     }).bind('typeahead:selected',
                     $.proxy(function(obj, topiccategory) {
                       // Add to tags
                       this.tagsinput('add', topiccategory);

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
                 } catch (e) {
                   console.warn('No tagsinput for ' + id +
                   ', error: ' + e.message);
                 }
               });
             };

             var checkState = function() {
               if (scope.isInitialized && !scope.invalidTopicCategoryMatch) {
                 getSnippet();

                 scope.$watch('results', getSnippet);
                 scope.$watch('selected', getSnippet);

                 initTagsInput();
               } else if (scope.invalidTopicCategoryMatch) {
                 // invalidate element ref to not trigger
                 // an update of the record with an invalid
                 // state ie. topic categories not loaded properly
                 scope.ref = '';
               }
             };


             var search = function() {
               gnTopicCategoryService.getTopicCategories()
                .then(function(listOfTopicCategories) {

                 // Remove from search already selected topic categories
                 scope.results = $.grep(listOfTopicCategories, function(n) {
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

             var getTopicCategoryIds = function() {
               var ids = [];
               angular.forEach(scope.selected, function(t) {
                 ids.push(t.getId());
               });
               return ids;
             };

             var getSnippet = function() {
               scope.snippets = gnTopicCategoryService
                .getXMLSnippets(getTopicCategoryIds());
             };

             init();

           }
         };
       }]);

})();
