/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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



/*
 * This directive allows for a single metadata-field to be broken up into multiple fields on the add online resource
 * dialog.
 *
 *  This is best to see as an example - the "Government of Canada" OrganisationName editor.
 *
 *  In the metadata schema, this is just a string.
 *
 *  However, it is to be formatted like;
 *    "Government of Canada; Organization of Public Services and Procurement Canada; Defence and Marine Procurement"
 *
 *   The first bit "Government of Canada" is HARD CODED
 *   The second bit "Canadian Environmental Assessment Agency" is a Thesaurus Keyword Picker
 *   The third bit "Defence and Marine Procurement" is a free text field
 *
 *   This makes it easier for a user to enter the information correctly.
 *
 *   This is multi-lingual.
 *
 *   The configuration (see example, below) defines;
 *
 *    a) how the fields are joined together ("; " in this example) -- combiner
 *    b) root_id of the field in the metadata.
 *    d) values -- actual Metadata record values (one per language)
 *    e) configuration - this defines how the edit fields are shown to the user
 *        a) type - what type is this?  (fixedValue, thesaurus, freeText)
 *        b) heading - helper text to show ABOVE the field
 *        c) other configuration information
 *
 *
 * 1. fixedValue
 *     This represents something that the user cannot edit - "Government of Canada" in our example.
 *           + values is a dictionary from language-name to value
 *
 * 2. thesaurus
 *      This represents a Thesaurus KeyWord Picker.
 *           + thesaurus is the name of the thesaurus
 *
 * 3. freeText
 *      This represents a field the user can type into.
 *        + it doesn't have any specific configuration
 *
 *
 *
 *    This directive also handles the multi-lingual aspects (and puts in nav-pills like the normal multi-lingual directive).
 */

/*
 *
 * SAMPLE CONFIGURATION - AS USED BY HNAP
 *
 * {
 *   "combiner":"; ",
 *   "fieldName":"desc",
 *   "defaultLang":"eng",
 *   "values":
 *       {
 *
 *         "eng":"Government of Canada; Organization of Public Services and Procurement Canada; Defence and Marine Procurement" ,
 *         "fra":"Gouvernement du Canada; Organisation de Services publics et Approvisionnement Canada; Approvisionnement maritime et de défense"
 *       },
 *
 *   "config":
 *   [
 *       {
 *         "type": "fixedValue",
 *         "heading": {
 *             "eng": "",
 *             "fra": ""
 *         },
 *         "values": {
 *           "eng": "Government of Canada",
 *           "fra": "Gouvernement du Canada"
 *         }
 *       },
 *       {
 *         "type": "thesaurus",
 *         "heading": {
 *           "eng": "Government of Canada Organization",
 *           "fra": "Organisation du Gouvernement du Canada"
 *         },
 *         "thesaurus": "external.theme.EC_Government_Titles"
 *       },
 *       {
 *         "type": "freeText",
 *         "heading": {
 *           "eng": "Branch/Sector/Division",
 *           "fra": "Branche/Secteur/Division"
 *         }
 *       }
 *   ]
 * }
 */

/*
 * Typical usage in the schema config/associated-panel/default.json (for desc field)
 *
 *
 *
 *   {
 *      "config": {
 *        "display": "radio",
 *        "types": [{
 *          "label": "addOnlinesrc",
 *          "sources": {
 *            "filestore": true
 *          },
 *          "icon": "fa gn-icon-onlinesrc",
 *          "process": "onlinesrc-add",
 *          "fields": {
 *            "protocol": {
 *              "value": "WWW:LINK-1.0-http--link",
 *              "isMultilingual": false,
 *              "required": true,
 *              "tooltip": "gmd:protocol"
 *            },
 *            "url": {
 *              "isMultilingual": false,
 *              "required": true,
 *              "tooltip": "gmd:URL"
 *            },
 *            "name": {"tooltip": "gmd:name"},
 *            "desc": {
 *              "tooltip": "gmd:description",
 *              "directive": "gn-multientry-combiner-online-resources-description",
 *              "directiveConfig": {
 *                "valueElementId": "multicombinervalue",
 *                "fieldName": "desc",
 *                "combiner":"; ",
 *                "config":
 *                [
 *                  {
 *                    "type": "thesaurus",
 *                    "heading": {
 *                      "eng": "Content type",
 *                      "fra": "Content type"
 *                    },
 *                    "thesaurus": "external.theme.GC_Resource_ContentTypes"
 *                  },
 *                  {
 *                    "type": "thesaurus",
 *                    "heading": {
 *                      "eng": "Format",
 *                      "fra": "Format"
 *                    },
 *                    "thesaurus": "external.theme.GC_Resource_Formats"
 *                  },
 *                  {
 *                    "type": "thesaurus",
 *                    "heading": {
 *                      "eng": "Language",
 *                      "fra": "Language"
 *                    },
 *                    "thesaurus": "external.theme.GC_Resource_Languages"
 *                  }
 *                ]
 *              }
 *            },
 *            "function": {
 *              "isMultilingual": false,
 *              "tooltip": "gmd:function"
 *            },
 *            "applicationProfile": {
 *              "isMultilingual": false,
 *              "tooltip": "gmd:applicationProfile"
 *            }
 *          }
 *        }, {
 *          "label": "addThumbnail",
 *          "sources": {
 *            "filestore": true,
 *            "thumbnailMaker": true
 *          },
 *          "icon": "fa gn-icon-thumbnail",
 *          "fileStoreFilter": "*.{jpg,JPG,jpeg,JPEG,png,PNG,gif,GIF}",
 *          "process": "thumbnail-add",
 *          "fields": {
 *            "url": {
 *              "param": "thumbnail_url",
 *              "isMultilingual": false,
 *              "required": true
 *            },
 *            "name": {"param": "thumbnail_desc"}
 *          }
 *        }],
 *        "multilingualFields": ["name", "desc"]
 *      }
 *    }
*/

(function() {
  goog.provide('gn_multientry_combiner_onlineresourcesdescription');

  var module = angular.module('gn_multientry_combiner_onlineresourcesdescription',
    ['pascalprecht.translate']);

  module
    .directive('gnMultientryCombinerOnlineResourcesDescription',
      ['gnCurrentEdit','gnGlobalSettings', 'gnLangs', '$log',
        function(gnCurrentEdit,gnGlobalSettings, gnLangs, $log) {
          return {
            restrict: 'A',
            transclude: true,
            replace: true,
            templateUrl: '../../catalog/components/edit/multientrycombiner/partials/multientrycombiner_onlineresourcesdescription.html',
            scope: {
              configuration: '@gnMultientryCombinerOnlineResourcesDescription'
            },
            link: function (scope, element, attrs) {
              scope.config = JSON.parse(scope.configuration);

              if (angular.isUndefined(scope.config.fieldName) || scope.config.fieldName === '') {
                $log.error("MultiEntryCombinerOnlineResourceDescription: The fieldName configuration option is mandatory" );
                throw "The fieldName configuration option is mandatory";
              }
              var emptyLangs = {};
              _.each(_.keys(gnCurrentEdit.allLanguages.code2iso), function (code) {
                var lang = code.replace("#", "");
                emptyLangs[lang] = "";
              });
              scope.config.values = angular.extend({}, scope.$parent.params[scope.config.fieldName], emptyLangs);


              // helper function - fix up values
              // if its a fixed values, but the user hasn't put anything in, put the fixed value in the correct location
              var fix_values = function() {
                //first, make sure missing items are ''
                var nExpectedNumber = scope.config.config.length;
                scope.individualValues = _.mapObject(scope.individualValues, function(val, key) {
                  val.length = nExpectedNumber; //extend array
                  //set any undefined to ''
                  val = _.map(val, function(v) {
                    if (v === undefined)
                      return '';
                    return v.trim();//remove leading/trailing spaces
                  });
                  //put in any fixedValue
                  for (var idx =0; idx < nExpectedNumber; idx++) {
                    var meta = scope.config.config[idx];
                    if (meta.type === 'fixedValue') {  // fixed values are always the same
                      val[idx] = meta.values[key];
                    } else {  // default values -- put in if its not set (only do this at start)
                      if ( (val[idx] === '') && (meta.defaultValues) && (meta.defaultValues[key]) ) {
                        val[idx] = meta.defaultValues[key];
                      }
                    }
                  }
                  return val;
                } );
              }

              scope.currentLang = gnCurrentEdit.mdLanguage;
              //get the current UI lang
              // will be "eng" or "fra"

              scope.initCurrentLang = function() {
                var detectedLang = gnCurrentEdit.allLanguages.iso2code[gnLangs.detectLang(
                    gnGlobalSettings.gnCfg.langDetector,
                    gnGlobalSettings
                )];

                if (angular.isUndefined(detectedLang)) {
                  $log.warn("The current UI language is not present in the metadata document: " +
                      gnLangs.detectLang(gnGlobalSettings.gnCfg.langDetector, gnGlobalSettings) + ". Defaulting to " + scope.currentLang)
                  scope.currentUILang = scope.currentLang;
                } else {
                  scope.currentUILang = detectedLang.replace("#", "");
                }
              };

              scope.showFieldsAfterDomRendered = function () {
                //runs after dom renders!
                // hide all language-based inputs except the current language
                setTimeout(function () {
                  if (scope.$parent.isMdMultilingual) {
                    var inputs = scope.element.find("input[lang='" + scope.currentLang + "']");
                    if (inputs.length === 0) {
                      inputs = scope.element.find("input[lang='" + gnCurrentEdit.allLanguages.iso2code[scope.currentLang].substring(1) + "']")
                    }
                    _.each(inputs, function (input) {
                      $(input).removeClass("hidden");
                    });
                  } else {
                    var inputs = scope.element.find("input");
                    _.each(inputs, function (input) {
                      $(input).removeClass("hidden");
                    });
                  }
                }, 0);
              };

              scope.initCurrentValues = function() {
                scope.currentLang = gnCurrentEdit.mdLanguage;
                scope.initCurrentLang();
                var parentParam = scope.$parent.params[scope.config.fieldName];
                if (angular.isObject(parentParam)) {
                  // multilingual
                  scope.config.values = angular.extend({}, scope.$parent.params[scope.config.fieldName], emptyLangs);
                  scope.individualValues = _.mapObject(scope.$parent.params[scope.config.fieldName], function(val, key){
                    return val.split(scope.combinerSimple); // split on simple one, then we will "fix up" trailing spaces
                  });
                } else {
                  // single language
                  scope.individualValues = {};
                  _.mapObject(emptyLangs, function (val, key) {
                    scope.individualValues[key] = parentParam.split(scope.combinerSimple);
                  });
                }
                fix_values();
                scope.showFieldsAfterDomRendered();

                //lang list [{lang:'eng',isolang:'eng'},{lang:'fra',isolang:'fre'}]
                scope.langs = _.map(_.keys(scope.config.values), function(l){
                  return {'lang': l, 'isolang': gnCurrentEdit.allLanguages.code2iso['#' + l]};
                } );
                if (scope.langs.length === 0) {
                  scope.langs = {'lang': scope.currentLang, 'isolang': gnCurrentEdit.allLanguages.code2iso['#' + scope.currentLang]};
                }
              };

              scope.initCurrentLang();

              scope.element = element;
              //we need to do this because GN trims a trailing "; ", which causes problems
              scope.combinerSimple = scope.config.combiner.trim(); //"; " -> ";"

              scope.$on('onlineSrcDialogInited', function(event, args) {
                scope.initCurrentValues();
                scope.$broadcast('resetValue', {reset: "true"});

              });

              scope.$on('onlineSrcDialogHidden', function(evt, msg) {
                // nothing to do
              })
              //values that the user has actually selected
              scope.initCurrentValues();



              //because the type-ahead control makes a lot of changes to the DOM, hiding the non-active language
              // is a bit more complicated that you would expect.
              //what we do is find the <input> for the language, and then find its <span> parent.
              // We then control the visibility of that span.
              scope.$watch('currentLang', function(newValue, oldValue) {
                if (scope.$parent.isMdMultilingual) {
                  //hide all inputs
                  var inputs = scope.element.find("input[lang]"); // all lang inputs
                  _.each(inputs, function (input) {
                    $(input).addClass("hidden");
                  });

                  //show language-appropriate inputs
                  if (newValue) {
                    var inputs = scope.element.find("input[lang='" + newValue + "']");
                    if (inputs.length === 0) {
                      inputs = scope.element.find("input[lang='" + gnCurrentEdit.allLanguages.iso2code[newValue].substring(1) + "']")
                    }
                    _.each(inputs, function (input) {
                      $(input).removeClass("hidden");
                    });
                  }
                } else {
                  var inputs = scope.element.find("input");
                  _.each(inputs, function (input) {
                    $(input).removeClass("hidden");
                  });
                }
              });


              //deep watch a model change
              scope.$watch('individualValues', function(newval, oldval){
                //build the master values...
                _.each(_.keys(scope.config.values), function(lang){
                  //values for this lang
                  //filter out blank values -- or you'll get stuff like "org; ;" or "; abc;"
                  var vs = scope.individualValues[lang].slice();
                  while (vs[vs.length - 1] === '') {  // remove trailing ones only
                    vs.pop();
                  }
                  var v = vs.join(scope.config.combiner); // use full value
                  scope.config.values[lang] = v;


                  if (scope.$parent.isMdMultilingual) {
                    //scope.$parent.params.desc[lang] = v;
                    scope.$parent.params[scope.config.fieldName][lang] = v;
                  } else {
                    //scope.$parent.params.desc = v;
                    scope.$parent.params[scope.config.fieldName] = v;
                  }
                });
              }, true);

              //nav pill clicked - change language
              scope.changeLang = function(newLang) {
                scope.currentLang = newLang;
              };

              scope.$watch("config.values", function(newVal, oldVal, localScope) {
                var tempArray = [];
                var inputValue = '';
                if (angular.isDefined(newVal)) {
                 if (angular.isObject(newVal)) {
                   _.mapObject(newVal, function(value, lang) {
                     var item = lang + "#" + value;
                     tempArray.push(item);
                   });
                   inputValue = tempArray.join("|");
                 } else {
                   inputValue = newVal;
                 }
                } else {
                  $log.info("config.values newValue is undefined");
                }
                scope.hiddenFieldValue = inputValue;
              }, true);

              scope.$on('$destroy', function() {
                $log.debug("destroy multientrycombiner");
              });

            } //link
          }
        }//fn
      ]);

})();
