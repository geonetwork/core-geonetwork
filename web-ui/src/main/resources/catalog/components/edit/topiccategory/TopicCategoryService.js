/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_topiccategory_service');

  var module = angular.module('gn_topiccategory_service', []);

  module.factory('TopicCategory', function() {
    function TopicCategory(k) {
      this.props = $.extend(true, {}, k);
      this.label = this.getLabel();
      this.tagClass = 'label label-info gn-line-height';
    };
    TopicCategory.prototype = {
      getId: function() {
        return this.props.id;
      },
      getLabel: function() {
        return this.props.value;
      }
    };

    return TopicCategory;
  });

  module.provider('gnTopicCategoryService',
      function() {
        this.$get = [
          '$q',
          '$rootScope',
          '$http',
          'gnUrlUtils',
          'TopicCategory',
          function($q, $rootScope, $http, gnUrlUtils, TopicCategory) {
            var getTopicCategoriesSearchUrl = function(schema) {
              return '../api/standards/' + (schema || 'iso19139') +
                     '/codelists/topicCategory';
            };

            var parseTopicCategoriesResponse = function(data, dataToExclude) {
              var listOfTopicCategories = [];
              angular.forEach(data, function(value, key) {
                if (value) {
                  listOfTopicCategories.push(new TopicCategory({id: key, value: value}));
                }
              });

              if (dataToExclude && dataToExclude.length > 0) {
                // Remove from search already selected topic categories
                listOfTopicCategories = $.grep(listOfTopicCategories, function(n) {
                  var isSelected = false;
                  isSelected = $.grep(dataToExclude, function(s) {
                    return s.getLabel() === n.getLabel();
                  }).length !== 0;
                  return !isSelected;
                });
              }
              return listOfTopicCategories;
            };


            return {
              /**
             * Number of topic categories to display in autocompletion list
             */
              getTopicCategoryAutocompleter: function(config) {
                var topicCategoryAutocompleter = new Bloodhound({
                  datumTokenizer: Bloodhound.tokenizers.obj.whitespace('label'),
                  queryTokenizer: Bloodhound.tokenizers.whitespace,
                  identify: function(obj) { return obj.getLabel(); },
                  sorter:
                  function(a, b) {
                    var nameA = a.getLabel().toUpperCase();
                    var nameB = b.getLabel().toUpperCase();
                    if (nameA < nameB) {
                      return -1;
                    }
                    if (nameA > nameB) {
                      return 1;
                    }
                    return 0;
                  },
                  limit: 100,
                  prefetch: {
                    url: getTopicCategoriesSearchUrl(config.schema),
                    prepare: function (settings) {
                      settings.headers = {
                        'Accept-Language': config.lang
                      };

                      return settings;
                    },
                    cache: false,
                    transform: function(response) {
                      var r = parseTopicCategoriesResponse(response, config.dataToExclude);
                      return r;
                    }
                  }
                });
                topicCategoryAutocompleter.initialize();
                return topicCategoryAutocompleter;
              },

              getTopicCategories: function() {
                var defer = $q.defer();
                var url = getTopicCategoriesSearchUrl();
                $http.get(url, { cache: true }).
                success(function(data) {
                  defer.resolve(parseTopicCategoriesResponse(data));
                }).
                error(function(data, status) {
                });
                return defer.promise;
              },

              suggest: null
            };
          }];
      });
})();
