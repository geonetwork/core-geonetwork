(function() {
  goog.provide('gn_harvest_controller');





  goog.require('gn_category');
  goog.require('gn_harvester');
  goog.require('gn_importxsl');

  var module = angular.module('gn_harvest_controller',
      ['gn_harvester', 'gn_category', 'gn_importxsl']);


  /**
   *
   */
  module.controller('GnHarvestController', [
    '$scope', '$http', '$translate', '$injector', '$rootScope',
    'gnSearchManagerService', 'gnUtilityService',
    function($scope, $http, $translate, $injector, $rootScope,
             gnSearchManagerService, gnUtilityService) {

      $scope.pageMenu = {
        folder: 'harvest/',
        defaultTab: 'harvest',
        tabs: []
      };
      $scope.harvesterTypes = {};
      $scope.harvesters = {};
      $scope.harvesterSelected = null;
      $scope.harvesterUpdated = false;
      $scope.harvesterNew = false;
      $scope.harvesterHistory = {};


      var unbindStatusListener = null;


      function loadHarvesters() {
        return $http.get('admin.harvester.list@json').success(function(data) {
          if (data != 'null') {
            $scope.harvesters = data;
            gnUtilityService.parseBoolean($scope.harvesters);
          }
        }).error(function(data) {
          // TODO
        });
      }


      function loadHistory() {
        $scope.harvesterHistory = undefined;
        $http.get('admin.harvester.history@json?uuid=' +
            $scope.harvesterSelected.site.uuid).success(function(data) {
          $scope.harvesterHistory = data.harvesthistory;
        }).error(function(data) {
          // TODO
        });
      }

      function loadHarvesterTypes() {
        $http.get('admin.harvester.info@json?type=harvesterTypes',
            {cache: true})
          .success(function(data) {
              angular.forEach(data[0], function(value) {
                $scope.harvesterTypes[value] = {
                  label: value,
                  text: $translate('harvester-' + value)
                };
                $.getScript('../../catalog/templates/admin/harvest/type/' +
                    value + '.js')
                .done(function(script, textStatus) {
                      $scope.$apply(function() {
                        $scope.harvesterTypes[value].loaded = true;
                      });
                      // FIXME: could we make those harvester specific
                      // function a controller
                    })
                .fail(function(jqxhr, settings, exception) {
                      $scope.harvesterTypes[value].loaded = false;
                    });
              });
            }).error(function(data) {
              // TODO
            });
      }

      $scope.getTplForHarvester = function() {
        // TODO : return view by calling harvester ?
        if ($scope.harvesterSelected) {
          return '../../catalog/templates/admin/' + $scope.pageMenu.folder +
              'type/' + $scope.harvesterSelected['@type'] + '.html';
        } else {
          return null;
        }
      };
      $scope.updatingHarvester = function() {
        $scope.harvesterUpdated = true;
      };
      $scope.addHarvester = function(type) {
        $scope.harvesterNew = true;
        $scope.harvesterHistory = {};
        $scope.harvesterSelected = window['gnHarvester' + type].createNew();
      };

      $scope.cloneHarvester = function(id) {
        $http.get('admin.harvester.clone@json?id=' +
            id)
          .success(function(data) {
              loadHarvesters().then(function() {
                // Select the clone
                angular.forEach($scope.harvesters, function(h) {
                  if (h['@id'] === data[0]) {
                    $scope.selectHarvester(h);
                  }
                });
              });
            }).error(function(data) {
              // TODO
            });
      };

      $scope.buildResponseGroup = function(h) {
        var groups = '';
        angular.forEach(h.privileges, function(p) {
          var ops = '';
          angular.forEach(p.operation, function(o) {
            ops += '<operation name="' + o['@name'] + '"/>';
          });
          groups +=
              '<group id="' + p['@id'] + '">' + ops + '</group>';
        });
        return '<privileges>' + groups + '</privileges>';
      };
      $scope.buildResponseCategory = function(h) {
        var cats = '';
        angular.forEach(h.categories, function(c) {
          cats +=
              '<category id="' + c['@id'] + '"/>';
        });
        return '<categories>' + cats + '</categories>';
      };


      $scope.saveHarvester = function() {
        var body = window['gnHarvester' + $scope.harvesterSelected['@type']]
          .buildResponse($scope.harvesterSelected, $scope);

        $http.post('admin.harvester.' +
            ($scope.harvesterNew ? 'add' : 'update') +
            '@json', body, {
              headers: {'Content-type': 'application/xml'}
            }).success(function(data) {
          loadHarvesters();
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate('harvesterUpdated'),
            timeout: 2,
            type: 'success'});
        }).error(function(data) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate('harvesterUpdated'),
            error: data,
            timeout: 2,
            type: 'danger'});
        });
      };
      $scope.selectHarvester = function(h) {

        // TODO: Specific to thredds
        if (h['@type'] === 'thredds') {

          $scope.threddsCollectionsMode =
              h.options.outputSchemaOnAtomicsDIF !== '' ? 'DIF' : 'UNIDATA';
          $scope.threddsAtomicsMode =
              h.options.outputSchemaOnCollectionsDIF !== '' ? 'DIF' : 'UNIDATA';


        }
        $scope.harvesterSelected = h;
        $scope.harvesterUpdated = false;
        $scope.harvesterNew = false;
        $scope.harvesterHistory = {};
        $scope.searchResults = null;

        loadHistory();

        // Retrieve records in that harvester
        $scope.harvesterRecordsFilter = {
          template: 'y or s or n',
          siteId: $scope.harvesterSelected.site.uuid,
          sortBy: 'title'
        };
        $scope.$broadcast('resetSearch', $scope.harvesterRecordsFilter);
      };

      $scope.refreshHarvester = function() {
        loadHarvesters();
      };
      $scope.deleteHarvester = function() {
        $http.get('admin.harvester.remove@json?id=' +
            $scope.harvesterSelected['@id'])
          .success(function(data) {
              $scope.harvesterSelected = {};
              $scope.harvesterUpdated = false;
              $scope.harvesterNew = false;
              loadHarvesters();
            }).error(function(data) {
              console.log(data);
            });
      };

      $scope.deleteHarvesterRecord = function() {
        $http.get('admin.harvester.clear@json?id=' +
            $scope.harvesterSelected['@id'])
          .success(function(data) {
              $scope.harvesterSelected = {};
              $scope.harvesterUpdated = false;
              $scope.harvesterNew = false;
              loadHarvesters();
            }).error(function(data) {
              console.log(data);
            });
      };
      $scope.deleteHarvesterHistory = function() {
        var ids = [];
        angular.forEach($scope.harvesterHistory, function(h) {
          ids.push(h.id);
        });
        $http.get('admin.harvester.history.delete@json?id=' + ids.join('&id='))
          .success(function(data) {
              loadHarvesters().then(function() {
                $scope.selectHarvester($scope.harvesterSelected);
              });
            });
      };
      $scope.runHarvester = function() {
        $http.get('admin.harvester.run@json?id=' +
            $scope.harvesterSelected['@id'])
          .success(function(data) {
              loadHarvesters();
            });
      };

      $scope.setHarvesterSchedule = function() {
        if (!$scope.harvesterSelected) {
          return;
        }
        var status = $scope.harvesterSelected.options.status;
        $http.get('admin.harvester.' +
            (status === 'active' ? 'start' : 'stop') +
            '@json?id=' +
            $scope.harvesterSelected['@id'])
          .success(function(data) {

            }).error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('harvesterSchedule' + status),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      // Register status listener
      unbindStatusListener = $scope.$watch('harvesterSelected.options.status',
          function() {
            $scope.setHarvesterSchedule();
          });

      loadHarvesters();
      loadHarvesterTypes();


      // ---------------------------------------
      // Those function are harvester dependant and
      // should move in the harvester code
      // TODO
      $scope.geonetworkGetSources = function(url) {
        $http.get($scope.proxyUrl +
            encodeURIComponent(url + '/srv/eng/info?type=sources'))
          .success(function(data) {
              $scope.geonetworkSources = [];
              var i = 0;
              var xmlDoc = $.parseXML(data);
              var $xml = $(xmlDoc);
              $sources = $xml.find('uuid');
              $names = $xml.find('name');

              angular.forEach($sources, function(s) {
                // FIXME: probably some issue on IE ?
                $scope.geonetworkSources.push({
                  uuid: s.textContent,
                  name: $names[i].textContent
                });
                i++;
              });
            }).error(function(data) {
              // TODO
            });
      };

      // TODO: Should move to OAIPMH
      $scope.oaipmhSets = null;
      $scope.oaipmhPrefix = null;
      $scope.oaipmhInfo = null;
      $scope.oaipmhGet = function() {
        $scope.oaipmhInfo = null;
        var body = '<request><type url="' +
            $scope.harvesterSelected.site.url +
            '">oaiPmhServer</type></request>';
        $http.post('admin.harvester.info@json', body, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
          if (data[0].sets && data[0].formats) {
            $scope.oaipmhSets = data[0].sets;
            $scope.oaipmhPrefix = data[0].formats;
          } else {
            $scope.oaipmhInfo = $translate('oaipmh-FailedToGetSetsAndPrefix');
          }
        }).error(function(data) {
        });
      };

      // TODO : enable watch only if OAIPMH
      $scope.$watch('harvesterSelected.site.url', function() {
        if ($scope.harvesterSelected &&
            $scope.harvesterSelected['@type'] === 'oaipmh') {
          $scope.oaipmhGet();
        }
      });

      // TODO: Should move to a CSW controller
      $scope.cswCriteria = [];
      $scope.cswCriteriaInfo = null;

      /**
       * Retrieve GetCapabilities document to retrieve
       * the list of possible search fields declared
       * in *Queryables.
       *
       * If the service is unavailable for a while and a user
       * go to the admin page, it may loose its filter.
       */
      $scope.cswGetCapabilities = function() {
        $scope.cswCriteriaInfo = null;

        if ($scope.harvesterSelected &&
            $scope.harvesterSelected.site &&
            $scope.harvesterSelected.site.capabilitiesUrl) {


          var url = $scope.harvesterSelected.site.capabilitiesUrl;

          // Add GetCapabilities if not already in URL
          // Parameter value is case sensitive.
          // Append a ? if not already in there and if not &
          if (url.indexOf('GetCapabilities') === -1) {
            url += (url.indexOf('?') === -1 ? '?' : '&') +
                'SERVICE=CSW&REQUEST=GetCapabilities&VERSION=2.0.2';
          }

          $http.get($scope.proxyUrl +
              encodeURIComponent(url))
            .success(function(data) {
                $scope.cswCriteria = [];

                var i = 0;
                try {
                  var xmlDoc = $.parseXML(data);

                  // Create properties in model if no criteria defined
                  if (!$scope.harvesterSelected.searches) {
                    $scope.harvesterSelected.searches = [{}];
                  }

                  var $xml = $(xmlDoc);
                  var matches = ['SupportedISOQueryables',
                    'SupportedQueryables',
                    'AdditionalQueryables'];
                  var parseCriteriaFn = function() {
                    var name = $(this).text();
                    $scope.cswCriteria.push(name);
                    if (!$scope.harvesterSelected.searches[0][name]) {
                      $scope.harvesterSelected.searches[0][name] =
                          {value: ''};
                    }
                  };
                  var parseQueryablesFn = function() {
                    if (matches.indexOf($(this).attr('name')) !== -1) {

                      // Add all queryables to the list of possible parameters
                      // and to the current harvester if not exist.
                      // When harvester is saved only criteria with
                      // value will be saved.
                      $(this).find('Value').each(parseCriteriaFn);
                      $(this).find('ows\\:Value').each(parseCriteriaFn);
                    }
                  };
                  // For Chrome and IE
                  $xml.find('Constraint').each(parseQueryablesFn);
                  // For FF, namespace parsing is different
                  if ($scope.cswCriteria.length === 0) {
                    $xml.find('ows\\:Constraint').each(parseQueryablesFn);
                  }

                  $scope.cswCriteria.sort();

                } catch (e) {
                  $scope.cswCriteriaInfo =
                      $translate('csw-FailedToParseCapabilities');
                }

              }).error(function(data) {
                // TODO
              });
        }
      };

      $scope.$watch('harvesterSelected.site.capabilitiesUrl', function() {
        $scope.cswGetCapabilities();
      });




      // WFS GetFeature harvester
      $scope.harvesterTemplates = null;
      var loadHarvesterTemplates = function() {
        $http.get('info@json?type=templates')
          .success(function(data) {
              $scope.harvesterTemplates = data.templates;
            });
      };


      $scope.harvesterGetFeatureXSLT = null;
      var wfsGetFeatureXSLT = function() {
        $scope.oaipmhInfo = null;
        var body = '<request><type>wfsFragmentStylesheets</type><schema>' +
            $scope.harvesterSelected.options.outputSchema +
            '</schema></request>';
        $http.post('admin.harvester.info@json', body, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
          $scope.harvesterGetFeatureXSLT = data[0];
        });
      };


      // When schema change reload the available XSLTs and templates
      $scope.$watch('harvesterSelected.options.outputSchema', function() {
        if ($scope.harvesterSelected &&
            $scope.harvesterSelected['@type'] === 'wfsfeatures') {
          wfsGetFeatureXSLT();
          loadHarvesterTemplates();
        }
      });


      // Z3950 GetFeature harvester
      $scope.harvesterZ3950repositories = null;
      var loadHarvesterZ3950Repositories = function() {
        $http.get('info@json?type=z3950repositories', {cache: true})
          .success(function(data) {
              $scope.harvesterZ3950repositories = data.z3950repositories;
            });
      };
      $scope.$watch('harvesterSelected.site.repositories',
          function() {
            if ($scope.harvesterSelected &&
                $scope.harvesterSelected['@type'] === 'z3950') {
              loadHarvesterZ3950Repositories();
            }
          });



      // Thredds
      $scope.threddsCollectionsMode = 'DIF';
      $scope.threddsAtomicsMode = 'DIF';
      $scope.harvesterThreddsXSLT = null;
      var threddsGetXSLT = function() {
        $scope.oaipmhInfo = null;
        var opt = $scope.harvesterSelected.options;
        var schema = ($scope.threddsCollectionsMode === 'DIF' ?
            opt.outputSchemaOnCollectionsDIF :
            opt.outputSchemaOnCollectionsFragments);
        var body = '<request><type>threddsFragmentStylesheets</type><schema>' +
            schema +
            '</schema></request>';
        $http.post('admin.harvester.info@json', body, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
          $scope.harvesterThreddsXSLT = data[0];
        });
      };
      $scope.$watch('harvesterSelected.options.outputSchemaOnCollectionsDIF',
          function() {
            if ($scope.harvesterSelected &&
                $scope.harvesterSelected['@type'] === 'thredds') {
              threddsGetXSLT();
              loadHarvesterTemplates();
            }
          });
      $scope.$watch(
          'harvesterSelected.options.outputSchemaOnCollectionsFragments',
          function() {
            if ($scope.harvesterSelected &&
                $scope.harvesterSelected['@type'] === 'thredds') {
              threddsGetXSLT();
              loadHarvesterTemplates();
            }
          });
      $scope.getHarvesterTypes = function() {
        var array = [];
        angular.forEach($scope.harvesterTypes, function(h) {
          array.push(h);
        });
        return array;
      };
    }]);

})();
