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
  "use strict";
  goog.provide("gn_schematronadminservice");

  var module = angular.module("gn_schematronadminservice", []);

  /**
   * Display harvester identification section with
   * name, group and icon
   */
  module.service("gnSchematronAdminService", [
    "$http",
    "$cacheFactory",
    "$q",
    function ($http, $cacheFactory, $q) {
      var TIMEOUT,
        cache,
        getDataFromCache,
        putDataIntoCache,
        removeElementFromCache,
        groupCacheId,
        updateCacheOnGroupChange,
        updateCacheOnCriteriaChange,
        sortByDisplayPriority,
        findIndex,
        criteriaComparator,
        groupComparator;
      TIMEOUT = 60000; // refresh cache every minute
      cache = $cacheFactory("gnSchematronAdminService");
      getDataFromCache = function (id, timeout) {
        var lastUsed, now;
        if (!timeout) {
          timeout = TIMEOUT;
        }
        lastUsed = cache.get("lastUsed_" + id);
        now = new Date();
        if (!lastUsed || now - lastUsed > timeout) {
          return undefined;
        }
        return cache.get(id);
      };
      putDataIntoCache = function (id, data) {
        var now = new Date().getTime();
        cache.put("lastUsed_" + id, now);
        cache.put(id, data);
      };
      removeElementFromCache = function (id) {
        cache.remove(id);
        cache.remove("lastUsed_" + id);
      };
      groupCacheId = function (schematronId) {
        return "criteriaGroup_" + schematronId;
      };

      updateCacheOnGroupChange = function (schematronId) {
        removeElementFromCache("criteriaTypes");
        removeElementFromCache(groupCacheId(schematronId));
      };
      updateCacheOnCriteriaChange = function (schematronId) {
        removeElementFromCache(groupCacheId(schematronId));
      };
      sortByDisplayPriority = function (a, b) {
        return a.displayPriority - b.displayPriority;
      };
      findIndex = function (array, object, comparator) {
        var i, arrayObj;
        for (i = 0; i < array.length; i++) {
          arrayObj = array[i];
          if (comparator(arrayObj, object)) {
            return i;
          }
        }
        return -1;
      };
      criteriaComparator = function (o1, o2) {
        return o1.id === o2.id;
      };
      groupComparator = function (o1, o2) {
        return o1.id.schematronId === o2.id.schematronId && o1.id.name === o2.id.name;
      };

      this.criteria = {
        remove: function (criteria, group) {
          $http.delete("../api/schematron/criteria/" + criteria.id).then(
            function () {
              var list, idx;
              updateCacheOnCriteriaChange(group.id.schematronId);
              list = group.criteria;
              idx = findIndex(list, criteria, criteriaComparator);
              if (idx !== -1) {
                list.splice(idx, 1);
              }
            },
            function () {
              alert("Error deleting criteria: " + criteria.id);
            }
          );
        },
        update: function (updated, original, group) {
          $http
            .put("../api/schematron/criteria/" + original.id, null, {
              params: {
                type: updated.type,
                value: updated.value,
                uiType: updated.uiType,
                uiValue: updated.uiValue
              }
            })
            .then(
              function () {
                updateCacheOnCriteriaChange(group.id.schematronId);

                angular.copy(updated, original);
              },
              function () {
                alert("Error updating criteria: " + original.id);
              }
            );
        },
        add: function (criteria, original, group) {
          $http
            .post(
              "../api/schematron/" + group.id.schematronId + "/groups/criteria",
              null,
              {
                params: {
                  groupName: group.id.name,
                  type: criteria.type,
                  value: criteria.value,
                  uiType: criteria.uiType,
                  uiValue: criteria.uiValue
                }
              }
            )
            .then(
              function (response) {
                updateCacheOnCriteriaChange(group.id.schematronId);
                var added = angular.copy(criteria);
                added.id = response.data.id;
                if (!group.criteria) {
                  group.criteria = [];
                }
                group.criteria.push(added);
                angular.copy(original, criteria);
              },
              function () {
                alert(
                  "Error adding criteria: {\n\ttype:" +
                    criteria.type +
                    ",\n\tvalue:" +
                    criteria.value +
                    ",\n\tgroupName:" +
                    group.id.name +
                    "schematronId: " +
                    group.id.schematronId
                );
              }
            );
        }
      };
      this.group = {
        remove: function (group, groupList, successCallback) {
          $http
            .delete("../api/schematron/" + group.id.schematronId + "/groups", {
              params: { name: group.id.name }
            })
            .then(
              function () {
                updateCacheOnGroupChange(group.id.schematronId);
                var idx = findIndex(groupList, group, groupComparator);
                if (idx !== -1) {
                  groupList.splice(idx, 1);
                }
                successCallback();
              },
              function () {
                alert("Error deleting Schematron Criteria Group: " + group.id.name);
              }
            );
        },
        update: function (updated, original) {
          var params = {
            name: original.id.name,
            requirement: updated.requirement
          };
          if (updated.id.name !== original.id.name) {
            params.newName = updated.id.name;
          }
          if (updated.id.schematronId !== original.id.schematronId) {
            params.newSchematronId = updated.id.schematronId;
          }
          $http
            .put("../api/schematron/" + original.id.schematronId + "/groups", null, {
              params: params
            })
            .then(
              function () {
                updateCacheOnGroupChange(original.id.schematronId);
                updateCacheOnGroupChange(updated.id.schematronId);
                original.id.name = updated.id.name;
                original.id.schematronId = updated.id.schematronId;
                original.requirement = updated.requirement;
              },
              function () {
                alert("Error editing Schematron Criteria Group: " + original.id.name);
              }
            );
        },
        add: function (group, groupList, successCallback) {
          $http
            .post("../api/schematron/" + group.id.schematronId + "/groups", null, {
              params: {
                name: group.id.name,
                requirement: group.requirement
              }
            })
            .then(
              function () {
                updateCacheOnGroupChange(group.id.schematronId);
                groupList.push(group);
                successCallback(group);
              },
              function () {
                alert("Error adding new Schematron Criteria Group: " + group.id.name);
              }
            );
        },
        list: function (schematronId, successFunction) {
          var data = getDataFromCache(groupCacheId(schematronId));
          if (data) {
            successFunction(data);
          } else {
            $http.get("../api/schematron/" + schematronId + "/groups").then(
              function (response) {
                putDataIntoCache(groupCacheId(schematronId), response.data);
                successFunction(response.data);
              },
              function () {
                alert(
                  "Error occurred during loading schematron criteria " +
                    " groups for schematron: " +
                    schematronId
                );
              }
            );
          }
        }
      };
      this.schematron = {
        swapPriority: function (schema, higherPriority, lowerPriority) {
          var newPriority, oldPriority, update1, update2;
          newPriority = higherPriority.displayPriority;
          oldPriority = lowerPriority.displayPriority;
          update1 = $http.put("../api/schematron/" + higherPriority.id, null, {
            params: { displayPriority: oldPriority }
          });
          update2 = $http.put("../api/schematron/" + lowerPriority.id, null, {
            params: { displayPriority: newPriority }
          });

          $q.all([update1, update2]).then(function () {
            higherPriority.displayPriority = oldPriority;
            lowerPriority.displayPriority = newPriority;
            schema.schematrons.sort(sortByDisplayPriority);
          });
        }
      };
      this.criteriaTypes = {
        list: function (successCallback) {
          var cachedCriteriaTypes = getDataFromCache("criteriaTypes");
          if (cachedCriteriaTypes) {
            successCallback(cachedCriteriaTypes);
          } else {
            $http.get("../api/schematron").then(
              function (response) {
                var data = response.data;
                putDataIntoCache("criteriaTypes", data);
                angular.forEach(data.schemas, function (schema) {
                  schema.schematrons.sort(sortByDisplayPriority);
                });
                successCallback(data);
              },
              function () {
                alert("An error occurred while loading the schematron configuration.");
              }
            );
          }
        }
      };
    }
  ]);
})();
