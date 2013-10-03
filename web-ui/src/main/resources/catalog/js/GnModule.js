(function() {
  goog.provide('gn');














  goog.require('gn_cat_controller');
  goog.require('gn_language_switcher');
  goog.require('gn_metadata_manager');
  goog.require('gn_pagination');
  goog.require('gn_search_controller');
  goog.require('gn_search_manager');
  goog.require('gn_search_results');
  goog.require('gn_translation');
  goog.require('gn_translation_controller');
  goog.require('gn_utility_service');

  var module = angular.module('gn', [
    'ngRoute',
    'pascalprecht.translate',
    'gn_language_switcher',
    'gn_utility_service',
    'gn_search_manager',
    'gn_metadata_manager',
    'gn_search_results',
    'gn_pagination',
    'gn_translation_controller',
    'gn_cat_controller',
    'gn_search_controller'
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
      var pathArray = window.location.pathname.split('/');
      var lang = pathArray[3][0] + pathArray[3][1];
      
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);

    }]);

})();
