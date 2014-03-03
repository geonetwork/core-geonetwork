package org.fao.geonet.domain;

/**
 * The datatype of a setting value.
 *
 * @author Jesse
 * @see Setting
 */
public enum SettingDataType {
    STRING {
        @Override
        public boolean validate(String value) {
            return true;
        }
    },
    INT {
        @Override
        public boolean validate(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    },
    BOOLEAN {
        @Override
        public boolean validate(String value) {
            try {
                Boolean.parseBoolean(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };

    public abstract boolean validate(String value);
}
