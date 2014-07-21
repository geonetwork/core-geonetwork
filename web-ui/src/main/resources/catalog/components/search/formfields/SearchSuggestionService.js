(function() {
  goog.provide('gn_searchsuggestion_service');

  angular.module('gn_searchsuggestion_service', [
  ])
  .service('suggestService', [
        'gnHttpServices',
        'gnUrlUtils',

        function(gnHttpServices, gnUrlUtils) {

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
           * @param type
           * @returns {*}
           */
          this.getInfoUrl = function(type) {
            return gnUrlUtils.append(gnHttpServices.info,
                gnUrlUtils.toKeyValue({
                  type: type
                })
            );
          };


          /**
           * Must return an array of datum. It is contained
           * in the second element of the json response.
           *
           * @param data
           * @return {Array|null}
           */
          this.filterResponse = function(data) {
            return data[1];
          };

        }]);
})();
