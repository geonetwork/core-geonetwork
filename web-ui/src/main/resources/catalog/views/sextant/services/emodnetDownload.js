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
   * The data filled in this form is then sent to a Matomo server (see
   * https://developer.matomo.org/guides/tracking-javascript-guide) using
   * hardcoded parameters.
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
    'gnPopup',
    function($rootScope, $q, gnPopup) {
      var modal = null;

      return {
        initialized: false,

        /**
         * This will add the Matomo/Piwik library to the page
         * Must be called when opening the form for the first time
         */
        importLibrary: function() {
          if (this.initialized) return;

          // global tracking array
          window._paq = window._paq || []

          var url = "//piwik.vliz.be/";
          _paq.push(['setTrackerUrl', url + 'piwik.php']);
          _paq.push(['setSiteId', '23']);

          var scriptTag = document.createElement('script');
          scriptTag.type = 'text/javascript';
          scriptTag.async = true;
          scriptTag.defer = true;
          scriptTag.src = url + 'piwik.js';
          var firstScriptTag = document.getElementsByTagName('script')[0];
          var header = firstScriptTag.parentNode;
          header.insertBefore(scriptTag, firstScriptTag);

          this.initialized = true;
        },

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
          this.importLibrary();

          var scope = $rootScope.$new(true);
          scope.values = {};

          var category = 'Download_form';

          var addTrackingValues = function (url, values) {
            // hardcoded tracking values
            values['DownloadForm-sender_id'] = 'sextant';
            values['DownloadForm-data_url'] = url;
            values['DownloadForm-UUID'] = mdUuid;

            Object.keys(values).forEach(function(key) {
              if (values[key]) {
                _paq.push(['trackEvent', category, key, values[key]]);
              }
            });
            _paq.push(['trackEvent', category, 'DownloadForm-jsondata', JSON.stringify(values)]);
          }

          scope.submit = function() {
            // for each url, send tracking values & open for download
            urls.forEach(function(url) {
              addTrackingValues(url, scope.values);
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
