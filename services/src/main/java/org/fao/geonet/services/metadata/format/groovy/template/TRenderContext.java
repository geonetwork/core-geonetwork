package org.fao.geonet.services.metadata.format.groovy.template;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A render context for rendering a Template Tree.
 *
 * @author Jesse on 11/29/2014.
 */
public class TRenderContext implements Appendable, Closeable {
    private final Map<String, Object> model;
    private final OutputStream outputStream;
    private final OutputStreamWriter writer;

    public TRenderContext(OutputStream outputStream, Map<String, Object> model) {
        this(outputStream, Constants.CHARSET, model);
    }
    public TRenderContext(OutputStream outputStream, Charset charset, Map<String, Object> model) {
        this.outputStream = outputStream;
        this.model = model;
        this.writer = new OutputStreamWriter(outputStream, charset);
    }

    @Override
    public TRenderContext append(CharSequence csq) throws IOException {
        writer.append(csq);
        writer.flush();
        return this;
    }

    @Override
    public TRenderContext append(CharSequence csq, int start, int end) throws IOException {
        writer.append(csq, start, end);
        writer.flush();
        return this;
    }

    @Override
    public TRenderContext append(char c) throws IOException {
        writer.append(c);
        writer.flush();
        return this;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(this.writer);
        IOUtils.closeQuietly(this.outputStream);
    }

    public Object getModelValue(String expr) {
        return this.model.get(expr);
    }
}
