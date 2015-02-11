(function() {
  goog.provide('gn_fields_directive');

  goog.require('gn_metadata_manager_service');

  var module = angular.module('gn_fields_directive',
      []);

  /**
   * Note: ng-model and angular checks could not be applied to
   * the editor form as it would require to init the model
   * from the form content using ng-init for example.
   */
  module.directive('gnCheck',
      function() {
        return {
          restrict: 'A',
          link: function(scope, element, attrs) {

            // Required attribute
            if (attrs.required) {
              element.keyup(function() {
                if ($(this).get(0).value == '') {
                  $(attrs.gnCheck).addClass('has-error');
                } else {
                  $(attrs.gnCheck).removeClass('has-error');
                }
              });
              element.keyup();
            }
          }
        };
      });

  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnFieldTooltip
   * @function
   *
   * @description
   * Initialized field label or fieldset legend tooltip
   * based on the tooltip configuration.
   *
   * If the element is input or textarea, the event to open
   * the tooltip is on focus, if not, the event is on click.
   *
   * @param {string} gnFieldTooltip The tooltip configuration
   *  which identified the schema, the element name and optionally the
   *  element parent name and XPath. eg. 'iso19139|gmd:fileIdentifier'.
   *
   * @param {string} placement Tooltip placement. Default to 'bottom'
   * See {@link http://getbootstrap.com/javascript/#tooltips}
   *
   *
   * @example
  <example>
    <file name="index.html">
      <label for="gn-field-3"
         data-gn-field-tooltip="iso19139|gmd:fileIdentifier|gmd:MD_Metadata|
         /gmd:MD_Metadata/gmd:fileIdentifier"
         data-placement="left">File identifier</label>
   </file>
   </example>
   */
  module.directive('gnFieldTooltip',
      ['gnSchemaManagerService', 'gnCurrentEdit',
       function(gnSchemaManagerService, gnCurrentEdit) {
         return {
           restrict: 'A',
           link: function(scope, element, attrs) {
             var isInitialized = false;
             var isField =
             element.is('input') || element.is('textarea');

             element.on('$destroy', function() {
               element.off();
             });

             var initTooltip = function(event) {
               if (!isInitialized && gnCurrentEdit.displayTooltips) {
                 // Retrieve field information (there is a cache)
                 gnSchemaManagerService
                  .getElementInfo(attrs.gnFieldTooltip).then(function(data) {
                   var info = data[0];
                   if (info.description && info.description.length > 0) {
                     // Initialize tooltip when description returned
                     var html = '';

                     // TODO: externalize in a template.
                     if (angular.isArray(info.help)) {
                       angular.forEach(info.help, function(helpText) {
                         if (helpText['@for']) {
                           html += helpText['#text'];
                         } else {
                           html += helpText;
                         }
                       });
                     } else if (info.help) {
                       html += info.help;
                     }


                     // Right same width as field
                     // For legend, popover is right
                     // Bottom is not recommended when typeahead
                     var placement = attrs.placement || 'right';

                     // TODO : improve. Here we fix the width
                     // to the remaining space between the element
                     // and the right window border.
                     var width = ($(window).width() -
                         element.offset().left -
                         element.outerWidth()) * .95;

                     var closeBtn = '<button onclick="$(this).' +
                     'closest(\'div.popover\').prev().' +
                     'popover(\'hide\');" type="button" ' +
                     'class="fa fa-times btn btn-link pull-right"></button>';

                     element.popover({
                       title: info.description,
                       content: html,
                       html: true,
                       placement: placement,
                       template: '<div class="popover gn-popover" ' +
                       'style="max-width:' + width + 'px;' +
                       'width:' + width + 'px"' +
                       '>' +
                       '<div class="arrow">' +
                       '</div><div class="popover-inner">' + closeBtn +
                       '<h3 class="popover-title"></h3>' +
                       '<div class="popover-content"><p></p></div></div></div>',
                       //                       trigger: 'click',
                       trigger: isField ? 'focus' : 'click'
                     });

                     //                     if (event === 'hover' && !isField) {
                     //                       element.popover('show');
                     //                     } else
                     if (event === 'click' && !isField) {
                       element.click('show');
                     } else {
                       element.focus();
                     }
                     isInitialized = true;
                   }
                 });
               }
             };

             // On hover trigger the tooltip init
             if (isField) {
               element.focus(function() {
                 initTooltip('focus');
               });
             } else {
               element.click(function() {
                 initTooltip('hover');
               });
             }
           }
         };
       }]);

  /**
   * Move an element up or down. If direction
   * is not defined, direction is down.
   */
  module.directive('gnEditorControlMove', ['gnEditor',
    function(gnEditor) {
      return {
        restrict: 'A',
        scope: {
          ref: '@gnEditorControlMove',
          domelementToMove: '@',
          direction: '@'
        },
        link: function(scope, element, attrs) {

          element.on('$destroy', function() {
            element.off();
          });

          $(element).click(function() {
            gnEditor.move(scope.ref, scope.direction || 'down',
                scope.domelementToMove);
          });
        }
      };
    }]);

  /**
   * Add a danger class to the element about
   * to be removed by this action
   */
  module.directive('gnFieldHighlightRemove', [
    function() {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          var ref = attrs['gnFieldHighlightRemove'],
              target = $('#gn-el-' + ref);

          element.on('mouseover', function(e) {
            target.addClass('text-danger');
          });
          element.on('mouseout', function() {
            target.removeClass('text-danger');
          });
        }
      };
    }]);

  /**
   * Highlight an element by adding field-bg class
   * and looking for all remove button to make them
   * visible.
   */
  module.directive('gnFieldHighlight', [
    function() {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {

          element.on('mouseover', function(e) {
            e.stopPropagation();
            // TODO: This may need improvements
            // on touchscreen delete action will not be visible

            element.addClass('field-bg');
            element.find('i.btn.fa-times.text-danger')
              .css('visibility', 'visible');
          });
          element.on('mouseout', function() {
            element.removeClass('field-bg');
            element.find('i.btn.fa-times.text-danger')
              .css('visibility', 'hidden');
          });
        }
      };
    }]);

})();
