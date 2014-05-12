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
  module.directive('gnCountryPicker', ['gnHttp',
    function(gnHttp) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
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

            $(element).typeahead({
              name: 'countries',
              valueKey: 'name',
              local: data,
              minLength: 0,
              limit: 30
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
            scope.region = data[0];
          });

          scope.setRegion = function(region) {
            scope.region = region;
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

          if (attrs['gnRegion']) {
            gnRegionService.loadList().then(function(data) {
              for (i = 0; i < data.length; ++i) {
                if (attrs['gnRegion'] == data[i].name) {
                  scope.region = data[i];
                }
              }
            });
          }
          scope.$watch('region', function(val) {
            if (scope.region) {
              gnRegionService.loadRegion(scope.region, scope.lang).then(
                  function(data) {
                    $(element).typeahead('destroy');
                    $(element).typeahead({
                      valueKey: 'name',
                      local: data,
                      minLength: 0,
                      limit: 30
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

            $(element).typeahead({
              name: 'isoLanguages',
              valueKey: 'code',
              template: function(datum) {
                return '<p>' + datum.name + ' (' + datum.code + ')</p>';
              },
              local: data,
              minLength: 0,
              limit: 30
            });
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
})();
