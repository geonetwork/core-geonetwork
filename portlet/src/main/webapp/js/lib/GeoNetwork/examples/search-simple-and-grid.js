var catalogue;

Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
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
        items: searchField
    });
    
    
    var grid = new Ext.grid.GridPanel({
        layout: 'fit',
        height: 280,
        border: false,
        renderTo: 'metadata-grid',
        store: catalogue.metadataStore,
        columns: [{
            id: 'title',
            header: 'Title',
            dataIndex: 'title'
        }, {
            id: 'uuid',
            header: 'Uuid',
            dataIndex: 'uuid'
        }],
        autoExpandColumn: 'title',
        listeners: {
            rowclick: function(grid, rowIndex, e){
                var data = grid.getStore().getAt(rowIndex).data;
                Ext.Msg.alert('You clicked on:', data.title);
            }
        }
    });
    
    searchField.focus(true);
});
