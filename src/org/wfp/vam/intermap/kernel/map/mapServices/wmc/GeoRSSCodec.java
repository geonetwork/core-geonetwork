/**
 * GeoRSSCodec.java
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc;

/**
 * This is a poor man's implementation of georss, adapted on just what gn needs.
 * Please feel free to use some real georss library.
 *
 * A simple GeoRSS Atom feed looks like this:
 *
<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom"
      xmlns:georss="http://www.georss.org/georss">
   <title>Earthquakes</title>

   <subtitle>International earthquake observation labs</subtitle>
   <link href="http://example.org/"/>
   <updated>2005-12-13T18:30:02Z</updated>
   <author>
      <name>Dr. Thaddeus Remor</name>

      <email>tremor@quakelab.edu</email>
   </author>
   <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>
   <entry>
      <title>M 3.2, Mona Passage</title>

      <link href="http://example.org/2005/09/09/atom01"/>
      <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
      <updated>2005-08-17T07:02:32Z</updated>
      <summary>We just had a big one.</summary>

      <georss:point>45.256 -71.92</georss:point>
      </entry>
</feed>

 *
 * @author ETj
 */

import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;
import org.wfp.vam.intermap.kernel.marker.Marker;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;

public class GeoRSSCodec
{
	private final static Namespace NS_ATOM   = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
	private final static Namespace NS_GEORSS = Namespace.getNamespace("georss", "http://www.georss.org/georss");

	public static Element getGeoRSS(MarkerSet ms)
	{
		Element feed = new Element("feed", NS_ATOM);
		feed.addContent(new Element("title", NS_ATOM).setText("intermap markerset"));

		for (Marker marker : ms)
		{
			feed.addContent(getGeoRSSEntry(marker));
		}

		return feed;
	}

	private static Element getGeoRSSEntry(Marker marker)
	{
		Element entry = new Element("entry", NS_ATOM);
		entry.addContent(new Element("title", NS_ATOM).setText(marker.getTitle()));
		entry.addContent(new Element("summary", NS_ATOM).setText(marker.getDesc()));
		entry.addContent(new Element("point", NS_GEORSS).setText(marker.getLat()+" "+marker.getLon()));
		return entry;
	}

	public static MarkerSet parseGeoRSS(Element feed)
	{
		MarkerSet ms = new MarkerSet();

		if(feed.getName().equals("feed"))
		{
			List<Element> entries = (List<Element>)feed.getChildren("entry", NS_ATOM);
			for(Element entry : entries)
			{
				ms.add(parseGeoRSSEntry(entry));
			}
		}
		else
		{
			throw new IllegalArgumentException("feed element not found. Found '"+feed.getName()+"'");
		}

		return ms;
	}

	/**
	 * Method parseGeoRSSEntry
	 *
	 * @param    entry               an Element
	 *
	 * @return   a  Marker
	 */
	private static Marker parseGeoRSSEntry(Element entry)
	{
		String title = entry.getChildTextTrim("title", NS_ATOM);
		String summary = entry.getChildTextTrim("summary", NS_ATOM);
		String scoords = entry.getChildTextTrim("point", NS_GEORSS);

		String coords[] = scoords.split(" ");
		Double lat = new Double(coords[0]);
		Double lon = new Double(coords[1]);

		return new Marker(lat, lon, title, summary);
	}

}

