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
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.util.z3950.jzkitextensions.GNCQLString;
import org.jzkit.search.util.QueryModel.Internal.AttrPlusTermNode;
import org.jzkit.search.util.QueryModel.Internal.AttrValue;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLTermNode;


/**
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>' adds default namespaces to Attributes,
 *         Relations and Structures FIXME: this is a huge mess that just tries to address some of
 *         the inconsistencies of JZKit. Should better be addressed by re-implementing the CQLString
 *         in JZKit.
 */
public class DefaultContextSetCQLString extends GNCQLString {

    private static final long serialVersionUID = 1L;
    private String default_attr_namespace;
    private String default_rel_namespace;
    private String default_struct_namespace;
    private boolean force_def = false;

    /**
     * @param cqlRoot
     */
    /**
     * @param cqlRoot
     * @param default_attr_namespace
     * @param default_rel_namespace
     * @param default_struct_namespace
     */
    public DefaultContextSetCQLString(CQLNode cqlRoot, String default_attr_namespace, String default_rel_namespace, String default_struct_namespace) {
        super(cqlRoot);
        this.default_attr_namespace = default_attr_namespace;
        this.default_rel_namespace = default_rel_namespace;
        this.default_struct_namespace = default_struct_namespace;
    }

    /**
     * parse SQL string and set default context sets of attributes,relations and structures
     */
    public DefaultContextSetCQLString(String cqlString, String default_attr_namespace, String default_rel_namespace, String default_struct_namespace) {
        super(cqlString);
        this.default_attr_namespace = default_attr_namespace;
        this.default_rel_namespace = default_rel_namespace;
        this.default_struct_namespace = default_struct_namespace;
    }


    /**
     * @param force force the overwriting of attribute and relation default context sets
     */
    public void setForceContextSet(boolean force) {
        this.force_def = force;
    }

       /*
       public InternalModelRootNode toInternalQueryModel(ApplicationContext ctx) throws InvalidQueryException {



               InternalModelRootNode result = super.toInternalQueryModel(ctx);

               if (!processed) {
                       // add default context sets
                       if(Log.isDebugEnabled(Geonet.SRU_SEARCH)) Log.debug(Geonet.SRU_SEARCH," adding default context sets..");
                       visitNode(result,null,null) ;
                       processed=true;
               }
               return result;
       } */


    protected void processCQLTermNode(AttrPlusTermNode aptn, CQLTermNode cql_term_node) {

        super.processCQLTermNode(aptn, cql_term_node);

        if (Log.isDebugEnabled(Geonet.SRU_SEARCH))
            Log.debug(Geonet.SRU_SEARCH, "Processing attrplustermnode:" + aptn);
        // Look up conversion information for source node

        // set default relation context set
        if (aptn.getRelation() != null) {
            AttrValue relation = (AttrValue) aptn.getRelation();

            if (relation != null && (relation.getNamespaceIdentifier() == null || force_def)) {
                relation.setNamespaceIdentifier(this.default_rel_namespace);
                if (Log.isDebugEnabled(Geonet.SRU_SEARCH))
                    Log.debug(Geonet.SRU_SEARCH, "Processing relation :" + relation);
            }
        }

        // set default attribute context set

        Object ap_node = aptn.getAccessPoint();
        if (ap_node != null) {
            AttrValue qualifier = (AttrValue) ap_node;

            if (qualifier != null && (qualifier.getNamespaceIdentifier() == null || force_def)) {
                qualifier.setNamespaceIdentifier(this.default_attr_namespace);
                if (Log.isDebugEnabled(Geonet.SRU_SEARCH))
                    Log.debug(Geonet.SRU_SEARCH, "Processing AccessPoint :" + qualifier);

            }

            // FIX incorrect behavior of very old CQL (0.0.7) library
            if (qualifier != null &&
                qualifier.getNamespaceIdentifier().equalsIgnoreCase("srw") &&
                qualifier.getValue().equalsIgnoreCase("serverChoice")) {
                if (Log.isDebugEnabled(Geonet.SRU_SEARCH))
                    Log.debug(Geonet.SRU_SEARCH, "Setting srw context set to cql for serverChoice");
                qualifier.setNamespaceIdentifier("cql");
            }


        }

        // set default structure context set
        if (aptn.getStructure() != null) {
            AttrValue structure = (AttrValue) aptn.getStructure();

            if (structure != null && (structure.getNamespaceIdentifier() == null || force_def)) {
                structure.setNamespaceIdentifier(this.default_struct_namespace);
                if (Log.isDebugEnabled(Geonet.SRU_SEARCH))
                    Log.debug(Geonet.SRU_SEARCH, "Processing structure :" + structure);
            }
        }


    }

       /*
       public void visitNode(QueryNode node, String source_ns, String target_ns) {
               if(Log.isDebugEnabled(Geonet.SRU_SEARCH)) Log.debug(Geonet.SRU_SEARCH,"visitNode");

               if ( node instanceof InternalModelRootNode ) {
                       if(Log.isDebugEnabled(Geonet.SRU_SEARCH)) Log.debug(Geonet.SRU_SEARCH,"Processing root");
                       // No special "Root" Node in cql
                       visitNode(((InternalModelRootNode)node).getChild(), source_ns, target_ns);
               }
               else if ( node instanceof InternalModelNamespaceNode ) {
                       if(Log.isDebugEnabled(Geonet.SRU_SEARCH)) Log.debug(Geonet.SRU_SEARCH,"Processing namespace: "+node);
                       InternalModelNamespaceNode ns_node = (InternalModelNamespaceNode)node;

                               visitNode(ns_node.getChild(), ns_node.getAttrset(), target_ns);

               }
               else if ( node instanceof ComplexNode ) {
                       if(Log.isDebugEnabled(Geonet.SRU_SEARCH)) Log.debug(Geonet.SRU_SEARCH,"Processing complex");
                       visitNode ( ((ComplexNode)node).getLHS(), source_ns, target_ns ) ;
                       visitNode ( ((ComplexNode)node).getRHS(), source_ns, target_ns );

               }
               else if ( node instanceof AttrPlusTermNode ) {
                       AttrPlusTermNode aptn = (AttrPlusTermNode)node;





               }

               if(Log.isDebugEnabled(Geonet.SRU_SEARCH)) Log.debug(Geonet.SRU_SEARCH,"visitNode");

       }
       */

}
