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

(function () {
  goog.provide("gn_thesaurus_service");

  var module = angular.module("gn_thesaurus_service", []);

  module.factory("Keyword", function () {
    function Keyword(k, UILangs) {
      this.UILangs = UILangs;
      this.props = $.extend(true, {}, k);
      this.label = this.getLabel();
      this.tagClass = "label label-info gn-line-height";
    }
    Keyword.prototype = {
      getId: function () {
        return this.props.uri;
      },
      getLabel: function () {
        if (this.props.values) {
          var UILangs = this.UILangs;
          var props = this.props;
          var foundLang = _.find(UILangs, function (l) {
            return props.values[l] !== undefined;
          });
          if (foundLang) return props.values[foundLang] || props.values["eng"];
        }
        return this.props.value["#text"] || this.props.value;
      }
    };

    return Keyword;
  });

  module.factory("Thesaurus", function () {
    function Thesaurus(k, UILangs) {
      this.UILangs = UILangs;
      this.props = $.extend(true, {}, k);
    }
    Thesaurus.prototype = {
      getKey: function () {
        return this.props.key;
      },
      getTitle: function () {
        var title = this.props.title;
        //there are multilingual
        if (this.props.multilingualTitles && this.props.multilingualTitles.length > 0) {
          var UILangs = this.UILangs;
          var props = this.props;

          var foundLang = _.find(UILangs, function (l) {
            return _.find(props.multilingualTitles, function (ml) {
              return ml["lang"] == l;
            });
          });
          if (foundLang) {
            var r = _.find(props.multilingualTitles, function (ml) {
              return ml["lang"] == foundLang;
            });
            return r["title"];
          }
        }
        return title;
      },
      getDescription: function () {
        var description = this.props.description;
        if (
          this.props.dublinCoreMultilinguals &&
          this.props.dublinCoreMultilinguals.length > 0
        ) {
          var UILangs = this.UILangs;
          var descriptionInUiLanguage = this.props.dublinCoreMultilinguals
            .filter(function (v) {
              return v.tag === "description";
            })
            .filter(function (v) {
              return v.lang === UILangs[1];
            });
          if (descriptionInUiLanguage.length > 0) {
            description = descriptionInUiLanguage[0].value;
          }
        }
        return description;
      },
      get: function () {
        return this.props;
      }
    };

    return Thesaurus;
  });

  module.provider("gnThesaurusService", function () {
    this.$get = [
      "$q",
      "$rootScope",
      "$http",
      "gnUrlUtils",
      "Keyword",
      "Thesaurus",
      "gnCurrentEdit",
      "gnLangs",
      "gnGlobalSettings",
      function (
        $q,
        $rootScope,
        $http,
        gnUrlUtils,
        Keyword,
        Thesaurus,
        gnCurrentEdit,
        gnLangs,
        gnGlobalSettings
      ) {
        var getKeywordsSearchUrl = function (
          filter,
          thesaurus,
          lang,
          max,
          typeSearch,
          outputLang
        ) {
          var parameters = {
            type: typeSearch || "CONTAINS",
            thesaurus: thesaurus,
            rows: max,
            q: filter || "",
            uri: "*" + filter + "*" || "",
            lang: lang || "eng"
          };
          if (outputLang) {
            parameters["pLang"] = outputLang;
          }
          if (lang !== "eng") {
            // Fallback in english if thesaurus has no translation in current record language
            parameters["pLang"] = ["eng", lang];
          }

          return gnUrlUtils.append(
            "../api/registries/vocabularies/search",
            gnUrlUtils.toKeyValue(parameters)
          );
        };

        var getUILangs = function () {
          var currentUILang_3char = gnLangs.detectLang(
            gnGlobalSettings.gnCfg.langDetector,
            gnGlobalSettings
          );
          var result = [currentUILang_3char];
          if (angular.isUndefined(gnCurrentEdit.allLanguages)) {
            return result;
          }

          var currentUILang2_3char =
            gnCurrentEdit.allLanguages.iso2code[currentUILang_3char];
          if (currentUILang2_3char) {
            currentUILang2_3char = currentUILang2_3char.replace("#", "");
            if (!_.includes(result, currentUILang2_3char))
              result.push(currentUILang2_3char);
          }

          if (gnLangs.langs[currentUILang_3char]) {
            var v = gnLangs.langs[currentUILang_3char];
            if (!_.includes(result, v)) result.push(v);
          }
          if (currentUILang2_3char && gnLangs.langs[currentUILang2_3char]) {
            var v = gnLangs.langs[currentUILang2_3char];
            if (!_.includes(result, v)) result.push(v);
          }
          return result;
        };

        var parseKeywordsResponse = function (data, dataToExclude, orderById) {
          var listOfKeywords = [];

          var uiLangs = getUILangs();
          angular.forEach(data, function (k) {
            if (k.value) {
              listOfKeywords.push(new Keyword(k, uiLangs));
            }
          });

          listOfKeywords.sort(function(k1,k2) {
            var s1;
            var s2;
            if (orderById == "true") {
              s1 = k1.props.uri;
              s2 = k2.props.uri;
            } else {
              s1 = k1.label.toLowerCase();
              s2 = k2.label.toLowerCase();
            }
            if (s1 < s2) {
              return -1;
            }
            if (s1 > s2) {
              return 1;
            }
            return 0;
          });

          if (dataToExclude && dataToExclude.length > 0) {
            // Remove from search already selected keywords
            listOfKeywords = $.grep(listOfKeywords, function (n) {
              var isSelected = false;
              isSelected =
                $.grep(dataToExclude, function (s) {
                  return s.getLabel() === n.getLabel();
                }).length !== 0;
              return !isSelected;
            });
          }
          return listOfKeywords;
        };

        // Get Top Concept from thesaurus
        var getTopConcept = function (thesaurus) {
          var defer = $q.defer();
          var url = gnUrlUtils.append(
            "thesaurus.topconcept?_content_type=json",
            gnUrlUtils.toKeyValue({
              thesaurus: thesaurus
            })
          );
          $http.get(url, { cache: true }).then(
            function (response) {
              var data = response.data;

              if (data != null && data.narrower) {
                defer.resolve(data);
              } else {
                // not a top concept
                defer.reject();
              }
            },
            function (response) {
              //                TODO handle error
              defer.reject();
            }
          );
          return defer.promise;
        };

        // Drive JSKOS API with these functions
        function getConcept(thesaurus, keywordUris) {
          var defer = $q.defer();
          var url = gnUrlUtils.append(
            "thesaurus.concept?_content_type=json",
            gnUrlUtils.toKeyValue({
              thesaurus: thesaurus,
              id: keywordUris instanceof Array ? keywordUris.join(",") : keywordUris || ""
            })
          );
          $http.get(url, { cache: true }).then(
            function (response) {
              defer.resolve(response.data);
            },
            function (response) {
              //                TODO handle error
              //                defer.reject(error);
            }
          );
          return defer.promise;
        }

        // expected to promise one JSKOS concept
        // [concept](http://gbv.github.io/jskos/jskos.html#concepts)
        var lookupURI = function (thesaurus, keywordUri) {
          var deferred = $q.defer();
          // first get concept, then get links to other concepts
          getConcept(thesaurus, keywordUri).then(function (c) {
            deferred.resolve(c);
          });
          return deferred.promise;
        };

        return {
          /**
           * Number of keywords returned by search (autocompletion
           * or selection, ...)
           */
          DEFAULT_NUMBER_OF_RESULTS: 200,
          /**
           * Number of keywords to display in autocompletion list
           */
          DEFAULT_NUMBER_OF_SUGGESTIONS: 30,
          getKeywordAutocompleter: function (config) {
            var keywordsAutocompleter = new Bloodhound({
              datumTokenizer: Bloodhound.tokenizers.obj.whitespace("value"),
              queryTokenizer: Bloodhound.tokenizers.whitespace,
              limit: config.max || this.DEFAULT_NUMBER_OF_RESULTS,
              remote: {
                wildcard: "QUERY",
                url: this.getKeywordsSearchUrl(
                  "QUERY",
                  config.thesaurusKey || "",
                  config.lang,
                  config.max || this.DEFAULT_NUMBER_OF_RESULTS,
                  undefined,
                  config.outputLang
                ),
                filter: function (data) {
                  return parseKeywordsResponse(data, config.dataToExclude, config.orderById);
                }
              }
            });

            keywordsAutocompleter.initialize();
            return keywordsAutocompleter;
          },
          /**
           * Request the XML for the thesaurus and its keywords
           * in a specific format (based on the transformation).
           *
           * eg. to-iso19139-keyword for default form.
           */
          getXML: function (
            thesaurus,
            keywordUris,
            transformation,
            lang,
            textgroupOnly,
            langConversion,
            wrapper
          ) {
            // http://localhost:8080/geonetwork/srv/eng/
            // xml.keyword.get?thesaurus=external.place.regions&id=&
            // multiple=false&transformation=to-iso19139-keyword&
            var defer = $q.defer();
            var params = {
              thesaurus: thesaurus,
              id:
                keywordUris instanceof Array ? keywordUris.join(",") : keywordUris || "",
              transformation: transformation || "to-iso19139-keyword"
            };
            if (langConversion) {
              params.langMap = JSON.stringify(langConversion);
            }
            if (lang) {
              params.lang = lang;
            }
            if (wrapper) {
              params.wrapper = wrapper;
            }
            if (textgroupOnly) {
              params.textgroupOnly = textgroupOnly;
            }
            var url = gnUrlUtils.append(
              "../api/registries/vocabularies/keyword",
              gnUrlUtils.toKeyValue(params)
            );
            $http
              .get(url, {
                cache: true,
                headers: {
                  Accept: "application/xml"
                }
              })
              .then(
                function (response) {
                  // TODO: could be a global constant ?
                  var xmlDeclaration = '<?xml version="1.0" encoding="UTF-8"?>';
                  defer.resolve(response.data.replace(xmlDeclaration, ""));
                },
                function (response) {
                  //                TODO handle error
                  //                defer.reject(error);
                }
              );
            return defer.promise;
          },
          /**
           * Get thesaurus list.
           */
          getAll: function (schema) {
            var defer = $q.defer();
            $http
              .get(
                "thesaurus?_content_type=json&" +
                  "element=gmd:descriptiveKeywords&schema=" +
                  (schema || "iso19139"),
                { cache: true }
              )
              .then(
                function (response) {
                  var listOfThesaurus = [];
                  //converted and non-converted value
                  // i.e. fra and fre
                  var uiLangs = getUILangs();
                  angular.forEach(response.data[0], function (k) {
                    listOfThesaurus.push(new Thesaurus(k, uiLangs));
                  });
                  defer.resolve(listOfThesaurus);
                },
                function (response) {
                  //                TODO handle error
                  //                defer.reject(error);
                }
              );
            return defer.promise;
          },
          getKeywordsSearchUrl: getKeywordsSearchUrl,
          /**
           * Convert JSON response to array of Keyword object.
           * Filter element if dataToExclude parameter defined.
           */
          parseKeywordsResponse: parseKeywordsResponse,
          getKeywords: function (filter, thesaurus, lang, max, typeSearch) {
            var defer = $q.defer();
            var allLangs = _.map(
              Object.keys(gnCurrentEdit.allLanguages.code2iso),
              function (k) {
                return k.replace("#", "");
              }
            ).join(",");
            var url = getKeywordsSearchUrl(
              filter,
              thesaurus,
              lang,
              max,
              typeSearch,
              allLangs
            );
            $http.get(url, { cache: true }).then(
              function (response) {
                defer.resolve(parseKeywordsResponse(response.data));
              },
              function (response) {
                //                TODO handle error
                //                defer.reject(error);
              }
            );
            return defer.promise;
          },

          // ConceptScheme API for JSKOS
          lookupURI: lookupURI,
          lookupNotation: null,
          lookupLabel: null,
          getTopConcept: getTopConcept,
          suggest: null
        };
      }
    ];
  });
})();
