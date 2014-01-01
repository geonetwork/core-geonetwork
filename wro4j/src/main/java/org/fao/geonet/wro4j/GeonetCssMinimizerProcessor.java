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
    private static final String START_COMMENT = AddFileUriCommentProcessor.START_JS_COMMENT.split("\\Q%s\\E")[0];
    private static final String END_COMMENT = AddFileUriCommentProcessor.END_JS_COMMENT.split("\\Q%s\\E")[0];
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
                    if(cur == '\n' || cur == '\r' || cur == '\f') {
                        state = State.COPY;
                    }
                    break;
                case START_PREFIX:
                    if (cur == '\r' || cur == '\n' || cur == '\f') {
                        state = State.COPY;
                        writer.write("\n\n\n\n");
                    } else {
                        writer.write(cur);
                    }
                    break;
                case END_PREFIX:
                    if (cur == '\r' || cur == '\n' || cur == '\f') {
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
                    } else if (cur == '\r' || cur == '\n' || cur == '\f'){
                        startPrefixMatch = endPrefixMatch = -1;
                    } else if (!prod){
                        if (startPrefixMatch > -1) {
                            if (startPrefixMatch < START_COMMENT.length() && cur == START_COMMENT.charAt(startPrefixMatch)) {
                                startPrefixMatch++;
                                if (startPrefixMatch >= START_COMMENT.length()) {
                                    state = State.START_PREFIX;
                                    writer.write(START_COMMENT);
                                    break;
                                }
                            } else {
                                startPrefixMatch = -1;
                            }
                        }
                        if (endPrefixMatch > -1) {
                            if (endPrefixMatch < END_COMMENT.length() && cur == END_COMMENT.charAt(endPrefixMatch)) {
                                endPrefixMatch++;
                                if (endPrefixMatch > END_COMMENT.length()) {
                                    state = State.END_PREFIX;
                                    writer.write(END_COMMENT);
                                    break;
                                }
                            } else {
                                endPrefixMatch = -1;
                            }
                        }
                    }
                    break;
                default:
                    if (last == '/') {
                        if (cur == '*') {
                            state = State.STARSLASH;
                            startPrefixMatch = 2;
                            endPrefixMatch = 2;
                        } else if (cur == '/') {
                            state = State.SLASHSLASH;
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
