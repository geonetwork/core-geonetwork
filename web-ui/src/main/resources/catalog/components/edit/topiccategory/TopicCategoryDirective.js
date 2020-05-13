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
  module.directive('gnTopiccategorySelectorDiv',
      ['$compile', '$timeout', '$translate',
       'gnTopicCategoryService', 'gnCurrentEdit',
       'TopicCategory', 'gnLangs',
       function($compile, $timeout, $translate,
                gnTopicCategoryService, gnCurrentEdit, TopicCategory, gnLangs) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             values: '@gnTopiccategorySelectorDiv',
             label: '@label',
             ref: '@ref',
             required: '@required'
           },
           templateUrl: '../../catalog/components/edit/topiccategory/partials/' +
           'topiccategory.html',
           link: function(scope) {
             scope.snippet = null;
             scope.selected = [];
             scope.initialTopicCategories = [];
             var xpathBySchema = {
               'iso19139': {
                 xpath: 'gmd:identificationInfo/*/gmd:topicCategory',
                 tpl: '<gmd:topicCategory ' +
                 'xmlns:gmd="http://www.isotc211.org/2005/gmd">' +
                 '<gmd:MD_TopicCategoryCode>{{t}}</gmd:MD_TopicCategoryCode>' +
                 '</gmd:topicCategory>'
               },
               'iso19115-3': {
                 xpath: 'mdb:identificationInfo/*/mri:topicCategory',
                 tpl: '<mri:topicCategory xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0">' +
                        '<mri:MD_TopicCategoryCode>{{t}}</mri:MD_TopicCategoryCode>\n' +
                      '</mri:topicCategory>'
               }
             }

             var schema = gnCurrentEdit.schema;

             // By convention schema extension will have
             // an identifier of the form {baseSchemaId}.{extensionId}
             var schemaForXpath = (gnCurrentEdit.schema.indexOf(".") !== -1) ?
               gnCurrentEdit.schema.split('.')[0] :
               gnCurrentEdit.schema;
             scope.xpath = xpathBySchema[schemaForXpath].xpath;

             // Initial values are comma separated encoded
             if (scope.values) {
               scope.initialTopicCategories = scope.values.split(',');
             }

             scope.maxTagsLabel = scope.maxTags || '∞';

             scope.buildFinalSnippet = function() {
               var snippet = "";

               if (scope.snippets) {
                 for(var i = 0; i < scope.snippets.length; i++) {
                   snippet +=
                     scope.snippets[i];
                 }
               }

               return "<gn_replace_all>" +
                 snippet + "</gn_replace_all>";
             };

             var init = function() {
              // Load topic category code list, then init widget
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
                     scope.selected.push(existingTopicCategory);
                   }
                 });

                 // Build XML for current topic categories
                 getSnippet();

                 scope.$watch('results', getSnippet);
                 scope.$watch('selected', getSnippet);

                 // Init autocompleter
                 initTagsInput(listOfTopicCategories);
               });

             };

             // Init typeahead and tag input
             var initTagsInput = function(listOfTopicCategories) {
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


                   var field = $(id).tagsinput('input');
                   field.attr('placeholder',
                   $translate.instant('searchTopicCategory'));

                   var topicCategoriesAutocompleter =
                   gnTopicCategoryService.getTopicCategoryAutocompleter({
                     dataToExclude: scope.selected,
                     lang: gnLangs.current,
                     schema: schema,
                     data: listOfTopicCategories
                   });


                   function allOrSearchFn(q, sync) {
                     if (q === '') {
                       sync(topicCategoriesAutocompleter.all());
                       // This is the only change needed to get 'ALL'
                       // items as the defaults
                     } else {
                       topicCategoriesAutocompleter.search(q, sync);
                     }
                   }
                   // Init typeahead
                   field.typeahead({
                     minLength: 0,
                     highlight: true
                   }, {
                     name: 'topiccategory',
                     displayKey: 'label',
                     limit: Infinity,
                     source: allOrSearchFn
                   }).bind('typeahead:selected',
                     $.proxy(function(obj, topiccategory) {
                       // Add to tags
                       this.tagsinput('add', topiccategory);

                       // Update selection and snippet
                       angular.copy(this.tagsinput('items'), scope.selected);
                       getSnippet();
                       scope.$apply();

                       // Clear typeahead
                       this.tagsinput('input').typeahead('val', '');

                       // Force prefect to update items to exclude
                       topicCategoriesAutocompleter.initialize(true);
                     }, $(id))
                   );

                   $(id).on('itemRemoved', function() {
                     angular.copy($(this)
                     .tagsinput('items'), scope.selected);
                     getSnippet();
                     scope.$apply();

                     // Force prefect to update items to exclude
                     topicCategoriesAutocompleter.initialize(true);
                   });

                 } catch (e) {
                   console.warn('No tagsinput for ' + id +
                   ', error: ' + e.message);
                 }
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
               var xml = [];
               angular.forEach(getTopicCategoryIds(), function(t) {
                 xml.push(xpathBySchema[schemaForXpath].tpl.replace('{{t}}', t));
               });
               scope.snippets = xml;
             };

             init();

           }
         };
       }]);

})();
