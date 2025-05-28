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
  goog.provide("gn_wfsfilter_service");

  var module = angular.module("gn_wfsfilter_service", []);

  module.service("wfsFilterService", [
    "gnIndexRequestManager",
    "gnHttp",
    "gnUrlUtils",
    "gnGlobalSettings",
    "$http",
    "$q",
    "$translate",
    "gnWfsFilterConfig",
    function (
      gnIndexRequestManager,
      gnHttp,
      gnUrlUtils,
      gnGlobalSettings,
      $http,
      $q,
      $translate,
      gnWfsFilterConfig
    ) {
      var indexProxyUrl = gnHttp.getService("featureindexproxy");

      var indexObject = gnIndexRequestManager.register("WfsFilter", "facets");

      var buildIndexUrl = function (params) {
        return gnUrlUtils.append(indexProxyUrl + "/query", gnUrlUtils.toKeyValue(params));
      };

      // transform date from dd-MM-YYYY to ISO (YYYY-MM-dd)
      function transformDate(d) {
        return d.substr(6) + "-" + d.substr(3, 2) + "-" + d.substr(0, 2);
      }

      this.registerEsObject = function (url, ftName) {
        return gnIndexRequestManager.register("WfsFilter", url + "#" + ftName);
      };
      this.getEsObject = function (url, ftName) {
        return gnIndexRequestManager.get("WfsFilter", url + "#" + ftName);
      };

      /**
       * Retrieve the index field object from the array given from feature type
       * info. The object contains the feature type attribute name, the index
       * indexed name, and its label from applicationProfile.
       * You can retrieve this object with the ftName or the docName.
       *
       * @param {string} name
       * @param {object} idxFields
       * @return {*}
       */
      var getIdxNameObj = function (name, idxFields) {
        for (var i = 0; i < idxFields.length; i++) {
          if (idxFields[i].label == name || idxFields[i].idxName == name) {
            return idxFields[i];
          }
        }
      };

      /**
       * Create an array of SLD filters for the facet rule. Those filters will
       * be gathered to create the full SLD filter config to send to the
       * generateSLD service.
       *
       * @param {string} name of the facet field
       * @param {*} value of the active filter
       * @param {string} type of the facet field (range, field etc..)
       * @param {Object} fieldInfo field info taken from the application profile
       * @param {string} tokenSeparator separator for tokenized fields; if defined, the field
       * is considered tokenized & the output filter will be `like '*value*'` instead of `= 'value'`
       * @return {Array} an array containing the filters
       */
      var buildSldFilter = function (name, value, type, fieldInfo, tokenSeparator) {
        var filterFields = [];

        // Transforms date format: dd-MM-YYYY > YYYY-MM-dd (ISO)
        function transformDate(d) {
          return d.substr(6) + "-" + d.substr(3, 2) + "-" + d.substr(0, 2);
        }

        // date range
        if (type == "rangeDate" && fieldInfo.minField && fieldInfo.maxField) {
          filterFields.push(
            {
              field_name: fieldInfo.maxField,
              filter: [
                {
                  filter_type: "PropertyIsGreaterThanOrEqualTo",
                  params: [transformDate(value.from)]
                }
              ]
            },
            {
              field_name: fieldInfo.minField,
              filter: [
                {
                  filter_type: "PropertyIsLessThanOrEqualTo",
                  params: [transformDate(value.to)]
                }
              ]
            }
          );
        }

        // date
        else if (type == "date") {
          filterFields.push(
            {
              field_name: name,
              filter: [
                {
                  filter_type: "PropertyIsGreaterThanOrEqualTo",
                  params: [transformDate(value.from)]
                }
              ]
            },
            {
              field_name: name,
              filter: [
                {
                  filter_type: "PropertyIsLessThanOrEqualTo",
                  params: [transformDate(value.to)]
                }
              ]
            }
          );
        }

        // numeric range
        else if (type == "range") {
          filterFields.push(
            {
              field_name: name,
              filter: [
                {
                  filter_type: "PropertyIsGreaterThanOrEqualTo",
                  params: [value.from]
                }
              ]
            },
            {
              field_name: name,
              filter: [
                {
                  filter_type: "PropertyIsLessThanOrEqualTo",
                  params: [value.to]
                }
              ]
            }
          );
        }

        // strings
        else if (type == "terms") {
          var filters = [];

          angular.forEach(value, function (v, k) {
            if (tokenSeparator !== undefined) {
              // handle 3 cases for a tokenized field: value is first, last or between both
              filters.push(
                {
                  filter_type: "PropertyIsLike",
                  params: [k + tokenSeparator + "*"]
                },
                {
                  filter_type: "PropertyIsLike",
                  params: ["*" + tokenSeparator + k]
                },
                {
                  filter_type: "PropertyIsLike",
                  params: ["*" + tokenSeparator + k + tokenSeparator + "*"]
                },
                {
                  // PropertyIsEqualTo ne fonctionne pas sur les CLOB, remplace par un PropertyIsLike
                  filter_type: "PropertyIsLike",
                  params: [k]
                }
              );
            } else {
              filters.push({
                filter_type: "PropertyIsEqualTo",
                params: [k]
              });
            }
          });

          filterFields.push({
            field_name: name,
            filter: filters
          });
        }

        // histogram
        else if (type == "histogram") {
          var filter = [];

          angular.forEach(value, function (v, k) {
            if (k.substring(0, 3) === ">= ") {
              var greaterThan = k.substring(3);
              filter.push({
                filter_type: "PropertyIsGreaterThanOrEqualTo",
                params: [greaterThan]
              });
            } else if (k.substring(0, 2) === "< ") {
              var lowerThan = k.substring(2);
              filter.push({
                filter_type: "PropertyIsLessThan",
                params: [lowerThan]
              });
            } else {
              var parts = k.split(" - ");
              filter.push(
                {
                  filter_type: "PropertyIsBetweenExclusive",
                  params: parts
                },
                {
                  filter_type: "PropertyIsEqualTo",
                  params: [parts[0]]
                }
              );
            }
          });

          filterFields.push({
            field_name: name,
            filter: filter
          });
        }

        return filterFields;
      };

      /**
       * Create the generateSLD service config from the facet ui state.
       * @param {object} facetState represents the choices from the facet ui
       * @param {object} appProfile optional, application profile holding field
       * data
       * @return {object} the sld config object
       */
      this.createSLDConfig = function (facetState, appProfile) {
        var sldConfig = {
          filters: []
        };

        angular.forEach(facetState, function (attrValue, attrName) {
          // fetch field info from attr name (expects 'ft_xxx_yy_zz')
          var fieldInfo = attrName.match(/ft_(.*?)_([a-z]+)(?:_(tree))?$/);
          var fieldName = fieldInfo ? fieldInfo[1] : attrName;
          var type = attrValue.type || "terms";
          var appProfileField =
            appProfile &&
            appProfile.fields &&
            appProfile.fields.filter(function (field) {
              return field.name === fieldName;
            })[0];
          var tokenSeparator =
            (appProfile &&
              appProfile.tokenizedFields &&
              appProfile.tokenizedFields[fieldName]) ||
            "";

          var values =
            attrValue.values && Object.keys(attrValue.values).length
              ? attrValue.values
              : attrValue.value;

          Array.prototype.push.apply(
            sldConfig.filters,
            buildSldFilter(fieldName, values, type, appProfileField, tokenSeparator)
          );
        });

        return sldConfig;
      };

      /**
       * Get the applicationProfile content from the metadata of the given
       * online resource.
       *
       * @param {string} uuid of the metadata
       * @param {string} ftName featuretype name
       * @param {string} wfsUrl url of the wfs service
       */
      this.getApplicationProfile = function (md, uuid, ftName, wfsUrl, protocol) {
        // Metadata is set (it will not on map reload)
        // and has a linksTree field
        if (md && md.linksTree) {
          var deferred = $q.defer();
          var applicationProfile = md.linksTree
            .map(function (d) {
              return d.filter(function (e) {
                return e.protocol === "OGC:WFS";
              });
            })
            .filter(function (f) {
              return f[0] ? f[0].name : undefined;
            })
            .find(function (s) {
              return s[0].name === ftName;
            })[0].applicationProfile;

          try {
            JSON.parse(applicationProfile);
            deferred.resolve({
              0: applicationProfile
            });
          } catch (e) {
            deferred.resolve({
              0: "{}"
            }); // no ApplicationProfile for current md
          }
          return deferred.promise;
        } else {
          return $http.post(
            "../api/records/" + encodeURIComponent(uuid) + "/query/wfs-indexing-config",
            {
              url: wfsUrl,
              name: ftName,
              protocol: protocol
            }
          );
        }
      };

      /**
       * Merge the fields with definition from application Profile.
       * Fields could be replaced or just updated, regarding to
       * `extendOnly` property.
       *
       * @param {Array} fields index fields definition
       * @param {Object} appProfile Config object.
       */
      this.indexMergeApplicationProfile = function (fields, appProfile) {
        var toRemoveIdx = [],
          mergedF = [];
        var newFields = appProfile.fields;
        var tokenizedFields = appProfile.tokenizedFields || [];

        var getNewFieldIdx = function (field) {
          for (var i = 0; i < newFields.length; i++) {
            if (field.name == newFields[i].name) {
              return i;
            }
          }
          return -1;
        };

        // Merge field objects and detect if we need to remove some
        fields.forEach(function (field, idx) {
          var keep;

          var newFieldIdx = getNewFieldIdx(field);
          if (newFieldIdx >= 0) {
            var newField = newFields[newFieldIdx];
            keep = true;
            mergedF.push(field.label);
            if (newField.label) {
              field.label = newField.label[gnGlobalSettings.lang];
            }
            field.definition = newField.definition;
            field.aggs = newField.aggs;
            field.display = newField.display;
            // add a flag for tokenized fields
            field.isTokenized = tokenizedFields[field.name] != null;
            field.tokenSeparator = tokenizedFields[field.name];
            field.suffix = newField.suffix;
            field.hidden = newField.hidden;
          }
          if (!keep) {
            toRemoveIdx.unshift(idx);
          }
        });

        var allFields = angular.copy(fields);

        if (!appProfile.extendOnly) {
          toRemoveIdx.forEach(function (i) {
            fields.splice(i, 1);
          });
        }

        // Add appProfile extra fields
        newFields.forEach(function (f) {
          if (mergedF.indexOf(f.name) < 0) {
            f.label = f.label[gnGlobalSettings.lang] || f.name;
            fields.push(f);
          }
        });

        if (!appProfile.extendOnly) {
          fields.sort(function (a, b) {
            return getNewFieldIdx(a) - getNewFieldIdx(b);
          });
        }

        return allFields;
      };

      /**
       * Call generateSLD service to create the SLD and get an url to reach it.
       *
       * @param {boolean} isSld Request a SLD URL or a OGC FILTER
       * @param {Object} rulesObj structure of the SLD rules to apply
       * @param {string} wmsUrl url of the WMS service
       * @param {string} featureTypeName of the featuretype
       * @return {HttpPromise} promise
       */
      this.getFilter = function (rulesObj, wmsUrl, featureTypeName) {
        var isSld = gnWfsFilterConfig.filterStrategy.indexOf("SLD") === 0,
          params = {
            filters: JSON.stringify(rulesObj),
            url: wmsUrl,
            layers: featureTypeName
          };

        return $http({
          method: "POST",
          url: "../api/tools/ogc/" + (isSld ? "sld" : "filter"),
          data: $.param(params),
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        }).then(
          function (response) {
            if (isSld) {
              var url = response.data;
              return this.pollSldUrl(url);
            } else {
              var defer = $q.defer();
              defer.resolve(
                response.data.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
              );
              return defer.promise;
            }
          }.bind(this)
        );
      };

      /**
       * Apply FILTER or SLD parameter to a layer depending on configuration.
       *
       * @param {boolean} layer Map layer
       * @param {Object} filterOrSldUrl OGC filter or SLD url or SLD body retrieved from getFilter.
       */
      this.applyFilter = function (layer, filterOrSldUrl) {
        var isSld = gnWfsFilterConfig.filterStrategy.indexOf("SLD") === 0;
        if (isSld) {
          var useSldBody = gnWfsFilterConfig.filterStrategy === "SLD_BODY";
          if (useSldBody) {
            $http.get(filterOrSldUrl).then(function (response) {
              layer.getSource().updateParams({
                SLD_BODY: response.data
              });
            });
          } else {
            layer.getSource().updateParams({
              SLD: filterOrSldUrl
            });
          }
        } else {
          var layers = layer.getSource().getParams().LAYERS,
            isGroupOfNLayers = layers.split(",").length;

          function buildFilterForEachLayers(nbOfLayers, filter) {
            if (filter == null) {
              return filter;
            }
            var listOfFilters = "";
            for (var i = 0; i < nbOfLayers; i++) {
              listOfFilters += "(" + filter + ")";
            }
            return listOfFilters;
          }
          layer.getSource().updateParams({
            FILTER: isGroupOfNLayers
              ? buildFilterForEachLayers(isGroupOfNLayers, filterOrSldUrl)
              : filterOrSldUrl
          });
        }
      };

      this.pollSldUrl = function (url) {
        var defer = $q.defer();
        var pollingTimeout = 100;
        var pollingAttempts = 0;
        var pollingMaxAttemps = 25;

        var poller = function () {
          pollingAttempts++;
          $http({
            method: "GET",
            url: url
          }).then(
            function () {
              defer.resolve(url);
            },
            function (error) {
              if (pollingAttempts < pollingMaxAttemps) {
                $timeout(poller, pollingTimeout);
              } else {
                defer.reject(error);
              }
            }
          );
        };
        poller();
        return defer.promise;
      };

      this.pollSldUrl = function (url) {
        var defer = $q.defer();
        var pollingTimeout = 100;
        var pollingAttempts = 0;
        var pollingMaxAttempts = 25;

        var poller = function () {
          pollingAttempts++;
          $http({
            method: "GET",
            url: url
          }).then(
            function () {
              defer.resolve(url);
            },
            function (error) {
              if (pollingAttempts < pollingMaxAttempts) {
                $timeout(poller, pollingTimeout);
              } else {
                defer.reject(error);
              }
            }
          );
        };
        poller();
        return defer.promise;
      };

      /**
       * Run the indexation of the feature
       *
       * @param {string} wfs service url
       * @param {string} featuretype name
       * @return {httpPromise} when indexing is done
       */
      this.indexWFSFeatures = function (
        url,
        type,
        idxConfig,
        treeFields,
        uuid,
        version,
        strategy
      ) {
        return $http
          .put("../api/workers/data/wfs/actions/start", {
            url: url,
            strategy: strategy, // means that the targetNs of GFI is used to define strategy.
            typeName: type,
            version: version || "1.1.0",
            tokenizedFields: idxConfig,
            treeFields: treeFields,
            metadataUuid: uuid
          })
          .then(
            function (data) {},
            function (response) {}
          );
      };

      // formats current filter state as a CQL request
      // if useActualParamName is true, param names will be stripped of their
      // prefixes & suffixes (used only for the index)
      this.toCQL = function (esObj, useActualParamName) {
        var state = esObj.getState();
        var where;

        if (!state) {
          console.warn("WFS filter state could not be fetched");
          return "";
        }

        where = [];
        angular.forEach(state.qParams, function (fObj, fName) {
          var config = esObj.getIdxNameObj_(fName);
          var clause = [];
          var values = fObj.values;
          var paramName = fName;

          if (useActualParamName) {
            var fieldInfo = paramName.match(/ft_(.*?)_([a-z]+)(?:_(tree))?$/);
            paramName = fieldInfo ? fieldInfo[1] : paramName;
          }

          if (config.isDateTime) {
            if (values.from && values.to) {
              where = where.concat([
                "(" + config.maxField + " >= '" + transformDate(values.from) + "')",
                "(" + config.minField + " <= '" + transformDate(values.to) + "')"
              ]);
            }
            return;
          }

          angular.forEach(values, function (v, k) {
            if (config.isRange) {
              if (k.substring(0, 3) === ">= ") {
                var greaterThan = k.substring(3);
                clause.push("(" + paramName + " >= " + greaterThan + ")");
              } else if (k.substring(0, 2) === "< ") {
                var lowerThan = k.substring(2);
                clause.push("(" + paramName + " < " + lowerThan + ")");
              } else {
                var parts = k.split(" - ");
                clause.push(
                  "(" +
                    paramName +
                    " >= " +
                    parts[0] +
                    " AND " +
                    paramName +
                    " < " +
                    parts[1] +
                    ")"
                );
              }
              return;
            }

            var escaped = k.replace(/'/g, "\\'");
            if (config.isTokenized) {
              var sep = config.tokenSeparator;
              clause.push(
                "(" + paramName + " LIKE '" + escaped + "')",
                "(" + paramName + " LIKE '%" + sep + escaped + sep + "%')",
                "(" + paramName + " LIKE '%" + sep + escaped + "')",
                "(" + paramName + " LIKE '" + escaped + sep + "%')"
              );
            } else {
              clause.push("(" + paramName + " = '" + escaped + "')");
            }
          });
          if (clause.length == 0) return;
          where.push("(" + clause.join(" OR ") + ")");
        });
        return where.join(" AND ");
      };

      /**
       * takes an Elasticsearch request object as input and ouputs a simplified
       * object with properties describing names and values of the defined
       * filters.
       * Values are always an array holding the different values if any.
       * note: the bbox filter is set in the 'geometry' key
       *
       * @param {Object} elasticSearchObject
       */
      this.toObjectProperties = function (esObject) {
        var state = esObject.getState();
        var result = {};

        if (state) {
          // add query params (qParams)
          angular.forEach(state.qParams, function (fObj, fName) {
            var config = esObject.getIdxNameObj_(fName);

            // init array
            result[fName] = [];

            // special case: date time, use 'from' and 'to' properties
            if (config.isDateTime) {
              if (fObj.values.from) {
                result[fName].push(transformDate(fObj.values.from));
              }
              if (fObj.values.to) {
                result[fName].push(transformDate(fObj.values.to));
              }
              return;
            }

            // adding all values to the array
            angular.forEach(fObj.values, function (value, key) {
              result[fName].push(key);
            });
          });

          // add geometry (array with only one value)
          if (state.geometry) {
            result.geometry = [
              state.geometry[0][0] +
                "," +
                state.geometry[1][1] +
                "," +
                state.geometry[1][0] +
                "," +
                state.geometry[0][1]
            ];
          }
        }

        return result;
      };

      /**
       * takes an Elasticsearch request object as input and ouputs an object
       * with human readable properties.
       * note: full text search & bbox extent are ignored
       *
       * @param {Object} elasticSearchObject
       */
      this.toReadableObject = function (esObject) {
        var state = esObject.getState();
        var result = {};

        if (state) {
          // add query params (qParams)
          angular.forEach(state.qParams, function (fObj, fName) {
            var config = esObject.getIdxNameObj_(fName);
            var paramName = config.label || config.name;

            // special case: date time, use 'from' property
            if (config.isDateTime) {
              if (fObj.values.from != "" && fObj.values.to) {
                result[fName] = {
                  name: paramName,
                  value: fObj.values.from + ", " + fObj.values.to
                };
              }
              return;
            }

            // values separated by comma
            result[fName] = {
              name: paramName,
              value: ""
            };
            angular.forEach(fObj.values, function (value, key) {
              result[fName].value += ", " + key;
            });
            result[fName].value = result[fName].value.substring(2);
          });
        }

        return result;
      };
    }
  ]);
})();
