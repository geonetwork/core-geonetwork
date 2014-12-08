(function() {
  goog.provide('gn_searchsuggestion_service');

  angular.module('gn_searchsuggestion_service', [
  ])
  .service('suggestService', [
        'gnHttpServices',
        'gnUrlUtils',
        '$http',
        function(gnHttpServices, gnUrlUtils, $http) {

          this.getUrl = function(filter, field, sortBy) {
            return gnUrlUtils.append(gnHttpServices.suggest,
                gnUrlUtils.toKeyValue({
                  field: field,
                  sortBy: sortBy,
                  q: filter || ''
                })
            );
          };

          /**
           * Return info service url, depending on the type.
           * Exemple : `geonetwork/srv/eng/info@json?type=categories`
           * @param {string} type
           * @return {*}
           */
          this.getInfoUrl = function(type) {
            return gnUrlUtils.append(gnHttpServices.info,
                gnUrlUtils.toKeyValue({
                  type: type
                })
            );
          };

          /**
           * Return suggestion for field 'any'
           * @param {string} val
           * @return {*}
           */
          this.getAnySuggestions = function(val) {
            var url = this.getUrl(val, 'anylight',
                ('STARTSWITHFIRST'));

            return $http.get(url, {
            }).then(function(res) {
              return res.data[1];
            });
          };


          /**
           * Must return an array of datum. It is contained
           * in the second element of the json response.
           *
           * @param {Array} data
           * @return {?Array}
           */
          this.filterResponse = function(data) {
            return data[1];
          };

          /**
           * Must return an array of datum objects(id/name). It is contained
           * in the second element of the json response.
           *
           * @param {Array} data
           * @return {?Array}
           */
          this.bhFilter = function(data) {
            var datum = [];
            data[1].forEach(function(item) {
              datum.push({ id: item, name: item });
            });
            return datum;
          };

        }]);
})();
