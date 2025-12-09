/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.monitor.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.fao.geonet.api.API;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.List;

/**
 * Services related to thread monitoring and activity.
 *
 * @author Jesse on 2/4/2015.
 */
@RequestMapping(value = {
    "/{portal}/api/site/threads"
})
@Controller("/thread")
public class Threads {
    @RequestMapping(
        value = "/status",
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAnyAuthority('Administrator', 'Monitor')")
    @ResponseBody
    public ThreadResponse status() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = bean.dumpAllThreads(true, true);
        ThreadResponse response = new ThreadResponse();
        response.threadContentionMonitoringEnabled = bean.isThreadContentionMonitoringEnabled();
        response.threadContentionMonitoringSupported = bean.isThreadContentionMonitoringSupported();
        response.threadCpuTimeEnabled = bean.isThreadCpuTimeEnabled();
        response.threadCpuTimeSupported = bean.isThreadCpuTimeSupported();
        HashSet<Long> deadlockedThreadIds = Sets.newHashSet();
        final long[] deadlockedThreads = bean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            for (long id : deadlockedThreads) {
                deadlockedThreadIds.add(id);
            }
        }

        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo.getThreadId() == Thread.currentThread().getId()) {
                continue;
            }
            final long userTime = bean.getThreadUserTime(threadInfo.getThreadId());
            final long cpuTime = bean.getThreadCpuTime(threadInfo.getThreadId());
            boolean isDeadlocked = deadlockedThreadIds.contains(threadInfo.getThreadId());
            response.threads.add(new ThreadDetail(threadInfo, userTime, cpuTime, isDeadlocked));
        }

        return response;
    }

    @RequestMapping(
        value = "/trace/{threadid}",
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAnyAuthority('Administrator', 'Monitor')")
    @ResponseBody
    public StackTrace trace(@PathVariable String threadid) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final StackTraceElement[] stackTrace = bean.getThreadInfo(Long.parseLong(threadid), 50).getStackTrace();
        return new StackTrace(stackTrace);
    }


    @RequestMapping(
        value = "/debugging/{contention}/{enablement}",
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAnyAuthority('Administrator', 'Monitor')")
    @ResponseBody
    public ThreadResponse debugging(
        @PathVariable(value = "contention") boolean threadContentionMonitoring,
        @PathVariable boolean enablement) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        if (threadContentionMonitoring && bean.isThreadContentionMonitoringSupported()) {
            bean.setThreadContentionMonitoringEnabled(enablement);
        }
        if (!threadContentionMonitoring && bean.isThreadCpuTimeSupported()) {
            bean.setThreadCpuTimeEnabled(enablement);
        }

        return status();
    }


    @XmlRootElement(name = "threads")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ThreadResponse implements Serializable {

        public boolean threadContentionMonitoringEnabled;
        public boolean threadContentionMonitoringSupported;
        public boolean threadCpuTimeSupported;
        public boolean threadCpuTimeEnabled;
        @XmlElement(name = "thread")
        private List<ThreadDetail> threads = Lists.newArrayList();

        public boolean isThreadContentionMonitoringEnabled() {
            return threadContentionMonitoringEnabled;
        }

        public boolean isThreadContentionMonitoringSupported() {
            return threadContentionMonitoringSupported;
        }

        public boolean isThreadCpuTimeSupported() {
            return threadCpuTimeSupported;
        }

        public boolean isThreadCpuTimeEnabled() {
            return threadCpuTimeEnabled;
        }

        public List<ThreadDetail> getThreads() {
            return threads;
        }
    }

    @XmlRootElement(name = "thread")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ThreadDetail implements Serializable {

        @XmlElement(name = "name")
        private final String name;
        @XmlElement(name = "id")
        private final long id;
        @XmlElement(name = "state")
        private final String state;
        @XmlElement(name = "userTime")
        private final long userTime;
        @XmlElement(name = "cpuTime")
        private final long cpuTime;
        @XmlElement(name = "waitTime")
        private final long waitTime;
        @XmlElement(name = "blockedTime")
        private final long blockedTime;
        @XmlElement(name = "deadlocked")
        private final boolean deadlocked;

        public ThreadDetail() {
            name = "";
            id = -1;
            state = Thread.State.RUNNABLE.name();
            this.userTime = -1;
            this.cpuTime = -1;
            this.deadlocked = false;
            this.waitTime = -1;
            this.blockedTime = -1;
        }

        public ThreadDetail(ThreadInfo threadInfo, long userTime, long cpuTime, boolean isDeadlocked) {
            this.name = threadInfo.getThreadName();
            this.id = threadInfo.getThreadId();
            this.state = threadInfo.getThreadState().name();
            this.userTime = userTime;
            this.cpuTime = cpuTime;
            this.deadlocked = isDeadlocked;
            this.blockedTime = threadInfo.getBlockedTime();
            this.waitTime = threadInfo.getWaitedTime();
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }

        public String getState() {
            return state;
        }

        public long getUserTime() {
            return userTime;
        }

        public long getCpuTime() {
            return cpuTime;
        }

        public long getWaitTime() {
            return waitTime;
        }

        public long getBlockedTime() {
            return blockedTime;
        }

        public boolean isDeadlocked() {
            return deadlocked;
        }
    }

    @XmlRootElement(name = "stackTrace")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class StackTrace implements Serializable {
        @XmlElement(name = "stackTrace")
        private final String stackTrace;

        private StackTrace() {
            stackTrace = "";
        }

        private StackTrace(StackTraceElement[] stackTrace) {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                builder.append(element.toString()).append('\n');
            }

            this.stackTrace = builder.toString();
        }

        public String getStackTrace() {
            return stackTrace;
        }
    }
}
