package org.fao.geonet.wro4j;

import org.slf4j.LoggerFactory;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static org.fao.geonet.wro4j.AddFileUriCommentProcessor.*;

/**
 * Remove comments and whitespace from css but keep the file separator comments
 * <p/>
 * User: Jesse
 * Date: 12/2/13
 * Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.CSS)
@Minimize
public class GeonetCssMinimizerProcessor implements ResourcePreProcessor {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GeonetCssMinimizerProcessor.class);

    public static final String PROD_ALIAS = "geonetProdCssMin";
    public static final String DEV_ALIAS = "geonetDevCssMin";

    final boolean prod;

    public GeonetCssMinimizerProcessor(boolean prod) {
        this.prod = prod;
    }

    enum State {
        SLASHSLASH, STARSLASH, COPY, START_PREFIX, END_PREFIX
    }

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        int startPrefixMatch = -1;
        int endPrefixMatch = -1;

        int cur = reader.read();
        int last = -1;
        State state = State.COPY;
        while (cur != -1) {
            switch (state) {
                case SLASHSLASH:
                    if(cur == '\n' || cur == '\r') {
                        state = State.COPY;
                    } else if (!prod){
                        if (startPrefixMatch > -1) {
                            if (cur == START_FILE_COMMENT.charAt(startPrefixMatch)) {
                                startPrefixMatch++;
                                if (startPrefixMatch >= START_FILE_COMMENT.length()) {
                                    state = State.START_PREFIX;
                                    writer.write(START_FILE_COMMENT);
                                    break;
                                }
                            } else {
                                startPrefixMatch = -1;
                            }
                        }
                        if (endPrefixMatch > -1) {
                            if (cur == END_FILE_COMMENT.charAt(endPrefixMatch)) {
                                endPrefixMatch++;
                                if (endPrefixMatch > END_FILE_COMMENT.length()) {
                                    state = State.END_PREFIX;
                                    writer.write(END_FILE_COMMENT);
                                    break;
                                }
                            } else {
                                endPrefixMatch = -1;
                            }
                        }
                    }
                    break;
                case START_PREFIX:
                    if (cur == '\r' || cur == '\n') {
                        state = State.COPY;
                        writer.write("\n\n\n\n");
                    } else {
                        writer.write(cur);
                    }
                    break;
                case END_PREFIX:
                    if (cur == '\r' || cur == '\n') {
                        state = State.COPY;
                        writer.write("\n\n");
                    } else {
                        writer.write(cur);
                    }
                    break;
                case STARSLASH:
                    if (cur == '/' && last == '*') {
                        state = State.COPY;
                        cur = -1;
                    }
                    break;
                default:
                    if (last == '/') {
                        if (cur == '*') {
                            state = State.STARSLASH;
                        } else if (cur == '/') {
                            state = State.SLASHSLASH;
                            startPrefixMatch = 2;
                            endPrefixMatch = 2;
                        } else {
                            if (last != -1) {
                                writer.write(last);
                            }
                            writer.write(cur);
                        }
                    } else if (cur != '/' && cur != ' ' && cur != '\t' && cur != '\n' && cur != '\r' && cur != '\f') {
                        writer.write(cur);
                    }
                    break;

            }
            last = cur;
            cur = reader.read();
        }
    }
}
