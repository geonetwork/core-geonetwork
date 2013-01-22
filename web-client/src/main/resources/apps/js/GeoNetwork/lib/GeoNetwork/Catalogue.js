/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
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
Ext.namespace('GeoNetwork');

/** api: (define)
 *  module = GeoNetwork
 *  class = Catalogue
 *  base_link = `Ext.util.Observable <http://extjs.com/deploy/dev/docs/?class=Ext.util.Observable>`_
 */
/** api: example 
 *
 *  Sample code to create a GeoNetwork catalogue connection on the
 *  same host as the web page (ig. http://localhost/geonetwork will be the
 *  default connection url):
 *
 *  .. code-block:: javascript
 *
 *    var catalogue = new GeoNetwork.Catalogue({
 *      metadataStore : GeoNetwork.data.MetadataResultsStore(),
 *      summaryStore : GeoNetwork.data.MetadataSummaryStore()
 *    });
 *
 */
/** api: constructor 
 *  .. class:: GeoNetwork(config)
 *
 *     Create a GeoNetwork catalogue client.
 *
 *
 *     Catalogue client is recommended to be running on the same
 *     host as the catalogue in order to be used without a proxy
 *     for XHR requests. Catalogue could run with a proxy but some
 *     functionnalities will not be able because they require a
 *     continuity in the session (often broken by the use of
 *     a proxy).
 *
 *     If the application need both cross domain and local
 *     domain XHR Request configure OpenLayers.ProxyHost with
 *     the following:
 *
 *     .. code-block:: javascript
 *
 *      OpenLayers.ProxyHost = function(url) {
 *        if (url.indexOf(window.location.host)!=-1) {
 *          return url;
 *        } else {
 *          return OpenLayers.ProxyHostURL + encodeURIComponent(url);
 *        }
 *      };
 *
 *
 *
 *     To be improved & discussed :
 *
 *      * i18n : generate from GeoNetwork file ?
 *      
 *      * memory usage (eg. destroy calls)
 *      
 *      * massive action : hack made embedded existing HTML page in a panel
 *      
 *      and overriding JS in Old.js
 *      
 *      * search form : remote & Z39.50
 *      
 *      * global var named catalogue required (eg. Templates) should be improved, How ?
 *      
 *      * add more events to component in order to reduce dependencies
 *      
 *      * session state ? using localStorage or cookie ?
 *      
 */
GeoNetwork.Catalogue = Ext.extend(Ext.util.Observable, {
    /** api: property[SERVERURL] 
     * ``String`` GeoNetwork catalogue host URL.
     */
    SERVERURL: null,
    
    /** api: property[URL] 
     * ``String`` GeoNetwork catalogue URL.
     */
    URL: null,
    
    /** api: property[LANG] 
     *  ``String`` Optional GeoNetwork language
     *  parameter use for all services (eg.
     *  http://localhost/geonetwork/en/xml.search).
     *  ``DEFAULT_LANG`` by default.
     */
    LANG: "en",
    
    /** api: property[LANG] 
     * ``String`` Default language
     */
    DEFAULT_LANG: "en",
    
    
    EDITOR_MODE : {
        IN_OTHER_WINDOW : 1,
        IN_EDITOR_POPUP : 2
    },
    
    editMode : 1,
    
    /** api: config[hostUrl] 
     * ``String`` Optional GeoNetwork host name.
     *  Default value is web page host name.
     *
     *  api: example
     *   .. code-block:: javascript
     *
     *     var catalogue = new GeoNetwork.Catalogue({
     *         hostUrl : 'http://www.fao.org/',
     *         servlet : 'geonetwork'
     *     });
     *
     *
     */
    hostUrl: null,
    
    /** api: config[servlet] 
     *  ``String`` Optional GeoNetwork servlet name.
     *  Default value is ``geonetwork``.
     */
    servlet: null,
    
    extentMap: null,
    
    /** api: property[services] 
     *  ``Object`` The list of GeoNetwork services
     */
    services: {},
    
    /** api: property[windowOption] 
     *  ``String`` Configuration of popup windows
     */
    windowOption: "menubar=no,location=no,toolbar=no,directories=no",
    
    /** api: property[windowName] 
     *  ``String`` Name of popup windows
     *
     *  FIXME : this is always the same, assumming the main app will
     *  always popup in the same window. Maybe that's not a good idea
     *  in order to allow multi editors for example.
     */
    windowName: "",
    
    /** api: property[startRecord] 
     *  ``Number`` Index of the first record for current search
     */
    startRecord: 1,
    
    /** api: config[mdStore]
     *  ``GeoNetwork.data.MetadataResultsStore`` A store holding
     *  search results (see :class:`GeoNetwork.data.MetadataResultsStore` )
     */
    mdStore: null,
    
    /** api: config[summaryStore]
     *  ``GeoNetwork.data.MetadataSummaryStore`` The store
     *  for search summary results  (see :class:`GeoNetwork.data.MetadataSummaryStore` ).
     */
    summaryStore: null,
    
    /** api: config[statusBarId] 
     *  ``String`` Optional identifier of an ``Ext.Element`` or a DOM element
     *  to be use to display status information.
     */
    statusBarId: null,
    
    /** api: config[mdDisplayPanelId] 
     *  ``String`` Optional identifier of a panel to use to display metadata
     */
    mdDisplayPanelId: undefined,
    /** api: config[resultsView] 
     *  ``String`` Optional metadata results view panel
     */
    resultsView: undefined,
    /** api: config[mdOverlayedCmpId] 
     *  ``String`` Not used for now
     */
    mdOverlayedCmpId: undefined,
    
    /** api: property[identifiedUser]
     *  ``Object`` Current user information and role.
     */
    identifiedUser: undefined,
    
    /** api: property[adminUser]
     *  ``Boolean`` True if user is admin
     *
     *  FIXME : How to handle login/logout the best way ?
     */
    adminUser: false,
    
    metadataEditFn: undefined,
    /** api: config[adminAppUrl]
     *  ``String`` URL to the administration interface
     *  TODO : should we go to admin service by default ?
     *
     */
    adminAppUrl: '',
    /** private: property[selectedRecords]
     *  ``Number``	Number of selected records
     */
    selectedRecords: 0,
    
    /**
     * private: property[casEnabled]
     *  ``Boolean``	If is cas is the authentication
     *  mechanism. getInfo will set this property
     */
    casEnabled : false,
    
    /** private: property[info]
     *  ``Object``  Information about the catalog retrieved from xml.info service
     */
    info: null,
    /** private: method[constructor]
     *  Initializes the catalogue connection configuration
     *  and service URL.
     *
     */
    constructor: function(config){
        config = config || {};
        Ext.apply(this, config);
        
        
        if (this.hostUrl) {
            // relative path
            if (this.hostUrl.indexOf('http') === -1) {
                this.hostUrl = window.location.href.match(/http.*\//, '') + this.hostUrl;
            }
            this.SERVERURL = this.hostUrl;
        } else {
            this.SERVERURL = 'http://' + window.location.host + '/';
        }
        
        // Set catalogue URL based on hostUrl first
        if (this.hostUrl) {
            this.URL = this.SERVERURL;
        } else if (this.servlet) {
            this.URL = this.SERVERURL + this.servlet;
        } else {
            this.URL = this.SERVERURL + 'geonetwork';
        }
        
        this.LANG = (this.lang ? this.lang : this.DEFAULT_LANG);
        
        // Register GeoNetwork services URL
        var serviceUrl = this.URL + '/srv/' + this.LANG + "/";
        this.services = {
            rootUrl: serviceUrl,
            csw: serviceUrl + 'csw',
            xmlSearch: serviceUrl + 'xml.search',
            mdSelect: serviceUrl + 'metadata.select',
            mdView: serviceUrl + 'view',
            mdXMLInsert: serviceUrl + 'xml.metadata.insert',
            mdShow: serviceUrl + 'metadata.show.embedded',
            mdMEF: serviceUrl + 'mef.export',
            mdXMLGet: serviceUrl + 'xml.metadata.get',
            mdRDFGet: serviceUrl + 'rdf.metadata.get',
            mdXMLGet19139: serviceUrl + 'xml_iso19139',
            mdXMLGetDC: serviceUrl + 'xml_dublin-core',
            mdXMLGetFGDC: serviceUrl + 'xml_fgdc-std',
            mdXMLGet19115: serviceUrl + 'xml_iso19115to19139',
            mdDuplicate: serviceUrl + 'metadata.duplicate.form',
            mdDelete: serviceUrl + 'metadata.delete',
            mdExtract: serviceUrl + 'metadata.service.extract',
            mdPrint: serviceUrl + 'pdf',
            //mdEdit : serviceUrl + 'metadata.edit',
            mdEdit: serviceUrl + 'edit',
            mdCreate: serviceUrl + 'metadata.create.new',
            mdUpdate: serviceUrl + 'metadata.update.new',
            mdProcessingXml: serviceUrl + 'xml.metadata.processing',
            mdProcessing: serviceUrl + 'metadata.processing.new',
            mdMassiveChildrenForm: serviceUrl + 'metadata.batch.children.form',
            mdAdmin: serviceUrl + 'metadata.admin.form',
            mdValidate: serviceUrl + 'xml.metadata.validate',
            mdSuggestion: serviceUrl + 'metadata.suggestion',
            mdCategory: serviceUrl + 'metadata.category.form',
            mdRelationInsert: serviceUrl + 'xml.relation.insert',
            mdRelationDelete: serviceUrl + 'xml.relation.delete',
            mdRelation: serviceUrl + 'xml.relation',
            mdGetThumbnail: serviceUrl + 'metadata.thumbnail',
            mdSetThumbnail: serviceUrl + 'metadata.thumbnail.set.new',
            mdUnsetThumbnail: serviceUrl + 'metadata.thumbnail.unset.new',
            mdImport: serviceUrl + 'metadata.xmlinsert.form',
            mdStatus: serviceUrl + 'metadata.status.form',
            mdVersioning: serviceUrl + 'metadata.version',
            subTemplateType: serviceUrl + 'subtemplate.types',
            subTemplate: serviceUrl + 'subtemplate',
            upload: serviceUrl + 'resources.upload.new',
            prepareDownload: serviceUrl + 'prepare.file.download',
            fileDisclaimer: serviceUrl + 'file.disclaimer',
            fileDownload: serviceUrl + 'file.download',
            geopublisher: serviceUrl + 'geoserver.publisher',
            login: this.URL + '/j_spring_security_check',
            logout: this.URL + '/j_spring_security_logout',
            mef: serviceUrl + 'mef.export?format=full&version=2',
            csv: serviceUrl + 'csv.search',
            pdf: serviceUrl + 'pdf.selection.search',
            harvestingStart: serviceUrl + 'xml.harvesting.start',
            harvestingStop: serviceUrl + 'xml.harvesting.stop',
            harvestingRun: serviceUrl + 'xml.harvesting.run',
            harvestingAdd: serviceUrl + 'xml.harvesting.add',
            harvestingUpdate: serviceUrl + 'xml.harvesting.update',
            harvestingRemove: serviceUrl + 'xml.harvesting.remove',
            thesaurusAdd: serviceUrl + 'thesaurus.add',
            thesaurusActivate: serviceUrl + 'thesaurus.activate',
            thesaurusDelete: serviceUrl + 'thesaurus.delete',
            thesaurusUpload: serviceUrl + 'xml.thesaurus.upload',
            thesaurusDownload: serviceUrl + 'thesaurus.download',
            thesaurusConceptAdd: serviceUrl + 'thesaurus.addelement',
            thesaurusConceptUpdate: serviceUrl + 'thesaurus.updateelement',
            thesaurusConceptDelete: serviceUrl + 'thesaurus.deleteelement',
            getIcons: serviceUrl + 'xml.harvesting.info?type=icons',
            opensearchSuggest: serviceUrl + 'main.search.suggest',
            massiveOp: {
                NewOwner: serviceUrl + 'metadata.batch.newowner.form',
                Categories: serviceUrl + 'metadata.batch.category.form',
                Delete: serviceUrl + 'metadata.batch.delete',
                Privileges: serviceUrl + 'metadata.batch.admin.form',
                Versioning: serviceUrl + 'metadata.batch.version',
                Status: serviceUrl + 'metadata.batch.status.form'
            },
            metadataMassiveUpdatePrivilege: serviceUrl + 'metadata.batch.update.privileges',
            metadataMassiveUpdateCategories: serviceUrl + 'metadata.batch.update.categories',
            metadataMassiveNewOwner: serviceUrl + 'metadata.batch.newowner',
            getMyInfo: serviceUrl + 'xml.info?type=me',
            getGroups: serviceUrl + 'xml.info?type=groups',
            getRegions: serviceUrl + 'xml.info?type=regions',
            getSources: serviceUrl + 'xml.info?type=sources',
            getUsers: serviceUrl + 'xml.info?type=users',
            getSiteInfo: serviceUrl + 'xml.info?type=site&type=auth',
            getInspireInfo: serviceUrl + 'xml.info?type=inspire',
            getIsoLanguages: serviceUrl + 'isolanguages',
            schemaInfo: serviceUrl + 'xml.schema.info',
            getZ3950repositories: serviceUrl + 'xml.info?type=z3950repositories',
            getCategories: serviceUrl + 'xml.info?type=categories',
            getHarvesters: serviceUrl + 'xml.harvesting.get',
            rate: serviceUrl + 'xml.metadata.rate',
            xmlConfig: serviceUrl + 'xml.config.get',
            admin: serviceUrl + 'admin',
            xmlError: serviceUrl + 'xml.main.error',
            searchKeyword: serviceUrl + 'xml.search.keywords',
            getThesaurus: serviceUrl + 'xml.thesaurus.getList',
            getStatus: serviceUrl + 'xml.metadata.status.values.list',
            getKeyword: serviceUrl + 'xml.keyword.get',
            searchCRS: serviceUrl + 'crs.search',
            getCRSTypes: serviceUrl + 'crs.types',
            logoAdd: serviceUrl + 'logo.add',
            logoUrl: this.URL + '/images/logos/',
            imgUrl: this.URL + '/images/',
            harvesterLogoUrl: this.URL + '/images/harvesting/'
        };
        
        // TODO : init only once required (ie. metadata show)
        this.extentMap = new GeoNetwork.map.ExtentMap();
        
        /** private: event[onSelectionChange] 
         *  Fires after the selection change.
         */
        /** private: event[afterLogin] 
         *  Fires after the user logged in.
         */
        /** private: event[afterLogout] 
         *  Fires after the user logged out.
         */
        /** private: event[afterBadLogin] 
         *  Fires after bad user logged in.
         */
        /** private: event[afterBadLogout] 
         *  Fires after bad user logged out.
         */
        this.addEvents('selectionchange', 'afterLogin', 'afterLogout', 'afterBadLogin', 'afterBadLogout', 'afterDelete');
        
        GeoNetwork.Catalogue.superclass.constructor.call(this, config);
    },
    setServiceUrl: function(serviceName, url) {
        this.services[serviceName] = this.services.rootUrl + url;
    },
    /** api: method[isIdentified]
     *
     *   FIXME : return Object
     */
    isIdentified: function(){
        return !this.identifiedUser ? false : true;
    },
    /** api: method[isAdmin]
     *  Return true if current user is an admin
     */
    isAdmin: function(){
        return this.adminUser;
    },
    /** api: method[onAfterLogin]
     *  :param e: ``Object``
     *
     *  The "onAfterLogin" listener.
     *
     *  Listeners will be called with the following arguments:
     *
     *    * ``this`` : GeoNetwork.Catalogue
     *    
     *    * ``user`` : Current user   FIXME
     *    
     */
    onAfterLogin: function(){
        this.fireEvent('afterLogin', this, this.identifiedUser);
    },
    /** api: method[onAfterBadLogin]
     *  :param e: ``Object``
     *
     *  The "onAfterBadLogin" listener.
     *
     */
    onAfterBadLogin: function(){
        this.fireEvent('afterBadLogin', this, this.identifiedUser);
    },
    /** api: method[onAfterLogout]
     *  :param e: ``Object``
     *
     *  The "onAfterLogout" listener.
     */
    onAfterLogout: function(){
        this.fireEvent('afterLogout', this, this.identifiedUser);
    },
    /** api: method[onAfterBadLogout]
     *  :param e: ``Object``
     *
     *  The "onAfterBadLogout" listener.
     */
    onAfterBadLogout: function(){
        this.fireEvent('afterBadLogout', this, this.identifiedUser);
    },
    /** api: method[onAfterDelete]
     *  :param e: ``Object``
     *
     *  The "onAfterDelete" listener.
     *
     *  Listeners will be called with the following arguments:
     *
     *    * ``this`` : GeoNetwork.Catalogue
     *    
     */
    onAfterDelete: function(){
        this.fireEvent('afterDelete', this);
    }, 
    /** api: method[onAfterRating]
     *  :param e: ``Object``
     *
     *  The "onAfterDelete" listener.
     *
     *  Listeners will be called with the following arguments:
     *
     *    * ``this`` : GeoNetwork.Catalogue
     *    
     */
    onAfterRating: function(){
        this.fireEvent('afterRating', this);
    },
    /** private: method[setSelectedRecords]
     *  :param nb: ``Number``
     *
     *  Set number of selected records
     */
    setSelectedRecords: function(nb){
        this.selectedRecords = parseInt(nb, 10);
        this.onSelectionChange();
    },
    getSelectedRecords: function(){
        return this.selectedRecords;
    },
    onSelectionChange: function(){
        this.fireEvent('selectionchange', this, this.getSelectedRecords());
    },
    /** api: method[getInfo]
     *  :param refresh: ``boolean`` force refreshing the catalog info if not available.
     *  
     *  Return catalogue information (site name, organization, id, casEnabled).
     */
    getInfo: function (refresh) {
        if (refresh || this.info === null) {
            this.info = {};
            var properties = ['name', 'organization', 'siteId', 'casEnabled'];
            var request = OpenLayers.Request.GET({
                url: this.services.getSiteInfo,
                async: false
            });
            
            if (request.responseXML) {
                var xml = request.responseXML.documentElement;
                Ext.each(properties, function(item, idx){
                    var children = xml.getElementsByTagName(item)[0];
                    if (children) {
                        this.info[item] = children.childNodes[0].nodeValue;
                    }
                }, this);
            }
            this.casEnabled = this.info.casEnabled === 'true';
        }
        return this.info;
    },
    /** api: method[getInspireInfo]
     *
     *  Return catalogue inspire information (enable, enableSearchPanel).
     */
    getInspireInfo: function(){
        var info = {};
        var properties = ['enable', 'enableSearchPanel'];
        var request = OpenLayers.Request.GET({
            url: this.services.getInspireInfo,
            async: false
        });

        if (request.responseXML) {
            var xml = request.responseXML.documentElement;
            Ext.each(properties, function(item, idx){
                var i = xml.getElementsByTagName(item)[0];
                info[item] = i && i.childNodes[0] && i.childNodes[0].nodeValue;
            });
        }
        return info;
    },
    /** private: method[updateStatus]
     *
     *  Update status bar information. Status bar could be an
     *  HTML div or an Ext.component
     *
     *  TODO : Ext.Element MUST have an update method.
     *
     */
    updateStatus: function(msg){
        if (this.statusBarId) {
            var el = Ext.getCmp(this.statusBarId);
            if (el) {
                // In case of a TextItem
                el.update(msg);
                return;
            }
            
            el = Ext.getDom(this.statusBarId);
            if (el) {
                el.innerHTML = msg;
                return;
            }
        }
    },
    /** api: method[search]
     *  :param formId: ``String`` An Ext.Form identifier.
     *  :param onSuccess: ``Function or null`` A function to trigger in
     *    case of success (Ext.emptyFn by default).
     *  :param onFailure: ``Function or null`` A function to trigger in
     *    case of failure (Ext.emptyFn by default).
     *  :param startRecord: ``Number or null`` The start record for the
     *    search (Default is 1). The number of records returned by a search
     *    is defined by an E_hits_per_page field defined in the form. If
     *    not default value is used.
     *  :param updateStore: ``Boolean`` false to not update catalogue stores.
     *    Default to true.
     *  :param metadataStore: ``GeoNetwork.data.MetadataResultsStore or GeoNetwork.data.MetadataResultsFastStore`` the metadata store to use. If undefined, catalogue metadata store
     *  :param summaryStore: ``GeoNetwork.data.MetadataSummaryStore`` the summary store to use. If undefined, the catalogue summary store.
     *  :param async: ``Boolean``   false to run in synchrone mode. Default is true.
     *  
     *  Run a search operation using GeoNetwork xml.search service.
     *  Initialize results and summary stores.
     */
    search: function(formOrParams, onSuccess, onFailure, startRecord, updateStore, metadataStore, summaryStore, async){
        
        var isCatalogueMdStore = this.metadataStore === metadataStore;
        if (isCatalogueMdStore) {
            this.updateStatus(OpenLayers.i18n('searching'));
        }
        
        if (updateStore !== false) {
            updateStore = true;
        }
        if (!startRecord) {
            startRecord = this.startRecord;
        }
        if (!onSuccess) {
            onSuccess = Ext.emptyFn;
        }
        if (!onFailure) {
            onFailure = Ext.emptyFn;
        }
        if (metadataStore === undefined) {
            metadataStore = this.metadataStore;
        }
        if (summaryStore === undefined) {
            summaryStore = this.summaryStore;
        }
        if (updateStore) {
            metadataStore.removeAll();
        }
        if (typeof formOrParams === 'object') {
            GeoNetwork.util.SearchTools.doQueryFromParams(formOrParams, this, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async);
        } else {
        	GeoNetwork.util.SearchTools.doQueryFromForm(formOrParams, this, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async);
        }
    },
    /** api: method[kvpSearch]
     *  :param query: ``String`` A KVP search string (eg. any=africa to
     *   do a full text search for africa). All GeoNetwork search terms
     *   could be used.
     *  :param onSuccess: ``Function or null`` A function to trigger in
     *   case of success (Ext.emptyFn by default).
     *  :param onFailure: ``Function or null`` A function to trigger in
     *   case of failure (Ext.emptyFn by default).
     *  :param startRecord: ``Number or null`` The start record for the
     *   search (Default is 1). The number of records returned by a search
     *   is defined by an E_hits_per_page field defined in the form. If
     *   not default value is used.
     *  :param updateStore: ``Boolean``	true to update catalogue attached stores.
     *  :param async: ``Boolean``   false to run in synchrone mode. Default is true.
     *  
     *  Run a search operation based on KVP query using GeoNetwork
     *  xml.search service. Initialize results and summary stores.
     *  KVP search could be used in fast mode in order to quickly
     *  populate a summary store (for a TagCloud for example).
     */
    kvpSearch: function(query, onSuccess, onFailure, startRecord, updateStore, metadataStore, summaryStore, async){
    
        if (updateStore !== false) {
            updateStore = true;
        }
        if (!startRecord) {
            startRecord = this.startRecord;
        }
        if (!onSuccess) {
            onSuccess = Ext.emptyFn;
        }
        if (!onFailure) {
            onFailure = Ext.emptyFn;
        }
        if (!metadataStore) {
            metadataStore = this.metadataStore;
        }
        if (!summaryStore) {
            summaryStore = this.summaryStore;
        }
        if (updateStore) {
            metadataStore.removeAll();
        }
        GeoNetwork.util.SearchTools.doQuery(query, this, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async);
    },
    /** api: method[cswSearch]
     *  :param formId: ``String`` An Ext.Form identifier
     *  :param onSuccess: ``Function or null`` A function to trigger in
     *   case of success (Ext.emptyFn by default)
     *  :param onFailure: ``Function or null`` A function to trigger in
     *   case of failure (Ext.emptyFn by default)
     *  :param startRecord: ``Number or null`` The start record for the
     *   search (Default is 1). The number of records returned by a search
     *   is defined by an E_hits_per_page field defined in the form. If
     *   not default value is used.
     *
     *  Run a CSW search operation using GeoNetwork csw service.
     *  Initialize results and summary stores.
     */
    cswSearch: function(formId, onSuccess, onFailure, startRecord){
        this.metadataCSWStore.removeAll();
        if (onSuccess) {
            onSuccess = Ext.emptyFn;
        }
        if (onFailure) {
            onFailure = Ext.emptyFn;
        }
        if (startRecord) {
            startRecord = this.startRecord;
        }
        GeoNetwork.util.CSWSearchTools.doCSWQueryFromForm(formId, this, startRecord, onSuccess, null, onFailure);
    },
    /** api: method[metadataSelect]
     *  :param uuids: ``Array(String)``	A list of uuids to select.
     *
     *  Add uuids to current user selection.
     */
    metadataSelect: function(type, uuids){
        this.setSelectedRecords(0);
        var app = this;
        var i = 0;
        
        for (i = 0; i < uuids.length; i++) {
            OpenLayers.Request.GET({
                url: this.services.mdSelect,
                params: {
                    id: uuids[i],
                    selected: type
                },
                success: function(response){
                    var nb = response.responseXML.documentElement.getElementsByTagName("Selected")[0].childNodes[0].nodeValue;
                    if (nb) {
                        app.setSelectedRecords(nb);
                    }
                },
                failure: function(response){
                    Ext.Msg.alert('Selection failed', response.responseText);
                }
            });
        }
    },
    /** api: method[metadataRate]
     *  :param uuid: ``String`` Metadata identifier
     *  :param rating: ``Number`` Rate
     *
     *  Update metadata record rating information
     */
    metadataRate: function(uuid, rating, onSuccess){
    
        OpenLayers.Request.GET({
            url: this.services.rate,
            params: {
                uuid: uuid,
                rating: rating
            },
            success: function(response){
                var nb = response.responseXML.documentElement.childNodes[0].nodeValue;
                if (onSuccess) {
                    onSuccess(nb);
                }
                
                catalogue.onAfterRating();
                
                return nb;
            },
            failure: function(response){
                Ext.Msg.alert('Rating failed', response.responseText);
            }
        });
    },
    /**	api: method[metadataSelectAll]
     *	:param onSuccess: ``Function``	A function to trigger in case of success.
     *
     *	Select all records in current search.
     */
    metadataSelectAll: function(onSuccess){
        this.metadataSelection('add-all', onSuccess);
    },
    /**	api: method[metadataSelectNone]
     *	:param onSuccess: ``Function``	A function to trigger in case of success.
     *
     *	Remove all records in current selection.
     */
    metadataSelectNone: function(onSuccess){
        this.metadataSelection('remove-all', onSuccess);
    },
    /** api: private[metadataSelection]
     *  :param type: remove-all or add-all
     *  :param onSuccess: function to trigger on success
     *
     *  Private method called after selection call operations.
     *  Use GeoNetwork selection service.
     */
    metadataSelection: function(type, onSuccess){
        // CHECKME : is this a good option ?
        var app = this;
        
        OpenLayers.Request.GET({
            url: this.services.mdSelect,
            params: {
                selected: type
            },
            success: function(response){
                var nb = response.responseXML.documentElement.getElementsByTagName("Selected")[0].childNodes[0].nodeValue;
                if (nb) {
                    app.setSelectedRecords(nb);
                }
                
                if (onSuccess) {
                    onSuccess();
                }
            },
            failure: function(response){
                // TODO app.selectedRecords =
                // response.responseXML.documentElement.getElementsByTagName("Selected")[0].childNodes[0].nodeValue;
            }
        });
    },
    /**	api: method[csvExport]
     *
     *	Export current selection in CSV format.
     */
    csvExport: function(){
        window.open(this.services.csv, this.windowName, this.windowOption);
    },
    /**	api: method[mefExport]
     *
     *	Export current selection in MEF format.
     */
    mefExport: function(){
        location.replace(this.services.mef);
    },
    /**	api: method[pdfExport]
     * :param sortField: ``String`` sort field name
     * :param sortOrder: ``String`` sort order
     *
     *	Export current selection in PDF format.
     */
    pdfExport: function(sortField, sortOrder){
        var pdfExportUrl = this.services.pdf;

        if (sortField != undefined) {
            pdfExportUrl = pdfExportUrl + "?sortBy=" + sortField;

            if (sortOrder != undefined) {
                pdfExportUrl = pdfExportUrl + "&sortOrder=" + sortOrder;
            }
        }

        location.replace(pdfExportUrl);
    },
    /** api: method[metadataShow]
     *  :param uuid: ``String`` uuid of the metadata to dislay
     *
     *  Display a metadata record in a Ext.Panel or a Ext.Window.
     *  Defined GeoNetwork.defaultViewMode variable to change default view mode. 
     *  If not set, simple mode is used.
     *  
     *  Define metadataShowFn in order to override metadata show default behavior::
     *  
     *    this.metadataShowFn(uuid, record, url, maximized, width, height);
     */
    metadataShow: function(uuid, maximized, width, height){
        // UUID may contains special character like #
        var url = this.services.mdView + '?uuid=' + escape(uuid);
        var bd = Ext.getBody();
        
        if (this.resultsView) { 
            var record = this.metadataStore.getAt(this.metadataStore.find('uuid', uuid));
            
            // No current search available with this record information
            if (!record) {
                // Retrieve information in synchrone mode
                var store = GeoNetwork.data.MetadataResultsFastStore();
                this.kvpSearch("fast=index&_uuid=" + uuid, null, null, null, true, store, null, false);
                record = store.getAt(store.find('uuid', uuid));
            }
            
            if (this.metadataShowFn) {
                this.metadataShowFn(uuid, record, url, maximized, width, height);
            } else {
                var win = new GeoNetwork.view.ViewWindow({
                    serviceUrl: url,
                    lang: this.lang,
                    currTab: GeoNetwork.defaultViewMode || 'simple',
                    printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
                    printUrl: GeoNetwork.printUrl || 'print.html',
                    catalogue: this,
                    maximized: maximized || false,
                    metadataUuid: uuid,
                    record: record,
                    resultsView: this.resultsView
                    });
                win.show(this.resultsView);
                win.alignTo(bd, 'tr-tr');
            }
        }
    },
     metadataShowById: function(id, maximized, width, height){
        var url = this.services.mdView + '?id=' + id, record;
        
        if (this.resultsView) {
            // No current search available with this record information
            if (!record) {
                // Retrieve information in synchrone mode
                var store = GeoNetwork.data.MetadataResultsFastStore();
                // TODO : This failed to load template information
                this.kvpSearch("fast=index&_id=" + id, null, null, null, true, store, null, false);
                record = store.getAt(store.find('id', id));
            }
            this.metadataShow(record ? record.get('uuid') : undefined, maximized, width, height);
        }
    },
    /** api: method[metadataXMLShow]
     *  :param uuid: ``String`` uuid of the metadata to dislay
     *
     *  Display a metadata record in a new window in XML format
     *
     */
    metadataXMLShow: function(uuid, schema){
        // Default GeoNetwork XML service
        var service = this.services.mdXMLGet;
        
        // ISO 19139 or ISO profil will be displayed in ISO19139 
        if (schema === 'iso19139') {
            service = this.services.mdXMLGet19139;
        } else if (schema === 'dublin-core') {
            service = this.services.mdXMLGetDC;
        } else if (schema === 'fgdc') {
            service = this.services.mdXMLGetFGDC;
        } else if (schema === 'iso19115') {
            service = this.services.mdXMLGet19115; // Force ISO19115 record to 19139
        }
        
        var url = service + '?uuid=' + uuid;
        window.open(url, this.windowName, this.windowOption);
    },
    /** api: method[metadataXMLShow]
     *  :param uuid: ``String`` uuid of the metadata to dislay
     *
     *  Display a metadata record in a new window in XML format
     *
     */
    metadataPrint: function(uuid){
        var url = this.services.mdPrint + '?uuid=' + uuid;
        location.replace(url);
    },
    /** api: method[metadataMEF]
     *  :param uuid: ``String`` uuid of the metadata to export in MEF format
     *
     *  Open new window to retrieve MEF file for the metadata record
     *
     */
    metadataMEF: function(uuid){
        var url = this.services.mdMEF + '?version=2&uuid=' + uuid;
        location.replace(url);
    },
    /** api: method[metadataEdit]
     *  :param uuid: ``String`` Uuid of the metadata record to edit
     *
     *  Open a metadata editor.
     *
     *  FIXME : metadata.edit service does not support uuid param
     */
    metadataEdit: function(id, create, group, child, isTemplate){
        
        switch(this.editMode) {
        case this.EDITOR_MODE.IN_EDITOR_POPUP:
            if (this.metadataEditFn) {
                this.metadataEditFn(id, create, group, child, isTemplate);
            }
        break;
        default: 
            window.open(this.services.mdEdit + '?id=' + id, this.windowName, this.windowOption);
        }
    },
    /** api: method[metadataDuplicate]
     *  :param uuid: ``String`` Uuid of the metadata to duplicate
     *
     *  Create a metadata by duplication of an existing one
     */
    metadataDuplicate: function(uuid){
        window.open(this.services.mdDuplicate + '?uuid=' + uuid, this.windowName, this.windowOption);
    },
    /** api: method[metadataCreateChild]
     *  :param uuid: ``String`` Uuid of the metadata to duplicate
     *
     *  Create a child metadata record from an existing one
     */
    metadataCreateChild: function(uuid){
        window.open(this.services.mdDuplicate + '?child=y&uuid=' + uuid, this.windowName, this.windowOption);
    },
    /** api: method[metadataDuplicateWithSchema]
     *  :param schema: ``String`` Schema
     *
     *  Create a metadata record in one of the schema
     */
    metadataDuplicateWithSchema: function(uuid, schema){
        window.open(this.services.mdDuplicate + '?uuid=' + uuid +
        "&schema=" +
        schema, this.windowName, this.windowOption);
    },
    /** api: method[metadataDelete]
     *  :param uuid: ``String`` Uuid of the metadata
     *
     *  Delete metadata record.
     */
    metadataDelete: function(uuid){
        Ext.Msg.confirm(OpenLayers.i18n('deleteRecord'), OpenLayers.i18n('deleteConfirm'), this.metadataDeleteDo, uuid);
    },
    /** api: private[metadataDeleteDo]
     *
     *  Private method called after user confirmation.
     *  Use GeoNetwork metadata.delete service.
     *
     *  FIXME : need a global var named catalogue
     *  TODO : trigger results refresh ?
     *  TODO : create a status or popup bar object to display info
     */
    metadataDeleteDo: function(btn){
        if (btn === 'yes') {
            var params = {
                uuid: this
            };
            catalogue.doAction(catalogue.services.mdDelete, params, 
                                    OpenLayers.i18n('deleteRecordSuccess'), 
                                    OpenLayers.i18n('deleteRecordFailure'), 
                                    catalogue.onAfterDelete.bind(catalogue));
        }
    },
    /** api: method[doAction]
     *  :param url: ``String`` The service URL to call
     *  :param params: ``Object`` The service parameters
     *  :param msgSuccess: ``String`` Optional popup title on success
     *  :param msgFailure: ``String`` Optional popup title on failure
     *  :param onSuccess: ``Function`` Optional function to trigger on success
     *  :param onFailure: ``Function`` Optional function to trigger on failure
     *
     *
     *  Method called to run a
     *  GeoNetwork service and popup a message after success and failure.
     *
     *  TODO : create a status or popup bar object to display info
     */
    doAction: function(url, params, msgSuccess, msgFailure, onSuccess, onFailure){
        if (url.indexOf('http') === -1) {
            url = this.services.rootUrl + url;
        }
        
        OpenLayers.Request.GET({
            url: url,
            params: params,
            success: function(response){
                if (msgSuccess) {
                    Ext.Msg.alert(msgSuccess, response.responseText);
                }
                
                if (onSuccess) {
                    onSuccess(response);
                }
            },
            failure: function(response){
                if (msgFailure) {
                    Ext.Msg.alert(msgFailure, response.responseText);
                }
                if (onFailure) {
                    onFailure(response);
                }
            }
        });
    },
    /** api: method[isLoggedIn]
     * 
     *  Get the xml.info for me. If user is not identified
     *  response xml will have a me element with an authenticated attribute. 
     *  If catalogue URL is wrong, response status is 404 (check catalogue URL).
     *  In case of exception continue catalogue connection validation
     *  using the xml.main.error service (@see checkError).
     */
    isLoggedIn: function(){
        var response = OpenLayers.Request.GET({
            url: this.services.getMyInfo,
            async: false
        }), exception, authenticated, me;
       
       me = response.responseXML.getElementsByTagName('me')[0];
       authenticated = me.getAttribute('authenticated') == 'true';
       
        // Check status and also check than an Exception is not described in the HTML response
        // in case of bad startup
        exception = response.responseText.indexOf('Exception') !== -1;
        
        if (response.status === 200 && authenticated) {
            this.identifiedUser = {
                username: me.getElementsByTagName('username')[0].innerText || me.getElementsByTagName('username')[0].textContent,
                name: me.getElementsByTagName('name')[0].innerText || me.getElementsByTagName('name')[0].textContent,
                surname: me.getElementsByTagName('surname')[0].innerText || me.getElementsByTagName('surname')[0].textContent,
                role: me.getElementsByTagName('profile')[0].innerText || me.getElementsByTagName('profile')[0].textContent
            };
            this.onAfterLogin();
            return true;
        } else if (response.status === 404) {
            this.showError(OpenLayers.i18n('connectIssue'), 
                OpenLayers.i18n('connectIssueMsg') + this.services.rootUrl + '.');
        } else if (exception) {
            this.checkError();
            return false;
        } else {
            // Reset user cookie information
            if (cookie) {
                cookie.set('user', undefined);
            }
            return false;
        }
    },
    
    /**	api: method[login]
     *	:param username: ``String`` The user name
     *	:param password: ``String`` The password for the user
     *
     *	Log in to the catalogue.
     *
     *  Fires the afterLogin or afterBadLogin events
     *
     *	TODO : GeoNetwork does not return any information about the
     *		user. Those information are required in the client side.
     */
    login: function(username, password){
        var app = this, user;
    	var intervalID;
    	var loginAttempts = 0;
    	var loginWindow;
        if (this.casEnabled) {
        	loginWindow = window.open(this.URL+'/srv/'+this.LANG+'/login.form?casLogin', '_casLogin', 'menubar=no,location=no,toolbar=no', true);
        	intervalID = setInterval(function (){
        		loginAttempts += 1;
        		if(loginAttempts > (5*60*2)) {
        			clearInterval (intervalID);
        			app.identifiedUser = undefined;
	                app.onAfterBadLogin();
        		} else if(loginWindow.closed) {
        			clearInterval (intervalID);
        			app.isLoggedIn();
        		}
        	}, 500);
        } else {
			OpenLayers.Request.POST({
			    url: this.services.login,
			    data: OpenLayers.Util.getParameterString({username: username,password: password}),
			    headers: {
			        "Content-Type": "application/x-www-form-urlencoded"
			    },
	            success: function(response){
	            	app.isLoggedIn();  // will get the user information and trigger after login event
	            },
	            failure: function(response){
	                app.identifiedUser = undefined;
	                app.onAfterBadLogin();
	                // TODO : Get Exception from GeoNetwork
	            }
	        });
        }
    },
    /**	api: method[logout]
     *	Log out from the catalogue.
     *
     *  Fires the afterLogout or afterBadLogout events
     */
    logout: function(){
    	if (this.casEnabled) {
        	window.location = this.services.logout;
        } else {
	        var app = this;
	        OpenLayers.Request.GET({
	            url: this.services.logout,
	            async: false,  // logout does not seem to work when it is asynchronous request
	            success: function(response){
	                app.identifiedUser = undefined;
	                app.onAfterLogout();
	            },
	            failure: function(response){
	                app.identifiedUser = undefined;
	                app.onAfterBadLogout();
	            }
	        });
        }
    },
    /** api: method[checkError]
     *  Check if catalogue started correctly
     *
     *  :param successCb: A callback to run on success. Argument this and response are passed as parameters.
     */
    checkError: function(successCb){
        var app = this, error, msg;
        
        OpenLayers.Request.GET({
            url: this.services.xmlError,
            success: function(response){
                error = app.parseError(response.responseXML);
                msg = error.info + " " + error.exc + "<br/>" + error.stack;
                app.showError('Catalogue error', msg);
                if (successCb) {
                    successCb(app, response);
                }
            },
            failure: function(response){
                app.showError('Catalogue error', response.status);
            }
        });
    },
    /** api: method[showError]
     *  Display an alert message box.
     *
     *  :param title: The message box title
     *  :param msg: The message in the box
     */
    showError: function(title, msg){
        Ext.MessageBox.show({
           title: title,
           msg: msg,
           buttons: Ext.MessageBox.OK,
           icon: Ext.MessageBox.ERROR
       });
    },
    /** api: method[parseError]
     *  Parse a GeoNetwork startup error exception.
     *
     *  :param xml: The xml error
     */
    parseError: function(xml){
         var error = xml.getElementsByTagName('error')[0];
                
        return {
            stack: error.getElementsByTagName('Stack')[0].firstChild.nodeValue,
            exc: error.getElementsByTagName('Exception')[0].firstChild.nodeValue,
            resource: error.getElementsByTagName('Resource')[0].firstChild.nodeValue,
            msg: error.getElementsByTagName('Message')[0].firstChild.nodeValue,
            provider: error.getElementsByTagName('Provider')[0].firstChild.nodeValue,
            info: error.getElementsByTagName('Error')[0].firstChild.nodeValue
        };
    },
    /** api: method[admin]
     *
     *  Open the administration interface according to adminAppUrl properties.
     */
    admin: function(){
        location.replace(this.adminAppUrl);
    },
    metadataImport: function(){
        location.replace(this.services.mdImport);
    },
    /**	api: method[massiveOp]
     *  :param type: Type of massive operation. One of ``NewOwner``,
     *    ``Categories``, ``Delete``, ``Privileges``
     *
     *  Load massive operation in a window
     *  align to top and centered.
     *
     *  TODO : Could we have a window like a modal box ?
     *  no bottom borders.
     *
     *  FIXME : Need work on GeoNetwork side to fix JS calls
     */
    massiveOp: function(type, cb){
        var url = this.services.massiveOp[type];
        this.modalAction(OpenLayers.i18n('massiveOp') + " - " + type, url, cb);
    },
    /** private: method[modalAction]
     *  
     *  Create a modal window and load the URL content.
     *  If no callback provided, default callback on error, close the window.
     *  
     *  TODO : retrieve error message on error (currently HTML services are
     *  called with HTML response not easy to parse)
     */
    modalAction: function(title, url, cb){
        if (url) {
            var app = this, win, defaultCb = function(el, success, response, options) {
                if (!success){
                    app.showError('Catalogue error', title);
                    win.close();
                }
            };
            win = new Ext.Window({
                id: 'modalWindow',
                layout: 'fit',
                width: 700,
                height: 400,
                closeAction: 'destroy',
                plain: true,
                modal: true,
                draggable: false,
                title: title,
                items: new Ext.Panel({
                    autoLoad: {
                        url: url,
                        callback: cb || defaultCb,
                        scope: win
                    },
                    border: false,
                    frame: false,
                    autoScroll: true
                })
            });
            win.show(this);
            win.alignTo(Ext.getBody(), 't-t');
            
        }
    },
    /** api: method[metadataAdmin]
     *  Metadata admin form for privileges
     */
    metadataAdmin: function(id){
        var url = this.services.mdAdmin + "?id=" + id;
        this.modalAction(OpenLayers.i18n('setPrivileges'), url);
    },
    /** api: method[metadataStatus]
     *  Change status for this metadata
     */
    metadataStatus: function(id){
        var url = this.services.mdStatus + "?id=" + id;
        this.modalAction(OpenLayers.i18n('setStatus'), url);
    },
    /** api: method[metadataVersioning]
     *  Active versioning for this metadata
     */
    metadataVersioning: function(id){
        var url = this.services.mdVersioning + "?id=" + id;
        this.modalAction(OpenLayers.i18n('setVersioning'), url);
    },
    /** api: method[metadataCategory]
     *  Metadata admin form for categories
     */
    metadataCategory: function(id){
        var url = this.services.mdCategory + "?id=" + id;
        this.modalAction(OpenLayers.i18n('setCategories'), url);
    },
    /** api: method[metadataPrepareDownload]
     *  Prepare download metadata popup
     */
    metadataPrepareDownload: function(id){
        var url = this.services.prepareDownload + "?id=" + id;
        this.modalAction(OpenLayers.i18n('prepareDownload'), url);
    }
});

/** api: xtype = gn_catalogue */
Ext.reg('gn_catalogue', GeoNetwork.Catalogue);
