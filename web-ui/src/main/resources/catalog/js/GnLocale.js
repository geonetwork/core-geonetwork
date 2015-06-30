(function() {
  goog.provide('gn_locale');
  goog.require('gn_cat_controller');

  var module = angular.module('gn_locale', [
    'pascalprecht.translate',
    'angular-md5',
    'gn_cat_controller'
  ]);

  module.constant('$LOCALE_MAP', function(threeCharLang) {
    var specialCases = {
      "spa" : "es",
      "ger" : "de",
      "bra" : "pt_BR",
      "swe" : "sv",
      "tur" : "tr",
      "por" : "pt",
      "gre" : "el",
      "per" : "fa",
      "chi" : "zh",
      "pol" : "pl",
      "wel" : "cy",
      "dut" : "nl"
    };
    var lang = specialCases[threeCharLang];
    if (angular.isDefined) {
      return lang;
    }

    return threeCharLang.substring(0, 2) || 'en';
  });
  module.constant('$LOCALES', ['core']);

  module.factory('localeLoader', ['$http', '$q', function($http, $q) {
    return function(options) {
      var allPromises = [];
      angular.forEach(options.locales, function(value, index) {
        var langUrl = options.prefix +
            options.key + '-' + value + options.suffix;

        var deferredInst = $q.defer();
        allPromises.push(deferredInst.promise);

        $http({
          method: 'GET',
          url: langUrl
        }).success(function(data) {
          deferredInst.resolve(data);
        }).error(function() {
          // Load english locale file if not available
          $http({
            method: 'GET',
            url: options.prefix +
                'en-' + value + options.suffix
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
  module.config(['$translateProvider', '$LOCALES', '$LOCALE_MAP', 'gnGlobalSettings',
    function($translateProvider, $LOCALES, $LOCALE_MAP, gnGlobalSettings) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: (gnGlobalSettings.locale.path || '../../') + 'catalog/locales/',
        suffix: '.json'
      });

      gnGlobalSettings.lang = gnGlobalSettings.locale.lang || $LOCALE_MAP(location.href.split('/')[5]);
      $translateProvider.preferredLanguage(gnGlobalSettings.lang);
      moment.lang(gnGlobalSettings.lang);
    }]);

})();
