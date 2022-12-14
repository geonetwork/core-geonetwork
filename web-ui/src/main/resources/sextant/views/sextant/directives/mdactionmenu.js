(function () {
  goog.provide("sxt_mdactionmenu");

  var module = angular.module("sxt_mdactionmenu", []);

  module.directive("sxtMdManageActionsMenu", [
    "gnMetadataActions",
    "$http",
    "$location",
    "Metadata",
    "sxtService",
    "gnConfig",
    function (gnMetadataActions, $http, $location, Metadata, sxtService, gnConfig) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/views/sextant/directives/" + "partials/mdmanageactionmenu.html",
        link: function linkFn(scope, element, attrs) {
          scope.md = scope.$eval(attrs.sxtMdManageActionsMenu);
          scope.tasks = [];
          scope.doiDefined =
            scope.md.getLinksByType("DOI", "WWW:LINK-1.0-http--metadata-URL").length > 0;
          scope.hasVisibletasks = false;

          function loadTasks() {
            return $http
              .get("../api/status/task", { cache: true })
              .success(function (data) {
                scope.tasks = data;
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
              isVisible: function (md) {
                return gnConfig["system.publication.doi.doienabled"];
              },
              isApplicable: function (md) {
                // TODO: Would be good to return why a task is not applicable as tooltip
                // TODO: Add has DOI already
                return (
                  md &&
                  md.isPublished() &&
                  md.isTemplate === "n" &&
                  JSON.parse(md.isHarvested) === false
                );
              }
            }
          };
          loadTasks();
        }
      };
    }
  ]);

  module.directive("sxtMdActionsMenu", [
    "gnMetadataActions",
    "$http",
    "$location",
    "Metadata",
    "sxtService",
    "gnConfig",
    function (gnMetadataActions, $http, $location, Metadata, sxtService, gnConfig) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/views/sextant/directives/" + "partials/mdactionmenu.html",
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.sxtMdActionsMenu);

          scope.tasks = [];
          scope.hasVisibletasks = false;

          function loadTasks() {
            return $http
              .get("../api/status/task", { cache: true })
              .success(function (data) {
                scope.tasks = data;
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
              isVisible: function (md) {
                return gnConfig["system.publication.doi.doienabled"];
              },
              isApplicable: function (md) {
                // TODO: Would be good to return why a task is not applicable as tooltip
                // TODO: Add has DOI already
                return (
                  md &&
                  md.isPublished() &&
                  md.isTemplate === "n" &&
                  JSON.parse(md.isHarvested) === false
                );
              }
            }
          };

          loadTasks();
          if (!scope.md) {
            var url = $location.url();
            var uuid = url.substring(url.lastIndexOf("/") + 1);
            $http
              .post("../api/search/records/_search", {
                query: {
                  bool: {
                    must: [
                      {
                        multi_match: {
                          query: uuid,
                          fields: ["id", "uuid"]
                        }
                      },
                      { terms: { draft: ["n", "y", "e"] } }
                    ]
                  }
                }
              })
              .success(function (resp) {
                scope.md = new Metadata(resp.metadata);
                sxtService.feedMd(scope);
              });
          }
          // START sextant SPECIFIC
          scope.location = window.location;
          // END sextant SPECIFIC
        }
      };
    }
  ]);
})();
