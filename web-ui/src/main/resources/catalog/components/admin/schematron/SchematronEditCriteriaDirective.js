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
  'use strict';
  goog.provide('gn_schematronadmin_editcriteriadirective');
  goog.require('gn_schematronadminservice');

  var module = angular.module('gn_schematronadmin_editcriteriadirective',
      ['gn_schematronadminservice']);


  /**
     * Display harvester identification section with
     * name, group and icon
     */
  module.directive('gnCriteriaEditor',
      ['gnSchematronAdminService', '$translate', '$timeout',
       function(gnSchematronAdminService, $translate, $timeout) {
         return {
           restrict: 'E',
           replace: true,
           transclude: false,
           scope: {
             original: '=criteria',
             schema: '=',
             group: '=',
             lang: '=',
             confirmationDialog: '='
           },

           templateUrl: '../../catalog/components/admin/schematron/' +
           'partials/criteria-viewer.html',
           link: function(scope, element) {
             var findValueInput, criteriaTypeToValueMap, i, type;
             findValueInput = function() {
               return element.find('input.form-control');
             };
             if (scope.original.value instanceof Array) {
               scope.original.value = '';
             }
             if (scope.original.uivalue instanceof Array) {
               scope.original.uivalue = '';
             }
             scope.criteria = angular.copy(scope.original);

             // Keep a map of the values that belong to each criteria type
             // so when a type is changed the value field can be updated with a
             // value that makes sense for the type.
             criteriaTypeToValueMap = {
               currentType__: scope.criteria.uitype
             };
             scope.criteriaTypes = {};
             if (scope.criteria.type === 'NEW') {
               scope.criteriaTypes.NEW = {
                 name: 'NEW',
                 type: 'NEW',
                 label: $translate.instant('NEW')};
               criteriaTypeToValueMap.NEW = '';
             }
             for (i = 0; i < scope.schema.criteriaTypes.type.length; i++) {
               type = scope.schema.criteriaTypes.type[i];
               scope.criteriaTypes[type.name] = type;
               criteriaTypeToValueMap[type.name] = '';
             }
             scope.isDirty = function() {
               return scope.criteria.uitype !== scope.original.uitype ||
               scope.criteria.uivalue !== scope.original.uivalue;
             };
             scope.calculateClassOnDirty = function(whenDirty, whenClean) {
               if (scope.isDirty()) {
                 return whenDirty;
               }
               return whenClean;
             };
             scope.editing = false;
             scope.startEditing = function() {
               if (scope.schema.editObject) {
                 scope.schema.editObject.editing = false;
               }
               scope.schema.editObject = scope;
               scope.editing = true;
               scope.updateTypeAhead();
               $timeout(function() {
                 var input = findValueInput();
                 if (!input.attr('disabled')) {
                   input.focus();
                   input.select();
                 } else {
                   element.find('select.form-control').focus();
                 }
               });
             };
             scope.updateType = function() {
               scope.criteria.type =
               scope.criteriaTypes[scope.criteria.uitype].type;
             };
             scope.updateValueField = function() {
               var oldType, newType, oldValue, newValue;
               oldType = criteriaTypeToValueMap.currentType__;
               newType = scope.criteria.uitype;
               oldValue = scope.criteria.uivalue;
               newValue = criteriaTypeToValueMap[newType];

               criteriaTypeToValueMap.currentType__ = newType;
               criteriaTypeToValueMap[oldType] = oldValue;
               scope.criteria.uivalue = newValue;
             };
             scope.describeCriteria = function() {
               switch (angular.uppercase(scope.original.uitype)) {
                 case 'ALWAYS_ACCEPT':
                   return $translate.instant(
                   'schematronDescriptionAlwaysAccept');
                 case 'NEW':
                   return $translate.instant('NEW');
                 case 'XPATH':
                   return $translate.instant('schematronDescriptionXpath',
                   {value: scope.original.uivalue});
                 default:
                   type = scope.criteriaTypes[scope.original.uitype].label;
                   return $translate.instant('schematronDescriptionGeneric',
                   {type: type, value: scope.original.uivalue});
               }
             };
             scope.handleKeyUp = function(keyCode) {
               switch (keyCode) {
                 case 27: // esc
                   scope.cancelEditing();
                   break;
                 case 13: // enter
                   scope.saveEdit();
                   break;
               }
             };
             scope.cancelEditing = function() {
               scope.schema.editObject = null;
               scope.criteria = angular.copy(scope.original, scope.criteria);

               scope.editing = false;
             };

             scope.deleteCriteria = function() {
               scope.confirmationDialog.message =
               $translate.instant('confirmDeleteSchematronCriteria');
               scope.confirmationDialog.deleteConfirmed = function() {
                 gnSchematronAdminService.criteria.
                 remove(scope.criteria, scope.group);
               };
               scope.confirmationDialog.showDialog();
             };
             scope.saveEdit = function() {
               var criteriaType, rawValue, value, isTypeahead;
               isTypeahead = false;
               if (scope.isDirty()) {
                 var input = findValueInput();
                 // it we are not using an autocompleter
                 // replace the value with valueUi
                 input.each(function(index, ele) {
                   isTypeahead = isTypeahead || $(ele).data('ttTypeahead');
                 });
                 if (!isTypeahead) {
                   criteriaType = scope.criteriaTypes[scope.criteria.uitype];
                   rawValue = scope.criteria.uivalue;
                   if (criteriaType.value) {
                     // Check because ALWAYS_ACCEPT has not value.
                     value = criteriaType.value.replace(/@@value@@/g, rawValue);
                     scope.criteria.value = value;
                   }
                 }

                 if (scope.criteria.id) {
                   // it is an updated
                   gnSchematronAdminService.criteria.
                   update(scope.criteria, scope.original, scope.group);
                 } else {
                   // it is an add
                   gnSchematronAdminService.criteria.
                   add(scope.criteria, scope.original, scope.group);
                 }
               }
               scope.editing = false;
             };
             scope.updateTypeAhead = function() {
               var input, criteriaType, source, typeaheadOptions,
               parseResponseFunction;
               input = findValueInput();
               input.typeahead('destroy');

               criteriaType = scope.criteriaTypes[scope.criteria.uitype];

               if (criteriaType &&
               (criteriaType.remote || criteriaType.local)) {
                 typeaheadOptions = {
                   name: scope.criteria.uitype
                 };

                 parseResponseFunction = function(parsedResponse) {
                   var selectRecordArray, selectValueFunction,
                   selectLabelFunction, selectTokensFunction, data, finalData,
                   i, record, name, rawValue, value, doEval;

                   doEval = function(propertyName) {
                     if (typeof(criteriaType.remote[propertyName]) !==
                     'function') {
                       return eval('(function ' +
                       criteriaType.remote[propertyName] + ')');
                     }
                     return criteriaType.remote[propertyName];
                   };
                   selectRecordArray = doEval('selectRecordArray');
                   selectLabelFunction = doEval('selectLabelFunction');
                   selectValueFunction = doEval('selectValueFunction');

                   if (criteriaType.remote.selectTokensFunction) {
                     selectTokensFunction = doEval('selectTokensFunction');
                   } else {
                     selectTokensFunction = function(record, scope) {
                       return selectLabelFunction(record, scope).split(/\s+/g);
                     };

                     criteriaType.remote.selectTokensFunction =
                     selectTokensFunction;
                   }
                   data = selectRecordArray(parsedResponse, scope);

                   finalData = [];
                   for (i = 0; i < data.length; i++) {
                     record = data[i];
                     name = selectLabelFunction(record, scope);
                     rawValue = selectValueFunction(record, scope);
                     value = criteriaType.value.replace(/@@value@@/g, rawValue);
                     finalData.push({
                       value: name,
                       data: value,
                       tokens: selectTokensFunction(record, scope)
                     });
                   }

                   return finalData;
                 };

                 if (criteriaType.remote) {
                   if (criteriaType.remote.cacheTime &&
                   criteriaType.remote.cacheTime > 0) {
                     source = new Bloodhound({
                       datumTokenizer:
                       Bloodhound.tokenizers.obj.whitespace('value'),
                       queryTokenizer: Bloodhound.tokenizers.whitespace,
                       prefetch: {
                         url: criteriaType.remote.url,
                         ttl: parseInt(criteriaType.remote.cacheTime),
                         filter: parseResponseFunction
                       },
                       limit: 30
                     });
                   } else {
                     source = new Bloodhound({
                       datumTokenizer:
                       Bloodhound.tokenizers.obj.whitespace('value'),
                       queryTokenizer: Bloodhound.tokenizers.whitespace,
                       remote: {
                         url: criteriaType.remote.url,
                         cache: false,
                         timeout: 1000,
                         wildcard: '@@search@@',
                         filter: parseResponseFunction
                       },
                       limit: 30
                     });
                   }
                 } else {
                   var source = new Bloodhound({
                     datumTokenizer:
                     Bloodhound.tokenizers.obj.whitespace('value'),
                     queryTokenizer: Bloodhound.tokenizers.whitespace,
                     local: data,
                     limit: 30
                   });
                 }
                 source.initialize();
                 input.typeahead({
                   minLength: 0,
                   highlight: true
                 }, {
                   name: typeaheadOptions.name,
                   displayKey: 'value',
                   source: source.ttAdapter()
                 });
                 input.on('typeahead:selected', function(event, data) {
                   scope.criteria.value = data.data;
                   scope.criteria.uivalue = data.value;
                   input.focus();
                 });
               }
             };
           }
         };
       }]);
}());
