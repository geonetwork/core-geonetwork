package org.fao.geonet.services.metadata.format.groovy;

/**
 * Represents the value of one of the parameters passed to the Format Service.
 *
 * @author Jesse on 10/17/2014.
 */
public class ParamValue {
    final String value;

    public ParamValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public boolean toBool() {
        String processedValue = this.value == null ? "false" : this.value;
        if (processedValue.equalsIgnoreCase("yes") || processedValue.equalsIgnoreCase("y") || processedValue.equals("1")) {
            return true;
        }
        return Boolean.parseBoolean(processedValue);
    }

    public int toInt() {
        return Integer.parseInt(this.value);
    }

    public Double toDouble() {
        return Double.parseDouble(this.value);
    }
}
