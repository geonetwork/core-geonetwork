(function() {

  goog.provide('sxt_mdactionmenu');

  var module = angular.module('sxt_mdactionmenu', []);


  module.directive('sxtMdActionsMenu', ['gnMetadataActions', '$http',
    '$location', 'Metadata', 'sxtService',
    function(gnMetadataActions, $http, $location, Metadata, sxtService) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.sxtMdActionsMenu);


          scope.tasks = [];
          scope.hasVisibletasks = false;

          function loadTasks() {
            return $http.get('../api/status/task', {cache: true}).
            success(function(data) {
              scope.tasks = data;
              scope.getVisibleTasks();
            });
          };

          scope.getVisibleTasks = function() {
            $.each(scope.tasks, function(i,t) {
              scope.hasVisibletasks = scope.taskConfiguration[t.name] &&
                scope.taskConfiguration[t.name].isVisible &&
                scope.taskConfiguration[t.name].isVisible();
            });
          }

          scope.taskConfiguration = {
            doiCreationTask: {
              isVisible: function(md) {
                return gnConfig['system.publication.doi.doienabled'];
              },
              isApplicable: function(md) {
                // TODO: Would be good to return why a task is not applicable as tooltip
                // TODO: Add has DOI already
                return md && md.isPublished()
                  && md.isTemplate === 'n'
                  && md.isHarvested === 'n';
              }
            }
          };

          loadTasks();
          if (!scope.md) {
            var url = $location.url();
            var uuid = url.substring(url.lastIndexOf('/') + 1);
            $http.get('q?_uuid=' + uuid + '&fast=index&_content_type=json&buildSummary=false').success (function (resp) {
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
