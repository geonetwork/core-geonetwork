(function() {
  goog.provide('gn_report_controller');

  var module = angular.module('gn_report_controller',
      []);

  /**
   * ReportController provides all necessary operations
   * to build reports.
   */
  module.controller('GnReportController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate',
    function($scope, $routeParams, $http, $rootScope, $translate) {


      $scope.pageMenu = {
        folder: 'report/',
        defaultTab: 'report-updated-metadata',
        tabs:
            [{
              type: 'report-updated-metadata',
              label: 'reportUpdatedMetadata',
              icon: 'fa-th',
              href: '#/reports/report-updated-metadata'
            },{
              type: 'report-internal-metadata',
              label: 'reportInternalMetadata',
              icon: 'fa-th',
              href: '#/reports/report-internal-metadata'
            },{
              type: 'report-fileupload-metadata',
              label: 'reportFileUploadMetadata',
              icon: 'fa-th',
              href: '#/reports/report-fileupload-metadata'
            },{
              type: 'report-filedownload-metadata',
              label: 'reportFileDownloadMetadata',
              icon: 'fa-th',
              href: '#/reports/report-filedownload-metadata'
            },{
              type: 'report-users',
              label: 'reportUsers',
              icon: 'fa-th',
              href: '#/reports/report-users'
            }]
      };

      $scope.groups = null;
      $scope.report = {};
      $scope.report.suggestedDate = '';

      $scope.report.dateFrom = new Date(moment().format('YYYY-MM-DD'));
      $scope.report.dateTo = new Date(moment().format('YYYY-MM-DD'));

      /**
       * Creates the records updated report
       */
      $scope.createReport = function(formId, service) {
        $(formId).attr('action', service);
        $(formId).submit();
      };

      /**
       * Listener for suggested date range selection to update
       * the date controls with the date range selected.
       */
      $scope.$watch(
          'report.suggestedDate',
          function(newValue, oldValue) {

            // Ignore empty value: in initial setup and
            // if form already mirrors new value.
            if ((newValue === '') || (newValue === oldValue) ||
                ($scope.report.suggestedDate.value === newValue)) {
              return;
            }

            // Calculate the dateFrom and dateTo values
            var today = moment();

            if (newValue === 'currentMonth') {
              var month = today.format('MM');
              var year = today.format('YYYY');

              $scope.report.dateFrom =
                  new Date(year + '-' + month + '-' + '01');
              $scope.report.dateTo = new Date(year + '-' + month + '-' +
                  today.daysInMonth());

            } else if (newValue === 'previousMonth') {
              // Set previous month
              today.add('months', -1);

              var month = today.format('MM');
              var year = today.format('YYYY');

              $scope.report.dateFrom =
                  new Date(year + '-' + month + '-' + '01');
              $scope.report.dateTo = new Date(year + '-' + month + '-' +
                  today.daysInMonth());

            } else if (newValue == 'currentYear') {
              var year = today.format('YYYY');

              $scope.report.dateFrom = new Date(year + '-' + '01' + '-' + '01');
              $scope.report.dateTo = new Date(year + '-' + '12' + '-' + '31');

            } else if (newValue == 'previousYear') {
              // Set previous year
              today.add('year', -1);

              var year = today.format('YYYY');

              $scope.report.dateFrom = new Date(year + '-' + '01' + '-' + '01');
              $scope.report.dateTo = new Date(year + '-' + '12' + '-' + '31');

            }
          });

      function loadGroups() {
        $http.get('admin.group.list@json').success(function(data) {
          $scope.groups = data !== 'null' ? data : null;
        }).error(function(data) {
          // TODO
        });
      }

      loadGroups();

    }]);

})();
