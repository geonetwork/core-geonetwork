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
  goog.provide('gn_es_service');

  var module = angular.module('gn_es_service', []);

  module.service('gnESService', ['gnESFacet', function(gnESFacet) {

    this.convertLuceneParams = function(p) {
      var params = {};
      var query = {};

      var excludeFields = ['_content_type', 'fast', 'from', 'to', 'bucket', 'sortBy', 'resultType', 'facet.q', 'any'];
      var mappingFields = {
        title: 'resourceTitle',
        abstract: 'resourceAbstract',
        type: 'resourceType'
      };

      if(p.from) {
        params.from = p.from - 1;
      }
      if(p.to) {
        params.size = p.to - p.from;
      }
      if(p.any) {
        var anys = [];
        p.any.split(' ').forEach(function(v) {
          anys.push('+*' + v + '*');
        });
        query.query_string = {
          query: anys.join(' ')
        };
      }
      if(p.sortBy) {
        var sort = {};
        params.sort = [];
        if(p.sortBy != 'relevance') {
          sort[getFieldName(mappingFields, p.sortBy)] = 'asc';
          params.sort.push(sort);
        }
        params.sort.push('_score');
      }
      var match = Object.keys(p).reduce(function(output, current) {
        var value = p[current];
        if(excludeFields.indexOf(current) < 0) {
          var newName = mappingFields[current] || current;
          output[newName] = value;
        }
        return output;
      }, {});

      if(Object.keys(match).length) {
        query.term = match;
      }
      if(Object.keys(query).length) {
        params.query = query;
      }

      gnESFacet.addFacets(params, 'mainsearch');

      console.log(params);
      return params;

    }

    function getFieldName(mapping, name) {
      return mapping[name] || name;
    }

  }]);
})();
