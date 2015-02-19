(function() {
  goog.provide('gn_utility_directive');

  var module = angular.module('gn_utility_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnCountryPicker
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   * Use the region API to retrieve the list of
   * Country.
   *
   * TODO: This could be used in other places
   * probably. Move to another common or language module ?
   */
  module.directive('gnCountryPicker', ['gnHttp', 'gnUtilityService',
    function(gnHttp, gnUtilityService) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          element.attr('placeholder', '...');
          gnHttp.callService('country', {}, {
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

          /**
          * Load list on init to fill the dropdown
          */
          gnRegionService.loadList().then(function(data) {
            scope.regionType = data[0];
          });

          scope.setRegion = function(regionType) {
            scope.regionType = regionType;
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
    'gnRegionService',
    function(gnRegionService) {
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
              gnRegionService.loadRegion(scope.regionType, scope.lang).then(
                  function(data) {
                    $(element).typeahead('destroy');
                    var source = new Bloodhound({
                      datumTokenizer:
                          Bloodhound.tokenizers.obj.whitespace('name'),
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
            }
          });
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnLanguagePicker
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
  module.directive('gnLanguagePicker', ['gnHttp',
    function(gnHttp) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          element.attr('placeholder', '...');
          gnHttp.callService('lang', {}, {
            cache: true
          }).success(function(data) {
            // Compute default name and add a
            // tokens element which is used for filter
            angular.forEach(data, function(lang) {
              var defaultName = lang.label['eng'];
              lang.name = lang.label[scope.lang] || defaultName;
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
        }
      };
    }]);

  module.directive('gnHumanizeTime', [
    function() {
      return {
        restrict: 'A',
        template: '<span title="{{title}}">{{value}}</span>',
        scope: {
          date: '@gnHumanizeTime',
          format: '@',
          fromNow: '@'
        },
        link: function linkFn(scope, element, attr) {
          scope.$watch('date', function(originalDate) {
            if (originalDate) {
              // Moment will properly parse YYYY, YYYY-MM,
              // YYYY-MM-DDTHH:mm:ss which are the formats
              // used in the common metadata standards.
              // By the way check Z
              var date = null, suffix = 'Z';
              if (originalDate.indexOf(suffix,
                  originalDate.length - suffix.length) !== -1) {
                date = moment(originalDate, 'YYYY-MM-DDtHH-mm-SSSZ');
              } else {
                date = moment(originalDate);
              }
              if (date.isValid()) {
                var fromNow = date.fromNow();
                var formattedDate = scope.format ?
                    date.format(scope.format) :
                    date.toString();
                scope.value = scope.fromNow !== undefined ?
                    fromNow : formattedDate;
                scope.title = scope.fromNow !== undefined ?
                    formattedDate : fromNow;
              }
            }
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_fields_directive.directive:gnDirectoryEntryPicker
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
               _isTemplate: 's',
               any: '*QUERY*',
               _root: 'gmd:CI_ResponsibleParty',
               sortBy: 'title',
               sortOrder: 'reverse',
               resultType: 'subtemplates',
               fast: 'index'
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
   * @name gn_utility_directive.directive:gnAutogrow
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
        ' style="height: 0px; position: absolute; visibility: hidden;"/>');
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
  module.directive('gnSlideToggle', [
    function() {
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
        }
      };
    }]);

  module.directive('gnClickAndSpin', ['$parse',
    function($parse) {
      return {
        restrict: 'A',
        compile: function(scope, element, attr) {
          var fn = $parse(element['gnClickAndSpin'], null, true);
          return function ngEventHandler(scope, element) {
            var running = false;
            var icon = element.find('i');
            var spinner = null;
            var start = function() {
              running = true;
              element.addClass('running');
              element.addClass('disabled');
              icon.addClass('hidden');
              spinner = element.
                  prepend('<i class="fa fa-spinner fa-spin"></i>');
            };
            var done = function() {
              running = false;
              element.removeClass('running');
              element.removeClass('disabled');
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
              callback().then(function() {
                done();
              });
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

  /**
   * Use to initialize bootstrap datepicker
   */
  module.directive('gnBootstrapDatepicker', [
    function() {

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
          if (k < year.min) year.min = k;
          if (k > year.max) year.max = k;
        }
        for (k in obj[year.min]) {
          if (k < month.min) month.min = k;
        }
        for (k in obj[year.max]) {
          if (k > month.max) month.max = k;
        }
        for (k in obj[year.min][month.min]) {
          if (obj[year.min][month.min][k] < day.min) day.min =
                obj[year.min][month.min][k];
        }
        for (k in obj[year.max][month.max]) {
          if (obj[year.min][month.min][k] > day.max) day.max =
                obj[year.min][month.min][k];
        }

        return {
          min: month.min + 1 + '/' + day.min + '/' + year.min,
          max: month.max + 1 + '/' + day.max + '/' + year.max
        };
      };

      return {
        restrict: 'A',
        scope: {
          date: '=gnBootstrapDatepicker',
          dates: '=dateAvailable'
        },
        link: function(scope, element, attrs, ngModelCtrl) {

          var available = function(date) {
            if (scope.dates[date.getFullYear()] &&
                scope.dates[date.getFullYear()][date.getMonth()] &&
                $.inArray(date.getDate(),
                    scope.dates[date.getFullYear()][date.getMonth()]) != -1) {
              return true;
            } else {
              return false;
            }
          };

          var limits;
          if (scope.dates) {
            limits = getMaxInProp(scope.dates);

          }

          $(element).datepicker(angular.isDefined(scope.dates) ? {
            beforeShowDay: function(dt, a, b) {
              return available(dt);
            },
            startDate: limits.min,
            endDate: limits.max
          } : {}).on('changeDate', function(ev) {
            // view -> model
            scope.$apply(function() {
              scope.date = $(element).find('input')[0].value;
            });
          });

          // model -> view
          scope.$watch('date', function(v) {
            if (angular.isUndefined(v)) {
              v = '';
            }
            $(element).find('input')[0].value = v;
          });
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_utility_directive.directive:gnPaginationList
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

              scope.$parent.firstPage = function() {
                scope.firstPage();
              };
              // Function that returns the reduced items list,
              // to use in ng-repeat
              scope.$parent.pageItems = function() {
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

  module.directive('gnCollapse', ['$compile', function($compile) {
    return {
      restrict: 'A',
      scope: true,
      link: function(scope, element, attrs) {
        scope.collapsed = attrs['gnCollapse'] == 'true';
        element.on('click', function(e) {
          var next = element.next();
          next.collapse('toggle');
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
  module.directive('gnActiveTbItem', ['$location', function($location) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var link = attrs.gnActiveTbItem, href,
            isCurrentService = false;

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
              isCurrentService ? '#/' : link + (scope.isDebug ? '?debug' : '');

        }

        // Set the href attribute for the element
        // with the link containing the debug mode
        // or not
        element.attr('href', href);

        function checkActive() {
          // Ignore the service parameters and
          // check url contains path
          var isActive = $location.absUrl().replace(/\?.*#/, '#').
              match('.*' + link + '.*') !== null;

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
  module.filter('newlines', function () {
    return function(text) {
      if (text) {
        return text.replace(/(\r)?\n/g, '<br/>');
      } else {
        return text;
      }
    }
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
          return ioFn(input, 'stringify');
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
})();
