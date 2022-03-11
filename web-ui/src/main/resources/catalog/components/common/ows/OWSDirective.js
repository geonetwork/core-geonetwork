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
  goog.provide('gn_ows_directive');

  angular.module('gn_ows_directive', [])

      .directive(
      'gnLayersGrid',
      [
       'gnOwsCapabilities',
       function(gnOwsCapabilities) {
         return {
           restrict: 'A',
           templateUrl: '../../catalog/components/common/ows/' +
           'partials/layersGrid.html',
           scope: {
             selection: '=',
             layers: '=',
             selectionMode: '=gnSelectionMode'
           },
           link: function(scope, element, attrs) {
             // Manage layers selection
             if (scope.selectionMode) {
               scope.isSelected = function(layerName) {
                 if (layerName) {
                   for (var i = 0; i < scope.selection.length; i++) {
                      if (scope.selection[i].Name === layerName) {
                        return true;
                      }
                   }
                 }
                 return false;
               };
               scope.select = function(layer) {
                 if (scope.selectionMode.indexOf('multiple') >= 0) {
                   var layerInSelection = _.find(scope.selection,  {'Name': layer.Name});

                   if (layerInSelection == undefined) {
                     scope.selection.push(layer);
                   }
                   else {
                     scope.selection.splice(scope.selection.indexOf(layer), 1);
                   }
                 }
                 else {
                   scope.selection.pop();
                   scope.selection.push(layer);
                 }
               };
             }
           }
         };
       }])

    .directive(
      'gnLayersTree',
      [
        'gnOwsCapabilities',
        function(gnOwsCapabilities) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/common/ows/' +
              'partials/layersTree.html',
            scope: {
              selection: '=',
              layers: '=',
              selectionMode: '='
            },
            controller: ['$scope', function($scope) {
              this.isSelected = function(layer) {
                var layerInSelection = _.find($scope.selection,  {'Name': layer.Name});

                return (layerInSelection != undefined);
              };

              this.addLayer = function(layer) {
                var layerInSelection = _.find($scope.selection,  {'Name': layer.Name});

                if (layerInSelection == undefined) {
                  if ($scope.selectionMode == 'single') {
                    $scope.selection = [];
                  }

                  $scope.selection.push(layer);
                }
                else {
                  $scope.selection.splice($scope.selection.indexOf(layer), 1);
                }
              };
            }],
            link: function(scope, element, attrs) {
              scope.removeLayer = function(layer) {
                scope.selection.splice(scope.selection.indexOf(layer), 1);
              };
            }
          };
        }])

    .directive('gnCapTreeColEditor', [
      '$translate',
      function($translate) {

        var label= $translate.instant('filter');

        return {
          restrict: 'E',
          replace: true,
          scope: {
            collection: '='
          },
          template: '<ul class="gn-layer-tree" style="list-style: none; margin-left: 0px;"><li data-ng-show="collection.length > 10" >' +
            "<div class='input-group input-group-sm'><span class='input-group-addon'><i class='fa fa-filter'></i></span>" +
            "<input class='form-control' aria-label='" + label + "' data-ng-model-options='{debounce: 200}' data-ng-model='layerSearchText'/></div>" +
            "</li>" +
            '<gn-cap-tree-elt-editor ng-repeat="member in collection | filter:layerSearchText | orderBy: \'Title\'" member="member">' +
            '</gn-cap-tree-elt-editor></ul>'
        };
      }])


  .directive('gnCapTreeEltEditor', [
    '$compile',
    '$translate',
    function($compile, $translate) {
      return {
        restrict: 'E',
        replace: true,
        require: '^gnLayersTree',
        scope: {
          member: '='
        },
        templateUrl: '../../catalog/components/common/ows/' +
          'partials/layer.html',
        link: function(scope, element, attrs, controller) {
          var el = element;

          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-folder-open-o')
              .toggleClass('fa-folder-o');
            el.children('ul').toggle();
            evt.stopPropagation();
          };

          scope.addLayer = function(c) {
            controller.addLayer(scope.member, c ? c : null);
          };

          scope.isSelected = function(layer) {
            var sel = controller.isSelected(layer);

            return sel;
          };

          scope.isParentNode = angular.isDefined(scope.member.Layer);

          // Add all subchildren
          if (angular.isArray(scope.member.Layer)) {
            element.append("<gn-cap-tree-col-editor " +
              "collection='member.Layer'></gn-cap-tree-col-editor>");
            $compile(element.find('gn-cap-tree-col-editor'))(scope);
          }
        }
      };
    }]);





})();
