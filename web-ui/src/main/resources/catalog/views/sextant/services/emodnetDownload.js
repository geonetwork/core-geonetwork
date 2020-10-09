(function() {

  goog.provide('sxt_emodnetdownload');

  var module = angular.module('sxt_emodnetdownload', []);

  /**
   * @ngdoc service
   * @kind function
   * @name sxtEmodnetDownload
   * @requires $rootScope
   * @requires $q
   * @requires gnPopup
   *
   * @description
   * This services handles the case where a form must be submitted before
   * downloading a file attached to a record.
   * The data filled in this form is then sent to a custom analytics service
   * using GET query parameters.
   * Multiple URLs are handled and the download of each one should be triggered
   * once the form is submitted.
   * The form is described in a separate HTML template that is included
   * dynamically in a popup.
   *
   * A Sextant API settings must be set to true: useEmodnetDownloadForm
   */
  module.factory('sxtEmodnetDownload', [
    '$rootScope',
    '$q',
    '$http',
    'gnPopup',
    function($rootScope, $q, $http, gnPopup) {
      var modal = null;

      return {
        /**
         * Will show the download form and, when submitted, will open all urls
         * to download files
         * Note: The API setting sxtSettings.useEmodnetDownloadForm must be set to true
         * @param {Array.<string>} urls
         * @param {string} mdUuid
         * @return a $q.defer object that resolves when the submission to the
         * analytics service is done
         */
        openDownloadForm: function(urls, mdUuid) {
          var scope = $rootScope.$new(true);
          scope.values = {};

          var sendAnalyticsReport = function (values) {
            // hardcoded values
            // see: https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/223
            values['service'] = '2'; // '2' is Sextant
            values['api'] = '2dbSmaEG4Ac27SYT';
            values['uuid'] = mdUuid;

            $http.get('https://nodc.inogs.it/emodnet-dev/extranet/analytics', {
              params: values
            });
          }

          scope.submit = function() {
            // for each url, send tracking values & open for download
            urls.forEach(function(url) {
              sendAnalyticsReport(scope.values);
              window.open(url);
            });

            modal.modal('hide');
          };

          var templateUrl = '../../catalog/views/sextant/services/emodnetdownloadform.html'
          modal = gnPopup.createModal({
            title: 'emodnetDownloadForm',
            content: '<div ng-include="\'' + templateUrl + '\'"></div>'
          }, scope);
        },

        /**
         * Returns true if the link must be hidden behind a download form
         */
        requiresDownloadForm: function(link) {
          if (typeof sxtSettings === 'undefined' ||
              !sxtSettings.useEmodnetDownloadForm) {
            return;
          }

          if (!link.url || !link.protocol) { return false; }

          return link.protocol === 'WWW:DOWNLOAD-1.0-link--download' &&
            !link.url.startsWith('http://doi.org') &&
            !link.url.startsWith('https://doi.org') &&
            !link.url.startsWith('http://dx.doi.org') &&
            !link.url.startsWith('https://dx.doi.org');
        }
      };
    }
  ]);
})();
