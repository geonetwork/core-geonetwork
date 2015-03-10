/**
 * Created by Jesse on 2/12/14.
 */
(function() {
  'use strict';

  goog.provide('gn_schematronadmin_controller');
  goog.require('gn_schematronadmin_editcriteriadirective');
  var module = angular.module('gn_schematronadmin_controller',
      ['gn_schematronadmin_editcriteriadirective']);

  /**
     * GnAdminMetadataController provides administration tools
     * for metadata and templates
     */
  module.controller('GnSchematronEditCriteriaController', [
    '$scope', '$routeParams', '$location', '$translate',
    '$timeout', 'gnSchematronAdminService',
    function($scope, $routeParams, $location, $translate,
             $timeout, gnSchematronAdminService) {
      var updateLocation, loadCriteria, updateGroupCount;
      updateLocation = function(schema, schematron, group) {
        var path = '/metadata/schematron';
        if (schema && schematron) {
          path += '/' + schema.name + '/' + schematron.id;
        }
        if (schema && schematron && group) {
          path += '/' + group.id.name;
        }
        $location.path(path);
        return path;
      };

      loadCriteria = function() {
        $scope.schematronGroups = null;
        gnSchematronAdminService.group.list(
            $scope.selection.schematron.id, function(data) {
              $scope.schematronGroups = data;
              $scope.selection.group = data.length > 0 ? data[0] : null;
            });
      };

      $scope.selection = {
        schema: null,
        schematron: null,
        group: null
      };

      $scope.schematronGroups = null;
      $scope.isShowSchematronGroupHelp = false;
      $scope.isSelected = function(schematron) {
        return $scope.selection.schematron === schematron;
      };
      $scope.selectSchematron = function(schema, schematron) {
        if ($scope.selection.schema !== schema ||
            $scope.selection.schematron !== schematron) {
          updateLocation(schema, schematron);
        }
      };
      $scope.editGroup = {
        group: null,
        updatedGroup: null
      };
      $scope.startGroupEditing = function(group) {
        if ($scope.selection.group === group &&
            $scope.editGroup.group !== group) {
          $scope.editGroup.group = group;
          $scope.editGroup.updatedGroup = angular.copy(group);
          $timeout(function() {
            $scope.editGroup.nameInput =
                angular.element(document.
                getElementById(group.id.name + '_NameInput'));
            var nameInput = $scope.editGroup.nameInput;
            nameInput.focus();
            nameInput.select();
            $scope.handleGroupEditKeyPress();
            // update dirtystate of input element
          });
        }
      };
      $scope.cancelGroupEdit = function() {
        $scope.editGroup.group = null;
        $scope.editGroup.updatedGroup = null;
      };
      $scope.saveGroupEdit = function() {
        gnSchematronAdminService.group
          .update($scope.editGroup.updatedGroup, $scope.selection.group);
        $scope.editGroup.group = null;
        $scope.editGroup.updatedGroup = null;
      };
      $scope.handleGroupEditKeyPress = function(keyCode) {
        var i, dupName, group;
        dupName = false;
        for (i = 0; i < $scope.schematronGroups.length; i++) {
          group = $scope.schematronGroups[i];
          if (group !== $scope.editGroup.group &&
              group.id.name === $scope.editGroup.updatedGroup.id.name) {
            dupName = true;
            break;
          }
        }
        switch (keyCode) {
          case 13: //ENTER key
            if (dupName) {
              alert($translate('dupNameWarning'));
            } else {
              $scope.saveGroupEdit();
            }
            break;
          case 27: // ESC key
            $scope.cancelGroupEdit();
            break;
          default:
            if (dupName) {
              $scope.editGroup.nameInput.addClass('ng-invalid');
              $scope.editGroup.nameInput.removeClass('ng-valid');
              console.log('invalid');
            } else {
              console.log('valid');
              $scope.editGroup.nameInput.addClass('ng-valid');
              $scope.editGroup.nameInput.removeClass('ng-invalid');
            }
        }
      };
      $scope.setRequirement = function(newRequirement) {
        if ($scope.selection.group.requirement !== newRequirement) {
          var updated = angular.copy($scope.selection.group);
          updated.requirement = newRequirement;
          gnSchematronAdminService.group
            .update(updated, $scope.selection.group);
        }
      };
      updateGroupCount = function(group, amount) {
        var i, schematron;
        for (i = 0; i < $scope.selection.schema.schematron.length; i++) {
          schematron = $scope.selection.schema.schematron[i];
          if (schematron.id === group.id.schematronid) {
            if (schematron.groupCount) {
              schematron.groupCount =
                  parseInt(schematron.groupCount) + amount;
            } else {
              schematron.groupCount = amount;
            }
          }
        }
      };
      $scope.confirmationDialog = {
        message: $translate('confirmDeleteSchematronCriteriaGroup'),
        showDialog: function() {
          $('#schematronConfirmationDialog').modal('show');
        },
        dismissDialog: function() {
          $('#schematronConfirmationDialog').modal('hide');
        },
        deleteConfirmed: function() {}
      };
      $scope.deleteSchematronGroup = function(group) {
        $scope.confirmationDialog.message =
            $translate('confirmDeleteSchematronCriteriaGroup');
        $scope.confirmationDialog.deleteConfirmed = function() {
          gnSchematronAdminService.group
            .remove(group, $scope.schematronGroups, function() {
                if ($scope.schematronGroups
                  .indexOf($scope.selection.group) < 0) {
                  if ($scope.schematronGroups.length === 0) {
                    $scope.selection.group = null;
                  } else {
                    $scope.selection.group = $scope.schematronGroups[0];
                  }
                  updateGroupCount(group, -1);
                }
              });
        };
        $scope.confirmationDialog.showDialog();
      };
      $scope.selectGroup = function(group) {
        if ($scope.selection.group !== group &&
            $scope.schematronGroups.indexOf(group) !== -1) {
          $scope.selection.group = group;
        }
      };
      $scope.raiseSchematron = function(schema, schematron) {
        var idx = schema.schematron.indexOf(schematron);
        if (idx !== 0) {
          gnSchematronAdminService.schematron
            .swapPriority(schema, schema.schematron[idx - 1], schematron);
        }
      };
      $scope.lowerSchematron = function(schema, schematron) {
        var idx = schema.schematron.indexOf(schematron);
        if (idx !== schema.schematron.length - 1) {
          gnSchematronAdminService.schematron
            .swapPriority(schema, schematron, schema.schematron[idx + 1]);
        }
      };
      $scope.createSchematronGroup = function(newGroup) {
        if (!newGroup) {
          newGroup = {
            id: {
              name: $translate('NEW'),
              schematronid: $scope.selection.schematron.id
            },
            requirement: $scope.requirements[0]
          };
        }
        var name, groups, i = -1;
        name = newGroup.id.name;
        groups = $scope.schematronGroups;
        if (!groups) {
          groups = [];
          $scope.schematronGroups = groups;
        }

        var isNameTaken = function(name) {
          var j, group;
          for (j = 0; j < groups.length; j++) {
            group = groups[j];
            if (group.id.name === (name)) {
              return true;
            }
          }
          return false;
        };
        if (isNameTaken(name)) {
          i = 1;
          while (isNameTaken(name + i)) {
            i++;
          }
          newGroup.id.name = name + i;
        }

        gnSchematronAdminService.group.add(newGroup, groups, function(group) {
          $scope.selection.group = group;
          updateGroupCount(group, 1);
          var i, criteria = group.criteria;
          group.criteria = [];
          for (i = 0; i < criteria.length; i++) {
            var template = angular.copy(criteria[i]);
            gnSchematronAdminService.criteria.add(criteria[i],
                group.criteria[i], group);
          }
        });
      };
      $scope.duplicateSchematronGroup = function() {
        $scope.createSchematronGroup(angular.copy($scope.selection.group));
      };
      gnSchematronAdminService.criteriaTypes.list(function(data) {
        $scope.schematrons = data.schemas;
        $scope.requirements = data.requirements;

        if ($routeParams.schemaName) {
          var findSchema, findSchematron, schema, schematron;
          findSchema = function(schemaName) {
            var key, schemaDef;
            for (key in $scope.schematrons) {
              if ($scope.schematrons.hasOwnProperty(key)) {
                schemaDef = $scope.schematrons[key];
                if (schemaDef.name === schemaName) {
                  return schemaDef;
                }
              }
            }
            return undefined;
          };

          findSchematron = function(schemaDef, schematronId) {
            var key, schematron;
            if (schematronId) {
              for (key in schemaDef.schematron) {
                if (schemaDef.schematron.hasOwnProperty(key)) {
                  schematron = schemaDef.schematron[key];
                  if (schematronId === schematron.id) {
                    return schematron;
                  }
                }
              }
            }
            return undefined;
          };

          schema = findSchema($routeParams.schemaName);

          if (!schema) {
            updateLocation();
            return;
          }

          schematron = findSchematron(schema, $routeParams.schematronId);

          if (!schematron) {
            if (schema.schematron.length === 0) {
              updateLocation();
            } else {
              schematron = schema.schematron[0];
            }
          }

          if (schematron) {
            $scope.selection.schema = schema;
            $scope.selection.schematron = schematron;
            updateLocation(schema, schematron);
            loadCriteria();
          }
        }
      });
    }]);
}());
