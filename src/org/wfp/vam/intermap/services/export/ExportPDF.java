

package org.wfp.vam.intermap.services.export;

import com.lowagie.text.*;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.util.Util;

public class ExportPDF implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

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

		String imagename = mm.merge(1160, 870);
		File imagepath = mm.getImageLocalPath();

		Document document = new Document(pagesize);
		document.addCreator("Geonetwork Intermap");
		document.addTitle("Geonetwork Map");
		document.addCreationDate();

		File pdfFile = GlobalTempFiles.getInstance().getFile(".pdf");
		PdfWriter pw = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
      document.open();

		// Add image
		Image image = Image.getInstance(imagepath.getAbsolutePath()+File.separator+imagename);
		image.scaleToFit(500,400);
		image.setAlignment(Image.MIDDLE);
		document.add(image);

		if(params.getChildText("layerlist") != null) // the user wants the layer list
		{
			document.add(new Paragraph("Layers list:"));
			List llist = new List(true, 20);

			for (int i = 0; i < mm.size(); i++)
			{
				ListItem litem = new ListItem();

				// Add layer name
				String servicename = mm.getServiceRanked(i).getName();
				Chunk name = new Chunk(servicename);
				if( ! mm.isVisibleRanked(i) )
				{
					System.out.println("Service " + servicename + " is hidden");
					Font font = new Font();
					font.setStyle(Font.STRIKETHRU);
					name.setFont(font);
				}
				litem.add(name);

				if(params.getChildText("details") != null) // the user wants details
				{
					// Add transparency info
					Font font = new Font();
					font.setSize(Font.DEFAULTSIZE*0.8f);
					font.setStyle(Font.ITALIC);
					int trasp = mm.getLayerTransparencyRanked(i);
					if(trasp != 100)
					{
						Chunk transp = new Chunk( "   (" + mm.getLayerTransparencyRanked(i)+"%)", font);
						litem.add(transp);
					}
				}

				llist.add(litem);
			}

			document.add(llist);
		}

		if(params.getChildText("boundingbox") != null) // the user wants the bbox
		{
			// Be careful: the requested bbox may be reaspected to respect image dimensions
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

	private PdfPTable getBBTable(BoundingBox bb)
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
	}

	private Rectangle parsePagesize(String ps)
	{
		Rectangle ret = PAGESIZE.get(ps);
		if(ret == null)
			ret = PageSize.A4; // set the default pagesize as you like

		return ret;
	}

//	public static void main(String[] args) throws DocumentException, FileNotFoundException, IOException, BadElementException
//	{
//		Document document = new Document(PageSize.A4);
//
//		PdfWriter pw = PdfWriter.getInstance(document, new FileOutputStream("/tmp/my.pdf"));
//
//		document.addCreator("Geonetwork Intermap");
//		document.addTitle("Geonetwork Map");
//		document.addCreationDate();
//
//      document.open();
//		document.add(new Paragraph("This is a paragraph"));
//		Image image = Image.getInstance(new URL("file:///tmp/1160x870.gif"));
//		System.out.println("XDPI="+image.getDpiX());
//		System.out.println("YDPI="+image.getDpiY());
//
//		image.scaleToFit(500,400);
//		image.setAlignment(Image.MIDDLE);
//		System.out.println("XDPI="+image.getDpiX());
//		System.out.println("YDPI="+image.getDpiY());
//		document.add(image);
//
//			PdfPTable t = new PdfPTable(3);
//
//			PdfPCell empty = new PdfPCell(new Paragraph(""));
//			empty.setBorder(0);
//
//			PdfPCell n = new PdfPCell(new Paragraph("N: 123.456"));
//			n.setVerticalAlignment(Cell.ALIGN_MIDDLE);
//			n.setHorizontalAlignment(Cell.ALIGN_CENTER);
////			n.set
//			PdfPCell e = new PdfPCell(new Paragraph("E: 23.56"));
//			e.setVerticalAlignment(Cell.ALIGN_MIDDLE);
//			e.setHorizontalAlignment(Cell.ALIGN_LEFT);
//			PdfPCell w = new PdfPCell(new Paragraph("W: -23.56"));
//			w.setVerticalAlignment(Cell.ALIGN_MIDDLE);
//			w.setHorizontalAlignment(Cell.ALIGN_RIGHT);
//			PdfPCell s = new PdfPCell(new Paragraph("S: 123.456"));
//			s.setVerticalAlignment(Cell.ALIGN_MIDDLE);
//			s.setHorizontalAlignment(Cell.ALIGN_CENTER);
//
//
//			t.addCell(new PdfPCell(empty));
//			t.addCell(n);
//			t.addCell(new PdfPCell(empty));
//
//			t.addCell(w);
//			t.addCell(new PdfPCell(empty));
//			t.addCell(e);
//
//			t.addCell(new PdfPCell(empty));
//			t.addCell(s);
//			t.addCell(new PdfPCell(empty));
//
//		document.add(t);
//
//		document.close();
//
//	}

}


