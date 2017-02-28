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
  goog.provide('gn_onlinesrc_directive');


  goog.require('ga_print_directive');
  goog.require('gn_utility');

  /**
   * @ngdoc overview
   * @name gn_onlinesrc
   *
   * @description
   * Provide directives for online resources
   * <ul>
   * <li>gnOnlinesrcList</li>
   * <li>gnAddOnlinesrc</li>
   * <li>gnLinkServiceToDataset</li>
   * <li>gnLinkToMetadata</li>
   * </ul>
   */
  angular.module('gn_onlinesrc_directive', [
    'gn_utility',
    'blueimp.fileupload',
    'ga_print_directive'
  ])

      /**
   * @ngdoc directive
   * @name gn_onlinesrc.directive:gnOnlinesrcList
   *
   * @restrict A
   *
   * @description
   * The `gnOnlinesrcList` directive is used
   * to display the list of
   * all online resources attached to the current metadata.
   * The template will show up a list of all kinds
   * of resource, and
   * links to create new resources of those kinds.
   *
   * The list is shown on directive call, and is
   * refresh on 2 events:
   * <ul>
   *  <li> When the flag onlinesrcService.reload is
   *  set to true, the service
   *    requires a refresh of the list, the directive
   *    here is watching this
   *    value to refresh when it is required.</li>
   *  <li> When the metadata is saved, the
   *  gnCurrentEdit.version is updated and the list
   *  of resources is reloaded.</li>
   * </ul>
   *
   */
      .directive('gnOnlinesrcList', ['gnOnlinesrc', 'gnCurrentEdit', '$filter',
        function(gnOnlinesrc, gnCurrentEdit, $filter) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/onlinesrcList.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.onlinesrcService = gnOnlinesrc;
              scope.gnCurrentEdit = gnCurrentEdit;
              scope.allowEdits = true;
              scope.lang = scope.$parent.lang;

              /**
               * Calls service 'relations.get' to load
               * all online resources of the current
               * metadata into the list
               */
              var loadRelations = function() {
                gnOnlinesrc.getAllResources()
                    .then(function(data) {
                      scope.relations = data;
                    });
              };
              scope.isCategoryEnable = function(category) {
                var config = gnCurrentEdit.schemaConfig.related;
                if (config.readonly === true) {
                  return false;
                } else {
                  if (config.categories &&
                      config.categories.length > 0 &&
                      $.inArray(category, config.categories) === -1) {
                    return false;
                  } else {
                    return true;
                  }
                }
              };

              // Reload relations when a directive requires it
              scope.$watch('onlinesrcService.reload', function() {
                if (scope.onlinesrcService.reload) {
                  loadRelations();
                  scope.onlinesrcService.reload = false;
                }
              });

              // When saving is done, refresh related resources
              scope.$watch('gnCurrentEdit.version',
                  function(newValue, oldValue) {
                    if (parseInt(newValue || 0) > parseInt(oldValue || 0)) {
                      loadRelations();
                    }
                  });
              scope.sortLinks = function(g) {
                return $filter('gnLocalized')(g);
              };
            }
          };
        }])

      /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnAddOnlinesrc
     * @restrict A
     * @requires gnOnlinesrc
     * @requires gnOwsCapabilities
     * @requires gnEditor
     * @requires gnCurrentEdit
     *
     * @description
     * The `gnAddOnlinesrc` directive provides a form to add a
     * new online resource
     * to the currend metadata. Depending on the protocol :
     * <ul>
     *  <li>DOWNLOAD : we upload a data from the disk.</li>
     *  <li>OGC:WMS : we call a capabilities on the given url,
     *  then the user can add
     *    several resources (layers) at the same time.</li>
     *  <li>Others : we just fill the form and call a batch processing.</li>
     * </ul>
     *
     * On submit, the metadata is saved, the thumbnail is added, then the form
     * and online resource list are refreshed.
     */
      .directive('gnAddOnlinesrc', [
        'gnOnlinesrc',
        'gnOwsCapabilities',
        'gnWfsService',
        'gnEditor',
        'gnCurrentEdit',
        'gnMap',
        'gnGlobalSettings',
        'Metadata',
        '$rootScope',
        '$translate',
        '$timeout',
        '$http',
        '$filter',
        function(gnOnlinesrc, gnOwsCapabilities, gnWfsService,
            gnEditor, gnCurrentEdit, gnMap, gnGlobalSettings, Metadata,
            $rootScope, $translate, $timeout, $http, $filter) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addOnlinesrc.html',
            link: {
              pre: function preLink(scope) {
                scope.searchObj = {
                  params: {}
                };
                scope.modelOptions =
                    angular.copy(gnGlobalSettings.modelOptions);
              },
              post: function(scope, element, attrs) {
                scope.popupid = attrs['gnPopupid'];

                //{
                //  // Optional / optGroup
                //  group: 'onlineDiscover',
                //  // Label
                //    label: 'onlineDiscoverWMS',
                //  // Optional / On select copy the label in desc field
                //  copyLabel: 'desc',
                //  // Optional / Icon
                //  icon: 'fa gn-icon-onlinesrc',
                //  // XSL process to run
                //  process: 'onlinesrc-add',
                //  // Optional / List of fields
                //  // (URL only will be displayed if none)
                //  fields: {
                //  'url': {},
                //  'protocol': {
                //    // Fixed value
                //    value: 'OGC:WMS',
                //      // Hide field
                //      hidden: true},
                //  'name': {},
                //  'desc': {
                //    // Rename parameter for the XSL process
                //    param: 'myParam'},
                //  'function': {value: 'browsing', hidden: true}
                //}
                //}

                var schemaConfig = {
                  'dublin-core': {
                    display: 'radio',
                    types: [{
                      label: 'addOnlinesrc',
                      sources: {
                        filestore: true,
                        thumbnailMaker: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {}
                      }
                    }]
                  },
                  'iso19139': {
                    display: 'radio',
                    types: [{
                      label: 'addOnlinesrc',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'WWW:LINK-1.0-http--link'},
                        'name': {},
                        'desc': {},
                        'function': {},
                        'applicationProfile': {}
                      }
                    }, {
                      label: 'addThumbnail',
                      sources: {
                        filestore: true,
                        thumbnailMaker: true
                      },
                      icon: 'fa gn-icon-thumbnail',
                      fileStoreFilter: '*.{jpg,JPG,png,PNG,gif,GIF}',
                      process: 'thumbnail-add',
                      fields: {
                        'url': {param: 'thumbnail_url'},
                        'name': {param: 'thumbnail_desc'}
                      }
                    }]
                  },
                  'iso19115-3': {
                    display: 'select',
                    types: [{
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverThumbnail',
                      sources: {
                        filestore: true,
                        thumbnailMaker: true
                      },
                      icon: 'fa gn-icon-thumbnail',
                      fileStoreFilter: '*.{jpg,JPG,png,PNG,gif,GIF}',
                      process: 'thumbnail-add',
                      fields: {
                        'url': {},
                        'name': {param: 'desc'}
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverInApp',
                      copyLabel: 'name',
                      sources: {
                        metadataStore: {
                          label: 'searchAnApplication',
                          params: {
                            type: 'application'
                          }
                        }
                      },
                      icon: 'fa gn-icon-map',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true}
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverWMS',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'OGC:WMS or WMS or view'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'OGC:WMS', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true}
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverINSPIREView',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'view'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'OGC:WMS', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true},
                        'applicationProfile': {
                          value: 'inspire-view', hidden: true
                        }
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverWMTS',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'OGC:WMTS or WMTS'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'OGC:WMTS', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true}
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverArcGIS',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'ESRI:REST'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'ESRI:REST', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true}
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverKML',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true},
                        'applicationProfile': {
                          value: 'application/vnd.google-earth.kml+xml',
                          hidden: true
                        }
                      }
                    }, {
                      group: 'onlineDiscover',
                      label: 'onlineDiscoverMap',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-map',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'browsing', hidden: true},
                        'applicationProfile': 'applicationProfile'
                      }
                    }, {
                      group: 'onlineDownload',
                      label: 'onlineDownloadFile',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'download', hidden: true}
                      }
                    }, {
                      group: 'onlineDownload',
                      label: 'onlineDownloadKML',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'download', hidden: true},
                        'applicationProfile': {
                          value: 'application/vnd.google-earth.kml+xml',
                          hidden: true
                        }
                      }
                    }, {
                      group: 'onlineDownload',
                      label: 'onlineDownloadWWW',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'download', hidden: true}
                      }
                    }, {
                      group: 'onlineDownload',
                      label: 'onlineDownloadWFS',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'OGC:WFS or WFS or download'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'OGC:WFS', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'download', hidden: true}
                      }
                    }, {
                      group: 'onlineDownload',
                      label: 'onlineDownloadWCS',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'OGC:WCS or WCS'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'OGC:WCS', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'download', hidden: true}
                      }
                    }, {
                      group: 'onlineDownload',
                      label: 'onlineDownloadINSPIRE',
                      copyLabel: 'desc',
                      icon: 'fa gn-icon-onlinesrc',
                      sources: {
                        metadataStore: {
                          label: 'searchAservice',
                          params: {
                            serviceType: 'download'
                          }
                        }
                      },
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {value: 'OGC:WFS', hidden: true},
                        'name': {},
                        'desc': {},
                        'function': {value: 'download', hidden: true},
                        'applicationProfile': {
                          value: 'inspire-download', hidden: true
                        }
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseFcats',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa fa-table',
                      process: 'fcats-file-add',
                      fields: {
                        'url': {},
                        'name': {}
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseDQReport',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa fa-table',
                      process: 'dq-report-add',
                      fields: {
                        'url': {},
                        'name': {},
                        'desc': {},
                        'type': {param: 'type', value: 'qualityReport'}
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseDQTOR',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa fa-table',
                      process: 'dq-report-add',
                      fields: {
                        'url': {},
                        'name': {},
                        'desc': {},
                        'type': {param: 'type', value: 'qualitySpecification'}
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseDQProdReport',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa fa-table',
                      process: 'dq-report-add',
                      fields: {
                        'url': {},
                        'name': {},
                        'desc': {},
                        'type': {param: 'type', value: 'lineage'}
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseLegendLYR',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      fileStoreFilter: '*.{lyr,LYR}',
                      icon: 'fa fa-table',
                      process: 'legend-add',
                      fields: {
                        'url': {},
                        'name': {}
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseStyleSLD',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      fileStoreFilter: '*.{sld,SLD}',
                      icon: 'fa fa-table',
                      process: 'legend-add',
                      fields: {
                        'url': {},
                        'name': {}
                      }
                    }, {
                      group: 'onlineUse',
                      label: 'onlineUseStyleQML',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      fileStoreFilter: '*.{qml,QML}',
                      icon: 'fa fa-table',
                      process: 'legend-add',
                      fields: {
                        'url': {},
                        'name': {}
                      }
                      //},{
                      //  group: 'onlineUse',
                      //  label: 'onlineUseLimitation',
                      //  copyLabel: 'name',
                      //  sources: {
                      //    templatestore: true,
                      //    filestore: true
                      //  },
                      //  icon: 'fa fa-table',
                      //  process: 'use-limitation-add',
                      //  fields: {
                      //  }
                      //},{
                      //  group: 'onlineUse',
                      //  label: 'onlineAccessLimitation',
                      //  copyLabel: 'name',
                      //  sources: {
                      //    templatestore: true,
                      //    filestore: true
                      //  },
                      //  icon: 'fa fa-table',
                      //  process: 'use-limitation-add',
                      //  fields: {
                      //  }
                    }, {
                      group: 'onlineMore',
                      label: 'onlineMoreWWW',
                      copyLabel: 'name',
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'information', hidden: true}
                      }
                    }, {
                      group: 'onlineMore',
                      label: 'onlineMoreFile',
                      copyLabel: 'name',
                      sources: {
                        filestore: true
                      },
                      icon: 'fa gn-icon-onlinesrc',
                      process: 'onlinesrc-add',
                      fields: {
                        'url': {},
                        'protocol': {
                          value: 'WWW:LINK-1.0-http--link', hidden: true
                        },
                        'name': {},
                        'desc': {},
                        'function': {value: 'information', hidden: true}
                      }
                    }]
                  }
                };
                scope.config = null;
                scope.linkType = null;

                scope.loaded = false;
                scope.layers = null;
                scope.mapId = 'gn-thumbnail-maker-map';
                scope.map = null;

                scope.searchObj = {
                  params: {
                    sortBy: 'title'
                  }
                };

                // This object is used to share value between this
                // directive and the SearchFormController scope that
                // is contained by the directive
                scope.stateObj = {};

                function loadLayers() {
                  if (!angular.isArray(scope.map.getSize()) ||
                      scope.map.getSize().indexOf(0) >= 0) {
                    $timeout(function() {
                      scope.map.updateSize();
                    }, 300);
                  }

                  // Reset map
                  angular.forEach(scope.map.getLayers(), function(layer) {
                    scope.map.removeLayer(layer);
                  });

                  scope.map.addLayer(gnMap.getLayersFromConfig());

                  // Add each WMS layer to the map
                  scope.layers = scope.gnCurrentEdit.layerConfig;
                  angular.forEach(scope.gnCurrentEdit.layerConfig,
                      function(layer) {
                        scope.map.addLayer(new ol.layer.Tile({
                          source: new ol.source.TileWMS({
                            url: layer.url,
                            params: {
                              'LAYERS': layer.name,
                              'URL': layer.url
                            }
                          })
                        }));
                      });

                  $timeout(function() {
                    if (angular.isArray(scope.gnCurrentEdit.extent)) {
                      // FIXME : only first extent is took into account
                      var extent = scope.gnCurrentEdit.extent[0],
                          proj = ol.proj.get(gnMap.getMapConfig().projection),
                          projectedExtent =
                          ol.extent.containsExtent(
                          proj.getWorldExtent(),
                          extent) ?
                          gnMap.reprojExtent(extent, 'EPSG:4326', proj) :
                          proj.getExtent();
                      scope.map.getView().fit(
                          projectedExtent,
                          scope.map.getSize());
                    }
                    // Trigger init of print directive
                    scope.mode = 'thumbnailMaker';
                  }, 300);
                };

                scope.generateThumbnail = function() {
                  return $http.put('../api/0.1/records/' +
                      scope.gnCurrentEdit.uuid +
                      '/attachments/print-thumbnail', null, {
                        params: {
                          jsonConfig: angular.fromJson(scope.jsonSpec)
                        }
                      }).then(function() {
                    $rootScope.$broadcast('gnFileStoreUploadDone');
                  });
                };

                var initThumbnailMaker = function() {

                  if (!scope.loaded) {
                    scope.map = new ol.Map({
                      layers: [],
                      renderer: 'canvas',
                      view: new ol.View({
                        center: [0, 0],
                        projection: gnMap.getMapConfig().projection,
                        zoom: 2
                      })
                    });

                    // we need to wait the scope.hidden binding is done
                    // before rendering the map.
                    scope.map.setTarget(scope.mapId);
                    scope.loaded = true;
                  }

                  scope.$watch('gnCurrentEdit.layerConfig', loadLayers);
                };

                // Check which config to load based on the link
                // to edit properties. A match is returned based
                // on link type and config process prefix. If none found
                // return the first config.
                function getTypeConfig(link) {
                  for (var i = 0; i < scope.config.types.length; i++) {
                    var c = scope.config.types[i];
                    if (scope.schema === 'iso19115-3') {
                      var p = c.fields &&
                              c.fields.protocol &&
                              c.fields.protocol.value || '',
                          f = c.fields &&
                          c.fields.function &&
                          c.fields.function.value || '',
                          ap = c.fields &&
                          c.fields.applicationProfile &&
                          c.fields.applicationProfile.value || '';
                      if (c.process.indexOf(link.type) === 0 &&
                          p === (link.protocol || '') &&
                          f === (link.function || '') &&
                          ap === (link.applicationProfile || '')
                      ) {
                        return c;
                      }
                    } else {
                      if (c.process.indexOf(link.type) === 0) {
                        return c;
                      }
                    }
                  }
                  return scope.config.types[0];
                };
                gnOnlinesrc.register('onlinesrc', function(linkToEdit) {
                  scope.isEditing = angular.isDefined(linkToEdit);

                  scope.metadataId = gnCurrentEdit.id;
                  scope.schema = gnCurrentEdit.schema;
                  scope.config = schemaConfig[scope.schema];
                  if (scope.config === undefined &&
                      scope.schema.indexOf('iso19139') === 0) {
                    scope.config = schemaConfig['iso19139'];
                  }

                  if (gnCurrentEdit.mdOtherLanguages) {

                    scope.mdOtherLanguages = gnCurrentEdit.mdOtherLanguages;
                    scope.mdLangs = JSON.parse(scope.mdOtherLanguages);

                    // not multilingual {"fre":"#"}
                    if (Object.keys(scope.mdLangs).length > 1) {
                      scope.isMdMultilingual = true;
                      scope.mdLang = gnCurrentEdit.mdLanguage;

                      for (var p in scope.mdLangs) {
                        var v = scope.mdLangs[p];
                        if (v.indexOf('#') == 0) {
                          var l = v.substr(1);
                          if (!l) {
                            l = scope.mdLang;
                          }
                          scope.mdLangs[p] = l;
                        }
                      }
                    } else {
                      scope.isMdMultilingual = false;
                    }
                  }

                  initThumbnailMaker();
                  resetForm();

                  $(scope.popupid).modal('show');

                  if (scope.isEditing) {
                    // If the title object contains more than one value,
                    // Then the record resource is multilingual (and
                    // probably the record also).
                    // scope.isMdMultilingual =
                    //   Object.keys(linkToEdit.title).length > 1 ||
                    //   Object.keys(linkToEdit.description).length > 1;


                    // Create a key which will be sent to XSL processing
                    // for finding which element to edit.
                    var keysuffix = $filter('gnLocalized')(linkToEdit.title);
                    if (scope.isMdMultilingual) {
                      // Key in multilingual mode is
                      // the title in the main language
                      keysuffix =
                          linkToEdit.title[Object.keys(scope.mdLangs)[0]];
                      if (angular.isUndefined(keysuffix)) {
                        console.warn(
                            'Failed to compute key for updating the resource.');
                      }
                    }
                    scope.editingKey = [linkToEdit.url,
                                        linkToEdit.protocol,
                                        keysuffix].join('');

                    scope.OGCProtocol = checkIsOgc(linkToEdit.protocol);

                    var name = $filter('gnLocalized')(linkToEdit.title),
                        desc = $filter('gnLocalized')(linkToEdit.description);

                    // For multilingual record, build
                    // name and desc based on loc IDs
                    // and no iso3letter code.
                    // If OGC, only take into account, the first element
                    if (scope.isMdMultilingual && scope.OGCProtocol == null) {
                      name = {};
                      desc = {};
                      $.each(scope.mdLangs, function(key, v) {
                        name[v] =
                            (linkToEdit.title && linkToEdit.title[key]) || '';
                      });
                      $.each(scope.mdLangs, function(key, v) {
                        desc[v] =
                            (linkToEdit.description &&
                             linkToEdit.description[key]) || '';
                      });
                    }

                    scope.params = {
                      linkType: getTypeConfig(linkToEdit),
                      url: linkToEdit.url,
                      protocol: linkToEdit.protocol,
                      name: name,
                      desc: desc,
                      applicationProfile: linkToEdit.applicationProfile,
                      function: linkToEdit.function,
                      selectedLayers: []
                      };
                      } else{
                      scope.editingKey= null;
                      scope.params.linkType= scope.config.types[0];
                      scope.params.protocol= null;
                      setParameterValue(scope.params.name, '');
                      setParameterValue(scope.params.desc, '');
                    }
                  });

                // mode can be 'url' or 'thumbnailMaker' to init thumbnail panel
                scope.mode = 'url';

                // the form parms that will be submited
                scope.params = {};

                // Tells if we need to display layer grid and send
                // layers to the submit
                scope.OGCProtocol = false;

                scope.onlinesrcService = gnOnlinesrc;
                scope.isUrlOk = false;
                scope.setUrl = function(url) {
                  scope.params.url = url;
                };

                var resetForm = function() {
                  if (scope.params) {
                    scope.params.url = '';
                    scope.params.protocol = '';
                    scope.params.function = '';
                    scope.params.applicationProfile = '';
                    resetProtocol();
                  }
                };
                var resetProtocol = function() {
                  scope.layers = [];
                  scope.OGCProtocol = false;
                  if (scope.params && !scope.isEditing) {
                    scope.params.name = scope.isMdMultilingual ? {} : '';
                    scope.params.desc = scope.isMdMultilingual ? {} : '';
                    scope.params.selectedLayers = [];
                    scope.params.layers = [];
                  }
                };


                function buildObjectParameter(param) {
                  if (angular.isObject(param)) {
                    var name = [];
                    for (var p in param) {
                      name.push(p + '#' + param[p]);
                    }
                    return name.join('|');
                  }
                  return param;
                }

                function setParameterValue(param, value) {
                  if (scope.isMdMultilingual) {
                    $.each(scope.mdLangs, function(key, v) {
                      param[v] = value;
                    });
                  } else {
                    param = value;
                  }
                }

                /**
                 *  Add online resource
                 *  If it is an upload, then we submit the
                 *  form with right content
                 *  If it is an URL, we just call a $http.get
                 */
                scope.addOnlinesrc = function() {
                  scope.params.name = buildObjectParameter(scope.params.name);
                  scope.params.desc = buildObjectParameter(scope.params.desc);


                  var processParams = {};
                  angular.forEach(scope.params.linkType.fields,
                      function(value, key) {
                        if (value.param) {
                          processParams[value.param] = scope.params[key];
                        } else {
                          processParams[key] = scope.params[key];
                        }
                      });

                  if (scope.isEditing) {
                    processParams.updateKey = scope.editingKey;
                  }

                  // Add list of layers for WMS
                  if (scope.params.selectedLayers) {
                    processParams.selectedLayers = scope.params.selectedLayers;
                  }
                  processParams.process = scope.params.linkType.process;
                  return scope.onlinesrcService.add(
                      processParams, scope.popupid).then(function() {
                    resetForm();
                  });
                };

                scope.onAddSuccess = function() {
                  gnEditor.refreshEditorForm();
                  scope.onlinesrcService.reload = true;
                };

                /**
                 * loadCurrentLink
                 *
                 * Call WMS capabilities request with params.url.
                 * Update params.layers scope value, that will be also
                 * passed to the layers grid directive.
                 */
                scope.loadCurrentLink = function(reportError) {
                  if (angular.isUndefined(scope.params.url) ||
                      scope.params.url == '') {
                    return;
                  }
                  if (scope.OGCProtocol) {
                    scope.layers = [];
                    if (scope.OGCProtocol == 'WMS') {
                      return gnOwsCapabilities.getWMSCapabilities(
                          scope.params.url)
                          .then(function(capabilities) {
                            scope.layers = [];
                            scope.isUrlOk = true;
                            angular.forEach(capabilities.layers, function(l) {
                              if (angular.isDefined(l.Name)) {
                                scope.layers.push(l);
                              }
                            });
                          }).catch(function(error) {
                            scope.isUrlOk = error === 200;
                          });
                    } else if (scope.OGCProtocol == 'WFS') {
                      return gnWfsService.getCapabilities(
                          scope.params.url)
                          .then(function(capabilities) {
                            scope.layers = [];
                            scope.isUrlOk = true;
                            angular.forEach(
                               capabilities.featureTypeList.featureType,
                               function(l) {
                                 if (angular.isDefined(l.name)) {
                                   scope.layers.push({
                                     Name: l.name.localPart,
                                     abstract: l._abstract,
                                     Title: l.title
                                   });
                                 }
                               });
                          }).catch(function(error) {
                            scope.isUrlOk = error === 200;
                          });
                    }
                  } else if (scope.params.url.indexOf('http') === 0) {
                    var useProxy =
                        scope.params.url.indexOf(location.hostname) === -1;
                    var url = useProxy ?
                        gnGlobalSettings.proxyUrl +
                        encodeURIComponent(scope.params.url) : scope.params.url;
                    return $http.get(url).then(function(response) {
                      scope.isUrlOk = response.status === 200;
                    },
                    function(response) {
                      // Proxy may return 500 when document is not proxyable
                      scope.isUrlOk = response.status === 200;
                    });
                  } else {
                    scope.isUrlOk = true;
                  }
                };

                function checkIsOgc(protocol) {
                  if (protocol && protocol.indexOf('OGC:WMS') >= 0) {
                    return 'WMS';
                  }
                  else if (protocol && protocol.indexOf('OGC:WFS') >= 0) {
                    return 'WFS';
                  }
                  else {
                    return null;
                  }
                };

                /**
                 * On protocol combo Change.
                 * Update OGCProtocol values to display or hide
                 * layer grid and call or not a getCapabilities.
                 */
                scope.$watch('params.protocol', function(n, o) {
                  if (!angular.isUndefined(scope.params.protocol) && o != n) {
                    resetProtocol();
                    scope.OGCProtocol = checkIsOgc(scope.params.protocol);
                    if (scope.OGCProtocol != null && !scope.isEditing) {
                      // Reset parameter in case of multilingual metadata
                      // Those parameters are object.
                      scope.params.name = '';
                      scope.params.desc = '';
                    }
                    scope.loadCurrentLink();
                  }
                });

                /**
                 * On URL change, reload WMS capabilities
                 * if the protocol is WMS
                 */
                scope.$watch('params.url', function() {
                  if (!angular.isUndefined(scope.params.url)) {
                    scope.loadCurrentLink();
                    scope.isImage =
                        scope.params.url.match(/.*.(png|jpg|gif)$/i);
                  }
                });

                /**
                 * Concat layer names and title in params names
                 * and desc fields.
                 * XSL processing tokenize thoses fields and add
                 * them to the record.
                 */
                scope.$watchCollection('params.selectedLayers', function(n, o) {
                  if (o != n &&
                      scope.params.selectedLayers &&
                      scope.params.selectedLayers.length > 0) {
                    var names = [],
                        descs = [];

                    angular.forEach(scope.params.selectedLayers,
                        function(layer) {
                          names.push(layer.Name || layer.name);
                          descs.push(layer.Title || layer.title);
                        });
                    angular.extend(scope.params, {
                      name: names.join(','),
                      desc: descs.join(',')
                    });
                  }
                });

                /**
                   * Init link based on linkType configuration.
                   * Reset metadata store search, set defaults.
                   */
                scope.$watch('params.linkType', function(newValue, oldValue) {
                  if (newValue !== oldValue) {
                    if (!scope.isEditing) {
                      resetForm();
                    }

                    if (newValue.sources && newValue.sources.metadataStore) {
                      scope.$broadcast('resetSearch',
                          newValue.sources.metadataStore.params);
                    }

                    if (!scope.isEditing &&
                        angular.isDefined(newValue.fields)) {
                      angular.forEach(newValue.fields, function(val, key) {
                        if (angular.isDefined(val.value)) {
                          scope.params[key] = val.value;
                        }
                      });
                    }
                    if (!scope.isEditing &&
                        angular.isDefined(newValue.copyLabel)) {
                      scope.params[newValue.copyLabel] =
                          $translate(newValue.label);
                    }

                    if (newValue.sources && newValue.sources.thumbnailMaker) {
                      loadLayers();
                    }
                  }
                });

                scope.resource = null;
                scope.$watch('resource', function() {
                  if (scope.resource && scope.resource.url) {
                    scope.params.url = '';
                    setParameterValue(scope.params.name, '');
                    $timeout(function() {
                      scope.params.url = scope.resource.url;
                      setParameterValue(scope.params.name,
                          scope.resource.id.split('/').splice(2).join('/'));
                    }, 100);
                  }
                });

                scope.$watchCollection('stateObj.selectRecords',
                    function(n, o) {
                      if (!angular.isUndefined(scope.stateObj.selectRecords) &&
                          scope.stateObj.selectRecords.length > 0 &&
                          n != o) {
                        scope.metadataLinks = [];
                        scope.metadataTitle = '';
                        var md = new Metadata(scope.stateObj.selectRecords[0]);
                        var links = md.getLinksByType();
                        if (angular.isArray(links) && links.length == 1) {
                          scope.params.url = links[0].url;
                        } else {
                          scope.metadataLinks = links;
                          scope.metadataTitle = md.title;
                        }
                      }
                    });
              }
            }
          };
        }])

      /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnLinkServiceToDataset
     * @restrict A
     * @requires gnOnlinesrc
     * @requires gnOwsCapabilities
     * @requires Metadata
     * @requires gnCurrentEdit
     *
     * @description
     * The `gnLinkServiceToDataset` directive provides a
     * form to either add a service
     * to a metadata of type dataset, or to add a dataset to a
     * metadata of service.
     * The process will update both of the metadatas, the current
     * one and the one it
     * is linked to.
     *
     * On submit, the metadata is saved, the thumbnail is added, then the form
     * and online resource list are refreshed.
     */
      .directive('gnLinkServiceToDataset', [
        'gnOnlinesrc',
        'Metadata',
        'gnOwsCapabilities',
        'gnCurrentEdit',
        '$rootScope',
        '$translate',
        'gnGlobalSettings',
        function(gnOnlinesrc, Metadata, gnOwsCapabilities,
            gnCurrentEdit, $rootScope, $translate, gnGlobalSettings) {
          return {
            restrict: 'A',
            scope: {},
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linkServiceToDataset.html',
            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope) {
                  scope.searchObj = {
                    params: {}
                  };
                  scope.modelOptions =
                      angular.copy(gnGlobalSettings.modelOptions);
                },
                post: function postLink(scope, iElement, iAttrs) {
                  scope.mode = iAttrs['gnLinkServiceToDataset'];
                  scope.popupid = '#linkto' + scope.mode + '-popup';
                  scope.alertMsg = null;
                  scope.layerSelectionMode = 'multiple';

                  gnOnlinesrc.register(scope.mode, function() {
                    $(scope.popupid).modal('show');

                    // parameters of the online resource form
                    scope.srcParams = {selectedLayers: []};

                    var searchParams = {
                      type: scope.mode
                    };
                    scope.$broadcast('resetSearch', searchParams);
                    scope.layers = [];
                    // Load service layers on load
                    if (scope.mode !== 'service') {
                      // TODO: Check the appropriate WMS service
                      // or list URLs if many
                      // TODO: If service URL is added, user need to reload
                      // editor to get URL or current record.
                      var links = [];
                      links = links.concat(
                          gnCurrentEdit.metadata.getLinksByType('OGC:WMS'));
                      links = links.concat(
                          gnCurrentEdit.metadata.getLinksByType('wms'));
                      if (angular.isArray(links) && links.length == 1) {
                        var serviceUrl = links[0].url;
                        scope.loadCurrentLink(serviceUrl);
                        scope.srcParams.url = serviceUrl;
                        scope.srcParams.protocol = links[0].protocol || '';
                        scope.srcParams.uuidSrv = gnCurrentEdit.uuid;
                      } else {
                        scope.alertMsg =
                            $translate.instant('linkToServiceWithoutURLError');
                      }
                    }
                  });

                  // This object is used to share value between this
                  // directive and the SearchFormController scope that
                  // is contained by the directive
                  scope.stateObj = {};
                  scope.currentMdTitle = null;

                  /**
                   * loadCurrentLink
                   *
                   * Call WMS capabilities on the service metadata URL.
                   * Update params.layers scope value, that will be also
                   * passed to the layers grid directive.
                   */
                  scope.loadCurrentLink = function(url) {
                    scope.alertMsg = null;
                    return gnOwsCapabilities.getWMSCapabilities(url)
                        .then(function(capabilities) {
                          scope.layers = [];
                          scope.srcParams.selectedLayers = [];
                          scope.layers.push(capabilities.Layer[0]);
                          angular.forEach(scope.layers[0].Layer, function(l) {
                            scope.layers.push(l);
                            // TODO: We may have more than one level
                          });
                        });
                  };

                  /**
                   * Watch the result metadata selection change.
                   * selectRecords is a value of the SearchFormController scope.
                   * On service metadata selection, check if the service has
                   * a WMS URL and send request if yes (then display
                   * layers grid).
                   */
                  scope.$watchCollection('stateObj.selectRecords', function() {
                    scope.currentMdTitle = null;
                    if (!angular.isUndefined(scope.stateObj.selectRecords) &&
                        scope.stateObj.selectRecords.length > 0) {
                      var md = new Metadata(scope.stateObj.selectRecords[0]);
                      scope.currentMdTitle = md.title || md.defaultTitle;
                      if (scope.mode == 'service') {
                        var links = [];
                        scope.layers = [];
                        scope.srcParams.selectedLayers = [];
                        // TODO: WFS ?
                        links = links.concat(md.getLinksByType('OGC:WMS'));
                        links = links.concat(md.getLinksByType('wms'));
                        scope.srcParams.uuidSrv = md.getUuid();
                        scope.srcParams.uuidDS = gnCurrentEdit.uuid;

                        if (angular.isArray(links) && links.length == 1) {
                          scope.loadCurrentLink(links[0].url);
                          scope.srcParams.url = links[0].url;
                        } else {
                          scope.srcParams.url = '';
                          scope.alertMsg = $translate.instant(
                              'linkToServiceWithoutURLError');
                        }
                      }
                      else {
                        scope.srcParams.uuidDS = md.getUuid();
                      }
                    }
                  });

                  /**
                   * Call 2 services:
                   *  - link a dataset to a service
                   *  - link a service to a dataset
                   * Hide modal on success.
                   */
                  scope.linkTo = function() {
                    if (scope.mode == 'service') {
                      return gnOnlinesrc.
                          linkToService(scope.srcParams, scope.popupid);
                    } else {
                      return gnOnlinesrc.
                          linkToDataset(scope.srcParams, scope.popupid);
                    }
                  };
                }
              };
            }
          };
        }])

      /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnLinkToMetadata
     * @restrict A
     * @requires gnOnlinesrc
     * @requires $translate
     *
     * @description
     * The `gnLinkServiceToDataset` directive provides
     * a form to link one metadata to
     * another as :
     * <ul>
     *  <li>parent</li>
     *  <li>feature catalog</li>
     *  <li>source dataset</li>
     * </ul>
     * The directive contains a search form allowing one local selection.
     *
     * On submit, the metadata is saved, the link is added,
     * then the form and online resource list are refreshed.
     */
      .directive('gnLinkToMetadata', [
        'gnOnlinesrc', '$translate', 'gnGlobalSettings',
        function(gnOnlinesrc, $translate, gnGlobalSettings) {
          return {
            restrict: 'A',
            scope: {},
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linkToMd.html',
            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope) {
                  scope.searchObj = {
                    any: '',
                    params: {}
                  };
                  scope.modelOptions =
                      angular.copy(gnGlobalSettings.modelOptions);
                },
                post: function postLink(scope, iElement, iAttrs) {
                  scope.mode = iAttrs['gnLinkToMetadata'];
                  scope.popupid = '#linkto' + scope.mode + '-popup';
                  scope.btn = {};


                  // Append * for like search
                  scope.updateParams = function() {
                    scope.searchObj.params.any =
                        '*' + scope.searchObj.any + '*';
                  };

                  /**
                   * Register a method on popup open to reset
                   * the search form and trigger a search.
                   */
                  gnOnlinesrc.register(scope.mode, function() {
                    $(scope.popupid).modal('show');
                    var searchParams = {};
                    if (scope.mode == 'fcats') {
                      searchParams = {
                        _schema: 'iso19110'
                      };
                      scope.btn = {
                        label: $translate.instant('linkToFeatureCatalog')
                      };
                    }
                    else if (scope.mode == 'parent') {
                      searchParams = {
                        hitsPerPage: 10
                      };
                      scope.btn = {
                        label: $translate.instant('linkToParent')
                      };
                    }
                    else if (scope.mode == 'source') {
                      searchParams = {
                        hitsPerPage: 10
                      };
                      scope.btn = {
                        label: $translate.instant('linkToSource')
                      };
                    }
                    scope.$broadcast('resetSearch', searchParams);
                  });

                  scope.gnOnlinesrc = gnOnlinesrc;
                }
              };
            }
          };
        }])

      /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnLinkToSibling
     * @restrict A
     * @requires gnOnlinesrc
     *
     * @description
     * The `gnLinkToSibling` directive provides a form to link siblings to the
     * current metadata. The user need to specify Association type and
     * Initiative type
     * to be able to add a metadata to his selection. The process allow
     * a multiple selection.
     *
     * On submit, the metadata is saved, the resource is associated,
     * then the form
     * and online resource list are refreshed.
     */
      .directive('gnLinkToSibling', ['gnOnlinesrc', 'gnGlobalSettings',
        function(gnOnlinesrc, gnGlobalSettings) {
          return {
            restrict: 'A',
            scope: {},
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linktosibling.html',
            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope) {
                  scope.searchObj = {
                    any: '',
                    defaultParams: {
                      any: '',
                      from: 1,
                      to: 50,
                      sortBy: 'title',
                      sortOrder: 'reverse'
                      // resultType: 'hits'
                    }
                  };
                  scope.searchObj.params = angular.extend({},
                      scope.searchObj.defaultParams);

                  // Define configuration to restrict search
                  // to a subset of records when an initiative type
                  // and/or association type is selected.
                  // eg. crossReference-study restrict to DC records
                  // using _schema=dublin-core
                  scope.searchParamsPerType = {
                    //'crossReference-study': {
                    //  _schema: 'dublin-core'
                    //},
                    //'crossReference-*': {
                    //  _isHarvested: 'n'
                    //}
                  };

                  scope.modelOptions =
                      angular.copy(gnGlobalSettings.modelOptions);
                },
                post: function postLink(scope, iElement, iAttrs) {
                  scope.popupid = iAttrs['gnLinkToSibling'];

                  /**
                   * Register a method on popup open to reset
                   * the search form and trigger a search.
                   */
                  gnOnlinesrc.register('sibling', function() {
                    $(scope.popupid).modal('show');

                    scope.$broadcast('resetSearch');
                    scope.selection = [];
                  });

                  // Append * for like search
                  scope.updateParams = function() {
                    scope.searchObj.params.any =
                        '*' + scope.searchObj.any + '*';
                  };

                  // Based on initiative type and association type
                  // define custom search parameter and refresh search
                  var setSearchParamsPerType = function() {
                    var p = scope.searchParamsPerType[
                        scope.config.associationType + '-' +
                        scope.config.initiativeType
                        ];
                    var pall = scope.searchParamsPerType[
                        scope.config.associationType + '-*'
                        ];
                    scope.searchObj.params = angular.extend({},
                        scope.searchObj.defaultParams,
                        angular.isDefined(p) ? p : (
                        angular.isDefined(pall) ? pall : {}));
                    scope.$broadcast('resetSearch', scope.searchObj.params);
                  };

                  scope.config = {
                    associationType: null,
                    initiativeType: null
                  };

                  scope.$watchCollection('config', function(n, o) {
                    if (n && n !== o) {
                      setSearchParamsPerType();
                    }
                  });

                  /**
                   * Search a metadata record into the selection.
                   * Return the index or -1 if not present.
                   */
                  var findObj = function(md) {
                    for (i = 0; i < scope.selection.length; ++i) {
                      if (scope.selection[i].md == md) {
                        return i;
                      }
                    }
                    return -1;
                  };

                  /**
                   * Add the result metadata to the selection.
                   * Add it only it associationType & initiativeType are set.
                   * If the metadata alreay exists, it override it with the new
                   * given associationType/initiativeType.
                   */
                  scope.addToSelection =
                      function(md, associationType, initiativeType) {
                    if (associationType) {
                      var idx = findObj(md);
                      if (idx < 0) {
                        scope.selection.push({
                          md: md,
                          associationType: associationType,
                          initiativeType: initiativeType || ''
                        });
                      }
                      else {
                        angular.extend(scope.selection[idx], {
                          associationType: associationType,
                          initiativeType: initiativeType || ''
                        });
                      }
                    }
                  };

                  /**
                   * Remove a record from the selection
                   */
                  scope.removeFromSelection = function(obj) {
                    var idx = findObj(obj.md);
                    if (idx >= 0) {
                      scope.selection.splice(idx, 1);
                    }
                  };

                  /**
                   * Call the batch process to add the sibling
                   * to the current edited metadata.
                   */
                  scope.linkToResource = function() {
                    var uuids = [];
                    for (i = 0; i < scope.selection.length; ++i) {
                      var obj = scope.selection[i];
                      uuids.push(obj.md['geonet:info'].uuid + '#' +
                          obj.associationType + '#' +
                          obj.initiativeType);
                    }
                    var params = {
                      initiativeType: scope.config.initiativeType,
                      associationType: scope.config.associationType,
                      uuids: uuids.join(',')
                    };
                    return gnOnlinesrc.linkToSibling(params, scope.popupid);
                  };
                }
              };
            }
          };
        }]);
})();
