/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.tn5250j.core.tn5250jpart;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import biz.isphere.tn5250j.core.Messages;
import biz.isphere.tn5250j.core.TN5250JCorePlugin;

public class TN5250JPart {

    public static int CLOSE_PART_YES = 0;
    public static int CLOSE_PART_NO = 1;
    private IWorkbenchPart workbenchPart;
    private IToolBarManager toolbarManager;
    private ITN5250JPart tn5250jPart;
    private boolean multiSession;
    private CTabFolder tabFolderSessions;
    private ArrayList<TN5250JPanel> arrayListTN5250JPanel;
    private Action actionBindingServiceOn;
    private Action actionBindingServiceOff;
    private Action actionAddSession;
    private Action actionRemoveSession;

    private IPartListener2 partListener2 = new IPartListener2() {

        public void partActivated(IWorkbenchPartReference partReference) {
            if (partReference.getPart(true) == tn5250jPart) {
                HandleBindingService.getInstance().restoreKeyFilterEnablement();
            }
        }

        public void partDeactivated(IWorkbenchPartReference partReference) {
            if (partReference.getPart(true) == tn5250jPart) {
                HandleBindingService.getInstance().enableEclipseKeyFilter();
            }
        }

        public void partBroughtToTop(IWorkbenchPartReference partReference) {
        }

        public void partClosed(IWorkbenchPartReference partReference) {
        }

        public void partHidden(IWorkbenchPartReference partReference) {
        }

        public void partInputChanged(IWorkbenchPartReference partReference) {
        }

        public void partOpened(IWorkbenchPartReference partReference) {
        }

        public void partVisible(IWorkbenchPartReference partReference) {
        }

    };

    public TN5250JPart(IWorkbenchPart workbenchPart, IToolBarManager toolbarManager, ITN5250JPart tn5250jPart, boolean multiSession) {
        this.workbenchPart = workbenchPart;
        this.toolbarManager = toolbarManager;
        this.tn5250jPart = tn5250jPart;
        this.multiSession = multiSession;
    }

    public void createPartControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        tabFolderSessions = new CTabFolder(container, SWT.CLOSE);
        tabFolderSessions.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {

                CTabItem closedTab = (CTabItem)event.item;

                ArrayList arrayListTabItemTN5250J = (ArrayList)closedTab.getData("TabItemTN5250J");

                boolean signOn = false;
                for (int idx = 0; idx < arrayListTabItemTN5250J.size(); idx++) {
                    TN5250JPanel tn5250jPanel = (TN5250JPanel)arrayListTabItemTN5250J.get(idx);
                    if (!tn5250jPanel.getSession5250().getGUI().isOnSignOnScreen()) {
                        signOn = true;
                        break;
                    }
                }

                if (signOn) {

                    MessageDialog dialog = null;

                    if (arrayListTabItemTN5250J.size() == 1) {
                        dialog = new MessageDialog(workbenchPart.getSite().getShell(), Messages.Close_session, null,
                            Messages.The_session_is_signed_on_Do_you_really_want_to_close_the_session, MessageDialog.QUESTION, new String[] {
                                IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
                    } else {
                        dialog = new MessageDialog(workbenchPart.getSite().getShell(), Messages.Close_sessions, null,
                            Messages.There_s_at_least_one_signed_on_session_Do_you_really_want_to_close_the_sessions, MessageDialog.QUESTION,
                            new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);

                    }

                    final int dialogResult = dialog.open();

                    if (dialogResult == 0) {
                        RemoveSession.run(closedTab, tn5250jPart);
                    } else {
                        event.doit = false;
                    }

                }

            }
        });
        tabFolderSessions.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                SetSessionFocus.run(tabFolderSessions.getSelectionIndex(), -1, tn5250jPart);

            }
        });

        arrayListTN5250JPanel = new ArrayList<TN5250JPanel>();

        workbenchPart.getSite().getPage().addPartListener(partListener2);

        HandleBindingService.getInstance().addTN5250JPart(tn5250jPart);

        createActions();
        initializeToolBar();
        initializeMenu();

        if (multiSession) {
            actionAddSession.setEnabled(false);
            actionRemoveSession.setEnabled(false);
        }

    }

    private void createActions() {

        actionBindingServiceOn = new Action("") {
            @Override
            public void run() {
                HandleBindingService.getInstance().setKeyFilterEnabled(true);
            }
        };
        actionBindingServiceOn.setToolTipText(Messages.Binding_service_on);
        actionBindingServiceOn.setImageDescriptor(TN5250JCorePlugin.getImageDescriptor(TN5250JCorePlugin.IMAGE_ON));

        actionBindingServiceOff = new Action("") {
            @Override
            public void run() {
                HandleBindingService.getInstance().setKeyFilterEnabled(false);
            }
        };
        actionBindingServiceOff.setToolTipText(Messages.Binding_service_off);
        actionBindingServiceOff.setImageDescriptor(TN5250JCorePlugin.getImageDescriptor(TN5250JCorePlugin.IMAGE_OFF));

        if (multiSession) {
            actionAddSession = new Action("") {
                @Override
                @SuppressWarnings("unchecked")
                public void run() {
                    AddMultiSession.run(tn5250jPart);
                }
            };
            actionAddSession.setToolTipText(Messages.Add_session);
            actionAddSession.setImageDescriptor(TN5250JCorePlugin.getImageDescriptor(TN5250JCorePlugin.IMAGE_PLUS));

            actionRemoveSession = new Action("") {
                @Override
                @SuppressWarnings("unchecked")
                public void run() {
                    CTabItem tabItemSession = tabFolderSessions.getSelection();
                    int sessionToDelete = ((Integer)tabItemSession.getData("LastFocus")).intValue();
                    ArrayList<TN5250JPanel> arrayListTabItemTN5250J = (ArrayList)tabItemSession.getData("TabItemTN5250J");
                    ArrayList<Composite> arrayListCompositeSession = (ArrayList)tabItemSession.getData("CompositeSession");
                    int numberOfSessions = arrayListCompositeSession.size();
                    if (numberOfSessions > 1) {
                        TN5250JPanel tn5250jPanel = arrayListTabItemTN5250J.get(sessionToDelete);
                        if (!tn5250jPanel.getSession5250().getGUI().isOnSignOnScreen()) {
                            MessageDialog dialog = new MessageDialog(workbenchPart.getSite().getShell(), Messages.Close_session, null,
                                Messages.The_session_is_signed_on_Do_you_really_want_to_close_the_session, MessageDialog.QUESTION, new String[] {
                                    IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
                            final int dialogResult = dialog.open();
                            if (dialogResult == 1) {
                                return;
                            }
                        }
                    }
                    RemoveMultiSession.run(tn5250jPart);
                }
            };
            actionRemoveSession.setToolTipText(Messages.Remove_session);
            actionRemoveSession.setImageDescriptor(TN5250JCorePlugin.getImageDescriptor(TN5250JCorePlugin.IMAGE_MINUS));
        }

    }

    private void initializeToolBar() {

        toolbarManager.add(actionBindingServiceOn);
        toolbarManager.add(actionBindingServiceOff);
        if (multiSession) {
            toolbarManager.add(actionAddSession);
            toolbarManager.add(actionRemoveSession);
        }

    }

    private void initializeMenu() {
    }

    public void dispose() {
        for (int idx = 0; idx < arrayListTN5250JPanel.size(); idx++) {
            TN5250JPanel tn5250JPanel = (arrayListTN5250JPanel.get(idx));
            tn5250JPanel.removeScreenListener();
            tn5250JPanel.getSession5250().disconnect();
        }
        workbenchPart.getSite().getPage().removePartListener(partListener2);
        HandleBindingService.getInstance().removeTN5250JPart(tn5250jPart);
    }

    public CTabFolder getTabFolderSessions() {
        return tabFolderSessions;
    }

    public void addTN5250JPanel(TN5250JPanel tn5250jPanel) {
        arrayListTN5250JPanel.add(tn5250jPanel);
    }

    public void removeTN5250JPanel(TN5250JPanel tn5250jPanel) {
        arrayListTN5250JPanel.remove(tn5250jPanel);
    }

    public void setAddSession(boolean value) {
        actionAddSession.setEnabled(value);
    }

    public void setRemoveSession(boolean value) {
        actionRemoveSession.setEnabled(value);
    }

    public void setBindingService(boolean value) {
        actionBindingServiceOn.setEnabled(value);
        actionBindingServiceOff.setEnabled(!value);
    }

    public int closePart() {

        boolean signOn = false;
        for (int idx = 0; idx < arrayListTN5250JPanel.size(); idx++) {
            TN5250JPanel tn5250jPanel = (arrayListTN5250JPanel.get(idx));
            if (!tn5250jPanel.getSession5250().getGUI().isOnSignOnScreen()) {
                signOn = true;
                break;
            }
        }

        if (signOn) {

            MessageDialog dialog = null;

            if (arrayListTN5250JPanel.size() == 1) {
                dialog = new MessageDialog(workbenchPart.getSite().getShell(), Messages.Close_session, null,
                    Messages.The_session_is_signed_on_Do_you_really_want_to_close_the_session, MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
            } else {
                dialog = new MessageDialog(workbenchPart.getSite().getShell(), Messages.Close_sessions, null,
                    Messages.There_s_at_least_one_signed_on_session_Do_you_really_want_to_close_the_sessions, MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
            }

            final int dialogResult = dialog.open();

            if (dialogResult == 0) {
                return CLOSE_PART_YES;
            } else {
                return CLOSE_PART_NO;
            }

        } else {

            return CLOSE_PART_YES;

        }

    }
}
