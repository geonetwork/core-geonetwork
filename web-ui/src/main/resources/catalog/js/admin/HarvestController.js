(function() {
  goog.provide('gn_harvest_controller');





  goog.require('gn_category');
  goog.require('gn_harvester');
  goog.require('gn_importxsl');

  var module = angular.module('gn_harvest_controller',
      ['gn_harvester', 'gn_category']);


  /**
   *
   */
  module.controller('GnHarvestController', [
    '$scope', '$http', '$translate', '$injector', '$rootScope',
    'gnSearchManagerService',
    function($scope, $http, $translate, $injector, $rootScope,
            gnSearchManagerService) {

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
      $scope.harvesterRecordsPagination = {
        pages: -1,
        currentPage: 0,
        hitsPerPage: 10
      };
      // List of metadata records attached to the selected user
      $scope.harvesterRecords = null;
      $scope.harvesterRecordsFilter = null;

      // Register the search results, filter and pager
      // and get the search function back
      $scope.harvesterRecordsSearch = gnSearchManagerService.register({
        records: 'harvesterRecords',
        filter: 'harvesterRecordsFilter',
        pager: 'harvesterRecordsPagination'
      }, $scope);

      // When the current page change trigger the search
      $scope.$watch('harvesterRecordsPagination.currentPage', function() {
        $scope.harvesterRecordsSearch();
      });
      function parseBoolean(object) {
        angular.forEach(object, function(value, key) {
          if (typeof value == 'string') {
            if (value == 'true' || value == 'false') {
              object[key] = (value == 'true');
            }
          } else {
            parseBoolean(value);
          }
        });
      }

      function loadHarvesters() {
        return $http.get('admin.harvester.list@json').success(function(data) {
          $scope.harvesters = data;
          parseBoolean($scope.harvesters);
        }).error(function(data) {
          // TODO
        });
      }


      function loadHistory() {
        $scope.harvesterHistory = undefined;
        $http.get('admin.harvester.history@json?uuid=' +
                $scope.harvesterSelected.site.uuid).success(function(data) {
          $scope.harvesterHistory = data.response;
        }).error(function(data) {
          // TODO
        });
      }

      function loadHarvesterTypes() {
        $http.get('admin.harvester.info@json?type=harvesterTypes')
        .success(function(data) {
              angular.forEach(data[0], function(value) {
                $scope.harvesterTypes[value] = {label: $translate(value)};
                $.getScript('../../catalog/templates/admin/harvest/type/' +
                    value + '.js')
                    .done(function(script, textStatus) {
                      $scope.harvesterTypes[value].loaded = true;
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
          console.log(data);
        });
      };
      $scope.selectHarvester = function(h) {
        $scope.harvesterSelected = h;
        $scope.harvesterUpdated = false;
        $scope.harvesterNew = false;

        loadHistory();

        // Retrieve records in that harvester
        $scope.harvesterRecordsFilter = {
          template: 'y or s or n',
          siteId: $scope.harvesterSelected.site.uuid,
          sortBy: 'title'
        };
        $scope.harvesterRecordsSearch();
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
            encodeURIComponent(url + '/srv/eng/xml.info?type=sources'))
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

    }]);

})();
