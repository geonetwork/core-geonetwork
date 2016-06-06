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

package org.fao.geonet.guiservices.versioning;

import java.util.Comparator;
import java.util.Date;

public class MetadataAction {
    public static final Comparator<MetadataAction> DATE_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            Date compareDate1 = o1.getDate();
            Date compareDate2 = o2.getDate();
            return compareDate1.compareTo(compareDate2);
        }
    };
    public static final Comparator<MetadataAction> DATE_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            Date compareDate1 = o1.getDate();
            Date compareDate2 = o2.getDate();
            return compareDate2.compareTo(compareDate1);
        }
    };
    public static final Comparator<MetadataAction> USERNAME_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String user1 = o1.getUsername();
            String user2 = o2.getUsername();
            return user1.compareTo(user2);
        }
    };
    public static final Comparator<MetadataAction> USERNAME_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String user1 = o1.getUsername();
            String user2 = o2.getUsername();
            return user2.compareTo(user1);
        }
    };
    public static final Comparator<MetadataAction> IP_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String ip1 = o1.getIp();
            String ip2 = o2.getIp();
            return ip1.compareTo(ip2);
        }
    };
    public static final Comparator<MetadataAction> IP_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String ip1 = o1.getIp();
            String ip2 = o2.getIp();
            return ip2.compareTo(ip1);
        }
    };
    public static final Comparator<MetadataAction> ACTION_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String act1 = o1.translatedAction();
            String act2 = o2.translatedAction();
            return act1.compareTo(act2);
        }
    };
    public static final Comparator<MetadataAction> ACTION_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String act1 = o1.translatedAction();
            String act2 = o2.translatedAction();
            return act2.compareTo(act1);
        }
    };
    public static final Comparator<MetadataAction> SUBJECT_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String sub1 = o1.translatedSubject();
            String sub2 = o2.translatedSubject();
            return sub1.compareTo(sub2);
        }
    };
    public static final Comparator<MetadataAction> SUBJECT_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String sub1 = o1.translatedSubject();
            String sub2 = o2.translatedSubject();
            return sub2.compareTo(sub1);
        }
    };
    public static final Comparator<MetadataAction> ID_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            Integer id1 = o1.getId();
            Integer id2 = o2.getId();
            return id1.compareTo(id2);
        }
    };
    public static final Comparator<MetadataAction> ID_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            Integer id1 = o1.getId();
            Integer id2 = o2.getId();
            return id2.compareTo(id1);
        }
    };
    public static final Comparator<MetadataAction> TITLE_COMPARATOR_ASC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String title1 = o1.getTitle();
            String title2 = o2.getTitle();
            return title1.compareTo(title2);
        }
    };
    public static final Comparator<MetadataAction> TITLE_COMPARATOR_DESC = new Comparator<MetadataAction>() {
        @Override
        public int compare(final MetadataAction o1, final MetadataAction o2) {
            String title1 = o1.getTitle();
            String title2 = o2.getTitle();
            return title2.compareTo(title1);
        }
    };
    private Date date;
    private String username;
    private String ip;
    private char action;
    private String subject;
    private int id;
    private String title;
    private Long revision;

    public MetadataAction(final MetadataAction another) {
        this.date = another.date;
        this.username = another.username;
        this.ip = another.ip;
        this.action = another.action;
        this.subject = another.subject;
        this.id = another.id;
        this.title = another.title;
        this.revision = another.revision;
    }

    public MetadataAction() {
    }

    public final Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(final Date d) {
        this.date = (Date) d.clone();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String u) {
        this.username = u;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(final String i) {
        this.ip = i;
    }

    public char getAction() {
        return action;
    }

    public void setAction(final char a) {
        this.action = a;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String s) {
        this.subject = s;
    }

    public int getId() {
        return id;
    }

    public void setId(final int i) {
        this.id = i;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String t) {
        this.title = t;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(final Long r) {
        this.revision = r;
    }

    //TO DO: make it always return the right language.

    /**
     * Alwyays returning english!
     */
    public final String translatedSubject() {
        if (subject.compareTo("all") == 0) {
            return "All";
        } else if (subject.compareTo("metadata") == 0) {
            return "Metadata";
        } else if (subject.compareTo("owner") == 0) {
            return "Owner";
        } else if (subject.compareTo("privileges") == 0) {
            return "Privileges";
        } else if (subject.compareTo("categories") == 0) {
            return "Categories";
        } else if (subject.compareTo("status") == 0) {
            return "Status";
        }
        return null;
    }
    //TO DO: make it always return the right language.

    /**
     * Alwyays returning english!
     */
    public final String translatedAction() {
        switch (action) {
            case 'A':
                return "Added";
            case 'M':
                return "Modified";
            case 'D':
                return "Deleted";
            default:
                return null;
        }
    }
}
