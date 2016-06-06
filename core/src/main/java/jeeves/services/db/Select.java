//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.services.db;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import javax.sql.DataSource;

import java.io.StringReader;
import java.nio.file.Path;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

//=============================================================================

/**
 * Performs a generic query
 */

public class Select implements Service {
    private static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------
    private String query;

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------
    private Vector<Element> inFields;
    private Vector<Element> outFields;

    public static Vector<Object> scanInFields(Element params, Vector<Element> inFields, Element result, ServiceContext context) throws Exception {
        // build argument list
        Vector<Object> vArgs = new Vector<Object>();

        for (Element field : inFields) {
            // extract field attributes and value
            String name = field.getAttributeValue(Jeeves.Attr.NAME);
            String mandatory = field.getAttributeValue(Jeeves.Attr.MANDATORY);
            String nullText = field.getAttributeValue(Jeeves.Attr.NULL);
            String format = field.getAttributeValue(Jeeves.Attr.FORMAT);
            String sqlValue = field.getAttributeValue(Jeeves.Attr.VALUE);
            String forward = field.getAttributeValue(Jeeves.Attr.FORWARD);
            String value = params.getChildText(name);

            // check if field name is defined
            if (name == null)
                throw new IllegalArgumentException("undefined field name: " + Xml.getString(field));

            // get value from sqlValue if needed
            if (sqlValue != null) {
                Element el = select(context, sqlValue);
                value = el.getChild(Jeeves.Elem.RECORD).getChildText(name);
            }
            // check if mandatory fields are present
            boolean mFlag;
            if (mandatory == null || mandatory.equals(Jeeves.Text.FALSE))
                mFlag = false;
            else if (mandatory.equals(Jeeves.Text.TRUE))
                mFlag = true;
            else
                throw new IllegalArgumentException("bad mandatory attribute value: " + mandatory);
            if (mFlag && value == null)
                throw new IllegalArgumentException("mandatory parameter missing: " + name);

            // add field to select arguments
            if (value == null || (nullText != null && nullText.equals(value)))
                vArgs.add(null);
            else {
                // forward field to output if needed
                boolean fFlag;
                if (forward == null || forward.equals(Jeeves.Text.FALSE))
                    fFlag = false;
                else if (forward.equals(Jeeves.Text.TRUE))
                    fFlag = true;
                else
                    throw new IllegalArgumentException("bad forward attribute value: " + forward);
                if (fFlag && result != null)
                    result.addContent(new Element(name).setText(value));

                String type = field.getAttributeValue(Jeeves.Attr.TYPE);

                if (type == null || type.equals(Jeeves.Attr.Type.STRING))
                    vArgs.add(value);

                else if (type.equals(Jeeves.Attr.Type.INT)) {
                    if (format == null)
                        vArgs.add(Integer.valueOf(value));
                    else {
                        DecimalFormat df = new DecimalFormat(format);
                        Number n = df.parse(value);
                        vArgs.add(Integer.valueOf(n.intValue()));
                    }
                } else if (type.equals(Jeeves.Attr.Type.DOUBLE)) {
                    if (format == null)
                        vArgs.add(new Double(value));
                    else {
                        DecimalFormat df = new DecimalFormat(format);
                        Number n = df.parse(value);
                        vArgs.add(new Double(n.doubleValue()));
                    }
                } else if (type.equals(Jeeves.Attr.Type.DATE)) {
                    SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_DATE_FORMAT) : new SimpleDateFormat(format);
                    Date date = df.parse(value);
                    vArgs.add(new java.sql.Date(date.getTime())); // directly passing date does not work, at least with MySql
                } else
                    throw new IllegalArgumentException("bad field type: " + type);
            }
        }
        return vArgs;
    }

    /* ************************** Methods for simplifying JPA migration    ****************************************** */

    public static Hashtable<String, String> scanOutFields(Vector<Element> outFields) throws Exception {
        Hashtable<String, String> formats = new Hashtable<String, String>();

        // scan fields info
        for (Element field : outFields) {
            // build Field
            String name = field.getAttributeValue(Jeeves.Attr.NAME);
            if (name == null)
                throw new IllegalArgumentException("undefined field name: " + Xml.getString(field));

            String format = field.getAttributeValue(Jeeves.Attr.FORMAT);
            if (format != null)
                formats.put(name, format);
        }
        return formats;
    }

    /**
     * This method is taken from the old Dbms class to allow reading as Element like a lot of xsl
     * require. This is to ease the migration to all JPA database access.
     */
    public static Element select(ServiceContext context, String query)
        throws SQLException {
        return selectFull(context, query, Collections.<String, String>emptyMap());
    }

    /**
     * This method is taken from the old Dbms class to allow reading as Element like a lot of xsl
     * require. This is to ease the migration to all JPA database access.
     */
    public static Element selectFull(ServiceContext context, String query, Map<String, String> formats, Object... args)
        throws SQLException {

        DataSource datasource = context.getBean(DataSource.class);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = datasource.getConnection();
            stmt = conn.prepareStatement(query);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    setObject(stmt, i, args[i]);
                }
            }
            resultSet = stmt.executeQuery();

            Element result = buildResponse(resultSet, formats);

            return result;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } finally {
                    if (conn != null) {
                        conn.close();
                    }
                }
            }
        }
    }

    private static void setObject(PreparedStatement stmt, int i, Object obj) throws SQLException {
        if (obj instanceof String) {
            String s = (String) obj;

            if (s.length() < 4000)
                stmt.setString(i + 1, s);
            else
                stmt.setCharacterStream(i + 1, new StringReader(s), s.length());
        } else
            stmt.setObject(i + 1, obj);
    }

    private static Element buildResponse(ResultSet rs, Map<String, String> formats) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();

        int colNum = md.getColumnCount();

        // --- retrieve name and type of fields

        Vector<String> vHeaders = new Vector<String>();
        Vector<Integer> vTypes = new Vector<Integer>();

        for (int i = 0; i < colNum; i++) {
            vHeaders.add(md.getColumnLabel(i + 1).toLowerCase());
            vTypes.add(Integer.valueOf(md.getColumnType(i + 1)));
        }

        // --- build the jdom tree

        Element root = new Element(Jeeves.Elem.RESPONSE);

        while (rs.next()) {
            Element record = new Element(Jeeves.Elem.RECORD);

            for (int i = 0; i < colNum; i++) {
                String name = vHeaders.get(i).toString();
                int type = ((Integer) vTypes.get(i)).intValue();
                record.addContent(buildElement(rs, i, name, type, formats));
            }
            root.addContent(record);
        }
        return root;
    }

    private static Element buildElement(ResultSet rs, int col, String name, int type, Map<String, String> formats) throws SQLException {
        String value = null;

        switch (type) {
            case Types.DATE:
                Date date = rs.getDate(col + 1);
                if (date == null) value = null;
                else {
                    String format = formats.get(name);
                    SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_DATE_FORMAT) : new SimpleDateFormat(format);
                    value = df.format(date);
                }
                break;

            case Types.TIME:
                Time time = rs.getTime(col + 1);
                if (time == null) value = null;
                else {
                    String format = formats.get(name);
                    SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_TIME_FORMAT) : new SimpleDateFormat(format);
                    value = df.format(time);
                }
                break;

            case Types.TIMESTAMP:
                Timestamp timestamp = rs.getTimestamp(col + 1);
                if (timestamp == null) value = null;
                else {
                    String format = formats.get(name);
                    SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT) : new SimpleDateFormat(format);
                    value = df.format(timestamp);
                }
                break;

            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                long l = rs.getLong(col + 1);
                if (rs.wasNull()) value = null;
                else {
                    String format = formats.get(name);
                    if (format == null) value = l + "";
                    else {
                        DecimalFormat df = new DecimalFormat(format);
                        value = df.format(l);
                    }
                }
                break;

            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL:
            case Types.NUMERIC:
                double n = rs.getDouble(col + 1);

                if (rs.wasNull())
                    value = null;
                else {
                    String format = formats.get(name);

                    if (format == null) {
                        value = n + "";

                        //--- this fix is mandatory for oracle
                        //--- that shit returns integers like xxx.0

                        if (value.endsWith(".0"))
                            value = value.substring(0, value.length() - 2);
                    } else {
                        DecimalFormat df = new DecimalFormat(format);
                        value = df.format(n);
                    }
                }
                break;

            default:
                value = rs.getString(col + 1);
                if (value != null) {
                    value = stripIllegalChars(value);
                }

                break;
        }
        return new Element(name).setText(value);
    }

    private static String stripIllegalChars(String input) {
        String output = input;
        for (int i = 127; i < 160; i++) {
            String c = String.valueOf((char) i);
            if (output.contains(c)) {
                output = output.replaceAll(c, "");
            }
        }

        return output;
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
        query = params.getMandatoryValue(Jeeves.Config.QUERY);
        List<Element> inList = params.getChildren(Jeeves.Config.IN_FIELDS, Jeeves.Config.FIELD);

        inFields = new Vector<Element>();
        if (inList != null) {
            for (Element field : inList) {
                inFields.add(field);
            }
        }

        List<Element> outList = params.getChildren(Jeeves.Config.OUT_FIELDS, Jeeves.Config.FIELD);
        outFields = new Vector<Element>();
        if (outList != null) {
            for (Element field : outList) {
                outFields.add(field);
            }
        }
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        Vector<Object> vArgs = scanInFields(params, inFields, null, context);
        Hashtable<String, String> formats = scanOutFields(outFields);
        return selectFull(context, query, formats, vArgs.toArray());
    }
}

//=============================================================================

