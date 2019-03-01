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
  goog.provide('gn_metadata_manager_service');

  goog.require('gn_editor_xml_service');
  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_metadata_manager_service',
      ['gn_schema_manager_service', 'gn_editor_xml_service']);

  /**
   * Contains all the value of the current edited
   * metadata (id, uuid, formId, version etc..)
   */
  module.value('gnCurrentEdit', {});

  module.factory('gnEditor',
      ['$q',
       '$http',
       '$translate',
       '$compile',
       'gnUrlUtils',
       'gnXmlTemplates',
       'gnHttp',
       'gnCurrentEdit',
       function($q, $http, $translate, $compile,
               gnUrlUtils, gnXmlTemplates,
               gnHttp, gnCurrentEdit) {

         /**
         * Animation duration for slide up/down
         */
         var duration = 300;

         var isFirstElementOfItsKind = function(element) {
           return !isSameKindOfElement(element, $(element).prev().get(0));
         };
         var isLastElementOfItsKind = function(element) {
           return !isSameKindOfElement(element, $(element).next().get(0));
         };
         /**
         * Compare the element input key with the previous element input key
         * Return true if the element it the first element of its kind.
         *
         * input key is composed of schema, element name and optionally xpath.
         *
         * Example:
         * iso19139|gmd:voice|gmd:CI_Telephone|/gmd:MD_Metadata/gmd:contact
         * /gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/
         * gmd:phone/gmd:CI_Telephone/gmd:voice
         *
         */
         var isSameKindOfElement = function(element, target) {
           var elementLabel = $(element).find('input,textarea').get(0);
           var elementKey = $(elementLabel).attr('data-gn-field-tooltip');
           if (target === undefined || target.length === 0) {
             return false;
           } else {
             var childrenLabel = $(target).find('input,textarea').get(0);
             if (childrenLabel) {
               var targetKey = $(childrenLabel).attr('data-gn-field-tooltip');
               return targetKey === elementKey;
             } else {
               return false;
             }
           }
         };
         // When adding a new element and the cardinality is 0-1 or 1-1,
         // then hide the add control.
         // When an element is removed and the cardinality is 0-1 or 1-1,
         // then display the add control
         var checkAddControls = function(element, isRemoved) {
           var addElement = $(element).next();
           if (addElement !== undefined) {
             var addBlock = addElement.get(0);
             if ($(addBlock).hasClass('gn-add-field') &&
                 (($(addBlock).attr('data-gn-cardinality') === '0-1') || ($(addBlock).attr('data-gn-cardinality') === '1-1'))) {
               $(addBlock).toggleClass('hidden', isRemoved ? false : true);
             }
           }
         };

         // When adding a new element, the down control
         // of the previous element must be enabled and
         // the up control enabled only if the previous
         // element is not on top.
         var checkMoveControls = function(element) {
           var previousElement = element.prev();
           if (previousElement !== undefined) {
             var findExp = 'div.gn-move';
             var previousElementCtrl = $(previousElement
             .find(findExp).get(0)).children();

             // Up control is enabled if the previous element is
             // not on top.
             var upCtrl = previousElementCtrl.get(0);
             var isTop = isFirstElementOfItsKind(previousElement.get(0));
             $(upCtrl).toggleClass('invisible', isTop);

             // Down control is on because we have a new element below.
             var downCtrl = previousElementCtrl.get(1);
             $(downCtrl).removeClass('invisible');
           }
         };
         var setStatus = function(status) {
           gnCurrentEdit.savedStatus = $translate.instant(status.msg);
           gnCurrentEdit.savedTime = moment();
           gnCurrentEdit.saving = status.saving;
         };

         // Remove XML header
         var cleanData = function(data) {
           return data.replace(/<\?xml version="1.0".*\?>\n/, '');
         };
         return {
           buildEditUrlPrefix: function(service) {
             var params = ['../api/records/',
               gnCurrentEdit.id, '/', service, '?'];
             gnCurrentEdit.tab ?
             params.push('&currTab=', gnCurrentEdit.tab) :
             params.push('&currTab=', 'default');
             gnCurrentEdit.withAttributes &&
             params.push('&withAttributes=',
             gnCurrentEdit.displayAttributes);
             return params.join('');
           },
           load: function(url) {
             var defer = $q.defer();
             var scope = this;
             $http.get(url,
             {
               headers: {'Content-Type':
                 'application/x-www-form-urlencoded'}
             }).success(function(data) {

               var snippet = $(cleanData(data));
               scope.refreshEditorForm(snippet);
               gnCurrentEdit.working = false;
               defer.resolve(snippet);
             }).error(function(error) {
               setStatus({msg: 'saveMetadataError', saving: false});
               gnCurrentEdit.working = false;
               defer.reject(error);
             });
             return defer.promise;
           },
           /**
           * Save the metadata record currently in editing session.
           *
           * If refreshForm is true, then will also update the current form.
           * This is required while switching tab for example. Update the tab
           * value in the form and trigger save to update the view.
           */
           save: function(refreshForm, silent, terminate) {
             var defer = $q.defer();
             var scope = this;
             if (gnCurrentEdit.saving) {
               return;
             } else {
               if (!silent) {
                 setStatus({msg: 'saving', saving: true});
               }
             }

             $('.popover').remove();

             function getFormParameters() {
               var params = $(gnCurrentEdit.formId).serializeArray();
               var formParams = {};
               for (var i = 0; i < params.length; i++) {
                 // Combine all XML snippet in the same parameters
                 formParams[params[i].name] =
                 (formParams[params[i].name] &&
                     params[i].name.indexOf('_X') === 0) ?
                     formParams[params[i].name] + '&&&' + params[i].value :
                     params[i].value;
               }
               var serializedParams = '';
               for (var key in formParams) {
                 if (formParams.hasOwnProperty(key)) {
                   serializedParams +=
                   encodeURIComponent(key) + '=' +
                   encodeURIComponent(formParams[key]) + '&';
                 }
               }
               return serializedParams;
             };

             gnCurrentEdit.working = true;
             $http.post(
             '../api/records/' + gnCurrentEdit.id + '/editor?' +
             (gnCurrentEdit.showValidationErrors ? '&withValidationErrors=true' : '') +
             (refreshForm ? '' : '&commit=true') +
             (terminate ? '&terminate=true' : ''),
             getFormParameters(),
             {
               headers: {'Content-Type':
                 'application/x-www-form-urlencoded'}
             }).success(function(data) {

                var snippet = $(cleanData(data));
                if (refreshForm) {
                  scope.refreshEditorForm(snippet);
                }
                if (!silent) {
                  setStatus({msg: 'allChangesSaved', saving: false});
                }
                gnCurrentEdit.working = false;
                defer.resolve(snippet);
              }).error(function(error) {
                if (!silent) {
                  setStatus({msg: 'saveMetadataError', saving: false});
                }

                gnCurrentEdit.working = false;

                // Error is returned in XML format, convert it to JSON
                var x2js = new X2JS();
                var errorJson = x2js.xml_str2json(error);

                defer.reject(errorJson.apiError);
              });
             return defer.promise;
           },
           /**
           * Cancel the changes
           */
           cancel: function(refreshForm) {
             var defer = $q.defer();
             if (gnCurrentEdit.saving) {
               return;
             } else {
               setStatus({msg: 'cancelling', saving: true});
             }

             $http.delete(
             '../api/records/' + gnCurrentEdit.id + '/editor'
             ).success(function(data) {
               setStatus({msg: 'allChangesCanceled', saving: false});
               defer.resolve(data);
             }).error(function(error) {
               setStatus({msg: 'cancelChangesError', saving: false});
               defer.reject(error);
             });
             return defer.promise;
           },

           /**
           * Reload editor with the html snippet given
           * in parameter. If no snippet is provided, then
           * just reload the metadata into the form.
           */
           refreshEditorForm: function(form, startNewSession) {
             var scope = this;
             var refreshForm = function(snippet) {
               // Compiling
               var content = snippet;

               // Remove form element / To be improved
               // If not doing this, a detached DOM tree
               // is preserved in memory on Chrome.
               // Firefox look to be able to GC those objects
               // properly without removing them. There is maybe
               // references to DOM objects in the JS code which
               // make those objects not reachable by GC.
               $(gnCurrentEdit.containerId).find('*').remove();

               $(gnCurrentEdit.containerId).replaceWith(snippet);

               if (gnCurrentEdit.compileScope) {
                 // Destroy previous scope
                 if (gnCurrentEdit.formScope) {
                   gnCurrentEdit.formScope.$destroy();
                 }

                 // Update form values
                 scope.onFormLoad();

                 // Compile against a new scope
                 gnCurrentEdit.formScope =
                 gnCurrentEdit.compileScope.$new();
                 $compile(snippet)(gnCurrentEdit.formScope);
               } else {
                 scope.onFormLoad();
               }

             };
             if (form) {
               refreshForm(form);
             }
             else {
               var params = {id: gnCurrentEdit.id};

               // If a new session, ask the server to save the original
               // record and update session start time
               if (startNewSession) {
                 angular.extend(params, {starteditingsession: 'yes'});
                 gnCurrentEdit.sessionStartTime = moment();
               }
               $http.get('../api/records/' + gnCurrentEdit.id + '/editor',
               params).then(function(data) {
                 refreshForm($(data.data));
               });
             }
           },
           setVersion: function(version) {
             $(gnCurrentEdit.formId).
             find('input[id="version"]').val(version);
           },
           /**
           * Called after the edit form has been loaded.
           * Fill gnCurrentEdit all the info of the current
           * editing session.
           */
           onFormLoad: function() {
             var getInputValue = function(id) {
               return $(gnCurrentEdit.formId).
               find('input[id="' + id + '"]').val();
             };

             var extent = [], value = getInputValue('extent');
             try {
               extent = angular.fromJson(value);
             } catch (e) {
               console.warn(
               'Failed to parse the following extent as JSON: ' +
               value);
             }
             angular.extend(gnCurrentEdit, {
               isService: getInputValue('isService') == 'true',
               isTemplate: getInputValue('template'),
               mdTitle: getInputValue('title'),
               mdLanguage: getInputValue('language'),
               mdOtherLanguages: getInputValue('otherLanguages'),
               showValidationErrors:
               getInputValue('showvalidationerrors') == 'true',
               uuid: getInputValue('uuid'),
               displayAttributes:
               getInputValue('displayAttributes') == 'true',
               displayTooltips:
               getInputValue('displayTooltips') == 'true',
               displayTooltipsMode:
               getInputValue('displayTooltipsMode') || '',
               schema: getInputValue('schema'),
               version: getInputValue('version'),
               tab: getInputValue('currTab'),
               geoPublisherConfig:
               angular.fromJson(getInputValue('geoPublisherConfig')),
               extent: extent,
               isMinor: getInputValue('minor') === 'true',
               layerConfig:
               angular.fromJson(getInputValue('layerConfig')),
               saving: false
             });

             gnCurrentEdit.allLanguages = {code2iso: {}, iso2code: {}, iso: []};
             if (gnCurrentEdit.mdOtherLanguages != '') {
               angular.forEach(JSON.parse(gnCurrentEdit.mdOtherLanguages), function(code, iso) {
                 gnCurrentEdit.allLanguages.code2iso[code] = iso;
                 gnCurrentEdit.allLanguages.iso2code[iso] = code;
                 gnCurrentEdit.allLanguages.iso.push(iso);

               });
             }

             if (angular.isFunction(gnCurrentEdit.formLoadExtraFn)) {
               gnCurrentEdit.formLoadExtraFn();
             }
           },
           //TODO : move edit services to new editor service
           /**
           * Add another element or attribute
           * of the same type to the metadata record.
           *
           * Position could be: after (default) or before
           *
           * When attribute is expanded, the returned element contains the field
           * and the element is replaced by the new one with the attribute
           * requested.
           */
           add: function(metadataId, ref, name,
           insertRef, position, attribute) {
             // for element: md.elem.add?id=1250&ref=41&
             //   name=gmd:presentationForm
             // for attribute md.elem.add?id=19&ref=42&name=gco:nilReason
             //                  &child=geonet:attribute

             var attributeAction = attribute ? '&child=geonet:attribute' : '';

             var defer = $q.defer();
             $http.put(this.buildEditUrlPrefix('editor/elements') +
             '&displayAttributes=' + gnCurrentEdit.displayAttributes +
             '&ref=' + ref + '&name=' + name + attributeAction)
              .success(function(data) {
               // Append HTML snippet after current element - compile Angular
               var target = $('#gn-el-' + insertRef);
               var snippet = $(cleanData(data));

               if (attribute) {
                 target.replaceWith(snippet);
               } else {
                 // If the element was a add button
                 // without any existing element, the
                 // gn-extra-field indicating a not first field
                 // was not set. After add, set the css class.
                 if (target.hasClass('gn-add-field')) {
                   target.addClass('gn-extra-field');
                 }
                 snippet.css('display', 'none');   // Hide
                 target[position || 'after'](snippet); // Insert
                 snippet.slideDown(duration, function() {});   // Slide

                 // Adapt the add & move element
                 checkAddControls(snippet);
                 checkMoveControls(snippet);
               }
               $compile(snippet)(gnCurrentEdit.formScope);
               defer.resolve(snippet);

             }).error(function(data) {
               defer.reject(data);
             });

             return defer.promise;
           },
           addChoice: function(metadataId, ref, parent, name,
           insertRef, position) {
             var defer = $q.defer();
             $http.put(this.buildEditUrlPrefix('editor/elements') +
             '&displayAttributes=' + gnCurrentEdit.displayAttributes +
             '&ref=' + ref +
             '&name=' + parent +
             '&child=' + name).success(function(data) {
               // Append HTML snippet after current element - compile Angular
               var target = $('#gn-el-' + insertRef);
               var snippet = $(cleanData(data));

               if (target.hasClass('gn-add-field')) {
                 target.addClass('gn-extra-field');
               }
               snippet.css('display', 'none');   // Hide
               target[position || 'before'](snippet); // Insert
               snippet.slideDown(duration, function() {});   // Slide

               checkAddControls(snippet);
               checkMoveControls(snippet);

               $compile(snippet)(gnCurrentEdit.formScope);
               defer.resolve(snippet);
             }).error(function(data) {
               defer.reject(data);
             });
             return defer.promise;
           },
           remove: function(metadataId, ref, parent, domRef) {
             // md.element.remove?id=<metadata_id>&ref=50&parent=41
             // Call service to remove element from metadata record in session
             var defer = $q.defer();
             $http.delete('../api/records/' + gnCurrentEdit.id +
             '/editor/elements?ref=' + ref +
             '&displayAttributes=' + gnCurrentEdit.displayAttributes +
             '&parent=' + parent)
              .success(function(data) {
               // For a fieldset, domref is equal to ref.
               // For an input, it may be different because
               // the element to remove is the parent of the input
               // element (eg. gmd:voice for a gco:CharacterString)
               domRef = domRef || ref;
               // The element to remove from the DOM
               var target = $('#gn-el-' + domRef);

               // When adding a new element, the down control
               // of the previous element must be enabled and
               // the up control enabled only if the previous
               // element is not on top.
               var checkMoveControls = function(element) {
                 // If first element with down only
                 //  apply this to the next one
                 var isFirst = isFirstElementOfItsKind(element);
                 if (isFirst) {
                   var next = $(element).next().get(0);
                   var elementCtrl = $(next).find('div.gn-move').get(0);
                   var ctrl = $(elementCtrl).children();
                   $(ctrl.get(0)).addClass('invisible');

                   if ($(next).hasClass('gn-extra-field')) {
                     $(next).removeClass('gn-extra-field');
                   }
                 } else {
                   // If middle element with up and down
                   // do nothing

                   // If last element with up only
                   //  apply this to previous
                   var isLast = isLastElementOfItsKind(element);
                   if (isLast) {
                     var prev = $(element).prev().get(0);
                     var elementCtrl = $(prev).find('div.gn-move').get(0);
                     var ctrl = $(elementCtrl).children();
                     $(ctrl.get(1)).addClass('invisible');

                     var next = $(element).next();
                     if (next.hasClass('gn-add-field')) {
                       next.removeClass('gn-extra-field');
                     }
                   }
                 }
               };

               checkAddControls(target.get(0), true);
               checkMoveControls(target.get(0));

               target.slideUp(duration, function() { $(this).remove();});

               // TODO: Take care of moving the + sign
               defer.resolve(data);
             }).error(function(data) {
               defer.reject(data);
             });
             return defer.promise;
           },
           removeAttribute: function(metadataId, ref) {
             var defer = $q.defer();
             $http.delete('../api/records/' + gnCurrentEdit.id +
             '/editor/attributes?ref=' + ref.replace('COLON', ':'))
              .success(function(data) {
               var target = $('#gn-attr-' + ref);
               target.slideUp(duration, function() { $(this).remove();});
             });
             return defer.promise;
           },
           /**
           * Move an element according to the direction defined.
           * Call the service to apply the change to the metadata,
           * switch HTML element if domelementToMove defined.
           */
           move: function(ref, direction, domelementToMove) {
             // md.element.up?id=<metadata_id>&ref=50
             var defer = $q.defer();


             var swapMoveControls = function(currentElement,
                                            switchWithElement) {
               var findExp = 'div.gn-move';
               var currentElementCtrl = $(currentElement
                .find(findExp).get(0)).children();
               var switchWithElementCtrl = $(switchWithElement
                .find(findExp).get(0)).children();

               // For each existing up/down control transfer
               // the hidden class between the two elements.
               angular.forEach(switchWithElementCtrl, function(ctrl, idx) {
                 var ctrl2 = currentElementCtrl[idx];
                 var ctrlHidden = $(ctrl).hasClass('invisible');
                 var ctrl2Hidden = $(ctrl2).hasClass('invisible');
                 $(ctrl).toggleClass('invisible', ctrl2Hidden);
                 $(ctrl2).toggleClass('invisible', ctrlHidden);
               });

               var hasClass = currentElement.hasClass('gn-extra-field');
               var hasClass2 = switchWithElement.hasClass('gn-extra-field');
               currentElement.toggleClass('gn-extra-field', hasClass2);
               switchWithElement.toggleClass('gn-extra-field', hasClass);
             };

             $http.put(this.buildEditUrlPrefix('editor/elements/' + direction) +
             '&ref=' + ref)
              .success(function(data) {
               // Switch with previous element
               if (domelementToMove) {
                 var currentElement = $('#gn-el-' + domelementToMove);
                 var switchWithElement;
                 if (direction === 'up') {
                   switchWithElement = currentElement.prev();
                   switchWithElement.insertAfter(currentElement);
                 } else {
                   switchWithElement = currentElement.next();
                   switchWithElement.insertBefore(currentElement);
                 }
                 swapMoveControls(currentElement, switchWithElement);
               }
               defer.resolve(data);
             }).error(function(data) {
               defer.reject(data);
             });
             return defer.promise;
           },
           view: function(md) {
             window.open('../../?uuid=' + md['geonet:info'].uuid,
             'gn-view');
           },
           edit: function(md) {
             location.href = 'catalog.edit?#/metadata/' +
             md['geonet:info'].id;
           },
           getRecord: function(uuid) {
             var defer = $q.defer();
             $http.get('../api/records/' + uuid).
             success(function(data) {
               defer.resolve(data);
             }).
             error(function(data) {
               //                TODO handle error
               //                defer.reject(error);
             });
             return defer.promise;
           },
           /**
           * Build a field name for an XML field
           */
           buildXMLFieldName: function(elementRef, elementName) {
             var t = ['_X', elementRef,
               '_', elementName.replace(':', 'COLON')];
             return t.join('');
           }
         };
       }]);
})();
