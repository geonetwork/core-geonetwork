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
 * David Blasby  -  GeoCat
 */

(function() {
  goog.provide('gn_schema_translations_loader');

  var module = angular.module('gn_schema_translations_loader',
      ['pascalprecht.translate']);

  //module.run -- init code
  module.run(['$http','$translate','$LOCALES',
    function($http,$translate,$LOCALES) {

      // add potential items like "en-schema-iso19139.ca.HNAP.json"
      $http.get('../api/standards')
        .success(function(data) {
             var locals =$LOCALES;
             var files = _.map(data,function(s){
                  $LOCALES.push("schema-"+s.name);
             });
             $translate.refresh(); // force reload
        });

    }
    ]);
    })();
