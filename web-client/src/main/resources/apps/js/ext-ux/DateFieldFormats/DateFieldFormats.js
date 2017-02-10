Ext.ux.form.DateFieldFormats = Ext.extend(Ext.form.DateField, {
// Behaves in the same way as a Ext.form.DateField except that the 
// format used to write the output value into the field can be a subset
// of the format specified in the config. eg. if the specified format is 
// Y-m-d and the format found in the field is Y-m then the output format
// will be Y-m as this is a valid subset of Y-m-d
	subsetFormat: undefined,
	parseDate : function(value) {
        if(!value || Ext.isDate(value)){
            return value;
        }

        var v = this.safeParse(value, this.format),
            af = this.altFormats,
            afa = this.altFormatsArray;

        if (!v && af) {
            afa = afa || af.split("|");

            for (var i = 0, len = afa.length; i < len && !v; i++) {
                v = this.safeParse(value, afa[i]);
								if (v && (this.format.indexOf(afa[i]) == 0)) {
									this.subsetFormat = afa[i]; 
																				// allow format to be a subset 
                                        // of initial format eg. Y-m is fine
                                        // if this.format is Y-m-d
								}
            }
        }
        return v;
  },
	formatDate : function(date){
		var result;
		if (this.subsetFormat) {
	   result = Ext.isDate(date) ? date.dateFormat(this.subsetFormat) : date;
		 this.subsetFormat = undefined;
		} else {
	   result = Ext.isDate(date) ? date.dateFormat(this.format) : date;
		}
		return result;
	}
});
// register xtype
Ext.reg('xdatefieldformats', Ext.ux.form.DateFieldFormats);
