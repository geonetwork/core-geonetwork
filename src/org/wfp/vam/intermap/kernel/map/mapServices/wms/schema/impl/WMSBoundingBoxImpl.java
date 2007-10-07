/**
 * BoundingBoxType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;


import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSBoundingBox;


/**
 * @author ETj
 */
public class WMSBoundingBoxImpl implements WMSBoundingBox
{
	private String _srs = null;
	private float _minx = Float.NaN;
	private float _miny = Float.NaN;
	private float _maxx = Float.NaN;
	private float _maxy = Float.NaN;
	private float _resx = Float.NaN; // opt
	private float _resy = Float.NaN; // opt

	private WMSBoundingBoxImpl()
	{
	}

	public static WMSBoundingBox newInstance()
	{
		return new WMSBoundingBoxImpl();
	}

	public static WMSBoundingBox parse(Element ebb)
	{
		WMSBoundingBoxImpl bb = new WMSBoundingBoxImpl();

		bb.setSRS(ebb.getAttributeValue("srs"));
		bb.setMinx(Float.parseFloat(ebb.getAttributeValue("minx")));
		bb.setMiny(Float.parseFloat(ebb.getAttributeValue("miny")));
		bb.setMaxx(Float.parseFloat(ebb.getAttributeValue("maxx")));
		bb.setMaxy(Float.parseFloat(ebb.getAttributeValue("maxy")));

		String sresx = ebb.getAttributeValue("resx");
		if(sresx != null)
			bb.setResx(Float.parseFloat(sresx));

		String sresy = ebb.getAttributeValue("resy");
		if(sresy != null)
			bb.setResy(Float.parseFloat(sresy));

		return bb;
	}

	/**
	 * Sets Srs
	 */
	public void setSRS(String srs)
	{
		_srs = srs;
	}

	/**
	 * Returns Srs
	 */
	public String getSRS()
	{
		return _srs;
	}

	/**
	 * Sets Minx
	 */
	public void setMinx(float minx)
	{
		_minx = minx;
	}

	/**
	 * Returns Minx
	 */
	public float getMinx()
	{
		return _minx;
	}

	/**
	 * Sets Miny
	 */
	public void setMiny(float miny)
	{
		_miny = miny;
	}

	/**
	 * Returns Miny
	 */
	public float getMiny()
	{
		return _miny;
	}

	/**
	 * Sets Maxx
	 */
	public void setMaxx(float maxx)
	{
		_maxx = maxx;
	}

	/**
	 * Returns Maxx
	 */
	public float getMaxx()
	{
		return _maxx;
	}

	/**
	 * Sets Maxy
	 */
	public void setMaxy(float maxy)
	{
		_maxy = maxy;
	}

	/**
	 * Returns Maxy
	 */
	public float getMaxy()
	{
		return _maxy;
	}

	/**
	 * Sets Resx
	 */
	public void setResx(float resx)
	{
		_resx = resx;
	}

	/**
	 * Returns Resx
	 */
	public float getResx()
	{
		return _resx;
	}

	/**
	 * Sets Resy
	 */
	public void setResy(float resy)
	{
		_resy = resy;
	}

	/**
	 * Returns Resy
	 */
	public float getResy()
	{
		return _resy;
	}


	/**
	 * Method toElement
	 */
	public Element toElement(String name)
	{
		if(_srs == null)
			throw new IllegalStateException(name + "/SRS is missing");
		if(_minx == Float.NaN)
			throw new IllegalStateException(name + "/minx is missing");
		if(_miny == Float.NaN)
			throw new IllegalStateException(name + "/miny is missing");
		if(_maxx == Float.NaN)
			throw new IllegalStateException(name + "/maxx is missing");
		if(_maxy == Float.NaN)
			throw new IllegalStateException(name + "/maxy is missing");

		Element ret = new Element(name)
			.setAttribute("SRS", _srs)
			.setAttribute("minx", ""+_minx)
			.setAttribute("miny", ""+_miny)
			.setAttribute("maxx", ""+_maxx)
			.setAttribute("maxy", ""+_maxy);

		if(_resx != Float.NaN)
			ret.setAttribute("resx", ""+_resx);
		if(_resy != Float.NaN)
			ret.setAttribute("resy", ""+_resy);

		return ret;
	}

}

