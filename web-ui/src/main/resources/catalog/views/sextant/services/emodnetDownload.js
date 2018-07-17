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
        importLibrary() {
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
         * @param {Array.<string>} urls
         * @return a $q.defer object that resolves when the submission to the
         * analytics service is done
         */
        openDownloadForm(urls) {
          this.importLibrary();

          var scope = $rootScope.$new(true);

          // hardcoded tracking values
          scope.values = {
            'DownloadForm-sender_id': 'sextant',
            'DownloadForm-data_url': urls[0]
          };

          var category = 'Download_form';

          scope.submit = function() {
            Object.keys(scope.values).forEach(function(key) {
              if (scope.values[key]) {
                _paq.push(['trackEvent', category, key, scope.values[key]]);
              }
            });
            _paq.push(['trackEvent', category, 'DownloadForm-jsondata', JSON.stringify(scope.values)]);

            // open each url for download
            urls.forEach(function(url) {
              window.open(url);
            });
            modal.modal('hide');
          };

          modal = gnPopup.createModal({
            title: 'emodnetDownloadForm',
            content: '<div ng-include="\'../../catalog/views/sextant/services/' +
            'partials/emodnetdownloadform.html\'"></div>'
          }, scope);
        }
      };
    }
  ]);
})();
