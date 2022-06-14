/*
 * Copyright (C) 2020 Food and Agriculture Organization of the
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
 * Contact:
 *       David Blasby - GeoCat
 *
 */



/*
 * This directive allows for a single metadata-field to be broken up into multiple fields on the editor.
 *
 *  This is best to see as an example - the "Government of Canada" OrganisationName editor.
 *
 *  In the metadata schema, this is just a string.
 *
 *  However, it is to be formated like;
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
 *    c) refs - this is the refs that should be sent back to the server to update fields
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
 *           + numberOfSuggestions is the number of suggestions to display (if not set, defaults to 20)
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
  * * {
     *   "combiner":"; ",
     *   "root_id":"14",
     *   "refs":
     *          {
     *            "eng":"15" ,           # default document lang - this is ref to <CharacterString>
     *            "fra":"lang_fra_14"    # missing lang - this will be created in record if set
     *          }
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
     *         "thesaurus": "external.theme.EC_Government_Titles",
     *         "numberOfSuggestions: 200,
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
     * Typical useage (sent to browser in "editor?" markup)
     *
     *
     *
     *               <div class="form-group gn-field gn-organisationName">
     *                     <label for="orgname" class="col-sm-2 control-label"
     *                            data-gn-field-tooltip="iso19139.ca.HNAP|gmd:organisationName">
     *                        <label>Organization Name</label>
     *                     </label>
     *                     <div data-gn-multientry-combiner="  ... SEE ABOVE ...     "
     *                          class="col-sm-9 col-xs-11 gn-value nopadding-in-table"
     *                         data-label="$labelConfig/label"/>
     *                     <div class="col-sm-1 gn-control"/>
    *                 </div>
  */

 (function() {
   goog.provide('gn_multientry_combiner');

  var module = angular.module('gn_multientry_combiner',
      ['pascalprecht.translate']);

  module
    .directive('gnMultientryCombiner',
    ['gnCurrentEdit','gnGlobalSettings', 'gnLangs',
      function(gnCurrentEdit,gnGlobalSettings, gnLangs) {
        return {
          restrict: 'A',
          replace: true,
          templateUrl: '../../catalog/components/edit/multientrycombiner/partials/multientrycombiner.html',
          scope: {
                    configuration: '@gnMultientryCombiner'
           },
          link: function (scope, element,attrs) {

            // helper function - fix up values
            // if its a fixed values, but the user hasn't put anything in, put the fixed value in the correct location
            var fix_values = function() {
              //first, make sure missing items are ''
              var nExpectedNumber = scope.config.config.length;
              scope.individualValues =_.mapValues(scope.individualValues,function(val,key) {
                 val.length = nExpectedNumber;//extend array
                 //set any undefined to ''
                 val=_.map(val,function(v){
                    if (v===undefined)
                      return '';
                    return v.trim();//remove leading/trailing spaces
                 });
                 //put in any fixedValue
                 for (var idx =0; idx<nExpectedNumber; idx++) {
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

            scope.config = JSON.parse(scope.configuration);
            scope.currentLang = scope.config["defaultLang"];
            //get the current UI lang
            // will be "eng" or "fra"
            scope.currentUILang = gnCurrentEdit.allLanguages.iso2code[gnLangs.detectLang(
                          gnGlobalSettings.gnCfg.langDetector,
                          gnGlobalSettings
                        )].replace("#","");
            scope.root_id = scope.config.root_id;
            scope.refs = scope.config.refs;
            scope.element = element;
            //we need to do this because GN trims a trailing "; ", which causes problems
            scope.combinerSimple = scope.config.combiner.trim(); //"; " -> ";"

            //values that the user has actually selected
            scope.individualValues=_.mapValues(scope.config.values,function(val,key){
                return val.split(scope.combinerSimple); // split on simple one, then we will "fix up" trailing spaces
                });
            fix_values();

            //lang list [{lang:'eng',isolang:'eng'},{lang:'fra',isolang:'fre'}]
            scope.langs = _.map(_.keys(scope.config.values), function(l){
              return {'lang':l,'isolang':gnCurrentEdit.allLanguages.code2iso['#'+l]};
             } );

            //runs after dom renders!
            // hide all language-based inputs except the current language
            setTimeout(function () {
                  var inputs =  scope.element.find("input[lang='"+scope.currentLang+"']");
                 _.forEach(inputs,function(input) { $(input).removeClass("hidden");} );
            },0);


            //because the type-ahead control makes a lot of changes to the DOM, hiding the non-active language
            // is a bit more complicated that you would expect.
            //what we do is find the <input> for the language, and then find its <span> parent.
            // We then control the visibility of that span.
            scope.$watch('currentLang', function(newValue,oldValue) {
                  //hide all inputs
                  var inputs =  scope.element.find("input[lang]"); // all lang inputs
                  _.forEach(inputs,function(input) { $(input).addClass("hidden");} );

                  //show language-appropriate inputs
                 if (newValue){
                     var inputs =  scope.element.find("input[lang='"+newValue+"']");
                     _.forEach(inputs,function(input) { $(input).removeClass("hidden");} );
                 }
            });

            //deep watch a model change
            scope.$watch('individualValues',function(newval,oldval){
                //build the master values...
                _.forEach(_.keys(scope.config.values),function(lang){
                    //values for this lang
                    //filter out blank values -- or you'll get stuff like "org; ;" or "; abc;"
                    var vs = scope.individualValues[lang].slice();
                    while (vs[vs.length-1] =='')  // remove trailing ones only
                        vs.pop();
                     var v = vs.join(scope.config.combiner); // use full value
                     scope.config.values[lang] = v;
                });
             },true);

            //nav pill clicked - change language
            scope.changeLang = function(newLang) {
                scope.currentLang = newLang;
            };

      } //link
      }
      }//fn
    ]);

})();
