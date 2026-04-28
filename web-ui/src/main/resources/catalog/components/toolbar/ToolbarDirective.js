/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
  goog.provide("gn_toolbar_directive");

  var module = angular.module("gn_toolbar_directive", []);

  module.constant("GN_DEFAULT_MENU", [
    "gn-site-name-menu",
    "gn-portal-switcher",
    "gn-search-menu",
    "gn-map-menu",
    "gn-contribute-menu",
    "gn-admin-menu",
    "gn-static-pages-list-viewer"
  ]);

  module.constant("GN_DEFAULT_RECORD_VIEW_MENU", [
    "gn-recordview-edit-menu",
    "gn-recordview-delete-menu",
    "gn-recordview-manage-menu",
    "gn-recordview-download-menu",
    "gn-recordview-display-menu"
  ]);

  function watchRecord(scope, mdExpression, onChange) {
    var initialized = false;

    scope.$watch(mdExpression, function (md) {
      if (!initialized || md !== scope.md) {
        initialized = true;
        onChange(md);
      }
    });
  }

  module.directive("gnToolbar", [
    "GN_DEFAULT_MENU",
    "GN_DEFAULT_RECORD_VIEW_MENU",
    "gnGlobalSettings",
    function (GN_DEFAULT_MENU, GN_DEFAULT_RECORD_VIEW_MENU, gnGlobalSettings) {
      return {
        templateUrl: "../../catalog/components/toolbar/partials/top-toolbar.html",
        link: function ($scope) {
          $scope.toolbarMenu =
            gnGlobalSettings.gnCfg.mods.header.topCustomMenu &&
            gnGlobalSettings.gnCfg.mods.header.topCustomMenu.length > 0
              ? gnGlobalSettings.gnCfg.mods.header.topCustomMenu
              : GN_DEFAULT_MENU;
          $scope.recordviewMenu =
            gnGlobalSettings.gnCfg.mods.recordview.recordviewCustomMenu &&
            gnGlobalSettings.gnCfg.mods.recordview.recordviewCustomMenu.length > 0
              ? gnGlobalSettings.gnCfg.mods.recordview.recordviewCustomMenu
              : GN_DEFAULT_RECORD_VIEW_MENU;

          $scope.isPage = function (page) {
            return angular.isObject(page) || page.indexOf("gn-") === -1;
          };
        }
      };
    }
  ]);
  module.directive("gnSiteNameMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-sitename.html"
      };
    }
  ]);
  module.directive("gnSearchMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-search.html"
      };
    }
  ]);
  module.directive("gnMapMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-map.html"
      };
    }
  ]);
  module.directive("gnContributeMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-contribute.html"
      };
    }
  ]);
  module.directive("gnAdminMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-admin.html"
      };
    }
  ]);
  module.directive("gnSigninMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-signin.html"
      };
    }
  ]);
  module.directive("gnLanguagesMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-languages.html"
      };
    }
  ]);
  module.directive("gnRecordViewEditMenu", [
    function () {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-recordview-edit.html"
      };
    }
  ]);
  module.directive("gnRecordViewDeleteMenu", [
    function () {
      return {
        replace: true,
        templateUrl:
          "../../catalog/components/toolbar/partials/menu-recordview-delete.html"
      };
    }
  ]);
  module.directive("gnRecordViewDisplayModeMenu", [
    function () {
      return {
        replace: true,
        templateUrl:
          "../../catalog/components/toolbar/partials/menu-recordview-display-mode.html"
      };
    }
  ]);

  module.directive("gnManageRecordMenu", [
    "gnMetadataActions",
    "$http",
    "gnConfig",
    "gnConfigService",
    "gnGlobalSettings",
    "gnLangs",
    function (
      gnMetadataActions,
      $http,
      gnConfig,
      gnConfigService,
      gnGlobalSettings,
      gnLangs
    ) {
      return {
        replace: true,
        templateUrl: "../../catalog/components/toolbar/partials/menu-manage-record.html",
        link: function (scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.tasks = [];
          scope.hasVisibletasks = false;
          scope.doiServers = [];
          scope.status = undefined;

          scope.statusEffects = {
            editor: [
              {
                from: "draft",
                to: "submitted"
              },
              {
                from: "retired",
                to: "draft"
              },
              {
                from: "submitted",
                to: "draft"
              }
            ],
            reviewer: [
              {
                from: "draft",
                to: "submitted"
              },
              {
                from: "submitted",
                to: "approved"
              },
              {
                from: "submitted",
                to: "draft"
              },
              {
                from: "draft",
                to: "approved"
              },
              {
                from: "approved",
                to: "retired"
              },
              {
                from: "retired",
                to: "draft"
              }
            ]
          };

          function loadWorkflowStatus() {
            return $http
              .get("../api/status/workflow", { cache: true })
              .then(function (response) {
                scope.status = {};
                response.data.forEach(function (s) {
                  scope.status[s.name] = s.id;
                });
              });
          }

          function loadTasks() {
            return $http
              .get("../api/status/task", { cache: true })
              .then(function (response) {
                scope.tasks = response.data;
                scope.getVisibleTasks();
              });
          }

          scope.getVisibleTasks = function () {
            $.each(scope.tasks, function (i, t) {
              scope.hasVisibletasks =
                scope.taskConfiguration[t.name] &&
                scope.taskConfiguration[t.name].isVisible &&
                scope.taskConfiguration[t.name].isVisible();
            });
          };

          scope.taskConfiguration = {
            doiCreationTask: {
              isVisible: function () {
                return scope.doiServers.length > 0;
              },
              isApplicable: function (md) {
                return (
                  md &&
                  md.isPublished() &&
                  md.isTemplate === "n" &&
                  JSON.parse(md.isHarvested) === false
                );
              }
            }
          };

          scope.displayPublicationOption = function (md, user, pubOption) {
            return (
              md &&
              md.canReview &&
              md.draft != "y" &&
              md.mdStatus != 3 &&
              ((md.isPublished(pubOption) && user.canUnpublishMetadata()) ||
                (!md.isPublished(pubOption) && user.canPublishMetadata()))
            );
          };

          scope.displayWorkflowStepOption = function (step, user) {
            return (
              user.id &&
              scope.md &&
              user.canEditRecord(scope.md) &&
              scope.md.groupOwner &&
              (user.isAdmin() || user.isEditorOrMoreForGroup(scope.md.groupOwner)) &&
              scope.md.mdStatus &&
              scope.status &&
              step.from &&
              scope.status[step.from] &&
              scope.md.mdStatus == scope.status[step.from] &&
              scope.isMdWorkflowEnable &&
              scope.md.isWorkflowEnabled()
            );
          };

          scope.anyWorkflowOptionDisplayed = function (user) {
            if (scope.displayEnableWorkflowOption(user)) {
              return true;
            }

            var statusEffects = scope.getStatusEffects(user);
            for (var i = 0; i < statusEffects.length; i++) {
              if (scope.displayWorkflowStepOption(statusEffects[i], user)) {
                return true;
              }
            }

            return false;
          };

          scope.getStatusEffects = function (user) {
            var isReviewer =
              user.isAdmin() || user.isReviewerForGroup(scope.md.groupOwner);
            return scope.statusEffects[isReviewer ? "reviewer" : "editor"];
          };

          scope.displayEnableWorkflowOption = function (user) {
            return (
              scope.isMdWorkflowEnable &&
              scope.md &&
              !scope.md.isWorkflowEnabled() &&
              user.id &&
              (user.isAdmin() || user.canEditRecord(scope.md)) &&
              gnMetadataActions.isGroupWithWorkflowEnabled(scope.ownerGroupName)
            );
          };

          scope.getScope = function () {
            return scope;
          };

          function updateMetadataContext(md) {
            scope.md = md;
            scope.doiServers = [];
            scope.ownerGroupName = undefined;

            if (md) {
              $http.get("../api/doiservers/metadata/" + md.id).then(function (response) {
                scope.doiServers = response.data;
              });

              if (md.groupOwner) {
                gnMetadataActions.getGroupName(md.groupOwner).then(function (name) {
                  scope.ownerGroupName = name;
                });
              }
            }
          }

          gnConfigService.load().then(function () {
            scope.isMdWorkflowEnable = gnConfig["metadata.workflow.enable"];
            scope.isMdWorkflowAssistEnable =
              gnGlobalSettings.gnCfg.mods.workflowHelper.enabled;
            scope.workFlowApps =
              gnGlobalSettings.gnCfg.mods.workflowHelper.workflowAssistApps;
            scope.iso2Lang = gnLangs.getIso2Lang(gnLangs.getCurrent());
          });

          loadTasks();
          loadWorkflowStatus();

          watchRecord(
            scope,
            attrs.gnManageRecordMenu || "mdView.current.record",
            updateMetadataContext
          );
        }
      };
    }
  ]);

  module.directive("gnDownloadRecordMenu", [
    "gnMetadataActions",
    "gnConfig",
    "gnConfigService",
    "gnGlobalSettings",
    "gnMdFormatter",
    function (
      gnMetadataActions,
      gnConfig,
      gnConfigService,
      gnGlobalSettings,
      gnMdFormatter
    ) {
      return {
        replace: true,
        templateUrl:
          "../../catalog/components/toolbar/partials/menu-download-record.html",
        link: function (scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.formatterList = [];
          scope.attachmentsExceedExportLimit = false;

          scope.buildFormatter = function (url, uuid, isDraft) {
            if (url.indexOf("${uuid}") !== -1) {
              return url.replace("${lang}", scope.lang).replace("${uuid}", uuid);
            } else {
              return (
                "../api/records/" +
                uuid +
                url.replace("${lang}", scope.lang) +
                (isDraft == "y"
                  ? (url.indexOf("?") !== -1 ? "&" : "?") + "approved=false"
                  : "")
              );
            }
          };

          function updateFormatterList(md) {
            if (!md) {
              scope.formatterList = [];
              return;
            }

            gnMdFormatter
              .getAvailableFormattersForRecord(md)
              .then(function (availableFormatters) {
                var formatterList = gnGlobalSettings.gnCfg.mods.search.downloadFormatter;

                scope.formatterList = gnMdFormatter.calculateValidFormattersForRecord(
                  formatterList,
                  availableFormatters
                );
              });
          }

          function updateAttachmentsExportLimit(md) {
            var totalAttachmentsSize = 0;

            if (md && Array.isArray(md.filestore)) {
              totalAttachmentsSize = md.filestore.reduce(function (sum, attachment) {
                return sum + attachment.size;
              }, 0);
            }

            if (scope.attachmentsSizeLimit > 0 && isFinite(scope.attachmentsSizeLimit)) {
              var attachmentsSizeLimitBytes = scope.attachmentsSizeLimit * 1024 * 1024;
              scope.attachmentsExceedExportLimit =
                totalAttachmentsSize > attachmentsSizeLimitBytes;
            } else {
              scope.attachmentsExceedExportLimit = false;
            }
          }

          function updateMetadataContext(md) {
            scope.md = md;
            updateFormatterList(md);
            updateAttachmentsExportLimit(md);
          }

          gnConfigService.load().then(function () {
            scope.attachmentsSizeLimit = Number(
              gnConfig["metadata.zipExport.attachmentsSizeLimit"]
            );

            updateAttachmentsExportLimit(scope.md);
          });

          watchRecord(
            scope,
            attrs.gnDownloadRecordMenu || "mdView.current.record",
            updateMetadataContext
          );
        }
      };
    }
  ]);
})();
