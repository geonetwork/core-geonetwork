Ext.BLANK_IMAGE_URL = window.gMfLocation + '../ext/resources/images/default/s.gif';

function fillStdToolbar(toolbar) {
    var map = toolbar.map;

    toolbar.addControl(new OpenLayers.Control.ZoomToMaxExtent({
        map: map,
        title: 'Zoom to full map extent'
    }), {
        iconCls: 'zoomfull',
        toggleGroup: 'map'
    });
    toolbar.addControl(new OpenLayers.Control.ZoomBox({
        title: 'Zoom in'
    }), {
        iconCls: 'zoomin',
        toggleGroup: 'map'
    });
    toolbar.addControl(new OpenLayers.Control.ZoomBox({
        out: true,
        title: 'Zoom out'
    }), {
        iconCls: 'zoomout',
        toggleGroup: 'map'
    });

    toolbar.add(new Ext.Toolbar.Separator());

    toolbar.addControl(new OpenLayers.Control.DragPan({
        isDefault: true,
        title: 'Pan'
    }), {
        iconCls: 'pan',
        toggleGroup: 'map'
    });
}

function createResizableMap(divId) {
    var div = Ext.get(divId);
    div.createChild({id: divId + 'Map'});
    var map = createMap(divId + 'Map', true);

    var toolbar = new mapfish.widgets.toolbar.Toolbar({
        map: map,
        configurable: false
    });
    //see http://trac.mapfish.org/trac/mapfish/ticket/126
    toolbar.autoHeight = false;
    toolbar.height = 26;

    var mapPanel = new Ext.Panel({
        renderTo: divId,
        layout: 'border',
        width: 500,
        height: 300,
        border: false,
        items: [
            {
                region: 'center',
                contentEl: divId + 'Map',
                layout: 'fit',
                tbar: toolbar
            },
            {
                region: 'east',
                title: 'Layers',
                xtype: 'layertree',
                id: divId+'LayerTree',
                map: map,
                enableDD: true,
                ascending: false,
                width: 150,
                minSize: 100,
                split: true,
                collapsible: true,
                collapsed: false,
                plugins: [
                    mapfish.widgets.LayerTree.createContextualMenuPlugin(['opacitySlide','remove'])
                ]
            }
        ]
    });
    fillStdToolbar(toolbar);


    var mapResizer = new Ext.Resizable(divId, {
        minWidth:200,
        minHeight:100
    });

    mapResizer.on('resize', function(resizable, width, height) {
        mapPanel.setSize(width, height);
        mapPanel.doLayout();
        updateMapSizes();
    });
    map.setCenter(map.getMaxExtent().getCenterLonLat(), 3);

    return {
        map: map
    };
}
