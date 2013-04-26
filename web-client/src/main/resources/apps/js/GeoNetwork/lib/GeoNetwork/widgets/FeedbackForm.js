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
      *  class = FeedbackForm
      */

            var generalFeedbackItemsInit = function() {
            return [
                    new Ext.form.FieldSet({
                            title: OpenLayers.i18n('feedbackcontactDetails'),
                        collapsible: true,
                        height:250,
                        items: [
                            {
                                    xtype: 'textfield',
                                fieldLabel : OpenLayers.i18n('feedbacknameInitials'),
                                name: 'name',
                                allowBlank : false,
                                emptyText : ''
                        },
                    {
                        xtype: 'radiogroup',
                            width:200,
                            items: [
                                {
                                        boxLabel: OpenLayers.i18n('feedbackmale'),
                                    name: 'sex',
                                    inputValue: 'm',
                                    checked: true
                            },
                        {
                            boxLabel: OpenLayers.i18n('feedbackfemale'),
                                name: 'sex',
                                inputValue: 'f'
                        }
                    ]
                },
                {
                    xtype: 'textfield',
                        fieldLabel : OpenLayers.i18n('feedbackposition'),
                        name: 'function',
                        allowBlank : false,
                        emptyText : ''
                },
                {
                    xtype: 'textfield',
                        fieldLabel : OpenLayers.i18n('feedbackorganization'),
                        name: 'organization',
                        allowBlank : false,
                        emptyText : ''
                },
                {
                    xtype: 'textfield',
                        fieldLabel : OpenLayers.i18n('feedbackphone'),
                        name: 'phone',
                        allowBlank : false,
                        emptyText : ''
                },
                {
                    xtype: 'textfield',
                        fieldLabel : OpenLayers.i18n('feedbackemail'),
                        name: 'email',
                        vtype:'email',
                        allowBlank : false,
                        emptyText : ''
                }
            ]
        }),
        new Ext.form.FieldSet({
                title: OpenLayers.i18n('feedbackfeedback'),
                collapsible: true,
                height:180,
                items: [
                    new Ext.form.ComboBox({
                            fieldLabel: OpenLayers.i18n('feedbackfunction'),
                        hiddenName: 'feedback-function',
                        store: new Ext.data.SimpleStore({
                            fields: ['feedback-function'],
                            data : [
                                [OpenLayers.i18n('feedbackquestion')],
                                [OpenLayers.i18n('feedbackerror')],
                                [OpenLayers.i18n('feedbackremark')],
                                [OpenLayers.i18n('feedbackcontact')]
                                ]
                    }),
                    displayField: 'feedback-function',
                        valueField: 'feedback-function',
                        value: OpenLayers.i18n('feedbackquestion'),
                        typeAhead: true,
                        mode: 'local',
                        triggerAction: 'all',
                        selectOnFocus:true
                }),
                new Ext.form.ComboBox({
                        fieldLabel: OpenLayers.i18n('feedbackcategory'),
                        hiddenName: 'category',
                        store: new Ext.data.SimpleStore({
                            fields: ['category'],
                            data : [
                                [OpenLayers.i18n('feedbackmetadataContent')],
                                [OpenLayers.i18n('feedbackdataContent')],
                                [OpenLayers.i18n('feedbackserviceContent')],
                                [OpenLayers.i18n('feedbacksupport')],
                                [OpenLayers.i18n('feedbackmapViewer')],
                                [OpenLayers.i18n('feedbacksearch')],
                                [OpenLayers.i18n('feedbackorganizations')],
                                [OpenLayers.i18n('feedbackcontentManagement')],
                                [OpenLayers.i18n('feedbackmetadataImport')],
                                [OpenLayers.i18n('feedbackmetadataEdit')],
                                [OpenLayers.i18n('feedbackharvesting')],
                                [OpenLayers.i18n('feedbackvalidator')],
                                [OpenLayers.i18n('feedbackother')]
                                ]
                    }),
                    displayField: 'category',
                        valueField: 'category',
                        value: OpenLayers.i18n('feedbackmetadataContent'),
                        typeAhead: true,
                        mode: 'local',
                        triggerAction: 'all',
                        selectOnFocus:true
                }),
                {
                    xtype: 'textarea',
                        fieldLabel : OpenLayers.i18n('feedbackremarks'),
                        name: 'remarks',
                        allowBlank : false,
                        emptyText : '',
                        width:300
                }
            ]
        })

        ];
}

    var metadataFeedbackItemsInit = function(record) {
            return [
                    new Ext.form.FieldSet({
                            title: OpenLayers.i18n('feedbackmetadataData'),
                        collapsible: true,
                        height:120,
                        items: [
                            {
                                    xtype: 'displayfield',
                                fieldLabel: OpenLayers.i18n('feedbacktitle'),
                                name: 'title',
                                value: record.get('title')
                        },
                    {
                        xtype: 'hidden',
                            name: 'title',
                            value: record.get('title')
                    },
                {
                    xtype:'hidden',
                        name: 'date',
                        value: record.get('changedate')
                },
                {
                    xtype: 'hidden',
                        name: 'uuid',
                        value: record.get('uuid')
                },
                {
                    xtype: 'displayfield',
                        fieldLabel: OpenLayers.i18n('feedbackorganization'),
                        name: 'metadataorganisation',
                        value: record.get('organization')
                },
                {
                    xtype: 'hidden',
                        name: 'metadataorganisation',
                        value: record.get('organization')
                },
                {
                    xtype: 'hidden',
                        name: 'metadataemail',
                        value: record.get('email')
                }
            ]
        })];
    };

    var feedbackPanelInit = function(record) {
        var metadataFeedbackItems = [];
        if(record) {
                metadataFeedbackItems = metadataFeedbackItemsInit(record);
            }
        var generalFeedbackItems = generalFeedbackItemsInit();
        var feedbackItems = metadataFeedbackItems.concat(generalFeedbackItems);
        return new Ext.form.FormPanel({
                id : 'feedbackPanel',
                xtype : 'form',
                width : 600,
                height : 750,
                title : OpenLayers.i18n('feedbackPanelTitle'),
                frame : true,
                bodyStyle : 'padding: 6px',
                labelWidth : 200,
                defaults : {
                    msgTarget : 'under',
                        anchor : '-20'
                },
            items : [
                    feedbackItems
                    ],
                buttons: [
                    {
                            text: OpenLayers.i18n('feedbacksubmit'),
                        handler: function() {
                            if (feedbackPanel.getForm().isValid()) {
                                    feedbackPanel.getForm().submit({
                                            url: catalogue.services.feedback, // FIXME : global catalogue var
                                            scope: this,
                                            success: function(fp, action){
                                                // this doesn't close the window
                                                    //if (this.ownerCt) {
                                                        //    this.ownerCt.close();
                                                            //}
                                                                Ext.getCmp('feedbackWin').destroy();
                                            },
                                        failure: function(response){
                                                Ext.Msg.show({
                                                        title: OpenLayers.i18n('feedbackerrorTitle'),
                                                        msg: OpenLayers.i18n('feedbackerrorMsg'),
                                                        width: 300,
                                                        buttons: Ext.MessageBox.OK,
                                                        icon: Ext.MessageBox.ERROR
                                                });
                                            Ext.getCmp('feedbackWin').destroy();
                                        }
                                });
                        }
                    }
            },
            {
                text: OpenLayers.i18n('feedbackcancel'),
                    handler: function() {
                        Ext.getCmp('feedbackWin').hide();
                    }
            }
        ]
    });
}

    var feedbackPanel;

    /** api: constructor
      *  .. class:: FeedbackForm()
      *
      */
        GeoNetwork.FeedbackForm = function(config, record) {
        var config = config || {} ;
        feedbackPanel = feedbackPanelInit(record);
        Ext.applyIf(config, {
                title: OpenLayers.i18n('feedbackWindowTitle'),
                closeAction : 'hide',
                id : 'feedbackWin',
                height : 750,
                width : 650,
                constrain : true,
                items: [
                    feedbackPanel
                    ]
        });
    GeoNetwork.FeedbackForm.superclass.constructor.call(this, config);
    this.window = this;
};

    Ext.extend(GeoNetwork.FeedbackForm, Ext.Window);

    /** api: xtype = gn_feedbackform */
        Ext.reg('gn_feedbackform', GeoNetwork.FeedbackForm);