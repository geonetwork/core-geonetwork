(function() {
  goog.provide('gn_csw_virtual_controller');


  var module = angular.module('gn_csw_virtual_controller',
      []);


  /**
   * GnCSWVirtualController provides management interface
   * for virtual CSW configuration.
   *
   */
  module.controller('GnCSWVirtualController', [
    '$scope', '$http', '$rootScope', '$translate', '$timeout',
    function($scope, $http, $rootScope, $translate, $timeout) {

      /**
       * CSW virtual
       */
      $scope.cswVirtual = null;
      $scope.virtualCSWSelected = null;
      $scope.virtualCSWUpdated = false;
      $scope.virtualCSWSearch = '';
      $scope.groupsFilter = {};
      $scope.sourcesFilter = {};
      $scope.categoriesFilter = {};
      $scope.newFilterField = null;
      $scope.newFilterValue = null;
      $scope.occurs = ['+',' ','-'];
      $scope.showExplicitQuery = false;
      $scope.filterHelper = ['any', 'title', 'abstract', 'keyword',
        'denominator', '_source', '_cat', '_groupPublished'];

      var operation = '';

      /**
       * Load catalog settings and extract CSW settings
       */
      function loadCSWVirtual() {
        $scope.virtualCSWSelected = {};
        $http.get('admin.config.virtualcsw.list?_content_type=json').
            success(function(data) {
              $scope.cswVirtual = data != 'null' ? data.record : [];
            }).error(function(data) {
              // TODO
            });

        // TODO : load categories and sources
        // to display combo in edit form
      }


      function loadFilterList() {
        $http.get('admin.group.list?_content_type=json').
            success(function(data) {
              $scope.groupsFilter = data;
            }).error(function(data) {
            });
      }
      function loadCategories() {
        $http.get('info?_content_type=json&type=categories').
            success(function(data) {
              $scope.categories = data.metadatacategory;
            }).error(function(data) {
              // TODO
            });
      }

      $scope.selectVirtualCSW = function(v) {
        operation = 'updateservice';
        $http.get('admin.config.virtualcsw.get?' +
            '_content_type=json&id=' + v.id)
          .success(function(data) {
              var params = [], formParams = ['abstract', 'title',
                '_source', '_cat', 'any', '_groupPublished', 'keyword',
                'denominator', 'type'];
              angular.copy(data.parameter, params);
              $scope.virtualCSWSelected = data;
              $scope.virtualCSWSelected.serviceParameters = {};
              $scope.showExplicitQuery = $scope.virtualCSWSelected.explicitQuery ? true : false;
              angular.forEach(params,
                  function(param) {
                    $scope.virtualCSWSelected.
                        serviceParameters[param.name] = {value: param.value, occur: param.occur};
                  });
              $scope.virtualCSWUpdated = false;

              $timeout(function() {
                $('#servicename').focus();
              }, 100);
            }).error(function(data) {
              // TODO
            });
      };

      $scope.addFilter = function() {
        $scope.virtualCSWSelected.serviceParameters[$scope.newFilterField] =
            $scope.newFilterValue;
        $scope.newFilterValue = $scope.newFilterField = null;
      };
      $scope.removeFilter = function(f) {
        delete $scope.virtualCSWSelected.serviceParameters[f];
      };
      $scope.setFilter = function(f) {
        $scope.newFilterField = f;
      };
      $scope.setFilterValue = function(field, value) {
        $scope.virtualCSWSelected.serviceParameters[field] =
            value;
      };

      $scope.$watchCollection('virtualCSWSelected', function() {
        $scope.virtualCSWUpdated = true;
      });

      $scope.addVirtualCSW = function() {
        operation = 'newservice';
        $scope.virtualCSWSelected = {
          'id': '',
          'name': 'csw-servicename',
          'description': '',
          'explicitQuery': '',
          'serviceParameters': {}
        };
        $timeout(function() {
          $('#servicename').focus();
        }, 100);
      };
      $scope.saveVirtualCSW = function(formId) {

        $http.get('admin.config.virtualcsw.update?' +
            '_content_type=json&operation=' + operation +
            '&' + $(formId).serialize())
          .success(function(data) {
              loadCSWVirtual();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('virtualCswUpdated'),
                timeout: 2,
                type: 'success'});
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('virtualCswUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.deleteVirtualCSW = function() {
        $http.get('admin.config.virtualcsw.remove?id=' +
            $scope.virtualCSWSelected.id)
          .success(function(data) {
              loadCSWVirtual();
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('virtualCswDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.sortByLabel = function(group) {
        return group.label[$scope.lang];
      };
      $scope.getCapabilitiesUrl = function(v) {
        if (v) {
          return v.name + '?SERVICE=CSW&REQUEST=GetCapabilities';
        } else {
          return null;
        }
      };

      loadCSWVirtual();
      loadFilterList();
      loadCategories();

    }]);

})();
