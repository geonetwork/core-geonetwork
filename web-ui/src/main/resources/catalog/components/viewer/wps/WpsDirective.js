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
  goog.provide("gn_wps_directive");

  goog.require("gn_wfsfilter_service");

  var module = angular.module("gn_wps_directive", []);

  function isFieldNotEmpty(value) {
    return value !== undefined && value !== "";
  }

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWpsProcessForm
   * @restrict AE
   *
   * @description
   * The `gnWpsProcessForm` build up a HTML form from the describe process
   * response object (after call the describe process request).
   * User inputs will be saved on the wpsLink object as 'inputs' with the
   * following structure:
   *  [{
   *    name: 'input_name',
   *    value: 'value entered by the user'
   *  },
   *  ...]
   * Existing inputs will be displayed in the form.
   * The directive keeps a cache of process descriptions & inputs.
   *
   * @directiveInfo {Object} map
   * @directiveInfo {Object} wpsLink this object holds information on the WPS
   *  service to use; expected keys are:
   *  * `name`: required, process identifier
   *  * `url`: required, process URL
   *  * `labels`: optional, object holding a string for different languages (use
   *     ISO3 language codes as returned by `$tanslate.use()`, see:
   *     https://angular-translate.github.io/docs/#/api/pascalprecht.translate.$translate)
   *  * `label`: optional, string
   *  * `applicationProfile`: optional, refer to the documentation on how to
   *    set up an application profile object for a WPS service
   * @directiveInfo {Object} wfsLink the WFS link object
   *  will be used to overload inputs based on active WFS feature filters
   * @directiveInfo {function} describedCallback will be called with no arg when
   *  the describeProcess request has been done & processed; this means the form
   *  is ready to use
   * @directiveInfo {boolean} hideExecuteButton if true, the 'execute' button
   *  will be hidden
   * @directiveInfo {boolean} hideTitle if true, the form title will be hidden
   * @directiveInfo {boolean} cancelPrevious if true, concurrent requests on WPS
   *  endpoints will be allowed; this permits having several forms at the same
   *  time in the UI
   */
  module.directive("gnWpsProcessForm", [
    "gnWpsService",
    "gnUrlUtils",
    "$timeout",
    "wfsFilterService",
    "$window",
    "gnGeometryService",
    "gnProfileService",
    function (
      gnWpsService,
      gnUrlUtils,
      $timeout,
      wfsFilterService,
      $window,
      gnGeometryService,
      gnProfileService
    ) {
      return {
        restrict: "AE",
        scope: {
          map: "=",
          wpsLink: "=",
          wfsLink: "=",
          describedCallback: "&"
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/viewer/wps/partials/processform.html"
          );
        },
        link: function (scope, element, attrs) {
          scope.describeState = "standby";
          scope.executeState = "";

          scope.selectedOutput = {
            identifier: "",
            mimeType: ""
          };

          scope.hideExecuteButton = !!scope.$eval(attrs.hideExecuteButton);
          scope.hideTitle = !!scope.$eval(attrs.hideTitle);
          scope.cancelPrevious =
            attrs.cancelPrevious !== undefined
              ? !!scope.$eval(attrs.cancelPrevious)
              : true;
          scope.activeGeometryTool = { current: undefined };

          scope.desactivateGeometryTool = function () {
            scope.activeGeometryTool = { current: undefined };
          };

          // this will hold pre-loaded process descriptions
          // keys are: '<processId>@<uri>'
          scope.loadedDescriptions = {};

          // this will hold the 'application profile' of the current WPS service
          scope.applicationProfile = null;

          // maximum number of processes id saved in local storage
          var maxHistoryCount = attrs["maxHistory"] || 6;

          var source = new ol.source.Vector();
          var wpsOutputLayer = new ol.layer.Vector({
            source: source
          });
          scope.map.addLayer(wpsOutputLayer);

          // query a process description when a new wps link is given
          // note: a deep equality is required, since what we are actually
          // comparing are process id and url (and not object ref)
          scope.$watch(
            function () {
              var wpsLink = scope.wpsLink || {};
              return {
                processId: attrs["processId"] || wpsLink.name,
                uri: attrs["uri"] || wpsLink.url
              };
            },
            function (newLink, oldLink) {
              // the WPS link is incomplete: leave & clear form
              if (!newLink.uri || !newLink.processId) {
                scope.processDescription = null;
                return;
              }

              if (scope.wpsLink.layer) {
                scope.wpsLink.layer.set("wpsfilter-el", element);
              }

              // prepare inputs & output object (use existing one if available)
              scope.wpsLink.inputs = scope.wpsLink.inputs || [];
              scope.wpsLink.output = scope.wpsLink.output || {};

              // inputs overriden by wfs filters are saved here
              scope.inputWfsOverride = {};

              scope.describeState = "sent";

              // reset executeState upon new selection
              scope.executeState = "";

              // parse application profile as JSON (if not already an object)
              // application profile holds 2 arrays: inputs and outputs
              scope.applicationProfile = scope.wpsLink.applicationProfile || null;
              if (
                scope.applicationProfile &&
                typeof scope.applicationProfile === "string"
              ) {
                try {
                  scope.applicationProfile = JSON.parse(scope.applicationProfile);
                } catch (e) {
                  console.warn(
                    "Error while loading application profile.",
                    scope.applicationProfile
                  );
                }
              }

              scope.isDateTime = function (date) {
                if (date.hasOwnProperty("metadata")) {
                  return date.metadata[0].href === "datetime";
                }
                return false;
              };

              scope.isBoolean = function (input) {
                if (input.hasOwnProperty("metadata")) {
                  return input.metadata[0].href === "boolean";
                }
                return false;
              };

              scope.checkOutput = function (outputs) {
                return outputs.filter(function (o) {
                  return o.reference.mimeType !== "application/x-ogc-wms";
                });
              };

              scope.getDateBounds = function (input, isMin) {
                if (!input) {
                  return;
                } else if (isMin) {
                  return input.literalData.allowedValues.valueOrRange[0].minimumValue
                    .value;
                }
                return input.literalData.allowedValues.valueOrRange[0].maximumValue.value;
              };

              // get values from wfs filters
              var wfsFilterValues = null;
              if (scope.wfsLink) {
                // this is the object holding current filter values
                var esObject = wfsFilterService.getEsObject(
                  scope.wfsLink.url,
                  scope.wfsLink.name
                );

                // use filter values in Elasticsearch object state
                // to overload input
                if (esObject) {
                  // this will hold wfs filter values
                  var currentFilters = wfsFilterService.toObjectProperties(esObject);
                  wfsFilterValues = {};

                  // remove prefix & suffix on filter keys
                  Object.keys(currentFilters).forEach(function (key) {
                    var cleanKey = key.replace(/^ft_|_s$|_dt$/g, "");
                    wfsFilterValues[cleanKey] = currentFilters[key];
                  });
                }
              }

              // query a description and build up the form
              gnWpsService
                .describeProcess(newLink.uri, newLink.processId, {
                  cancelPrevious: scope.cancelPrevious
                })
                .then(
                  function (response) {
                    scope.describeState = "succeeded";
                    scope.describeResponse = response;
                    scope.activeGeometryTool = { current: undefined };

                    if (response.processDescription) {
                      // Bind input directly in link object
                      scope.processDescription = response.processDescription[0];

                      // by default, do not use profile graph output
                      scope.outputAsGraph = false;

                      // loop on process expected inputs to prepare the form
                      angular.forEach(
                        scope.processDescription.dataInputs.input,
                        function (input) {
                          var inputName = input.identifier.value;
                          var value;
                          var defaultValue;
                          var wfsFilterValue;

                          // look for input info in app profile
                          if (
                            scope.applicationProfile &&
                            scope.applicationProfile.inputs
                          ) {
                            scope.applicationProfile.inputs.forEach(function (input) {
                              if (input.identifier == inputName) {
                                defaultValue = input.defaultValue;

                                // check if there is a wfs filter active
                                // & apply value
                                var wfsFilter = input.linkedWfsFilter || "";

                                // handle the case where the link points to "from"
                                // or "to" dates of a filter
                                var valueIndex = -1;
                                if (wfsFilter.substr(-5) === ".from") {
                                  wfsFilter = wfsFilter.substr(0, wfsFilter.length - 5);
                                  valueIndex = 0;
                                } else if (wfsFilter.substr(-3) === ".to") {
                                  wfsFilter = wfsFilter.substr(0, wfsFilter.length - 3);
                                  valueIndex = 1;
                                }

                                if (
                                  wfsFilter &&
                                  wfsFilterValues &&
                                  isFieldNotEmpty(wfsFilterValues[wfsFilter])
                                ) {
                                  // take value at specific index, or all values
                                  if (valueIndex >= 0) {
                                    wfsFilterValue = [
                                      wfsFilterValues[wfsFilter][valueIndex]
                                    ];
                                  } else {
                                    wfsFilterValue = wfsFilterValues[wfsFilter];
                                  }
                                }
                              }
                            });
                          }

                          // display field as overriden
                          scope.inputWfsOverride[inputName] =
                            isFieldNotEmpty(wfsFilterValue) && wfsFilterValue.length > 0;

                          // literal data (basic form input)
                          if (input.literalData !== undefined) {
                            // Default value (if not already there)
                            if (
                              input.literalData.defaultValue !== undefined &&
                              defaultValue === undefined
                            ) {
                              defaultValue = input.literalData.defaultValue;

                              // convert value if necessary
                              if (
                                input.literalData.dataType &&
                                input.literalData.dataType.value == "float"
                              ) {
                                defaultValue = parseFloat(defaultValue);
                              }
                            }
                          }

                          // bouding box data
                          if (input.boundingBoxData != undefined) {
                            // format default value for the bbox directive
                            if (defaultValue) {
                              defaultValue = defaultValue
                                .split(",")
                                .slice(0, 4)
                                .join(",");
                            }
                          }

                          // complex data: draw a feature on map
                          if (input.complexData != undefined) {
                            // this will be a {ol.Feature} object once drawn
                            input.feature = null;

                            // output format
                            var preferedOutputFormat = "wkt";
                            input.outputFormat = null;
                            for (
                              var i = 0;
                              i < input.complexData.supported.format.length;
                              i++
                            ) {
                              var f = input.complexData.supported.format[i],
                                found = gnGeometryService.getFormatFromMimeType(
                                  f.mimeType
                                );
                              if (found === preferedOutputFormat) {
                                input.outputFormat = found;
                                input.mimeType = f.mimeType;
                                break;
                              }
                            }
                            if (
                              input.outputFormat === null &&
                              input.complexData._default.format
                            ) {
                              input.outputFormat =
                                gnGeometryService.getFormatFromMimeType(
                                  input.complexData._default.format.mimeType
                                );
                              input.mimeType = input.complexData._default.format.mimeType;
                            }

                            // check if geom can be a multi
                            var isMulti = false;
                            if (input.metadata !== undefined) {
                              input.metadata.forEach(function (m) {
                                if (m.title.contains("allowMultipart")) {
                                  isMulti = true;
                                }
                              });
                            }

                            scope.getGeomType = function (geom) {
                              if (!geom) {
                                return;
                              }
                              var geom_type;
                              geom = geom.toLowerCase();
                              switch (geom) {
                                case "line":
                                  geom_type = "LineString";
                                  break;

                                case "point":
                                  geom_type = "Point";
                                  break;

                                case "polygon":
                                  geom_type = "Polygon";
                                  break;

                                // TODO: add other types?

                                default:
                                  geom_type = null;
                              }
                              if (geom_type && isMulti) {
                                geom_type = "Multi" + geom_type;
                              }
                              return geom_type;
                            };
                            // guess geometry type from schema url
                            var url = input.complexData._default.format.schema;
                            var result = /\?.*GEOMETRYNAME=([^&\b]*)/gi.exec(url);
                            var geom =
                              result && result[1] ? result[1].toLowerCase() : null;
                            input.geometryType = scope.getGeomType(geom);
                            // Deal with multi processing:geometryType
                            if (!input.geometryType) {
                              input.geometryType = scope.getGeomType(
                                input.metadata
                                  .filter(function (i) {
                                    return (
                                      i.hasOwnProperty("title") &&
                                      i.title === "processing:geometryType"
                                    );
                                  })
                                  .map(function (i) {
                                    return i.href;
                                  })[0]
                              );
                            }
                            // try in ows:Metadata if not found
                            if (
                              !input.geometryType &&
                              input.metadata &&
                              input.metadata.length
                            ) {
                              var type = input.metadata[0].href;
                              if (type === "point") {
                                input.geometryType = "Point";
                              }
                            }

                            var pointTypeIdentifier = [
                              "location",
                              "position",
                              "point",
                              "center"
                            ];
                            if (
                              input.identifier.value &&
                              pointTypeIdentifier.indexOf(
                                input.identifier.value.toLowerCase()
                              ) != -1
                            ) {
                              input.geometryType = "Point";
                            }
                          }

                          // add missing input fields (add 1 by default)
                          var minCount = Math.max(1, input.minOccurs);
                          var maxCount = input.maxOccurs || 1;
                          var inputs = scope.getInputsByName(inputName);

                          // add enough fields to hold all default values
                          if (Array.isArray(defaultValue)) {
                            minCount = Math.max(
                              minCount,
                              Math.min(maxCount, defaultValue.length)
                            );
                          }
                          var count = inputs.length;
                          while (count < minCount) {
                            count++;
                            scope.wpsLink.inputs.push({
                              name: inputName,
                              value: ""
                            });
                          }

                          // force values if a wfs filter is present
                          // note: wfs filter value is an array of values
                          if (wfsFilterValue && wfsFilterValue.length) {
                            scope.removeAllInputValuesByName(inputName);
                            wfsFilterValue
                              .filter(function (value, index) {
                                return index < maxCount;
                              })
                              .forEach(function (value) {
                                scope.wpsLink.inputs.push({
                                  name: inputName,
                                  value: value
                                });
                              });
                          }
                          // apply default values if any
                          else if (isFieldNotEmpty(defaultValue)) {
                            inputs = scope.getInputsByName(inputName);
                            var defaultValueArray = Array.isArray(defaultValue)
                              ? defaultValue
                              : [defaultValue];
                            for (var i = 0; i < inputs.length; i++) {
                              if (
                                !isFieldNotEmpty(inputs[i].value) &&
                                isFieldNotEmpty(defaultValueArray[i])
                              ) {
                                scope.setInputValueByName(
                                  inputName,
                                  i,
                                  defaultValueArray[i]
                                );
                              }
                            }
                          }
                        }
                      );

                      var defaultOutput;
                      var defaultMimeType;

                      angular.forEach(
                        scope.processDescription.processOutputs.output,
                        function (output) {
                          var outputName = output.identifier.value;

                          // output already selected yet: leave
                          if (defaultOutput) {
                            return;
                          }

                          // no output selected yet: take this one
                          defaultOutput = outputName;
                          defaultMimeType = output.complexOutput._default.format.mimeType;

                          // look for output info in app profile
                          if (
                            scope.applicationProfile &&
                            scope.applicationProfile.outputs
                          ) {
                            scope.applicationProfile.outputs.forEach(function (output) {
                              if (output.identifier == outputName) {
                                // assign mime type if available
                                defaultMimeType =
                                  output.defaultMimeType || defaultMimeType;

                                // check if we need to get into 'profile graph' mode
                                // (display graph options are defined)
                                // TODO: actually parse these options
                                if (output.displayGraphOptions) {
                                  scope.outputAsGraph = output.displayGraphOptions
                                    ? true
                                    : false;
                                }
                              }
                            });
                          }
                        }
                      );

                      // if there is a mimeType containing WMS: use it instead
                      var wmsOutput = gnWpsService.getProcessOutputWMSMimeType(
                        scope.processDescription
                      );
                      if (wmsOutput) {
                        defaultOutput = wmsOutput.outputIdentifier;
                        defaultMimeType = wmsOutput.mimeType;
                      }

                      // assign default output & mimeType
                      scope.wpsLink.output.identifier = defaultOutput;
                      scope.wpsLink.output.mimeType = defaultMimeType;

                      // use output as reference unless doing a profile graph
                      scope.wpsLink.output.asReference = scope.outputAsGraph
                        ? false
                        : true;

                      scope.wpsLink.output.loadReferenceInMap = true;

                      scope.outputsVisible = true;

                      scope.wpsLink.output.lineage = false;
                      scope.wpsLink.output.storeExecuteResponse = false;
                      scope.wpsLink.output.status = false;
                      scope.optionsVisible = true;

                      // use existing process desc if available
                      var processKey = newLink.processId + "@" + newLink.uri;
                      var existingDesc = scope.loadedDescriptions[processKey];
                      if (existingDesc) {
                        scope.processDescription = angular.extend(
                          scope.processDescription,
                          existingDesc
                        );
                      }
                      scope.loadedDescriptions[processKey] = angular.extend(
                        {},
                        scope.processDescription
                      );
                    }

                    // described callback
                    if (scope.describedCallback) {
                      scope.describedCallback();
                    }
                  },
                  function (response) {
                    scope.describeState = "failed";
                    scope.describeResponse = response;
                  }
                );
            },
            true
          );

          scope.close = function () {
            scope.wpsLink.name = "";
            scope.wpsLink.url = "";
            scope.wpsLink.applicationProfile = null;
            scope.describeState = "standby";
          };

          scope.toggleOutputs = function () {
            scope.outputsVisible = !scope.outputsVisible;
          };

          scope.toggleOptions = function () {
            scope.optionsVisible = !scope.optionsVisible;
          };

          scope.clearGeometry = function () {
            source.clear();
          };

          scope.submit = function () {
            source.clear();
            scope.validation_messages = [];
            scope.exception = undefined;

            // Check that inputs have the required values
            var invalid = false;
            angular.forEach(scope.processDescription.dataInputs.input, function (input) {
              // count the number of non empty values
              var valueCount = scope
                .getInputsByName(input.identifier.value)
                .filter(function (input) {
                  return isFieldNotEmpty(input.value);
                }).length;

              // this will be used to show errors on the form
              input.missingOccursCount = Math.max(0, input.minOccurs - valueCount);

              if (input.missingOccursCount > 0) {
                invalid = true;
              }
            });

            // there are errors with inputs: leave
            if (invalid) {
              return;
            }

            var inputs = scope.wpsLink.inputs;
            var output = scope.wpsLink.output;

            updateStatus = function (statusLocation) {
              gnWpsService.getStatus(statusLocation).then(
                function (response) {
                  processResponse(response);
                },
                function (response) {
                  scope.executeState = "failed";
                  scope.executeResponse = response;
                  scope.running = false;
                }
              );
            };

            processResponse = function (response) {
              if (response.TYPE_NAME === "OWS_1_1_0.ExceptionReport") {
                scope.executeState = "finished";
                scope.running = false;
              }
              if (response.TYPE_NAME === "WPS_1_0_0.ExecuteResponse") {
                if (response.status != undefined) {
                  if (
                    response.status.processAccepted != undefined ||
                    response.status.processPaused != undefined ||
                    response.status.processStarted != undefined
                  ) {
                    scope.executeState = "pending";
                    scope.statusPromise = $timeout(
                      function () {
                        updateStatus(response.statusLocation);
                      },
                      1000,
                      true
                    );
                  }
                  if (
                    response.status.processSucceeded != undefined ||
                    response.status.processFailed != undefined
                  ) {
                    scope.executeState = "finished";
                    scope.running = false;

                    if (response.status.processSucceeded) {
                      var wmsOutput = gnWpsService.responseHasWmsService(response);
                      if (wmsOutput !== null) {
                        gnWpsService.extractWmsLayerFromResponse(
                          response,
                          wmsOutput,
                          scope.map,
                          scope.wpsLink.layer
                        );
                      } else {
                        gnWpsService
                          .getGeometryOutput(
                            response,
                            scope.wpsLink.output.loadReferenceInMap
                          )
                          .then(function (geomOutput) {
                            if (geomOutput != null) {
                              source.addFeatures(geomOutput.data);
                              scope.map
                                .getView()
                                .fit(source.getExtent(), scope.map.getSize());
                            }
                          });
                      }
                    }
                  }
                }
              }
              scope.executeResponse = response;

              // save raw graph data on view controller & hide it in wps form
              if (scope.outputAsGraph && response.processOutputs) {
                output.asReference = false;
                try {
                  var jsonData = JSON.parse(
                    response.processOutputs.output[0].data.complexData.content
                  );

                  // TODO: use applicationProfile.displayGraphOptions here
                  gnProfileService.displayProfileGraph(jsonData.profile, {
                    valuesProperty: "values",
                    xProperty: "lon",
                    yProperty: "lat",
                    distanceProperty: "dist",
                    crs: "EPSG:4326"
                  });
                } catch (e) {
                  console.error("Error parsing WPS graph data:", response.processOutputs);
                }
                scope.executeResponse = null;
              }
            };

            var processUri = attrs["uri"] || scope.wpsLink.url;
            var processId = attrs["processId"] || scope.wpsLink.name;

            scope.running = true;
            scope.executeState = "sent";
            gnWpsService
              .execute(processUri, processId, inputs, output, scope.processDescription)
              .then(
                function (response) {
                  processResponse(response);
                },
                function (response) {
                  scope.executeState = "failed";
                  scope.executeResponse = response;
                  scope.running = false;
                }
              );

            // update local storage
            if ($window.localStorage) {
              var key = "gn-wps-processes-history";
              var processKey = processId + "@" + processUri;
              var history = JSON.parse($window.localStorage.getItem(key) || "{}");
              history.processes = history.processes || [];
              history.processes.unshift(processKey);

              // remove dupes and apply limit
              var count = 0;
              history.processes = history.processes.filter(function (
                value,
                index,
                array
              ) {
                if (array.indexOf(value) !== index || count >= maxHistoryCount) {
                  return false;
                } else {
                  count++;
                  return true;
                }
              });

              $window.localStorage.setItem(key, JSON.stringify(history));
            }
          };

          scope.cancel = function () {
            if ($timeout.cancel(scope.statusPromise)) {
              scope.statusPromise = undefined;
              scope.executeState = "cancelled";
              scope.running = false;
            }
          };

          scope.responseDocumentStatusChanged = function () {
            if (scope.wpsLink.output.status == true) {
              scope.wpsLink.output.storeExecuteResponse = true;
            }
          };

          // Guess the mimeType associated with the selected output.
          // scope.$watch('selectedOutput.identifier', function(v) {
          scope.setOutput = function (identifier, mimeType) {
            scope.wpsLink.output.identifier = identifier;
            scope.wpsLink.output.mimeType = mimeType;
          };

          // returns a valid input type (for literal data)
          scope.getInputType = function (literalDataType) {
            switch (literalDataType) {
              case "float":
                return "number";
              default:
                return "text";
            }
          };

          scope.getIsAllowedValuesRange = function (allowedValues) {
            if (!allowedValues || !allowedValues.valueOrRange) {
              return false;
            }
            return allowedValues.valueOrRange[0].TYPE_NAME === "OWS_1_1_0.RangeType";
          };
          scope.getMinValueFromAllowedValues = function (allowedValues) {
            if (scope.getIsAllowedValuesRange(allowedValues)) {
              return Math.max(
                Number.MIN_SAFE_INTEGER,
                parseFloat(allowedValues.valueOrRange[0].minimumValue.value)
              );
            }
          };
          scope.getMaxValueFromAllowedValues = function (allowedValues) {
            if (scope.getIsAllowedValuesRange(allowedValues)) {
              return Math.min(
                Number.MAX_SAFE_INTEGER,
                parseFloat(allowedValues.valueOrRange[0].maximumValue.value)
              );
            }
          };

          // get/set input values
          scope.getInputsByName = function (name) {
            return scope.wpsLink.inputs.filter(function (input) {
              return input.name == name;
            });
          };
          scope.setInputValueByName = function (name, index, value) {
            var current = 0;
            scope.wpsLink.inputs.forEach(function (input) {
              if (input.name === name) {
                if (current == index) {
                  input.value = value;
                }
                current++;
              }
            });
          };

          // add or remove an input value
          scope.addInputValueByName = function (name) {
            scope.wpsLink.inputs.push({
              name: name,
              value: undefined
            });
          };
          scope.removeInputValueByName = function (name, indexToRemove) {
            var realIndex = -1;
            scope.wpsLink.inputs.forEach(function (input, index) {
              var innerIndex = scope.getInputsByName(name).indexOf(input);
              if (innerIndex === indexToRemove) {
                realIndex = index;
              }
            });
            if (realIndex > -1) {
              scope.wpsLink.inputs.splice(realIndex, 1);
            }
          };
          scope.removeAllInputValuesByName = function (name) {
            scope.wpsLink.inputs = scope.wpsLink.inputs.filter(function (input) {
              return input.name !== name;
            });
          };

          // checks are made against the application profile
          scope._getInputInfo = function (name) {
            var appProfile = scope.applicationProfile;
            if (!appProfile || !appProfile.inputs) {
              return false;
            }
            return appProfile.inputs.filter(function (input) {
              return input.identifier === name;
            })[0];
          };
          scope.isInputHidden = function (name) {
            var input = scope._getInputInfo(name);
            return input ? !!input.hidden : false;
          };
          scope.isInputDisabled = function (name) {
            var input = scope._getInputInfo(name);
            return input ? !!input.disabled : false;
          };

          scope.getLabel = function () {
            return gnWpsService.getProcessLabel(scope.wpsLink);
          };
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWpsUrlDiscovery
   * @restrict E
   *
   * @description
   * This directive allows the user to input a URL and receive a list of WPS
   * processes based on a GetCapabilities call to that URL.
   * @directiveInfo {Object} wpsLink: selected process description (required)
   */
  module.directive("gnWpsUrlDiscovery", [
    "gnWpsService",
    "gnUrlUtils",
    function (gnWpsService, gnUrlUtils) {
      return {
        restrict: "E",
        templateUrl:
          "../../catalog/components/viewer/wps/" + "partials/urldiscovery.html",
        scope: {
          wpsLink: "="
        },
        controllerAs: "ctrl",
        controller: [
          "$scope",
          function ($scope) {
            $scope.loading = false;
            $scope.processes = [];
            $scope.error = null;
            $scope.url = "";

            this.doRequest = function () {
              // do nothing if invalid url
              if (!gnUrlUtils.isValid($scope.url)) {
                return;
              }

              $scope.loading = true;
              $scope.processes = [];
              $scope.error = null;

              gnWpsService
                .getCapabilities($scope.url, {
                  cancelPrevious: true
                })
                .then(
                  function (data) {
                    $scope.loading = false;

                    if (!data) {
                      $scope.error = "Service not found";
                      return;
                    }
                    $scope.processes = data.processOfferings.process;
                  },
                  function (error) {
                    $scope.loading = false;
                    $scope.processes = [];
                    $scope.error = error.status + " " + error.statusText;
                  }
                );
            };

            this.select = function (p) {
              if (!$scope.wpsLink) {
                return;
              }
              $scope.wpsLink.name = p.identifier.value;
              $scope.wpsLink.url = $scope.url;
            };

            // watch url change from outside
            $scope.$watch("wpsLink.url", function (value) {
              if (value) {
                $scope.url = value;
                $scope.ctrl.doRequest();
              }
            });
          }
        ]
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWpsRecentList
   * @restrict E
   *
   * @description
   * This directive outputs a list of recently used WPS processes based on
   * local storage.
   */
  module.directive("gnWpsRecentList", [
    "gnWpsService",
    function (gnWpsService) {
      return {
        restrict: "E",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/wps/" + "partials/recentprocesses.html",
        scope: {
          wpsLink: "="
        },
        controllerAs: "ctrl",
        controller: [
          "$scope",
          "$window",
          function ($scope, $window) {
            if (!$window.localStorage) {
              $scope.notSupported = true;
              return;
            }

            $scope.processes = [];

            $scope.$watch(
              function () {
                return $window.localStorage.getItem("gn-wps-processes-history") || "{}";
              },
              function (value) {
                var wpsHistory = JSON.parse(value);
                $scope.processes =
                  wpsHistory.processes &&
                  wpsHistory.processes.map(function (p) {
                    var values = p.split("@");
                    return {
                      name: values[0],
                      url: values[1]
                    };
                  });
              }
            );

            this.select = function (p) {
              if (!$scope.wpsLink) {
                return;
              }
              $scope.wpsLink.name = p.name;
              $scope.wpsLink.url = p.url;
            };
          }
        ]
      };
    }
  ]);
})();
