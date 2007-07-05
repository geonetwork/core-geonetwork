/**
 * Util.java
 *
 *
 * @author ETj
 */

package org.wfp.vam.intermap.util;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.jdom.Element;



public class Util
{

	/**
	 * Retrieve bbnorth, bb... from params.
	 * If all of them are parsable coords, the corresponding BoundingBox is returned.
	 *
	 * @return   a BoundingBox or null if a bb was not parsable,
	 *
	 * @author ETj
	 */
	public static BoundingBox parseBoundingBox(Element params)
	{
		try
		{
			float n = Float.parseFloat(params.getChildText("bbnorth"));
			float e = Float.parseFloat(params.getChildText("bbeast"));
			float s = Float.parseFloat(params.getChildText("bbsouth"));
			float w = Float.parseFloat(params.getChildText("bbwest"));
			return new BoundingBox(n, s, e, w);
		}
		catch (NullPointerException e) // child not found
		{
			System.err.println("No valid bbox found");
			return null;
		}
		catch (NumberFormatException e) //
		{
			System.err.println("Bad float value ("+e.getMessage()+")");
			return null;
		}
	}

	/**
	 * Try and parse a String as an int.
	 *
	 * @return the parsed int, or defaultValue if parsableInteger is null or not parsable (in latter case, an error will be output)
	 *
	 * @author ETj
	 */
	public static int parseInt(String parsableInteger, int defaultValue)
	{
		if(parsableInteger == null)
			return defaultValue;

		try
		{
			return Integer.parseInt(parsableInteger);
		}
		catch (NumberFormatException e)
		{
			System.err.println("Bad int value '"+parsableInteger+"'");
			return defaultValue;
		}
	}

}

