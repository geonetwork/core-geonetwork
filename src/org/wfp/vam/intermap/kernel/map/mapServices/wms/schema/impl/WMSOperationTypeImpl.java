/**
 * WMSOperationTypeImpl.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import java.util.*;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSDCPType;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSFormat;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOperationType;

/**
 * @author ETj
 */
public class WMSOperationTypeImpl implements WMSOperationType
{
	private Set<WMSFormat> _formatList = EnumSet.noneOf(WMSFormat.class); //new HashSet<WMSFormat>(); // 1..n
	private List<WMSDCPType> _dcptypeList = new ArrayList<WMSDCPType>(); // 1..n

	private WMSOperationTypeImpl()
	{}

	public static WMSOperationType newInstance()
	{
		return new WMSOperationTypeImpl();
	}

	public static WMSOperationType parse(Element eRequest)
	{
		WMSOperationTypeImpl r = new WMSOperationTypeImpl();

		for(Element eFormat: (List<Element>)eRequest.getChildren("Format"))
			r.addFormat(WMSFormat.parse(eFormat));

		for(Element eDCP: (List<Element>)eRequest.getChildren("DCPType"))
			r.addDCPType(WMSFactory.parseDCPType(eDCP));

		return r;
	}

	////////////////////////////////////////////////////////////////////////////
	// Format

	public void addFormat(List<WMSFormat> format)
	{
		if(format.get(0) == null)
		{
			System.out.println("* skipping null WMSFormat");
			return;
		}
		_formatList.addAll(format);
	}

	public void addFormat(WMSFormat format)
	{
		_formatList.add(format);
	}

	public Set<WMSFormat> getFormats()
	{
		return Collections.unmodifiableSet(_formatList);
	}

	////////////////////////////////////////////////////////////////////////////
	// DCPType

	public void addDCPType(WMSDCPType dcpType)
	{
		_dcptypeList.add(dcpType);
	}

	public WMSDCPType getDCPType(int index)
	{
		return _dcptypeList.get(index);
	}

	public Iterable<WMSDCPType> getDCPTypeIterator()
	{
		return new Iterable<WMSDCPType>()
		{
			public Iterator<WMSDCPType> iterator()
			{
				return _dcptypeList.iterator();
			}
		};
	}
}

