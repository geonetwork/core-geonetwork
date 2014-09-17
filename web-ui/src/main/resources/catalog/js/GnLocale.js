(function() {
  goog.provide('gn_locale');

  var module = angular.module('gn_locale', [
    'pascalprecht.translate',
    'angular-md5'
  ]);

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
        }).success(function(data, status, header, config) {
          deferredInst.resolve(data);
        }).error(function(data, status, header, config) {
          deferredInst.reject(options.key);
        });
      });

      // Finally, create a single promise containing all the promises
      // for each app module:
      var deferred = $q.all(allPromises);
      return deferred;
    };
  }]);


  // TODO: could be improved instead of putting this in all main modules ?
  module.config(['$translateProvider', '$LOCALES',
    function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);

})();
