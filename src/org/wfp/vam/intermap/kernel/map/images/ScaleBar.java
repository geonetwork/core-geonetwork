
package org.wfp.vam.intermap.kernel.map.images;

import java.awt.*;

import java.awt.image.BufferedImage;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;

public class ScaleBar
{
	public static final float EARTH_RADIUS_KM = 6371;

	/**
	 * Build a scalebar image.
	 *
	 * @param    lat                 the map latitude
	 * @param    westBL              the map westBL
	 * @param    eastBL              the map eastBL
	 * @param    mapwidth            the width of the map in pixel
	 * @param    initscalewidth      an approximative length in pixel of the scale bar. It will be increased to reach a nice number for the scale.
	 *
	 * @return   The scalebar image
	 *
	 */
	static public BufferedImage getScaleBar(BoundingBox bb, int mapwidth, int initscalewidth)
	{
		double mapKMwidth = getMapRad(bb) * EARTH_RADIUS_KM;
		System.out.println("MAP LENGTH (" + bb + ") ---> " + mapKMwidth);
		ScaleBarInfo sbw = getNormalizedScalebar(mapKMwidth, mapwidth, initscalewidth);
		System.out.println("PREF SCALEBAR WDT " + initscalewidth + " --> " + sbw.px);
		System.out.println("SCALEBAR "+ sbw.kmmant + "E" + sbw.kmexp);
		String kmstr = getKMString(sbw.kmexp, sbw.kmmant);

		return buildScaleBarImage(sbw.px, kmstr);
	}


	/**
	 * Method getKMString
	 *
	 * @param    kmexp               an int
	 * @param    p1                  an int
	 *
	 * @return   a  String
	 */
	private static String getKMString(int exp, int mant)
	{
		StringBuffer sb = new StringBuffer();
		if(exp>=0)
		{
			sb.append(mant);
			for (int i = 0; i < exp; i++)
				sb.append("0");
			sb.append(" Km");
		}
		else
		{
			sb.append("This map is really small!"); // FIXME
		}

		return sb.toString();
	}

	/**
	 * Method getScalebarWidth
	 *
	 * @param    mapKMwidth          a  double
	 * @param    mapwidth            an int
	 * @param    initscalewidth      an approximative length in pixel of the scale bar. It will be increased to reach a nice number for the scale.
	 *
	 * @return   an int
	 */
	private static ScaleBarInfo getNormalizedScalebar(double mapKM, int mapPX, int prefSBpx)
	{
		double initsbw = mapKM/mapPX*prefSBpx; // length in km of the proposed scalebar width
		System.out.println("SB proposed len KM " + initsbw);

		double[] norm = exp(initsbw);
		int quant = (int)Math.ceil(norm[1]);

		int sbkm = (int)(quant * Math.pow(10.0, norm[0]));
		int sbpx = (int)(mapPX * sbkm/mapKM);

		return new ScaleBarInfo((int)norm[0], quant, sbpx);
	}

	static class ScaleBarInfo
	{
		int kmexp;
		int kmmant;
		int px;

		public ScaleBarInfo(int kmexp, int kmmant, int px)
		{
			this.kmexp = kmexp;
			this.kmmant = kmmant;
			this.px = px;
		}
	}

	/**
	 * Computes exponent and mantissa.
	 *
	 * @return   a  double[]  {exp, mant}
	 */
	private static double[] exp(double mant)
	{
		if(mant<=0)
			throw new IllegalArgumentException(""+mant);

		double exp = 0;

		if(mant > 1)
			while(mant > 10)
			{
				exp++;
				mant/=10;
			}
		else
			while(mant < 1)
			{
				exp--;
				mant*=10;
			}

		return new double[]{exp, mant};
	}

	/**
	 * Great Circle Distances
	 *  The great circle distance between two points is often difficult to measure on a globe and,
	 *  in general, cannot be measured accurately on a map due to distortion introduced in representing
	 *  the approximately spherical geometry of the Earth on a flat map.
	 * However, great circle distances can be calculated easily
	 * given the latitudes and longitudes of the two points,
	 * using the following formula from spherical trigonometry:
	 *
	 * [Law of Cosines for Spherical Trigonometry]
	 *     cos D = ( sin a )(sin b) + (cos a)(cos b)(cos P)
	 *
	 * where:
	 *
	 * D is the angular distance between points A and B
	 * a is the latitude of point A
	 * b is the latitude of point B
	 * P is the longitudinal difference between points A and B
	 *
	 * In applying the above formula, south latitudes and west longitudes
	 * are treated as negative angles. Once cos D has been calculated,
	 * the angle D can be determined using the ARCOS function.
	 *
	 * The distance in km is obtained by multiplying the angle D in degrees by 111 km
	 *
	 * @param    lat                 a  float
	 * @param    westBL              a  float
	 * @param    eastBL              a  float
	 *
	 * @return   the lenght between the two points in kilometers
	 */
	private static double getMapLength_LawOfCosines(BoundingBox bb)
	{
		float lat;

		if(bb.getNorth()*bb.getSouth() < 0)
			lat = 0; // take the equator when it is inside the map
		else
			lat = Math.min(Math.abs(bb.getNorth()), Math.abs(bb.getSouth())); // take the more meaningful one

		// This method is mathematically bugged
		// FIXME Furthermore, if W*E<0, we should compute
		//  DIST(w,e) = DIST(W,0) + DIST(0,E)
		// or we get the MINOR distance between the two points
		// (think for instance w=-179,e=179)

		double sina = Math.sin(Math.toRadians(lat));
		double cosa = Math.cos(Math.toRadians(lat));
		double cosd = sina*sina + cosa*cosa*Math.cos(Math.toRadians(bb.getLongDiff()));
		double d = Math.acos(cosd);
		System.out.print(" --- SIN(LAT="+lat+")="+sina);
		System.out.print(" --- COS(LAT="+lat+")="+cosa);
		System.out.println(" --- COSD="+cosd);
		return Math.toDegrees(d) *111;
	}

	/**
	 * Presuming a spherical Earth with radius R (see below),
	 * and the locations of the two points in spherical coordinates(longitude and latitude)
	 * are lon1,lat1 and lon2,lat2
	 * then the
	 * Haversine Formula (from R.W. Sinnott, "Virtues of the Haversine", Sky and Telescope, vol. 68, no. 2, 1984, p. 159):
	 * will give mathematically and computationally exact results.
	 *
	 * The intermediate result c is the great circle distance in radians.
	 * The great circle distance d will be in the same units as R.
	 *
	 * dlon = lon2 - lon1
	 * dlat = lat2 - lat1
	 * a = sin^2(dlat/2) + cos(lat1) * cos(lat2) * sin^2(dlon/2)
	 * c = 2 * arcsin(min(1,sqrt(a)))
	 * d = R * c
	 */
	private static double getMapRad_Haversine(double nrad, double erad, double srad, double wrad)
	{
		double dlon = nrad-srad;
		double dlat = wrad-erad;

		double sindlat2 = Math.sin(dlat/2);
		double sindlon2 = Math.sin(dlon/2);
		double a = sindlat2*sindlat2 + Math.cos(srad) * Math.cos(nrad) * sindlon2 * sindlon2;
		double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));

		System.out.print(" --- SIN(DLAT="+dlat+"/2)="+sindlat2);
		System.out.println(" --- COS(DLON="+dlon+"/2)="+sindlon2);
		System.out.print(" --- A="+a);
		System.out.println(" --- C="+c);

		return c;
	}


	private static double getMapRad(BoundingBox bb)
	{
		double n,s;
		double e = Math.toRadians(bb.getEast());
		double w = Math.toRadians(bb.getWest());

		if(bb.getNorth()*bb.getSouth() < 0)
		{
			// take the equator when it is inside the map
			n=s=0;
		}
		else
			n=s = Math.toRadians(Math.min(Math.abs(bb.getNorth()), Math.abs(bb.getSouth()))); // take the more meaningful one

		if( e*w < 0)
		{
			// compute 2 semi-arcs
			double rad1 = getMapRad_Haversine(n, 0, s, w);
			double rad2 = getMapRad_Haversine(n, e, s, 0);

			return rad1 + rad2;
		}
		else
		{
			return getMapRad_Haversine(n, e, s, w);
		}
	}

	/**
	 * Build the image
	 *
	 * @param    px                  an int
	 * @param    kmstr               a  String
	 *
	 * @return   a  BufferedImage
	 */
	private static BufferedImage buildScaleBarImage(int px, String text)
	{
		Font font = new Font("helvetica", Font.PLAIN, 15);
		Color bg = Color.WHITE;
		Color fg = Color.BLACK;
		int paddingleft   = 5;
		int paddingbottom = 5;
		int dist = 3;
		int sbheight = 10;

		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int fw = fm.stringWidth(text) + 4;
		int fh = fm.getHeight() + 2 ;

		int width = paddingleft + Math.max(fw , px) ;
		int height = fh + dist + sbheight + paddingbottom;

//System.out.println("Creating label image '"+text+"' "+w+"x"+h);

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D    g2d = img.createGraphics();

		g2d.setFont(font);
		g2d.setColor(bg);
		// paddingleft + shadow + offset
		g2d.drawString(text, paddingleft +1 +0, fm.getAscent() -1 );
		g2d.drawString(text, paddingleft +1 +1, fm.getAscent() -1 );
		g2d.drawString(text, paddingleft +1 +1, fm.getAscent() -0 );
		g2d.drawString(text, paddingleft +1 +1, fm.getAscent() +1 );
		g2d.drawString(text, paddingleft +1 +0, fm.getAscent() +1 );
		g2d.drawString(text, paddingleft +1 -1, fm.getAscent() +1 );
		g2d.drawString(text, paddingleft +1 -1, fm.getAscent() -0 );
		g2d.drawString(text, paddingleft +1 -1, fm.getAscent() -1 );

		g2d.setColor(fg);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.drawString(text, paddingleft +1  , fm.getAscent()  );

		g2d.setColor(bg);
		g2d.fillRect(paddingleft, fh + dist -1, px+2, sbheight+2);

		g2d.setColor(fg);
		g2d.fillRect(paddingleft+1, fh + dist , px, sbheight);

		return img;
	}


}
