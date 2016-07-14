<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">


  <xsl:template mode="generate-tpl" match="metadata-view">
    <div class="modal-header">
      <h3>{{modalMD.title || modalMD.defaultTitle}}

        <button ng-click="initEditor()" ng-hide="!isEditing" class="btn pull-right">Init</button>

        <div class="btn-group pull-right" ng-hide="!isEditing">
          <button class="btn dropdown-toggle" data-toggle="dropdown">Display
            <span class="caret"></span>
          </button>
          <ul class="dropdown-menu">
            <li>
              <a href="" ng-click="setEditTab('default')">Default view</a>
            </li>
            <li>
              <a href="" ng-click="setEditTab('advanced')">Advanced view</a>
            </li>
            <li>
              <a href="" ng-click="setEditTab('xml')">XML</a>
            </li>
          </ul>
        </div>
      </h3>
    </div>
    <div class="modal-body">
      <div class="container-fluid">
        <div class="row-fluid">
          <div class="span4">
            <div class="gn-thumbnail" ng-repeat="i in modalMD.image">
              <a href="" class="thumbnail">
                <img src="{{{{i | getThumbnailUrl}}}}" alt="Overview"/>
              </a>
            </div>
            <div ng-hide="!links">
              <h5>Related resources:</h5>
              <div gn-metadata-relations=""></div>
            </div>

            <button ng-click="addRelation('', $event)" ng-hide="!isEditing" class="btn btn-primary">
              Link data
            </button>

          </div>

          <!-- Basic info -->
          <div class="span8" ng-hide="snippet != ''">
            <h5>Abstract :</h5>
            <p>{{modalMD.abstract}}</p>
            <div ng-hide="!modalMD.keyword">
              <h5>Keywords :</h5>
              <span ng-repeat="k in modalMD.keyword" class="label gn-keyword">{{k}}</span>
            </div>
          </div>

          <!-- or XSL rendered viewer or editor -->
          <div class="span8" ng-bind-html-unsafe="snippet">
          </div>

        </div>
      </div>

    </div>
    <div class="modal-footer">
      <!-- information -->
      <div ng-hide="!isSaving" class="pull-left">
        <strong>Saving!</strong>
        ...
      </div>

      <div class="btn-toolbar" style="margin: 0;">

        <button ng-click="getView('basic')" class="btn" ng-disabled="isEditing">Main information
        </button>
        <button ng-click="getView('simple')" class="btn" ng-disabled="isEditing">More details
        </button>
        <xsl:if test="$isLoggedIn">

          <div class="btn-group dropup" ng-hide="isEditing">
            <button ng-click="edit('simple', $event)" ng-hide="isEditing" class="btn btn-primary"
                    data-loading-text="Loading...">Edit
            </button>
            <button ng-click="publish('', $event)" ng-hide="isEditing" class="btn btn-primary"
                    data-loading-text="Loading...">Publish
            </button>
            <button class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
              <span class="caret"></span>
            </button>
            <ul class="dropdown-menu pull-right">
              <li>
                <a href="" ng-click="">Call for review</a>
              </li>
              <li>
                <a href="" ng-click="">Tag</a>
              </li>
            </ul>
          </div>

          <div class="btn-group dropup" ng-hide="!isEditing">
            <button class="btn btn-warning" ng-click="save({{}}, $event)">Save</button>
            <button class="btn btn-warning"
                    ng-click="save({{validate: false, close: true}}, $event)">Save and close
            </button>
            <button class="btn btn-warning dropdown-toggle" data-toggle="dropdown">
              <span class="caret"></span>
            </button>
            <ul class="dropdown-menu pull-right">
              <li>
                <a href="" ng-click="save({{cancel: true}})">Cancel</a>
              </li>
              <li class="divider"></li>
              <li>
                <a href="" ng-click="save({{validate: true}}, $event)">Save and validate</a>
              </li>
            </ul>
          </div>

        </xsl:if>
        <button ng-click="close()" class="btn">Close</button>

      </div>
    </div>
  </xsl:template>


  <xsl:template mode="generate-tpl" match="metadata-relation">
    <div class="modal-header">
      <h3>Link ressources to {{modalMD.title || modalMD.defaultTitle}}</h3>
    </div>
    <div class="modal-body">
    </div>

    <div class="modal-footer">
      <button ng-click="closeRelationDialog()" class="btn">Close</button>
    </div>
  </xsl:template>


</xsl:stylesheet>
