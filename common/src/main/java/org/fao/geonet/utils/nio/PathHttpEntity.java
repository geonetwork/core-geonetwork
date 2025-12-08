/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.fao.geonet.utils.nio;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;
import org.fao.geonet.utils.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A self contained, repeatable entity that obtains its content from a file.
 *
 * @since 4.0
 */
@NotThreadSafe
public class PathHttpEntity extends AbstractHttpEntity implements Cloneable {

    protected final Path file;
    static final int OUTPUT_BUFFER_SIZE = 4096;


    public PathHttpEntity(final Path file, final ContentType contentType) {
        super(contentType,null);
        this.file = Args.notNull(file, "File");
//        if (contentType != null) {
//            setContentType(contentType.toString());
//        }
    }

    public PathHttpEntity(final Path file) {
        this(file, null);
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        try {
            return Files.size(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getContent() throws IOException {
        return IO.newInputStream(this.file);
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");

        try (final InputStream instream = IO.newInputStream(this.file)) {
            final byte[] tmp = new byte[OUTPUT_BUFFER_SIZE];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                outstream.write(tmp, 0, l);
            }
            outstream.flush();
        }
    }

    /**
     * Tells that this entity is not streaming.
     *
     * @return <code>false</code>
     */
    public boolean isStreaming() {
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // File instance is considered immutable
        // No need to make a copy of it
        return super.clone();
    }


    @Override
    public void close() throws IOException {
         //TODO: should this do anything?

    }
} // class FileEntity
