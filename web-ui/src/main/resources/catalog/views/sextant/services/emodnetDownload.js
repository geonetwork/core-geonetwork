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
        /**
         * Will show the download form and, when submitted, will open all urls
         * to download files
         * @param {Array.<string>} urls
         * @return a $q.defer object that resolves when the submission to the
         * analytics service is done
         */
        openDownloadForm(urls) {
          var scope = $rootScope.$new(true);
          scope.submit = function() {
            var defer = $q.defer();

            setTimeout(function() {
              urls.forEach(function(url) {
                console.log(url);
              });
              defer.resolve();
              modal.modal('hide');
            }, 1000);

            return defer.promise;
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
