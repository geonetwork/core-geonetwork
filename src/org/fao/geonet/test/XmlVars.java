package org.fao.geonet.test;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manage symbolic variables in XML.
 * <p/>
 * XML requests/responses may carry attributes and/or text values that may need to
 * be reused in subsequent requests/responses. For example when creating a
 * Harvester an "id" is returned. This "id" is then required to e.g.
 * start/stop/remove the Harvester in sa subsequent request. By embedding symbolic
 * variables in the form ${varname} in the test-XML, these values can be extracted and
 * substituted at will. Also XML responses may carry variable content that may cause a
 * mismatch in XML comparison, for example a Date or UUID. These can be made symbolic
 * with e.g. ${date} and ${uuid}.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class XmlVars {
	/**
	 * Variable names as keys, encountered values as values.
	 */
	private Map<String, String> variables = new HashMap<String, String>();

	/**
	 * Regular expression of what a variable declaration looks like: ${example}
	 */
	private static final String VARDECLARATION = "\\$\\{[a-zA-Z_][a-zA-Z0-9_]*\\}";
	/**
	 * Regular expression of what a variable reference looks like: #{example}
	 */
	private static final String VARREFERENCE = "#\\{[a-zA-Z_][a-zA-Z0-9_]*\\}";
	/**
	 * regular expression of what a value can be.
	 */
	private static final String VALUE = "[a-zA-Z0-9_\\-\\:\\/\\.]*";
	private static final Pattern VARDECPATTERN = Pattern.compile(VARDECLARATION);
	private static final Pattern VARREFPATTERN = Pattern.compile(VARREFERENCE);
	private static final Pattern VALUEPATTERN = Pattern.compile(VALUE);


	public XmlVars() {
	}

	private void p(String s) {
		System.out.println("XmlVars: " + s);
	}


	/**
	 * Clear all vars.
	 */
	public void clearVariables() {
		variables.clear();
	}

	/**
	 * Expand some attribute values like local addresses that can only be known at runtime and the values in the variables table.
	 */
	public void expandVariables(Element aCommand) {

		// handle attributes
		List attributes = aCommand.getAttributes();
		//List newAttributes = new ArrayList(attributes.size());
		Attribute attribute;
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			attribute = (Attribute) iter.next();
			String nextKey = attribute.getName();
			String nextValue = attribute.getValue();
			Namespace ns = attribute.getNamespace();

			// Attribute newAttribute = new Attribute(nextKey, nextValue);
			if (XmlVars.containsVariableDeclarations(nextValue)) {
				aCommand.setAttribute(nextKey, replaceVariablesWithValues(nextValue));
			}
			//newAttributes.add(newAttribute);
		}

		//aCommand.setAttributes(newAttributes);

		// text elements...
		String text = aCommand.getText().trim();
		if (XmlVars.containsVariableDeclarations(text)) {
			aCommand.setText(replaceVariablesWithValues(text));
		}

		// handle child elements
		List children = aCommand.getChildren();
		for (int i = 0; i < children.size(); i++) {
			expandVariables((Element) children.get(i));
		}
	}

	/**
	 * Get a variable.
	 */
	public String getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * Set a variable.
	 */
	public void setVariable(String name, String value) {
		variables.put(name, value);
	}

	/**
	 * Store the values we receive in the variables table.
	 */
	public void storeVariables(Element expected, Element received) {

		List attributes = expected.getAttributes();
		Attribute attribute;
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			attribute = (Attribute) iter.next();
			String nextKey = attribute.getName();
			String nextValue = attribute.getValue();
			if (nextValue != null) {
				// JvdB 4.11.08: sometimes an attr may contain multiple vars e.g. ids="${id1},${id2}"
				String[] nextValues = nextValue.split(",");
				for (String nextVal : nextValues) {
					if (XmlVars.containsVariableDeclarations(nextVal)) {
						Path path = Path.createPathToAttribute(expected, nextKey);
						String value = path.findValue(received);
						if (value == null) {
							Element receivedParent = (Element) received.getParent();
							while (receivedParent != null) {
								value = path.findValue(receivedParent);
								if (value == null) {
									receivedParent = (Element) receivedParent.getParent();
								} else {
									break;
								}
							}
						}
						storeVariableValues(nextVal, value);
					}
				}
			}
		}

		String text = expected.getText().trim();
		if (XmlVars.containsVariableDeclarations(text)) {
			Path path = Path.createPathToText(expected);
			String value = path.findValue(received);
			if (value == null) {
				Element receivedParent = (Element) received.getParent();
				while (receivedParent != null) {
					value = path.findValue(receivedParent);
					if (value == null) {
						receivedParent = (Element) receivedParent.getParent();
					} else {
						break;
					}
				}
			}

			storeVariableValues(text, value);
		}

		List expectedChildren = expected.getChildren();
		List receivedChildren = received.getChildren();
		for (int i = 0; i < expectedChildren.size(); i++) {
			storeVariables((Element) expectedChildren.get(i), (Element) receivedChildren.get(i));
		}
	}


	/**
	 * This method is called on the expected response element. It removes variable declarations (because they would not match the actual value received) and replaces variable references with their stores values (so that we can check for some id that we've received earlier).
	 *
	 * @param elm the element to process.
	 */
	public void processVariables(Element elm) {
		// handle attributes
		List attributes = elm.getAttributes();
		// List newAttributes = new ArrayList(attributes.size());
		Attribute attribute;
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			attribute = (Attribute) iter.next();
			String nextKey = attribute.getName();
			String nextValue = attribute.getValue();
			// Attribute newAttribute = new Attribute(nextKey, nextValue);
			if (nextValue != null) {
				// JvdB 4.11.08: sometimes an attr may contain multiple vars e.g. ids="${id1},${id2}"
				String[] nextValues = nextValue.split(",");
				for (String nextVal : nextValues) {
					if (XmlVars.containsVariableDeclarations(nextValue)) {
						elm.setAttribute(nextKey, clearVariables(nextVal));
					}
					if (XmlVars.containsVariableReferences(nextValue)) {
						elm.setAttribute(nextKey, replaceVariablesReferencesWithValues(nextVal));
					}
				}
			}
			// newAttributes.add(newAttribute);
		}
		// elm.setAttributes(newAttributes);

		// text elements...
		String text = elm.getText().trim();
		if (XmlVars.containsVariableDeclarations(text)) {
			elm.setText(clearVariables(text));
		}
		if (XmlVars.containsVariableReferences(text)) {
			elm.setText(replaceVariablesReferencesWithValues(text));
		}

		// handle child elements
		List children = elm.getChildren();
		for (int i = 0; i < children.size(); i++) {
			processVariables((Element) children.get(i));
		}

	}


	/**
	 * Replace the variables in the given String with their currently known values.
	 *
	 * @param s the String to replace the variables in.
	 * @return the result of the replacements.
	 */
	private String replaceVariablesWithValues(String s) {
		for (Iterator<String> iter = variables.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			String val = getVariable(key);
			s = s.replaceAll("\\$\\{" + key + "\\}", val);
		}
		return s;
	}

	/**
	 * Replace the variables in the given String with their currently known values.
	 *
	 * @param s the String to replace the variables in.
	 * @return the result of the replacements.
	 */
	private String replaceVariablesReferencesWithValues(String s) {
		for (Iterator<String> iter = variables.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			String val = getVariable(key);
			s = s.replaceAll("#\\{" + key + "\\}", val);
		}
		return s;
	}

	/**
	 * Set variables values.
	 * This metod reads variable definitions from the expected string and
	 * reads the values or these variables from the received string. After
	 * this call the variables table will contain tuples in the form of
	 * variable name -> variable value.
	 *
	 * @param expected the String in which the variables are defined.
	 * @param received the String in which the values of the variables can be found.
	 */
	private void storeVariableValues(String expected, String received) {

		Matcher expMatcher = VARDECPATTERN.matcher(expected);
		Matcher recMatcher = VALUEPATTERN.matcher((received != null ? received : ""));

		int offset = 0;
		while (expMatcher.find()) {
			String key = expMatcher.group();
			String val = "";
			if (received != null && received.length() > offset + expMatcher.start() && recMatcher.find(offset + expMatcher.start())) {
				val = recMatcher.group();
			}

			setVariable(key.substring(2, key.length() - 1), val);

			offset += (val.length() - key.length());
		}

	}

	/**
	 * Check the given String for variables.
	 *
	 * @param s the String to check for variable definitions
	 * @retun true if variables are defined in the given String, false otherwise.
	 */
	private static boolean containsVariableDeclarations(String s) {
		Matcher matcher = VARDECPATTERN.matcher(s);
		return matcher.find();
	}

	/**
	 * Check the given String for variables.
	 *
	 * @param s the String to check for variable definitions
	 * @retun true if variables are defined in the given String, false otherwise.
	 */
	private static boolean containsVariableReferences(String s) {
		Matcher matcher = VARREFPATTERN.matcher(s);
		return matcher.find();
	}

	/**
	 * Remove all variables from the given String
	 *
	 * @param s the String to remove the variables from.
	 * @return the input String without the variables.
	 */
	private String clearVariables(String s) {
		for (Iterator<String> iter = variables.keySet().iterator(); iter.hasNext();) {
			s = s.replaceAll("\\$\\{" + iter.next() + "\\}", "");
		}
		return s;
	}
}

