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

package org.fao.geonet.services.util.z3950.jzkitextensions;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.ServiceDirectory.AttributeSetDBO;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.configuration.api.ConfigurationException;
import org.jzkit.search.util.Profile.*;
import org.jzkit.search.util.QueryModel.Internal.*;
import org.jzkit.search.util.QueryModel.QueryModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * code copied and pasted from JZKit sourcecode to fix a bug in the original class
 *
 * @author 'Ian Ibbotson <ianibbo@googlemail.com>'
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 * @see org.jzkit.search.util.Profile.ProfileServiceImpl
 */
public class GNProfileService implements ProfileService, ApplicationContextAware {

    public static final int ERROR_QUERY = 1;
    public static final int ERROR_CONFIG = 2;
    // private Map m = new HashMap();
    private static Log log = LogFactory.getLog(GNProfileService.class);

//  /** If we can't map directly, abort */
//  private static final int SEMANTIC_ACTION_STRICT = 1;
//  /** If we can't map directly, strip the un-mappable component (Should feedback somehow) */
//  private static final int SEMANTIC_ACTION_STRIP = 2;
//  /** If we can't map directly, do a best effort match */
//  private static final int SEMANTIC_ACTION_FUZZY = 3;
    private ApplicationContext ctx = null;
    private Configuration configuration = null;

    public GNProfileService() {
    }

    public void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

  /* For backwards compatibility */
    // public InternalModelRootNode makeConformant(QueryModel qm, String profile_code) throws ProfileServiceException {
    //   return makeConformant(qm,profile_code,SEMANTIC_ACTION_STRICT);
    // }

    public InternalModelRootNode makeConformant(QueryModel qm,
                                                Map<String, AttributeSetDBO> valid_attributes,
                                                Map<String, AttrValue> service_specific_rewrite_rules,
                                                String profile_code) throws ProfileServiceException {

        InternalModelRootNode result = null;

        // Walk the query tree.. validate each node.
        if (log.isDebugEnabled())
            log.debug("makeConformant profile:" + profile_code + " query:" + qm.toString());

        try {
            ProfileDBO p = configuration.lookupProfile(profile_code);

            if ((p == null) && (valid_attributes == null)) {
                if (log.isDebugEnabled())
                    log.debug("No profile defined and no valid attributes list, unable to rewrite");
                result = qm.toInternalQueryModel(ctx);
            } else {
                if (log.isDebugEnabled()) log.debug("Rewriting");
                result = (InternalModelRootNode) visit(qm.toInternalQueryModel(ctx), "bib-1", valid_attributes, service_specific_rewrite_rules, p);
            }
        } catch (org.jzkit.search.util.QueryModel.InvalidQueryException iqe) {
            throw new ProfileServiceException(iqe.toString(), ERROR_QUERY);
        } catch (ConfigurationException ce) {
            throw new ProfileServiceException(ce.toString(), ERROR_CONFIG);
        }

        // if(log.isDebugEnabled()) log.debug("makeConformant result="+result);
        return result;
    }

    private QueryNode visit(QueryNode qn,
                            String default_namespace,
                            Map<String, AttributeSetDBO> valid_attributes,
                            Map<String, AttrValue> service_specific_rewrite_rules,
                            ProfileDBO p) throws org.jzkit.search.util.QueryModel.InvalidQueryException, ProfileServiceException {

        if (qn == null)
            throw new org.jzkit.search.util.QueryModel.InvalidQueryException("Query node was null, unable to rewrite");

        if (log.isDebugEnabled())
            log.debug("Rewrite: visit instance of " + qn.getClass().getName());

        if (qn instanceof InternalModelRootNode) {
            InternalModelRootNode imrn = (InternalModelRootNode) qn;
            return new InternalModelRootNode(visit(imrn.getChild(), default_namespace, valid_attributes, service_specific_rewrite_rules, p));
        } else if (qn instanceof InternalModelNamespaceNode) {
            InternalModelNamespaceNode imns = (InternalModelNamespaceNode) qn;
            if (log.isDebugEnabled())
                log.debug("child default attrset will be " + imns.getAttrset());
            return new InternalModelNamespaceNode(imns.getAttrset(), visit(imns.getChild(),
                imns.getAttrset(),
                valid_attributes,
                service_specific_rewrite_rules,
                p));
        } else if (qn instanceof ComplexNode) {
            ComplexNode cn = (ComplexNode) qn;

            QueryNode lhs = null;
            QueryNode rhs = null;

            if ((cn.getLHS() != null) && (cn.getLHS().countChildrenWithTerms() > 0))
                lhs = visit(cn.getLHS(), default_namespace, valid_attributes, service_specific_rewrite_rules, p);

            if ((cn.getRHS() != null) && (cn.getRHS().countChildrenWithTerms() > 0))
                rhs = visit(cn.getRHS(), default_namespace, valid_attributes, service_specific_rewrite_rules, p);

            if ((lhs != null) && (rhs != null))
                return new ComplexNode(lhs, rhs, cn.getOp());
            else if (lhs != null)
                return lhs;
            else
                return rhs;
        } else if (qn instanceof AttrPlusTermNode) {
            AttrPlusTermNode aptn = null;

            if ((valid_attributes != null) &&
                (service_specific_rewrite_rules != null) &&
                (valid_attributes.size() > 0)) {
                // Use explain mode - valid queries taken from service itself
                aptn = rewriteUntilValid((AttrPlusTermNode) qn, valid_attributes, service_specific_rewrite_rules, default_namespace);
            } else {
                // Use profile mode - valid queries determined from a pre-arranged profile
                aptn = rewriteUntilValid((AttrPlusTermNode) qn, p, default_namespace);
            }

            // If we are in strict mode, throw an exception
            if (aptn == null)
                throw new ProfileServiceException("Unable to rewrite node. Semantic action was set to strict, and there appears to be no valid alternatives for node " + qn, ERROR_QUERY);

            return aptn;
        } else
            throw new ProfileServiceException("Should never be here");
    }

    private AttrPlusTermNode rewriteUntilValid(AttrPlusTermNode q,
                                               Map<String, AttributeSetDBO> valid_attributes,
                                               Map<String, AttrValue> service_specific_rewrite_rules,
                                               String default_namespace)
        throws org.jzkit.search.util.QueryModel.InvalidQueryException, ProfileServiceException {

        AttrPlusTermNode result = q;

        @SuppressWarnings("unchecked")
        Iterator<String> attrIterator = q.getAttrIterator();
        for (Iterator<String> i = attrIterator; i.hasNext(); ) {
            // 1. extract and rewrite use attribute
            String attr_type = i.next();
            AttrValue av = (AttrValue) q.getAttr(attr_type);
            if (log.isDebugEnabled()) log.debug("Rewriting " + attr_type + "=" + av);
            AttributeSetDBO as = valid_attributes.get(attr_type);

            if (as == null)
                throw new ProfileServiceException("No " + attr_type + " attr types allowed for target repository", 4);

            AttrValue new_av = rewriteUntilValid(av, as.getAttrs(), service_specific_rewrite_rules, default_namespace);
            if (log.isDebugEnabled()) log.debug("Setting attr " + attr_type + " to " + new_av);
            q.setAttr(attr_type, new_av);

        }

        if (log.isDebugEnabled()) log.debug(q.getAttrs());

        return result;
    }

    private AttrValue rewriteUntilValid(AttrValue av,
                                        Set<AttrValue> explain_use_indexes,
                                        Map<String, AttrValue> service_specific_rewrite_rules,
                                        String default_namespace) throws ProfileServiceException {
        AttrValue result = av;

        if (av != null) {
            String av_str_val = av.getWithDefaultNamespace(default_namespace);
            if (explain_use_indexes.contains(av)) {
                if (log.isDebugEnabled())
                    log.debug("No need to rewrite, source index " + av + " is already allowed by target");
            } else {
                if (log.isDebugEnabled())
                    log.debug("Rewrite, source index " + av + " is disallowed, scanning server alternatives allowed=" + explain_use_indexes);
                boolean found = false;
                Set<Entry<String, AttrValue>> entrySet = service_specific_rewrite_rules.entrySet();
                for (Iterator<Entry<String, AttrValue>> i = entrySet.iterator(); i.hasNext() && !found; ) {
                    Entry<String, AttrValue> e = i.next();
                    if (e.getKey().equals(av_str_val)) {
                        AttrValue new_av = (AttrValue) e.getValue();
                        if (log.isDebugEnabled()) log.debug("Possible rewrite: " + new_av);
                        if (explain_use_indexes.contains(new_av)) {
                            if (log.isDebugEnabled()) log.debug("Matched, replacing");
                            result = new_av;
                            found = true;
                        }
                    }
                }
                if (!found) {
                    if (log.isDebugEnabled()) log.debug("Unable to rewrite query, exception");
                    throw new ProfileServiceException("Unable to rewrite access point '" + av_str_val + "' to comply with service explain record", ERROR_QUERY);
                }
            }
        }

        return result;
    }

    /**
     * Continue to rewrite the source query until one which validates agains the profile is found.
     * Returns null if there are no valid expansions.
     */
    private AttrPlusTermNode rewriteUntilValid(AttrPlusTermNode q,
                                               ProfileDBO p,
                                               String default_namespace) throws ProfileServiceException {

        if (log.isDebugEnabled()) log.debug("rewriteUntilValid.... def ns = " + default_namespace);

        QueryVerifyResult qvr = p.validate(q, default_namespace);
        // if ( p.isValid(q, default_namespace) )
        AttrPlusTermNode result = null;

        if (qvr.queryIsValid()) {
            if (log.isDebugEnabled()) log.debug("Node is conformant to profile.... return it");
            result = q;
        } else {
            if (log.isDebugEnabled())
                log.debug("Node does not conform to profile (" + q.getAccessPoint() + " not allowed by profile " + p.getCode() + ")");
            // Get failing attr from QVR, generate expansions, rewriteUntilValid each expansion.
            // What if failing attr was an AND..?.. Still had to be a component that failed. The Rule that returned false.
            String failing_attr_type = qvr.getFailingAttr();
            AttrValue av = (AttrValue) q.getAttr(failing_attr_type);

            if (av != null) {
                Set<AttrValue> possible_alternatives = lookupKnownAlternatives(av, default_namespace);
                if (possible_alternatives != null) {
                    if (log.isDebugEnabled())
                        log.debug("Check out alternatives for " + failing_attr_type + ":" + possible_alternatives);
                    for (Iterator<AttrValue> i = possible_alternatives.iterator(); ((i.hasNext()) && (result == null)); ) {
                        AttrValue target_av = (AttrValue) i.next();
                        AttrPlusTermNode new_variant = q.cloneForAttrs();
                        new_variant.setAttr(failing_attr_type, target_av);

                        result = rewriteUntilValid(new_variant, p, default_namespace);
                    }
                } else {
                    if (log.isDebugEnabled()) log.debug("No expansions available. Return null");
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("Hmm.. It appears that we failed because a rule required an attr type which is not present in the query tree(" + failing_attr_type + "). Perhaps we should add missing attrs ;)");
            }
        }

        return result;
    }

    private Set<AttrValue> lookupKnownAlternatives(AttrValue av, String default_namespace) {
        Set<AttrValue> result = null;
        try {
            String namespace = av.getNamespaceIdentifier();
            if (namespace == null)
                namespace = default_namespace;

            if (log.isDebugEnabled())
                log.debug("Lookup mappings from namespace " + namespace + " attr value = " + av.getValue());

            CrosswalkDBO cw = configuration.lookupCrosswalk(namespace);

            if (cw != null) {
                AttrMappingDBO am = cw.lookupMapping(av.getValue().toString());
                if (am != null) {
                    result = am.getTargetAttrs();
                }
            } else {
                log.warn("No crosswalk available for source namespace " + namespace);
            }
        } catch (ConfigurationException ce) {
            log.warn("Problem looking up alternatives for " + av.getValue().toString(), ce);
        }

        return result;
    }

}
