package com.aitrus97.decideomatic;

import java.io.Serializable;

public class Choice implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String value;
	private boolean elminated;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isElminated() {
		return elminated;
	}
	public void setElminated(boolean elminated) {
		this.elminated = elminated;
	}
	
}
