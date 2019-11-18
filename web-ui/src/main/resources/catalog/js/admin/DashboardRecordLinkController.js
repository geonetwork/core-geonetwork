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
  goog.provide('gn_dashboard_record_link_controller');

  goog.require('gn_utility_service');


  var module = angular.module('gn_dashboard_record_link_controller',
      ['gn_utility_service']);

  module.controller('GnDashboardRecordLinksController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', 'gnLangs', '$compile', 'gnHumanizeTimeService', '$window', 'getBsTableLang',
    function($scope, $routeParams, $http, $rootScope, $translate, gnLangs, $compile, gnHumanizeTimeService, $window, getBsTableLang) {

      $scope.analyzeLinks = function() {
        $http.post('../api/records/links?analyze=true');
      };

      $scope.removeAll = function() {
        $http.delete('../api/records/links');
      };

      $window.lastState = {ok: 'OK', ko: 'KO', unknown: '?'};

      $scope.bsTableControl = {
            options: {
                url: '../api/records/links',
                sidePagination: 'server',
                queryParamsType: 'page,size',
                contentType: 'application/x-www-form-urlencoded',
                method: 'get',
                pagination: true,
                paginationLoop: true,
                paginationHAlign: 'right',
                paginationVAlign: 'bottom',
                paginationDetailHAlign: 'left',
                paginationPreText: 'previous',
                paginationNextText: 'Next page',
                filterControl: true,
                style: 'min-height:100',
                classes: 'table table-responsive full-width',
                height: '800',
                sortName: 'lastState',
                sortOrder: 'desc',

                responseHandler: function(res) {
                  return {
                    rows: res.content,
                    total: res.totalElements,
                    pageNumber: res.number,
                    pageSize: res.size
                  };
                },

                queryParams: function(params) {
                  return {
                    filter: params.filter,
                    page: params.pageNumber - 1,
                    size: params.pageSize,
                    sort: params.sortName + ',' + params.sortOrder
                  };
                },

                columns: [{
                  field: 'lastState',
                  title: '',
                  titleTooltip: '',
                  filterControl: 'select',
                  filterData: 'var:lastState',
                  formatter: function(val, row) {
                    var _class = 'fa-question text-muted';
                    // as I can't upgrade bstable version, defining key so is a very dirty fix for
                    // https://github.com/wenzhixin/bootstrap-table/commit/961eed40b81b7133578e21358b5299629d642825
                    // key is bound with  $window.lastState key
                    var _key = 'unknown';
                    if (val == -1) {
                      _class = 'fa-exclamation-triangle text-danger';
                      _key = 'ko';
                    } else if (val == 1) {
                      _class = 'fa-check text-success';
                      _key = 'ok';
                    }
                    return '<div><i class="fa fa-fw fa-2x ' + _class + '"><p class="hidden">' + _key + '</p></i></div>';}.bind(this)},{

                  field: 'url',
                  title: $translate.instant('url'),
                  titleTooltip: $translate.instant('url'),
                  sortable: true,
                  filterControl: 'input',
                  filterControlPlaceholder: '',
                  formatter: function(val, row) {
                    return '<a href="' + row.url + '">' + row.url + '</a>';
                  }.bind(this)}, {

                  field: 'lastCheck',
                  title: $translate.instant('lastCheck'),
                  titleTooltip: $translate.instant('lastCheck'),
                  sortable: true,
                  formatter: function(val, row) {
                    if (row.lastCheck) {
                      return gnHumanizeTimeService(row.lastCheck.dateAndTime, null, false).value;
                    } else {
                      return '';
                    }}.bind(this)}, {

                  field: 'records',
                  title: $translate.instant('associatedRecords'),
                  titleTooltip: $translate.instant('associatedRecords'),
                  sortable: false,
                  formatter: function(val, row) {
                    var ulElem = '<ul>';
                    for (var i = 0; i < row.records.length; i++) {
                      var record = row.records[i];
                      var aElem = '<li><a href="catalog.search#/metadata/' + record.metadataUuid + '">' + record.metadataUuid + '</a></li>';
                      ulElem = ulElem + aElem;
                    }
                    ulElem = ulElem + '</ul>';
                    return ulElem;}.bind(this)}
                ],
                locale: getBsTableLang()
            }
        };
    }]);
})();
