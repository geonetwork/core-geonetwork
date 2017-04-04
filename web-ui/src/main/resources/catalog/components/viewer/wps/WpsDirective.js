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
   * @param {Object} map
   * @param {Object} wpsLink
   * @param {boolean} hideExecuteButton if true, the 'execute' button is hidden
   * @param {Object} wfsLink the WFS link object will be used to overload
   *  inputs based on active WFS feature filters
   *
   * TODO: Add batch mode using md.privileges.batch
   * and md.privileges.batch.update services.
   *
   * TODO: User group only privilege
   */
  module.directive('gnWpsProcessForm', [
    'gnWpsService',
    'gnUrlUtils',
    '$timeout',
    'wfsFilterService',
    function(gnWpsService, gnUrlUtils, $timeout, wfsFilterService) {

      var inputTypes = {
        string: 'text',
        float: 'number'
      };

      var parseKvpParams = function(str) {
        var escaper = function(match, p1) {
          return '=' + gnUrlUtils.encodeUriQuery(p1, true);
        };
        str = str.replace(/=\[([^&]*)\]/gi, escaper);

        var queryParams = gnUrlUtils.parseKeyValue(str);
        var defaults = {};
        for (var prop in queryParams) {
          if (queryParams.hasOwnProperty(prop)) {
            defaults[prop.toLowerCase()] = queryParams[prop];
          }
        }

        var parseDataInputs = function(value) {
          var datainputs = {};

          if (value === undefined) {
            return datainputs;
          }

          value.split(';').map(function(str) {
            var list = str.split('@');

            var input = list[0].split('=');
            var identifier = input[0];
            var value = input[1];
            list = list.slice(1);

            var attributes = {};
            list.map(function(attribute) {
              keyValue = attribute.split('=');
              attributes[keyValue[0].toLowerCase()] = keyValue[1];
            });

            datainputs[identifier] = {
              value: value,
              attributes: attributes
            };
          });
          return datainputs;
        };

        defaults.datainputs = parseDataInputs(defaults.datainputs);
        defaults.responsedocument = parseDataInputs(defaults.responsedocument);

        return defaults;
      };

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
          hideExecuteButton: '=',
          wfsLink: '='
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/viewer/wps/partials/processform.html';
        },

        link: function(scope, element, attrs) {
          var defaults;

          var processId = attrs['processId'] || scope.wpsLink.name;
          var uri = attrs['uri'] || scope.wpsLink.url;
          var defaults = scope.$eval(attrs['defaults']) ||
              scope.wpsLink.applicationProfile;

          scope.wpsLink.layer.set('wpsfilter-el', element);

          if (defaults) {
            defaults = parseKvpParams(defaults);
          }

          scope.describeState = 'sended';
          scope.executeState = '';

          scope.selectedOutput = {
            identifier: '',
            mimeType: ''
          };

          gnWpsService.describeProcess(uri, processId)
              .then(
              function(response) {
                scope.describeState = 'succeeded';
                scope.describeResponse = response;

                if (response.processDescription) {

                  // Bind input directly in link object
                  scope.processDescription = scope.wpsLink.processDescription ||
                    response.processDescription[0];
                  scope.wpsLink.processDescription = scope.processDescription;

                  angular.forEach(scope.processDescription.dataInputs.input,
                      function(input) {

                        if(input.value) return;
                        var value;
                        var defaultValue;

                        if (defaults) {
                          var datainput =
                              defaults.datainputs[input.identifier.value];
                          if (datainput != undefined) {
                            defaultValue = datainput.value;
                          }
                        }

                        if (input.literalData != undefined) {

                          // Input type
                          input.type =
                            inputTypes[input.literalData.dataType.value];

                          // Default value
                          if (input.literalData.defaultValue != undefined) {
                            value = input.literalData.defaultValue;
                          }
                          if (defaultValue != undefined) {
                            value = defaultValue;
                          }

                          // use overloaded value if applicable
                          if (scope.inputOverloads &&
                            scope.inputOverloads[input.identifier.value]) {
                            value = scope.inputOverloads[input.identifier.value]
                              .currentValue;
                          }

                          // Format conversion
                          switch (input.literalData.dataType.value) {
                            case 'float':
                              value = parseFloat(value); break;
                            case 'string':
                              value = value || ''; break;
                          }
                          input.value = value;
                        }

                        if (input.boundingBoxData != undefined) {
                          input.value = '';
                          if (defaultValue) {
                            input.value = defaultValue.split(',')
                              .slice(0, 4).join(',');
                          }
                        }
                      }
                  );

                  angular.forEach(
                      scope.processDescription.processOutputs.output,
                      function(output, idx) {
                        output.asReference = true;

                        var outputDefault = defaults &&
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
                        }
                        else if (idx == 0) {
                          scope.selectedOutput.identifier =
                              output.identifier.value;
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
                }
                setTimeout(function() {
                  element.parents('[sxt-custom-scroll]').
                      mCustomScrollbar('update');
                }, 0);
              },
              function(response) {
                scope.describeState = 'failed';
                scope.describeResponse = response;
              }
              );

          scope.close = function() {
            element.remove();
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

            // Validate inputs
            var invalid = false;
            angular.forEach(scope.processDescription.dataInputs.input,
                function(input) {
                  input.invalid = undefined;
                  if (input.minOccurs > 0 && (input.value === null ||
                      input.value === '')) {
                    input.invalid = input.title.value + ' is mandatory';
                    invalid = true;
                  }
                });
            if (invalid) { return; }

            var inputs = scope.processDescription.dataInputs.input.reduce(
                function(o, v, i) {
                  if (v.identifier.value == 'limits' &&
                    (v.value == '' || v.value == ',,,')) {
                    if(v.minOccurs > 0) {
                      o['limits'] = 'NaN,NaN,NaN,NaN';
                    }
                  } else {
                    if(v.minOccurs > 0 || v.value) {
                      o[v.identifier.value] = v.value;
                    }
                  }
                  return o;
                }, {});

            var outputs = [];
            angular.forEach(scope.processDescription.processOutputs.output,
                function(output) {
                  if (output.identifier.value ==
                      scope.selectedOutput.identifier) {
                    outputs.push({
                      asReference: output.asReference,
                      mimeType: output.mimeType,
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

                    if (response.status.processSucceeded) {
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
            };

            scope.running = true;
            scope.executeState = 'sended';
            gnWpsService.execute(
                uri,
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
          scope.$watch('selectedOutput.identifier', function(v) {
            if (v) {
              try {
                scope.selectedOutput.mimeType = '';
                var os = scope.describeResponse.
                    processDescription[0].processOutputs.output;

                for (var i = 0; i < os.length; i++) {
                  var o = os[i];
                  if (v == o.identifier.value) {
                    for (var j = 0;
                         j < o.complexOutput.supported.format.length;
                         j++) {
                      var f = o.complexOutput.supported.format[j];
                      if (f.mimeType == gnWpsService.WMS_MIMETYPE) {
                        o.mimeType = f.mimeType;
                        break;
                      }
                    }
                    if (!o.mimeType) {
                      o.mimeType = o.complexOutput._default.format.mimeType;
                    }
                    break;
                  }
                }
              }
              catch (e) {
                // can't auto find mimetype
              }
            }
          });

          // helpers for accessing input values
          var getInputValue = function (name) {
            if (!scope.processDescription) { return; }

            var result = null;
            angular.forEach(scope.processDescription.dataInputs.input,
              function(input) {
                if (input.identifier.value == name) {
                  result = input.value;
                }
              });
            return result;
          }
          var setInputValue = function (name, value) {
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
            scope.esObject = wfsFilterService.getEsObject(scope.wfsLink.url,
              scope.wfsLink.name);

            // this will hold input overload info
            // keys are overloaded inputs names, values are objects like so:
            //  { currentValue: any, oldValue: any }
            scope.inputOverloads = {};

            // on esObject change (deep check), input overload info is refreshed
            scope.$watch('esObject', function(newValue, oldValue) {
              // do nothing if the object is not present
              if (!newValue) { return; }

              // TEMP: this is supposed to be on the WPS link object!!
              var appProfile = {
                DATAINPUTS: {
                  'produit_id': 'ft_ent_prog_cd_s',
                  limits: 'filtre_EFG'
                },
              };

              // get list of filters
              var filterValues = wfsFilterService.toObjectProperties(newValue);

              // transform according to app profile
              var inputValues = {};
              Object.keys(appProfile.DATAINPUTS).forEach(function (key) {
                if (filterValues[appProfile.DATAINPUTS[key]]) {
                  inputValues[key] = filterValues[appProfile.DATAINPUTS[key]];
                }
              });

              // loop on these
              Object.keys(inputValues).forEach(function (name) {
                // new overload
                if (!scope.inputOverloads[name]) {
                  scope.inputOverloads[name] = {
                    oldValue: getInputValue(name)
                  }
                }
                scope.inputOverloads[name].currentValue = inputValues[name];
                setInputValue(name, inputValues[name]);
              });

              // clear non existing overloads
              Object.keys(scope.inputOverloads).forEach(function (name) {
                // new overload
                if (!inputValues[name]) {
                  setInputValue(name, scope.inputOverloads[name].oldValue);
                  delete scope.inputOverloads[name];
                }
              });

            }, true);
          }
        }
      };
    }
  ]);
})();
