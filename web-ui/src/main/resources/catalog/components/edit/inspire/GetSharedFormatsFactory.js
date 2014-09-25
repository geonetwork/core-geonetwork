(function() {
  'use strict';
  goog.provide('inspire_get_shared_formats_factory');

  var module = angular.module('inspire_get_shared_formats_factory', []);
  module.factory('inspireGetFormatsFactory', [ '$http', '$q', function($http, $q) {
    return {
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
        var formatURL = url + 'reusable.list.js?type=formats&validated=';

        var processData = function(data, validated) {
          var i, format, formats, parts;
          if (data.indexOf("<") !== 0) {
            data.sort(function(u1, u2) {
              var startsWithCharacter = /^[0-9a-zA-Z]/;
              if (startsWithCharacter.test(u1.desc) && !startsWithCharacter.test(u2.desc)) {
                return -1;
              } else if (!startsWithCharacter.test(u1.desc) && startsWithCharacter.test(u2.desc)) {
                return 1;
              }
              return u1.desc.localeCompare(u2.desc);
            });

            formats = [];
            for (i = 0; i < data.length; i++) {
              if (data[i].desc) {
                parts = /(.*) \((.*)\)/.exec(data[i].desc);
              } else {
                parts = ["", "", ""];
              }

              format = {};
              format.id = data[i].id;
              format.name = parts[1];
              format.version = parts[2];
              format.validated = validated;

              formats.push(format);
            }

            return formats;
          }
          return undefined;
        };

        $http.get(formatURL + false).then (
          function success(data) {
            nonValidated = true;

            users.nonValidated = processData(data.data, false);
            if (validated) {
              deferred.resolve(users);
            }
          },
          error
        );

        $http.get(formatURL + true).then (
          function success(data) {
            validated = true;
            users.validated = processData(data.data, true);
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

