/**
 * Layer.java
 *
 * @author Created by ETj
 */

package org.wfp.vam.intermap.kernel.map;

import org.wfp.vam.intermap.kernel.map.mapServices.MapService;

public class Layer
{
	private final MapService _service;
	private float _transparency = 1.0F;
	private boolean _expanded = true; 	// Used for ARCims services only
	private boolean _visible = true;

	public Layer(MapService service)
	{
		_service = service;
	}

//		public void setService(MapService service){_service = service;}
	public MapService getService(){return _service;}

	public void setTransparency(float transparency){_transparency = transparency;}
	public float getTransparency(){return _transparency;}
	public int getIntTransparency(){return  (int)(_transparency * 100);}

	// Used for ARCims services only
	public void   setExpanded(boolean isExpanded){_expanded = isExpanded;}
	public boolean isExpanded(){return _expanded;}

	public void setVisible(boolean isVisibile){_visible = isVisibile;}
	public boolean isVisible(){return _visible;}
}

