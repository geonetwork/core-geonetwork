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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;

import org.jdom.Element;

//=============================================================================

/** DB Utilities
  */

public final class DBUtils
{
	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";
	
	/**
    * Default constructor.
    * Builds a DBUtils.
    */
   private DBUtils() {}
	
   public static Vector<Object> scanInFields(Element params, Vector<Element> inFields,
         Element result, Dbms dbms) throws Exception {
		// build argument list
		Vector<Object> vArgs = new Vector<Object>();

		for (Element field : inFields)
		{
			// extract field attributes and value
			String name      = field.getAttributeValue(Jeeves.Attr.NAME);
			String mandatory = field.getAttributeValue(Jeeves.Attr.MANDATORY);
			String nullText  = field.getAttributeValue(Jeeves.Attr.NULL);
			String format    = field.getAttributeValue(Jeeves.Attr.FORMAT);
			String sqlValue  = field.getAttributeValue(Jeeves.Attr.VALUE);
			String forward   = field.getAttributeValue(Jeeves.Attr.FORWARD);
			String value     = params.getChildText(name);
			
			// check if field name is defined
			if (name == null)
				throw new IllegalArgumentException("undefined field name: " + Xml.getString(field));
			
			// get value from sqlValue if needed
			if (sqlValue != null)
			{
				Element el = dbms.select(sqlValue);
				value = el.getChild(Jeeves.Elem.RECORD).getChildText(name);
			}
			// check if mandatory fields are present
			boolean mFlag;
			if (mandatory == null || mandatory.equals(Jeeves.Text.FALSE)) mFlag = false;
			else if (mandatory.equals(Jeeves.Text.TRUE))                  mFlag = true;
			else throw new IllegalArgumentException("bad mandatory attribute value: " + mandatory);
			if (mFlag && value == null)
				throw new IllegalArgumentException("mandatory parameter missing: " + name);
			
			// add field to select arguments
			if (value == null || (nullText != null && nullText.equals(value)))
				vArgs.add(null);
			else
			{
				// forward field to output if needed
				boolean fFlag;
				if (forward == null || forward.equals(Jeeves.Text.FALSE)) fFlag = false;
				else if (forward.equals(Jeeves.Text.TRUE))                fFlag = true;
				else throw new IllegalArgumentException("bad forward attribute value: " + forward);
				if (fFlag && value != null && result != null)
					result.addContent(new Element(name).setText(value));
				
				String type = field.getAttributeValue(Jeeves.Attr.TYPE);
	
				if (type == null || type.equals(Jeeves.Attr.Type.STRING))
					vArgs.add(value);
	
				else if (type.equals(Jeeves.Attr.Type.INT))
				{
					if (format == null)
						vArgs.add(new Integer(value));
					else
					{
						DecimalFormat df = new DecimalFormat(format);
						Number n = df.parse(value);
						vArgs.add(new Integer(n.intValue()));
					}
				}
				else if (type.equals(Jeeves.Attr.Type.DOUBLE))
				{
					if (format == null)
						vArgs.add(new Double(value));
					else
					{
						DecimalFormat df = new DecimalFormat(format);
						Number n = df.parse(value);
						vArgs.add(new Double(n.doubleValue()));
					}
				}
				else if (type.equals(Jeeves.Attr.Type.DATE))
				{
					SimpleDateFormat df = (format == null) ? new SimpleDateFormat(DEFAULT_DATE_FORMAT) : new SimpleDateFormat(format);
					Date date = df.parse(value);
					vArgs.add(new java.sql.Date(date.getTime())); // directly passing date does not work, at least with MySql
				}
				else throw new IllegalArgumentException("bad field type: " + type);
			}
		}
		return vArgs;
	}

   public static Hashtable<String, String> scanOutFields(Vector<Element> outFields)
         throws Exception {
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
}

//=============================================================================

