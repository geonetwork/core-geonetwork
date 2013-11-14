Ext.namespace('GeoNetwork.admin');

GeoNetwork.admin.PrivilegesPanel = Ext.extend(Ext.grid.GridPanel, {
    
    defaultConfig: {
        autoScroll: true,
        stateful: false,
        cls: 'privileges-panel',
        onlyUserGroup: false,
        batch: false
    },
    
    /** admin service url
     * ex : http://localhost:8080/geonetwork/srv/fre/xml.metadata.admin.form?id=543 **/
    url : undefined,

    /**
     * id of the metadata for which privileges are loaded
     */
    id : undefined,
    
    /**
     * Return true is the <on/> element exists in the n element where <id> 
     * match with the field name.
     * this is a reference to the Ext.data.Field.
     * 
     * Field with name 'oper0' will retrieve this element
     * 
     * <oper>
     *   <id>0</id>
     *   <on/>
     * </oper>
     * 
     * and return true
     */
    convertOper: function(v,n) {
        /**
         * Next sibling for IE7, 8
         */
        var nextElementSibling = function ( el ) {
            if (el.nextElementSibling) return el.nextElementSibling;
            do { el = el.nextSibling } while ( el && el.nodeType !== 1 );
            return el;
        };
        
        var id = this.name.charAt(this.name.length-1 );
        var idElt = Ext.DomQuery.selectNode('oper/id:nodeValue('+id+')',n);
        var isOn = idElt && nextElementSibling(idElt);
        return isOn ? true : false ;
    },
    
    /**
     * Return the basic Ext.data.Field config for <oper> elements
     */
    getOperField : function(id) {
        return { 
            name: 'oper' + id,
            type: 'bool',
            convert: this.convertOper
        }
    },
    isTopGroups : function(id) {
        if (GeoNetwork.Settings.privileges && GeoNetwork.Settings.privileges.topGroups) {
            return GeoNetwork.Settings.privileges.topGroups.indexOf(id) !== -1;
        } else {
            return false;
        }
    },
    getColumnOrder : function() {
        if (GeoNetwork.Settings.privileges && GeoNetwork.Settings.privileges.columnOrder) {
            return GeoNetwork.Settings.privileges.columnOrder;
        } else {
            return null;
        }
    },
    initComponent : function() {
        
        Ext.applyIf(this, this.defaultConfig);
        
        // Read <operations> element to get columns definition
        var operationsStore = new Ext.data.XmlStore({
            record: 'record',
            idPath: 'id',
            totalRecords: '@TotalResults',
            fields: ['id','name', 'reserved', 
                 {
                    name: 'label',
                    mapping : 'label > ' + catalogue.lang
                 }
            ]
        });
        
        // top bar with text field filter
        this.tbar = this.tbar || [{
            xtype: 'box',
            autoEl: {
                tag: 'img',
                cls: 'filter-text-icon'
            }
        },{
            xtype: 'textfield',
            width: 190,
            emptyText: OpenLayers.i18n('filterGroup'),
            enableKeyEvents: true,
            listeners: {
                'keyup' : {
                    // Filter the grid store
                    fn: function(txtF, event) {
                        store.filter('label', txtF.getValue());
                    }
                }
            }
        }];
        
        // bottom bar with submit button
        this.bbar = this.bbar || [{
            text: OpenLayers.i18n('save'),
            iconCls: 'saveMetadata',
            ctCls: 'gn-bt-main',
            handler: function() {
                var args={};
                var submitFn = function(group) {
                    
                    
                    for (var i=0;i<this.colModel.config.length-1;i++) {
                        var di = this.colModel.config[i].dataIndex;
                        if(di.indexOf('oper') == 0 && group.get(di)) {
                            // Internal groups only managed by admin and reviewer
                            if(group.id == 0 || group.id == -1 || group.id == 1) {
                                if(catalogue.canSetInternalPrivileges()) {
                                    args['_' + group.id + '_' + di.charAt(di.length-1 )] ='on';
                                }
                            } else {
                                args['_' + group.id + '_' + di.charAt(di.length-1 )] ='on';
                            }
                        }
                    }
                };
                
                // if the store is filtered, use the snapshot collection
                if(this.store.snapshot) {
                    this.store.snapshot.each(submitFn,this);
                }
                else {
                    this.store.each(submitFn,this);
                }
                
                // update privileges
                args.id = this.id;
                args.timeType = 'on';
                
                Ext.Ajax.request({
                    url : this.batch ? catalogue.services.mdBatchSaveXml : catalogue.services.mdAdminSave,
                    disableCaching: false,
                    params: args
                });
                // TODO: could be relevant to report any errors.
                
                if(this.ownerCt.getXType() == 'window') {
                    this.ownerCt.close();
                }
            },
            scope: this
        }];
        
        // empty config (store + cm) to initiate the first time the gridPanel
        // then the real grid panel is loaded on store.load callback with reconfigure()
        this.store= new Ext.data.Store();
        this.cm = new Ext.grid.ColumnModel({
            columns:[]
        });
        
        var self = this;
        
        // Store the grid is based on : contains all groups informations
        var store = new Ext.data.XmlStore({
            autoDestroy: true,
            storeId: 'privilegesStore',
            url: this.url, 
            record: 'group',
            idPath: 'id',
            totalRecords: '@TotalResults',
            sortInfo: {
                field: 'label'
            },
            fields: [
                 'name',
                 'description',  {
                     name: 'userGroup',
                     mapping: '@userGroup'
                 }, {
                     name: 'label',
                     convert: function(v,n) {
                         var label = Ext.DomQuery.selectNode('label/' + catalogue.lang,n);
                         var prefix = "";
                         var id = Ext.DomQuery.selectNode('id', n);
                         
                         if (catalogue.getNodeText(id) <= 1 || 
                                 self.isTopGroups(catalogue.getNodeText(id))) {
                             prefix = " - ";
                         }
                         var trueLabel = 
                           label ? 
                               catalogue.getNodeText(label) : 
                               catalogue.getNodeText(n.getElementsByTagName('name')[0]);
                         
                         return prefix + trueLabel + prefix;
                     }
                 },
                 this.getOperField(0),
                 this.getOperField(1),
                 this.getOperField(2),
                 this.getOperField(3),
                 this.getOperField(4),
                 this.getOperField(5),
                 this.getOperField(6), {
                     name: 'all',
                     type: 'bool',
                     defaultValue: false
                 }
                 
             ]
        });
        
        if (this.onlyUserGroup) {
            store.on('load', function () {
                this.filterBy(function (record, id) {
                    if (record.get('userGroup') === "true" || record.get('id') <= 1) {
                        return true;
                    }
                    return false;
                })
            });
        }
        
        // Load the store to get the Ext.grid.ColumnModel, the view and the datas
        store.load({
            callback: function(recs,opt,suc) {
                operationsStore.loadData(Ext.DomQuery.selectNode('operations',store.reader.xmlData));
                
                // Disable groups you d'ont have rights on
                var groupOwner = this.batch ? '' : catalogue.getNodeText(store.reader.xmlData.getElementsByTagName('groupOwner')[0]);
                var isOwner = this.batch ? '' : catalogue.getNodeText(store.reader.xmlData.getElementsByTagName('owner')[0]);
                
                var columns = [{
                    id: 'group',
                    header: OpenLayers.i18n('group'),
                    dataIndex: 'label',
                    width: 220
                }];
                
                // build column depending on operations Store or columnOrder array
                if (self.getColumnOrder()) {
                    Ext.each(self.getColumnOrder(), function(id) {
                        var recId = operationsStore.find('id',id);
                        if (recId !== undefined) {
                            var rec = operationsStore.getAt(recId);
                            columns.push({
                                 xtype: 'checkcolumn',
                                 header: rec.get('label'),
                                 dataIndex: 'oper'+rec.get('id'),
                                 width: 80,
                                 align: 'center'
                             });
                        }
                    });
                } else {
                    operationsStore.each(function(rec) {
                        columns.push({
                            xtype: 'checkcolumn',
                            header: rec.get('label'),
                            dataIndex: 'oper'+rec.get('id'),
                            width: 80,
                            align: 'center'
                        });
                    });
                }
                
                columns.push({
                    xtype: 'checkcolumn',
                    header: OpenLayers.i18n('checkAllOrNone'),
                    dataIndex: 'all',
                    width: 80,
                    align: 'center'
                });
                
                var cm = new Ext.grid.ColumnModel({
                    defaults: {
                        sortable: true,
                        hideable: false,
                        menuDisabled: true
                    },
                    columns: columns
                });
                
                // grid view to disable rows depending on rights
                this.getView().getRowClass = function(record, index) {
                    var css = record.get('userGroup') == "true" ? '' : 'privilges-not-user-groups ';
                    if(record.id == 0 || record.id == -1 || record.id == 1 || self.isTopGroups(record.id)) {
                        if(catalogue.canSetInternalPrivileges()) {
                            return css + 'privileges-internal';
                        }
                        else {
                            return css + 'privileges-grid-disable privileges-internal';
                        }
                        
                    }
                    else if(isOwner == 'false') {
                        return css + 'privileges-grid-disable';
                    }
                    else {
                        return css + '';
                    }
                };
                
                this.reconfigure(store,cm);
            },
            scope: this
        });
        GeoNetwork.admin.PrivilegesPanel.superclass.initComponent.call(this);
    }
});