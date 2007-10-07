/**
 * WMSDimension.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

import java.util.List;
import org.jdom.Element;


/**
 * @author ETj
 */
public interface WMSDimension
{
	public void setValue(String value);
	public String getValue();

	public void setName(String name);
	public String getName();

	public void setUnits(String units);
	public String getUnits();

	public void setUnitSymbol(String unitSymbol);
	public String getUnitSymbol();

	public void setDefault(String def);
	public String getDefault();

	public void setMultipleValues(boolean multipleValues);
	public boolean isMultipleValues();

	public void setNearestValue(boolean nearestValue);
	public boolean isNearestValue();

	public void setCurrent(boolean current);
	public boolean isCurrent();

	/**
	 * Import in current 1.3.0 Dimension the attributes that in 1.1.x were in Extent elements
	 */
	public void setExtent(List<Element> extentList);

}

