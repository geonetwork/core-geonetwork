//=============================================================================
//===  Copyright (C) 2009 World Meteorological Organization
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Timo Proescholdt
//===  email: tproescholdt_at_wmo.int
//==============================================================================

package org.fao.geonet.services.util.z3950;

import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.util.QueryModel.Internal.*;
import org.jzkit.search.util.QueryModel.InvalidQueryException;
import org.jzkit.search.util.QueryModel.QueryModel;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Stack;

/**
 * transforms a JZKit internal query into the GN XML query format
 *
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 */
public class GNXMLQuery {


    ApplicationContext ctx;
    QueryModel querymodel;
    List<String> collections;

    @SuppressWarnings("unchecked")
    public GNXMLQuery(IRQuery q, ApplicationContext ctx) {
        this.ctx = ctx;
        this.querymodel = q.getQueryModel();
        this.collections = q.getCollections();
    }


    public Element toGNXMLRep() {

        GNRemoteQueryDecoder decoder = new GNRemoteQueryDecoder(querymodel, ctx);
        return decoder.getQuery();
    }

    public String toString() {
        return "";
    }

    public List<String> getCollections() {
        return collections;
    }

}

//--------------------------------------------------------------------------
//converts an internal query format query to GN xml
class GNRemoteQueryDecoder {
    private Stack<Element> stack = new Stack<Element>();

    public GNRemoteQueryDecoder(QueryModel qm, ApplicationContext ctx) {
        try {
            InternalModelRootNode rn = qm.toInternalQueryModel(ctx);
            QueryNodeVisitor qnv = new QueryNodeVisitor() {
                public void visit(AttrPlusTermNode aptn) {
                    super.visit(aptn);
                    Element node = new Element("term");

                    if (aptn.getAccessPoint() != null) {
                        node.setAttribute(new Attribute("use", getAttrVal((AttrValue) aptn.getAccessPoint())));
                    }

                    if (aptn.getRelation() != null) {
                        node.setAttribute(new Attribute("relation", getAttrVal((AttrValue) aptn.getRelation())));
                    }

                    if (aptn.getStructure() != null) {
                        node.setAttribute(new Attribute("structure", getAttrVal((AttrValue) aptn.getStructure())));
                    }

                    if (aptn.getTruncation() != null) {
                        node.setAttribute(new Attribute("truncation", getAttrVal((AttrValue) aptn.getTruncation())));
                    }

                    node.addContent(aptn.getTermAsString(false));
                    stack.push(node);
                }

                public void visit(ComplexNode cn) {
                    super.visit(cn);
                    Element rightChild = (Element) stack.pop();
                    Element leftChild = (Element) stack.pop();
                    Element node = new Element(getOpString(cn.getOp()));
                    node.addContent(leftChild);
                    node.addContent(rightChild);
                    stack.push(node);
                }

                public void visit(InternalModelRootNode rn) {
                    super.visit(rn);
                    Element query = new Element("query");

                    //QueryNode node = rn.getChild(); TODO: I dont know if this is important
                    //query.setAttribute("attrset", node.getAttrs().toString() );
                    query.addContent((Element) stack.pop());
                    stack.push(query);
                }

                @Override
                public void onAttrPlusTermNode(AttrPlusTermNode aptn) {
                    if (Log.isDebugEnabled(Geonet.SRU))
                        Log.debug(Geonet.SRU, "doing nothing..." + aptn); //TODO: find out how this is supposed to be used
                }


                /**
                 * @param val
                 * @return
                 * extracts the last index of an attribute (e.G 1.4 becomes 4)
                 */
                private String getAttrVal(AttrValue val) {

                    if (val == null || val.getValue() == null) return null;

                    String value = val.getValue();
                    String ret = value;

                    String[] temp = value.split("\\.");

                    if (temp != null && temp.length > 1) {
                        ret = temp[temp.length - 1];
                    }

                    return ret;
                }

            };
            qnv.visit(rn);
        } catch (InvalidQueryException iqe) {
            Log.error(Geonet.SRU, "GNRemoteQueryDecoder - invalid query error: " + iqe.getMessage(), iqe);
        }
    }

    public Element getQuery() {
        return (Element) stack.peek();
    }

    public String toString() {
        return Xml.getString(getQuery());
    }

    private String getOpString(int op) {
        switch (op) {
            case ComplexNode.COMPLEX_AND:
                return "and";
            case ComplexNode.COMPLEX_ANDNOT:
                return "not";
            case ComplexNode.COMPLEX_OR:
                return "or";
            case ComplexNode.COMPLEX_PROX:
                return "prox";
            default:
                return op + "";
        }
    }

}
