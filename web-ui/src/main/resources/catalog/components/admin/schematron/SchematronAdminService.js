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
  'use strict';
  goog.provide('gn_schematronadminservice');

  var module = angular.module('gn_schematronadminservice', []);


  /**
     * Display harvester identification section with
     * name, group and icon
     */
  module.service('gnSchematronAdminService', ['$http', '$cacheFactory', '$q',
    function($http, $cacheFactory, $q) {
      var TIMEOUT, cache, getDataFromCache, putDataIntoCache,
          removeElementFromCache, groupCacheId, updateCacheOnGroupChange,
          updateCacheOnCriteriaChange, sortByDisplayPriority,
          findIndex, criteriaComparator, groupComparator;
      TIMEOUT = 60000; // refresh cache every minute
      cache = $cacheFactory('gnSchematronAdminService');
      getDataFromCache = function(id, timeout) {
        var lastUsed, now;
        if (!timeout) {
          timeout = TIMEOUT;
        }
        lastUsed = cache.get('lastUsed_' + id);
        now = new Date();
        if (!lastUsed || (now - lastUsed) > timeout) {
          return undefined;
        }
        return cache.get(id);
      };
      putDataIntoCache = function(id, data) {
        var now = new Date().getTime();
        cache.put('lastUsed_' + id, now);
        cache.put(id, data);
      };
      removeElementFromCache = function(id) {
        cache.remove(id);
        cache.remove('lastUsed_' + id);
      };
      groupCacheId = function(schematronId) {
        return 'criteriaGroup_' + schematronId;
      };

      updateCacheOnGroupChange = function(schematronId) {
        removeElementFromCache('criteriaTypes');
        removeElementFromCache(groupCacheId(schematronId));
      };
      updateCacheOnCriteriaChange = function(schematronId) {
        removeElementFromCache(groupCacheId(schematronId));
      };
      sortByDisplayPriority = function(a, b) {
        return a.displaypriority - b.displaypriority;
      };
      findIndex = function(array, object, comparator) {
        var i, arrayObj;
        for (i = 0; i < array.length; i++) {
          arrayObj = array[i];
          if (comparator(arrayObj, object)) {
                        return i;
          }
        }
        return -1;
      };
      criteriaComparator = function(o1, o2) {return o1.id === o2.id; };
      groupComparator = function(o1, o2) {
        return o1.id.schematronid === o2.id.schematronid &&
            o1.id.name === o2.id.name;
      };

      this.criteria = {
        remove: function(criteria, group) {
          $http({
            method: 'GET',
            url: 'admin.schematroncriteria.delete?_content_type=json',
            params: {
              id: criteria.id
            }
          }).success(function() {
            var list, idx;
            updateCacheOnCriteriaChange(group.id.schematronid, group.id.name);
            list = group.criteria;
            idx = findIndex(list, criteria, criteriaComparator);
            if (idx !== -1) {
              list.splice(idx, 1);
            }
          }).error(function() {
            alert('Error deleting criteria: ' + criteria.id);
          });
        },
        update: function(updated, original, group) {
          $http({
            method: 'POST',
            url: 'admin.schematroncriteria.update?_content_type=',
            params: {
              id: original.id,
              type: updated.type,
              value: updated.value,
              uitype: updated.uitype,
              uivalue: updated.uivalue
            }
          }).success(function() {
            updateCacheOnCriteriaChange(group.id.schematronid, group.id.name);

            angular.copy(updated, original);
          }).error(function() {
            alert('Error updating criteria: ' + original.id);
          });
        },
        add: function(criteria, original, group) {
          $http({
            method: 'POST',
            url: 'admin.schematroncriteria.add?_content_type=json',
            params: {
              type: criteria.type,
              value: criteria.value,
              uitype: criteria.uitype,
              uivalue: criteria.uivalue,
              groupName: group.id.name,
              schematronId: group.id.schematronid
            }
          }).success(function(response) {
            updateCacheOnCriteriaChange(group.id.schematronid, group.id.name);
            var added = angular.copy(criteria);
            added.id = response.id;
            if (!group.criteria) {
              group.criteria = [];
            }
            group.criteria.push(added);
            angular.copy(original, criteria);
          }).error(function() {
            alert('Error adding criteria: {\n\ttype:' + criteria.type +
                ',\n\tvalue:' + criteria.value +
                ',\n\tgroupName:' + group.id.name + 'schematronId: ' +
                group.id.schematronid);
          });
        }
      };
      this.group = {
        remove: function(group, groupList, successCallback) {
          $http({
            method: 'GET',
            url: 'admin.schematroncriteriagroup.delete?_content_type=json',
            params: {
              groupName: group.id.name,
              schematronId: group.id.schematronid
            }
          }).success(function() {
            updateCacheOnGroupChange(group.id.schematronid);
            var idx = findIndex(groupList, group, groupComparator);
            if (idx !== -1) {
              groupList.splice(idx, 1);
            }
            successCallback();
          }).error(function() {
            alert('Error deleting Schematron Criteria Group: ' + group.id);
          });
        },
        update: function(updated, original) {
          var params = {
            groupName: original.id.name,
            schematronId: original.id.schematronid,
            requirement: updated.requirement
          };
          if (updated.id.name !== original.id.name) {
            params.newGroupName = updated.id.name;
          }
          if (updated.id.schematronid !== original.id.schematronid) {
            params.newSchematronid = updated.id.schematronid;
          }
          $http({
            method: 'GET',
            url: 'admin.schematroncriteriagroup.update?_content_type=json',
            params: params
          }).success(function() {
            original.id.name = updated.id.name;
            original.id.schematronId = updated.id.schematronid;
            original.requirement = updated.requirement;
          }).error(function() {
            alert('Error editing Schematron Criteria Group: ' + original.id);
          });
        },
        add: function(group, groupList, successCallback) {
          $http({
            method: 'GET',
            url: 'admin.schematroncriteriagroup.add?_content_type=json',
            params: {
              groupName: group.id.name,
              schematronId: group.id.schematronid,
              requirement: group.requirement
            }
          }).success(function() {
            updateCacheOnGroupChange(group.id.schematronid);
            groupList.push(group);
            successCallback(group);
          }).error(function() {
            alert('Error adding new Schematron Criteria Group: ' + group.id);
          });
        },
        list: function(schematronId, successFunction) {
          var data = getDataFromCache(groupCacheId(schematronId));
          if (data) {
            successFunction(data);
          } else {
            $http({
              method: 'GET',
              url: 'admin.schematroncriteriagroup.list?_content_type=json',
              params: {
                includeCriteria: true,
                schematronId: schematronId
              }
            }).success(function(data) {
              if (data === 'null') {
                data = [];
              }
              putDataIntoCache(groupCacheId(schematronId), data);
              successFunction(data);
            }).error(function() {
              alert('Error occurred during loading schematron criteria ' +
                  ' groups for schematron: ' + schematronId);
            });
          }
        }
      };
      this.schematron = {
        swapPriority: function(schema, higherPriority, lowerPriority) {
          var newPriority, oldPriority, update1, update2;
          newPriority = higherPriority.displaypriority;
          oldPriority = lowerPriority.displaypriority;
          update1 = $http({
            method: 'GET',
            url: 'admin.schematron.update?_content_type=json',
            params: {
              id: higherPriority.id,
              displaypriority: oldPriority
            }
          });
          update2 = $http({
            method: 'GET',
            url: 'admin.schematron.update?_content_type=json',
            params: {
              id: lowerPriority.id,
              displaypriority: newPriority
            }
          });

          $q.all([update1, update2]).then(function() {
            higherPriority.displaypriority = oldPriority;
            lowerPriority.displaypriority = newPriority;
            schema.schematron.sort(sortByDisplayPriority);
          });
        }
      };
      this.criteriaTypes = {
        list: function(successCallback) {
          var cachedCriteriaTypes = getDataFromCache('criteriaTypes');
          if (cachedCriteriaTypes) {
            successCallback(cachedCriteriaTypes);
          } else {
            $http.get('admin.schematrontype?_content_type=json').
                success(function(data) {
                  putDataIntoCache('criteriaTypes', data);
                  angular.forEach(data.schemas, function(schema) {
                    schema.schematron.sort(sortByDisplayPriority);
                  });
                  successCallback(data);
                }).error(function(data) {
                  alert('An Error occurred with the ' +
                      'admin.schematrontype ' +
                      ' request:' + data);
                });
          }
        }
      };
    }]);
}());
