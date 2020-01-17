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

      $scope.groupId = '';

      $scope.triggerSearch = function () {
        $('#bstable').bootstrapTable('refresh')
      }

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
                    groupIdFilter: $scope.groupId,
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
                    return '<a href="' + row.url + '" target="_blank">' + row.url + '</a>';
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
                  filterControl: 'input',
                  filterControlPlaceholder: '',
                  formatter: function(val, row) {
                    var ulElem = '<ul>';
                    for (var i = 0; i < row.records.length; i++) {
                      var record = row.records[i];
                      var aElem = '<li><a href="catalog.search#/metadata/' + record.metadataUuid + '" target="_blank">' + record.metadataUuid + '</a></li>';
                      ulElem = ulElem + aElem;
                    }
                    ulElem = ulElem + '</ul>';
                    return ulElem;}.bind(this)}
                ],
                locale: getBsTableLang()
            }
        };
    }]);

  var STATUS_UNDEFINED = 0;
  var STATUS_PROBABLE = 1;
  var STATUS_INPROGRESS = 2;
  var STATUS_FINISHED = 3;
  var STATUS_ERRORS = 4;

  var ANALYZE_RECORD_LABEL = [
    'taskUndefined',
    'taskProbable',
    'analyseRecordRunning',
    'analyseRecordFinished',
    'analyseRecordFinishedWithErrors'
  ];

  var TEST_LINK_LABEL = ['taskUndefined', 'taskProbable', 'testLinkRunning', 'testLinkFinished'];
  var ICON = ['fa-question', 'fa-question', 'fa-spinner fa-spin', 'fa-check', 'fa-exclamation-triangle'];
  var CLASS = ['', '', '', 'success', 'warning'];

  module.directive('gnDashboardRecordLinksProcessesContainer', ['$http', function($http) {
    return {
      restrict: 'E',
      scope: {},
      templateUrl: '../../catalog/components/admin/recordlink/partials/recordlinksanalyseprocesscontainer.html',
      link: function(scope, element, attrs) {},
      controllerAs: 'ctrl',
      controller: ['$scope', '$element', '$attrs', function($scope, $element, $attrs) {
        this.tasks = [];
        var me = this;

        this.getStatusCode = function(errors, processed, total) {
           if (total === -1) {
             return STATUS_PROBABLE;
           }
           if (total > processed + errors) {
             return STATUS_INPROGRESS;
           }
           if (total === processed) {
             return STATUS_FINISHED;
           }
           if (total === processed + errors) {
             return STATUS_ERRORS;
           }
           return STATUS_UNDEFINED;
         };

        this.getProcessRatio = function(processedErrorOrNot, total) {
           return Math.round(1000 * processedErrorOrNot / total) * 0.001;
        };

        this.refresh = function() {
          $http.get('../../jolokia/read/geonetwork:name=url-check,idx=*').then(function(result) {

            if (!result.data || !result.data.value) { return; }

            me.tasks = [];
            var probes = Object.values(result.data.value);
            probes.sort(function(a, b) {return b.AnalyseMdDate - a.AnalyseMdDate;});
            probes.forEach(function(probe) {
              var probeName = probe.ObjectName.objectName;
              if (probeName && !probeName.includes('empty-slot')) {
                var analyzeRecordStatus = me.getStatusCode(probe.MetadataNotAnalysedInError, probe.MetadataAnalysed, probe.MetadataToAnalyseCount);
                var testLinkStatus = me.getStatusCode(0, probe.UrlChecked, probe.UrlToCheckCount);
                me.tasks.push({
                  id: probeName,
                  records: {
                    errors: probe.MetadataNotAnalysedInError,
                    processed: probe.MetadataAnalysed,
                    total: probe.MetadataToAnalyseCount,
                    label: ANALYZE_RECORD_LABEL[analyzeRecordStatus],
                    class: CLASS[analyzeRecordStatus],
                    icon: ICON[analyzeRecordStatus],
                    ratio: me.getProcessRatio(
                      probe.MetadataNotAnalysedInError + probe.MetadataAnalysed,
                      probe.MetadataToAnalyseCount)
                  },
                  links: {
                    errors: 0,
                    processed: probe.UrlChecked,
                    total: probe.UrlToCheckCount,
                    label: TEST_LINK_LABEL[testLinkStatus],
                    class: CLASS[testLinkStatus],
                    icon: ICON[testLinkStatus],
                    ratio: me.getProcessRatio(probe.UrlChecked, probe.UrlToCheckCount)
                  }
                });
              }
            });
            setTimeout(me.refresh, 1000);
          });
        };

        this.refresh();
      }]
    };
  }]);

  module.directive('gnDashboardRecordLinksProcessesStatus', [
    function() {
      return {
        restrict: 'E',
        scope: {taskInfo: '<'},
        templateUrl: '../../catalog/components/admin/recordlink/partials/recordlinksanalyseprocessstatus.html',
        link: function(scope, element, attrs) {}
    };
  }]);
})();
