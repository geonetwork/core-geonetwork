/**
 * JXyDiff: An XML Diff Written in Java
 *
 * Contact: pascal.molli@loria.fr
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of QPL/CeCill
 *
 * See licences details in QPL.txt and CeCill.txt
 *
 * Initial developer: Raphael Tani
 * Initial Developer: Gregory Cobena
 * Initial Developer: Gerald Oster
 * Initial Developer: Pascal Molli
 * Initial Developer: Serge Abiteboul
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package fr.loria.ecoo.so6.xml.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/*
 *
 * This code has been duplicated from fr.loria.ecoo.so6.engine.util.FileUtils
 * in order to remove dependencies from so6.jar
 */
public class SmallFileUtils {
    /**
     * Base tmp file directory
     */
    public final static String BASE_TMP_DIR = "so6.tmp";
    private static int number = 1;

    /**
     * Copy the content of the input file path to the output file path.
     *
     * @param input
     * @param output
     * @throws Exception
     */
    public static void copy(String input, String output)
            throws Exception {
        FileInputStream fis = new FileInputStream(input);
        FileOutputStream fos = new FileOutputStream(output);

        byte[] buffer = new byte[1024];

        int length;

        while((length = fis.read(buffer)) != - 1) {
            fos.write(buffer, 0, length);
        }

        fos.flush();
        fos.close();
        fis.close();
    }

    /**
     * Copy the content of the input file to the output file .
     *
     * @param input
     * @param output
     * @throws Exception
     */
    public static void copy(File src, File dst) throws Exception {
        if(src.isDirectory()) {
            createDirIfNotExist(dst.getPath());

            File[] files = src.listFiles();

            for(int i = 0; i < files.length; i++) {
                copy(files[i], new File(dst, files[i].getName()));
            }
        }
        else {
            copy(src.getPath(), dst.getPath());
        }
    }

    /**
     * Create a directory if needed and throw an exception if the creation
     * failed
     */
    public static void createDirIfNotExist(String path)
            throws Exception {
        File f = new File(path);

        if(f.exists() && (f.isDirectory())) {
            return;
        }

        if(f.exists() && (! f.isDirectory())) {
            throw new RuntimeException("Cannot create :" + path + " already exists as a file");
        }

        createDir(path);
    }

    /**
     * Use create a directory and throw an exception if the creation failed or
     * if the directory already exist
     */
    public static void createDir(String path) throws Exception {
        File d = new File(path);

        if(! (d.mkdirs())) {
            throw new Exception("cannot create dir:" + path);
        }
    }

    /**
     * Create a tmp directory in the BASE_TMP_DIR
     */
    public static File createTmpDir() throws Exception {
        File f = new File(getBaseTmpPath() + File.separator + "SO6_" + System.currentTimeMillis());

        while(f.exists()) {
            f = new File(f.getPath() + "_" + number++);
        }

        if(! f.mkdirs()) {
            throw new Exception("cannot create tmpdir:" + f.getPath());
        }

        return f;
    }

    /**
     * Return the base tmp path
     *
     * @return
     */
    public static String getBaseTmpPath() {
        return System.getProperty("java.io.tmpdir") + File.separator + BASE_TMP_DIR;
    }
}
