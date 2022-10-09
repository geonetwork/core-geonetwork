"use strict";

angular.module("ui.multiselect", ["multiselect.tpl.html"])
  //from bootstrap-ui typeahead parser
  .factory("optionParser", ["$parse", function($parse) {
    //                      00000111000000000000022200000000000000003333333333333330000000000044000
    var TYPEAHEAD_REGEXP = /^\s*(.*?)(?:\s+as\s+(.*?))?\s+for\s+(?:([\$\w][\$\w\d]*))\s+in\s+(.*)$/;

    return {
      parse: function(input) {

        var match = input.match(TYPEAHEAD_REGEXP);
        if(!match) {
          throw new Error("Expected typeahead specification in form of '_modelValue_ (as _label_)? for _item_ in _collection_'" + " but got '" + input + "'.");
        }

        return {
          itemName   : match[3],
          source     : $parse(match[4]),
          viewMapper : $parse(match[2] || match[1]),
          modelMapper: $parse(match[1])
        };
      }
    };
  }])
  .directive("multiselect", ["$parse", "$document", "$compile", "$interpolate", "optionParser", function($parse, $document, $compile, $interpolate, optionParser) {
    return {
      restrict: "E",
      require : "ngModel",
      link    : function(originalScope, element, attrs, modelCtrl) {

        var exp = attrs.options;
        var parsedResult = optionParser.parse(exp);
        var isMultiple = attrs.multiple ? true : false;
        var compareByKey = attrs.compareBy;
        var headerKey = attrs.headerKey;
        var dividerKey = attrs.dividerKey;
        var scrollAfterRows = attrs.scrollAfterRows;
        var tabindex = attrs.tabindex;
        var maxWidth = attrs.maxWidth;
        var required = false;
        var scope = originalScope.$new();
        scope.filterAfterRows = attrs.filterAfterRows;
        var changeHandler = attrs.change || angular.noop;

        scope.items = [];
        scope.header = "Select";
        scope.multiple = isMultiple;
        scope.disabled = false;

        scope.ulStyle = {};
        if(scrollAfterRows !== undefined && parseInt(scrollAfterRows).toString() === scrollAfterRows) {
          scope.ulStyle = {"max-height": (scrollAfterRows*26+14)+"px", "overflow-y": "auto", "overflow-x": "hidden"};
        }
        if(tabindex !== undefined && parseInt(tabindex).toString() === tabindex) {
          scope.tabindex = tabindex;
        }
        if(maxWidth !== undefined && parseInt(maxWidth).toString() === maxWidth) {
          scope.maxWidth = {"max-width": maxWidth + "px"};
        }

        originalScope.$on("$destroy", function() {
          scope.$destroy();
        });

        var popUpEl = angular.element("<multiselect-popup></multiselect-popup>");

        //required validator
        if(attrs.required || attrs.ngRequired) {
          required = true;
        }
        attrs.$observe("required", function(newVal) {
          required = newVal;
        });

        //watch disabled state
        scope.$watch(function() {
          return $parse(attrs.ngDisabled)(originalScope);
        }, function(newVal) {
          scope.disabled = newVal;
        });

        //watch single/multiple state for dynamically change single to multiple
        scope.$watch(function() {
          return $parse(attrs.multiple)(originalScope);
        }, function(newVal) {
          isMultiple = newVal || false;
        });

        //watch option changes for options that are populated dynamically
        scope.$watch(function() {
          return parsedResult.source(originalScope);
        }, function(newVal) {
          if(angular.isDefined(newVal)) {
            parseModel();
          }
        }, true);

        //watch model change
        scope.$watch(function() {
          return modelCtrl.$modelValue;
        }, function(newVal, oldVal) {
          //when directive initialize, newVal usually undefined. Also, if model value already set in the controller
          //for preselected list then we need to mark checked in our scope item. But we don't want to do this every time
          //model changes. We need to do this only if it is done outside directive scope, from controller, for example.
          if(angular.isDefined(newVal)) {
            markChecked(newVal);
            scope.$eval(changeHandler);
          }
          getHeaderText();
          modelCtrl.$setValidity("required", scope.valid());
        });

        function parseModel() {
          scope.items.length = 0;
          var model = parsedResult.source(originalScope);
          if(!angular.isDefined(model) || model === null) {
            return;
          }
          for(var i = 0; i < model.length; i++) {
            var local = {};
            local[parsedResult.itemName] = model[i];
            scope.items.push({
              label  : parsedResult.viewMapper(local),
              model  : model[i],
              checked: false,
              header : model[i][headerKey],
              divider : model[i][dividerKey]
            });
          }
        }

        parseModel();

        element.append($compile(popUpEl)(scope));

        function getHeaderText() {
          if(isEmpty(modelCtrl.$modelValue)) {
            scope.header = attrs.msHeader || "Select";
            return scope.header;
          }

          if(isMultiple) {
            if(attrs.msSelected) {
              scope.header = $interpolate(attrs.msSelected)(scope);
            } else {
              scope.header = modelCtrl.$modelValue.length + " " + "selected";
            }

          } else {
            var local = {};
            local[parsedResult.itemName] = modelCtrl.$modelValue;
            scope.header = parsedResult.viewMapper(local);
          }
        }

        function isEmpty(obj) {
          if(obj === true || obj === false) {
            return false;
          }
          if(!obj) {
            return true;
          }
          if(obj.length && obj.length > 0) {
            return false;
          }
          for(var prop in obj) {
            if(obj[prop]) {
              return false;
            }
          }
          if(compareByKey !== undefined && obj[compareByKey] !== undefined) {
            return false;
          }

          return true;
        }

        scope.valid = function validModel() {
          if(!required) {
            return true;
          }
          var value = modelCtrl.$modelValue;
          return (angular.isArray(value) && value.length > 0) || (!angular.isArray(value) && value !== null);
        };

        function selectSingle(item) {
          if(!item.checked) {
            scope.uncheckAll();
            item.checked = !item.checked;
          }
          setModelValue(false);
        }

        function selectMultiple(item) {
          item.checked = !item.checked;
          setModelValue(true);
        }

        function setModelValue(isMultiple) {
          var value;

          if(isMultiple) {
            value = [];
            angular.forEach(scope.items, function(item) {
              if(item.checked) {
                value.push(item.model);
              }
            });
          } else {
            angular.forEach(scope.items, function(item) {
              if(item.checked) {
                value = item.model;
                return false;
              }
            });
          }
          modelCtrl.$setViewValue(value);
        }

        function markChecked(newVal) {
          if(!angular.isArray(newVal)) {
            angular.forEach(scope.items, function(item) {
              item.checked = false;
              if(compareByKey === undefined && angular.equals(item.model, newVal)) {
                item.checked = true;
              } else if(compareByKey !== undefined && newVal !== null && item.model[compareByKey] !== undefined && angular.equals(item.model[compareByKey], newVal[compareByKey])) {
                item.checked = true;
              }
            });
          } else {
            angular.forEach(scope.items, function(item) {
              item.checked = false;
              angular.forEach(newVal, function(i) {
                if(compareByKey === undefined && angular.equals(item.model, i)) {
                  item.checked = true;
                } else if(compareByKey !== undefined && item.model[compareByKey] !== undefined && angular.equals(item.model[compareByKey], i[compareByKey])) {
                  item.checked = true;
                }
              });
            });
          }
        }

        scope.checkAll = function() {
          if(!isMultiple) {
            return;
          }
          angular.forEach(scope.items, function(item) {
            item.checked = true;
          });
          setModelValue(true);
        };

        scope.uncheckAll = function() {
          angular.forEach(scope.items, function(item) {
            item.checked = false;
          });
          setModelValue(true);
        };

        scope.select = function(event, item) {
          if(isMultiple === false) {
            selectSingle(item);
            scope.toggleSelect();
          } else {
            event.stopPropagation();
            selectMultiple(item);
          }
        };

        scope.activeFilter = function(a) {
          if(scope.filterActive === true) {
            if(a.checked === false) {
              return false;
            }
          }
          return true;
        };
      }
    };
  }])
  .directive("multiselectPopup", ["$document", function($document) {
    return {
      restrict   : "E",
      scope      : false,
      replace    : true,
      templateUrl: "multiselect.tpl.html",
      link       : function(scope, element, attrs) {

        scope.isVisible = false;

        scope.toggleSelect = function() {
          if(element.hasClass("open")) {
            scope.filter = "";
            scope.filterActive = false;
            element.removeClass("open");
            $document.unbind("click", clickHandler);
          } else {
            scope.filter = "";
            scope.filterActive = false;
            element.addClass("open");
            $document.bind("click", clickHandler);
          }
        };

        //				$("ul.dropdown-menu").on("click", "[data-stopPropagation]", function(e) {
        //					e.stopPropagation();
        //				});

        function clickHandler(event) {
          if(elementMatchesAnyInArray(event.target, element.find(event.target.tagName))) {
            return;
          }
          element.removeClass("open");
          $document.unbind("click", clickHandler);
          scope.$apply();
        }

        var elementMatchesAnyInArray = function(element, elementArray) {
          for(var i = 0; i < elementArray.length; i++) {
            if(element === elementArray[i]) {
              return true;
            }
          }
          return false;
        };
      }
    };
  }]);

angular.module("multiselect.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("multiselect.tpl.html",
    "<div class=\"btn-group\">\n" +
    "  <button tabindex=\"{{tabindex}}\" title=\"{{header}}\" type=\"button\" class=\"btn btn-default dropdown-toggle\" ng-click=\"toggleSelect()\" ng-disabled=\"disabled\" ng-class=\"{'error': !valid()}\">\n" +
    "    <div ng-style=\"maxWidth\" style=\"padding-right: 13px; overflow: hidden; text-overflow: ellipsis;\">{{header}}</div><span class=\"caret\" style=\"position:absolute;right:10px;top:14px;\"></span>\n" +
    "  </button>\n" +
    "  <ul class=\"dropdown-menu\" style=\"margin-bottom:30px;padding-left:5px;padding-right:5px;\" ng-style=\"ulStyle\">\n" +
    "    <input ng-show=\"items.length > filterAfterRows\" ng-model=\"filter\" style=\"width: 150px; padding: 0px 3px;margin-right: 35px; margin-bottom: 4px;\" placeholder=\"Type to filter options\">" +
    "    <input ng-show=\"items.length > filterAfterRows\" ng-model=\"filterActive\" type=\"checkbox\" style=\"position: absolute; left: 160px; top: 7px;\" title=\"Show only selected items\">" +
    "    <li data-stopPropagation=\"true\" ng-repeat=\"i in items | filter:filter | filter:activeFilter\" ng-class=\"{'dropdown-header': i.header, 'divider': i.divider}\">\n" +
    "      <a ng-if=\"!i.header && !i.divider\" ng-click=\"select($event, i)\" style=\"padding:3px 10px;cursor:pointer;\">\n" +
    "        <i class=\"glyphicon\" ng-class=\"{'glyphicon-ok': i.checked, 'empty': !i.checked}\"></i> {{i.label}}" +
    "      </a>\n" +
    "      <span ng-if=\"i.header\">{{i.label}}</span>" +
    "    </li>\n" +
    "  </ul>\n" +
    "</div>");
}]);
