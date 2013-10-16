package org.fao.geonet;

public class Column implements Comparable<Column> {
    String tableName, columnName, defaultVal, isNullable, dataType, charLength;
    private boolean ignoreDefVal;

    public static Column column(String tableName, String columnName, String defaultVal, String isNullable, String dataType,
                                String charLength) {
        return column(tableName, columnName, defaultVal, isNullable, dataType, charLength, false);
    }

    public static Column column(String tableName, String columnName, String defaultVal, String isNullable, String dataType,
                                String charLength, boolean ignoreDefVal) {
        Column column = new Column();
        column.tableName = regularize(tableName, true);
        column.columnName = regularize(columnName, true);
        column.defaultVal = regularize(defaultVal, false);
        column.isNullable = regularize(isNullable, true);
        column.dataType = regularize(dataType, true);
        column.charLength = regularize(charLength, true);
        column.ignoreDefVal = ignoreDefVal;

        return column;
    }

    private static String regularize(String columnName, boolean toUpperCase) {
        if (columnName == null) {
            return "<null>";
        }
        columnName = columnName.trim();
        if (toUpperCase) {
            columnName = columnName.toUpperCase();
        }

        return columnName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charLength == null) ? 0 : charLength.hashCode());
        result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        if (!ignoreDefVal) {
            result = prime * result + ((defaultVal == null) ? 0 : defaultVal.hashCode());
        }
        result = prime * result + ((isNullable == null) ? 0 : isNullable.hashCode());
        result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Column other = (Column) obj;
        if (charLength == null) {
            if (other.charLength != null)
                return false;
        } else if (!charLength.equals(other.charLength))
            return false;
        if (columnName == null) {
            if (other.columnName != null)
                return false;
        } else if (!columnName.equals(other.columnName))
            return false;
        if (dataType == null) {
            if (other.dataType != null)
                return false;
        } else if (!dataType.equals(other.dataType))
            return false;
        if (!ignoreDefVal && !other.ignoreDefVal) {
            if (defaultVal == null) {
                if (other.defaultVal != null)
                    return false;
            } else if (!defaultVal.equals(other.defaultVal))
                return false;
        }
        if (isNullable == null) {
            if (other.isNullable != null)
                return false;
        } else if (!isNullable.equals(other.isNullable))
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String ldefaultVal = ignoreDefVal ? "<ignored>" : defaultVal;
        return "Column [tableName=" + tableName + ", columnName=" + columnName + ", defaultVal=" + ldefaultVal + ", isNullable="
               + isNullable + ", dataType=" + dataType + ", charLength=" + charLength + "]";
    }

    @Override
    public int compareTo(Column o) {
        return (tableName + "." + columnName).compareTo(o.tableName + "." + o.columnName);
    }
}
