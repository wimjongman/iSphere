/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.bindingdirectoryeditor;

import java.util.ArrayList;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.internal.DialogActionTypes;
import biz.isphere.core.internal.Validator;

public class BindingDirectoryEntryDetail {

	private String level;
	private int actionType;
	private BindingDirectoryEntry _bindingDirectoryEntry;
	private ArrayList<BindingDirectoryEntry> _bindingDirectoryEntries;
	private CCombo comboLibrary;
	private Text textObject;
	private CCombo comboObjectType;
	private CCombo comboActivation;
	private Validator validatorLibrary;
	private Validator validatorObject;
	private Validator validatorObjectType;
	private Validator validatorActivation;
	private StatusLineManager statusLineManager;
	
	public BindingDirectoryEntryDetail(
			String level,
			int actionType, 
			BindingDirectoryEntry _bindingDirectoryEntry, 
			ArrayList<BindingDirectoryEntry> _bindingDirectoryEntries) {
		this.level = level;
		this.actionType = actionType;
		this._bindingDirectoryEntry = _bindingDirectoryEntry;
		this._bindingDirectoryEntries = _bindingDirectoryEntries;
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void createContents(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());

		// Action
		
		final Label labelAction = new Label(container, SWT.CENTER | SWT.BORDER);
		labelAction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		labelAction.setText(DialogActionTypes.getText(actionType));
		labelAction.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		if (actionType == DialogActionTypes.DELETE) {
			labelAction.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		} 
		else {
			labelAction.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));			
		}

		// Header
		
		final Composite compositeHeader = new Composite(container, SWT.NONE);
		compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayoutCompositeHeader = new GridLayout();
		gridLayoutCompositeHeader.numColumns = 2;
		compositeHeader.setLayout(gridLayoutCompositeHeader);

		// Library
		
		final Label labelLibrary = new Label(compositeHeader, SWT.NONE);
		labelLibrary.setText(Messages.getString("Library_colon"));

		comboLibrary = new CCombo(compositeHeader, SWT.BORDER);
		comboLibrary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comboLibrary.setTextLimit(10);
		comboLibrary.add("*LIBL");
		if (actionType == DialogActionTypes.CREATE) {
			comboLibrary.setText("*LIBL");
		} 
		else if (actionType == DialogActionTypes.CHANGE || actionType == DialogActionTypes.COPY || actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
			comboLibrary.setText(_bindingDirectoryEntry.getLibrary());
		}
		if (actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
			comboLibrary.setEnabled(false);
		}
		
		validatorLibrary = new Validator();
		validatorLibrary.setType("*NAME");
		validatorLibrary.setLength(10);
		validatorLibrary.setRestricted(false);
		validatorLibrary.addSpecialValue("*LIBL");

		// Object
		
		final Label labelObject = new Label(compositeHeader, SWT.NONE);
		labelObject.setText(Messages.getString("Object_colon"));

		textObject = new Text(compositeHeader, SWT.BORDER);
		textObject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textObject.setTextLimit(10);
		if (actionType == DialogActionTypes.CREATE) {
			textObject.setText("");
		} 
		else if (actionType == DialogActionTypes.CHANGE || actionType == DialogActionTypes.COPY || actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
			textObject.setText(_bindingDirectoryEntry.getObject());
		}
		if (actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
			textObject.setEnabled(false);
		}
		
		validatorObject = new Validator();
		validatorObject.setType("*NAME");
		validatorObject.setLength(10);

		// Object type
		
		final Label labelObjectType = new Label(compositeHeader, SWT.NONE);
		labelObjectType.setText(Messages.getString("Object_type_colon"));

		comboObjectType = new CCombo(compositeHeader, SWT.BORDER);
		comboObjectType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comboObjectType.setTextLimit(10);
		comboObjectType.add("*SRVPGM");
		comboObjectType.add("*MODULE");
		if (actionType == DialogActionTypes.CREATE) {
			comboObjectType.setText("");
		} 
		else if (actionType == DialogActionTypes.CHANGE || actionType == DialogActionTypes.COPY || actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
			comboObjectType.setText(_bindingDirectoryEntry.getObjectType());
		}
		if (actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
			comboObjectType.setEnabled(false);
		}
		if (level.compareTo("V6R1M0") >= 0) {
			comboObjectType.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					if (comboObjectType.getText().toUpperCase().trim().equals("*SRVPGM")) {
						comboActivation.setEnabled(true);
					}
					else {
						comboActivation.setText("");
						comboActivation.setEnabled(false);
					}
				}
			});
		}
		
		validatorObjectType = new Validator();
		validatorObjectType.setType("*CHAR");
		validatorObjectType.setLength(10);
		validatorObjectType.setRestricted(true);
		validatorObjectType.addSpecialValue("*SRVPGM");
		validatorObjectType.addSpecialValue("*MODULE");

		// Activation

		if (level.compareTo("V6R1M0") >= 0) {

			final Label labelActivation = new Label(compositeHeader, SWT.NONE);
			labelActivation.setText(Messages.getString("Activation_colon"));

			comboActivation = new CCombo(compositeHeader, SWT.BORDER);
			comboActivation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			comboActivation.setTextLimit(10);
			comboActivation.add("*IMMED");
			comboActivation.add("*DEFER");
			if (actionType == DialogActionTypes.CREATE) {
				comboActivation.setText("*IMMED");
			} 
			else if (actionType == DialogActionTypes.CHANGE || actionType == DialogActionTypes.COPY || actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
				if (comboObjectType.getText().toUpperCase().trim().equals("*SRVPGM")) {
					comboActivation.setText(_bindingDirectoryEntry.getActivation());
					comboActivation.setEnabled(true);
				}
				else {
					comboActivation.setText("");
					comboActivation.setEnabled(false);
				}
			}
			if (actionType == DialogActionTypes.DELETE || actionType == DialogActionTypes.DISPLAY) {
				comboActivation.setEnabled(false);
			}
			
			validatorActivation = new Validator();
			validatorActivation.setType("*CHAR");
			validatorActivation.setLength(10);
			validatorActivation.setRestricted(true);
			validatorActivation.addSpecialValue("*IMMED");
			validatorActivation.addSpecialValue("*DEFER");
			
		}
		
		// Status line
		
		statusLineManager = new StatusLineManager(); 
        statusLineManager.createControl(container, SWT.NONE);
        Control statusLine = statusLineManager.getControl();        
		final GridData gridDataStatusLine = new GridData(SWT.FILL, SWT.CENTER, true, false);
		statusLine.setLayoutData(gridDataStatusLine);

        // Set focus
        
		comboLibrary.setFocus();			
		
	}
	
	protected void setErrorMessage(String errorMessage) {
		if (errorMessage != null) {
			statusLineManager.setErrorMessage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_ERROR), errorMessage);
		}
		else {
			statusLineManager.setErrorMessage(null, null);
		}
	}
	
	public boolean processButtonPressed() {	
		switch (actionType) {
			case DialogActionTypes.CREATE: {
				convertData();
				if (checkData()) {
					transferData();
					return true;
				}
				return false;
			}
			case DialogActionTypes.CHANGE: {
				convertData();
				if (checkData()) {
					transferData();
					return true;
				}
				return false;
			}
			case DialogActionTypes.COPY: {
				convertData();
				if (checkData()) {
					transferData();
					return true;
				}
				return false;
			}
			case DialogActionTypes.DELETE: {
				return true;
			}
			case DialogActionTypes.DISPLAY: {
				return true;
			}
		}
		return false;
	}
	
	protected void convertData() {
		comboLibrary.setText(comboLibrary.getText().toUpperCase().trim());
		textObject.setText(textObject.getText().toUpperCase().trim());
		comboObjectType.setText(comboObjectType.getText().toUpperCase().trim());
		if (level.compareTo("V6R1M0") >= 0) {
			comboActivation.setText(comboActivation.getText().toUpperCase().trim());
		}
	}
	
	protected boolean checkData() {

		// The value in field 'Library' is not valid.
		
		if (!validatorLibrary.validate(comboLibrary.getText())) {
			setErrorMessage(Messages.getString("The_value_in_field_'Library'_is_not_valid."));
			comboLibrary.setFocus();
			return false;
		}

		// The value in field 'Object' is not valid.
		
		if (!validatorObject.validate(textObject.getText())) {
			setErrorMessage(Messages.getString("The_value_in_field_'Object'_is_not_valid."));
			textObject.setFocus();
			return false;
		}

		// The value in field 'Object type' is not valid.
		
		if (!validatorObjectType.validate(comboObjectType.getText())) {
			setErrorMessage(Messages.getString("The_value_in_field_'Object_type'_is_not_valid."));
			comboObjectType.setFocus();
			return false;
		}

		// The value in field 'Activation' is not valid.
		
		if (level.compareTo("V6R1M0") >= 0 && comboObjectType.getText().equals("*SRVPGM") && !validatorActivation.validate(comboActivation.getText())) {
			setErrorMessage(Messages.getString("The_value_in_field_'Activation'_is_not_valid."));
			comboActivation.setFocus();
			return false;
		}
		
		// The entry does already exist.
		
		if (actionType == DialogActionTypes.CREATE || 
				actionType == DialogActionTypes.COPY || 
				(actionType == DialogActionTypes.CHANGE && 
						(!_bindingDirectoryEntry.getLibrary().equals(comboLibrary.getText()) || 
						 !_bindingDirectoryEntry.getObject().equals(textObject.getText()) || 
						 !_bindingDirectoryEntry.getObjectType().equals(comboObjectType.getText())))) {
			for (int idx = 0; idx < _bindingDirectoryEntries.size(); idx++) {
				BindingDirectoryEntry entry = _bindingDirectoryEntries.get(idx);
				if (entry.getLibrary().equals(comboLibrary.getText()) &&
						entry.getObject().equals(textObject.getText()) &&
						entry.getObjectType().equals(comboObjectType.getText())) {
					setErrorMessage(Messages.getString("The_entry_does_already_exist."));
					comboLibrary.setFocus();
					return false;
				}
			}
		}
		
		// Everything is alright
		
		return true;
	}
	
	protected void transferData() {
		_bindingDirectoryEntry.setLibrary(comboLibrary.getText());
		_bindingDirectoryEntry.setObject(textObject.getText());
		_bindingDirectoryEntry.setObjectType(comboObjectType.getText());
		if (level.compareTo("V6R1M0") >= 0 && comboObjectType.getText().equals("*SRVPGM")) {
			_bindingDirectoryEntry.setActivation(comboActivation.getText());
		}
		else {
			_bindingDirectoryEntry.setActivation("");
		}
	}
}