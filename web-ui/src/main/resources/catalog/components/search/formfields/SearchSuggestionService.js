(function() {
  goog.provide('gn_searchsuggestion_service');

  angular.module('gn_searchsuggestion_service', [
  ])

  /**
   * @ngdoc service
   * @kind function
   * @name gn_searchsuggestion.service:suggestService
   * @requires gnHttpServices
   * @requires gnUrlUtils
   * @requires $http
   *
   * @description
   * The `suggestService` service provides all tools required to get
   * suggestions from the index.
   */

      .service('suggestService', [
        'gnHttpServices',
        'gnUrlUtils',
        '$http',
        function(gnHttpServices, gnUrlUtils, $http) {

          /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#getUrl
           *
           * @description
           * Build the suggestion url with given parameters.
           *
           * @param {string} filter the filter for search query.
           * @param {string} field index field you search on.
           * @param {string} sortBy option.
           * @return {string} service
           */
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
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#getInfoUrl
           *
           * @description
           * Return info service url, depending on the type.
           *
           * @param {string} type of info
           * @return {string} url
           */
          this.getInfoUrl = function(type) {
            return gnUrlUtils.append(gnHttpServices.info,
                gnUrlUtils.toKeyValue({
                  type: type
                })
            );
          };

          /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#getAnySuggestions
           *
           * @description
           * Return suggestion for field 'any'
           *
           * @param {string} val any filter
           * @return {HttpPromise} promise
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
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#filterResponse
           *
           * @description
           * Must return an array of datum. It is contained
           * in the second element of the json response.
           *
           * @param {Array} data needed to be filtered
           * @return {?Array} suggestions array
           */
          this.filterResponse = function(data) {
            return data[1];
          };

          /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#bhFilter
           *
           * @description
           * Must return an array of datum objects(id/name). It is contained
           * in the second element of the json response.
           *
           * @param {Array} data needed to be filtered
           * @return {?Array} suggestions array
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
