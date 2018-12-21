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
  goog.provide('gn_fields_directive');

  goog.require('gn_metadata_manager_service');

  var module = angular.module('gn_fields_directive',
      []);

  /**
   * @ngdoc directive
   * @name gn_fields.directive:gnSlidingWindow
   * @function
   *
   * @description
   * Sliding window encoder for Copernicus Marine
   */
  module.directive('gnSlidingWindowDiv',
    function() {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/edit/partials/' +
        'sliding-window.html',
        scope: {
          value: '@gnSlidingWindowDiv',
          label: '@label',
          ref: '@ref'
        },
        link: function(scope, element, attrs) {
          var token = scope.value.split('/');
          if (token.length > 1) {
            var regex = /-?P{1}(([0-9]*)M{1})?(([0-9]*)D{1})?(T?([0-9]*)H{1})?/;
            var startPeriod = token[0].match(regex);
            var endPeriod = token[1].match(regex);
            scope.sw = {
              startPeriod: token[0].indexOf('-') === 0 ? '-' : '',
              smonths: Math.abs(startPeriod[2]),
              sdays: Math.abs(startPeriod[4]),
              shours: Math.abs(startPeriod[6]),
              endPeriod: token[1].indexOf('-') === 0 ? '-' : '',
              emonths: Math.abs(endPeriod[2]),
              edays: Math.abs(endPeriod[4]),
              ehours: Math.abs(endPeriod[6])
            };
          } else {
            scope.sw = {
              startPeriod: '',
              smonths: '',
              sdays: '',
              shours: '',
              endPeriod: '',
              emonths: '',
              edays: '',
              ehours: ''
            };
          }

          scope.$watchCollection('sw', function (n, o) {
            if (n !== o ){
              var p = [
                scope.sw.startPeriod, 'P',
                scope.sw.smonths || 0, 'M',
                scope.sw.sdays || 0, 'DT',
                scope.sw.shours || 0, 'H',
                '/',
                scope.sw.endPeriod, 'P',
                scope.sw.emonths || 0, 'M',
                scope.sw.edays || 0, 'DT',
                scope.sw.ehours || 0, 'H'
              ];
              scope.value = p.join('');
            }
          });
        }
      };
    });

  module.directive('gnCheckpointCptCovered',
      function() {
        return {
          restrict: 'A',
          templateUrl: '../../catalog/components/edit/partials/' +
              'checkpoint-cpt-not-covered.html',
          scope: {
            id: '@id',
            titleId: '@',
            abstractId: '@'
          },
          link: function(scope, element, attrs) {
            scope.options = [
              {'label': 'Component is covered', 'status': true},
              {'label': 'Component is not covered because existing data not available', 'status': false},
              {'label': 'Component is not covered because data does not exist', 'status': false}
            ];

            var title =  $('#gn-field-' + scope.titleId),
              abs = $('#gn-field-' + scope.abstractId);

            function update() {
              $('#' + scope.id)
                .toggleClass('hidden', scope.covered.status);
              $('#' + scope.id + '-table')
                .toggleClass('hidden', !scope.covered.status);
              if (scope.covered.label) {
                title.val(scope.covered.label);
              }
              if (scope.covered.status) {
                abs.val('');
              }
              if (abs.val() === '') {
                abs.val(scope.covered.status ? '' : '-- Explain why --');
              }
            }


            scope.$watchCollection('covered', function(n, o) {
              if (n !== o) {
                update();
              }
            });

            // Set initial value
            if (title.val() !== '') {
              for(var i = 0; i < scope.options.length; i++) {
                if (scope.options[i].label === title.val()) {
                  scope.covered = scope.options[i];
                  break;
                }
              }
            } else {
              scope.covered = {status: attrs.gnCheckpointCptCovered == 'true'};
            }
            update();
          }
        };
      });

  module.directive('gnCheckpointLineage',
    function() {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/edit/partials/' +
        'checkpoint-lineage.html',
        transclude: true,
        scope: {
          ref: '@'
        },
        link: function(scope, element, attrs) {
          var field = document.gnEditor[scope.ref] || $('#' + scope.ref).get(O);
          scope.isMandatory = true;

          function init() {
            var tokens = field.value.match(/(.*) \((optional|mandatory)\)/);

            // Old style fields or new value from helper
            if (tokens == null) {
              scope.lineage = field.value.trim();
              field.value = buildValue();
            } else {
              scope.isMandatory = tokens.length === 3 ?
                tokens[2] === 'mandatory' : true;
              scope.lineage = tokens.length === 3 ?
                tokens[1].trim() : field.value.trim();
            }
          };

          function buildValue() {
            if (scope.lineage == '') {
              return '';
            } else {
              return scope.lineage +
                ' (' + (scope.isMandatory ? 'mandatory' : 'optional' )+ ')';
            }
          }

          scope.$watch('isMandatory', function (n, o) {
            if (n !== o) {
              field.value = buildValue();
            }
          });

          scope.$watch('lineage', function (n, o) {
            if (n !== o) {
              field.value = buildValue();
            }
          });

          $(field).on('change', init);

          init();
        }
      };
    });
  module.directive('gnTitle',
      ['$http', '$rootScope', '$translate',
       'gnCurrentEdit', 'gnSearchManagerService',
       function($http, $rootScope, $translate,
               gnCurrentEdit, gnSearchManagerService) {
         return {
           restrict: 'C',
           link: function(scope, element, attrs) {
             var titleEl = element.find('div > input[name]');
             var titleCheck = function() {
               // Search by title. Field will be analyzed, so we can't expect
               // to have exact match. There is no capability to exclude current
               // record so we will check if current record is the one currently
               // edited.
               $http.get('q?fast=index&_content_type=json&_title=' +
               titleEl.val(), {cache: true})
               .then(function(r) {
                 var records = gnSearchManagerService.format(r.data);

                 // Don't take pagination in account, we don't expect
                 // to have that many duplicates.
                 if (records.metadata) {
                   var count = 0;
                   for (var i = 0; i < records.metadata.length; i++) {
                     if (records.metadata[i]['geonet:info'].id !==
                     gnCurrentEdit.id) {
                       count++;
                     }
                   }
                   element.toggleClass('has-error', count > 0);
                   if (count > 0) {
                     $rootScope.$broadcast('StatusUpdated', {
                       title: count + $translate.instant('similarRecordsExist'),
                       timeout: 5,
                       type: 'danger'});
                   }
                 }
               });
             };
             titleEl.change(titleCheck);
             titleEl.blur(titleCheck);
           }
         };
       }]);
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
   * @name gn_fields.directive:gnFieldWithPrefixOrSuffix
   * @function
   *
   * @description
   * A field which can hide a prefix or suffix
   * added automatically to an existing value.
   * The field type can also be constrained using fieldType attribute.
   */
  module.directive('gnFieldWithPrefixOrSuffix',
    function() {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          // Using Jquery to parse attribute to preserve
          // leading/trailing space which may have sense
          scope.prefix = element.attr('data-prefix') || '';
          scope.suffix = element.attr('data-suffix') || '';
          var fieldType = attrs['fieldType'] || 'text';

          // Create an input
          var input = $('<input class="form-control" type="' + fieldType + '">');
          // Copy the value without prefix/suffix
          input.val(element.val()
            .replace(scope.prefix, '')
            .replace(scope.suffix, '')).change(function() {
            element.val(scope.prefix + input.val() + scope.suffix);
          });
          element.after(input);
          element.hide();
        }
      };
    });

  /**
   * @ngdoc directive
   * @name gn_fields.directive:gnMeasure
   * @function
   *
   * @description
   * Component to edit a measure type field composed
   * of a numberic value and a unit.
   */
  module.directive('gnMeasure',
      function() {
        return {
          restrict: 'A',
          templateUrl: '../../catalog/components/edit/partials/' +
              'measure.html',
          scope: {
            uom: '@',
            ref: '@'
          },
          link: function(scope, element, attrs) {
            scope.value = parseFloat(attrs['gnMeasure'], 10) || null;

            // Load the config from the textarea containing the helpers
            scope.config =
                angular.fromJson($('#' + scope.ref + '_config')[0].value);
            if (scope.config == null) {
              scope.config = {
                option: []
              };
            }
            // If only one option, convert to an array
            if (!$.isArray(scope.config.option)) {
              scope.config.option = [scope.config.option];
            }
            if (angular.isArray(scope.config)) {
              scope.config.option = scope.config;
            }
            scope.$watch('selected', function(n, o) {
              if (n && n !== o) {
                if (n['@value']) {
                  scope.value = parseFloat(n['@value'], 10);
                }
                if (n['@title']) {
                  scope.uom = n['@title'];
                }
              }
            });
          }
        };
      });
  /**
   * @ngdoc directive
   * @name gn_fields.directive:gnFieldTooltip
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
      ['gnSchemaManagerService', 'gnCurrentEdit', '$compile',
       function(gnSchemaManagerService, gnCurrentEdit, $compile) {

         var iconTemplate =
         "<a class='btn field-tooltip' " +
         "data-ng-show='gnCurrentEdit.displayTooltips'>" +
         "<span class='fa fa-question-circle'></span></a>";

         return {
           restrict: 'A',
           link: function(scope, element, attrs) {
             var isInitialized = false;
             var isField =
             element.is('input') ||
             element.is('textarea') ||
             element.is('select');
             var tooltipTarget = element;
             var iconMode = gnCurrentEdit.displayTooltipsMode === 'icon';
             var isDatePicker = 'gnDatePicker' in attrs;

             var createTooltipForDatePicker = function (el, tooltip) {
               var controlColumn = el.closest(".gn-field").find("div.gn-control");
               if(controlColumn.length > 0) {
                 controlColumn.append(tooltip);
               }
             };

             // use a icon to click on for a tooltip
             if (iconMode) {
               var tooltipAfterLabel = false;
               var tooltipIconCompiled = $compile(iconTemplate)(scope);
               var asideCol;

               if (isField && element.attr('type') !== 'hidden') {

                 if (tooltipAfterLabel) {
                   // try to find the label (with class 'control-label') that
                   // is before this element in the DOM and append the tooltip
                   // button to it.
                   //
                   //  If it's not found,
                   // place the button just before the element
                   asideCol = element.parent('div').prev();

                   if (asideCol.hasClass('control-label')) {
                     asideCol.append(tooltipIconCompiled);
                   } else {
                     element.before(tooltipIconCompiled);
                   }
                 } else {
                   // if element div parent has another
                   // div width gn-control class,
                   // put the tooltip there.
                   asideCol = element.closest('.col-sm-11')
                   .next('div.gn-control');
                   if (asideCol.length > 0) {
                     asideCol.append(tooltipIconCompiled);
                   } else {

                     asideCol = element.closest('.col-sm-9')
                     .next('div.gn-control');

                     if (asideCol.length > 0) {
                       asideCol.append(tooltipIconCompiled);
                     } else {
                       // if element is part of a template snippet,
                       // look for the
                       // previous label and add the icon after it
                       var id = element.attr('id');
                       var re = /^_X[0-9]+_replace_.+$/;
                       if (id && id.match(re)) {
                         var label = element.closest('div')
                         .children('label[for=' + id + ']')
                         .after(tooltipIconCompiled);
                       } else {
                         // Add tooltip after the input element
                         element.after(tooltipIconCompiled);
                       }
                     }
                   }
                 }
               } else if (element.is('legend')) {
                 element.contents().first().after(tooltipIconCompiled);
               } else if (isDatePicker) {
                 element.closest(".gn-field").find("div.gn-control").append(tooltipIconCompiled);
               } else if (element.is('label')) {
                 if (tooltipAfterLabel) {
                   element.parent().children('div')
                   .append(tooltipIconCompiled);
                 } else {
                   element.after(tooltipIconCompiled);
                 }
               }

               // close tooltips on click in editor container
               $('.gn-editor-container').on('mousedown', function(e) {
                 //did not click a popover toggle or popover
                 if ($(e.target).data('toggle') !== 'popover' &&
                 $(e.target).parents('.popover.in').length === 0) {
                   closeTooltips();
                 }
               });

               // replace element with tooltip
               tooltipTarget = tooltipIconCompiled;
             } else {
               element.on('$destroy', function() {
                 element.off();
               });
             }

             var closeTooltips = function() {
               // Close all tooltips/popovers
               // (there still might be some open)
               $('.popover').popover('hide');
               // Less official way to hide
               $('.popover').hide();
             };

             var initTooltip = function(event) {
               if (!isInitialized && gnCurrentEdit.displayTooltips) {
                 // Retrieve field information (there is a cache)
                 gnSchemaManagerService
                  .getElementInfo(attrs.gnFieldTooltip)
                 .then(function(data) {
                   var info = data;
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

                     // Only one tooltip visible at a time
                     if (iconMode) {
                       closeTooltips();
                     }

                     // Add description to the body html
                     html += '<p>' + info.description + '</p>';

                     // Right same width as field
                     // For legend, popover is right
                     // Bottom is not recommended when typeahead
                     var placement = attrs.placement || 'right';

                     // TODO : improve. Here we fix the width
                     // to the remaining space between the element
                     // and the right window border.
                     var width = ($(window).width() -
                     tooltipTarget.offset().left -
                     tooltipTarget.outerWidth()) * .95;

                     var closeBtn = '<button onclick="$(this).' +
                     'closest(\'div.popover\').remove();" type="button" ' +
                     'class="fa fa-times btn btn-link pull-right"></button>';

                     tooltipTarget.popover({
                       title: info.label,
                       container: 'body',
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


                     if (event === 'click' && !isField) {
                       tooltipTarget.click('show');
                     } else {
                       tooltipTarget.focus();
                     }

                     if (iconMode) {
                       tooltipTarget.mouseleave(function() {
                         tooltipTarget.popover('hide');
                         isInitialized = false;
                       });
                     }


                     tooltipTarget.on('shown.bs.popover', function(event) {
                       // move popover under navbar in case they are
                       // above visible area.
                       if ($('div.popover').css('top') &&
                           $('div.popover').css('top').charAt(0) === '-') {
                         var oldTopPopover = $('div.popover').position().top;
                         var newTopPopover =
                         $(".navbar:not('.ng-hide')").outerHeight() + 5;
                         var oldTopArrow = $('.popover>.arrow').position().top;
                         $('div.popover').css('top', newTopPopover);
                         $('.popover>.arrow').css('top',
                         oldTopArrow - newTopPopover + oldTopPopover);
                       }
                     });

                     tooltipTarget.popover('show');
                     isInitialized = true;
                   }
                 });
               }
             };

             // On hover trigger the tooltip init
             if (iconMode) {
               tooltipTarget.hover(function(event) {
                 event.stopPropagation;
               });
               tooltipTarget.hover(function() {
                 initTooltip('hover');
               });
             } else {
               if (isField) {
                 tooltipTarget.focus(function() {
                   initTooltip('focus');
                 });
               } else {
                 tooltipTarget.click(function() {
                   initTooltip('hover');
                 });
               }
             }
           }
         };
       }]);

  /**
   * @ngdoc directive
   * @name gn_fields.directive:gnEditorControlMove
   *
   * @description
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
   * @ngdoc directive
   * @name gn_fields.directive:gnFieldHighlightRemove
   *
   * @description
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
   * @ngdoc directive
   * @name gn_fields.directive:gnFieldHighlight
   *
   * @description
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
            element.find('a').has('.fa-times.text-danger')
                .css('visibility', 'visible');
          });
          element.on('mouseout', function() {
            element.removeClass('field-bg');
            element.find('a').has('.fa-times.text-danger')
                .css('visibility', 'hidden');
          });
        }
      };
    }]);

})();
