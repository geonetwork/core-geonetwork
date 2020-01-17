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
  goog.provide('gn_featurestable_directive');

  var module = angular.module('gn_featurestable_directive', ['gn_utility_service']);

  module.directive('gnFeaturesTable', ['$http', 'gfiTemplateURL', 'getBsTableLang',
    function($http, gfiTemplateURL, getBsTableLang) {

      return {
        restrict: 'E',
        scope: {
          loader: '=?gnFeaturesTableLoader',
          map: '=?gnFeaturesTableLoaderMap',
          active: '=gnActive'
        },
        controllerAs: 'ctrl',
        bindToController: true,
        controller: 'gnFeaturesTableController',
        require: {
          featuresTablesCtrl: '^^gnFeaturesTables',
          ctrl: 'gnFeaturesTable'
        },
        templateUrl: '../../catalog/components/viewer/gfi/partials/' +
            'featurestable.html',
        link: function(scope, element, attrs, ctrls) {
          ctrls.ctrl.initTable(element.find('table'), scope, getBsTableLang);
        }
      };
    }]);

  var GnFeaturesTableController = function() {
    this.promise = this.loader.loadAll();
  };

  GnFeaturesTableController.prototype.initTable = function(element, scope, getBsTableLang) {
    
    // See http://stackoverflow.com/a/13382873/29655
    function getScrollbarWidth() {
      var outer = document.createElement('div');
      outer.style.visibility = 'hidden';
      outer.style.width = '100px';
      outer.style.msOverflowStyle = 'scrollbar';
      document.body.appendChild(outer);
      var widthNoScroll = outer.offsetWidth;
      outer.style.overflow = 'scroll';
      var inner = document.createElement('div');
      inner.style.width = '100%';
      outer.appendChild(inner);
      var widthWithScroll = inner.offsetWidth;
      outer.parentNode.removeChild(outer);
      return widthNoScroll - widthWithScroll;
    }

    // Force the table to resetWidth on window resize
    // this enables the header and the rows to be aligned
    function resizeBsTable() {
      element.bootstrapTable('resetWidth');
      element.bootstrapTable('resetView');
    }

    this.loader.getBsTableConfig().then(function(bstConfig) {
      var once = true;
      element.bootstrapTable('destroy');
      element.bootstrapTable(
          angular.extend({
            height: 250,
            sortable: true,
            onPostBody: function() {
              var trs = element.find('tbody').children();
              for (var i = 0; i < trs.length; i++) {
                $(trs[i]).mouseenter(function(e) {
                  // Hackish over event from:
                  // https://github.com/wenzhixin/bootstrap-table/issues/782
                  var row = $(e.currentTarget)
                  .parents('table')
                  .data()['bootstrap.table']
                  .data[$(e.currentTarget).data('index')];
                  if (!row) { return; }
                  var feature = this.loader.getFeatureFromRow(row);
                  var source = this.featuresTablesCtrl.fOverlay.getSource();
                  source.clear();
                  if (feature && feature.getGeometry()) {
                    source.addFeature(feature);
                  }
                }.bind(this));
                $(trs[i]).mouseleave(function(e) {
                  this.featuresTablesCtrl.fOverlay.getSource().clear();
                }.bind(this));
              }
              element.parents('gn-features-table').find('.clearfix')
              .addClass('sxt-clearfix')
              .removeClass('clearfix');

              // trigger an async digest loop to make the table appear
              setTimeout(function() { scope.$apply(); });
            }.bind(this),
            onPostHeader: function() { // avoid resizing issue on page change
              if (!once) { return; }
              element.bootstrapTable('resetView');
              once = false;
            },
            onDblClickRow: function(row, elt) {
              if (!this.map) {
                return;
              }
              var feature = this.loader.getFeatureFromRow(row);
              if (feature && feature.getGeometry()) {
                this.map.getView().fit(
                    feature.getGeometry(),
                    this.map.getSize(),
                    { minResolution: 40, duration: 500 }
                );
              }
            }.bind(this),
            showExport: true,
            exportTypes: ['csv'],
            exportDataType: 'all',
            locale: getBsTableLang()
          },bstConfig)
      );
      scope.$watch('ctrl.active', function() {
        element.bootstrapTable('resetWidth');
        element.bootstrapTable('resetView');
      });
    }.bind(this));
  };

  module.controller('gnFeaturesTableController', GnFeaturesTableController);

})();

