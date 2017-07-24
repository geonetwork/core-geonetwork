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
  goog.provide('gn_wps_directive');

  goog.require('gn_wfsfilter_service');

  var module = angular.module('gn_wps_directive', [
  ]);

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
   *  service to use, namely name and url properties
   * @directiveInfo {Object} wfsLink the WFS link object
   *  will be used to overload inputs based on active WFS feature filters
   * @directiveInfo {boolean} hideExecuteButton if true,
   *  the 'execute' button is hidden
   */
  module.directive('gnWpsProcessForm', [
    'gnWpsService',
    'gnUrlUtils',
    '$timeout',
    'wfsFilterService',
    '$window',
    'gnGeometryService',
    'gnProfileService',
    function(gnWpsService, gnUrlUtils, $timeout, wfsFilterService,
        $window, gnGeometryService, gnProfileService) {

      var toBool = function(str, defaultVal) {
        if (str === undefined) {
          return defaultVal;
        }
        return str.toLowerCase() === 'true';
      };

      return {
        restrict: 'AE',
        scope: {
          map: '=',
          wpsLink: '=',
          wfsLink: '='
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/viewer/wps/partials/processform.html';
        },
        link: function(scope, element, attrs) {
          scope.describeState = 'standby';
          scope.executeState = '';

          scope.selectedOutput = {
            identifier: '',
            mimeType: ''
          };

          scope.hideExecuteButton = attrs.hideExecuteButton;

          // this will hold pre-loaded process descriptions
          // keys are: '<processId>@<uri>'
          scope.loadedDescriptions = {};

          // maximum number of processes id saved in local storage
          var maxHistoryCount = attrs['maxHistory'] || 6;

          // query a process description when a new wps link is given
          // note: a deep equality is required, since what we are actually
          // comparing are process id and url (and not object ref)
          scope.$watch(function() {
            var wpsLink = scope.wpsLink || {};
            return {
              processId: attrs['processId'] || wpsLink.name,
              uri: attrs['uri'] || wpsLink.url
            };
          }, function(newLink, oldLink) {
            // the WPS link is incomplete: leave & clear form
            if (!newLink.uri || !newLink.processId) {
              scope.processDescription = null;
              return;
            }

            if (scope.wpsLink.layer) {
              scope.wpsLink.layer.set('wpsfilter-el', element);
            }

            // prepare inputs object (use existing one if available)
            scope.wpsLink.inputs = scope.wpsLink.inputs || [];

            scope.describeState = 'sent';

            // parse application profile as JSON
            var applicationProfile;
            try {
              applicationProfile = scope.wpsLink.applicationProfile ?
                  JSON.parse(scope.wpsLink.applicationProfile) : null;
            }
            catch (e) {
              console.warn('Error while loading application profile.');
            }

            // getting defaults
            var defaults = scope.$eval(attrs['defaults']);
            if (!defaults && applicationProfile) {
              defaults = applicationProfile.defaults;
            }

            // query a description and build up the form
            gnWpsService.describeProcess(newLink.uri, newLink.processId, {
              cancelPrevious: true
            }).then(
              function(response) {
                scope.describeState = 'succeeded';
                scope.describeResponse = response;

                if (response.processDescription) {
                  // Bind input directly in link object
                  scope.processDescription = response.processDescription[0];

                  // check if we need to get into 'profile graph' mode
                  // FIXME: look for an actual way to determine
                  // the output type...
                  if (scope.processDescription.identifier.value ==
                  'script:computemultirasterprofile') {
                    scope.outputAsGraph = true;
                  } else {
                    scope.outputAsGraph = false;
                  }

                  // loop on process expected inputs to prepare the form
                  angular.forEach(scope.processDescription.dataInputs.input,
                    function(input) {
                      var inputName = input.identifier.value;
                      var value;
                      var defaultValue;

                      // look for default value from app profile field
                      if (defaults && defaults[inputName]) {
                        defaultValue = defaults[inputName];
                      }

                      // use overloaded value if applicable
                      if (scope.inputOverloads &&
                        scope.inputOverloads[inputName]) {
                        defaultValue =
                          scope.inputOverloads[inputName]
                          .currentValue;
                      }

                      // literal data (basic form input)
                      if (input.literalData != undefined) {
                        // Default value (if not already there)
                        if (input.literalData.defaultValue != undefined &&
                          defaultValue === undefined) {
                          defaultValue = input.literalData.defaultValue;

                          // convert value if necessary
                          if (input.literalData.dataType.value == 'float') {
                            defaultValue = parseFloat(defaultValue);
                          }
                        }
                      }

                      // bouding box data
                      if (input.boundingBoxData != undefined) {
                        // format default value for the bbox directive
                        if (defaultValue) {
                          defaultValue = defaultValue.split(',')
                            .slice(0, 4).join(',');
                        }
                      }

                      // complex data: draw a feature on map
                      if (input.complexData != undefined) {
                        // this will be a {ol.Feature} object once drawn
                        input.feature = null;

                        // output format
                        input.outputFormat = gnGeometryService
                          .getFormatFromMimeType(
                              input.complexData._default.format.mimeType
                          ) || 'gml';

                        // guess geometry type from schema url
                        var url = input.complexData._default.format.schema;
                        var result = /\?.*GEOMETRYNAME=([^&\b]*)/gi.exec(url);
                        switch (result && result[1] ?
                          result[1].toLowerCase() : null) {
                          case 'line':
                            input.geometryType = 'LineString';
                            break;

                          case 'point':
                            input.geometryType = 'Point';
                            break;

                          case 'polygon':
                            input.geometryType = 'Polygon';
                            break;

                          // TODO: add other types?

                          default:
                            input.geometryType = null;
                        }
                      }

                      // add input fields if required (add 1 by default)
                      var minCount = Math.max(1, input.minOccurs);
                      var count = scope.getInputsByName(inputName).length;
                      while (count < minCount) {
                        count++;
                        scope.wpsLink.inputs.push({
                          name: inputName,
                          value: defaultValue
                        });
                      }
                    }
                  );

                  angular.forEach(
                    scope.processDescription.processOutputs.output,
                    function(output, idx) {
                      output.asReference = scope.outputAsGraph ? false : true;

                      // untested code
                      var outputDefault = defaults &&
                        defaults.responsedocument &&
                        defaults.responsedocument[output.identifier.value];
                      if (outputDefault) {
                        output.value = true;
                        var defaultAsReference =
                          outputDefault.attributes['asreference'];
                        if (defaultAsReference !== undefined) {
                          output.asReference = toBool(defaultAsReference);
                        }
                        scope.selectedOutput.identifier =
                          output.identifier.value;
                        scope.selectedOutput.mimeType =
                          output.complexOutput._default.format.mimeType;
                      }
                      else if (idx == 0) {
                        scope.selectedOutput.identifier =
                          output.identifier.value;
                        scope.selectedOutput.mimeType =
                          output.complexOutput._default.format.mimeType;
                      }
                    }
                  );
                  var output = scope.processDescription.processOutputs.output;
                  if (output.length == 1) {
                    output[0].value = true;
                  }
                  scope.outputsVisible = true;

                  scope.responseDocument = {
                    lineage: toBool(defaults && defaults.lineage, false),
                    storeExecuteResponse: toBool(defaults &&
                    defaults.storeexecuteresponse, false),
                    status: toBool(defaults && defaults.status, false)
                  };
                  scope.optionsVisible = true;

                  // use existing inputs if available
                  var processKey = newLink.processId + '@' + newLink.uri;
                  var existingDesc = scope.loadedDescriptions[processKey];
                  if (existingDesc) {
                    scope.processDescription = angular.extend(
                      scope.processDescription,
                      existingDesc
                    );
                  }
                  scope.loadedDescriptions[processKey] =
                    angular.extend({}, scope.processDescription);
                }
              },
              function(response) {
                scope.describeState = 'failed';
                scope.describeResponse = response;
              }
            );
          }, true);

          scope.close = function() {
            scope.wpsLink.name = '';
            scope.wpsLink.url = '';
            scope.describeState = 'standby';
          };

          scope.toggleOutputs = function() {
            scope.outputsVisible = !scope.outputsVisible;
          };

          scope.toggleOptions = function() {
            scope.optionsVisible = !scope.optionsVisible;
          };

          scope.submit = function() {
            scope.validation_messages = [];
            scope.exception = undefined;

            // Check that inputs have the required values
            var invalid = false;
            angular.forEach(scope.processDescription.dataInputs.input,
              function(input) {
                // count the number of non empty values
                var valueCount = scope.getInputsByName(input.identifier.value)
                  .filter(function (input) {
                    return input.value;
                  }).length;

                // this will be used to show errors on the form
                input.missingOccursCount = Math.max(0,
                  input.minOccurs - valueCount);

                if (input.missingOccursCount > 0) {
                  invalid = true;
                }
              });

            // there are errors with inputs: leave
            if (invalid) { return; }

            var inputs = scope.wpsLink.inputs;

            // output selection based on form control
            var outputs = [];
            angular.forEach(scope.processDescription.processOutputs.output,
              function(output) {
                if (output.identifier.value ==
                    scope.selectedOutput.identifier) {
                  outputs.push({
                    asReference: output.asReference,
                    mimeType: scope.selectedOutput.mimeType,
                    identifier: {
                      value: output.identifier.value
                    }
                  });
                }
              }, {});
            scope.responseDocument.output = outputs;

            updateStatus = function(statusLocation) {
              gnWpsService.getStatus(statusLocation).then(
                function(response) {
                  processResponse(response);
                },
                function(response) {
                  scope.executeState = 'failed';
                  scope.executeResponse = response;
                }
              );
            };

            processResponse = function(response) {
              if (response.TYPE_NAME === 'OWS_1_1_0.ExceptionReport') {
                scope.executeState = 'finished';
              }
              if (response.TYPE_NAME === 'WPS_1_0_0.ExecuteResponse') {
                if (response.status != undefined) {
                  if (response.status.processAccepted != undefined ||
                      response.status.processPaused != undefined ||
                      response.status.processStarted != undefined) {
                    scope.executeState = 'pending';
                    scope.statusPromise = $timeout(function() {
                      updateStatus(response.statusLocation);
                    }, 1000, true);
                  }
                  if (response.status.processSucceeded != undefined ||
                      response.status.processFailed != undefined) {
                    scope.executeState = 'finished';

                    if (response.status.processSucceeded &&
                        scope.wpsLink.layer) {
                      gnWpsService.extractWmsLayerFromResponse(
                        response, scope.map, scope.wpsLink.layer, {
                          exclude: /^OUTPUT_/
                        }
                      );
                    }
                  }
                }
              }
              scope.executeResponse = response;

              // save raw graph data on view controller & hide it in wps form
              if (scope.outputAsGraph && response.processOutputs) {
                try {
                  var jsonData = JSON.parse(response.processOutputs.output[0]
                    .data.complexData.content);

                  // TODO: use applicationProfile for this
                  gnProfileService.displayProfileGraph(
                    jsonData.profile,
                    {
                      valuesProperty: 'values',
                      xProperty: 'lon',
                      yProperty: 'lat',
                      distanceProperty: 'dist',
                      crs: 'EPSG:4326'
                    }
                  );
                } catch (e) {
                  console.error('Error parsing WPS graph data:',
                    response.processOutputs);
                }
                scope.executeResponse = null;
              }
            };

            var processUri = attrs['uri'] || scope.wpsLink.url;
            var processId = attrs['processId'] || scope.wpsLink.name;

            scope.running = true;
            scope.executeState = 'sent';
            gnWpsService.execute(
              processUri,
              processId,
              inputs,
              scope.responseDocument
            ).then(
              function(response) {
                processResponse(response);
              },
              function(response) {
                scope.executeState = 'failed';
                scope.executeResponse = response;
              }
            ).finally(
              function() {
                scope.running = false;
              });

            // update local storage
            if ($window.localStorage) {
              var key = 'gn-wps-processes-history';
              var processKey = processId + '@' + processUri;
              var history = JSON.parse(
                $window.localStorage.getItem(key) || '{}');
              history.processes = history.processes || [];
              history.processes.unshift(processKey);

              // remove dupes and apply limit
              var count = 0;
              history.processes = history.processes.filter(
                function(value, index, array) {
                  if (array.indexOf(value) !== index ||
                  count >= maxHistoryCount) {
                    return false;
                  } else {
                    count++;
                    return true;
                  }
                }
              );

              $window.localStorage.setItem(key, JSON.stringify(history));
            }
          };

          scope.cancel = function() {
            if ($timeout.cancel(scope.statusPromise)) {
              scope.statusPromise = undefined;
              scope.executeState = 'cancelled';
            }
          };

          scope.responseDocumentStatusChanged = function() {
            if (scope.responseDocument.status == true) {
              scope.responseDocument.storeExecuteResponse = true;
            }
          };

          // Guess the mimeType associated with the selected output.
          // scope.$watch('selectedOutput.identifier', function(v) {
          scope.setOutput = function(identifier, mimeType) {
            scope.selectedOutput.identifier = identifier;
            scope.selectedOutput.mimeType = mimeType;
          };

          // returns a valid input type (for literal data)
          scope.getInputType = function (literalDataType) {
            switch (literalDataType) {
              case 'float': return 'number';
              default: return 'text';
            }
          };

          // get all input values
          scope.getInputsByName = function (name) {
            return scope.wpsLink.inputs.filter(function (input) {
              return input.name == name;
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

          // helpers for accessing input values
          var getInputValue = function(name) {
            if (!scope.processDescription) { return; }

            var result = null;
            angular.forEach(scope.processDescription.dataInputs.input,
              function(input) {
                if (input.identifier.value == name) {
                  result = input.value;
                }
              });
            return result;
          };
          var setInputValue = function(name, value) {
            if (!scope.processDescription) { return; }

            angular.forEach(scope.processDescription.dataInputs.input,
              function(input) {
                if (input.identifier.value == name) {
                  input.value = value;
                }
              });
          };

          // handle input overload from WFS link
          if (scope.wfsLink) {
            // this is the object holding current filter values
            var esObject = wfsFilterService.getEsObject(scope.wfsLink.url,
                scope.wfsLink.name);

            // this will hold input overload info
            // keys are overloaded inputs names, values are objects like so:
            //  { currentValue: any, oldValue: any }
            scope.inputOverloads = {};

            // use filter values in ElasticSearch object state to overload input
            if (esObject) {
              var wfsFilterLinks = applicationProfile &&
                  applicationProfile.wfsFilterLinks ?
                  applicationProfile.wfsFilterLinks : {};

              // get list of filters
              var filterValues = wfsFilterService.toObjectProperties(esObject);

              // transform according to app profile
              var inputValues = {};
              Object.keys(wfsFilterLinks).forEach(function(key) {

                // prefix & suffix are added to the raw filter key
                var filterKey = wfsFilterLinks[key];
                var stringFilterKey = 'ft_' + wfsFilterLinks[key] + '_s';
                var dateFilterKey = 'ft_' + wfsFilterLinks[key] + '_dt';

                // testing each case
                if (filterValues[filterKey]) {
                  inputValues[key] = filterValues[filterKey];
                }
                else if (filterValues[stringFilterKey]) {
                  inputValues[key] = filterValues[stringFilterKey];
                }
                else if (filterValues[dateFilterKey]) {
                  inputValues[key] = filterValues[dateFilterKey];
                }
              });

              // loop on these
              Object.keys(inputValues).forEach(function(name) {
                // new overload
                if (!scope.inputOverloads[name]) {
                  scope.inputOverloads[name] = {
                    oldValue: getInputValue(name)
                  };
                }
                scope.inputOverloads[name].currentValue = inputValues[name];
                setInputValue(name, inputValues[name]);
              });

              // clear non existing overloads
              Object.keys(scope.inputOverloads).forEach(function(name) {
                // new overload
                if (!inputValues[name]) {
                  setInputValue(name, scope.inputOverloads[name].oldValue);
                  delete scope.inputOverloads[name];
                }
              });
            }
          }
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
  module.directive('gnWpsUrlDiscovery', [
    'gnWpsService',
    'gnUrlUtils',
    function(gnWpsService, gnUrlUtils) {
      return {
        restrict: 'E',
        templateUrl: '../../catalog/components/viewer/wps/' +
            'partials/urldiscovery.html',
        scope: {
          wpsLink: '='
        },
        controllerAs: 'ctrl',
        controller: ['$scope', function($scope) {
          $scope.loading = false;
          $scope.processes = [];
          $scope.error = null;
          $scope.url = '';

          this.doRequest = function() {
            // do nothing if invalid url
            if (!gnUrlUtils.isValid($scope.url)) {
              return;
            }

            $scope.loading = true;
            $scope.processes = [];
            $scope.error = null;

            gnWpsService.getCapabilities($scope.url, {
              cancelPrevious: true
            }).then(function(data) {
              $scope.loading = false;

              if (!data) {
                $scope.error = 'Service not found';
                return;
              }
              $scope.processes = data.processOfferings.process;
            }, function(error) {
              $scope.loading = false;
              $scope.processes = [];
              $scope.error = error.status + ' ' + error.statusText;
            });
          };

          this.select = function(p) {
            if (!$scope.wpsLink) { return; }
            $scope.wpsLink.name = p.identifier.value;
            $scope.wpsLink.url = $scope.url;
          };

          // watch url change from outside
          $scope.$watch('wpsLink.url', function(value) {
            if (value) {
              $scope.url = value;
              $scope.ctrl.doRequest();
            }
          });
        }]
      };
    }]
  );

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWpsRecentList
   * @restrict E
   *
   * @description
   * This directive outputs a list of recently used WPS processes based on
   * local storage.
   */
  module.directive('gnWpsRecentList', [
    'gnWpsService',
    function(gnWpsService) {
      return {
        restrict: 'E',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wps/' +
            'partials/recentprocesses.html',
        scope: {
          wpsLink: '='
        },
        controllerAs: 'ctrl',
        controller: ['$scope', '$window', function($scope, $window) {
          if (!$window.localStorage) {
            $scope.notSupported = true;
            return;
          }

          $scope.processes = [];

          $scope.$watch(function() {
            return $window.localStorage.getItem('gn-wps-processes-history') ||
                '{}';
          }, function(value) {
            var wpsHistory = JSON.parse(value);
            $scope.processes = wpsHistory.processes &&
                wpsHistory.processes.map(function(p) {
                  var values = p.split('@');
                  return {
                    name: values[0],
                    url: values[1]
                  };
                });
          });

          this.select = function(p) {
            if (!$scope.wpsLink) { return; }
            $scope.wpsLink.name = p.name;
            $scope.wpsLink.url = p.url;
          };
        }]
      };
    }]
  );

})();
