(function() {
  goog.provide('gn_scroll_spy_directive');

  goog.require('gn_utility_service');

  var module = angular.module('gn_scroll_spy_directive', []);

  /**
   * Scroll spy navigation bar. Only cover 2 levels of fieldsets.
   *
   * Fieldset could be directly nested ie. fieldset > fieldset
   * or allDepth parameter is set to true to skip on level of
   * fieldset ie. fieldset > fieldset (skipped) > fieldset. This
   * mode correspond to an editor in non flat mode.
   *
   * The watch attribute define a scope variable to watch before
   * initialization. Initialization is done once.
   *
   * By default, the scroll spy div is displayed. Use collapse
   * attribute to collapsed it.
   */
  module.directive('gnScrollSpy', [
    'gnUtilityService', '$timeout',
    function(gnUtilityService, $timeout) {

      return {
        restrict: 'A',
        replace: false,
        scope: {
          id: '@gnScrollSpy',
          watch: '=',
          depth: '@',
          allDepth: '@',
          collapse: '@'
        },
        templateUrl: '../../catalog/components/common/scrollspy/partials/' +
            'scrollspy.html',
        link: function(scope, element, attrs) {
          var counter = 0,
              depth = scope.depth || 2,
              rootElementDepth = 0,
              isInView = gnUtilityService.isInView,
              childrenSearch =
              (scope.allDepth == 'true' ?
                  'fieldset > legend' : 'fieldset > fieldset > legend');

          scope.scrollTo = gnUtilityService.scrollTo;
          // Ordered list in an array of elements to spy
          scope.spyElems = null;

          var registerSpy = function() {
            var id = $(this).attr('id'), currentDepth = $(this).parents(
                'fieldset').length - 1 - rootElementDepth;

            if (currentDepth <= depth) {
              // Get the element id or create an id for the element to spy
              if (!id) {
                id = scope.id + '-' + currentDepth + '-' + (counter++);
                $(this).attr('id', id);
              }

              // Spy link configuration
              var spy = {
                id: '#' + id,
                label: $(this).text(),
                elem: $(this).parent(),
                active: false,
                children: [] // May contain children
              };

              // Root element registration
              if (currentDepth === 0) {
                scope.spyElems.push(spy);
              } else {
                // Children registration
                // Skip on fieldset if requested
                var parent =
                    scope.allDepth == 'true' ?
                        $(this) : $(this).parent('fieldset');
                var parentFieldsetId = parent
                    .parent('fieldset').parent('fieldset')
                    .children('legend').attr('id');
                if (parentFieldsetId) {
                  var parentSpy = $.grep(scope.spyElems, function(spy) {
                    return spy.id === '#' + parentFieldsetId;
                  });

                  // Add the child
                  parentSpy[0] && parentSpy[0].children.push(spy);
                }
              }

              $(this).parent().find(childrenSearch).each(
                  registerSpy);
            }
          };

          var init = function() {
            // Look for fieldsets and register spy
            scope.spyElems = [];
            rootElement = $('#' + scope.id);

            // Get the number of fieldset above the current element
            // to compute depth later.
            rootElementDepth = rootElement.parents('fieldset').length;
            if (rootElement.prop('tagName') === undefined) {
              console.log(scope.id +
                  ' element is not available for scroll spy.');
              return;
            }
            if (rootElement.prop('tagName').toLowerCase() === 'fieldset') {
              rootElementDepth++;
            }

            // Spy only first level of fieldsets
            rootElement.find('> fieldset > legend').each(registerSpy);

            $(window).scroll(function() {
              scope.$apply(function() {
                var firstFound = false;

                // Check position of each first and second
                // level element to spy and activate them
                // if in the current viewport.
                angular.forEach(scope.spyElems, function(spy) {
                  spy.active = isInView(spy.elem) ? true : false;
                  spy.children &&
                      angular.forEach(spy.children, function(child) {
                        child.active = isInView(child.elem) ? true : false;
                      });
                });
              });
            });
          };

          // Watch a model value before trigger spy initialization
          // This is required as the scrollspy need the element
          // to be available in the DOM to be initialized.
          if (scope.watch) {
            scope.$watch('watch', function(value) {
              // Wait for the template to render
              // FIXME: may not work properly ?
              $timeout(function() {
                if (scope.spyElems === null) {
                  init();
                }
              }, 200);
            }, true);
          } else {
            init();
          }
        }
      };
    }]);
})();
