/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.bindingdirectoryeditor;

import java.sql.Connection;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.internal.DialogActionTypes;
import biz.isphere.core.internal.Size;

import com.ibm.as400.access.AS400;

public class BindingDirectoryEntryViewer {

	private String level;
	private AS400 as400;
	private	Connection jdbcConnection;
	private String connection;
	private String library;
	private String bindingDirectory;
	private String mode;
	private TableViewer _tableViewer;
	private Table _table;
	private Object[] selectedItems;
	private Shell shell;
	private ArrayList<BindingDirectoryEntry> _bindingDirectoryEntries = new ArrayList<BindingDirectoryEntry>();
	private Button buttonUp;
	private Button buttonDown;
		
	private class LabelProviderTableViewer extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			BindingDirectoryEntry bindingDirectoryEntry = (BindingDirectoryEntry)element;
			if (columnIndex == 0) {
				return bindingDirectoryEntry.getLibrary();
			}
			else if (columnIndex == 1) {
				return bindingDirectoryEntry.getObject();
			} 
			else if (columnIndex == 2) {
				return bindingDirectoryEntry.getObjectType();
			}
			else if (level.compareTo("V6R1M0") >= 0 && columnIndex == 3) {
				return bindingDirectoryEntry.getActivation();
			}
			return "*UNKNOWN";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	private class ContentProviderTableViewer implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			BindingDirectoryEntry[] bindingDirectoryEntries = new BindingDirectoryEntry[_bindingDirectoryEntries.size()];
			_bindingDirectoryEntries.toArray(bindingDirectoryEntries);
			return bindingDirectoryEntries;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	public BindingDirectoryEntryViewer(
			String level,
			AS400 as400, 
			Connection jdbcConnection,
			String connection, 
			String library, 
			String bindingDirectory, 
			String mode) {

		this.level = level;
		this.as400 = as400;
		this.jdbcConnection = jdbcConnection;
		this.connection = connection;
		this.library = library;
		this.bindingDirectory = bindingDirectory;
		this.mode = mode;
		
		_bindingDirectoryEntries = 
			BindingDirectory.getEntries(
					level,
					as400,
					jdbcConnection, 
					connection, 
					library, 
					bindingDirectory);
		
	}
		
	/**
	 * @wbp.parser.entryPoint
	 */
	public void createContents(Composite parent) {	
		
		shell = parent.getShell();
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		_tableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
		_tableViewer.setLabelProvider(new LabelProviderTableViewer());
		_tableViewer.setContentProvider(new ContentProviderTableViewer());
		_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if(_tableViewer.getSelection() instanceof IStructuredSelection) {

                    IStructuredSelection structuredSelection = (IStructuredSelection)_tableViewer.getSelection();
                    if (structuredSelection.getFirstElement() instanceof BindingDirectoryEntry) {

                        BindingDirectoryEntry bindingDirectoryEntry = (BindingDirectoryEntry)structuredSelection.getFirstElement();
                        BindingDirectoryEntryDetailDialog _bindingDirectoryEntryDetailDialog = 
                            new BindingDirectoryEntryDetailDialog(
                                    shell, 
                                    level,
                                    DialogActionTypes.CHANGE, 
                                    bindingDirectoryEntry,
                                    _bindingDirectoryEntries);
                        if (_bindingDirectoryEntryDetailDialog.open() == Dialog.OK) {
                            uploadEntries();
                            _tableViewer.refresh();
                        }
                    
                    }
                }
            }
        });
		_table = _tableViewer.getTable();	
		_table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refreshUpDown();
			}
		});
		_table.setLinesVisible(true);
		_table.setHeaderVisible(true);
		_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableColumn columnLibrary = new TableColumn(_table, SWT.NONE);
		columnLibrary.setWidth(Size.getSize(100));
		columnLibrary.setText(Messages.getString("Library"));

		final TableColumn columnObject = new TableColumn(_table, SWT.NONE);
		columnObject.setWidth(Size.getSize(100));
		columnObject.setText(Messages.getString("Object"));

		final TableColumn columnObjectType = new TableColumn(_table, SWT.NONE);
		columnObjectType.setWidth(Size.getSize(100));
		columnObjectType.setText(Messages.getString("Object_type"));
		
		if (level.compareTo("V6R1M0") >= 0) {
			final TableColumn columnActivation = new TableColumn(_table, SWT.NONE);
			columnActivation.setWidth(Size.getSize(100));
			columnActivation.setText(Messages.getString("Activation"));
		}
		
		Composite compositeUpDown = new Composite(container, SWT.NONE);
		compositeUpDown.setLayout(new GridLayout(1, false));
		compositeUpDown.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		
		buttonUp = new Button(compositeUpDown, SWT.NONE);
		buttonUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				moveUpDown(-1);
			}
		});
		buttonUp.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true, 1, 1));
		buttonUp.setText(Messages.getString("Up"));
		buttonUp.setEnabled(false);
		
		buttonDown = new Button(compositeUpDown, SWT.NONE);
		buttonDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				moveUpDown(+1);
			}
		});
		buttonDown.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true, 1, 1));
		buttonDown.setText(Messages.getString("Down"));
		buttonDown.setEnabled(false);
		
		final Menu menuTableBindingDirectoryEntries = new Menu(_table);
		menuTableBindingDirectoryEntries.addMenuListener(new MenuAdapter() {
			
			private MenuItem menuItemNew;
			private MenuItem menuItemChange;
			private MenuItem menuItemCopy;
			private MenuItem menuItemDelete;
			private MenuItem menuItemDisplay;
			private MenuItem menuSeparator;
			private MenuItem menuItemRefresh;
			
			public void menuShown(MenuEvent event) {
				retrieveSelectedTableItems();
				destroyMenuItems();
				createMenuItems();
			}	
			public void destroyMenuItems() {
				if (!((menuItemNew == null) || (menuItemNew.isDisposed()))) {
					menuItemNew.dispose();
				}
				if (!((menuItemChange == null) || (menuItemChange.isDisposed()))) {
					menuItemChange.dispose();
				}
				if (!((menuItemCopy == null) || (menuItemCopy.isDisposed()))) {
					menuItemCopy.dispose();
				}
				if (!((menuItemDelete == null) || (menuItemDelete.isDisposed()))) {
					menuItemDelete.dispose();
				}
				if (!((menuItemDisplay == null) || (menuItemDisplay.isDisposed()))) {
					menuItemDisplay.dispose();
				}
				if (!((menuSeparator == null) || (menuSeparator.isDisposed()))) {
					menuSeparator.dispose();
				}
				if (!((menuItemRefresh == null) || (menuItemRefresh.isDisposed()))) {
					menuItemRefresh.dispose();
				}
			}
			public void createMenuItems() {
				boolean isChange = false;
				boolean isCopy = false;
				boolean isDelete = false;
				boolean isDisplay = false;
				boolean isRefresh = false;
				for (int idx=0; idx < selectedItems.length; idx++) {
					if (selectedItems[idx] instanceof BindingDirectoryEntry) {
						if (mode.equals("*EDIT")) {
							isChange = true;
							isCopy = true;
							isDelete = true;
						}
						isDisplay = true;
						isRefresh = true;
					}	
				}		
				if (mode.equals("*EDIT")) {
					createMenuItemNew();	
				}
				if (isChange) createMenuItemChange();
				if (isCopy) createMenuItemCopy();
				if (isDelete) createMenuItemDelete();
				if (isDisplay) createMenuItemDisplay();

				if (isRefresh) {
					menuSeparator = new MenuItem(menuTableBindingDirectoryEntries, SWT.SEPARATOR);
					createMenuItemRefresh();
				}
			}
			public void createMenuItemNew() {
				menuItemNew = new MenuItem(menuTableBindingDirectoryEntries, SWT.NONE);
				menuItemNew.setText(Messages.getString("New"));
				menuItemNew.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_NEW));
				menuItemNew.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						
						BindingDirectoryEntry _bindingDirectoryEntry = new BindingDirectoryEntry();
						_bindingDirectoryEntry.setConnection(connection);
						BindingDirectoryEntryDetailDialog _bindingDirectoryEntryDetailDialog = 
							new BindingDirectoryEntryDetailDialog(
									shell, 
									level,
									DialogActionTypes.CREATE, 
									_bindingDirectoryEntry,
									_bindingDirectoryEntries);
						if (_bindingDirectoryEntryDetailDialog.open() == Dialog.OK) {
							_bindingDirectoryEntries.add(_bindingDirectoryEntry);
							uploadEntries();
							_tableViewer.refresh();
						}
						
						deSelectAllItems();
						
					}
				}); 	
			}
			public void createMenuItemChange() {
				menuItemChange = new MenuItem(menuTableBindingDirectoryEntries, SWT.NONE);
				menuItemChange.setText(Messages.getString("Change"));
				menuItemChange.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_CHANGE));
				menuItemChange.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						for (int idx=0; idx < selectedItems.length; idx++) {
							if (selectedItems[idx] instanceof BindingDirectoryEntry) {
								
								BindingDirectoryEntryDetailDialog _bindingDirectoryEntryDetailDialog = 
									new BindingDirectoryEntryDetailDialog(
											shell, 
											level,
											DialogActionTypes.CHANGE, 
											(BindingDirectoryEntry)selectedItems[idx],
											_bindingDirectoryEntries);
								if (_bindingDirectoryEntryDetailDialog.open() == Dialog.OK) {
									uploadEntries();
									_tableViewer.refresh();
								}
								
							}	
						}
						deSelectAllItems();
					}
				}); 
			}			
			public void createMenuItemCopy() {
				menuItemCopy = new MenuItem(menuTableBindingDirectoryEntries, SWT.NONE);
				menuItemCopy.setText(Messages.getString("Copy"));
				menuItemCopy.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_COPY));
				menuItemCopy.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						for (int idx=0; idx < selectedItems.length; idx++) {
							if (selectedItems[idx] instanceof BindingDirectoryEntry) {
								
								BindingDirectoryEntry _bindingDirectoryEntry = new BindingDirectoryEntry();
								_bindingDirectoryEntry.setConnection(((BindingDirectoryEntry)selectedItems[idx]).getConnection());
								_bindingDirectoryEntry.setLibrary(((BindingDirectoryEntry)selectedItems[idx]).getLibrary());
								_bindingDirectoryEntry.setObject(((BindingDirectoryEntry)selectedItems[idx]).getObject());
								_bindingDirectoryEntry.setObjectType(((BindingDirectoryEntry)selectedItems[idx]).getObjectType());
								if (level.compareTo("V6R1M0") >= 0) {
									_bindingDirectoryEntry.setActivation(((BindingDirectoryEntry)selectedItems[idx]).getActivation());
								}
								
								BindingDirectoryEntryDetailDialog _bindingDirectoryEntryDetailDialog = 
									new BindingDirectoryEntryDetailDialog(
											shell, 
											level,
											DialogActionTypes.COPY, 
											_bindingDirectoryEntry,
											_bindingDirectoryEntries);
								if (_bindingDirectoryEntryDetailDialog.open() == Dialog.OK) {
									_bindingDirectoryEntries.add(_bindingDirectoryEntry);
									uploadEntries();
									_tableViewer.refresh();
								}
								
							}	
						}
						deSelectAllItems();
					}
				}); 
			}			
			public void createMenuItemDelete() {
				menuItemDelete = new MenuItem(menuTableBindingDirectoryEntries, SWT.NONE);
				menuItemDelete.setText(Messages.getString("Delete"));
				menuItemDelete.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_DELETE));
				menuItemDelete.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						for (int idx=0; idx < selectedItems.length; idx++) {
							if (selectedItems[idx] instanceof BindingDirectoryEntry) {
								
								BindingDirectoryEntryDetailDialog _bindingDirectoryEntryDetailDialog = 
									new BindingDirectoryEntryDetailDialog(
											shell, 
											level,
											DialogActionTypes.DELETE, 
											(BindingDirectoryEntry)selectedItems[idx],
											_bindingDirectoryEntries);
								if (_bindingDirectoryEntryDetailDialog.open() == Dialog.OK) {
									_bindingDirectoryEntries.remove(selectedItems[idx]);
									uploadEntries();
									_tableViewer.refresh();
								}
								
							}	
						}
						deSelectAllItems();
					}
				}); 
			}
			public void createMenuItemDisplay() {
				menuItemDisplay = new MenuItem(menuTableBindingDirectoryEntries, SWT.NONE);
				menuItemDisplay.setText(Messages.getString("Display"));
				menuItemDisplay.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_DISPLAY));
				menuItemDisplay.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						for (int idx=0; idx < selectedItems.length; idx++) {
							if (selectedItems[idx] instanceof BindingDirectoryEntry) {
								
								BindingDirectoryEntryDetailDialog _bindingDirectoryEntryDetailDialog = 
									new BindingDirectoryEntryDetailDialog(
											shell,
											level,
											DialogActionTypes.DISPLAY, 
											(BindingDirectoryEntry)selectedItems[idx],
											_bindingDirectoryEntries);
								if (_bindingDirectoryEntryDetailDialog.open() == Dialog.OK) {
								}
								
							}	
						}
						deSelectAllItems();
					}
				}); 
			}
			public void createMenuItemRefresh() {
				menuItemRefresh = new MenuItem(menuTableBindingDirectoryEntries, SWT.NONE);
				menuItemRefresh.setText(Messages.getString("Refresh"));
				menuItemRefresh.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_REFRESH));
				menuItemRefresh.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {

						_bindingDirectoryEntries = 
							BindingDirectory.getEntries(
									level,
									as400,
									jdbcConnection, 
									connection, 
									library, 
									bindingDirectory);
						
						_tableViewer.refresh();
						
						deSelectAllItems();

					}
				}); 
			}
		});
		_table.setMenu(menuTableBindingDirectoryEntries);

		_tableViewer.setInput(new Object());	
	}

	private void retrieveSelectedTableItems() {
	    if(_tableViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)_tableViewer.getSelection();
			selectedItems = structuredSelection.toArray();
	    } 
	    else {
		    selectedItems = new Object[0];
	    }
	}

	private void refreshUpDown() {

		buttonUp.setEnabled(false);
		buttonDown.setEnabled(false);

		if (mode.equals("*EDIT")) {

			retrieveSelectedTableItems();
			
			if (selectedItems.length == 1) {
				if (selectedItems[0] instanceof BindingDirectoryEntry) {
					BindingDirectoryEntry bindingDirectoryEntry = (BindingDirectoryEntry)selectedItems[0];
					int position = _bindingDirectoryEntries.indexOf(bindingDirectoryEntry) + 1;
					if (position > 1) {
						buttonUp.setEnabled(true);
					}
					if (position < _bindingDirectoryEntries.size()) {
						buttonDown.setEnabled(true);
					}
				}	
			}
			
		}

	}
	
	private void moveUpDown(int offset) {

		retrieveSelectedTableItems();
		
		if (selectedItems.length == 1) {
			
			if (selectedItems[0] instanceof BindingDirectoryEntry) {
				
				BindingDirectoryEntry bindingDirectoryEntry = (BindingDirectoryEntry)selectedItems[0];
				
				int position = _bindingDirectoryEntries.indexOf(bindingDirectoryEntry);
				
				_bindingDirectoryEntries.remove(position);
				
				_bindingDirectoryEntries.add(position + offset, bindingDirectoryEntry);
				
				uploadEntries();
				
				_tableViewer.refresh();

				refreshUpDown();
				
				_table.setFocus();
				
			}
			
		}
	
	}
	
	private void deSelectAllItems() {
		
		_tableViewer.setSelection(new StructuredSelection(), true);
	    
		selectedItems = new Object[0];

	    buttonUp.setEnabled(false);
		buttonDown.setEnabled(false);
		
	}

	private void uploadEntries() {
		
		if (BindingDirectory.removeEntries(
				level,
				as400, 
				jdbcConnection, 
				connection, 
				library, 
				bindingDirectory)) {
			BindingDirectory.addEntries(
					level,
					as400, 
					jdbcConnection,
					connection, 
					library, 
					bindingDirectory, 
					_bindingDirectoryEntries);
		}
		
	}
	
}
