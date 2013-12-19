package org.fao.geonet.bean.statistic;

import java.io.Serializable;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.statistic.LuceneQueryParamType;

/**
 * Entity representing the search parameters of a request. Related to
 * {@link SearchRequest}.
 * 
 * @author Jesse
 */
public class SearchRequestParam implements Serializable {
	private static final long serialVersionUID = 7559077566870546729L;
	private int _id;
	private LuceneQueryParamType _queryType;
	private String _termField;
	private String _termText;
	private double _similarity;
	private String _lowerText;
	private String _upperText;
	private char _inclusive = Constants.YN_FALSE;
	private SearchRequest _request;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public LuceneQueryParamType get_queryType() {
		return _queryType;
	}

	public void set_queryType(LuceneQueryParamType _queryType) {
		this._queryType = _queryType;
	}

	public String get_termField() {
		return _termField;
	}

	public void set_termField(String _termField) {
		this._termField = _termField;
	}

	public String get_termText() {
		return _termText;
	}

	public void set_termText(String _termText) {
		this._termText = _termText;
	}

	public double get_similarity() {
		return _similarity;
	}

	public void set_similarity(double _similarity) {
		this._similarity = _similarity;
	}

	public String get_lowerText() {
		return _lowerText;
	}

	public void set_lowerText(String _lowerText) {
		this._lowerText = _lowerText;
	}

	public String get_upperText() {
		return _upperText;
	}

	public void set_upperText(String _upperText) {
		this._upperText = _upperText;
	}

	public char get_inclusive() {
		return _inclusive;
	}

	public void set_inclusive(char _inclusive) {
		this._inclusive = _inclusive;
	}

	public SearchRequest get_request() {
		return _request;
	}

	public void set_request(SearchRequest _request) {
		this._request = _request;
	}
}
