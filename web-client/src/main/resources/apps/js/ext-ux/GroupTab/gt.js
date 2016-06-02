/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.onReady(function() {
	Ext.QuickTips.init();
    
    // create some portlet tools using built in Ext tool ids
    var tools = [{
        id:'gear',
        handler: function(){
            Ext.Msg.alert('Message', 'The Settings tool was clicked.');
        }
    },{
        id:'close',
        handler: function(e, target, panel){
            panel.ownerCt.remove(panel, true);
        }
    }];

    var viewport = new Ext.Viewport({
        layout:'fit',
        items:[{
            xtype: 'grouptabpanel',
    		tabWidth: 130,
    		activeGroup: 0,
    		items: [{
    			mainItem: 1,
    			items: [{
    				title: 'Tickets',
                    layout: 'fit',
                    iconCls: 'x-icon-tickets',
                    tabTip: 'Tickets tabtip',
                    style: 'padding: 10px;',
    				items: []
    			}, 
                {
                    //xtype: 'portal',
                    title: 'Dashboard',
                    tabTip: 'Dashboard tabtip',
                    items:[{
                        columnWidth:.33,
                        style:'padding:10px 0 10px 10px',
                        items:[{
                            title: 'Grid in a Portlet',
                            layout:'fit',
                            tools: tools
                        },{
                            title: 'Another Panel 1',
                            tools: tools,
                            html: '<span>test</span>'
                        }]
                    },{
                        columnWidth:.33,
                        style:'padding:10px 0 10px 10px',
                        items:[{
                            title: 'Panel 2',
                            tools: tools,
                            html: '<span>test</span>'
                        },{
                            title: 'Another Panel 2',
                            tools: tools,
                            html: '<span>test</span>'
                        }]
                    },{
                        columnWidth:.33,
                        style:'padding:10px',
                        items:[{
                            title: 'Panel 3',
                            tools: tools,
                            html: '<span>test</span>'
                        },{
                            title: 'Another Panel 3',
                            tools: tools,
                            html: '<span>test</span>'
                        }]
                    }]                    
                }, {
    				title: 'Subscriptions',
                    iconCls: 'x-icon-subscriptions',
                    tabTip: 'Subscriptions tabtip',
                    style: 'padding: 10px;',
					layout: 'fit',
    				items: [{
						xtype: 'tabpanel',
						activeTab: 1,
						items: [{
							title: 'Nested Tabs',
							html: '<span>test</span>'
						}]	
					}]	
    			}, {
    				title: 'Users',
                    iconCls: 'x-icon-users',
                    tabTip: 'Users tabtip',
                    style: 'padding: 10px;',
    				html: '<span>test</span>'			
    			}]
            }, {
                expanded: true,
                items: [{
                    title: 'Configuration',
                    iconCls: 'x-icon-configuration',
                    tabTip: 'Configuration tabtip',
                    style: 'padding: 10px;',
                    html: '<span>test</span>' 
                }, {
                    title: 'Email Templates',
                    iconCls: 'x-icon-templates',
                    tabTip: 'Templates tabtip',
                    style: 'padding: 10px;',
                    html: '<span>test</span>'
                }]
            }]
		}]
    });
});