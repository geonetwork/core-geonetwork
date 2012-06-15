Ext.namespace("geocat.edit.Extent");

geocat.edit.Extent = {
    searchWindow:null,
    formatsCombo:null,
    inclusionCombo:null,
    searchCombo:null,
    submit: function(xlink) {
        var self = geocat.edit.Extent;
        var format = self.formatsCombo.getValue();
        var inclusion = self.inclusionCombo.getValue();
        var xlinkWithOutFormat = xlink.split(/&format=[^&]*/).join('');
        xlinks[0].href = xlinkWithOutFormat+'&format=' + format + '&extentTypeCode=' + inclusion;
        self.searchWindow.hide();
        geocat.edit.submitXLink();
    },
    openWindow: function(name) {
        var edit = geocat.edit;
        var self = edit.Extent;
        var id = 'extentSearchCombo';
        if(self.searchWindow === null) {
            var addButton = edit.addButton(function(){self.submit(self.searchCombo.getValue());});
            var newButton = edit.createButton(function(){
                self.searchWindow.hide();
                doNewElementAction('metadata.elem.add',dialogRequest.ref,dialogRequest.name,dialogRequest.id,dialogRequest.replacement,dialogRequest.max);
            });
            var newGeographicButton = edit.createButton(
                function(){self.submit('local://xml.extent.get?wfs=default&typename=gn:non_validated&id=createNewExtent');}, 
                translate('xlink.newGeographic')
            );
            self.searchCombo = edit.createSearchCombo({
                id: id,
                service:"extent.search.list",
                addButton: addButton,
                fieldLabel: translate('popXlink.extent.search'),
                queryParam:'pattern', 
                baseParams:{method:'loose', property:'desc', numResults:25}
            });
            self.formatsCombo = edit.createArrayCombo({
                value: 'gmd_complete',
                fieldLabel: edit.capitalize(translate('format')),
                elements: [['gmd_bbox',translate('extentBbox')],['gmd_complete',translate('extentBboxAndPolygon')]]
            });
            self.inclusionCombo = edit.createArrayCombo({
                value: 'true', 
                fieldLabel: edit.capitalize(translate('extentTypeCode')),
                elements: [['true',edit.capitalize(translate('inclusion'))],['gmd_complete',edit.capitalize(translate('exclusion'))]]
            });
            self.searchWindow = edit.createWindow(self, {
                title: translate('ExtentSelectionTitle'),
                items: [self.searchCombo,self.formatsCombo, self.inclusionCombo],
                buttons: [addButton, newButton, newGeographicButton]
            });
        }

        geocat.edit.showWindow(self);
    },
    accepts:function(name) {
        var ucName = name.toUpperCase();
        return ucName == 'GMD:GEOGRAPHICELEMENT' || ucName == 'GMD:POLYGON' 
        	|| ucName == 'GMD:EXTENT' || ucName == 'GMD:SERVICEEXTENT'
    		|| ucName == 'GMD:SOURCEEXTENT' || ucName == 'GMD:SPATIALEXTENT'
    		|| ucName == 'CHE:REVISIONEXTENT';
    }
};
