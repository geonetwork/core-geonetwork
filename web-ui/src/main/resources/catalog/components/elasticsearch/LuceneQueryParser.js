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

(function () {
  goog.provide('gn_es_query_parser')

  var module = angular.module('gn_es_query_parser', [])

  module.service('gnEsLuceneQueryParser', [function () {

    this.facetGroupToLuceneQuery = function (indexKey, values) {
      return (values && values.length) ? ('+' + indexKey + ':("' + values.join('" "') + '")').trim() : ''
    }

    this.combineQueryGroups = function (queryGroups) {
      return queryGroups ? queryGroups.join(' AND ').trim() : ''
    }

    /**
     * Facet state is an object like this:
     *
     * {
     *   'tag': {
     *     'world': true,
     *     'vector': true
     *   },
     *   'availableInService' : {
     *     'availableInViewService': '+linkProtocol:\/OGC:WMS.*\/'
     *   },
     *   'resourceType': {
     *     'service': {
     *       'serviceType': {
     *         'OGC:WMS': true
     *         'OGC:WFS': false
     *       }
     *     },
     *     'download': {
     *       'serviceType': {
     *       }
     *     },
     *     'dataset': true
     *   }
     * }
     *
     * @param facetsState
     * @returns {string}
     */
    this.facetsToLuceneQuery = function (facetsState) {
      var query = []
      for (var indexKey in facetsState) {

        var query_chunk = parseStateNode(indexKey, facetsState[indexKey])
        if (query_chunk) {
          query.push(query_chunk)
        }
      }
      return this.combineQueryGroups(query)
    }

    function parseStateNode(nodeName, node, indexKey) {
      var query_string = ''
      if (angular.isObject(node)) {
        var chunks = []
        for (var p in node) {

          // nesting
          if (angular.isObject(node[p])) {
            var nextLvlKey = Object.keys(node[p])[0]
            var nextLvlState = node[p][nextLvlKey]
            if(Object.keys(nextLvlState).length) {
              var nestedChunks = [nodeName + ':' + '"' + p + '"']
              var chunk = parseStateNode(nextLvlKey, nextLvlState, nextLvlKey).trim()
              if (chunk) {
                nestedChunks.push(chunk)
              }
              chunks.push('(' + nestedChunks.join(' AND ') + ')')
            }
          } else {
            var chunk = parseStateNode(p, node[p], nodeName).trim()
            if (chunk) {
              chunks.push(chunk)
            }
          }
        }
        if (chunks && chunks.length) {
          query_string += '('
          query_string += chunks.join(' ')
          query_string += ')'
        }
      } else if (angular.isString(node)) {
        query_string += node
      } else if (node === true) {
        query_string += indexKey + ':"' + nodeName + '"'
      } else if (node === false) {
        query_string += '-' + indexKey + ':"' + nodeName + '"'
      }
      return query_string
    }

    function parseAstNode(node, facets) {
      if (!node) return

      var left = node.left
      var right = node.right
      var field = node.field
      var operator = node.operator
      var term = node.term
      var nextGroup = facets

      if (field) {
        var indexKey = field.field
        facets[indexKey] = facets[indexKey] || []
        nextGroup = facets[indexKey]
      }

      if (Array.isArray(facets)) {
        if (term) {
          facets.push(term)
        }
      } else if (typeof facets == 'object') {

      }
      parseAstNode(left, nextGroup)
      parseAstNode(right, nextGroup)

    }
  }])
})()
