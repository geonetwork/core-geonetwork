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
  goog.provide('gn_utility_directive');


  var module = angular.module('gn_utility_directive', []);

  module.directive('gnRecordOriginLogo', [
    'gnConfig', 'gnConfigService', 'gnGlobalSettings', '$http',
    function(gnConfig, gnConfigService, gnGlobalSettings, $http) {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          md: '=gnRecordOriginLogo'
        },
        templateUrl: '../../catalog/components/utility/' +
          'partials/recordOriginLogo.html',
        link: function(scope, element, attrs) {
          gnConfigService.load().then(function(c) {
            scope.recordGroup = null;
            scope.gnUrl = gnGlobalSettings.gnUrl;
            scope.isPreferGroupLogo = gnConfig['system.metadata.prefergrouplogo'];

            function getRecordGroup() {
              if (scope.md
                && scope.md.groupOwner) {
                $http.get('../api/groups/' + scope.md.groupOwner,
                  {cache: true}).success(function (data) {
                  scope.recordGroup = data;
                });
              }
            }

            scope.$watch('md', getRecordGroup);
          });
        }
      };
    }
  ]);

  module.directive('gnConfirmClick', [
    function() {
      return {
        priority: -1,
        restrict: 'A',
        link: function(scope, element, attrs) {
          element.bind('click', function(e) {
            var message = attrs.gnConfirmClick;
            if (message && !confirm(message)) {
              e.stopImmediatePropagation();
              e.preventDefault();
            }
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnCountryPicker
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   * Use the region API to retrieve the list of
   * Country.
   *
   * TODO: This could be used in other places
   * probably. Move to another common or language module ?
   */
  module.directive('gnCountryPicker', ['$http',
    function($http) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          element.attr('placeholder', '...');
          element.on('focus', function() {
            $http.get('../api/regions?categoryId=' +
              'http%3A%2F%2Fwww.naturalearthdata.com%2Fne_admin%23Country',
              {}, {
                cache: true
              }).success(function(response) {
              var data = response.region;

              // Compute default name and add a
              // tokens element which is used for filter
              angular.forEach(data, function(country) {
                country.tokens = [];
                angular.forEach(country.label, function(label) {
                  country.tokens.push(label);
                });
                country.name = country.label[scope.lang];
              });
              var source = new Bloodhound({
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                local: data,
                limit: 30
              });
              source.initialize();
              $(element).typeahead({
                minLength: 0,
                highlight: true
              }, {
                name: 'countries',
                displayKey: 'name',
                source: source.ttAdapter()
              }).on('typeahead:selected', function(event, datum) {
                if (angular.isFunction(scope.onRegionSelect)) {
                  scope.onRegionSelect(datum);
                }
              });
            });
            element.unbind("focus")
          });
        }
      };
    }]);

  module.directive('gnRegionPicker', ['gnRegionService',
    function(gnRegionService) {
      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/components/utility/' +
            'partials/regionpicker.html',
        link: function(scope, element, attrs) {
          scope.gnRegionService = gnRegionService;

          var addGeonames = !attrs['disableGeonames'];
          scope.regionTypes = [];

          function setDefault() {
            var defaultThesaurus = attrs['default'];
            for (t in scope.regionTypes) {
              if (scope.regionTypes[t].name === defaultThesaurus) {
                scope.regionType = scope.regionTypes[t];
                return;
              }
            }
            scope.regionType = scope.regionTypes[0];
          }

          /**
           * Load list on init to fill the dropdown
           */
          gnRegionService.loadList().then(function(data) {
            scope.regionTypes = angular.copy(data);
            if (addGeonames) {
              scope.regionTypes.unshift({
                name: 'Geonames',
                id: 'geonames'
              });
            }
            setDefault();
          });

          scope.setRegion = function(regionType) {
            scope.regionType = regionType;
            // clear the input field
            scope.resetRegion();
          };
        }
      };
    }]);

  module.directive('gnUserPicker', ['$http',
    function($http) {
      return {
        restrict: 'A',
        scope: {
          user: '=gnUserPicker'
        },
        link: function(scope, element, attrs) {
          element.attr('placeholder', '...');
          // TODO: Add by profile and by group
          $http.get('../api/users',
            {}, {
              cache: true
            }).then(function(r) {
            // var data = data;
            var source = new Bloodhound({
              datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
              queryTokenizer: Bloodhound.tokenizers.whitespace,
              local: r.data,
              identify: function(obj) { return obj.username; },
              limit: 30
            });

            function sourceWithDefaults(q, sync) {
              if (q === '') {
                sync(source.all());
              } else {
                source.search(q, sync);
              }
            }

            source.initialize();
            $(element).typeahead({
              minLength: 0,
              highlight: true
            }, {
              displayKey: 'username',
              templates: {
                suggestion: function(datum) {
                  return '<p>' + datum.name + ' ' + datum.surname +
                         ' (' + datum.profile + ')</p>';
                }
              },
              source: sourceWithDefaults
            }).on('typeahead:selected', function(event, datum) {
              scope.user = datum;
            });
          });
        }
      };
    }]);

  module.directive('gnBatchReport', [
    function() {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          processReport: '=gnBatchReport'
        },
        templateUrl: '../../catalog/components/utility/' +
            'partials/batchreport.html',
        link: function(scope, element, attrs) {
          scope.$watch('processReport', function(n, o) {
            if (n && n != o) {
              scope.processReportWarning = n.notFound != 0 ||
                  n.notOwner != 0 ||
                  n.notProcessFound != 0 ||
                  n.metadataErrorReport.metadataErrorReport.length != 0;
            }
          });
        }
      };
    }]);

  module.directive('gnDuplicateCheck', ['$translate', '$http', '$q',
    function($translate, $http, $q) {
      return {
        restrict: 'A',
        require: 'ngModel',
        scope: {
          value: '=gnDuplicateCheck',
          list: '=gnDuplicateCheckList',
          apply: '=gnDuplicateCheckApply',
          remote: '@gnDuplicateCheckRemote',
          property: '@gnDuplicateCheckProperty'
        },
        link: function(scope, element, attrs, ngModel) {
          var cssClass = 'gn-duplicate';
          if (!angular.isArray(scope.list) && scope.remote === undefined) {
            console.warn('gnDuplicateCheck need an array of values for the list or a remote URL.')
            return;
          }

          if (angular.isArray(scope.list)) {
            var existingValues = scope.property ? [] : scope.list;
            if (scope.property) {
              var path = scope.property.split('.');
              for (var i = 0; i < scope.list.length; i++) {
                var v = scope.list[i];
                if (angular.isObject(v)) {
                  for (var j = 0; j < path.length; j++) {
                    v = v[path[j]];
                    existingValues.push(v);
                  }
                }
              }
            }
          }

          ngModel.$asyncValidators.gnDuplicateCheck = function(value, viewValue) {
            value = value || viewValue;
            if (scope.apply === false) {
              return $q.when(true);
            }
            if (angular.isArray(existingValues)) {
              if (existingValues.indexOf(value) !== -1) {
                ngModel.$setValidity(cssClass, false);
                return $q.reject(false);
              } else {
                return $q.when(true);
              }
            } else if (scope.remote) {
              var deferred = $q.defer();

              // Promise server side check
              $http.get(scope.remote.replace('{value}', value)).then(function (r){
                if (r.status !== 404) {
                  ngModel.$setValidity(cssClass, false);
                  deferred.reject(false);
                } else {
                  ngModel.$setValidity(cssClass, true);
                  deferred.resolve(true);
                }
              }, function (e){
                ngModel.$setValidity(cssClass, true);
                deferred.resolve(true);
              });

              return deferred.promise;
            }
          };
        }
      };
    }]);

  /**
   * Region picker coupled with typeahead.
   * scope.region will tell what kind of region to load
   * (country, ocean, continent), inherited from parent scope.
   * But you can also set it to use the directive in an
   * independent way by passing the attribute gn-region.
   *
   * Specify a scope.onRegionSelect function if you want
   * to catch event from selection.
   */
  module.directive('gnRegionPickerInput', [
    'gnRegionService', 'gnUrlUtils', 'gnGlobalSettings',
    'gnViewerSettings',
    function(gnRegionService, gnUrlUtils, gnGlobalSettings,
        gnViewerSettings) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {

          if (attrs['gnRegionType']) {
            gnRegionService.loadList().then(function(data) {
              for (i = 0; i < data.length; ++i) {
                if (attrs['gnRegionType'] == data[i].name) {
                  scope.regionType = data[i];
                }
              }
            });
          }
          scope.$watch('regionType', function(val) {
            if (scope.regionType) {
              if (scope.regionType.id == 'geonames') {
                $(element).typeahead('destroy');
                var url = gnViewerSettings.geocoder;
                url = gnUrlUtils.append(url, gnUrlUtils.toKeyValue({
                  lang: scope.lang,
                  style: 'full',
                  type: 'json',
                  maxRows: 10,
                  name_startsWith: 'QUERY',
                  username: 'georchestra'
                }));

                url = gnGlobalSettings.proxyUrl + encodeURIComponent(url);

                var autocompleter = new Bloodhound({
                  datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
                  queryTokenizer: Bloodhound.tokenizers.whitespace,
                  limit: 30,
                  remote: {
                    wildcard: 'QUERY',
                    url: url,
                    ajax: {
                      beforeSend: function() {
                        scope.regionLoading = true;
                        scope.$apply();
                      },
                      complete: function() {
                        scope.regionLoading = false;
                        scope.$apply();
                      }
                    },
                    filter: function(data) {
                      return data.geonames;
                    }
                  }
                });
                autocompleter.initialize();
                $(element).typeahead({
                  minLength: 1,
                  highlight: true
                }, {
                  name: 'places',
                  displayKey: 'name',
                  source: autocompleter.ttAdapter(),
                  templates: {
                    suggestion: function(loc) {
                      var props = [];
                      ['adminName1', 'countryName'].
                          forEach(function(p) {
                            if (loc[p]) { props.push(loc[p]); }
                          });
                      return '<div>' + loc.name + ((props.length == 0) ? '' :
                          ' — <em>' + props.join(', ') + '</em></div>');
                    }
                  }

                }).on('typeahead:selected', function(event, datum) {
                  if (angular.isFunction(scope.onRegionSelect)) {
                    scope.onRegionSelect(datum);
                  }
                });
              }
              else {
                gnRegionService.loadRegion(scope.regionType, scope.lang).then(
                    function(data) {
                      if (data) {
                        $(element).typeahead('destroy');
                        var source = new Bloodhound({
                          datumTokenizer:
                              Bloodhound.tokenizers.obj.whitespace('name'),
                          queryTokenizer: Bloodhound.tokenizers.whitespace,
                          local: data
                        });
                        source.initialize();

                        function allOrSearchFn(q, sync) {
                          if (q === '') {
                            sync(source.all());
                            // This is the only change needed to get 'ALL'
                            // items as the defaults
                          } else {
                            source.search(q, sync);
                          }
                        }

                        $(element).typeahead({
                          minLength: 0,
                          highlight: true
                        }, {
                          name: 'countries',
                          displayKey: 'name',
                          limit: 100,
                          source: allOrSearchFn
                        }).on('typeahead:selected', function(event, datum) {
                          if (angular.isFunction(scope.onRegionSelect)) {
                            scope.onRegionSelect(datum);
                          }
                        });
                      }
                    });
              }
            }
          });
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnLanguagePicker
   * @function
   *
   * @description
   * Use the lang service to retrieve the list of
   * ISO language available and provide autocompletion
   * for the input field with that directive attached.
   *
   * TODO: This could be used in other places
   * like admin > harvesting > OGC WxS
   * probably. Move to another common or language module ?
   */
  module.directive('gnLanguagePicker', ['$http',
    function($http) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          scope.prefix = attrs['prefix'] || '';
          element.attr('placeholder', '...');
          element.on('focus', function() {
            $http.get('../api/isolanguages', {}, {
              cache: true
            }).success(function(data) {
              // Compute default name and add a
              // tokens element which is used for filter
              angular.forEach(data, function(lang) {
                var defaultName = lang.label['eng'];
                lang.name = lang.label[scope.lang] || defaultName;
                lang.code = scope.prefix + lang.code;
                lang.tokens = [lang.name, lang.code, defaultName];
              });
              var source = new Bloodhound({
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                local: data,
                limit: 30
              });
              source.initialize();
              $(element).typeahead({
                minLength: 0,
                highlight: true
              }, {
                name: 'isoLanguages',
                displayKey: 'code',
                source: source.ttAdapter(),
                templates: {
                  suggestion: function(datum) {
                    return '<p>' + datum.name + ' (' + datum.code + ')</p>';
                  }
                }
              });
            });
            element.unbind("focus")
          })
        }
      };
    }]);

  module.directive('gnHumanizeTime', ['gnGlobalSettings', 'gnHumanizeTimeService',
    function(gnGlobalSettings, gnHumanizeTimeService) {
      return {
        restrict: 'A',
        template: '<span title="{{title}}">{{value}}</span>',
        scope: {
          date: '@gnHumanizeTime',
          format: '@',
          fromNow: '@'
        },
        link: function linkFn(scope, element, attr) {
          var useFromNowSetting = gnGlobalSettings.gnCfg.mods.global.humanizeDates,
              format = gnGlobalSettings.gnCfg.mods.global.dateFormat;
          scope.$watch('date', function(originalDate) {
            if (originalDate) {
              var attempt = gnHumanizeTimeService(originalDate,
                scope.format || format,
                scope.fromNow !== undefined)
              if (attempt !== undefined) {
                scope.value = attempt.value;
                scope.title = attempt.title;
              }
            }
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnCopyToClipboard
   * @function
   *
   * @description
   * Put a string in a input field with copy to clipboard functions attached to it.
   *
   * The code to be used in a HTML page:
   *
   * <span gn-copy-to-clipboard="{{r.url | gnLocalized: lang}}"></span>
   *
   * or
   *
   * <span gn-copy-to-clipboard="{{r.url | gnLocalized: lang}}" gn-copy-button-only="true"></span>
   *
   * The first option displays an input and copy button. Copying the text to the clipboard is triggered by
   * clicking on the button or in the input.
   *
   * The second option only displays the copy button (in case the input is not needed). The input is
   * moved out of sight, because for copying you need an input (or textarea)
   */
  module.directive('gnCopyToClipboard', ['gnAlertService','$translate',
    function (gnAlertService,$translate) {
      return {
        restrict: 'A',
        scope: {
          copytext: '@gnCopyToClipboard',
          buttononly: '@gnCopyButtonOnly'
        },
        templateUrl: '../../catalog/components/utility/' +
          'partials/copytoclipboard.html',
        link: function(scope, element, attr) {

          var target = element.find('input:first');
          scope.moveinput = (scope.buttononly === 'true');

          scope.copyToClipboard = function() {
            // select the current focus
            var currentFocus = document.activeElement;

            // set focus on the text input
            target.focus();
            target[0].setSelectionRange(0, target.val().length);

            // copy the selection
            var succeed;

            try {
              succeed = document.execCommand("copy");
            } catch (e) {
              console.warn(e);
              succeed = false;
            }

            // Restore original focus
            if (currentFocus && typeof currentFocus.focus === "function") {
              currentFocus.focus();
            }

            if (succeed) {
              gnAlertService.addAlert({
                msg: $translate.instant('textCopied'),
                delay: 5000,
                type: 'success'});
            }

            return succeed;
          };
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnMetadataPicker
   * @function
   *
   * @description
   * Use the search service
   * to retrieve the list of entry available and provide autocompletion
   * for the input field with that directive attached.
   *
   */
  module.directive('gnMetadataPicker',
      ['gnUrlUtils', 'gnSearchManagerService',
       function(gnUrlUtils, gnSearchManagerService) {
         return {
           restrict: 'A',
           link: function(scope, element, attrs) {
             element.attr('placeholder', '...');
             var displayField = attrs['displayField'] || 'resourceTitle';
             var valueField = attrs['valueField'] || displayField;
             var params = angular.fromJson(element.attr('params') || '{}');

             var url = gnUrlUtils.append('q?_content_type=json',
              gnUrlUtils.toKeyValue(angular.extend({
               isTemplate: 'n',
               any: '*QUERY*',
               sortBy: 'resourceTitleObject.default.keyword'
             }, params)
              )
             );
             var parseResponse = function(data) {
               var records = gnSearchManagerService.format(data);
               return records.metadata;
             };
             var source = new Bloodhound({
               datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
               queryTokenizer: Bloodhound.tokenizers.whitespace,
               limit: 200,
               remote: {
                 wildcard: 'QUERY',
                 url: url,
                 filter: parseResponse
               }
             });
             source.initialize();
             $(element).typeahead({
               minLength: 0,
               highlight: true
             }, {
               name: 'metadata',
               displayKey: function(data) {
                 if (valueField === 'uuid') {
                   return data.uuid;
                 } else {
                   return data[valueField];
                 }
               },
               source: source.ttAdapter(),
               templates: {
                 suggestion: function(datum) {
                   return '<p>' + datum[displayField] + '</p>';
                 }
               }
             });
           }
         };
       }]);

  /**
   * @name gn_utility.directive:gnClickToggle
   * @function
   *
   * @description
   * Trigger an event (default is click) of all element matching
   * the gnSectionToggle selector. By default, all elements
   * matching form > fieldset > legend[data-gn-slide-toggle]
   * ie. first level legend are clicked.
   *
   * This is usefull to quickly collapse all section in the editor.
   *
   * Add the event attribute to define a custom event.
   */
  module.directive('gnToggle', [
    function() {
      return {
        restrict: 'A',
        template: '<button title="{{\'gnToggle\' | translate}}">' +
            '<i class="fa fa-fw fa-angle-double-up"/>&nbsp;' +
            '</button>',
        link: function linkFn(scope, element, attr) {
          var selector = attr['gnSectionToggle'] ||
              'form > fieldset > legend[data-gn-slide-toggle]',
              event = attr['event'] || 'click';
          element.on('click', function() {
            $(selector).each(function(idx, elem) {
              $(elem).trigger(event);
            });
            $(this).find('i').toggleClass(
                'fa-angle-double-up fa-angle-double-down');
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnDirectoryEntryPicker
   * @function
   *
   * @description
   * Use the directory (aka subtemplate) search service
   * to retrieve the list of entry available and provide autocompletion
   * for the input field with that directive attached.
   *
   */
  module.directive('gnDirectoryEntryPicker',
      ['gnUrlUtils', 'gnSearchManagerService',
       function(gnUrlUtils, gnSearchManagerService) {
         return {
           restrict: 'A',
           link: function(scope, element, attrs) {
             element.attr('placeholder', '...');

             var url = gnUrlUtils.append('q@json',
              gnUrlUtils.toKeyValue({
                isTemplate: 's',
                any: '*QUERY*',
                root: 'gmd:CI_ResponsibleParty',
                sortBy: 'resourceTitleObject.default.keyword',
                sortOrder: '',
                resultType: 'subtemplates'
              })
             );
             var parseResponse = function(data) {
               var records = gnSearchManagerService.format(data);
               return records.metadata;
             };
             var source = new Bloodhound({
               datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
               queryTokenizer: Bloodhound.tokenizers.whitespace,
               limit: 200,
               remote: {
                 wildcard: 'QUERY',
                 url: url,
                 filter: parseResponse
               }
             });
             source.initialize();
             $(element).typeahead({
               minLength: 0,
               highlight: true
             }, {
               name: 'directoryEntry',
               displayKey: 'title',
               source: source.ttAdapter(),
               templates: {
                 suggestion: function(datum) {
                   return '<p>' + datum.title + '</p>';
                 }
               }
             });
           }
         };
       }]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnAutogrow
   * @function
   *
   * @description
   * Adjust textarea size onload and when text change.
   *
   * Source: Comes from grunt ngdoc example.
   */
  module.directive('gnAutogrow', function() {
    // add helper for measurement to body
    var testObj = angular.element('<textarea ' +
        ' style="height: 0px; position: ' +
        'absolute; top: 0; visibility: hidden;"/>');
    angular.element(window.document.body).append(testObj);

    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var maxHeight = 1000;
        var defaultWidth = 400;
        var adjustHeight = function() {
          // Height is computed based on scollHeight from
          // the testObj. Max height is 1000px.
          // Width is set to the element width
          // or its parent if hidden (eg. multilingual field
          // on load).
          var height,
              width = element.is(':hidden') ?
              element.parent().width() || defaultWidth :
              element[0].clientWidth;
          testObj.css('width', width + 'px').val(element.val());
          height = Math.min(testObj[0].scrollHeight, maxHeight);
          element.css('height', height + 18 + 'px');
        };

        // adjust on load
        adjustHeight();

        // adjust on model change.
        // There is no model here. scope.$watch(attrs.ngModel, adjustHeight);

        // model value is trimmed so adjust on enter, space, delete too
        element.bind('keyup', function(event) {
          var key = event.keyCode;
          if (key === 13 || key === 32 || key === 8 || key === 46) {
            adjustHeight();
          }
        });
        // insert only returns & spaces and delete per
        // context menu is not covered;
      }
    };
  });


  /**
   * Make an element able to collapse/expand
   * the next element. An icon is added before
   * the element to indicate the status
   * collapsed or expanded.
   */
  module.directive('gnSlideToggle', ["$timeout",
    function($timeout) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          element.on('click', function(e) {
            /**
             * Toggle collapse-expand fieldsets
             * TODO: This is in conflict with click
             * event added by field tooltip
             */
            var legend = $(this);
            //getting the next element
            var content = legend.nextAll();
            //open up the content needed - toggle the slide-
            //if visible, slide up, if not slidedown.
            content.slideToggle(attrs.duration || 250, function() {
              //execute this after slideToggle is done
              //change the icon of the legend based on
              // visibility of content div
              if (content.is(':visible')) {
                legend.removeClass('collapsed');
              } else {
                legend.addClass('collapsed');
              }
            });
          });
          if (attrs['gnSlideToggle'] == 'true') {
            $timeout(function() {
                 element.click();
            },0); //this needs to be done after the DOM is updated
          }
        }
      };
    }]);

  module.directive('gnClickAndSpin', ['$parse',
    function($parse) {
      return {
        restrict: 'A',
        compile: function(scope, element, attr) {
          var fn = $parse(element['gnClickAndSpin'], null, true);
          return function ngEventHandler(scope, element, attr) {
            var running = false;
            var icon = element.find('i');
            var spinner = null;
            var start = function() {
              running = true;
              element.addClass('running');
              element.addClass('disabled');
              icon.addClass('hidden');
              spinner = element.
                  prepend('<i class="fa fa-fw fa-spinner fa-spin"></i>');
            };
            var done = function() {
              running = false;
              element.removeClass('running');
              var stayDisabled = attr['gnClickAndSpinStayDisabled'];
              if (!stayDisabled) {
                element.removeClass('disabled');
              }
              element.find('i').first().remove();
              icon.removeClass('hidden');
            };

            element.on('click', function(event) {
              start();
              var callback = function() {
                return fn(scope, {$event: event});
              };
              // Available on ng-click - not sure if we may use it
              //if (forceAsyncEvents[eventName] && $rootScope.$$phase) {
              //  scope.$evalAsync(callback);
              //} else {
              try {
                callback().then(function() {
                  done();
                }, function() {
                  done();
                });
              }
              catch (e) {
                done();
              }
              //if (angular.isFunction(callback.then)) {
              //  callback().then(function() {
              //    done();
              //  });
              //} else {
              //  scope.$apply(callback);
              //  done();
              //}
            });
          };
        }
      };
    }]);

  module.directive('gnFocusOn', ['$timeout', function($timeout) {
    return {
      restrict: 'A',
      link: function($scope, $element, $attr) {
        $scope.$watch($attr.gnFocusOn, function(o, n) {
          if (o != n) {
            $timeout(function() {
              o ? $element.focus() :
                  $element.blur();
            });
          }
        });
      }
    };
  }]);

  /**
   * Use to initialize bootstrap datepicker
   * Can handle two pickers to select a range
   * The change callback will be called when the value is updated from the
   * calendar component. When modified from outside, the internal value of the
   * picker will be updated accordingly so that the calendar stays in sync.
   */
  module.directive('gnBootstrapDatepicker', ['$timeout', 'gnLangs',
    function($timeout, gnLangs) {

      // to MM-dd-yyyy
      var formatDate = function(day, month, year) {
        return ('0' + day).slice(-2) + '-' +
            ('0' + month).slice(-2) + '-' + year;
      };

      var getMaxInProp = function(obj) {
        var year = {
          min: 3000,
          max: -1
        };
        var month = {
          min: 12,
          max: -1
        };
        var day = {
          min: 32,
          max: -1
        };

        for (var k in obj) {
          k = parseInt(k);
          if (k < year.min) year.min = k;
          if (k > year.max) year.max = k;
        }
        for (k in obj[year.min]) {
          k = parseInt(k);
          if (k < month.min) month.min = k;
        }
        for (k in obj[year.max]) {
          k = parseInt(k);
          if (k > month.max) month.max = k;
        }
        for (k in obj[year.min][month.min]) {
          k = parseInt(k);
          if (obj[year.min][month.min][k] < day.min) {
            day.min = obj[year.min][month.min][k];
          }
        }
        for (k in obj[year.max][month.max]) {
          k = parseInt(k);

          if (obj[year.min][month.min][k] > day.max) {
            day.max = obj[year.min][month.min][k];
          }
        }

        return {
          min: formatDate(day.min, month.min + 1, year.min),
          max: formatDate(day.max, month.max + 1, year.max)
        };
      };

      // check that the date is in dd-mm-yyyy string format
      function isDateValid(date) {
        return moment(date, 'DD-MM-YYYY', true).isValid();
      }

      return {
        restrict: 'A',
        scope: {
          date: '=gnBootstrapDatepicker',
          dates: '=dateAvailable',
          config: '=config',
          onChangeFn: '&?'
        },
        link: function(scope, element, attrs) {

          var available, limits;
          var rendered = false;
          var isRange = ($(element).find('input').length == 2);
          var highlight = attrs['dateOnlyHighlight'] === 'true';

          // TODO: handle available dates change?
          // scope.$watch('dates', function(dates, old) {
          // });

          var init = function() {
            var hasBounds = scope.config && (scope.config.dateMin || scope.config.dateMax);
            if (hasBounds) {
              limits = {};
              if (scope.config.dateMin) {
                limits.min = new Date(scope.config.dateMin);
              }
              if (scope.config.dateMax) {
                limits.max = new Date(scope.config.dateMax);
              }
            }
            // if dates is specified it overrides the min/max params
            if (scope.dates) {
              // Time epoch
              if (angular.isArray(scope.dates) &&
                  Number.isInteger(scope.dates[0])) {

                limits = {
                  min: new Date(Math.min.apply(null, scope.dates)),
                  max: new Date(Math.max.apply(null, scope.dates))
                };

                scope.times = scope.dates.map(function(time) {
                  return moment(time).format('YYYY-MM-DD');
                });

                available = function(date) {
                  return scope.times.indexOf(
                      moment(date).format('YYYY-MM-DD')) >= 0;
                };
              }

              // ncwms dates object (year/month/day)
              else if (angular.isObject(scope.dates)) {

                limits = getMaxInProp(scope.dates);

                available = function(date) {
                  if (scope.dates[date.getFullYear()] &&
                      scope.dates[date.getFullYear()][date.getMonth()] &&
                      $.inArray(date.getDate(),
                      scope.dates[date.getFullYear()][date.getMonth()]) != -1) {
                    return true;
                  } else {
                    return false;
                  }
                };
              }
            }

            if (rendered) {
              $(element).datepicker('destroy');
            }

            var datepickConfig = angular.extend({
              container: typeof sxtSettings != 'undefined' ?
                  '.g' : 'body',
              autoclose: true,
              keepEmptyValues: true,
              clearBtn: true,
              todayHighlight: false,
              language: gnLangs.getIso2Lang(gnLangs.getCurrent())
            }, scope.config);

            // apply range and limits if defined
            if (angular.isDefined(scope.dates)) {
              angular.extend(datepickConfig, {
                beforeShowDay: function(dt, a, b) {
                  var isEnable = available(dt);
                  return highlight ? (isEnable ? 'gn-date-hl' : undefined) :
                      isEnable;
                },
                startDate: limits.min,
                endDate: limits.max
              });
            }
            // only display available dates if either min or max is specified
            if (hasBounds) {
              angular.extend(datepickConfig, {
                startDate: limits.min,
                endDate: limits.max
              });
            }
            $(element).datepicker(datepickConfig)
                .on('changeDate clearDate', function(ev) {
                  // view -> model
                  scope.$apply(function() {
                    if (!isRange) {
                      var date = $(element).find('input')[0].value;
                      scope.date = date !== '' ? date : undefined;
                    }
                    else {
                      var target = ev.target;
                      var pickers = $(element).find('input');
                      var dateFrom = pickers[0].value;
                      var dateTo = pickers[1].value;
                      var changed = false;

                      // only apply the date which was modified if it is valid
                      // (or cleared)
                      if (target === pickers[0] &&
                      (isDateValid(dateFrom) || dateFrom == '')) {
                        scope.date.from = dateFrom !== '' ?
                        dateFrom : undefined;
                        changed = true;
                      } else if (target === pickers[1] &&
                      (isDateValid(dateTo) || dateTo == '')) {
                        scope.date.to = dateTo !== '' ?
                        dateTo : undefined;
                        changed = true;
                      }

                      // call the change function if the value was changed
                      if (changed) {
                        scope.internalChange = true;
                        scope.onChangeFn();
                      }
                    }
                  });
                });
            rendered = true;

            // set initial dates (use $timeout to avoid messing with ng digest)
            if (scope.date) {
              $timeout(function() {
                var picker = $(element).data('datepicker');
                if (isRange) {
                  picker.pickers[0].setDate(scope.date.from);
                  picker.pickers[1].setDate(scope.date.to);
                } else {
                  picker.setDate(scope.date);
                }
              });
            }
          };

          init();

          // model -> view
          if (!isRange) {
            scope.$watch('date', function(v, o) {

              if (angular.isDefined(v) &&
                  angular.isFunction(scope.onChangeFn)) {
                scope.onChangeFn();
              }
              if (v != o) {
                $(element).find('input')[0].value = v || '';
              }
            });
          }
          else {
            scope.$watch('config', function(n, o) {
              init();
            });
            scope.$watchCollection('date', function(newValue, oldValue) {
              if (!scope.date) {
                scope.date = {};
                return;
              }
              // skip if internal change
              if (scope.internalChange) {
                scope.internalChange = false;
                return;
              }
              var dateFrom = (newValue && newValue.from) || '';
              var dateTo = (newValue && newValue.to) || '';
              var previousFrom = (oldValue && oldValue.from) || '';
              var previousTo = (oldValue && oldValue.to) || '';
              if (dateFrom != previousFrom || dateTo != previousTo) {
                $timeout(function() {
                  var picker = $(element).data('datepicker');
                  $(element).find('input')[0].value = dateFrom;
                  $(element).find('input')[1].value = dateTo;
                  picker.pickers[0].update();
                  picker.pickers[1].update();
                });
              }
            });
          }
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnPaginationList
   * @function
   *
   * @description
   * Adjust textarea size onload and when text change.
   *
   * Source: http://www.frangular.com/2012/12/
   *  pagination-cote-client-directive-angularjs.html
   */
  module.factory('gnPaginationListStateCache', ['$cacheFactory',
    function($cacheFactory) {
      return $cacheFactory('gnPaginationListStateCache');
    }]);
  module.directive('gnPaginationList', ['gnPaginationListStateCache',
    function(gnPaginationListStateCache) {
      var pageSizeLabel = 'Page size';
      return {
        priority: 0,
        restrict: 'A',
        scope: {items: '&'},
        templateUrl: '../../catalog/components/utility/' +
            'partials/paginationlist.html',
        replace: false,
        compile: function compile(tElement, tAttrs) {
          var cacheId = tAttrs.cache ? tAttrs.cache + '.paginator' : '';
          var getItemsFunctionName = tAttrs.getItemsFunctionName ? tAttrs.getItemsFunctionName: 'pageItems';
          var firstPageFunctionName = tAttrs.firstPageFunctionName ? tAttrs.firstPageFunctionName: 'firstPage';

          return {
            pre: function preLink(scope) {
              scope.pageSizeList = [10, 20, 50, 100];
              var defaultSettings = {
                pageSize: 10,
                currentPage: 0
              };
              scope.paginator = cacheId ?
                  gnPaginationListStateCache.get(cacheId) || defaultSettings :
                  defaultSettings;
              if (cacheId) {
                gnPaginationListStateCache.put(cacheId, scope.paginator);
              }
              scope.isFirstPage = function() {
                return scope.paginator.currentPage == 0;
              };
              scope.isLastPage = function() {
                if (scope.items()) {
                  return scope.paginator.currentPage >=
                      scope.items().length / scope.paginator.pageSize - 1;
                } else {
                  return false;
                }
              };
              scope.incPage = function() {
                if (!scope.isLastPage()) {
                  scope.paginator.currentPage++;
                }
              };
              scope.decPage = function() {
                if (!scope.isFirstPage()) {
                  scope.paginator.currentPage--;
                }
              };
              scope.firstPage = function() {
                scope.paginator.currentPage = 0;
              };
              scope.numberOfPages = function() {
                if (scope.items()) {
                  return Math.ceil(scope.items().length /
                      scope.paginator.pageSize);
                } else {
                  return 0;
                }
              };
              scope.$watch('paginator.pageSize',
                  function(newValue, oldValue) {
                    if (newValue != oldValue) {
                      scope.firstPage();
                    }
                  });

              // ---- Functions available in parent scope -----

              scope.$parent[firstPageFunctionName] = function() {
                scope.firstPage();
              };
              // Function that returns the reduced items list,
              // to use in ng-repeat
              scope.$parent[getItemsFunctionName] = function() {
                if (angular.isArray(scope.items())) {
                  var start = scope.paginator.currentPage *
                      scope.paginator.pageSize;
                  var limit = scope.paginator.pageSize;
                  return scope.items().slice(start, start + limit);
                } else {
                  return null;
                }
              };
            }
          };
        }
      };
    }]);

  module.directive('ddTextCollapse', ['$compile', function($compile) {
    return {
      restrict: 'A',
      scope: true,
      link: function(scope, element, attrs) {
        // start collapsed
        scope.collapsed = false;
        // create the function to toggle the collapse
        scope.toggle = function() {
          scope.collapsed = !scope.collapsed;
        };
        // wait for changes on the text
        attrs.$observe('ddTextCollapseText', function(text) {
          // get the length from the attributes
          var maxLength = scope.$eval(attrs.ddTextCollapseMaxLength);
          if (text.length > maxLength) {
            // split the text in two parts, the first always showing
            var firstPart = String(text).substring(0, maxLength);
            var secondPart = String(text).substring(maxLength, text.length);
            // create some new html elements to hold the separate info
            var firstSpan = $compile('<span>' + firstPart + '</span>')(scope);
            var secondSpan = $compile('<span ng-if="collapsed">' +
                secondPart + '</span>')(scope);
            var moreIndicatorSpan = $compile(
                '<span ng-if="!collapsed">... </span>')(scope);
            var lineBreak = $compile('<br ng-if="collapsed">')(scope);
            var toggleButton = $compile(
                '<span class="collapse-text-toggle" ng-click="toggle()">' +
                '  <span ng-show="collapsed" translate>less</span>' +
                '  <span ng-show="!collapsed" translate>more</span>' +
                '</span>'
                )(scope);
            // remove the current contents of the element
            // and add the new ones we created
            element.empty();
            element.append(firstSpan);
            element.append(secondSpan);
            element.append(moreIndicatorSpan);
            element.append(lineBreak);
            element.append(toggleButton);
          }
          else {
            element.empty();
            element.append(text);
          }
        });
      }
    };
  }]);


  module.directive('gnCollapsible', ['$parse', function($parse) {
    return {
      restrict: 'A',
      scope: false,
      link: function(scope, element, attrs) {
        var getter = $parse(attrs['gnCollapsible']);
        var setter = getter.assign;

        element.on('click', function(e) {
          scope.$apply(function() {
            var collapsed = getter(scope);
            setter(scope, !collapsed);
          });
        });
      }
    };
  }]);


  /**
   * Directive which create the href attribute
   * for an element preserving the debug mode
   * if activated and adding an active class
   * to the parent element (required to highlight
   * element in navbar)
   */
  module.directive('gnActiveTbItem', ['$location', 'gnLangs', 'gnConfig',
    function($location, gnLangs, gnConfig) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          var link = attrs.gnActiveTbItem, href,
              isCurrentService = false;

          // Replace lang in link (three character language code i.e. eng, fre)
          link = link.replace('{{lang}}', gnLangs.getCurrent());
          // Replace standard ISO lang in link (two character language code i.e. en, fr)
          link = link.replace('{{isoLang}}', gnLangs.getIso2Lang(gnLangs.getCurrent()));
          link = link.replace('{{node}}', gnConfig.env.node);

          // Insert debug mode between service and route
          if (link.indexOf('#') !== -1) {
            var tokens = link.split('#');
            isCurrentService = window.location.pathname.
                match('.*' + tokens[0] + '$') !== null;
            href =
                (isCurrentService ? '' :
                tokens[0] + (scope.isDebug ? '?debug' : '')
                ) + '#' +
                tokens[1];
          } else {
            isCurrentService = window.location.pathname.
                match('.*' + link + '$') !== null;
            href =
                isCurrentService ? '#/' : link +
                (scope.isDebug ? '?debug' : '');

          }

          // Set the href attribute for the element
          // with the link containing the debug mode
          // or not
          element.attr('href', href);

          function checkActive() {
            // regexps for getting the service & path
            var serviceRE = /\/?([^\/\?#]*)\??[^\/]*(?:#|$)/;
            var pathRE = /#\/?([^\?]*)/;

            // compare current url & input href
            var url = $location.absUrl();
            var currentService =
                url.match(serviceRE) ? url.match(serviceRE)[1] : '';
            var currentPath = $location.path().substring(1);
            var targetService =
                link.match(serviceRE) ? link.match(serviceRE)[1] : '';
            var targetPath =
                link.match(pathRE) ? link.match(pathRE)[1] : '';
            var isActive = currentService == targetService &&
                (!targetPath || currentPath.indexOf(targetPath) > -1);

            if (isActive) {
              element.parent().addClass('active');
            } else {
              element.parent().removeClass('active');
            }
          }

          scope.$on('$locationChangeSuccess', checkActive);

          checkActive();
        }
      };
    }]);
  module.filter('signInLink', ['$location', 'gnLangs', 'gnConfig',
    function($location, gnLangs, gnConfig) {
      return function(href) {
        href = href.replace('{{lang}}', gnLangs.getCurrent())
                   .replace('{{node}}', gnConfig.env.node) +
            '?redirect=' + encodeURIComponent(window.location.href);
        return href;
      }}
  ]);
  /**
   * Append size parameter to request a smaller thumbnail.
   */
  module.filter('thumbnailUrlSize', function() {
      return function(href, size) {
        if(href && href.indexOf('api/records/') !== -1) {
          var suffix = 'size=' + (size || 140);
          return href.indexOf('?') !== -1 ?
            href + '&' + suffix :
            href + '?' + suffix;
        } else {
          return href;
        }
      }});
  module.filter('newlines', function() {
    return function(value) {
      if (angular.isArray(value)) {
        var finalText = '';
        angular.forEach(value, function(value, key) {
          if (value) {
            finalText += '<p>' + value + '</p>';
          }
        });

        return finalText;

      } else if (angular.isString(value)) {
        if (value) {
          return value.replace(/(\r)?\n/g, '<br/>')
              .replace(/(&#13;)?&#10;/g, '<br/>');
        } else {
          return value;
        }
      } else {
        return value;
      }
    }
  });
  module.filter('geojsonToWkt', function() {
    return function(val) {
      var wkt_format = new ol.format.WKT();
      var geojson_format = new ol.format.GeoJSON();
      return wkt_format.writeGeometry(geojson_format.readGeometry(val));
    }
  });
  module.filter('encodeURIComponent', function() {
    return window.encodeURIComponent;
  });
  module.directive('gnJsonText', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {
        function into(input) {
          return ioFn(input, 'parse');
        }
        function out(input) {
          // If model value is a string
          // No need to stringify it.
          if (attr['gnJsonIsJson']) {
            return ioFn(input, 'stringify');
          } else {
            return input;
          }
        }
        function ioFn(input, method) {
          var json;
          try {
            json = JSON[method](input);
            ngModel.$setValidity('json', true);
          } catch (e) {
            ngModel.$setValidity('json', false);
          }
          return json;
        }
        ngModel.$parsers.push(into);
        ngModel.$formatters.push(out);

      }
    };
  });
  module.directive('gnImgModal', ['$filter', function($filter) {
    return {
      restrict: 'A',
      link: function(scope, element, attr, ngModel) {
        var modalElt;

        element.bind('click', function() {
          var imgOrMd = scope.$eval(attr['gnImgModal']);
          var img = undefined;
          if(imgOrMd.overview) {
            var imgs = imgOrMd.overview;
            var url = $(element).attr('src');
            for (var i = 0; i < imgs.list.length; i++) {
              // the thumbnails url might end with `?approved=false/true`, which is not
              // present on img
              if (imgs.list[i].url.indexOf(url) === 0) {
                img = imgs.list[i];
                break;
              }
            }
          } else {
            img = imgOrMd;
          }

          // Toggle the modal if already displayed
          if (modalElt) {
            modalElt.modal('hide');
            modalElt = null;
            return;
          }
          if (img) {
            var label = (img.label || (
                $filter('gnLocalized')(img.title, scope.lang)) || '');
            var labelDiv =
                '<div class="gn-img-background">' +
                '  <div class="gn-img-thumbnail-caption">' +
                label + '</div>' +
                '</div>';
            modalElt = angular.element('' +
                '<div class="modal fade in"' +
                '     id="gn-img-modal-' + (img.id || img.lUrl || img.url) + '">' +
                '<div class="modal-dialog gn-img-modal in">' +
                '  <button type=button class="btn btn-link gn-btn-modal-img">' +
                '<i class="fa fa-times text-danger"/></button>' +
                '  <img src="' + (img.lUrl || img.url || img.id) + '"/>' +
                (label != '' ? labelDiv : '') +
                '</div>' +
                '</div>');

            $(document.body).append(modalElt);
            modalElt.modal();
            modalElt.on('hidden.bs.modal', function() {
              if (modalElt) {
                modalElt.remove();
              }
            });
            modalElt.find('.gn-btn-modal-img').on('click', function() {
              modalElt.modal('hide');
            });
          }
        });
      }
    };
  }]);

  module.directive('gnPopoverDropdown', ['$timeout', function($timeout) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        // Container is one ul with class list-group
        // Avoid to set style on embedded drop down menu
        var content = element.find('ul.list-group').css('display', 'none');
        var button = element.find('> .btn');

        $timeout(function() {
          var className = (attrs['fixedHeight'] != 'false') ?
              'popover-dropdown popover-dropdown-' + content.find('li').length :
              '';
          button.popover({
            animation: false,
            container: '[gn-main-viewer]',
            placement: attrs['placement'] || 'bottom',
            content: ' ',
            template:
                '<div class="popover ' + className + '">' +
                '  <div class="arrow"></div>' +
                '  <h3 class="popover-title"></h3>' +
                '  <div class="popover-content"></div>' +
                '</div>'
          });
        }, 1);

        button.on('shown.bs.popover', function() {
          var $tip = button.data('bs.popover').$tip;
          content.css('display', 'inline').appendTo(
              $tip.find('.popover-content')
          );
        });
        button.on('hidden.bs.popover', function() {
          content.css('display', 'none').appendTo(element);
        });

        var hidePopover = function() {
          button.popover('hide');
          button.data('bs.popover').inState.click = false;
        };

        // can’t use dismiss boostrap option: incompatible with opacity slider
        var onMousedown = function(e) {
          if ((button.data('bs.popover') && button.data('bs.popover').$tip) &&
              (button[0] != e.target) &&
              (!$.contains(button[0], e.target)) &&
              (
              $(e.target).parents('.popover')[0] !=
              button.data('bs.popover').$tip[0])
          ) {
            $timeout(hidePopover, 30, false);
          }
        };

        $('body').on('mousedown click', onMousedown);

        if (attrs['gnPopoverDismiss']) {
          $(attrs['gnPopoverDismiss']).on('scroll', hidePopover);
        }

        element.on('$destroy', function() {
          $('body').off('mousedown click', onMousedown);
          if (attrs['gnPopoverDismiss']) {
            $(attrs['gnPopoverDismiss']).off('scroll', hidePopover);
          }
        });
      }
    };
  }]);
  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnLynky
   *
   * @description
   * If the text provided contains the following format:
   * link|URL|Text, it's converted to an hyperlink, otherwise
   * the text is displayed without any formatting.
   *
   */
  module.directive('gnLynky', ['$compile',
    function($compile) {
      return {
        restrict: 'A',
        scope: {
          text: '@gnLynky'
        },
        link: function(scope, element, attrs) {
          if ((scope.text.indexOf('link') == 0) &&
              (scope.text.split('|').length == 3)) {
            scope.link = scope.text.split('|')[1];
            scope.value = scope.text.split('|')[2];

            element.replaceWith($compile('<a data-ng-href="{{link}}" ' +
                'data-ng-bind-html="value | newlines"></a>')(scope));
          } else {

            element.replaceWith($compile('<span ' +
                'data-ng-bind-html="text | linky | newlines"></span>')(scope));
          }
        }

      };
    }
  ]);


  /**
   * @ngdoc gnApiLink
   * @name gn_utility.directive:gnApiLink
   *
   * @description
   * Convert the element href attribute
   * from /srv/api/records/... to a link
   * for the JS apps. This is usefull if
   * a formatter is loaded into the JS app
   * in order to have links to record to
   * open in current app.
   */
  module.directive('gnApiLink', ['$compile',
    function($compile) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          var href = $(element).attr('href'),
              apiPath = '/srv/api/records/';
          if (href.indexOf(apiPath) != -1) {
            $(element).attr('href',
              href.replace(/.*\/srv\/api\/records\//, '#/metadata/'));
          }
        }
      };
    }
  ]);


  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnStringToNumber
   *
   * @description
   * Converts a string with a number value to a number.
   * To be used for example in input type=number fields
   * when the model value is stored in a string field.
   *
   */
  module.directive('gnStringToNumber', function() {
    return {
      require: 'ngModel',
      link: function (scope, element, attrs, ngModel) {
        ngModel.$parsers.push(function (value) {
          return '' + value;
        });
        ngModel.$formatters.push(function (value) {
          return parseFloat(value);
        });
      }
    };
  });

  /**
   * @ngdoc directive
   * @name gn_utility.directive:gnHideShowPassword
   *
   * @description
   * Toggles the visibility of a  related input password field.
   * To be used in input type=password fields and display a button
   * to toggle the visibility of the password field.
   *
   */
  module.directive('gnHideShowPassword', function() {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        inputId: '@'
      },
      templateUrl: '../../catalog/components/utility/' +
        'partials/hideshowpassword.html',
      link: function (scope) {
        var cssInputPasswordType = 'fa fa-eye';
        var cssInputTextType = 'fa fa-eye-slash';

        var target = $('#' + scope.inputId)[0];

        var updateInputCss = function() {
          if(target != null) {
            if(target.type == 'password') {
              scope.showHideClass = cssInputPasswordType;
            } else {
              scope.showHideClass =  cssInputTextType;
            }
          }
        }

        scope.hideShowPassword = function(){
          // Toggle the control type and button icon
          if(target != null) {
            if(target.type == 'password') {
              target.type = 'text';
            } else {
              target.type = 'password';
            }

            updateInputCss();
          }
        };

        updateInputCss();
      }
    };
  });
})();
