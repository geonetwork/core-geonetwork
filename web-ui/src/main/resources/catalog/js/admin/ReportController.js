(function () {
  goog.provide('gn_report_controller');

  var module = angular.module('gn_report_controller',
    []);


  /**
   * ReportController provides all necessary operations
   * to build reports.
   */
  module.controller('GnReportController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate',
    function ($scope, $routeParams, $http, $rootScope, $translate) {


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
      $scope.report.suggestedDate = "";

      $scope.report.dateFrom = getCurrentDate();
      $scope.report.dateTo = getCurrentDate();

      /**
       * Creates the records updated report
       */
      $scope.createReport = function (formId, service) {
        $http({
          method: 'POST',
          url: service,
          data: $(formId).serialize(),
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
          .success(function(data) {
            // Download the csv file. The AngularJs friendly way!
            var element = angular.element('<a/>');
            element.attr({
              href: 'data:attachment/csv;charset=utf-8,' + encodeURI(data),
              target: '_blank',
              download: service
            })[0].click();

          })
      };

      /**
       * Listener for suggested date range selection to update the date controls with the date range selected.
       */
      $scope.$watch(
        "report.suggestedDate",
        function( newValue, oldValue ) {

          // Ignore empty value, initial setup and if form already mirrors new value.
          if ((newValue === "") || (newValue === oldValue)  || ($scope.report.suggestedDate.value === newValue)) {
            return;
          }

          // Calculate the dateFrom and dateTo values
          var today = new Date();

          if (newValue === "currentMonth") {
            var month = (today.getMonth() + 1 < 10) ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1)
            var year = today.getFullYear();

            $scope.report.dateFrom  = year + "-" + month + "-" + "01";;
            $scope.report.dateTo =  year + "-" + month + "-" + daysInMonth(today.getMonth(), today.getYear());

          } else if (newValue === "previousMonth") {
            // Set previous month
            today.setMonth(today.getMonth() - 1);

            var month = (today.getMonth() + 1 < 10) ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1)
            var year = today.getFullYear();

            $scope.report.dateFrom = year + "-" + month + "-" + "01"; // + "T00:00:00.000Z";
            $scope.report.dateTo  = year + "-" + month + "-" + daysInMonth(today.getMonth(), today.getYear());

          } else if (newValue == "currentYear") {
            var year = today.getFullYear();

            $scope.report.dateFrom  = year + "-" + "01" + "-" + "01";
            $scope.report.dateTo = year + "-" + "12" + "-" + "31";

          } else if (newValue == "previousYear") {
            // Set previous year
            var year = today.getFullYear() -1 ;

            $scope.report.dateFrom  = year + "-" + "01" + "-" + "01";
            $scope.report.dateTo = year + "-" + "12" + "-" + "31";

          }
        });

      // http://dzone.com/snippets/determining-number-days-month
      function daysInMonth(iMonth, iYear)     {
        return 32 - new Date(iYear, iMonth, 32).getDate();
      }

      function getCurrentDate() {
        var today = new Date();
        var day = (today.getDate() < 10) ? ("0" + today.getDate()) : (today.getDate());
        var month = (today.getMonth() + 1 < 10) ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1);
        var year = today.getFullYear();

        return year + "-" + month + "-" + day;
      }

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
