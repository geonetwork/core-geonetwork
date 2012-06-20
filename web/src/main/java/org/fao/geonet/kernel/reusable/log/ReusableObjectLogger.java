//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.reusable.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.internet.AddressException;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.XMLLayout;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;

/**
 * Logs to the records to the associated logger based on the record type and
 * also keeps the records to send to the administrator in a single large email
 * 
 * NOTE: This is not thread safe and Replace has the lock that protects this
 * class. If multiple threads need to share this class then consider refactoring
 * or using unique loggers for each session. Right now shared loggers are being
 * used because it doesnt make sense to have multiple threads going through
 * replace.
 * 
 * @author jeichar
 */
public class ReusableObjectLogger
{

    /**
     * A logger that does not keep the records and therefore cannot email the
     * log.
     */
    public static final ReusableObjectLogger THREAD_SAFE_LOGGER = new ReusableObjectLogger()
            {

                @Override
                protected void init() throws IOException
                {
                    // do
                    // nothing
                }

                @Override
                protected void appendMessage(String arg0,
                        Record arg1)
                {
                    // no
                    // impl
                }

                @Override
                public void sendEmail(ServiceContext arg0, boolean testing)
                        throws AddressException, MessagingException
                {
                    throw new MethodNotSupportedException(
                            "this cannot be used");
                }
            };

    final long                               startTime          = System.currentTimeMillis();
    final Map<Record.Type, StringBuilder>    messages           = new HashMap<Record.Type, StringBuilder>();

    public ReusableObjectLogger()
    {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void init() throws IOException
    {
        // for each session Log to a different file
        // There is a log in the Replace.execute method to ensure there is only
        // one thread at a time running through here
        // so this is acceptable
        Appender appender = Record.Type.parentLogger().getAppender("tmp.reusable");
        if (appender == null) {
            appender = Record.Type.parentLogger().getAppender("reusable");
        }

        String rootFile;
        if (appender instanceof FileAppender) {
            FileAppender fAppender = (FileAppender) appender;
            rootFile = fAppender.getFile();
            if (rootFile.endsWith(".log")) {
                rootFile = rootFile.substring(0, rootFile.lastIndexOf(".log"));
            }
        } else {
            rootFile = "logs/reusable";
        }

        for (Record.Type type : Record.Type.values()) {
            XMLLayout layout = new XMLLayout();
            FileAppender newAppender = new FileAppender(layout, rootFile + "_" + startTime + "_" + type.desc + ".log");
            newAppender.setName(type.desc);
            Logger logger = type.logger();
            logger.removeAppender(type.desc);
            logger.addAppender(newAppender);
        }
    }

    protected void appendMessage(String prefix, Record record)
    {
        StringBuilder message = messages.get(record.type());
        if (message == null) {
            message = new StringBuilder();
            messages.put(record.type(), message);
        }

        message.append(prefix + " " + record.message());
        message.append("\n\n");
    }

    public void log(Record record) throws AddressException, MessagingException
    {
        logger(record).log(record.level, record.message());
        if (logger(record).isEnabledFor(record.level)) {
            appendMessage(record.level + ":", record);
        }
    }

    private Logger logger(Record record)
    {
        return record.type().logger();
    }

    public void sendEmail(ServiceContext context, boolean testing) throws AddressException, MessagingException
    {

        if (messages.isEmpty()) {
            return;
        }

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        String emailSubject = "New potential shared metadata elements";

        String start = SimpleDateFormat.getDateTimeInstance().format(new Date(startTime));
        String end = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));

        StringBuffer emailBody = new StringBuffer();

        for (Map.Entry<Record.Type, StringBuilder> entry : messages.entrySet()) {
            if (entry.getValue().length() > 0) {
                emailBody.append(entry.getKey() + " from update starting at " + start + " until " + end);
                emailBody.append("\n============================================================\n");
                emailBody.append(entry.getValue());
                emailBody.append("\n------------------------------------------------------------\n");
                emailBody.append("\n\n");
            }
        }

        try {
            gc.getEmail().sendToAdmin(emailSubject, emailBody.toString(),testing);
        } catch (Exception e) {
            Log.error(Geocat.Module.REUSABLE,
                    "Unable to send message to admin about new missed reusable objects");
        }
    }

    public void log(Level priority, Record.Type type, String msg) throws AddressException, MessagingException
    {
        log(new Record(priority, type, msg));
    }
}
