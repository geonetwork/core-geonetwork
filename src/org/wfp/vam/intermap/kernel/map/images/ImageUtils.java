/**
 * ImageUtils.java
 *
 * @author ETj
 */

package org.wfp.vam.intermap.kernel.map.images;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class ImageUtils
{
	public static BufferedImage createRect(int w, int h, Color color)
	{
		BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D destG = dest.createGraphics();

		destG.setColor(color);
		destG.fillRect(0,0,w,h);

		return dest;
	}

	public static BufferedImage load(String fullpath)
	{
		Image base = new ImageIcon(fullpath).getImage();

		int bw = base.getWidth(null);
		int bh = base.getHeight(null);

		BufferedImage dest = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D destG = dest.createGraphics();
		destG.drawImage(base, 0, 0, null);
		return dest;
	}


}

