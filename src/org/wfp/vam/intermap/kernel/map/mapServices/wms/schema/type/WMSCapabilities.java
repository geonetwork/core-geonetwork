/**
 * WMSCapabilitiesDocument.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSCapabilities
{
	public enum WMSVer
	{
		V100("1.0.0"),
		V110("1.1.0"),
		V111("1.1.1"),
		V130("1.3.0");

		private String _verString;

		private WMSVer(String v)
		{
			_verString = v;
			System.out.println("CREATING VERSION " + this.name() + " with string '"+v);//+"' on map " + _map );
		}

		public String toString()
		{
			return _verString;
		}

		public static WMSVer parse(String ver)
		{
			for(WMSVer v: WMSVer.values())
				if(v.toString().equals(ver))
					return v;
			System.out.println("WARNING: WMSVer: version '"+ver+"' is not defined internally. Please update the internal version list.");
			return null;
		}
	}

	public static final WMSVer DEFVERSION = WMSVer.V130;
	/**
	 * Method setService
	 */
	public void setService(WMSService oservice);
	public WMSService getService();

	/**
	 * Sets Capability
	 */
	public void setCapability(WMSCapability capability);
	public WMSCapability getCapability();

	/**
	 * Sets Version
	 */
	public void setVersion(String version);
	public void setVersion(WMSVer version);
	public WMSVer getVersion();

	public void setUpdateSequence(String updateSequence);
	public String getUpdateSequence();

}

