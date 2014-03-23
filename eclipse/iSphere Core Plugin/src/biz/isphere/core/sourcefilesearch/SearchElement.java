/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.sourcefilesearch;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import biz.isphere.core.ISpherePlugin;


public class SearchElement {

	private String library;
	private String file;
	private String member;
	private String description;
	private Object data;

	public SearchElement() {
		library = "";
		file = "";
		member = "";
		description = "";
		data = null;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static void setSearchElements(Connection jdbcConnection, int handle, ArrayList<SearchElement> _searchElements) {
		
		String _separator;
		try {
			_separator = jdbcConnection.getMetaData().getCatalogSeparator();
		} 
		catch (SQLException e) {
			_separator = ".";
			e.printStackTrace();
		}
		
		if (_searchElements.size() > 0) {
			
			int _start;
			int _end;
			int _elements = 500;
			
			_start = 1;
			
			do {
				
				_end = _start + _elements - 1;
				
				if (_end > _searchElements.size()) {
					_end = _searchElements.size();
				}

				StringBuffer sqlInsert = new StringBuffer();
				sqlInsert.append("INSERT INTO " + ISpherePlugin.getISphereLibrary() + _separator + "FNDSTRI (XIHDL, XILIB, XIFILE, XIMBR) VALUES");
				boolean first = true;
				
				for (int idx = _start - 1; idx <= _end - 1; idx++) {
				
					if (first) {
						first = false;
						sqlInsert.append(" ");
					}
					else {
						sqlInsert.append(", ");
					}
					
					sqlInsert.append(
							"('" + Integer.toString(handle) + "', " +
							"'" + _searchElements.get(idx).getLibrary() + "', " +
							"'" + _searchElements.get(idx).getFile() + "', " +
							"'" + _searchElements.get(idx).getMember() + "')");
					
				}
				
				String _sqlInsert = sqlInsert.toString();
					
				Statement statementInsert = null;
				
				try {
					statementInsert = jdbcConnection.createStatement();
					statementInsert.executeUpdate(_sqlInsert);
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
				
				if (statementInsert != null) {
					try {
						statementInsert.close();
					} 
					catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				_start = _start + _elements;
				
			}
			while (_end < _searchElements.size());
			
		}
		
	}
	
}
