(function() {

  goog.provide('gn_formfields_directive');

  angular.module('gn_formfields_directive', [])
  /**
   * @ngdoc directive
   * @name gn_form_fields_directive.directive:gnTypeahead
   * @restrict A
   *
   * @description
   * It binds a tagsinput to the input for multi select.
   * By default, the list is shown on click even if the input value is
   * empty.
   */

  .directive('gnTypeahead', [function() {

        /**
         * If data are prefetched, get the label from the value
         * Uses for model -> ui
         * @param {array} a
         * @param {string} v
         * @return {string|undefined}
         */
        var findLabel = function(a, v) {
          for (var i = 0; i < a.length; i++) {
            if (a[i].id == v) {
              return a[i].name;
            }
          }
        };

        return {
          restrict: 'A',
          scope: {
            options: '=gnTypeahead',
            gnValues: '='
          },
          link: function(scope, element, attrs) {
            var config = scope.options.config || {};
            var doLink = function(data, remote) {

              var bloodhoundConf = {
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                limit: 10000000,
                sorter: function(a, b) {
                  if (a.name < b.name) return -1;
                  else if (a.name > b.name) return 1;
                  else return 0;
                }
              };

              if (data) {
                bloodhoundConf.local = data;
              } else if (remote) {
                bloodhoundConf.remote = remote;
                // Remove from suggestion values already selected in remote mode
                if (angular.isFunction(remote.filter)) {
                  var filterFn = remote.filter;
                  bloodhoundConf.remote.filter = function(data) {
                    var filtered = filterFn(data);
                    var datums = [];
                    for (var i = 0; i < filtered.length; i++) {
                      if (stringValues.indexOf(filtered[i].id) < 0) {
                        datums.push(filtered[i]);
                      }
                    }
                    return datums;
                  };
                }
              }
              var engine = new Bloodhound(bloodhoundConf);
              engine.initialize();

              // Remove from suggestion values already selected for local mode
              var refreshDatum = function() {
                if (bloodhoundConf.local) {
                  engine.clear();
                  for (var i = 0; i < data.length; i++) {
                    if (stringValues.indexOf(data[i].id) < 0) {
                      engine.add(data[i]);
                    }
                  }
                }
              };

              // Initialize tagsinput in the element
              $(element).tagsinput({
                itemValue: 'id',
                itemText: 'name'
              });

              // Initialize typeahead
              var field = $(element).tagsinput('input');
              field.typeahead({
                minLength: 0,
                hint: true,
                highlight: true
              }, angular.extend({
                name: 'datasource',
                displayKey: 'name',
                source: engine.ttAdapter()
              }, config)).on('typeahead:selected', function(event, datum) {
                field.typeahead('val', '');
                $(element).tagsinput('add', datum);
              });

              /** Binds input content to model values */
              var stringValues = [];
              var prev = stringValues.slice();

              // ui -> model
              $(element).on('itemAdded', function(event) {
                if (stringValues.indexOf(event.item.id) === -1) {
                  stringValues.push(event.item.id);
                  prev = stringValues.slice();
                  scope.gnValues = stringValues.join(' or ');
                  scope.$apply();
                }
                refreshDatum();

              });
              $(element).on('itemRemoved', function(event) {
                var idx = stringValues.indexOf(event.item.id);
                if (idx !== -1) {
                  stringValues.splice(idx, 1);
                  prev = stringValues.slice();
                  scope.gnValues = stringValues.join(' or ');
                  scope.$apply();
                }
                refreshDatum();
              });

              // model -> ui
              scope.$watch('gnValues', function() {
                if (angular.isDefined(scope.gnValues) && scope.gnValues != '') {
                  stringValues = scope.gnValues.split(' or ');
                }
                else {
                  stringValues = [];
                }

                var added = stringValues.filter(function(i) {
                  return prev.indexOf(i) === -1;
                }),
                    removed = prev.filter(function(i) {
                      return stringValues.indexOf(i) === -1;
                    }),
                    i;
                prev = stringValues.slice();

                // Remove tags no longer in binded model
                for (i = 0; i < removed.length; i++) {
                  $(element).tagsinput('remove', removed[i]);
                }

                // Refresh remaining tags
                $(element).tagsinput('refresh');

                // Add new items in model as tags
                for (i = 0; i < added.length; i++) {
                  $(element).tagsinput('add', {
                    id: added[i],
                    name: findLabel(data, added[i])
                  });
                }
              }, true);

              /** Manage the cross to clear the input */
              var triggerElt = $('<span class="close tagsinput-trigger' +
                  ' fa fa-ellipsis-v"></span>');
              field.parent().after(triggerElt);
              var resetElt = $('<span class="close ' +
                  'tagsinput-clear">&times;</span>')
              .on('click', function() {
                    scope.gnValues = '';
                    scope.$apply();
                  });
              field.parent().after(resetElt);
              resetElt.hide();

              $(element).on('change', function() {
                resetElt.toggle($(element).val() != '');
              });
            };

            if (scope.options.mode == 'prefetch') {
              scope.options.promise.then(doLink);
            } else if (scope.options.mode == 'remote') {
              doLink(null, scope.options.remote);
            } else if (scope.options.mode == 'local') {
              doLink(scope.options.data);
            }

          }
        };
      }])


    .directive('groupsCombo', ['$http', function($http) {
        return {

          restrict: 'A',
          templateUrl: '../../catalog/components/search/formfields/' +
              'partials/groupsCombo.html',
          scope: {
            ownerGroup: '=',
            lang: '=',
            groups: '=',
            excludeSpecialGroups: '='
          },

          link: function(scope, element, attrs) {
            var url = 'info?_content_type=json' +
                '&type=groupsIncludingSystemGroups';
            if (attrs.profile) {
              url = 'info?_content_type=json' +
                  '&type=groups&profile=' + attrs.profile;
            }
            $http.get(url, {cache: true}).
                success(function(data) {
                  scope.groups = data !== 'null' ? data.group : null;

                  // Select by default the first group.
                  if ((angular.isUndefined(scope.ownerGroup) ||
                      scope.ownerGroup === '') && data.group) {
                    scope.ownerGroup = data.group[0]['@id'];
                  }
                });
          }

        };
      }])

  .directive('protocolsCombo', ['$http', 'gnSchemaManagerService',
        function($http, gnSchemaManagerService) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/protocolsCombo.html',
            scope: {
              protocol: '=',
              lang: '='
            },
            controller: ['$scope', '$translate', function($scope, $translate) {
              var config = 'iso19139|gmd:protocol|||';
              gnSchemaManagerService.getElementInfo(config).then(
                  function(data) {
                    $scope.protocols = data !== 'null' ?
                        data[0].helper.option : null;
                  });
            }]
          };
        }])

      .directive('sortbyCombo', ['$translate', 'hotkeys',
        function($translate, hotkeys) {
          return {
            restrict: 'A',
            require: '^ngSearchForm',
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/sortByCombo.html',
            scope: {
              params: '=',
              values: '=gnSortbyValues'
            },
            link: function(scope, element, attrs, searchFormCtrl) {
              scope.params.sortBy = scope.params.sortBy ||
                  scope.values[0].sortBy;
              scope.sortBy = function(v) {
                angular.extend(scope.params, v);
                searchFormCtrl.triggerSearch(true);
              };
              hotkeys.bindTo(scope)
                .add({
                    combo: 's',
                    description: $translate('hotkeySortBy'),
                    callback: function() {
                      for (var i = 0; i < scope.values.length; i++) {
                        if (scope.values[i].sortBy === scope.params.sortBy) {
                          var nextOptions = i === scope.values.length - 1 ?
                              0 : i + 1;
                          scope.sortBy(scope.values[nextOptions]);
                          return;
                        }
                      }
                    }
                  });
            }
          };
        }])

      .directive('hitsperpageCombo', [
        function() {
          return {
            restrict: 'A',
            require: '^ngSearchForm',
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/hitsperpageCombo.html',
            scope: {
              pagination: '=paginationCfg',
              values: '=gnHitsperpageValues'
            },
            link: function(scope, element, attrs, searchFormCtrl) {
              scope.updatePagination = function() {
                searchFormCtrl.resetPagination();
                searchFormCtrl.triggerSearch();
              };
            }
          };
        }])

      /**
   * @ngdoc directive
   * @name gn_form_fields_directive.directive:gnSearchSuggest
   * @restrict A
   *
   * @description
   * Add a multiselect typeahead based input for suggestion.
   * It binds a tagsinput to the input for multi select.
   * It uses typeahead to retrieve and display suggestions from the geonetwork
   * open suggestion service `suggest`, in remote mode.
   * The index fields for the suggestion is given by the `gnSearchSuggest`
   * attribute.
   * By default, the list is not shown on click even if the input value is
   * empty.
   */
      .directive('gnSearchSuggest',
      ['suggestService',
        function(suggestService) {
          return {
            restrict: 'A',
            scope: {
              field: '@gnSearchSuggest',
              startswith: '@gnSearchSuggestStartswith',
              multi: '@'
            },
            link: function(scope, element, attrs) {
              var remote = {
                url: suggestService.getUrl('QUERY', scope.field,
                    (scope.startswith ? 'STARTSWITHFIRST' : 'ALPHA')),
                filter: suggestService.filterResponse,
                wildcard: 'QUERY'
              };
              if (angular.isUndefined(scope.multi)) {
                element.typeahead({
                  remote: remote
                });
              }
              else {
                element.tagsinput({
                });
                element.tagsinput('input').typeahead({
                  remote: remote
                }).bind('typeahead:selected', $.proxy(function(obj, datum) {
                  this.tagsinput('add', datum.value);
                  this.tagsinput('input').typeahead('setQuery', '');
                }, element));
              }
            }
          };
        }])

      /**
   * @ngdoc directive
   * @name gn_form_fields_directive.directive:gnRegionMultiselect
   * @restrict A
   *
   * @description
   * Add a multiselect typeahead based input for regions.
   * It binds a tagsinput to the input for multi select.
   * It calls the region service once on init, to feed the list, then
   * use typeahead in local mode to display region suggestions.
   * The type of regions to match is given by the `gnRegionMultiselect`
   * attribute.
   * By default, the list is shown on click even if the input value is
   * empty.
   */

  .directive('gnRegionMultiselect',
      ['gnRegionService',
        function(gnRegionService) {
          return {
            restrict: 'A',
            scope: {
              field: '@gnRegionMultiselect',
              callback: '=gnCallback'
            },
            link: function(scope, element, attrs) {
              var type = {
                id: 'http://geonetwork-opensource.org/regions#' + scope.field
              };
              gnRegionService.loadRegion(type, 'fre').then(
                  function(data) {

                    $(element).tagsinput({
                      itemValue: 'id',
                      itemText: 'name'
                    });
                    var field = $(element).tagsinput('input');
                    field.typeahead({
                      valueKey: 'name',
                      local: data,
                      minLength: 0,
                      limit: 5
                    }).on('typeahead:selected', function(event, datum) {
                      $(element).tagsinput('add', datum);
                      field.typeahead('setQuery', '');
                    });

                    $('input.tt-query')
                        .on('click', function() {
                          var $input = $(this);

                          // these are all expected to be objects
                          // so falsey check is fine
                          if (!$input.data() || !$input.data().ttView ||
                              !$input.data().ttView.datasets ||
                              !$input.data().ttView.dropdownView ||
                              !$input.data().ttView.inputView) {
                            return;
                          }

                          var ttView = $input.data().ttView;

                          var toggleAttribute = $input.attr('data-toggled');

                          if (!toggleAttribute || toggleAttribute === 'off') {
                            $input.attr('data-toggled', 'on');

                            $input.typeahead('setQuery', '');

                            if ($.isArray(ttView.datasets) &&
                                ttView.datasets.length > 0) {
                              // only pulling the first dataset for this hack
                              var fullSuggestionList = [];
                              // renderSuggestions expects a
                              // suggestions array not an object
                              $.each(ttView.datasets[0].itemHash,
                                  function(i, item) {
                       fullSuggestionList.push(item);
                     });

                              ttView.dropdownView.renderSuggestions(
                                  ttView.datasets[0], fullSuggestionList);
                              ttView.inputView.setHintValue('');
                              ttView.dropdownView.open();
                            }
                          }
                          else if (toggleAttribute === 'on') {
                            $input.attr('data-toggled', 'off');
                            ttView.dropdownView.close();
                          }
                        });

                  });
            }
          };
        }])

      /**
   * @ngdoc directive
   * @name gn_form_fields_directive.directive:schemaInfoCombo
   * @restrict A
   * @requires gnSchemaManagerService
   * @requires $http
   * @requires gnCurrentEdit
   *
   * @description
   * The `schemaInfoCombo` directive provides a combo box based on
   * a codelist (<schema>/loc/<lang>/codelist.xml) or an
   * element helper (<schema>/loc/<lang>/helper.xml) from a
   * schema plugins.
   *
   * The combo initialization is made on mouseover in order to not
   * link all combos (including hidden one) on load.
   *
   * The gn-schema-info-combo attribute can contains one of the
   * registered element (which are profile dependant ie. protocol,
   * associationType, initiativeType) or any element name with
   * namespace prefix eg. 'gmd:protocol'.
   *
   * The schema used to retrieve the element info is based on
   * the gnCurrentEdit object or 'iso19139' if not defined.
   */
  .directive('schemaInfoCombo', ['$http', 'gnSchemaManagerService',
        'gnCurrentEdit', 'gnElementsMap',
        function($http, gnSchemaManagerService,
                 gnCurrentEdit, gnElementsMap) {
          return {
            restrict: 'A',
            replace: true,
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/schemainfocombo.html',
            scope: {
              selectedInfo: '=',
              lang: '=',
              allowBlank: '@'
            },
            link: function(scope, element, attrs) {
              var initialized = false;
              var defaultValue;

              var addBlankValueAndSetDefault = function() {
                var blank = {label: '', code: ''};
                if (scope.infos != null && scope.allowBlank !== undefined) {
                  scope.infos.unshift(blank);
                }
                // Search default value
                angular.forEach(scope.infos, function(h) {
                  if (h['default'] == 'true') {
                    defaultValue = h.code;
                  }
                });

                // If no blank value allowed select default or first
                // If no value defined, select defautl or blank one
                if (!angular.isDefined(scope.selectedInfo)) {
                  scope.selectedInfo = defaultValue || scope.infos[0].code;
                }
                // This will avoid to have undefined selected option
                // on top of the list.
              };


              var init = function() {
                var schema = gnCurrentEdit.schema || 'iso19139';
                var element = (gnElementsMap[attrs['gnSchemaInfo']] &&
                    gnElementsMap[attrs['gnSchemaInfo']][schema]) ||
                    attrs['gnSchemaInfo'];
                var config = schema + '|' + element + '|||';

                scope.type = attrs['schemaInfoCombo'];
                if (scope.type == 'codelist') {
                  gnSchemaManagerService.getCodelist(config).then(
                      function(data) {
                        if (data !== 'null') {
                          scope.infos = [];
                          angular.copy(data[0].entry, scope.infos);
                        } else {
                          scope.infos = data[0].entry;
                        }

                        addBlankValueAndSetDefault();
                      });
                }
                else if (scope.type == 'element') {
                  gnSchemaManagerService.getElementInfo(config).then(
                      function(data) {
                        if (data !== 'null') {
                          scope.infos = [];
                          // Helper element may be embbeded in an option
                          // property when attributes are defined
                          angular.forEach(data[0].helper.option ||
                              data[0].helper,
                              function(h) {
                                scope.infos.push({
                                  code: h['@value'],
                                  label: h['#text'],
                                  description: h['@title'] || '',
                                  'default': h['@default'] == '' ?
                                     'true' : 'false'
                                });
                              });
                        } else {
                          scope.infos = null;
                        }
                        addBlankValueAndSetDefault();
                      });
                }
                initialized = true;
              };
              element.bind('mouseover', function() {
                if (!initialized) {
                  init();
                }
              });
            }
          };
        }]);
})();
