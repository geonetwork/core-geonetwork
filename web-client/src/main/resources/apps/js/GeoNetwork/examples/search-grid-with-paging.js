var catalogue;

Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL,
        statusBarId: 'info',
        metadataStore: GeoNetwork.data.MetadataResultsFastStore(),
    });
    
    
    var searchField = new GeoNetwork.form.SearchField({
        name: 'E_any',
        width: 350,
        store: catalogue.metadataStore,
        triggerAction: function(scope){
            scope.search('searchForm', null, null);
        },
        scope: catalogue
    });
    
    var searchForm = new Ext.FormPanel({
        id: 'searchForm',
        border: false,
        renderTo: 'search-form',
        items: [
                searchField, {
                  xtype: 'hidden',
                  name: 'E__indexingError',
                  value: '1'
                }
        ]
    });
    
    
    var previousAction = new Ext.Action({
        id: 'previousBt',
        text: '&lt;&lt;',
        handler: function(){
        	var from = catalogue.startRecord - 50;
            if (from > 0) {
            	catalogue.startRecord = from;
            	catalogue.search('searchForm', null, null);
            }
        },
        scope: this
    });
    
    var nextAction = new Ext.Action({
        id: 'nextBt',
        text: '&gt;&gt;',
        handler: function(){
            catalogue.startRecord += 50;
            catalogue.search('searchForm', null, null);
        },
        scope: this
    });
    
    var bbar = new Ext.Toolbar({
        items: [previousAction, '|', nextAction, '|', {
            xtype: 'tbtext',
            text: '',
            id: 'info'
        }]
    });
    
    function renderError(value, p, record){
        return String.format(
                '{0}<br/>{1} - {2}<br/><b><a href="{3}">Edit</a></b>',
                record.data.idxMsg[0], record.data.idxMsg[1], record.data.idxMsg[2], record.data.uuid);
    }
    
    var grid = new Ext.grid.GridPanel({
        layout: 'fit',
        height: 280,
        border: false,
        renderTo: 'metadata-grid',
        store: catalogue.metadataStore,
        columns: [{
            id: 'title',
            header: 'Title',
            sortable: true,
            dataIndex: 'title'
        }, {
            id: 'idxMsg',
            header: 'idxMsg',
            sortable: true,
            renderer: renderError
        }, {
            id: 'uuid',
            header: 'Uuid',
            dataIndex: 'uuid'
        }],
        autoExpandColumn: 'idxMsg',
        listeners: {
            rowclick: function(grid, rowIndex, e){
                var data = grid.getStore().getAt(rowIndex).data;
                Ext.Msg.alert('You clicked on:', data.title);
            }
        },
        bbar: bbar
    });
    
    searchField.focus(true);
});
