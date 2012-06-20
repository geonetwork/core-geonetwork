package org.fao.geonet.kernel.reusable;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;

public class SendEmailParameter
{
    public final ServiceContext           context;
    public final Dbms                     dbms;
    public final String                   msg;
    public final Multimap<String, String> emailInfo;
    public final String                   baseURL;
    public final String                   msgHeader;
    public final String                   subject;
    public final boolean                  testing;

    public SendEmailParameter(ServiceContext context, Dbms dbms, String msg, Multimap<String, String> emailInfo,
            String baseURL, String msgHeader, String subject, boolean testing)
    {
        this.context = context;
        this.dbms = dbms;
        this.msg = msg;
        this.emailInfo = emailInfo;
        this.baseURL = baseURL;
        this.msgHeader = msgHeader;
        this.subject = subject;
        this.testing = testing;
    }
}