package org.fao.geonet.test;

import org.custommonkey.xmlunit.Difference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Compares Nodes for the presence of child Nodes with target name.
 * <p/>
 * <p>
 * Can be used when for example a response contains Nodes with metadata, but
 * we only need to confirm that there are nodes, not the specifics of the MD nodes itself.
 * </p>
 * @author Just van den Broecke - just@justobjects.nl
 */
public class HasChildNodesDifferenceListener extends ChildNodesDifferenceListener
{
	/** The name the target Node must have. */
	private String nodeName;

	/** The name the target's child Node(s) must have. */
	private String childNodeName;

	public HasChildNodesDifferenceListener(String nodeName, String childNodeName)
	{
		this.nodeName = nodeName;
		this.childNodeName = childNodeName;
	}


	/**
	 * Test if child node difference is the presence of the target.
	 */
	public int childNodeDifferenceFound(Difference difference, Node control, Node test)
	{
		if (!control.getNodeName().equals(this.nodeName) || !test.getNodeName().equals(this.nodeName))
		{
			// Not the target node: thus another node hence a difference
			return RETURN_ACCEPT_DIFFERENCE;
		}

		if (!test.hasChildNodes())
		{
			// Test node must have children: thus a difference
			return RETURN_ACCEPT_DIFFERENCE;
		}

		// ASSERT: is target node and has children

		// Check if element children have the specified child node name
		NodeList childNodes = test.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node childNode = childNodes.item(i);

			// If child node is an Element it must have specified tag name
			if (childNode.getNodeType() == Node.ELEMENT_NODE && !childNode.getNodeName().equals(childNodeName))
			{
				return RETURN_ACCEPT_DIFFERENCE;
			}
		}

		// All ok: similar
		return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
	}
}
