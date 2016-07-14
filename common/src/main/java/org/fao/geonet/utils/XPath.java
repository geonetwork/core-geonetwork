/* ====================================================================
 * The VM Systems, Inc. Software License, Version 1.0
 *
 * Copyright (c) 2001 VM Systems, Inc.  All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED PURSUANT TO THE TERMS OF THIS LICENSE.
 * ANY USE, REPRODUCTION, OR DISTRIBUTION OF THE SOFTWARE OR ANY PART
 * THEREOF CONSTITUTES ACCEPTANCE OF THE TERMS AND CONDITIONS HEREOF.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        VM Systems, Inc. (http://www.vmguys.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "VM Systems" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For written
 *    permission, please contact info@vmguys.com.
 *
 * 5. VM Systems, Inc. and any other person or entity that creates or
 *    contributes to the creation of any modifications to the original
 *    software specifically disclaims any liability to any person or
 *    entity for claims brought based on infringement of intellectual
 *    property rights or otherwise. No assurances are provided that the
 *    software does not infringe on the property rights of others.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE TITLE
 * AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT SHALL VM SYSTEMS, INC.,
 * ITS SHAREHOLDERS, DIRECTORS OR EMPLOYEES BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. EACH RECIPIENT OR USER IS SOLELY RESPONSIBLE
 * FOR DETERMINING THE APPROPRIATENESS OF USING AND DISTRIBUTING THE SOFTWARE
 * AND ASSUMES ALL RISKS ASSOCIATED WITH ITS EXERCISE OF RIGHTS HEREUNDER,
 * INCLUDING BUT NOT LIMITED TO THE RISKS (INCLUDING COSTS) OF ERRORS,
 * COMPLIANCE WITH APPLICABLE LAWS OR INTERRUPTION OF OPERATIONS.
 * ====================================================================
 */

/* Bug Fixes: fix for first level elements - root element was being lost
 *            adapt for latest version of JDOM
 *            																		Simon Pigot, April, 2008
 */


package org.fao.geonet.utils;

import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.ProcessingInstruction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * <p>This class implements a subset of XPath, which allows an element in an XML document to be
 * specified by giving a path to it. In this subset, all paths must be absolute (they must start at
 * the root), and they must lead to a single element (the resulting nodeset must contain just one
 * node).</p>
 *
 * <p>In Xpath, the index numbers of children start at 1. However, all the indexing done in computer
 * data structures starts at zero. To assist in this, all the methods in this class automatically
 * adjust, so the calling application will always use origin-0 indexes.</p>
 */
public class XPath {
    /**
     * The constructor is private so the class cannot be instantiated. The methods are static, so an
     * instance is not needed.
     */
    private XPath() {
    }


    public static String getXPath(Element target) throws JDOMException {
        Element root = target;
        while (root.getParent() != null && root.getParent() instanceof Element)
            root = (Element) root.getParent();
        return getXPath(root, target);
    }

    /**
     * This method generates an XPath that leads from the root element of the document to the target
     * element.
     */
    public static String getXPath(Element root, Object target) throws JDOMException {
        StringBuffer xpath = null;

        if (root == target) {
            xpath = new StringBuffer("/").append(root.getQualifiedName());
        } else {
            xpath = followXPath(root, target);
        }

        return xpath.toString();
    }


    /**
     * This method generates an XPath that leads from the root element of the document to the target
     * element. If the caller knows the target's parent, it can reduce the amount of work.
     */
    public static String getXPath(Element root, Element parent, Object target) throws JDOMException {
        return computeXPath(root, parent, target).toString();
    }


    /**
     * This method generates an XPath that leads from the parent element to the target element. This
     * is a relative path, not an absolute path.
     */
    public static String getChildXPath(Element parent, Object target) throws JDOMException {
        String xpath = null;

        if (parent == target) {
            xpath = ".";
        } else {
            xpath = followXPath(parent, target).toString();

            if (xpath == null) {
                throw new JDOMException("the target element is not a descendant of the parent element");
            }
        }

        return xpath;
    }


    /**
     * Walks the tree to determine the Xpath of the target.
     *
     * NOTE: This does not compare root to target. That should be done before calling this method.
     */
    private static StringBuffer followXPath(Element root, Object target) throws JDOMException {
        StringBuffer xpath;

        if (target instanceof Element) {
            if (((Element) target).isRootElement()) {
                xpath = computeXPath(root, null, target);
            } else {
                xpath = computeXPath(root, ((Element) target).getParent(), target);
            }
        } else if (target instanceof Comment) {
            xpath = computeXPath(root, ((Comment) target).getParent(), target);
        } else if (target instanceof ProcessingInstruction) {
            xpath = computeXPath(root, ((ProcessingInstruction) target).getParent(), target);
        } else if (target instanceof EntityRef) {
            xpath = computeXPath(root, ((EntityRef) target).getParent(), target);
        } else {
            Element parent = findTarget(root, target);
            if (parent != null) {
                xpath = computeXPath(root, parent, target);
            } else {
                throw new JDOMException("the target object is not in the tree (" + target + ")");
            }
        }

        return xpath;
    }


    /**
     * Computes the xpath of the target. At this point its parent is known, so the algorithm can
     * walk up the tree from the parent to the root, which is easier than walking down the tree.
     */
    private static StringBuffer computeXPath(Element root, Parent parent, Object target) throws JDOMException {
        StringBuffer xpath;

        if (target == root) {
            xpath = new StringBuffer("/").append(root.getQualifiedName());
        } else if (parent == root) {
            xpath = new StringBuffer("/").append(root.getQualifiedName());
            xpath.append(targetXPath((Element) parent, target));
        } else {
            xpath = computeXPathToNode(root, (Element) parent);
            xpath.append(targetXPath((Element) parent, target));
        }

        return xpath;
    }


    /**
     * Computes the child's xpath relative to its parent.
     */
    private static StringBuffer targetXPath(Element parent, Object child) throws JDOMException {
        StringBuffer xpath;

        if (parent == null || child == null) {
            throw new JDOMException("can't use null parent or child");
        }

        if (child instanceof Element) {
            xpath = new StringBuffer();
            xpath.append("/").append(((Element) child).getQualifiedName());
            int i = computeTwinIndex(parent, (Element) child);
            if (i > 0) {
                xpath.append("[").append(i).append("]");
            }
        } else {
            // The node is not an Element. The other types do not have
            // a name, so the xpath has to use the ::node() syntax.

            int i = computeChildIndex(parent, child);
            xpath = new StringBuffer("/self::node()[").append(i).append("]");
        }

        return xpath;
    }

    /**
     * Computes the xpath from the root to the node. It returns the result in a StringBuffer.
     */
    private static StringBuffer computeXPathToNode(Element root, Element node) throws JDOMException {
        StringBuffer xpath = new StringBuffer();
        Element n = node;

        if (root == null || node == null) {
            throw new JDOMException("can't use null root or node");
        }

        while (n != null) {
            Element p = null;
            if (n.getParent() != null && n.getParent() instanceof Element)
                p = (Element) (n.getParent());

            if (p != null) {
                int ti = computeTwinIndex(p, n);
                if (ti > 0) {
                    xpath.insert(0, "]");
                    xpath.insert(0, ti);
                    xpath.insert(0, "[");
                }
            }

            xpath.insert(0, n.getQualifiedName());
            xpath.insert(0, "/");
            n = p;
        }

        return xpath;
    }


    /**
     * Walks the tree of Elements from parent down, looking for one that matches the target object.
     * Returns the parent of the target, or null if the target can't be found. This method is called
     * only if the target is not an Element, Comment, or other similar type. Therefore we know the
     * target is not an Element, and therefore can't be the tree root, and therefore it will have a
     * parent.
     */
    private static Element findTarget(Element parent, Object target) {
        Element rslt = null;

        List<?> children = parent.getContent();
        for (int i = 0; rslt == null && i < children.size(); ++i) {
            Object x = children.get(i);

            if (x == target) {
                rslt = parent;
            } else if (x instanceof Element) {
                rslt = findTarget((Element) x, target);
            }
        }

        return rslt;
    }


    /**
     * <p>This method follows the xpath from the root element and returns the element specified by
     * the path. It throws an exception if there is an error. It returns null if the path does not
     * lead to an element.</p>
     *
     * <p>This subset implementation recognizes only three of the XPath operators: '/', '[]', and
     * ::node. It is sufficient to select one node from the XML document based on its heritage and
     * position, and no more.
     *
     * <p>The <code>root</code> parameter is assumed to be the root of the XML document, but this
     * method has no way to verify that. It does check to be sure the path is absolute by verifying
     * the first character is '/'.
     */
    @SuppressWarnings("unchecked")
    public static Object getElement(Element root, String xpath) throws JDOMException {
        //System.out.println("xpath is \"" + xpath + "\"");

        // This works best if root is really a node within a tree, so we
        // can obtain its parent. However, if it has no parent this will
        // set rslt to null. We have code below to check for that.

        Object rslt = root.getParent();

        // This indicates we're at the root of the tree, and so rslt is
        // allowed to be null. Once we've started walking the tree, rslt
        // will only be null if there's a problem.

        boolean atRoot = true;

        int pathPartStart = 0;

        if (xpath.charAt(0) == '/') {
            pathPartStart = 1;

            // If rslt becomes null, it indicates something wrong with the
            // xpath. Don't continue if that happens.

            while ((atRoot || rslt != null) && pathPartStart < xpath.length()) {
                // Extract the next part of the path.

                int pathPartEnd = xpath.indexOf('/', pathPartStart);
                if (pathPartEnd < 0) {
                    // There are no more slashes in the xpath. This path part
                    // includes the end of the input String.

                    pathPartEnd = xpath.length();
                }

                String pathPart = xpath.substring(pathPartStart, pathPartEnd);

                //System.out.println("xpath part is \"" + pathPart + "\"");

                if (rslt == null) {
                    // We're just starting at the root of the tree, so the
                    // only possibility is that the first part of the xpath
                    // represents the root node we were given. We need to verify
                    // that the name matches.

                    if (root.getQualifiedName().equals(pathPart)) {
                        rslt = root;
                    }
                } else {
                    @SuppressWarnings("unused")
                    int doubleColon = -1;
                    @SuppressWarnings("unused")
                    int openBracket = -1;

                    if ((doubleColon = pathPart.indexOf("self::node()[")) >= 0) {
                        // The ::node() syntax has been used, so the desired
                        // node is the nth node of ALL the children (of all
                        // types). The getIndex method subtracts 1 to convert
                        // from the 1-origin index used by Xpath.

                        int childIndex = getIndex(pathPart);
                        //System.out.println("index is " + childIndex);


                        // Get a list of the children.

                        List<?> children = ((Element) rslt).getContent();

                        if (childIndex >= 0 && childIndex < children.size()) {
                            // Get the nth child from that list.

                            rslt = children.get(childIndex);
                            //System.out.println("got child " + childIndex + " of " + children.size());
                        } else {
                            rslt = null;
                            //System.out.println("there are only " + children.size() + " children");
                        }
                    } else if ((openBracket = pathPart.indexOf('[')) >= 0) {
                        // An index is specified, so we'll get all the children
                        // with the given name and then choose the correct one.
                        // NOTE: The index specified in XPath is origin-1, so
                        // getIndex adjusts it before using it to index into the
                        // array.

                        String nodeName = getNodeName(pathPart);
                        int childIndex = getIndex(pathPart);
                        //System.out.println("looking for child with name " + nodeName + " and index " + childIndex);

                        // Get a list of the children with the specified name.

                        List<Element> children = getNamedChildren((Element) rslt, nodeName);

                        if (childIndex >= 0 && childIndex < children.size()) {
                            // Get the nth child from that list.

                            rslt = children.get(childIndex);
                            //System.out.println("got child " + childIndex + " of " + children.size());
                        } else {
                            rslt = null;
                            //System.out.println("there are only " + children.size() + " children");
                        }
                    } else {
                        // No index is specified, so we'll get just the first
                        // child with the given name.
                        List<Element> children = ((Element) rslt).getChildren();

                        for (Element child : children) {
                            if (child.getQualifiedName().equals(pathPart)) {
                                rslt = child;
                                break;
                            }
                        }
                        //System.out.println("Round again with rslt "+rslt);
                    }
                }

                pathPartStart = pathPartEnd + 1;
                atRoot = false;
            }
        } else {
            throw new JDOMException("xpath is not absolute (must begin with '/')");
        }

        return rslt;
    }


    /**
     * Given an xpath to a node in a tree, returns the parent of that node. Returns null if the
     * parent can't be determined. The parent should be an Element, because if it isn't it can't
     * have children.
     */
    public static Element getParentElement(Element root, String xpath) throws JDOMException {
        Element rslt = null;

        String parentXpath = getParentXpath(xpath);
        if (parentXpath != null) {
            Object x = getElement(root, parentXpath);
            if (x instanceof Element) {
                rslt = (Element) x;
            } else {
                throw new JDOMException("parent element should be type Element, not " + x.getClass().getName());
            }
        }

        return rslt;
    }


    /**
     * Given an xpath to a node in a tree, returns an xpath to that node's parent. Returns null if
     * the result can't be generated.
     */
    public static String getParentXpath(String xpath) {
        String rslt = null;

        int lastSlash = xpath.lastIndexOf('/');
        if (lastSlash >= 0) {
            rslt = xpath.substring(0, lastSlash);
        }
//		else {
//			The input path has no slashes, so we can't figure what
//			to do. Just return null.
//		}

        return rslt;
    }


    /**
     * Given a part of a path like "foobar[5]" return the part before the left bracket.
     */
    public static String getNodeName(String pathPart) {
        return pathPart.substring(0, pathPart.indexOf('['));
    }


    /**
     * <p>Given a part of a path like "foobar[5]" or "self::node()[5]" return the index value.</p>
     *
     * <p>NOTE: The value will be converted from the origin-1 index used by Xpath to an origin-0
     * value.</p>
     */
    public static int getIndex(String pathPart) throws JDOMException {
        int rslt = 0;

        // Verify the syntax. The index should have only digits
        // between '[' and ']', and it should be at the end of
        // the path part.

        int lbracket = pathPart.indexOf('[');
        int rbracket = pathPart.indexOf(']');

        if (rbracket == pathPart.length() - 1) {
            String numStr = pathPart.substring(lbracket + 1, rbracket);
            try {
                rslt = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                throw new JDOMException("invalid index in \"" + pathPart + "\"");
            }
        } else {
            throw new JDOMException("index must be the last part of \"" + pathPart + "\"");
        }

        return rslt - 1;
    }


    /**
     * Get the children of an Element with the specified name. This is similar to
     * Element.getChildren(String name) except that it doesn't care about the namespace of the
     * children.
     */
    private static List<Element> getNamedChildren(Element parent, String name) throws JDOMException {
        List<Element> children = new ArrayList<Element>();

        if (parent == null) {
            throw new JDOMException("can't use null parent");
        }

        Iterator<?> it = parent.getChildren().iterator();
        while (it.hasNext()) {
            Object child = it.next();
            if (child instanceof Element && ((Element) child).getQualifiedName().equals(name)) {
                children.add((Element) child);
            }
        }

        return children;
    }


    /**
     * If the parent has other children with the same name, this returns the index of the specified
     * child (origin 1). If the child is uniquely named, this returns -1.
     */
    private static int computeTwinIndex(Element parent, Element child) throws JDOMException {
        int index = -1;

        if (parent == null) {
            throw new JDOMException("can't use null parent");
        }

        List<Element> identicalTwins = getNamedChildren(parent, child.getQualifiedName());

        if (identicalTwins.size() > 1) {
            for (int j = 0; index < 0 && j < identicalTwins.size(); ++j) {
                if (identicalTwins.get(j) == child) {
                    // Add 1 to convert to 1-origin index used by
                    // Xpath.

                    index = j + 1;
                }
            }

            if (index < 0) {
                // ??? Something is wrong. We didn't find child
                // among its parent's children.

                throw new JDOMException("error in tree: node is not listed among its parent's children");
            }
        }

        return index;
    }


    /**
     * Determines the number of the child. The leftmost sibling is 1, the next is 2, etc.
     */
    private static int computeChildIndex(Element parent, Object child) throws JDOMException {
        int index = -1;

        List<?> content = parent.getContent();

        for (int i = 0; index < 0 && i < content.size(); ++i) {
            if (content.get(i) == child) {
                // Add 1 to convert to 1-origin index used by
                // Xpath.

                index = i + 1;
            }
        }

        if (index < 0) {
            // ??? Something is wrong. We didn't find child
            // among its parent's children.

            throw new JDOMException("error in tree: object is not listed among its parent's children");
        }

        return index;
    }
}





/*
 * $Log: XPath.java,v $
 * Revision 1.7  2002/01/31 22:42:19  gwheeler
 * Updated javadocs.
 *
 * Revision 1.6  2002/01/28 21:23:31  gwheeler
 * Fixed findTarget to return the correct result.
 * Added an overloaded version of getXpath to use when the caller already knows
 * the node's parent, to save work in here.
 *
 * Revision 1.5  2002/01/25 16:14:16  gwheeler
 * Made changes to increase efficiency. Realized that several node types
 * (Element, EntityRef, ProcessingInstruction, etc.) have getParent() methods
 * and it is quite easy to walk up the tree from the node to the root. This is
 * much better than walking down the tree to the node.
 *
 * Revision 1.4  2001/12/12 14:55:44  gwheeler
 * Consolidated the code that adjusts from origin-1 counting (as used
 * in Xpath) to origin-0 counting (as needed by the code).
 *
 * Modified some code to account for namespaces.
 *
 * Revision 1.3  2001/10/15 18:34:55  gwheeler
 * Added method getChildXPath to get the relative path from a parent
 * to one of its children. There was similar code in DifferenceFinder,
 * but that was changed to use this method.
 *
 * Revision 1.2  2001/10/11 15:51:05  gwheeler
 * Modified the class so it now supports enough xpath to understand
 * the content of an Element.
 *
 * Revision 1.1.1.1  2001/10/04 18:52:54  gwheeler
 *
 *
 * Revision 1.6  2001/07/13 20:15:10  gwheeler
 * Made constructor private so class cannot be instantiated.
 *
 * Revision 1.5  2001/06/27 18:23:19  gwheeler
 * Fixed followXPath to check for siblings with same name and generate index number
 * if necessary.
 *
 * Changed getElement so it doesn't add a temporary parent to the root element. It turns out
 * the parent wasn't temporary, and making a second call to the method would fail because
 * the node already had a parent.
 *
 * Revision 1.4  2001/06/22 18:12:03  gwheeler
 * Minor changes to match changes in other classes.
 *
 */
