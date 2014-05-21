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
                 label: $translate('NEW')};
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
                   return $translate('schematronDescriptionAlwaysAccept');
                 case 'NEW':
                   return $translate('NEW');
                 case 'XPATH':
                   return $translate('schematronDescriptionXpath',
                   {value: scope.original.uivalue});
                 default:
                   type = scope.criteriaTypes[scope.original.uitype].label;
                   return $translate('schematronDescriptionGeneric',
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
               $translate('confirmDeleteSchematronCriteria');
               scope.confirmationDialog.deleteConfirmed = function() {
                 gnSchematronAdminService.criteria.
                 remove(scope.criteria, scope.group);
               };
               scope.confirmationDialog.showDialog();
             };
             scope.saveEdit = function() {
               if (scope.isDirty()) {
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
               var input, criteriaType, typeaheadOptions, parseResponseFunction;
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
                   console.log(selectRecordArray);
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
                     typeaheadOptions.prefetch = {
                       url: criteriaType.remote.url,
                       ttl: parseInt(criteriaType.remote.cacheTime),
                       filter: parseResponseFunction
                     };
                   } else {
                     typeaheadOptions.remote = {
                       url: criteriaType.remote.url,
                       cache: false,
                       timeout: 1000,
                       wildcard: '@@search@@',
                       filter: parseResponseFunction
                     };
                   }
                 } else {
                   typeaheadOptions.local = criteriaType.local;
                 }

                 input.typeahead(typeaheadOptions);
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
