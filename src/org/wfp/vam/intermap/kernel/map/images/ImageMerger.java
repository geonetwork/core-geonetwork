// ==============================================================================
//=== ImageMerger
//===
//=== Merges different files into one.
//===
//=== Coded by Emanuele Tajariol - Dec 2002
//===
//=== Test performed under Java 1.3.1_06 on Linux and JAI 1.1.2 Beta
//==============================================================================

package org.wfp.vam.intermap.kernel.map.images;

import Acme.JPM.Encoders.GifEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codecimpl.PNGCodec;
import com.sun.media.jai.codecimpl.PNGImageEncoder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;

public class ImageMerger {
	static public final int JPG = 0;
	static public final int PNG = 1;
	static public final int GIF = 2;

	/**
	 * Merge several images into one. The first file supplied will be put at top.
	 * The destination image has the same size as the first input file.
	 *
	 * @param images
	 *          is a List of Images or Strings denoting the full path of the input image
	 * @param outputFile
	 *          is a String denoting path+filename of the output image
	 * @param format
	 *          is JPG or PNG as defined in the public constants.
	 *
	 */
	public static void mergeAndSave( List images,
											   List<Float> transparency,
												String outputFile,
												int format)
	{
		BufferedImage bi = merge(images, transparency);
		saveImage(bi, outputFile, format);
	}

	public static void saveImage(BufferedImage bi, String outputFile, int format)
	{
		try
		{
			OutputStream os = new FileOutputStream(outputFile);
			//format=2; // DEBUG
			switch (format)
			{

				case JPG :
					encodeJPG(os, bi);
					break;

				case PNG :
					encodePNG(os, bi);
					break;

				case GIF :
					try
					{
						encodeGIF(os, bi);
					}
					catch (IOException e)
					{
						if (e.getMessage() == "too many colors for a GIF")
							System.out.println("too many colors for a GIF, will try to generate JPG");
						else
							System.out.println("error in encoding GIF file: " + e.getMessage() + "; will try to generate a JPG");

						encodeJPG(os, bi);
					}
					break;

				default :
					try
					{
						encodePNG(os, bi);
						break;
					}
					catch (IOException e)
					{
						System.out.println("error in encoding PNG file: " + e.getMessage() + "; will try to generate a JPG");
						encodeJPG(os, bi);
					}
			}
			os.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** UNUSED */
//	public static byte[] mergeB(Vector inputFileNames, Vector transparency) {
//		BufferedImage bi = merge(inputFileNames, transparency);
//
//		try {
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			encodeGIF(bos, bi);
//			//			encodePNG(bos, bi);
//			//			encodeJPG(bos, bi);
//			byte[] out = bos.toByteArray();
//			bos.close();
//			return out;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

	/**
	 * Encodes the BufferedImage <I>bi</I> into the stream <I>os</I> as a JPG
	 *
	 * --- JAI implementation
	 */
	/*
	 * public static void encodeJPG(OutputStream os, BufferedImage bi) throws
	 * IOException { // We need to eliminate the alpha channel in order to save
	 * the image as a JPEG file. if(bi.getColorModel().hasAlpha()) bi =
	 * dealpha(bi);
	 *
	 * JPEGImageEncoder encoder =
	 * (JPEGImageEncoder)JPEGCodec.createImageEncoder("jpeg", os, null);
	 * JPEGEncodeParam param = encoder.getParam(); float quality = 0.60f;
	 * param.setQuality(quality,false); encoder.setParam(param);
	 * encoder.encode(bi); }
	 */

	public static synchronized void encodeJPG(OutputStream os, BufferedImage bi)
		throws Exception {
		com.sun.image.codec.jpeg.JPEGEncodeParam encodeParam = null;

		// We need to eliminate the alpha channel in order to save the image as a
		// JPEG file.
		if (bi.getColorModel().hasAlpha())
			bi = dealpha(bi);

		// encode JPEG
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
		com.sun.image.codec.jpeg.JPEGEncodeParam jpgParams =
			encoder.getDefaultJPEGEncodeParam(bi);
		jpgParams.setQuality(0.65f, false);
		encoder.setJPEGEncodeParam(jpgParams);
		encoder.encode(bi);
	}

	public static synchronized void encodeJPG(
		OutputStream os,
		BufferedImage bi,
		float quality)
		throws Exception {
		com.sun.image.codec.jpeg.JPEGEncodeParam encodeParam = null;

		// We need to eliminate the alpha channel in order to save the image as a
		// JPEG file.
		if (bi.getColorModel().hasAlpha())
			bi = dealpha(bi);

		// encode JPEG
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
		com.sun.image.codec.jpeg.JPEGEncodeParam jpgParams =
			encoder.getDefaultJPEGEncodeParam(bi);
		jpgParams.setQuality(quality, false);
		encoder.setJPEGEncodeParam(jpgParams);

		encoder.encode(bi);
	}

	/**
	 * Encodes the BufferedImage <I>bi</I> into the stram <I>os</I> --- JAI
	 * implementation
	 */
	public static synchronized void encodePNG(OutputStream os, BufferedImage bi)
		throws IOException {

		PNGEncodeParam pngParams = PNGEncodeParam.getDefaultEncodeParam(bi);
		//	  PNGEncodeParam pngParams = new PNGEncodeParam.RGB();
		pngParams.setBitDepth(8);
		PNGImageEncoder encoder =
			(PNGImageEncoder) PNGCodec.createImageEncoder("png", os, pngParams);
		encoder.setParam(pngParams);

		// TODO make this into a user setting to allow to select for high or low
		// quality images
		// user sets number of colors to be used

		//		PNGEncodeParam.RGB pngParamsRGB = (PNGEncodeParam.RGB)pngParams;
		//		pngParamsRGB.setBitDepth(8);
		//		PNGImageEncoder encoder =
		// (PNGImageEncoder)PNGCodec.createImageEncoder("png", os, pngParamsRGB);
		//		encoder.setParam(pngParamsRGB);

		encoder.encode(bi);
		os.close();
	}

	public static synchronized void encodeGIF(OutputStream os, BufferedImage bi)
		throws IOException {

		GifEncoder encoder = new GifEncoder(bi, os);
		encoder.encode();
		os.close();
	}

	/**
	 * Merges the image listed in <I>images</I>, each with the related <I>transparency</I>.
	 * <I>images</I> items can be Images or String (representing the file path of the image).
	 * Images and Strings can also be mixed in the List.
	 * the higher in the image stack it will be printed.
	 */
	public static BufferedImage merge(List images, List<Float> transparency)
	{
		BufferedImage dest = null;
		Graphics2D destG = null;
		int rule; // This is SRC for the top image, and DST_OVER for the other ones
		float alpha;
		// This is 1.0 for the bottom image, and 0.9 for the other ones

		for (int i = 0, size = images.size(); i < size; i++)
		{
			Object o = images.get(i);
			Image image;
			if(o instanceof String)
			{
				String filename = (String)o;
				image = new ImageIcon(filename).getImage();
			}
			else if(o instanceof Image)
			{
				image = (Image)o;
			}
			else
				throw new IllegalArgumentException(o + " is not an image");

			rule = AlphaComposite.SRC_OVER; // Default value

			// Set alpha
			alpha = transparency.get(i).floatValue();
			//			alpha = 0.9F; 	// Light transparence effect
			//			alpha = 1F; 	// Solid colors

			if (i == 0)
			{
				//- init
				dest = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				destG = dest.createGraphics();

				//- values for top image
				rule = AlphaComposite.SRC; // Rule for 1st image
			}

//			if (i == size - 1)
//			{
//				//- value for bottom image
//				alpha = 1F;
//			}

//			System.out.println("i: " + i + "; alpha: " + alpha); // DEBUG
			destG.setComposite(AlphaComposite.getInstance(rule, alpha));
			destG.drawImage(image, 0, 0, null);
		}

		return dest;
	}

	public static BufferedImage merge(String base, String over, int x, int y)
	{
		Image ibase = new ImageIcon(base).getImage();
		Image iover = new ImageIcon(over).getImage();
		return merge(ibase, iover, x, y);
	}

	public static BufferedImage merge(String base, Image over, int x, int y)
	{
		Image ibase = new ImageIcon(base).getImage();
		return merge(ibase, over, x, y);
	}

	public static BufferedImage merge(Image base, Image over, int x, int y)
	{
		float alpha  = 1.0f;

		int bw = base.getWidth(null);
		int bh = base.getHeight(null);
		int ow = over.getWidth(null);
		int oh = over.getHeight(null);

		BufferedImage dest = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D destG = dest.createGraphics();

		destG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, alpha));
		destG.drawImage(base, 0, 0, null);

		System.out.println("Compositing images ("+ow+","+oh+") over ("+bw+","+bh+") @"+x+"+"+y);

		// negative position starts from lower right corner
		if(x<0)
			x = bw - ow + x;

		if(y<0)
			y = bh - oh + y;

		System.out.println("                   @"+x+"+"+y);
		destG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		destG.drawImage(over, x, y, null);

		return dest;
	}


	/**
	 * You cannot convert to JPEG an image with an alpha channel.
	 */
	public static BufferedImage dealpha(BufferedImage bi) {
		return dealpha(bi, Color.white);
	}

	/**
	 * You cannot convert to JPEG an image with an alpha channel.
	 */
	public static BufferedImage dealpha(BufferedImage bi, Color background) {
		BufferedImage ret =
			new BufferedImage(
			bi.getWidth(),
			bi.getHeight(),
			BufferedImage.TYPE_INT_RGB);
		Graphics2D retG = ret.createGraphics();
		retG.setColor(background);
		retG.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		retG.drawImage(bi, 0, 0, null);
		return ret;
	}

	/** Just a test... */
	private static void printCodecs() {
		Enumeration e = ImageCodec.getCodecs();
		while (e.hasMoreElements()) {
			ImageCodec ic = (ImageCodec) e.nextElement();
			System.out.println(ic.getFormatName());
		}
	}

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println("Usage: ImageMerger destFile srcFile1 ...\n");
			System.exit(1);
		}

		Vector inFiles = new Vector(args.length - 1);
		for (int i = 1; i < args.length; i++)
			inFiles.add(args[i]);

		mergeAndSave(inFiles, null, args[0], GIF);
		System.exit(0);
	}
}

//===
//=== Consider also the following hints to save image file in JVM 1.4
//===

//		         ImageWriteParam iwparam = new MyImageWriteParam();
//            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
//            iwparam.setCompressionQuality(compressionQuality);
//   	try
//		{
//			File file = new File(outputFile);
//			// formatName = "jpeg" | "png" | ...
//			// bi is a BufferedImage
//        	ImageIO.write(bi, formatName, file);
//    	}
//		catch (IOException e)
//		{
//		 e.printStackTrace();
//   	}

//=== Following snippet can be used to save JPG with JVM >= 1.2
//=== It can't save in formats other than JPG

//	/** Encodes the BufferedImage <I>bi</I> into the stream <I>os</I>
//	 *
//	 * --- com.sun...jpg package ---
//	 */
//	public static void encodeJPG(OutputStream os, BufferedImage bi)
//		throws IOException
//	{
//		JPEGImageEncoder encoder =
// (JPEGImageEncoder)JPEGCodec.createJPEGEncoder(os);
//		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
//		float quality = 0.80f;
//		param.setQuality(quality, false);
//		encoder.setJPEGEncodeParam(param);
//		encoder.encode(bi);
//	}
