/**
 * A class representing a map, as a group of services. It provides methods
 * to add / remove services, and to set and change the bounding box.
 *
 * @author Stefano Giaccio, Emanuele Tajariol

 */

package org.wfp.vam.intermap.kernel.map;

import java.util.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import org.jdom.Element;
import org.wfp.vam.intermap.http.ConcurrentHTTPTransactionHandler;
import org.wfp.vam.intermap.http.cache.HttpGetFileCache;
import org.wfp.vam.intermap.kernel.GlobalTempFiles;
import org.wfp.vam.intermap.kernel.TempFiles;
import org.wfp.vam.intermap.kernel.map.images.ImageMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.HttpClient;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.ServiceException;


public class MapMerger
{
	private static HttpGetFileCache cache;
	private static int dpi = 96;

	private BoundingBox bBox = new BoundingBox();  // Image extent

	private Map<Integer, Layer> _layers = new HashMap<Integer, Layer>();

	private List<Integer> vRank = new ArrayList<Integer>(); // Service rank
	private int nextId = 1;

	private int activeServiceId;
	private String imageName = null; // The name of the merge image file
	private String imagePath = null; // The path of the merge image file
	private float degScale; // The map scale
	private float distScale; // The map scale
	private Hashtable htErrors = new Hashtable();
	private boolean reaspectWms = true;

	public static void setCache(HttpGetFileCache cache) {
		MapMerger.cache = cache;
	}
	public static void setDpi(int dpi) {
		MapMerger.dpi = dpi;
	}

	public void reaspectWms(boolean reaspect) { this.reaspectWms = reaspect; };

	/** Sets the Map BoundingBox */
	public void setBoundingBox(BoundingBox bb) { bBox = bb; }

	/** Returns the Map BoudingBox */
	public BoundingBox getBoundingBox() { return bBox; }

	/** Sets the active layer inside a given servce */
	public void setActiveLayer(int service, int layer)
		throws Exception
	{
//		if (! htServices.containsKey(new Integer(service)))
		if (! _layers.containsKey(service))
			throw new Exception("");
		activeServiceId = service;
		getService(service).setActiveLayer(layer);
	}

	/** Returns the active service id */
	public int getActiveServiceId() { return activeServiceId; }

	/** Returns the active Layer id */
	public int getActiveLayerId() {
//		System.out.println("this: " +  this);
//		System.out.println("getActiveServiceId: " + getActiveServiceId());
//		System.out.println("getActiveLayer: " + getService(getActiveServiceId()).getActiveLayer());

		return getService(getActiveServiceId()).getActiveLayer();
	}

	/** Adds a service on the top
	 *
	 * @return the id given to the service
	 * */
	public int addService(MapService service) {
//		System.out.println("service: " + service); // DEBUG
//		if (htServices.size() == 0) activeServiceId = nextId;
		if ( _layers.isEmpty() ) activeServiceId = nextId;

		Integer id = new Integer(nextId++);

		Layer layer = new Layer(service);
		_layers.put(id, layer);

//		htServices.put(id, service);
//		htTransparency.put(id, new Float(1.0));
//		htExpanded.put(id, new Boolean(true));
//		htShow.put(id, new Boolean(true));
		vRank.add(0, id);

		return id.intValue();
//		System.out.println("htServices: " + htServices); // DEBUG
	}

	/** Removes the service with the given id */
	public void delService(int id)
		throws Exception
	{
		_layers.remove(id);
		vRank.remove(new Integer(id)); // be careful: we're removing the id object (Integer); an int would be used as index

//		Integer t = new Integer(id);
//		htServices.remove(t);
//		htTransparency.remove(t);
//		htExpanded.remove(t);

		if (activeServiceId == id)
			if (vRank.size() > 0)
			{
				activeServiceId = vRank.get(0).intValue();
				getService(activeServiceId).setActiveLayer(1);
			}
			else
			{
				activeServiceId = -1;
			}
	}

	/** Moves a service up */
	public void moveServiceUp(int id) {
		Integer t = new Integer(id);
		int pos = vRank.indexOf(t);
		if (pos > 0) {
			vRank.remove(t);
			vRank.add(pos - 1, t);
		}
	}

	/** Moves a service down */
	public void moveServiceDown(int id) {
		Integer t = new Integer(id);
		int pos = vRank.indexOf(t);
		if (pos < vRank.size() - 1) {
			vRank.remove(t);
			vRank.add(pos + 1, t);
		}
	}

	public void setServicesOrder(int[] order) {
		vRank.clear();
		for (int i = 0 ; i < order.length; i++)
			vRank.add(new Integer(order[i]));
	}

	public Element toElementSimple() {
		Element elServices = new Element("services");

		// Add each contained service to the elServices, ordered by vRank
//		for (int i = 0; i < vRank.size(); i++) {
		for (int rank:  vRank) {
//			MapService s = (MapService)htServices.get(vRank.get(i));
			Layer layer = _layers.get(rank);
			MapService s = layer.getService();

			elServices.addContent(new Element("layer")
//									  .setAttribute("id", vRank.get(i) + "")
									  .setAttribute("id", rank + "")
								 	  .setAttribute("title", "" + s.getTitle())
								 	  .setAttribute("type", "" + s.getType())
								 	  .setAttribute("transparency", "" + layer.getIntTransparency())
								 );
		}

		return elServices;
	}

	/** Converts this object to a JDOM Element */
	public Element toElement() {
		Element elServices = new Element("services");

		// Add each contained service to the elServices, ordered by vRank
//		for (int i = 0; i < vRank.size(); i++) {
//			MapService s = (MapService)htServices.get(vRank.get(i));
//
//			elServices.addContent(s.toElement()
//									  .setAttribute("id", vRank.get(i) + ""));
//		}
		for (int rank:  vRank)
		{
			MapService s = _layers.get(rank).getService();
			elServices.addContent(s.toElement()
									  .setAttribute("id", rank + ""));
		}


		DecimalFormat df = new DecimalFormat("0.0000000000");
		elServices.addContent( new Element("activeLayer")
										 .setAttribute("service", "" + activeServiceId)
								  		 .setAttribute("layer", "" + getActiveLayerId()) )
			.addContent(new Element("degScale").setText(df.format(degScale)))
			.addContent(new Element("distScale").setText(df.format(distScale) + ""))
			.addContent(bBox.toElement())
			.addContent(getStructTransparencies())
			.addContent(getStructExpanded())
			.addContent(getErrors());

		return elServices;
	}

//	public Iterable<Layer> getLayers()
//	{
//		return new Iterable<Layer>()
//		{
//			public Iterator<Layer> iterator()
//			{
//				return new Iterator<Layer>()
//				{
//					private int i = 0;
//					private int last = vRank.size();
//
//					public boolean hasNext()	{ return i<last; }
//					public Layer 	next()		{ return _layers.get(vRank.get(i++)); }
//					public void 	remove()		{ throw new UnsupportedOperationException();}
//				};
//			}
//		};
//	}


	/** Returns an Element containing the transparency value for each service */
	public Element getStructTransparencies() {
		Element elTransparency = new Element("transparency");
//		for (Enumeration e = htServices.keys(); e.hasMoreElements(); ) {
//			Integer serviceId = (Integer)e.nextElement();
//			int f = (int)(((Float)htTransparency.get(serviceId)).floatValue() * 100);
//			elTransparency.addContent( new Element("service").setAttribute("id", serviceId.toString())
//										  .setAttribute("transparency", f + "")
//										  .setAttribute("name", ((MapService)htServices.get(serviceId)).getName()) );
//		}

		for(Integer id: _layers.keySet())
		{
			Layer layer = _layers.get(id);

			elTransparency.addContent(
				new Element("service")
					.setAttribute("id", 				id.toString())
				  	.setAttribute("transparency", ""+layer.getIntTransparency())
					.setAttribute("name", 			layer.getService().getName() ));
		}
		return elTransparency;
	}

	/** Returns the transparency of a given layer */
//	public float getLayerTransparency(int id) {
//		Integer serviceId = new Integer(id);
//		return  (int)(((Float)htTransparency.get(serviceId)).floatValue() * 100);
	public int getLayerTransparency(int id) {
		return _layers.get(id).getIntTransparency();
	}
	public int getLayerTransparencyRanked(int id) {
		return _layers.get(vRank.get(id)).getIntTransparency();
	}

	/** Sets the transparency value for a given service */
	public void setTransparency(int id, float transparency) throws Exception {
//		if (htServices.get(new Integer(id)) == null)
//			throw new Exception("Illegal service id");
//
//		htTransparency.put(new Integer(id), new Float(transparency));
		if ( ! _layers.containsKey(id))
			throw new Exception("Illegal service id");

		_layers.get(id).setTransparency(transparency);
	}

	/** Expands a service in the layer frame (used for ArcIMS services only) */
	public void expandService(int id) throws Exception {
//		htExpanded.put(new Integer(id), new Boolean(true));
		if ( ! _layers.containsKey(id))
			throw new Exception("Illegal service id");

		_layers.get(id).setExpanded(true);
	}

	/** Collapses a service in the layer frame (used for ArcIMS services only) */
	public void collapseService(int id) throws Exception {
		if ( ! _layers.containsKey(id))
			throw new Exception("Illegal service id");

		_layers.get(id).setExpanded(false);
	}


	/** ??? Expands a service in the layer frame (used for ArcIMS services only) */
	public void showService(int id) throws Exception {
		if ( ! _layers.containsKey(id))
			throw new Exception("Illegal service id");

		_layers.get(id).setVisible(true);
//		htShow.put(new Integer(id), new Boolean(true));
	}

	/** ??? Collapses a service in the layer frame (used for ArcIMS services only) */
	public void hideService(int id) throws Exception {
		if ( ! _layers.containsKey(id))
			throw new Exception("Illegal service id");

		_layers.get(id).setVisible(false);
//		htShow.put(new Integer(id), new Boolean(false));
	}

	public boolean toggleVisibility(int id) throws Exception {
		if ( ! _layers.containsKey(id))
			throw new Exception("Illegal service id");

		boolean newVis = ! _layers.get(id).isVisible();
		_layers.get(id).setVisible(newVis);
		return newVis;

//		Boolean bVisibility = (Boolean)htShow.get(new Integer(id));
//
//		boolean newVisibility = !bVisibility.booleanValue();
//		htShow.put(new Integer(id), new Boolean(newVisibility));
//		return newVisibility;
	}

	/** Get error messages from the remote servers */
	public Element getErrors() {
		Element errors = new Element("errors");
		for (Enumeration e = htErrors.keys(); e.hasMoreElements(); ) {
			Integer id = (Integer)e.nextElement();
			errors.addContent( new Element("layer")
				.setAttribute("id", id.toString())
				.setAttribute("message", (String)htErrors.get(id)) );
		}

		return errors;
	}

	/** Returns number of map services contained */
	public int size() { return _layers.size(); }

	/** Returns an Enumeration containing all the services */
	public Iterable<MapService> getServices()
	{
		return new Iterable<MapService>()
		{
			public Iterator<MapService> iterator()
			{
				return new Iterator<MapService>()
				{
					private Iterator<Layer> _layerIterator = _layers.values().iterator();

					public boolean hasNext()	{ return _layerIterator.hasNext(); }
					public MapService next()	{ return _layerIterator.next().getService(); }
					public void remove()			{ throw new UnsupportedOperationException();}
				};

			}
		};
//		return htServices.elements();
	}

	/** Returns the MapService element with the given id*/
	public MapService getService(int id) { return _layers.get(id).getService(); }
	public MapService getServiceRanked(int id) { return _layers.get(vRank.get(id)).getService(); }
//	public MapService getService(int id) { return (MapService)htServices.get(new Integer(id)); }
//	public boolean isVisible(int id) { return _layers.get(id).isVisible(); }
	public boolean isVisibleRanked(int id) { return _layers.get(vRank.get(id)).isVisible(); }

	/** Returns an element containing informations about the expanded or collapsed
	 *  services
	 */
	private Element getStructExpanded()
	{
//		Element expanded = new Element("expandedServices");
//		for (Enumeration e = htExpanded.keys(); e.hasMoreElements(); ) {
//			Integer serviceId = (Integer)e.nextElement();
//			boolean ex = ((Boolean)htExpanded.get(serviceId)).booleanValue();
//			expanded.addContent(new Element("service")
//									.setAttribute("id", serviceId.toString())
//									.setAttribute("expanded", ex ? "true" : "false"));
//		}
		Element expanded = new Element("expandedServices");

		for(Integer id: _layers.keySet())
		{
			Layer layer = _layers.get(id);

			expanded.addContent(new Element("service")
									.setAttribute("id", 			id.toString())
									.setAttribute("expanded", 	layer.isExpanded()? "true" : "false"));
		}

		return expanded;
	}

	private void buildWmsRequests(int width, int height)
	{
//		if (reaspectWms) bBox = reaspect(bBox, width, height); // reaspect the bounding box if no ArcIMS service did it
//
////		MapService prevService = (MapService)htServices.get(vRank.get(0));
//		MapService prevService = _layers.get(vRank.get(0)).getService();
//		if (vRank.size() == 1)
//		{
//			try { vImageUrls.add(prevService.getImageUrl(bBox, width, height)); }
//			catch (Exception e) { e.printStackTrace(); /* DEBUG */ }
//			return;
//		}
//
//		MapService service;
//		Vector imageNames = new Vector();
//		String serverURL = null;
//
//		int i = 0;
//		boolean flag = false;
//		while (!flag)
//		{
////			prevService = (MapService)htServices.get(vRank.get(i));
//			Layer layer = _layers.get(vRank.get(i));
//			prevService = layer.getService();
////			if (  htShow.get(vRank.get(i)).equals(new Boolean(true)) ) {
//			if (  layer.isVisible() ) {
//				imageNames.add(prevService.getName());
//				flag = true;
//			}
//			i++;
//		}
//
//		while (i < vRank.size())
//		{
//			System.out.println(i + " - imageNames = " + imageNames);
//
//
//			// get the map service
////			service = (MapService)htServices.get(vRank.get(i));
//			Layer layer = _layers.get(vRank.get(i));
//			service = layer.getService();
//
//			//System.out.println("i = " + i + " - vRank.size() = " + vRank.size());
//			serverURL = service.getServerURL();
//
////			if (htShow.get(vRank.get(i)).equals(new Boolean(true)))
//			if ( layer.isVisible() )
//			{
//				if (serverURL.equals(prevService.getServerURL()))
//				{
//					//System.out.println(ms.getName());
//					vImageUrls.add(null);
//				}
//				else
//				{
//					vImageUrls.add(prevService.getGroupImageUrl(bBox, width, height, imageNames));
//					imageNames.clear();
//				}
//			}
//			else
//				vImageUrls.add(null);
//
//			imageNames.add(service.getName());
//
//
//			if ((i == vRank.size() - 1) && (prevService.getServerURL().equals(serverURL)))
//				vImageUrls.add(service.getGroupImageUrl(bBox, width, height, imageNames));
//
//			prevService = service;
//
//			i++;
//		}
//
//		System.out.println("- imageNames = " + imageNames);
//		System.out.println("- vImageUrls = " + vImageUrls);
//
//		i++;
	}

	private List<String> sendGetImageRequests(int width, int height)
	{
		List<String> vImageUrls = new ArrayList<String>();

//		vImageUrls.clear();
		//buildWmsRequests(width, height); // TODO: fix that function and uncomment these two lines
		//if (false)

		{
			// Retrieve WMS services urls
			if (reaspectWms)
				bBox = reaspect(bBox, width, height); // Reaspect the bounding box if no ArcIMS service did it

//			for (int i = 0; i < vRank.size(); i++)
//			{
//				MapService ms = (MapService)htServices.get(vRank.get(i));
//				if (htShow.get(vRank.get(i)).equals(new Boolean(true)))
//				{
//					try { vImageUrls.add(ms.getImageUrl(bBox, width, height)); }
//					catch (Exception e) { e.printStackTrace(); /* DEBUG */ }
//				}
//				else vImageUrls.add(null);
//			}
			for (int rank: vRank)
			{
				Layer layer = _layers.get(rank);
				MapService ms = layer.getService();
				if (layer.isVisible())
				{
					try { vImageUrls.add(ms.getImageUrl(bBox, width, height)); }
					catch (Exception e) { e.printStackTrace(); /* DEBUG */ }
				}
				else vImageUrls.add(null);
			}
		}

		// Set the scale
		degScale = Math.abs(getBoundingBox().getEast() - getBoundingBox().getWest()) / width;
		distScale = (long)(423307109.727 * degScale / 96.0 * dpi);

		return vImageUrls;
	}

	/**
	 * <UL>
	 *    <LI>Fetch images from remote servers</LI>
	 *    <LI>Merge them together</LI>
	 *    <LI>Create a temp file containing the merged image</LI>
	 * </UL>
	 *
	 * @return the image file name, or an empty string when
	 */
	public String merge(int width, int height)
		throws Exception
	{
		// Clear the error list
		htErrors.clear();

		List<String> vImageUrls = sendGetImageRequests(width, height);

//		/*DEBUG*/System.err.println("MERGE");
//		/*DEBUG*/System.err.println("  requrls:");
//		/*DEBUG*/for(String url : vImageUrls)
//		/*DEBUG*/	System.err.println("      " + url);

		List<String> files = new Vector<String>();

		ConcurrentHTTPTransactionHandler c = new ConcurrentHTTPTransactionHandler();// DEBUG
		c.setCache(cache); // DEBUG
		c.checkIfModified(false); //DEBUG

//		for (int i = 0; i < vImageUrls.size(); i++) {
//			if ((String)vImageUrls.get(i) != null) { // null if some error encountered (never with WMS servers)
//				c.register((String)vImageUrls.get(i));
//			}
		for (String url: vImageUrls)
		{
			if (url != null) { // null if some error encountered (never with WMS servers)
				c.register(url);
			}
			else // Only for ArcIMS services
			{// Tell the user
			}
		}
		c.doTransactions();

//		System.out.println("vImageUrls.size() = " + vImageUrls.size()); // DEBUG
		List<Float> vTransparency = new ArrayList<Float>(); // Image transparency

		for (int i = 0; i < vImageUrls.size(); i++)
		{
			if (vImageUrls.get(i) != null)
			{
				String path = c.getResponseFilePath(vImageUrls.get(i));
	//			System.out.println("vImageUrls.get(i) = " + (String)vImageUrls.get(i));
	//			System.out.println("path = " + path);
				if (path != null)
				{
					String contentType = c.getHeaderValue(vImageUrls.get(i), "content-type");
	//				System.out.println("contentType = " + contentType); // DEBUG
					if (contentType.startsWith("image"))
					{
						files.add(path);
//						vTransparency.add(htTransparency.get(vRank.get(i)));
						vTransparency.add(_layers.get(vRank.get(i)).getTransparency());
						//System.out.println("file: " + path + "; transparency: " + htTransparency.get(vRank.get(i))); // DEBUG
					}
				}
			}
		}

//		System.out.println("files.size() = " + files.size()); // DEBUG

//		imageName="";

		if (files.size() > 1) {
			// Merge the images
			File output = GlobalTempFiles.getInstance().getFile();
			String path = output.getPath();
//			System.out.println("vTransparency" + vTransparency); // DEBUG
			Collections.reverse(files);
			Collections.reverse(vTransparency);
			ImageMerger.mergeAndSave(files, vTransparency, path, ImageMerger.GIF);
			imageName = output.getName();
			imagePath = output.getPath();
//			System.out.println("\n\n\nimagePath: " + imagePath + "\n\n\n");
//			return(imageName);
		}
		else if (files.size() == 1) {
//			System.out.println("files.get(0) = " + files.get(0)); // DEBUG
			File f = new File( files.get(0) );
			File out = GlobalTempFiles.getInstance().getFile();
//			System.out.println("out.getPath() = " + out.getPath()); // DEBUG

			BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(out));

			byte buf[] = new byte[1024];
			for (int nRead; (nRead = is.read(buf, 0, 1024)) > 0; os.write(buf, 0, nRead))
				;
			os.flush();
			os.close();
			is.close();

			imageName = out.getName();
//			return imageName;
		}
		else
		{
			System.out.println("\n\n\nno change to imagename"); // DEBUG
//			return ""; // no change to imagename
		}

		System.out.println("\n\n\nimagePath: " + imagePath + "\n\n\n"); // DEBUG
		return imageName;
	}

	public String getImageName()
	{
		return imageName;
	}

	public File getImageLocalPath()
	{
		return GlobalTempFiles.getInstance().getDir();
	}

//	public String getImagePath() { return imagePath; }

	public String getDegScale() {
		DecimalFormat df = new DecimalFormat("###,###");
		return df.format(degScale);
	}

	public String getDistScale() {
		DecimalFormat df = new DecimalFormat("###,###");
		return df.format(distScale);
	}

	private static BoundingBox reaspect(BoundingBox bb, int w, int h) {
		// Get boundaries
		float north = bb.getNorth();
		float south = bb.getSouth();
		float east = bb.getEast();
		float west = bb.getWest();

		float dx = Math.abs(east - west);
		float dy = Math.abs(north - south);

		// Reaspect
		if ((w / dx) > (h / dy))
		{
			float d = dy * w / h - dx;
			west -= d / 2;
			east += d / 2;
			System.out.println("REASPECTING - changing ratio WE += " + d);
		}
		else if ((h / dy) > (w / dx))
		{
			float d = dx * h / w - dy;
			south -= d / 2;
			north += d / 2;
			System.out.println("REASPECTING - changing ratio NS += " + d);
		}

		// Check for pan overflows: shift the map up or down if it can
		// N-S: limit navigation
		if(north > 90 && south>-90)
		{
			float off = north - 90;
			north = 90;
			south -= off;
			System.out.println("REASPECTING - shifting NS -= " + off);
		}

		if(south < -90 && north < 90)
		{
			float off = - 90 - south;
			south = -90;
			north += off;
			System.out.println("REASPECTING - shifting NS += " + off);
		}

		// If the map has scrolled enough sideways, then roll the view
		// W-E: wrap navigation
		if(west > 180)
		{
			east -= 180;
			west -= 180;
			System.out.println("REASPECTING - wrapping WE -= 180");
		}

		if(east < -180)
		{
			east += 180;
			west += 180;
			System.out.println("REASPECTING - wrapping WE += 180");
		}

		// If the map is too much reduced, zoom it to fit the view
		if ((Math.abs(east - west)) > 360 && (Math.abs(north - south)) > 180)
		{
			// Which side can be fully extended?
			if(w/360f > h/180f)
			{
				north = 90;
				south = -90;

				float we = 180.0f/h*w;
				west = -we/2;
				east = we/2;
				System.out.println("REASPECTING - NS fit, WE = " + we);
			}
			else
			{
				west = -180;
				east = 180;

				float ns = 360.0f/w*h;
				north = ns/2;
				south = -ns/2;
				System.out.println("REASPECTING - WE fit, NS = " + ns);
			}
		}

//		if ((Math.abs(east - west)) > 360)
//		{
//			east = 180;
//			west = -180;
//			float d = (float)h / w * 180;
//			north = d;
//			south = -d;
//		}
//
//		if ((Math.abs(north - south)) > 180) {
//			north = 90;
//			south = -90;
//			float d = (float)w / h * 90;
//			east = d;
//			west = -d;
//		}
//

//		System.out.println("north = " + north + "; south = " + south + "; east = " + east + "; west = " + west);

		return new BoundingBox(north, south, east, west);
	}

	private class GetImageUrlThread extends Thread {
		private MapService service;
		private BoundingBox bb;
		private int width, height;
		private String url;
		private boolean serviceError = false; // Flag used to detect if a serviceException was thrown by getImageUrl
		private Element error;

		public void run() { sendRequest(); }

		private void sendRequest() {
			try {
				url = service.getImageUrl(bb, width, height);
			}
			catch (ServiceException e) {
				// The service returned an error message (ArcIMS services only)
				serviceError = true;
			}
			catch (Exception e) {
				// Generic error
//				e.printStackTrace(); // DEBUG
				serviceError = true;
				url = null;
			}
		}

		public MapService getService() { return service; }

		public void setParameters(MapService service, BoundingBox bb, int width, int height) {
			this.service = service;
			this.bb = bb;
			this.width = width;
			this.height = height;
		}

		public String getUrl() throws ServiceException {
			if (serviceError) {
				throw new ServiceException();
			}
			return url;
		}

		public Element getResponse() { return service.getLastResponse(); }

	}

	private class HttpThread extends Thread {
		private static final int BUF_LEN = 1024;

		private String stUrl;
		private String path;
		private HttpClient c;

		public void run() { connect(); }

		public void setParameters(String url) { stUrl = url; }

		public String getPath() { return path; }

		public HttpClient getHttpClient() { return c; }

		private void connect() {
			BufferedInputStream is = null;
			BufferedOutputStream os = null;

			try {
				c = new HttpClient(stUrl);
				File tf = GlobalTempFiles.getInstance().getFile();
				c.getFile(tf);
				path = tf.getPath();
			}
			catch (Exception e) {
				path = null;
				e.printStackTrace(); // DEBUG
			}
			finally {
				// Close the streams
				try { is.close(); } catch (Exception e) {}
				try { os.close(); } catch (Exception e) {}
			}
		}

	}
}

class Layer
{
	private final MapService _service;
	private float _transparency = 1.0F;
	private boolean _expanded = true;
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

	public void   setExpanded(boolean isExpanded){_expanded = isExpanded;}
	public boolean isExpanded(){return _expanded;}

	public void setVisible(boolean isVisibile){_visible = isVisibile;}
	public boolean isVisible(){return _visible;}
}

