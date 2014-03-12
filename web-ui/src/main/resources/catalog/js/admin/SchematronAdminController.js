/**
 * Created by Jesse on 2/12/14.
 */
(function() {
    'use strict';

    goog.provide('gn_schematronadmin_controller');
    goog.require('gn_schematronadmin_editcriteriadirective')
    var module = angular.module('gn_schematronadmin_controller', ['gn_schematronadmin_editcriteriadirective']);

    /**
     * GnAdminMetadataController provides administration tools
     * for metadata and templates
     */
    module.controller('GnSchematronEditCriteriaController', [
        '$scope', '$routeParams', '$location', '$translate', '$timeout', 'gnSchematronAdminService',
        function($scope, $routeParams, $location, $translate, $timeout, gnSchematronAdminService) {
            var updateLocation = function (schema, schematron, group) {
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

            var loadCriteria = function() {
                $scope.schematronGroups = null;
                gnSchematronAdminService.group.list($scope.selection.schematron.id, function (data) {
                    $scope.schematronGroups = data;
                    $scope.selection.group = data.length > 0 ? data[0] : null;
                });
            };

            $scope.selection = {
                schema: null,
                schematron : null,
                group: null
            };


            $scope.schematronGroups = null;
            $scope.isShowSchematronGroupHelp = false;
            $scope.isSelected = function(schematron) {return $scope.selection.schematron === schematron;};
            $scope.selectSchematron = function(schema, schematron) {
                if ($scope.selection.schema !== schema || $scope.selection.schematron !== schematron) {
                    updateLocation(schema, schematron);
                }
            };
            $scope.editGroup = {
                group: null,
                updatedGroup: null
            };
            $scope.startGroupEditing = function (group) {
                if ($scope.selection.group === group && $scope.editGroup.group !== group) {
                    $scope.editGroup.group = group;
                    $scope.editGroup.updatedGroup = angular.copy(group);
                    $timeout(function(){
                        $scope.editGroup.nameInput = angular.element(document.getElementById(group.id.name + '_NameInput'));
                        var nameInput = $scope.editGroup.nameInput;
                        nameInput.focus();
                        nameInput.select();
                        $scope.handleGroupEditKeyPress(); // update dirtystate of input element
                    });
                }
            };
            $scope.cancelGroupEdit = function () {
                $scope.editGroup.group = null;
                $scope.editGroup.updatedGroup = null;
            };
            $scope.saveGroupEdit = function () {
                gnSchematronAdminService.group.update($scope.editGroup.updatedGroup, $scope.selection.group);
                $scope.editGroup.group = null;
                $scope.editGroup.updatedGroup = null;
            };
            $scope.handleGroupEditKeyPress = function (keyCode) {
                var dupName = false;
                for (var i = 0; i < $scope.schematronGroups.length; i++) {
                    var group = $scope.schematronGroups[i];
                    if (group !== $scope.editGroup.group && group.id.name === $scope.editGroup.updatedGroup.id.name) {
                        dupName = true;
                        break;
                    }
                }
                switch (keyCode) {
                    case 13: //ENTER key
                        if (dupName) {
                            alert($translate("dupNameWarning"));
                        } else {
                            $scope.saveGroupEdit();
                        }
                        break;
                    case 27: // ESC key
                        $scope.cancelGroupEdit();
                        break;
                    default:
                        if (dupName) {
                            $scope.editGroup.nameInput.addClass("ng-invalid");
                            $scope.editGroup.nameInput.removeClass("ng-valid");
                            console.log("invalid");
                        } else {
                            console.log("valid");
                            $scope.editGroup.nameInput.addClass("ng-valid");
                            $scope.editGroup.nameInput.removeClass("ng-invalid");
                        }

                }
            };
            $scope.setRequirement = function(newRequirement) {
                if ($scope.selection.group.requirement !== newRequirement) {
                    var updated = angular.copy($scope.selection.group);
                    updated.requirement = newRequirement;
                    gnSchematronAdminService.group.update(updated, $scope.selection.group)
                }
            };
            var updateGroupCount = function (group, amount) {
                for (var i = 0; i < $scope.selection.schema.schematron.length; i++) {
                    var schematron = $scope.selection.schema.schematron[i];
                    if (schematron.id === group.id.schematronid) {
                        if (schematron.groupCount) {
                            schematron.groupCount = parseInt(schematron.groupCount) + amount;
                        } else {
                            schematron.groupCount = amount;
                        }
                    }
                }
            };
            $scope.dialog = {
                showDialog: function(){$('#schematronConfirmationDialog').modal('show')},
                dismissDialog: function(){$('#schematronConfirmationDialog').modal('hide')},
                deleteConfirmed: function(){}
            }
            $scope.deleteSchematronGroup = function (group) {
                $scope.dialog.deleteConfirmed = function() {
                    gnSchematronAdminService.group.remove(group, $scope.schematronGroups, function(){
                        if ($scope.schematronGroups.indexOf($scope.selection.group) < 0) {
                            if ($scope.schematronGroups.length == 0) {
                                $scope.selection.group = null;
                            } else {
                                $scope.selection.group = $scope.schematronGroups[0];
                            }
                            updateGroupCount(group, -1);
                        }
                    });
                };
                $scope.dialog.showDialog();
            };
            $scope.selectGroup = function (group) {
                if ($scope.selection.group !== group && $scope.schematronGroups.indexOf(group) != -1) {
                    $scope.selection.group = group;
                }
            };
            $scope.createSchematronGroup = function() {
                var name = $translate("NEW");
                var groups = $scope.schematronGroups;
                if (!groups) {
                    groups = [];
                    $scope.schematronGroups = groups;
                }

                var isNameTaken = function () {
                    for (var j = 0; j < groups.length; j++) {
                        var group = groups[j];
                        if (group.id.name === name) {
                            return true;
                        }
                    }
                    return false;
                };
                var i = 1;
                while(isNameTaken()) {
                    i ++;
                    name = $translate("NEW") + i;
                }
                var newGroup = {
                    id: {
                        name: name,
                        schematronid: $scope.selection.schematron.id
                    },
                    requirement: $scope.requirements[0]
                };
                gnSchematronAdminService.group.add(newGroup, groups, function(group) {
                    $scope.selection.group = group;
                    updateGroupCount(group, 1);
                });
            };
            gnSchematronAdminService.criteriaTypes.list(function(data){
                $scope.schematrons = data.schemas;
                $scope.requirements = data.requirements;

                if ($routeParams.schemaName) {
                    var findSchema = function(schemaName) {
                        for (var i = 0; i < $scope.schematrons.length; i++) {
                            var schemaDef = $scope.schematrons[i];
                            if (schemaDef.name === $routeParams.schemaName) {
                                return schemaDef;
                            }
                        }
                        return undefined;
                    };
                    var findSchematron = function (schemaDef, schematronId) {
                        if (schematronId) {
                            for (var i = 0; i < schema.schematron.length; i++) {
                                var schematron = schema.schematron[i];
                                if (schematronId === schematron.id) {
                                    return schematron;
                                }
                            }
                        }
                    };

                    var schema = findSchema($routeParams.schemaName);

                    if (!schema) {
                        updateLocation();
                        return;
                    }

                    var schematron = findSchematron(schema, $routeParams.schematronId);

                    if (!schematron) {
                        if (schema.schematron.length == 0) {
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
})();
