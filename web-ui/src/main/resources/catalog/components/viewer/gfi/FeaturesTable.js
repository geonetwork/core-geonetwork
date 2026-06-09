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

(function () {
  goog.provide("gn_featurestable_directive");

  var module = angular.module("gn_featurestable_directive", ["gn_utility_service"]);

  module.directive("gnFeaturesTable", [
    "$http",
    "gfiTemplateURL",
    "getBsTableLang",
    "$translate",
    function ($http, gfiTemplateURL, getBsTableLang, $translate) {
      return {
        restrict: "E",
        scope: {
          loader: "=?gnFeaturesTableLoader",
          map: "=?gnFeaturesTableLoaderMap",
          active: "=gnActive",
          showExport: "=",
          height: "="
        },
        controllerAs: "ctrl",
        bindToController: true,
        controller: "gnFeaturesTableController",
        require: {
          featuresTablesCtrl: "^^gnFeaturesTables",
          ctrl: "gnFeaturesTable"
        },
        templateUrl: "../../catalog/components/viewer/gfi/partials/featurestable.html",
        link: function (scope, element, attrs, ctrls) {
          ctrls.ctrl.initTable(element.find("table"), scope, getBsTableLang, $translate);
        }
      };
    }
  ]);

  var GnFeaturesTableController = function () {};

  GnFeaturesTableController.prototype.initTable = function (
    element,
    scope,
    getBsTableLang,
    $translate
  ) {
    // See http://stackoverflow.com/a/13382873/29655
    function getScrollbarWidth() {
      var outer = document.createElement("div");
      outer.style.visibility = "hidden";
      outer.style.width = "100px";
      outer.style.msOverflowStyle = "scrollbar";
      document.body.appendChild(outer);
      var widthNoScroll = outer.offsetWidth;
      outer.style.overflow = "scroll";
      var inner = document.createElement("div");
      inner.style.width = "100%";
      outer.appendChild(inner);
      var widthWithScroll = inner.offsetWidth;
      outer.parentNode.removeChild(outer);
      return widthNoScroll - widthWithScroll;
    }

    // Force the table to resetView on window resize
    // this enables the header and the rows to be aligned
    function resizeBsTable() {
      element.bootstrapTable("resetView");
    }
    this.loader.loadAll();
    this.loader.getBsTableConfig().then(
      function (bstConfig) {
        var once = true;
        element.bootstrapTable("destroy");
        element.bootstrapTable(
          angular.extend(
            {
              // TODO Fixing the height breaks horizontal scroll.
              // For now setting height using CSS.
              // height: this.ctrl.height || 250,
              sortable: true,
              striped: true,
              showToggle: true,
              iconsPrefix: "fa",
              icons: {
                toggleOff: "fa-list-alt icon-list-alt",
                toggleOn: "fa-table icon-list-alt",
                export: "fa-download"
              },
              onPostBody: function (data) {
                var trs = element.find("tbody").children();
                for (var i = 0; i < trs.length; i++) {
                  $(trs[i]).mouseenter(
                    function (e) {
                      // Hackish over event from:
                      // https://github.com/wenzhixin/bootstrap-table/issues/782
                      var row = $(e.currentTarget).parents("table").data()[
                        "bootstrap.table"
                      ].data[$(e.currentTarget).data("index")];
                      if (!row) {
                        return;
                      }
                      var feature = this.loader.getFeatureFromRow(row);
                      var source = this.featuresTablesCtrl.highlightOverlay.getSource();
                      source.clear();
                      if (feature && feature.getGeometry()) {
                        source.addFeature(feature);
                      }
                    }.bind(this)
                  );

                  $(trs[i]).mouseleave(
                    function (e) {
                      this.featuresTablesCtrl.highlightOverlay.getSource().clear();
                    }.bind(this)
                  );
                }

                var d = element.bootstrapTable().data()["bootstrap.table"].data;
                if (angular.isArray(d)) {
                  var source = this.featuresTablesCtrl.featuresOverlay.getSource();
                  source.clear();
                  for (var i = 0; i < d.length; i++) {
                    var feature = this.loader.getFeatureFromRow(d[i]);
                    if (feature && feature.getGeometry()) {
                      source.addFeature(feature);
                    }
                  }
                }

                // element.parents('gn-features-table').find('.clearfix')
                // .addClass('sxt-clearfix')
                // .removeClass('clearfix');

                // trigger an async digest loop to make the table appear
                setTimeout(function () {
                  scope.$apply();
                });
              }.bind(this),
              onPostHeader: function () {
                // avoid resizing issue on page change
                // if (!once) { return; }
                // element.bootstrapTable('resetView');
                // once = false;
                // var el = $('.fixed-table-header');
                // setTimeout(function() {
                //   var width = getScrollbarWidth();
                //   if (parseInt(el.css('margin-right'), 10) > width) {
                //     el.css('margin-right', width + 'px');
                //   }
                // }, 0);
              },
              onDblClickRow: function (row, elt) {
                if (!this.map) {
                  return;
                }
                var feature = this.loader.getFeatureFromRow(row);
                if (feature && feature.getGeometry()) {
                  this.map.getView().fit(feature.getGeometry(), this.map.getSize(), {
                    minResolution: 40,
                    duration: 500
                  });
                }
              }.bind(this),
              showExport: this.ctrl.showExport !== false,
              exportTypes: ["csv"],
              exportDataType: "all",
              exportOptions: {
                onCellHtmlData: function onCellHtmlData(
                  cell,
                  rowIndex,
                  colIndex,
                  htmlData
                ) {
                  if (cell.is("th")) {
                    return cell.find(".th-inner").text();
                  } else if (cell.is("td") && cell.children("a").length > 0) {
                    return cell.find("a").context.innerHTML.match(/href="([^"]*)/)[1];
                  }
                  return htmlData;
                }
              },
              formatRecordsPerPage: function (pageNumber) {
                return pageNumber;
              },
              formatLoadingMessage: function () {
                return "...";
              },
              formatShowingRows: function (pageFrom, pageTo, totalRows) {
                return (
                  "" +
                  pageFrom +
                  " - " +
                  pageTo +
                  " " +
                  $translate.instant("resultXonY") +
                  " " +
                  totalRows +
                  " " +
                  $translate.instant("features")
                );
              },
              locale: getBsTableLang()
            },
            bstConfig
          )
        );
        scope.$watch("ctrl.active", resizeBsTable);
      }.bind(this)
    );
  };

  module.controller("gnFeaturesTableController", GnFeaturesTableController);
})();
