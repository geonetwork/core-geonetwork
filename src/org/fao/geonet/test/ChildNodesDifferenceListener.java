package org.fao.geonet.test;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * Base class for DifferenceListeners related to child nodes.
 * <p/>
 * <p>Subclasses get a chance to hook into {@link #childNodeDifferenceFound childNodeDifferenceFound}.
 * to implement a custom difference strategy.
 * </p>
 * @author Just van den Broecke - just@justobjects.nl
 */
public abstract class ChildNodesDifferenceListener implements DifferenceListener
{

	public ChildNodesDifferenceListener()
	{
	}

	/**
	 * Callback from Diff on difference found.
	 */
	public int differenceFound(Difference difference)
	{
		// Only difference id's related to child nodes are valid
		switch (difference.getId()) {
			case DifferenceConstants.HAS_CHILD_NODES_ID:
			case DifferenceConstants.CHILD_NODELIST_LENGTH_ID:
			case DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID:
			case DifferenceConstants.CHILD_NODE_NOT_FOUND_ID:
				break;

			default:
				// Any other id: stop, we have a difference
				return RETURN_ACCEPT_DIFFERENCE;

		}

		// Let subclass figure out the difference related to the child nodes
		return childNodeDifferenceFound(difference, difference.getControlNodeDetail().getNode(),
				difference.getTestNodeDetail().getNode());
	}


	public void skippedComparison(Node control, Node test)
	{
		p("skippedComparison: ctrl=" + control.getNodeName() + " test=" + test.getNodeName());
	}

	/**
	 * Overridden in subclass for custom Node comparison.
	 */
	public abstract int childNodeDifferenceFound(Difference difference, Node control, Node test);

	private void p(String s)
	{
		// System.out.println(s);
	}

}
