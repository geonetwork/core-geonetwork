//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Lucene configuration class load Lucene XML configuration file.
 * 
 * @author fxprunayre
 * 
 */
public class LuceneConfig {

	private static final int ANALYZER_CLASS = 1;
	private static final int BOOST_CLASS = 2;

	private File configurationFile;
	private String appPath;

	/**
	 * Lucene numeric field configuration
	 */
	public class LuceneConfigNumericField {
		private String name;
		private String DEFAULT_TYPE = "int";
		private String type;
		private int precisionStep = NumericUtils.PRECISION_STEP_DEFAULT;

		public LuceneConfigNumericField(String name, String type,
				String precisionStep) {
			this.name = name;
			if (type != null)
				this.type = type;
			else
				this.type = DEFAULT_TYPE;

			try {
				int p = Integer.valueOf(precisionStep);
				this.precisionStep = p;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public int getPrecisionStep() {
			return precisionStep;
		}

		public boolean equals(Object a) {
			if (this == a)
				return true;
			if (!(a instanceof LuceneConfigNumericField))
				return false;
			LuceneConfigNumericField f = (LuceneConfigNumericField) a;
			return f.getName().equals(this.name);
		}
	};

	private HashSet<String> tokenizedFields = new HashSet<String>();
	private HashMap<String, LuceneConfigNumericField> numericFields = new HashMap<String, LuceneConfigNumericField>();

	private String defaultAnalyzerClass;

	private HashMap<String, String> fieldSpecificAnalyzers = new HashMap<String, String>();
	private HashMap<String, Object[]> analyzerParameters = new HashMap<String, Object[]>();
	private HashMap<String, Class[]> analyzerParametersClass = new HashMap<String, Class[]>();

	private String boostQueryClass;
	private HashMap<String, Object[]> boostQueryParameters = new HashMap<String, Object[]>();
	private HashMap<String, Class[]> boostQueryParametersClass = new HashMap<String, Class[]>();

	private Element luceneConfig;

	private double RAMBufferSizeMB = 48.0d;
	private static final double DEFAULT_RAMBUFFERSIZEMB = 48.0d;

	private int MergeFactor = 10;
	private static final int DEFAULT_MERGEFACTOR = 10;

	private boolean trackDocScores = false;
	private boolean trackMaxScore = false;
	private boolean docsScoredInOrder = false;

	private Version LUCENE_VERSION = Version.LUCENE_30;

	/**
	 * Creates a new Lucene configuration from an XML configuration file.
	 * 
	 * @param appPath
     * @param luceneConfigXmlFile
	 */
	public LuceneConfig(String appPath, String luceneConfigXmlFile) {
		Log.debug(Geonet.SEARCH_ENGINE, "Loading Lucene configuration ...");
		this.appPath = appPath;
		this.configurationFile = new File(appPath + luceneConfigXmlFile);
		this.load();
	}

	private void load() {
		try {
			luceneConfig = Xml.loadStream(new FileInputStream(
					this.configurationFile));

			// Main Lucene index configuration option
			Element elem = luceneConfig.getChild("index");
			String version = elem.getChildText("luceneVersion");

			if (version == null) {
				try {
					LUCENE_VERSION = Version.valueOf("LUCENE_" + version);
				} catch (Exception e) {
					Log.warning(Geonet.SEARCH_ENGINE,
							"Failed to set Lucene version to: " + version
									+ ". Set to default LUCENE_29.");
					LUCENE_VERSION = Version.valueOf("LUCENE_29");
				}
			}

			String rb = elem.getChildText("RAMBufferSizeMB");
			if (rb == null) {
				RAMBufferSizeMB = DEFAULT_RAMBUFFERSIZEMB;
			} else {
				try {
					RAMBufferSizeMB = Double.valueOf(rb);
				} catch (NumberFormatException e) {
					Log.warning(Geonet.SEARCH_ENGINE,
							"Invalid double value for RAM buffer size. Using default value.");
					RAMBufferSizeMB = DEFAULT_RAMBUFFERSIZEMB;
				}
			}

			String mf = elem.getChildText("MergeFactor");
			if (mf == null) {
				MergeFactor = DEFAULT_MERGEFACTOR;
			} else {
				try {
					MergeFactor = Integer.valueOf(mf);
				} catch (NumberFormatException e) {
					Log.warning(Geonet.SEARCH_ENGINE,
							"Invalid integer value for merge factor. Using default value.");
					MergeFactor = DEFAULT_MERGEFACTOR;
				}
			}

			// Tokenized fields
			elem = luceneConfig.getChild("tokenized");
			tokenizedFields = new HashSet<String>();
			if (elem != null) {
				for (Object o : elem.getChildren()) {
					if (o instanceof Element) {
						String name = ((Element) o).getAttributeValue("name");
						if (name == null) {
							Log.warning(
									Geonet.SEARCH_ENGINE,
									"Tokenized element must have a name attribute, check Lucene configuration file.");
						} else {
							tokenizedFields.add(name);
						}
					}
				}
			}

			// Numeric fields
			elem = luceneConfig.getChild("numeric");
			numericFields = new HashMap<String, LuceneConfigNumericField>();
			if (elem != null) {
				for (Object o : elem.getChildren()) {
					if (o instanceof Element) {
						String name = ((Element) o).getAttributeValue("name");
						String type = ((Element) o).getAttributeValue("type");
						String precisionStep = ((Element) o)
								.getAttributeValue("precision");
						if (name == null) {
							Log.warning(
									Geonet.SEARCH_ENGINE,
									"Numeric field element must have a name attribute, check Lucene configuration file.");
						} else {

							LuceneConfigNumericField field = new LuceneConfigNumericField(
									name, type, precisionStep);
							numericFields.put(name, field);
						}
					}
				}
			}

			// Default analyzer
			elem = luceneConfig.getChild("defaultAnalyzer");
			if (elem != null) {
				defaultAnalyzerClass = elem.getAttribute("name").getValue();
				loadClassParameters(ANALYZER_CLASS, "",
						defaultAnalyzerClass, elem.getChildren("Param"));
			}

			// Fields specific analyzer
			elem = luceneConfig.getChild("fieldSpecificAnalyzer");
			fieldSpecificAnalyzers = new HashMap<String, String>();
			for (Object o : elem.getChildren()) {
				if (o instanceof Element) {
					Element e = (Element) o;
					String name = e.getAttributeValue("name");
					String analyzer = e.getAttributeValue("analyzer");
					if (name == null || analyzer == null) {
						Log.warning(
								Geonet.SEARCH_ENGINE,
								"Field must have a name and an analyzer attribute, check Lucene configuration file.");
					} else {
						fieldSpecificAnalyzers.put(name, analyzer);
						loadClassParameters(ANALYZER_CLASS, name, analyzer,
								e.getChildren("Param"));
					}
				}
			}

			// Search
			Element searchConfig = luceneConfig.getChild("search");

			// Boosting
			elem = searchConfig.getChild("boostQuery");
			if (elem != null) {
				// TODO : maybe try to create a boost query instance to
				// check class is in classpath.
				boostQueryClass = elem.getAttribute("name").getValue();
				loadClassParameters(BOOST_CLASS, "", boostQueryClass,
						elem.getChildren("Param"));
			}

			// Score
			elem = searchConfig.getChild("trackDocScores");
			if (elem != null && elem.getText().equals("true")) {
				setTrackDocScores(true);
			}
			elem = searchConfig.getChild("trackMaxScore");
			if (elem != null && elem.getText().equals("true")) {
				setTrackMaxScore(true);
			}
			elem = searchConfig.getChild("docsScoredInOrder");
			if (elem != null && elem.getText().equals("true")) {
				setDocsScoredInOrder(true);
			}

		} catch (FileNotFoundException e) {
			Log.error(
					Geonet.SEARCH_ENGINE,
					"Can't find Lucene configuration file. Error is: "
							+ e.getMessage());
		} catch (IOException e) {
			Log.error(
					Geonet.SEARCH_ENGINE,
					"Failed to load Lucene configuration file. Error is: "
							+ e.getMessage());
		} catch (JDOMException e) {
			Log.error(Geonet.SEARCH_ENGINE,
					"Failed to load Lucene configuration XML file. Error is: "
							+ e.getMessage());
		}
	}

	private void loadClassParameters(int type, String field, String clazz,
			List<Object> children) {
		if (children == null)
			return; // No params

		Log.debug(Geonet.SEARCH_ENGINE, "  Field: " + field
				+ ", loading analyzer " + clazz + " ...");

		Object[] params = new Object[children.size()];
		Class[] paramsClass = new Class[children.size()];
		int i = 0;
		for (Object o : children) {
			if (o instanceof Element) {
				Element c = (Element) o;
				String name = c.getAttributeValue("name");
				String paramType = c.getAttributeValue("type");
				String value = c.getAttributeValue("value");

				Log.debug(Geonet.SEARCH_ENGINE, "    * Parameter: " + name
						+ ", type: " + paramType + ", value: " + value);

				try {

					if ("double".equals(paramType)) {
						paramsClass[i] = double.class;
					} else if ("int".equals(paramType)) {
						paramsClass[i] = int.class;
					} else {
						paramsClass[i] = Class.forName(paramType);
					}

					if ("org.apache.lucene.util.Version".equals(paramType)) {
						params[i] = LUCENE_VERSION;
					} else if ("java.io.File".equals(paramType)
							&& value != null) {
						File f = new File(value);
						if (!f.exists()) { // try relative to appPath
							f = new File(appPath + value);
						}
						if (f != null) {
							params[i] = f;
						}
					} else if ("double".equals(paramType) && value != null) {
						params[i] = Double.parseDouble(value);
					} else if ("int".equals(paramType) && value != null) {
						params[i] = Integer.parseInt(value);
					} else if (value != null) {
						params[i] = value;
					} else {
						// No value. eg. Version
					}

					i++;

				} catch (ClassNotFoundException e) {
					Log.warning(Geonet.SEARCH_ENGINE,
							"  Class not found for parameter: " + name
									+ ", type: " + paramType);
					e.printStackTrace();
					return;
				}

			}
		}

		String id = field + clazz;

		switch (type) {
		case ANALYZER_CLASS:
			analyzerParametersClass.put(id, paramsClass);
			analyzerParameters.put(id, params);
			break;
		case BOOST_CLASS:
			boostQueryParametersClass.put(id, paramsClass);
			boostQueryParameters.put(id, params);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @return The list of tokenized fields which could not determined using
	 *         Lucene API.
	 */
	public HashSet<String> getTokenizedField() {
		return this.tokenizedFields;
	}

	/**
	 * 
	 * @param name
	 * @return	True if the field has to be tokenized
	 */
	public boolean isTokenizedField(String name) {
		return this.tokenizedFields.contains(name);
	}

	/**
	 * 
	 * @return The list of numeric fields which could not determined using
	 *         Lucene API.
	 */
	public HashMap<String, LuceneConfigNumericField> getNumericFields() {
		return this.numericFields;
	}

	/**
	 * 
	 * @return The list of numeric fields which could not determined using
	 *         Lucene API.
	 */
	public LuceneConfigNumericField getNumericField(String fieldName) {
		return this.numericFields.get(fieldName);
	}

	/**
	 * Check if a field is numeric or not
	 * 
	 * @param The
	 *            field name
	 * @return True if the field has to be indexed as a numeric field
	 */
	public boolean isNumericField(String fieldName) {
		return this.numericFields.containsKey(fieldName);
	}

	/**
	 * 
	 * @param analyzer
	 *            The analyzer name (could be a class name or field concatenated
	 *            with class name for specific field analyzer)
	 * @return The list of values for analyzer parameters.
	 */
	public Object[] getAnalyzerParameter(String analyzer) {
		return this.analyzerParameters.get(analyzer);
	}

	/**
	 * 
	 * @param analyzer
	 *            The analyzer name (could be a class name or field concatenated
	 *            with class name for specific field analyzer)
	 * @return The list of classes for analyzer parameters
	 */
	public Class[] getAnalyzerParameterClass(String analyzer) {
		return this.analyzerParametersClass.get(analyzer);
	}

	/**
	 * 
	 * @return The list of values for boost query parameters.
	 */
	public Object[] getBoostQueryParameter() {
		return this.boostQueryParameters.get(boostQueryClass);
	}

	/**
	 * 
	 * @return The list of classes for boost query parameters.
	 */
	public Class[] getBoostQueryParameterClass() {
		return this.boostQueryParametersClass.get(boostQueryClass);
	}

	/**
	 * 
	 * @return Class name of the boosting query or null if not defined.
	 */
	public String getBoostQueryClass() {
		return boostQueryClass;
	}

	/**
	 * 
	 * @return Class name of the default analyzer (default is StandardAnalyzer).
	 */
	public String getDefaultAnalyzerClass() {
		return defaultAnalyzerClass;
	}

	/**
	 * 
	 * @return Each specific fields analyzer.
	 */
	public HashMap<String, String> getFieldSpecificAnalyzers() {
		return this.fieldSpecificAnalyzers;
	}

	/**
	 * 
	 * @return The merge factor.
	 */
	public int getMergeFactor() {
		return MergeFactor;
	}

	/**
	 * 
	 * @return The RAM buffer size.
	 */
	public double getRAMBufferSize() {
		return RAMBufferSizeMB;
	}

	/**
	 * 
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Lucene configuration:\n");
		sb.append(" * RAMBufferSize: " + getRAMBufferSize() + "\n");
		sb.append(" * MergeFactor: " + getMergeFactor() + "\n");
		sb.append(" * Default analyzer: " + getDefaultAnalyzerClass() + "\n");
		sb.append(" * Field analyzers: "
				+ getFieldSpecificAnalyzers().toString() + "\n");
		sb.append(" * Tokenized fields: " + getTokenizedField().toString()
				+ "\n");
		sb.append(" * Numeric fields: "
				+ getNumericFields().keySet().toString() + "\n");
		sb.append(" * Search boost query: " + getBoostQueryClass() + "\n");
		sb.append(" * Score: \n");
		sb.append("  * trackDocScores: " + isTrackDocScores() + " \n");
		sb.append("  * trackMaxScore: " + isTrackMaxScore() + " \n");
		sb.append("  * docsScoredInOrder: " + isDocsScoredInOrder() + " \n");
		return sb.toString();
	}

	private void setTrackDocScores(boolean trackDocScore) {
		this.trackDocScores = trackDocScore;
	}

	/**
	 * @see TopFieldCollector#create(org.apache.lucene.search.Sort, int,
	 *      boolean, boolean, boolean, boolean)
	 * 
	 * @return whether document scores should be tracked and set on the results.
	 */
	public boolean isTrackDocScores() {
		return trackDocScores;
	}

	private void setTrackMaxScore(boolean trackMaxScore) {
		this.trackMaxScore = trackMaxScore;
	}

	/**
	 * @see TopFieldCollector#create(org.apache.lucene.search.Sort, int,
	 *      boolean, boolean, boolean, boolean)
	 * 
	 * @return whether the query's maxScore should be tracked and set on the
	 *         resulting TopDocs
	 */
	public boolean isTrackMaxScore() {
		return trackMaxScore;
	}

	private void setDocsScoredInOrder(boolean docsScoredInOrder) {
		this.docsScoredInOrder = docsScoredInOrder;
	}

	/**
	 * @see TopFieldCollector#create(org.apache.lucene.search.Sort, int,
	 *      boolean, boolean, boolean, boolean)
	 * 
	 * @return whether documents are scored in doc Id order or not by the given
	 *         Scorer
	 */
	public boolean isDocsScoredInOrder() {
		return docsScoredInOrder;
	}
}
