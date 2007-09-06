

package org.wfp.vam.intermap.services.export;


import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.GlobalTempFiles;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.images.ImageMerger;
import org.wfp.vam.intermap.kernel.map.images.ScaleBar;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.util.Util;

public class ExportPDF implements Service
{
	private final static int MAP_WIDTH_PX = 1160;
	private final static int MAP_HEIGHT_PX = 870;

	private String _northarrowfile;

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		_northarrowfile = config.getMandatoryValue("northArrowImage");
	}

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		MapMerger mm = MapUtil.getMapMerger(context);

		Rectangle pagesize 	= parsePagesize(params.getChildText("pagesize"));
		boolean isLandscape  = isLandscape(params.getChildText("orientation"));
		if(isLandscape)
			pagesize = pagesize.rotate();

		BoundingBox bb = Util.parseBoundingBox(params); // search bb in params
		if( bb != null)
			mm.setBoundingBox(bb);

		String title = params.getChildText("title");
		String copyright = params.getChildText("copyright");

		boolean drawScale = "on".equals(params.getChildText("scalebar"));
		boolean drawArrow = "on".equals(params.getChildText("arrow"));
		boolean showLayerList = "on".equals(params.getChildText("layerlist"));
		boolean showDetails = "on".equals(params.getChildText("details"));

		Document document = new Document(pagesize);

		File pdfFile = GlobalTempFiles.getInstance().getFile(".pdf");
		PdfWriter pw = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

		document.addTitle(title!=null? title: "Geonetwork Map");
		document.addSubject("");
		document.addCreator("Geonetwork Intermap");
		document.addCreationDate();
		document.addAuthor("ETj");
		document.addProducer();
		document.addKeywords(getKeywords(mm));
		if(copyright!=null)
			document.addHeader("Copyright note", copyright);

		if(copyright != null)
			pw.setPageEvent(new PE(copyright));

      document.open();

		// TITLE
		if( title != null)
		{
			Font font = new Font(Font.HELVETICA, 20, Font.NORMAL);
			Paragraph para = new Paragraph(title, font);
			para.setAlignment(Paragraph.ALIGN_CENTER);
			document.add(para);
		}

		// IMAGE
		String map = mm.merge(MAP_WIDTH_PX, MAP_HEIGHT_PX);
		String mapImageFullPath = mm.getImageLocalPath().getAbsolutePath() + File.separatorChar + map;

		if(drawArrow)
		{
			BufferedImage merged = ImageMerger.merge(mapImageFullPath, _northarrowfile, -5, 5);
			mapImageFullPath = GlobalTempFiles.getInstance().getFile().getPath(); // we need a new file: MapMerger could reuse its own file
			ImageMerger.saveImage(merged, mapImageFullPath, ImageMerger.PNG);
		}

		if(drawScale)
		{
			BufferedImage sb = ScaleBar.getScaleBar(mm.getBoundingBox(), MAP_WIDTH_PX, 150);
			BufferedImage merged = ImageMerger.merge(mapImageFullPath, sb, 5, -5);
			mapImageFullPath = GlobalTempFiles.getInstance().getFile().getPath(); // we need a new file: drawArrow may be not requested
			ImageMerger.saveImage(merged, mapImageFullPath, ImageMerger.PNG);
		}

		Image mapImage = Image.getInstance(mapImageFullPath);
		mapImage.scaleToFit(500,400);
		mapImage.setAlignment(Image.MIDDLE);
		document.add(mapImage);

		if(showLayerList)
		{
			document.add(new Paragraph("Layers list:")); // FIXME: i18n me!
			List llist = new List(true, 20);

			for (int i = 0; i < mm.size(); i++)
			{
				ListItem litem = getLayerPara(mm, i, showDetails);
				llist.add(litem);
			}

			document.add(llist);
		}

		if(params.getChildText("boundingbox") != null) // the user wants the bbox
		{
			// Be careful: the requested bbox may have been reaspected to keep the image dimensions ratio
			// Let's say we'll print the reaspected bbox

			PdfPTable t = getBBTable(mm.getBoundingBox());
			t.setSpacingBefore(30);
			document.add(t);
		}

		document.close();

		return new Element("response")
			.addContent(new Element("pdf")
				.addContent(new Element("url").setText(MapUtil.getTempUrl() + "/" + pdfFile.getName())));
	}

	private ListItem getLayerPara(MapMerger mm, int layerRank, boolean showDetails)
	{
		ListItem litem = new ListItem();

		// Add layer name
		String servicename = mm.getServiceRanked(layerRank).getName();
		Chunk name = new Chunk(servicename);

		if( ! mm.isVisibleRanked(layerRank) )
		{
			Font font = new Font();
			font.setStyle(Font.STRIKETHRU);
			name.setFont(font);
		}
		litem.add(name);

		if(showDetails) // the user wants details
		{
			// Add transparency info
			Font font = new Font();
			font.setSize(Font.DEFAULTSIZE*0.8f);
			font.setStyle(Font.ITALIC);
			int trasp = mm.getLayerTransparencyRanked(layerRank);
			if(trasp != 100)
			{
				Chunk transp = new Chunk( "   (" + mm.getLayerTransparencyRanked(layerRank)+"%)", font);
				litem.add(transp);
			}
		}
		return litem;
	}

	private String getKeywords(MapMerger mm)
	{
		StringBuffer sb = new StringBuffer("Geonetwork, Intermap");

		for (int layerRank = 0; layerRank < mm.size(); layerRank++)
		{
			if( mm.isVisibleRanked(layerRank) )
			{
				String servicename = mm.getServiceRanked(layerRank).getName();
				sb.append(", ").append(servicename);
			}
		}

		return sb.toString();
	}


	private static PdfPTable getBBTable(BoundingBox bb)
	{
		PdfPTable t = new PdfPTable(3);

		PdfPCell empty = new PdfPCell(new Paragraph(""));
		empty.setBorder(0);

		PdfPCell n = new PdfPCell(new Paragraph("N: " + bb.getNorth()));
		n.setVerticalAlignment(Cell.ALIGN_MIDDLE);
		n.setHorizontalAlignment(Cell.ALIGN_CENTER);
		PdfPCell e = new PdfPCell(new Paragraph("E: " + bb.getEast()));
		e.setVerticalAlignment(Cell.ALIGN_MIDDLE);
		e.setHorizontalAlignment(Cell.ALIGN_LEFT);
		PdfPCell w = new PdfPCell(new Paragraph("W: " + bb.getWest()));
		w.setVerticalAlignment(Cell.ALIGN_MIDDLE);
		w.setHorizontalAlignment(Cell.ALIGN_RIGHT);
		PdfPCell s = new PdfPCell(new Paragraph("S: " + bb.getSouth()));
		s.setVerticalAlignment(Cell.ALIGN_MIDDLE);
		s.setHorizontalAlignment(Cell.ALIGN_CENTER);

		t.addCell(new PdfPCell(empty));
		t.addCell(n);
		t.addCell(new PdfPCell(empty));

		t.addCell(w);
		t.addCell(new PdfPCell(empty));
		t.addCell(e);

		t.addCell(new PdfPCell(empty));
		t.addCell(s);
		t.addCell(new PdfPCell(empty));

		return t;
	}

	private boolean isLandscape(String o)
	{
		if(o.equalsIgnoreCase("landscape"))
			return true;
		else if(o.equalsIgnoreCase("portrait"))
			return false;
		else
			return false; // Set the default value to your liking
	}

	private static final Map<String, Rectangle> PAGESIZE = new HashMap<String, Rectangle>();
	static
	{
		PAGESIZE.put("a4", PageSize.A4);
		PAGESIZE.put("a3", PageSize.A3);
		PAGESIZE.put("letter", PageSize.LETTER);
		PAGESIZE.put("legal", PageSize.LEGAL);
		// This list may be extended with other pagesize values
	}

	private Rectangle parsePagesize(String ps)
	{
		Rectangle ret = PAGESIZE.get(ps);
		if(ret == null)
			ret = PageSize.A4; // set the default pagesize as you like

		return ret;
	}

	static class PE extends PdfPageEventHelper
	{
		protected PdfTemplate total;
		protected BaseFont _watermarkFont;
		protected PdfGState gstate;
		protected Phrase footer;

		protected String _watermarkText;

		public PE(String watermark)
		{
			this._watermarkText = watermark;

			footer = new Phrase(watermark);
			Font font = new Font();
			font.setSize(Font.DEFAULTSIZE * 0.9f);
			font.setStyle(Font.ITALIC);
			footer.setFont(font);
		}

		public void onOpenDocument(PdfWriter writer, Document document)
		{
			total = writer.getDirectContent().createTemplate(100, 100);
			total.setBoundingBox(new Rectangle(-20, -20, 100, 100));

			try
			{
				_watermarkFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			}
			catch (Exception e)
			{
				throw new ExceptionConverter(e);
			}

			gstate = new PdfGState();
			gstate.setFillOpacity(0.3f);
			gstate.setStrokeOpacity(0.3f);
		}

		public void onEndPage(PdfWriter writer, Document document)
		{
				// Draw watermark
				PdfContentByte contentunder = writer.getDirectContent();
				contentunder.saveState();
				contentunder.setGState(gstate);
				contentunder.setColorFill(Color.blue);
				contentunder.beginText();
				contentunder.setFontAndSize(_watermarkFont, 48);
				contentunder.showTextAligned(com.lowagie.text.Element.ALIGN_CENTER,
													  _watermarkText,
													  document.getPageSize().getWidth() / 2,
													  document.getPageSize().getHeight() / 2, 45);
				contentunder.endText();
				contentunder.restoreState();

				// Draw footer
				PdfContentByte cb = writer.getDirectContent();
				ColumnText.showTextAligned(cb,
													com.lowagie.text.Element.ALIGN_CENTER,
													footer,
													(document.right() - document.left()) / 2 + document.leftMargin(),
													document.bottom() - 10, 0);
			}
	}

}

