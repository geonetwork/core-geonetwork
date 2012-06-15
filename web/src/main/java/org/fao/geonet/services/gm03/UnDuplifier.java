package org.fao.geonet.services.gm03;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compare the nodes one by one and remove duplicates using interlis references.
 */
public class UnDuplifier {
    private final Map<Integer, List<Node>> nodesByHash = new HashMap<Integer, List<Node>>();
    private final Map<String, String> translations = new HashMap<String, String>();

    /**
     * Add a node to un-duplify.
     *
     * @return true if it was a duplified node.
     */
    public boolean addNode(Node root, Node node) throws ISO19139CHEtoGM03.FlattenerException {
        // extents should not be removed because of reusable objects.  A real corner case but possible
        if(node.getNodeName().equals("GM03_2Core.Core.EX_Extent")) return false;

        int hash = computeNodeHash(node);
        List<Node> curSlot = nodesByHash.get(hash);
        if (curSlot == null) {
            final ArrayList<Node> newSlot = new ArrayList<Node>(1);
            newSlot.add(node);
            nodesByHash.put(hash, newSlot);
            return false;
        } else {
            for (int i = 0; i < curSlot.size(); i++) {
                Node orig = curSlot.get(i);
                if (equals(node, orig)) {
                    String nodeTid = node.getAttributes().getNamedItem("TID").getTextContent();
                    String origTid = orig.getAttributes().getNamedItem("TID").getTextContent();
                    root.removeChild(node);
                    translations.put(nodeTid, origTid);
                    return true;
                }
            }
            curSlot.add(node);
            return false;
        }
    }

    /**
     * Translate the REF attributes
     *
     * @param node
     * @throws ISO19139CHEtoGM03.FlattenerException
     *
     */
    public void translateRefs(Node node) throws org.fao.geonet.services.gm03.ISO19139CHEtoGM03.FlattenerException {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                Node ref = node.getAttributes().getNamedItem("REF");
                if (ref != null) {
                    String newRef = translations.get(ref.getTextContent());
                    if (newRef != null) {
                        ref.setTextContent(newRef);
                    }
                }
                NodeList childs = node.getChildNodes();
                for (int i = 0; i < childs.getLength(); ++i) {
                    Node cur = childs.item(i);
                    translateRefs(cur);
                }
                break;

            case Node.TEXT_NODE:
                break;

            default:
                throw new ISO19139CHEtoGM03.FlattenerException("Un-supported node type: " + node.getNodeType());
        }
    }

    private int computeNodeHash(Node node) throws ISO19139CHEtoGM03.FlattenerException {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                NodeList nodes = node.getChildNodes();
                int hash = node.getLocalName().hashCode();
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Node cur = nodes.item(i);
                    hash = hash ^ computeNodeHash(cur);
                }

                NamedNodeMap params = node.getAttributes();
                for (int j = 0; j < params.getLength(); ++j) {
                    Node cur = params.item(j);
                    final String name = cur.getNodeName();
                    if (!name.equals("TID")) {
                        hash = hash ^ name.hashCode();
                        hash = hash ^ cur.getTextContent().hashCode();
                    }
                }
                return hash;

            case Node.TEXT_NODE:
                return node.getTextContent().hashCode();

            case Node.COMMENT_NODE:
                return node.getTextContent().hashCode();

            default:
                throw new ISO19139CHEtoGM03.FlattenerException("Un-supported node type: " + node.getNodeType());
        }
    }

    private static boolean equals(Node a, Node b) throws ISO19139CHEtoGM03.FlattenerException {
        if (a.getNodeType() != b.getNodeType()) return false;

        switch (a.getNodeType()) {
            case Node.ELEMENT_NODE:
                if (!a.getNodeName().equals(b.getNodeName())) return false;
                NodeList childsA = a.getChildNodes();
                NodeList childsB = b.getChildNodes();
                NamedNodeMap attrsA = a.getAttributes();
                NamedNodeMap attrsB = b.getAttributes();
                if (childsA.getLength() != childsB.getLength()) return false;
                if (attrsA.getLength() != attrsB.getLength()) return false;
                for (int i = 0; i < childsA.getLength(); ++i) {
                    Node curA = childsA.item(i);
                    Node curB = childsB.item(i);
                    if (!equals(curA, curB)) return false;
                }
                for (int i = 0; i < attrsA.getLength(); ++i) {
                    Node curA = attrsA.item(i);
                    if (!curA.getNodeName().equals("TID")) {
                        Node curB = attrsB.getNamedItemNS(curA.getNamespaceURI(), curA.getNodeName());
                        if (curB == null) return false;
                        if (!curA.getTextContent().equals(curB.getTextContent()))
                            return false;
                    }
                }

                return true;

            case Node.TEXT_NODE:
                return a.getTextContent().equals(b.getTextContent());

            default:
                throw new ISO19139CHEtoGM03.FlattenerException("Un-supported node type: " + a.getNodeType());
        }

    }
}
