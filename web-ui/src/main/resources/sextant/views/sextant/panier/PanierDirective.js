(function () {
  goog.provide("sxt_panier_directive");

  goog.require("sx_popup");

  var module = angular.module("sxt_panier_directive", []);

  /**
   * @ngdoc directive
   * @name sxt_panier_directive.directive:sxtPanier
   *
   * @description
   */
  module.directive("sxtPanier", [
    "sxtPanierService",
    "gnSearchLocation",
    function (sxtPanierService, gnSearchLocation) {
      return {
        restrict: "A",
        replace: true,
        scope: true,
        templateUrl: "../../sextant/views/sextant/panier/" + "partials/panier.html",
        controller: [
          "$scope",
          function ($scope) {
            this.del = function (element) {
              var index = $scope.panier.indexOf(element);
              $scope.clearWpsInputs(index);
              $scope.panier.splice(index, 1);
            };
          }
        ],
        link: function (scope, element, attrs, controller) {
          scope.$watch("searchObj.panier", function (v) {
            scope.panier = v;
          });

          var modal = element.find(".modal");
          $(".g").append(element.find(".modal"));
          modal.on("shown.bs.modal", function () {
            $(".g").append(modal.data("bs.modal").$backdrop);
          });

          scope.locService = gnSearchLocation;

          scope.formObj = {
            user: {
              lastname: "",
              firstname: "",
              mail: "",
              org: ""
            },
            layers: []
          };

          // because user loading is asynch
          var unregisterUserWatch = scope.$watch(
            "user",
            function (user) {
              if (user && user.surname) {
                scope.prefillUser = !user.generic;
                if (scope.prefillUser) {
                  angular.extend(scope.formObj.user, {
                    lastname: user.surname,
                    firstname: user.name,
                    mail: angular.isArray(user.email) ? user.email[0] : user.email,
                    org:
                      (angular.isArray(user.organisation)
                        ? user.organisation[0]
                        : user.organisation) || ""
                  });
                }
                unregisterUserWatch();
              }
            },
            true
          );

          scope.extract = function () {
            sxtPanierService.extract(scope.formObj).then(function (data) {
              modal.modal("hide");
              if (data.data.success) {
                scope.report = {
                  success: true
                };

                scope.clearWpsInputs();
                scope.searchObj.panier = [];
                scope.formObj.layers = [];
              } else {
                scope.report = {
                  success: false
                };
              }
            });
          };

          scope.goBack = function () {
            window.history.back();
          };

          scope.resetReport = function () {
            scope.report = undefined;
          };

          scope.$on("renderPanierMap", function () {
            scope.resetReport();
          });

          /**
           * Will clear WPS form inputs for the given element; if no index specified, clears inputs of all processes
           * and trigger a reset of the WPS form state.
           * Please note: this will have side effects as processes are objects shared among several WPS form directives
           * @param {number=} index Index of the element to clear the inputs of
           */
          scope.clearWpsInputs = function (index) {
            if (!scope.panier || !Array.isArray(scope.panier)) {
              return;
            }
            scope.panier.forEach(function (elt, i) {
              if (index !== undefined && index !== i) {
                return;
              }
              if (!elt.processes || !Array.isArray(elt.processes)) {
                return;
              }

              elt.processes.forEach(function (process) {
                process.inputs.length = 0;
                process.resetForm = true; // this is watched by WpsDirective
              });
            });
          };
        }
      };
    }
  ]);

  module.directive("sxtPanierElt", [
    "gnMap",
    "gnSearchSettings",
    "gnPanierSettings",
    "gnSearchLocation",
    "$timeout",
    function (gnMap, gnSearchSettings, gnPanierSettings, gnSearchLocation, $timeout) {
      return {
        restrict: "A",
        require: "^sxtPanier",
        replace: true,
        scope: {
          element: "=sxtPanierElt",
          formObj: "=sxtPanierEltForm"
        },
        templateUrl:
          "../../sextant/views/sextant/panier/" + "partials/panierelement.html",
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              /** To iniate combo box values */
              scope.settings = gnPanierSettings;

              scope.isCopyfile = scope.element.link.protocol == "COPYFILE";

              /** Use to know if we need to zoom on the md extent or not */
              var rendered = false;

              /** object that contains the form values */
              var inputCrs,
                outputCrs,
                crs = angular.isArray(scope.element.md.crsDetails)
                  ? scope.element.md.crsDetails[0]
                  : scope.element.md.crsDetails;

              var crsExp = crs && /(?:EPSG:)(\d+)/.exec(crs.code),
                httpUrlPrefix = "http://www.opengis.net/def/crs/EPSG/0/";
              if (angular.isArray(crsExp)) {
                inputCrs = crsExp[1];
              } else if (crs && crs.code.indexOf(httpUrlPrefix) === 0) {
                inputCrs = crs.code.replace(httpUrlPrefix, "");
              } else {
                inputCrs = crs && crs.code.split("::")[crs.code.split("::").length - 1];
              }

              if (!inputCrs) {
                inputCrs = gnPanierSettings.projs[0].value;
              }

              if (inputCrs) {
                for (var i = 0; i < gnPanierSettings.projs.length; i++) {
                  var p = gnPanierSettings.projs[i];
                  if (p.value == inputCrs) {
                    outputCrs = inputCrs;
                    break;
                  }
                }
              }

              if (!outputCrs) {
                outputCrs = gnPanierSettings.projs[0].value;
              }

              var dataTypes = scope.element.md.cl_spatialRepresentationType;
              var dataType;
              if (dataTypes && dataTypes.length > 0) {
                dataType = dataTypes[0]["key"];
              }
              dataType = dataType == "grid" ? "raster" : dataType;

              // To pass the extent into an object for scope issues
              scope.prop = {};
              scope.formats = dataType
                ? scope.settings.formats[dataType]
                : scope.settings.formats["vector"].concat(
                    scope.settings.formats["raster"]
                  );

              scope.form = {
                id: scope.element.md.uuid,
                input: {
                  format: dataType,
                  epsg: inputCrs,
                  protocol: scope.element.link.protocol,
                  linkage: scope.element.link.url
                },
                output: {
                  format: scope.formats[0].value,
                  epsg: outputCrs,
                  name: scope.element.link.name
                },

                // this ref to the element will not be sent to the extractor
                // and is only used to add additional inputs to the form object
                _element: scope.element
              };

              if (!scope.isCopyfile) {
                /** Map useed to draw the bbox */
                scope.map = new ol.Map({
                  layers: [
                    new ol.layer.Tile({
                      source: new ol.source.OSM()
                    })
                  ],
                  controls: [],
                  view: new ol.View({
                    center: [0, 0],
                    zoom: 2
                  })
                });

                // Set initial extent to draw the BBOX
                var extents = gnMap.getBboxFromMd(scope.element.md);
                if (extents.length > 0) {
                  // Fixed feature overlay to show extent of the md
                  var feature = gnMap.getBboxFeatureFromMd(
                    scope.element.md,
                    scope.map.getView().getProjection()
                  );

                  var featureOverlay = new ol.layer.Vector({
                    source: new ol.source.Vector({
                      useSpatialIndex: false
                    }),
                    style: gnSearchSettings.olStyles.mdExtent,
                    map: scope.map
                  });

                  featureOverlay.getSource().addFeature(feature);

                  // Set coords in the scope to pass it to the mapField directive
                  // (only if raster format to avoid excluding data by mistake: https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/199)
                  if (dataType === "raster") {
                    var extent = extents[0];
                    var proj = scope.map.getView().getProjection();
                    extent = ol.extent.containsExtent(proj.getWorldExtent(), extent)
                      ? ol.proj.transformExtent(extent, "EPSG:4326", proj)
                      : proj.getExtent();

                    scope.extentCoords = gnMap.getPolygonFromExtent(extent);
                  }
                }

                // To update size on first maps render
                scope.$on("renderPanierMap", function () {
                  scope.map.updateSize();
                  if (feature && !rendered) {
                    scope.map
                      .getView()
                      .fit(feature.getGeometry().getExtent(), scope.map.getSize());
                  }
                  rendered = true;
                });
              } else {
                scope.form.input.epsg = "";
                scope.form.input.format = "";
                scope.form.output.epsg = "";
                scope.form.output.format = "";
              }

              // by default, include WFS filters if there are any
              scope.form.useFilters = true;

              // register WFS link if any
              scope.getWfsLink = function () {
                return scope.element.link.protocol === "OGC:WFS"
                  ? scope.element.link
                  : null;
              };

              scope.isVisible = function () {
                return gnSearchLocation.path() === "/panier";
              };

              // open the wps modal with which we share a parent
              scope.openWpsModal = function ($event) {
                $($event.currentTarget.parentElement.querySelector(".modal")).modal(
                  "show"
                );
              };
            },
            post: function preLink(scope, iElement, iAttrs, controller) {
              scope.formObj.layers.push(scope.form);

              scope.del = function (element, form) {
                controller.del(element);
                scope.formObj.layers.splice(scope.formObj.layers.indexOf(form), 1);
              };

              var format = new ol.format.WKT();
              scope.$watch("element.filter.extent", function (e) {
                if (e) {
                  scope.prop.extent = format.writeGeometry(ol.geom.Polygon.fromExtent(e));
                }
              });

              scope.$watch("prop.extent", function (n) {
                if (n) {
                  try {
                    var g = format.readGeometry(n);
                    var e = g.getExtent();
                    angular.extend(scope.form.output, {
                      xmin: e[0].toString(),
                      ymin: e[1].toString(),
                      xmax: e[2].toString(),
                      ymax: e[3].toString(),
                      mercator_lat: ""
                    });
                  } catch (e) {}
                }
              });

              // Generate WPS form
              scope.saveExecuteMessage = function (message, process) {
                process.executeMessage = message;
                process.described = true;
              };

              scope.submitProcess = function (evt) {
                $timeout(function () {
                  $(evt.target).parent().parent().find("button[type=submit]").click();
                });
              };
            }
          };
        }
      };
    }
  ]);
})();
