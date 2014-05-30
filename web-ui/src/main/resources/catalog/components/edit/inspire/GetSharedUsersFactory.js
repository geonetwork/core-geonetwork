(function() {
  'use strict';
  goog.provide('inspire_get_shared_users_factory');

  var module = angular.module('inspire_get_shared_users_factory', []);
  /**
   * Returns results of shared users request:
   *
   * {
   *     "url":"local://shared.user.edit?closeOnSave&id=5&validated=n&operation=fullupdate",
   *     "id":"5",
   *     "type":"contact",
   *     "xlink":"local://xml.user.get?id=5*",
   *     "desc":"&lt;geodata@swisstopo.ch&gt;",
   *     "search":"5 &lt;geodata@swisstopo.ch&gt;"
   * }
   */
  module.factory('inspireGetSharedUsersFactory', [ '$http', '$q', function($http, $q) {
    return {
      loadDetails: function(url, userId) {
        var deferred = $q.defer();

        $http.get(url + 'plain.xml.user.get@json?id=' + userId).success(function(data) {
          data = data.record;
          var lang, threeLetterCode;
          var langMap = {
            'DE': 'ger',
            'EN': 'eng',
            'FR': 'fre',
            'IT': 'ita',
            'RM': 'roh'
          };
          var user = {
            id: userId,
            name: data.name || '',
            surname: data.surname || '',
            email: data.email || '',
            organization: {},
            validated: data.validated === 'y'
          };

          for (lang in data.organisation) {
            if (data.organisation.hasOwnProperty(lang)) {
              threeLetterCode = langMap[lang];
              user.organization[threeLetterCode] = data.organisation[lang];
            }
          }

          deferred.resolve(user);
        }).error(function (data) {
          deferred.reject(data);
        });
        return deferred.promise;
      },
      loadAll: function(url) {
        var deferred = $q.defer();
        var validated = false, nonValidated = false;
        var users = {
          validated: [],
          nonValidated: []
        };
        var error = function (error) {
          deferred.reject(error);
        };
        var contactURL = url + 'reusable.list.js?type=contacts&validated=';

        var processData = function(data) {
          var i;
          if (data.indexOf("<") !== 0) {
            for (i = 0; i < data.length; i++) {
              if (data[i].url) {
                data[i].url = data[i].url.replace(/local:\/\//g, '');
              }
              if (data[i].desc) {
                data[i].desc = data[i].desc.replace(/\&lt;/g, '<').replace(/\&gt;/g, '>');
              } else {
                data[i].desc = 'No description provided';
              }
            }

            return data;
          }
          return undefined;
        };

        $http.get(contactURL + false).then (
          function success(data) {
            nonValidated = true;

            users.nonValidated = processData(data.data);
            if (validated) {
              deferred.resolve(users);
            }
          },
          error
        );

        $http.get(contactURL + true).then (
          function success(data) {
            validated = true;
            users.validated = processData(data.data);
            if (nonValidated) {
              deferred.resolve(users);
            }
          },
          error
        );

        return deferred.promise;
      }
    };
  }]);
}());

