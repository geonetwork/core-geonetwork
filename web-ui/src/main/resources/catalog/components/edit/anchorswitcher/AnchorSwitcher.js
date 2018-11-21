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
  goog.provide('gn_anchor_switcher_directive');

  var module = angular.module('gn_anchor_switcher_directive', []);

  module.directive('gnAnchorSwitcher',
      ['gnEditor', 'gnCurrentEdit', '$compile', '$q',
       function(gnEditor, gnCurrentEdit, $compile, $q) {
         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             id: '@',
             textValue: '@value',
             elementRef: '@name',
             gnFieldTooltip: '@'
           },
           templateUrl: '../../catalog/components/edit/anchorswitcher/partials/' +
           'anchorswitcher.html',
           link: function(scope, element, attrs) {
             var attributeHtmlTemplate = _.template(
             '<div class="form-group" id="gn-attr-<%= id %>_xlinkCOLONhref">' +
             '<label class="col-sm-4" data-translate>url</label>' +
             '<div class="col-sm-7">' +
             '<input type="text" class="" ' +
             'name="<%= id %>_xlinkCOLONhref" value="">' +
             '</div>' +
             '<div class="col-sm-1">' +
             '<a class="btn pull-right" ' +
             'data-gn-click-and-spin="removeAttribute(\'<%= id %>_xlinkCOLONhref\')" ' +
             'data-toggle="tooltip" data-placement="top" ' +
             'title="{{\'deleteField\' |translate}}" style="visibility: hidden;">' +
             '<i class="fa fa-times text-danger"></i>' +
             '</a>' +
             '</div>' +
             '</div>'
             );


             scope.checkMode = function() {
               var xlinkHrefDomEl = $('[name="' + scope.elementRef +
               '_xlinkCOLONhref' + '"]');
               if (xlinkHrefDomEl.length > 0) {
                 scope.mode = 'anchor';
               } else {
                 scope.mode = 'characterString';
               }
               return scope.mode;
             };

             scope.setAttributesVisibility = function(mode, newAttributes) {
               var visibility = newAttributes ||
               gnCurrentEdit.displayAttributes;

               if (mode === 'anchor') {
                 visibility = true;
               }
               var attributesDiv = $('#gn-attr-div' + scope.elementRef);
               // Toggle class on all gn-attr widgets
               if (visibility) {
                 attributesDiv.removeClass('hidden');
               } else {
                 attributesDiv.addClass('hidden');
               }
             };

             scope.$watch('getDisplayAttributes()', function(newAttributes) {
               scope.setAttributesVisibility(scope.mode, newAttributes);
             });

             scope.getDisplayAttributes = function() {
               return gnCurrentEdit.displayAttributes;
             };

             scope.$watch('mode', function(newMode) {
               var xlinkInput = $('[name="' + scope.elementRef +
               '_xlinkCOLONhref' + '"]');
               if (newMode === 'anchor') {
                 if (xlinkInput.length === 0) {
                   addXlinkHrefElement();
                 }

               } else if (newMode === 'characterString') {

               }
               scope.setAttributesVisibility(newMode);
             });

             var addXlinkHrefElement = function() {
               var snippedDiv = $('#gn-attr-div' + scope.elementRef);
               var xlinkDivTemplate = attributeHtmlTemplate({
                 id: scope.elementRef
               });
               var xlinkDiv = $(xlinkDivTemplate);
               var compiledAngularDiv = $compile(xlinkDiv)(scope);
               snippedDiv.append(compiledAngularDiv);
             };

             scope.setMode = function(newMode) {
               scope.mode = newMode;
             };

             scope.removeAttribute = function(ref) {
               var defer = $q.defer();
               gnEditor.removeAttribute(gnCurrentEdit.id, ref).then(function() {
                 defer.resolve();
               }, function() {
                 defer.reject();
               });
               return defer.promise;
             };

             scope.$on('attributeRemoved', function(event, ref) {
               if (ref === scope.elementRef + '_xlinkCOLONhref') {
                 scope.setMode('characterString');
               }
             });


             // init
             element.removeClass('form-control');
             scope.initialMode = scope.checkMode();
           }
         };
       }]);
})();
