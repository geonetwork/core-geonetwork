(function() {
  goog.provide('gn_locale');
  goog.require('gn_cat_controller');

  var module = angular.module('gn_locale', [
    'pascalprecht.translate',
    'angular-md5',
    'gn_cat_controller'
  ]);

  module.constant('$LOCALES', ['core']);

  module.factory('localeLoader', ['$http', '$q', 'gnLangs',
    function($http, $q, gnLangs) {
      return function(options) {

        function buildUrl(prefix, lang, value, suffix) {
          if (value.indexOf('/') === 0) {
            return value.substring(1);
          } else {
            return prefix + gnLangs.getIso2Lang(lang) + '-' + value + suffix;
          }
        };
        var allPromises = [];
        angular.forEach(options.locales, function(value, index) {
          var langUrl = buildUrl(options.prefix, options.key,
              value, options.suffix);

          var deferredInst = $q.defer();
          allPromises.push(deferredInst.promise);

          $http({
            method: 'GET',
            url: langUrl,
            headers: {
              'Accept-Language': options.key
            }
          }).success(function(data) {
            deferredInst.resolve(data);
          }).error(function() {
            // Load english locale file if not available
            $http({
              method: 'GET',
              url: buildUrl(options.prefix, 'en', value, options.suffix)
            }).success(function(data) {
              deferredInst.resolve(data);
            }).error(function() {
              deferredInst.reject(options.key);
            });
          });
        });

        // Finally, create a single promise containing all the promises
        // for each app module:
        var deferred = $q.all(allPromises);
        return deferred;
      };
    }]);


  // TODO: could be improved instead of putting this in all main modules ?
  module.config(['$translateProvider', '$LOCALES', 'gnGlobalSettings',
    'gnLangs',
    function($translateProvider, $LOCALES, gnGlobalSettings, gnLangs) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: (gnGlobalSettings.locale.path || '../../') + 'catalog/locales/',
        suffix: '.json'
      });

      gnGlobalSettings.iso3lang = gnGlobalSettings.locale.iso3lang ||
          location.href.split('/')[5] || 'eng';
      gnGlobalSettings.lang = gnLangs.getIso2Lang(gnGlobalSettings.iso3lang);
      $translateProvider.preferredLanguage(gnGlobalSettings.iso3lang);
      moment.lang(gnGlobalSettings.lang);
    }]);

})();
