//==============================================================================
//===
//===   CopyFiles
//===
//==============================================================================
//===	Lifted from the net  - 
//=== http://forum.java.sun.com/thread.jspa?threadID=328293&messageID=1334818
//==============================================================================

package org.fao.geonet.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
 
public class FileCopyMgr {

		private static void copy(File source, File target) throws IOException { 
      FileChannel sourceChannel = new FileInputStream(source).getChannel();
      FileChannel targetChannel = new FileOutputStream(target).getChannel();
      sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
      sourceChannel.close();
      targetChannel.close();
		}
 
		public static void copyFiles(String strPath, String dstPath) 
														throws IOException {

			File src = new File(strPath);
			File dest = new File(dstPath);
			copyFiles(src, dest);
		}

		public static void removeDirectoryOrFile(File dir) throws IOException {
			if (dir.isDirectory()) {
				File list[] = dir.listFiles();
				for (int i = 0; i < list.length; i++) {
					list[i].delete(); 
				}
			}
			dir.delete();
		}

		public static void copyFiles(File src, File dest) 
														throws IOException {
			if (src.isDirectory()) {
				if(dest.exists()!=true)
					dest.mkdirs();
				String list[] = src.list();

				for (int i = 0; i < list.length; i++) {
					String dest1 = dest.getAbsolutePath() + File.separator + list[i];
					String src1 = src.getAbsolutePath() + File.separator + list[i];
					copyFiles(src1 , dest1);
				}
			} else {
				boolean ready = dest.createNewFile(); 
				copy(src,dest); 
			}
		}
}
//==============================================================================
