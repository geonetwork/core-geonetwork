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
   * <li>gnAddThumbnail</li>
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
   *  gnCurrentEdit.saving flag is set
   *    to true and we refresh the data. It doesn't
   *    append when the onlinesrcService
   *    wants to save the metadata (the saving flag
   *    is kept as false) to avoid
   *    undesired refresh.</li>
   * </ul>
   *
   */
  .directive('gnOnlinesrcList', ['gnOnlinesrc', 'gnCurrentEdit',
        function(gnOnlinesrc, gnCurrentEdit) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/onlinesrcList.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.onlinesrcService = gnOnlinesrc;
              scope.gnCurrentEdit = gnCurrentEdit;

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

              // When saving is done, refresh validation report
              scope.$watch('gnCurrentEdit.saving', function(newValue) {
                if (newValue === false) {
                  loadRelations();
                }
              });
            }
          };
        }])

      /**
   * @ngdoc directive
   * @name gn_onlinesrc.directive:gnAddThumbnail
   * @restrict A
   * @requires gnOnlinesrc
   * @requires gnEditor
   * @requires gnCurrentEdit
   *
   * @description
   * The `gnAddThumbnail` directive provides a form to add a new thumbnail
   * from an url or by uploading an image.
   * On submit, the metadata is saved, the thumbnail is added, then the form
   * and online resource list are refreshed.
   */
   .directive('gnAddThumbnail', ['$http', '$rootScope', '$translate',
        '$timeout', '$q',
        'gnOnlinesrc',
        'gnEditor',
        'gnCurrentEdit',
        'gnConfig',
        'gnMap',
        function($http, $rootScope, $translate, $timeout, $q,
                 gnOnlinesrc, gnEditor, gnCurrentEdit,
                 gnConfig, gnMap) {
          return {
            restrict: 'A',
            scope: {
              gnPopupid: '='
            },
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addThumbnail.html',
            scope: {},
            link: function(scope, element, attrs) {

              // mode can be 'url' or 'upload'
              scope.mode = 'url';
              scope.action = 'md.thumbnail.upload';

              // the form params that will be submitted
              scope.params = {};

              scope.popupid = attrs['gnPopupid'];
              scope.mapId = 'gn-thumbnail-maker-map';
              scope.loaded = false;
              scope.layers = null;
              scope.gnCurrentEdit = gnCurrentEdit;
              scope.map = null;

              function loadLayers() {
                if (!angular.isArray(scope.map.getSize()) ||
                    scope.map.getSize().indexOf(0) >= 0) {
                  $timeout(function() {
                    scope.map.updateSize();
                  });
                }

                // Reset map
                angular.forEach(scope.map.getLayers(), function(layer) {
                  scope.map.removeLayer(layer);
                });

                scope.map.addLayer(gnMap.getLayersFromConfig());

                // Add each WMS layer to the map
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
                });
              };

              var init = function() {

                if (scope.mode === 'thumbnailMaker') {
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

                  loadLayers();

                  scope.$watch('gnCurrentEdit.layerConfig', loadLayers);
                }
              };

              // TODO: should be in gnEditor ?
              var getVersion = function() {
                scope.metadataId = gnCurrentEdit.id;
                return scope.params.version = gnCurrentEdit.version;
              };

              var resetForm = function() {
                if (scope.params) {
                  scope.params.url = '';
                  scope.params.thumbnail_url = '';
                  scope.params.thumbnail_desc = '';
                }
                scope.clear(scope.queue);
              };

              /**
               * Onlinesrc uploaded with success, close the popup,
               * refresh the metadata form.
               * Callback of the submit().
               */
              var uploadThumbnailDone = function(evt, data) {
                resetForm();
                gnEditor.refreshEditorForm();
                gnOnlinesrc.reload = true;
                $(scope.popupid).modal('hide');
              };

              /**
               * Onlinesrc uploaded with error, broadcast it.
               */
              var uploadThumbnailError = function(data) {
              };

              // upload directive options
              scope.thumbnailUploadOptions = {
                autoUpload: false,
                url: 'md.thumbnail.upload',
                maxNumberOfFiles: 1,
                dropZone: $('#gn-upload-thumbnail'),
                //acceptFileTypes: /(\.|\/)(gif|jpe?g|png|tif?f)$/i,
                done: uploadThumbnailDone,
                fail: uploadThumbnailError
              };

              /**
               *  Add thumbnail
               *  If it is an upload, then we submit the form with right content
               *  If it is an URL, we just call a $http.get
               */
              scope.addThumbnail = function() {
                if (scope.mode == 'upload') {
                  getVersion();
                  return gnEditor.save(false, true)
                  .then(function(data) {
                        scope.submit();
                      });
                } else if (scope.mode == 'thumbnailMaker') {
                  getVersion();
                  var deferred = $q.defer();

                  gnEditor.save(false, true)
                    .then(function(data) {
                        scope.action =
                            'md.thumbnail.generate?_content_type=json&';
                        $http.post(scope.action,
                            $('#gn-upload-thumbnail').serialize(), {
                              headers: {'Content-Type':
                                    'application/x-www-form-urlencoded'}
                            }).success(function(data) {
                          uploadThumbnailDone();
                          deferred.resolve(data);
                        }).error(function(data, status, headers, config) {
                          $rootScope.$broadcast('StatusUpdated', {
                            title: $translate('thumbnailCreationError'),
                            // Hack to extract error message
                            // from HTML page. At some point
                            // the service should return JSON error
                            error: {
                              message: $(data).find('td[align=center]').text()
                            },
                            timeout: 0,
                            type: 'danger'});
                          deferred.resolve(data);
                        });
                      });
                  return deferred.promise;
                } else {
                  return gnOnlinesrc.addThumbnailByURL(scope.params,
                      scope.popupid).then(function() {
                    resetForm();
                  });
                }
              };

              scope.$watch('mode', init);

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
   * The `gnAddOnlinesrc` directive provides a form to add a new online resource
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
        'gnEditor',
        'gnCurrentEdit',
        function(gnOnlinesrc, gnOwsCapabilities, gnEditor, gnCurrentEdit) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addOnlinesrc.html',
            link: function(scope, element, attrs) {

              scope.popupid = attrs['gnPopupid'];

              gnOnlinesrc.register('onlinesrc', function() {
                scope.metadataId = gnCurrentEdit.id;
                scope.schema = gnCurrentEdit.schema;

                if (angular.isUndefined(scope.isMdMultilingual) &&
                    gnCurrentEdit.mdOtherLanguages) {

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
                  }
                  else {
                    scope.isMdMultilingual = false;
                  }
                }

                $(scope.popupid).modal('show');

              });

              // mode can be 'url' or 'upload'
              scope.mode = 'url';

              // the form parms that will be submited
              scope.params = {};

              // Tells if we need to display layer grid and send
              // layers to the submit
              scope.isWMSProtocol = false;

              scope.onlinesrcService = gnOnlinesrc;

              var resetForm = function() {
                if (scope.params) {
                  scope.params.desc = scope.isMdMultilingual ? {} : '';
                  scope.params.url = '';
                  scope.params.name = scope.isMdMultilingual ? {} : '';
                  scope.params.protocol = '';
                }
                scope.clear(scope.queue);
              };

              /**
               * Onlinesrc uploaded with success, close the popup,
               * refresh the metadata.
               */
              var uploadOnlinesrcDone = function(data) {
                resetForm();
                gnEditor.refreshEditorForm();
                gnOnlinesrc.reload = true;
                $(scope.popupid).modal('hide');
              };

              /**
               * Onlinesrc uploaded with error, broadcast it.
               */
              var uploadOnlineSrcError = function(data) {
              };

              scope.onlinesrcUploadOptions = {
                autoUpload: false,
                url: 'resource.upload.and.link',
                dropZone: $('#gn-upload-onlinesrc'),
                // TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
                done: uploadOnlinesrcDone,
                fail: uploadOnlineSrcError
              };

              /**
               *  Add online resource
               *  If it is an upload, then we submit the form with right content
               *  If it is an URL, we just call a $http.get
               */
              scope.addOnlinesrc = function() {
                if (scope.mode == 'upload') {
                  return scope.submit();
                } else {
                  if (angular.isObject(scope.params.name)) {
                    var name = [];
                    for (var p in scope.params.name) {
                      name.push(p + '#' + scope.params.name[p]);
                    }
                    scope.params.name = name.join('|');
                  }
                  if (angular.isObject(scope.params.desc)) {
                    var desc = [];
                    for (var p in scope.params.desc) {
                      desc.push(p + '#' + scope.params.desc[p]);
                    }
                    scope.params.desc = desc.join('|');
                  }

                  return gnOnlinesrc.addOnlinesrc(scope.params, scope.popupid).
                      then(function() {
                        resetForm();
                      });
                }
              };

              scope.onAddSuccess = function() {
                gnEditor.refreshEditorForm();
                scope.onlinesrcService.reload = true;
              };

              /**
               * loadWMSCapabilities
               *
               * Call WMS capabilities request with params.url.
               * Update params.layers scope value, that will be also
               * passed to the layers grid directive.
               */
              scope.loadWMSCapabilities = function() {
                if (scope.isWMSProtocol) {
                  gnOwsCapabilities.getWMSCapabilities(scope.params.url)
                  .then(function(capabilities) {
                        scope.layers = [];
                        angular.forEach(capabilities.layers, function(l) {
                          if (angular.isDefined(l.Name)) {
                            scope.layers.push(l);
                          }
                        });
                      });
                }
              };

              /**
               * On protocol combo Change.
               * Update isWMSProtocol values to display or hide
               * layer grid and call or not a getCapabilities.
               */
              scope.$watch('params.protocol', function() {
                if (!angular.isUndefined(scope.params.protocol)) {
                  scope.isWMSProtocol = (scope.params.protocol.
                      indexOf('OGC:WMS') >= 0);
                  scope.loadWMSCapabilities();
                }
              });

              /**
               * On URL change, reload WMS capabilities
               * if the protocol is WMS
               */
              scope.$watch('params.url', function() {
                if (!angular.isUndefined(scope.params.url)) {
                  scope.loadWMSCapabilities();
                }
              });

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

                  gnOnlinesrc.register(scope.mode, function() {
                    $(scope.popupid).modal('show');

                    // parameters of the online resource form
                    scope.srcParams = {selectedLayers: []};

                    var searchParams = {
                      type: scope.mode
                    };
                    scope.$broadcast('resetSearch', searchParams);
                    scope.layers = [];
                  });

                  // This object is used to share value between this
                  // directive and the SearchFormController scope that
                  // is contained by the directive
                  scope.stateObj = {};

                  /**
                   * loadWMSCapabilities
                   *
                   * Call WMS capabilities on the service metadata URL.
                   * Update params.layers scope value, that will be also
                   * passed to the layers grid directive.
                   */
                  scope.loadWMSCapabilities = function(url) {
                    scope.alertMsg = null;
                    gnOwsCapabilities.getWMSCapabilities(url)
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
                    if (!angular.isUndefined(scope.stateObj.selectRecords) &&
                        scope.stateObj.selectRecords.length > 0) {
                      var md = new Metadata(scope.stateObj.selectRecords[0]);
                      var links = [];

                      scope.srcParams.selectedLayers = [];
                      if (scope.mode == 'service') {
                        links = links.concat(md.getLinksByType('OGC:WMS'));
                        links = links.concat(md.getLinksByType('wms'));
                        scope.srcParams.uuidSrv = md.getUuid();
                        scope.srcParams.uuidDS = gnCurrentEdit.uuid;

                        if (angular.isArray(links) && links.length == 1) {
                          scope.loadWMSCapabilities(links[0].url);
                          scope.srcParams.url = links[0].url;
                        } else {
                          scope.srcParams.url = '';
                          scope.alertMsg =
                              $translate('linkToServiceWithoutURLError');
                        }
                      }
                      else {
                        // TODO: Check the appropriate WMS service
                        // or list URLs if many
                        links = links.concat(
                            gnCurrentEdit.metadata.getLinksByType('OGC:WMS'));
                        links = links.concat(
                            gnCurrentEdit.metadata.getLinksByType('wms'));
                        var serviceUrl = links[0].url;
                        scope.loadWMSCapabilities(serviceUrl);
                        scope.srcParams.url = serviceUrl;
                        scope.srcParams.uuidDS = md.getUuid();
                        scope.srcParams.uuidSrv = gnCurrentEdit.uuid;
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
   * On submit, the metadata is saved, the thumbnail is added,
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
                    params: {}
                  };
                  scope.modelOptions =
                      angular.copy(gnGlobalSettings.modelOptions);
                },
                post: function postLink(scope, iElement, iAttrs) {
                  scope.mode = iAttrs['gnLinkToMetadata'];
                  scope.popupid = '#linkto' + scope.mode + '-popup';
                  scope.btn = {};

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
                        icon: 'fa-table',
                        label: $translate('linkToFeatureCatalog')
                      };
                    }
                    else if (scope.mode == 'parent') {
                      searchParams = {
                        hitsPerPage: 10
                      };
                      scope.btn = {
                        icon: 'fa-sitemap',
                        label: $translate('linkToParent')
                      };
                    }
                    else if (scope.mode == 'source') {
                      searchParams = {
                        hitsPerPage: 10
                      };
                      scope.btn = {
                        icon: 'fa-sitemap fa-rotate-180',
                        label: $translate('linkToSource')
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
   * current metdata. The user need to specify Association type and
   * Initiative type
   * to be able to add a metadata to his selection. The process alow a multiple
   * selection.
   *
   * On submit, the metadata is saved, the resource is associated, then the form
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
                    params: {}
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

                  /**
                   * Search a metada record into the selection.
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
                      initiativeType: scope.initiativeType,
                      associationType: scope.associationType,
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
