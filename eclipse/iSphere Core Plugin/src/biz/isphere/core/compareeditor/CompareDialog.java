/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.compareeditor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Version;

import biz.isphere.base.jface.dialogs.XDialog;
import biz.isphere.base.versioncheck.PluginCheck;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.internal.Member;
import biz.isphere.core.preferences.DoNotAskMeAgain;
import biz.isphere.core.preferences.DoNotAskMeAgainDialog;
import biz.isphere.core.swt.widgets.WidgetFactory;

public abstract class CompareDialog extends XDialog {

    // Must match the qualified name of class RSECompareDialog.
    public static final String DIALOG_SETTINGS = "biz.isphere.rse.compareeditor.RSECompareDialog"; //$NON-NLS-1$

    protected static final String SPECIAL_MEMBER_NAME_LEFT = "*LEFT"; //$NON-NLS-1$
    protected static final String SPECIAL_MEMBER_NAME_SELECTED = "*SELECTED"; //$NON-NLS-1$

    private static final String EDITABLE_PROPERTY = "EDITABLE_PROPERTY"; //$NON-NLS-1$
    public static final String CONSIDER_DATE_PROPERTY = "CONSIDER_DATE_PROPERTY"; //$NON-NLS-1$
    private static final String IGNORE_CASE_PROPERTY = "IGNORE_CASE_PROPERTY"; //$NON-NLS-1$

    private boolean selectEditable;
    private Member leftMember;
    private Member rightMember;
    private Member ancestorMember;
    private Button editButton;
    private Button browseButton;
    private Button dontIgnoreCaseButton;
    private Button ignoreCaseButton;
    private Button dontConsiderDateButton;
    private Button considerDateButton;
    private Button twoWayButton;
    private Button threeWayButton;
    private Button okButton;
    private boolean editable;
    private boolean considerDate;
    private boolean ignoreCase;
    private boolean threeWay;
    private boolean hasLeftMember;
    private boolean hasRightMember;
    private boolean hasMultipleRightMembers;
    private boolean hasAncestorMember;
    private Image switchImage;
    private Text leftConnectionText;
    private Text leftLibraryText;
    private Text leftFileText;
    private Text leftMemberText;
    private Text rightConnectionText;
    private Text rightLibraryText;
    private Text rightFileText;
    private Text rightMemberText;

    /**
     * Creates the compare dialog, for 0 selected members.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option
     *        "Open for browse/edit" is displayed
     */
    public CompareDialog(Shell parentShell, boolean selectEditable) {
        super(parentShell);
        initialize(parentShell, selectEditable, null, null, null);
        hasMultipleRightMembers = false;

        checkCompareFilters();
    }

    /**
     * Creates the compare dialog, for 1 selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option
     *        "Open for browse/edit" is displayed
     * @param leftMember - the left selected member
     */
    public CompareDialog(Shell parentShell, boolean selectEditable, Member leftMember) {
        super(parentShell);
        initialize(parentShell, selectEditable, leftMember, null, null);
        hasMultipleRightMembers = false;

        checkCompareFilters();
    }

    /**
     * Creates the compare dialog, for 2 selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option
     *        "Open for browse/edit" is displayed
     * @param leftMember - the left selected member
     * @param rightMember - the right selected member
     */
    public CompareDialog(Shell parentShell, boolean selectEditable, Member leftMember, Member rightMember) {
        super(parentShell);
        initialize(parentShell, selectEditable, leftMember, rightMember, null);
        hasMultipleRightMembers = false;

        checkCompareFilters();
    }

    /**
     * Creates the compare dialog, for 3 and more selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option
     *        "Open for browse/edit" is displayed
     * @param selectedMembers - the selected members that go to the right side
     *        of the compare dialog
     */
    public CompareDialog(Shell parentShell, boolean selectEditable, Member[] selectedMembers) {
        super(parentShell);
        initialize(parentShell, selectEditable, selectedMembers[0], selectedMembers[0], null);
        hasMultipleRightMembers = true;

        checkCompareFilters();
    }

    /**
     * Creates a three-way compare dialog.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option
     *        "Open for browse/edit" is displayed
     * @param leftMember - the left selected member
     * @param rightMember - the right selected member
     * @param ancestorMember - the ancestor member
     */
    public CompareDialog(Shell parentShell, boolean selectEditable, Member leftMember, Member rightMember, Member ancestorMember) {
        super(parentShell);
        initialize(parentShell, selectEditable, leftMember, rightMember, ancestorMember);
        hasMultipleRightMembers = false;

        checkCompareFilters();
    }

    private void initialize(Shell parentShell, boolean selectEditable, Member leftMember, Member rightMember, Member ancestorMember) {
        this.selectEditable = selectEditable;
        this.leftMember = leftMember;
        this.rightMember = rightMember;
        this.ancestorMember = ancestorMember;

        loadScreenValues();

        if (this.leftMember == null) {
            hasLeftMember = false;
        } else {
            hasLeftMember = true;
        }

        if (this.rightMember == null) {
            hasRightMember = false;
        } else {
            hasRightMember = true;
        }

        if (this.ancestorMember == null) {
            hasAncestorMember = false;
            threeWay = false;
        } else {
            hasAncestorMember = true;
            threeWay = true;
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite rtnGroup = (Composite)super.createDialogArea(parent);
        parent.getShell().setText(Messages.Compare_Source_Members);

        GridLayout rtnLayout = new GridLayout();
        rtnLayout.numColumns = 1;
        rtnGroup.setLayout(rtnLayout);
        rtnGroup.setLayoutData(getGridData());

        Group modeGroup = new Group(rtnGroup, SWT.NONE);
        modeGroup.setText(Messages.Mode);
        GridLayout modeLayout = new GridLayout();
        modeLayout.numColumns = 1;
        modeGroup.setLayout(modeLayout);
        modeGroup.setLayoutData(getGridData());

        /*
         * ---------------- Create options area ----------------
         */
        if (selectEditable) {

            Composite editableGroup = new Composite(modeGroup, SWT.NONE);
            GridLayout editableLayout = new GridLayout(2, true);
            editableGroup.setLayout(editableLayout);
            editableGroup.setLayoutData(getGridData());

            browseButton = WidgetFactory.createRadioButton(editableGroup);
            browseButton.setText(Messages.Open_for_browse);
            browseButton.setLayoutData(getGridData());
            if (!editable || ignoreCase) {
                browseButton.setSelection(true);
            }

            editButton = WidgetFactory.createRadioButton(editableGroup);
            editButton.setText(Messages.Open_for_edit);
            editButton.setLayoutData(getGridData());
            editButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (editButton.getSelection()) {
                        dontIgnoreCaseButton.setSelection(true);
                        ignoreCaseButton.setSelection(false);
                    }
                }
            });
            if (editable && !ignoreCase) {
                editButton.setSelection(true);
            }

        }

        Composite considerDateGroup = new Composite(modeGroup, SWT.NONE);
        GridLayout considerDateLayout = new GridLayout(2, true);
        considerDateGroup.setLayout(considerDateLayout);
        considerDateGroup.setLayoutData(getGridData());

        dontConsiderDateButton = WidgetFactory.createRadioButton(considerDateGroup);
        dontConsiderDateButton.setText(Messages.Don_t_consider_date);
        dontConsiderDateButton.setLayoutData(getGridData());
        if (!considerDate) {
            dontConsiderDateButton.setSelection(true);
        }

        considerDateButton = WidgetFactory.createRadioButton(considerDateGroup);
        considerDateButton.setText(Messages.Consider_date);
        considerDateButton.setLayoutData(getGridData());
        if (considerDate) {
            considerDateButton.setSelection(true);
        }

        Composite ignoreCaseGroup = new Composite(modeGroup, SWT.NONE);
        GridLayout ignoreCaseLayout = new GridLayout(2, true);
        ignoreCaseGroup.setLayout(ignoreCaseLayout);
        ignoreCaseGroup.setLayoutData(getGridData());

        dontIgnoreCaseButton = WidgetFactory.createRadioButton(ignoreCaseGroup);
        dontIgnoreCaseButton.setText(Messages.Don_t_ignore_case);
        dontIgnoreCaseButton.setLayoutData(getGridData());
        if (!ignoreCase) {
            dontIgnoreCaseButton.setSelection(true);
        }

        ignoreCaseButton = WidgetFactory.createRadioButton(ignoreCaseGroup);
        ignoreCaseButton.setText(Messages.Ignore_case);
        ignoreCaseButton.setLayoutData(getGridData());
        ignoreCaseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (selectEditable) {
                    if (ignoreCaseButton.getSelection()) {
                        browseButton.setSelection(true);
                        editButton.setSelection(false);
                    }
                }
            }
        });
        if (ignoreCase) {
            ignoreCaseButton.setSelection(true);
        }

        if (!hasRightMember) {

            Composite threeWayGroup = new Composite(modeGroup, SWT.NONE);
            GridLayout threeWayLayout = new GridLayout(2, true);
            threeWayGroup.setLayout(threeWayLayout);
            threeWayGroup.setLayoutData(getGridData());

            twoWayButton = WidgetFactory.createRadioButton(threeWayGroup);
            twoWayButton.setText(Messages.Two_way_compare);
            twoWayButton.setLayoutData(getGridData());
            if (!threeWay) {
                twoWayButton.setSelection(true);
            }
            twoWayButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    threeWay = false;
                    setAncestorVisible(false);
                    okButton.setEnabled(canFinish());
                }
            });

            threeWayButton = WidgetFactory.createRadioButton(threeWayGroup);
            threeWayButton.setText(Messages.Three_way_compare);
            threeWayButton.setLayoutData(getGridData());
            if (threeWay) {
                threeWayButton.setSelection(true);
            }
            threeWayButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    threeWay = true;
                    setAncestorVisible(true);
                    okButton.setEnabled(canFinish());
                }
            });

        }

        /*
         * ---------------- Create left area ----------------
         */
        if (hasLeftMember) {
            createReadOnlyLeftArea(rtnGroup);
        } else {
            createEditableLeftArea(rtnGroup);
        }

        /*
         * -------- Create right and ancestor areas --------
         */
        if (hasRightMember) {
            if (!hasMultipleRightMembers) {
                createSwitchMemberButton(rtnGroup);
                createReadOnlyRightArea(rtnGroup);
            } else {
                createEditableRightArea(rtnGroup);
            }
        } else {
            createEditableRightArea(rtnGroup);
            createEditableAncestorArea(rtnGroup);
        }

        if (!hasRightMember) {
            if (!threeWay) {
                setAncestorVisible(false);
            } else {
                setAncestorVisible(true);
            }
        }

        setFocus();

        return rtnGroup;
    }

    protected GridData getGridData() {
        return new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
    }

    private Image getSwitchImage() {
        if (switchImage == null) {
            switchImage = ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_SWITCH_MEMBER);
        }
        return switchImage;
    }

    protected void switchLeftAndRightMember(Member leftMember, Member rightMember) {
        Member tempMember = leftMember;
        this.leftMember = rightMember;

        leftConnectionText.setText(this.leftMember.getConnection());
        leftLibraryText.setText(this.leftMember.getLibrary());
        leftFileText.setText(this.leftMember.getSourceFile());
        leftMemberText.setText(this.leftMember.getMember());

        this.rightMember = tempMember;

        rightConnectionText.setText(this.rightMember.getConnection());
        rightLibraryText.setText(this.rightMember.getLibrary());
        rightFileText.setText(this.rightMember.getSourceFile());
        rightMemberText.setText(this.rightMember.getMember());
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
        if (id == OK) {
            okButton = button;
            if (hasRightMember && !hasMultipleRightMembers) {
                okButton.setEnabled(true);
            } else {
                okButton.setEnabled(false);
            }
        }
        return button;
    }

    @Override
    protected void okPressed() {
        if (selectEditable) {
            editable = editButton.getSelection();
        } else {
            editable = false;
        }
        considerDate = considerDateButton.getSelection();
        ignoreCase = ignoreCaseButton.getSelection();
        if (!hasRightMember) {
            threeWay = threeWayButton.getSelection();
        }

        storeScreenValues();

        // Close screen
        super.okPressed();
    }

    protected Button getOkButton() {
        return okButton;
    }

    protected void createEditableLeftArea(Composite parent) {
    }

    protected void createEditableRightArea(Composite parent) {
    }

    protected void createEditableAncestorArea(Composite parent) {
    }

    protected void setAncestorVisible(boolean visible) {
    }

    private void createReadOnlyLeftArea(Composite parent) {

        Group leftGroup = new Group(parent, SWT.NONE);
        leftGroup.setText(Messages.Left);
        GridLayout leftLayout = new GridLayout(2, false);
        leftGroup.setLayout(leftLayout);
        leftGroup.setLayoutData(getGridData());

        Label leftConnectionLabel = new Label(leftGroup, SWT.NONE);
        leftConnectionLabel.setText(Messages.Connection_colon);

        leftConnectionText = WidgetFactory.createReadOnlyText(leftGroup);
        leftConnectionText.setLayoutData(getGridData());
        leftConnectionText.setText(leftMember.getConnection());

        Label leftLibraryLabel = new Label(leftGroup, SWT.NONE);
        leftLibraryLabel.setText(Messages.Library_colon);
        leftLibraryText = WidgetFactory.createReadOnlyText(leftGroup);
        leftLibraryText.setLayoutData(getGridData());
        if (leftMember.isArchive()) {
            leftLibraryText.setText(leftMember.getArchiveLibrary());
        } else {
            leftLibraryText.setText(leftMember.getLibrary());
        }

        Label leftFileLabel = new Label(leftGroup, SWT.NONE);
        leftFileLabel.setText(Messages.File_colon);
        leftFileText = WidgetFactory.createReadOnlyText(leftGroup);
        leftFileText.setLayoutData(getGridData());
        if (leftMember.isArchive()) {
            leftFileText.setText(leftMember.getArchiveFile());
        } else {
            leftFileText.setText(leftMember.getSourceFile());
        }

        Label leftMemberLabel = new Label(leftGroup, SWT.NONE);
        leftMemberLabel.setText(Messages.Member_colon);
        leftMemberText = WidgetFactory.createReadOnlyText(leftGroup);
        leftMemberText.setLayoutData(getGridData());

        if (hasMultipleRightMembers()) {
            leftMemberText.setText(SPECIAL_MEMBER_NAME_SELECTED);
        } else {
            if (leftMember.isArchive()) {
                leftMemberText.setText(leftMember.getArchiveMember());
            } else {
                leftMemberText.setText(leftMember.getMember());
            }
        }

        if (leftMember.isArchive()) {
            Label leftTimeLabel = new Label(leftGroup, SWT.NONE);
            leftTimeLabel.setText(Messages.Archive_colon);
            Text leftTimeText = WidgetFactory.createReadOnlyText(leftGroup);
            leftTimeText.setLayoutData(getGridData());
            leftTimeText.setText(leftMember.getArchiveDate() + " - " + leftMember.getArchiveTime()); //$NON-NLS-1$
        }
    }

    private void createReadOnlyRightArea(Composite parent) {

        Group rightGroup = new Group(parent, SWT.NONE);
        rightGroup.setText(Messages.Right);
        GridLayout rightLayout = new GridLayout(2, false);
        rightGroup.setLayout(rightLayout);
        rightGroup.setLayoutData(getGridData());

        Label rightConnectionLabel = new Label(rightGroup, SWT.NONE);
        rightConnectionLabel.setText(Messages.Connection_colon);

        rightConnectionText = WidgetFactory.createReadOnlyText(rightGroup);
        rightConnectionText.setLayoutData(getGridData());
        rightConnectionText.setText(rightMember.getConnection());

        Label rightLibraryLabel = new Label(rightGroup, SWT.NONE);
        rightLibraryLabel.setText(Messages.Library_colon);
        rightLibraryText = WidgetFactory.createReadOnlyText(rightGroup);
        rightLibraryText.setLayoutData(getGridData());

        if (rightMember.isArchive()) {
            rightLibraryText.setText(rightMember.getArchiveLibrary());
        } else {
            rightLibraryText.setText(rightMember.getLibrary());
        }

        Label rightFileLabel = new Label(rightGroup, SWT.NONE);
        rightFileLabel.setText(Messages.File_colon);
        rightFileText = WidgetFactory.createReadOnlyText(rightGroup);
        rightFileText.setLayoutData(getGridData());
        if (rightMember.isArchive()) {
            rightFileText.setText(rightMember.getArchiveFile());
        } else {
            rightFileText.setText(rightMember.getSourceFile());
        }

        Label rightMemberLabel = new Label(rightGroup, SWT.NONE);
        rightMemberLabel.setText(Messages.Member_colon);
        rightMemberText = WidgetFactory.createReadOnlyText(rightGroup);
        rightMemberText.setLayoutData(getGridData());

        if (rightMember.isArchive()) {
            rightMemberText.setText(rightMember.getArchiveMember());
        } else {
            rightMemberText.setText(rightMember.getMember());
        }

        if (rightMember.isArchive()) {
            Label rightTimeLabel = new Label(rightGroup, SWT.NONE);
            rightTimeLabel.setText(Messages.Archive_colon);
            Text rightTimeText = WidgetFactory.createReadOnlyText(rightGroup);
            rightTimeText.setLayoutData(getGridData());
            rightTimeText.setText(rightMember.getArchiveDate() + " - " + rightMember.getArchiveTime()); //$NON-NLS-1$
        }
    }

    private void createSwitchMemberButton(Composite parent) {

        Composite switchPanel = new Composite(parent, SWT.NONE);
        GridLayout middleLayout = new GridLayout();
        middleLayout.numColumns = 1;
        switchPanel.setLayout(middleLayout);
        switchPanel.setLayoutData(getGridData());

        Button switchMemberButton = WidgetFactory.createPushButton(switchPanel);
        switchMemberButton.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
        switchMemberButton.setImage(getSwitchImage());
        switchMemberButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                switchLeftAndRightMember(leftMember, rightMember);
            }
        });
    }

    protected boolean canFinish() {
        return true;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isConsiderDate() {
        return considerDate;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isThreeWay() {
        return threeWay;
    }

    protected boolean hasLeftMember() {
        return hasLeftMember;
    }

    protected boolean hasRightMember() {
        return hasRightMember;
    }

    protected boolean hasMultipleRightMembers() {
        return hasMultipleRightMembers;
    }

    protected boolean hasAncestorMember() {
        return hasAncestorMember;
    }

    protected void loadScreenValues() {
        if (selectEditable) {
            editable = getDialogBoundsSettings().getBoolean(EDITABLE_PROPERTY);
        } else {
            editable = false;
        }
        considerDate = getDialogBoundsSettings().getBoolean(CONSIDER_DATE_PROPERTY);
        ignoreCase = getDialogBoundsSettings().getBoolean(IGNORE_CASE_PROPERTY);
    }

    protected void storeScreenValues() {
        if (selectEditable) {
            getDialogBoundsSettings().put(EDITABLE_PROPERTY, editable);
        }
        getDialogBoundsSettings().put(CONSIDER_DATE_PROPERTY, considerDate);
        getDialogBoundsSettings().put(IGNORE_CASE_PROPERTY, ignoreCase);
    }

    private void checkCompareFilters() {

        Version platformVersion = PluginCheck.getVersion("org.eclipse.platform"); //$NON-NLS-1$
        if (platformVersion.compareTo(new Version("4.4.0")) >= 0) { //$NON-NLS-1$
            if (!PluginCheck.hasPlugin("biz.isphere.comparefilters")) { //$NON-NLS-1$
                UIJob job = new UIJob("") {
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        DoNotAskMeAgainDialog.openWarning(getShell(), DoNotAskMeAgain.WARNING_COMPARE_FILTERS_NOT_INSTALLED,
                            Messages.Compare_Filters_not_installed_message);
                        return Status.OK_STATUS;
                    }
                };
                job.schedule();
            }
        }
    }

    /**
     * Overridden to let {@link XDialog} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return super.getDialogBoundsSettings(ISpherePlugin.getDefault().getDialogSettings());
    }

}
