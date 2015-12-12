/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataspaceeditor.delegates;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.wb.swt.SWTResourceManager;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.dataspaceeditor.AbstractDataSpaceEditor;
import biz.isphere.core.dataspaceeditor.StatusLine;
import biz.isphere.core.dataspaceeditor.controls.DataAreaText;
import biz.isphere.core.dataspaceeditor.events.StatusChangedEvent;
import biz.isphere.core.dataspaceeditor.events.StatusChangedListener;
import biz.isphere.core.internal.FontHelper;

/**
 * Editor delegate that edits a *CHAR data area.
 * <p>
 * The delegate implements all the specific stuff that is needed to edit a data
 * area of type *CHAR. Basically it implements these things:
 * 
 * <pre>
 * The content of the data area is displayed in lines with a fixed length
 * Cut & Paste actions had to be overridden to handle these action properly
 * The delegate uses to original Eclipse Find & Replace dialog for Find & Replace action
 * The delegate (and the DataAreaText widget) uses the FontRegistry and the ColorRegistry to paint the editor
 * </pre>
 */
public class CharacterDataAreaEditorDelegate extends AbstractDataSpaceEditorDelegate {

    private static final int OFFSET_LABEL_WIDTH_HINT = 55;

    protected static int DEFAULT_EDITOR_WIDTH = 50; // default width on 5250
                                                    // screen
    private DataAreaText dataAreaText;

    private String statusMessage;

    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;
    private Action undoAction;
    private Action redoAction;
    private Action deleteAction;
    private Action selectAllAction;
    private Action findReplaceAction;

    private Composite parent;
    private ScrolledComposite editorAreaScrollable;
    private Composite editorArea;
    private Composite rulerArea;
    private Label offsetLabel;
    private Label ruler;
    private Composite offsetArea;

    private int lastTopIndex = -1;
    private Font lastFont = null;
    private int lastNumRows = -1;

    private int currentWidth;

    public CharacterDataAreaEditorDelegate(AbstractDataSpaceEditor aDataAreaEditor) {
        super(aDataAreaEditor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(final Composite aParent) {

        parent = aParent;

        FontRegistry registry = ISpherePlugin.getDefault().getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
        registry.addListener(new ThemeChangedListener());

        editorAreaScrollable = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.NONE);
        editorAreaScrollable.setLayout(new GridLayout(1, false));
        editorAreaScrollable.setLayoutData(new GridData(GridData.FILL_BOTH));
        editorAreaScrollable.setExpandHorizontal(true);
        editorAreaScrollable.setExpandVertical(true);

        editorArea = createEditorArea(editorAreaScrollable, 1);

        rulerArea = new Composite(editorArea, SWT.NONE);
        GridLayout rulerAreaLayout = new GridLayout(2, false);
        rulerAreaLayout.marginHeight = 0;
        rulerAreaLayout.marginWidth = 0;
        rulerAreaLayout.verticalSpacing = 0;
        rulerArea.setLayout(rulerAreaLayout);
        GridData rulerAreaLayoutData = createRulerAndEditorLayoutData();
        rulerArea.setLayoutData(rulerAreaLayoutData);

        offsetLabel = createTextOffsetLabel(rulerArea, getEditorFont(), Messages.Offset);

        currentWidth = DEFAULT_EDITOR_WIDTH;

        ruler = new Label(rulerArea, SWT.NONE);
        ruler.setFont(getEditorFont());
        ruler.setText(getRulerText(currentWidth));
        GridData rulerLayoutData = new GridData();
        rulerLayoutData.verticalAlignment = 0;
        rulerLayoutData.horizontalIndent = 5;
        ruler.setLayoutData(rulerLayoutData);

        offsetArea = new Composite(rulerArea, SWT.NONE);
        GridLayout offsetAreaLayout = new GridLayout(1, false);
        offsetAreaLayout.marginTop = 2;
        offsetAreaLayout.marginWidth = 0;
        offsetAreaLayout.marginHeight = 0;
        offsetAreaLayout.verticalSpacing = 0;
        offsetArea.setLayout(offsetAreaLayout);
        GridData offsetAreaLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        offsetArea.setLayoutData(offsetAreaLayoutData);

        dataAreaText = new DataAreaText(rulerArea, currentWidth);
        dataAreaText.setTextLimit(getWrappedDataSpace().getLength());
        dataAreaText.setFont(getEditorFont());
        GridData dataAreaTextLayoutData = createRulerAndEditorLayoutData();
        dataAreaText.setLayoutData(dataAreaTextLayoutData);

        // Set screen value
        dataAreaText.setText(getWrappedDataSpace().getStringValue());

        // Add 'status changed' listener
        dataAreaText.addStatusChangedListener(new DataAreaTextStatusChangedListener());

        layoutEditorArea();

        parent.addPaintListener(new ParentPaintListener());
    }

    @Override
    public void setStatusMessage(String message) {
        statusMessage = message;
    }

    /**
     * Updates the status of actions: enables/disables them depending on whether
     * there is text selected and whether inserting or overwriting is active.
     * Undo/redo actions are enabled/disabled as well.
     */
    @Override
    public void updateActionsStatus() {

        boolean textSelected = dataAreaText.isSelected();
        boolean lengthModifiable = textSelected;

        IAction action;
        IActionBars bars = getEditorSite().getActionBars();

        action = bars.getGlobalActionHandler(ActionFactory.CUT.getId());
        if (action != null) {
            action.setEnabled(lengthModifiable);
        }

        action = bars.getGlobalActionHandler(ActionFactory.COPY.getId());
        if (action != null) {
            action.setEnabled(textSelected);
        }

        action = bars.getGlobalActionHandler(ActionFactory.PASTE.getId());
        if (action != null) {
            action.setEnabled(true);
        }

        action = bars.getGlobalActionHandler(ActionFactory.UNDO.getId());
        if (action != null) {
            action.setEnabled(false);
        }

        action = bars.getGlobalActionHandler(ActionFactory.REDO.getId());
        if (action != null) {
            action.setEnabled(false);
        }

        action = bars.getGlobalActionHandler(ActionFactory.DELETE.getId());
        if (action != null) {
            action.setEnabled(lengthModifiable);
        }

        action = bars.getGlobalActionHandler(ActionFactory.SELECT_ALL.getId());
        if (action != null) {
            action.setEnabled(true);
        }

        bars.updateActionBars();
    }

    /**
     * Updates the status line.
     */
    @Override
    public void updateStatusLine() {

        StatusLine statusLine = getStatusLine();
        if (statusLine == null) {
            return;
        }

        statusLine.setShowMode(true);
        statusLine.setShowPosition(true);
        statusLine.setShowValue(false);
        statusLine.setShowMessage(true);

        if (statusLine != null) {
            statusLine.setMessage(statusMessage);
            statusMessage = null;
            if (dataAreaText != null) {
                if (dataAreaText.isSelected()) {
                    statusLine.setSelection(dataAreaText.getSelection(), (byte)0);
                } else {
                    statusLine.setPosition(dataAreaText.getCaretPosition(), (byte)0);
                }
                statusLine.setInsertMode(!dataAreaText.isOverwriteMode());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        dataAreaText.setEditable(isEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSave(IProgressMonitor aMonitor) {
        Throwable exception = getWrappedDataSpace().setValue(dataAreaText.getText());
        handleSaveResult(aMonitor, exception);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetDirtyFlag() {
        super.resetDirtyFlag();
        dataAreaText.resetDirtyFlag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialFocus() {
        dataAreaText.setFocus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getCutAction() {
        if (cutAction == null) {
            cutAction = new CutAction();
        }
        return cutAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getCopyAction() {
        if (copyAction == null) {
            copyAction = new CopyAction();
        }
        return copyAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getPasteAction() {
        if (pasteAction == null) {
            pasteAction = new PasteAction();
        }
        return pasteAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getUndoAction() {
        if (undoAction == null) {
            undoAction = new UndoAction();
        }
        return undoAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getRedoAction() {
        if (redoAction == null) {
            redoAction = new RedoAction();
        }
        return redoAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getDeleteAction() {
        if (deleteAction == null) {
            deleteAction = new DeleteAction();
        }
        return deleteAction; // deleteAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getSelectAllAction() {
        if (selectAllAction == null) {
            selectAllAction = new SelectAllAction();
        }
        return null; // selectAllAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getFindReplaceAction(EditorPart anEditorPart) {
        if (findReplaceAction == null) {
            findReplaceAction = new FindReplaceAction(ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedTextEditorMessages"), null,
                anEditorPart);
        }
        return findReplaceAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPerformFind() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findAndSelect(int aWidgetOffset, String aFindString, boolean aSearchForward, boolean aCaseSensitive, boolean aWholeWord) {

        // Tested regular expression beforehand
        // TODO: implement IFindReplaceTargetExtension3
        boolean isRegEx = false;

        String findRegEx;
        String text;
        if (aSearchForward) {
            findRegEx = aFindString;
            text = dataAreaText.getText();
        } else {
            findRegEx = StringHelper.reverse(aFindString);
            text = StringHelper.reverse(dataAreaText.getText());
        }

        int offset;
        if (aWidgetOffset < 0) {
            // wrap
            offset = 0;
        } else {
            if (aSearchForward) {
                offset = aWidgetOffset;
            } else {
                offset = text.length() - aWidgetOffset - 1;
            }
        }

        int index;
        if (isRegEx) {
            int flags = 0;
            if (!aCaseSensitive) {
                flags = flags | Pattern.CASE_INSENSITIVE;
            }
            if (aWholeWord) {
                findRegEx = "\\b" + findRegEx + "\\b";
            }
            Pattern pattern = Pattern.compile(findRegEx, flags);
            Matcher matcher = pattern.matcher(text);
            if (!matcher.find(offset)) {
                return -1;
            }
            index = matcher.start();
        } else {
            if (!aCaseSensitive) {
                findRegEx = findRegEx.toLowerCase();
                text = text.toLowerCase();
            }
            index = text.indexOf(findRegEx, offset);
            if (index >= 0) {
                if (aWholeWord && !(isPrecededByWordBoundary(text, index) && isFollowedByWordBoundary(text, index, aFindString.length()))) {
                    int newOffset;
                    if (aSearchForward) {
                        newOffset = index + aFindString.length();
                    } else {
                        newOffset = aWidgetOffset - aFindString.length();
                    }
                    return findAndSelect(newOffset, aFindString, aSearchForward, aCaseSensitive, aWholeWord);
                }
            }
        }

        if (index < 0) {
            return -1;
        }

        if (aSearchForward) {
            dataAreaText.setSelection(index, index + aFindString.length());
            return index;
        } else {
            index = dataAreaText.getText().length() - index - aFindString.length();
            dataAreaText.setSelection(index, index + aFindString.length());
            return index;
        }
    }

    private boolean isPrecededByWordBoundary(String text, int offset) {
        if (offset == 0) {
            return true;
        }
        if (isWordBoundaryChar(text.substring(offset - 1, offset))) {
            return true;
        }
        return false;
    }

    private boolean isFollowedByWordBoundary(String text, int offset, int length) {
        if (offset == text.length()) {
            return true;
        }
        if (isWordBoundaryChar(text.substring(offset + length, offset + length + 1))) {
            return true;
        }
        return false;
    }

    private boolean isWordBoundaryChar(String aChar) {
        // [^a-zA-Z_0-9]
        Pattern pattern = Pattern.compile("[^a-zA-Z_0-9]");
        Matcher matcher = pattern.matcher(aChar);
        return matcher.find();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getSelection() {
        int start = dataAreaText.getSelection().x;
        int length = dataAreaText.getSelection().y - start;
        Point selection = new Point(start, length);
        return selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectionText() {
        return dataAreaText.getSelectionText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditable() {
        return dataAreaText.isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceSelection(String aText) {
        dataAreaText.replaceTextRange(getSelection(), aText);
    }

    /**
     * Returns the font used for the ruler, offset column and editor.
     * 
     * @return font for ruler, offset column and editor
     */
    private Font getEditorFont() {
        return FontHelper.getFixedSizeFont();
    }

    /**
     * Returns height of the characters of the font of the editor.
     * 
     * @return character height
     */
    private int getEditorCharHeight() {
        return FontHelper.getFontCharHeight(ruler);
    }

    /**
     * Updates the editor, when its height has been changed.
     * 
     * @param aNumRows - new height of the editor
     */
    private void updateEditorHeight(int aNumRows) {
        if (aNumRows == lastNumRows) {
            return;
        }
        lastNumRows = aNumRows;

        int heightHint = aNumRows * getEditorCharHeight();

        GridData gd;
        gd = (GridData)offsetArea.getLayoutData();
        gd.heightHint = heightHint;
        gd = (GridData)dataAreaText.getLayoutData();
        gd.heightHint = heightHint;

        updateEditor(ruler.getFont(), aNumRows);
    }

    /**
     * Updates the editor, when its height has been changed.
     * 
     * @param aFont - new font of the editor
     */
    private void updateEditorFont(Font aFont) {
        if (aFont == lastFont) {
            return;
        }
        lastFont = aFont;

        offsetLabel.setFont(aFont);
        ruler.setFont(aFont);
        dataAreaText.setFont(aFont);

        int numRows = getNumRowsOfEditorArea();
        if (offsetArea.getChildren().length == 0) {
            createOffsetLabels(aFont, numRows);
        }

        updateEditor(aFont, numRows);
    }

    /**
     * Updates the editor, when its height has been changed.
     * 
     * @param aFont - new font of the editor
     */
    private void updateEditor(Font aFont, int aNumRows) {

        int maxLength = offsetLabel.getText().length();

        if (offsetArea.getChildren().length != aNumRows) {
            while (offsetArea.getChildren().length > aNumRows) {
                int i = offsetArea.getChildren().length - 1;
                offsetArea.getChildren()[i].dispose();
            }
            while (offsetArea.getChildren().length < aNumRows) {
                createNumericOffsetLabel(offsetArea, aFont);
            }
            if (lastTopIndex == -1) {
                drawOffsetLabels(0);
            } else {
                drawOffsetLabels(lastTopIndex);
            }
        }

        Control[] labels = offsetArea.getChildren();
        for (Control control : labels) {
            Label label = (Label)control;
            label.setFont(aFont);
            if (label.getText().length() > maxLength) {
                maxLength = label.getText().length();
            }
        }

        int widthHint = maxLength * FontHelper.getFontCharWidth(ruler);

        ((GridData)offsetLabel.getLayoutData()).widthHint = widthHint;
        labels = offsetArea.getChildren();
        for (Control control : labels) {
            Label label = (Label)control;
            ((GridData)label.getLayoutData()).widthHint = widthHint;
        }

        layoutEditorArea();
    }

    /**
     * Layouts the editor area.
     */
    private void layoutEditorArea() {
        offsetArea.layout();
        rulerArea.layout();
        editorArea.layout();
        editorAreaScrollable.setContent(editorArea);
        editorAreaScrollable.setMinSize(editorArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Creates the labels of the offset column.
     * 
     * @param aFont - font used for the labels
     * @param aNumRows - number of offset labels to create
     */
    private void createOffsetLabels(Font aFont, int aNumRows) {
        for (int i = 0; i < aNumRows; i++) {
            createNumericOffsetLabel(offsetArea, aFont);
        }
        drawOffsetLabels(0);
    }

    /**
     * Creates a right aligned offset label for a given parent.
     * 
     * @param aParent - parent, the label is attached to
     * @param aFont - font used for the label
     */
    private void createNumericOffsetLabel(Composite aParent, Font aFont) {
        Label label = createTextOffsetLabel(aParent, aFont, "-1");
        label.setAlignment(SWT.RIGHT);
    }

    /**
     * Creates an offset label with the specified text. The label is attached to
     * the specified parent.
     * 
     * @param aParent - parent, the label is attached to
     * @param aFont - font used for the label
     * @param aText - text of the label
     * @return
     */
    private Label createTextOffsetLabel(Composite aParent, Font aFont, String aText) {
        Label label = new Label(aParent, SWT.NONE);
        label.setFont(aFont);
        label.setAlignment(SWT.LEFT);
        GridData offsetHeadlineLayoutData = new GridData();
        offsetHeadlineLayoutData.widthHint = OFFSET_LABEL_WIDTH_HINT;
        label.setLayoutData(offsetHeadlineLayoutData);
        label.setText(aText);
        return label;
    }

    /**
     * Draws the offset labels starting at a given top index.
     * 
     * @param aTopIndex - top index used to calculate the first offset
     */
    private void drawOffsetLabels(int aTopIndex) {
        int lineLength = currentWidth;
        int offset = aTopIndex * lineLength;
        Control[] labels = offsetArea.getChildren();
        for (Control control : labels) {
            Label label = (Label)control;
            label.setText(offset + "");
            offset = offset + lineLength;
        }
        lastTopIndex = aTopIndex;
    }

    /**
     * Returns the number of rows that fit into the editor area.
     * 
     * @return number of rows fitting into the editor area
     */
    private int getNumRowsOfEditorArea() {
        int numRows = (parent.getClientArea().height - 145) / getEditorCharHeight();
        if (numRows > dataAreaText.getTotalNumberOfRows()) {
            numRows = dataAreaText.getTotalNumberOfRows();
        }
        if (numRows < 0) {
            numRows = 0;
        }
        return numRows;
    }

    /**
     * Creates the layout data used for the ruler and the editor.
     * 
     * @return layout data for ruler and editor
     */
    private GridData createRulerAndEditorLayoutData() {
        GridData rulerAreaLayoutData = new GridData();
        rulerAreaLayoutData.horizontalAlignment = GridData.FILL;
        rulerAreaLayoutData.verticalAlignment = GridData.BEGINNING;
        rulerAreaLayoutData.grabExcessHorizontalSpace = false;
        return rulerAreaLayoutData;
    }

    /**
     * Returns the ruler text.
     * 
     * @param aLength - line length in characters
     * @return ruler text
     */
    private String getRulerText(int aLength) {
        StringBuilder ruler = new StringBuilder();
        int tensDigit = 0;
        for (int i = 1; i <= aLength; i++) {
            if (i < 5) {
                ruler.append(".");
            } else if ((i - 1) % 10 == 0) {
                ruler.append(" ");
            } else if (i % 10 == 0) {
                tensDigit++;
                ruler.append(tensDigit);
            } else if ((i + 1) % 10 == 0) {
                ruler.append(" ");
            } else if (i % 5 == 0) {
                ruler.append("+");
            } else {
                ruler.append(".");
            }
        }
        return ruler.toString();
    }

    /**
     * Class, that overrides the original "paste" action of the SWT Text widget.
     * <ul>
     * <li>Text of multiple lines<br>
     * Lines are expanded to the current line length of the editor. If one line
     * exceeds the line length of the editor, all new line characters are
     * removed.</li>
     * <li>Single line text<br>
     * The text is inserted as it is.</li>
     * </ul>
     * 
     * @see DataAreaText#replaceTextRange(Point, String)
     */
    private class PasteAction extends Action {
        @Override
        public void run() {
            if (dataAreaText.hasFocus()) {
                String text = getClipboardText();
                if (StringHelper.isNullOrEmpty(text)) {
                    return;
                }
                replaceSelection(text);
            }
        }

        @Override
        public String getId() {
            return ActionFactory.PASTE.getId();
        }
    };

    /**
     * Class, that overrides the original "copy" action of the SWT Text widget.
     * <p>
     * Intended behavior:
     * <ul>
     * <li>Text is returned with no linefeeds, of course.</li>
     * </ul>
     * 
     * @see DataAreaText#getSelectionText(Point, String)
     */
    private class CopyAction extends Action {
        @Override
        public void run() {
            if (dataAreaText.hasFocus()) {
                String text = getSelectionText();
                if (StringHelper.isNullOrEmpty(text)) {
                    return;
                }
                setClipboardText(text);
            }
        }

        @Override
        public String getId() {
            return ActionFactory.COPY.getId();
        }
    }

    /**
     * Class, that overrides the original "cut" action of the SWT Text widget.
     * <p>
     * Intended behavior:
     * <ul>
     * <li>Text is returned with no linefeeds, of course.</li>
     * </ul>
     * 
     * @see DataAreaText#getSelectionText(Point, String)
     */
    private class CutAction extends Action {
        @Override
        public void run() {
            if (dataAreaText.hasFocus()) {
                String text = getSelectionText();
                if (StringHelper.isNullOrEmpty(text)) {
                    return;
                }
                setClipboardText(text);
                replaceSelection("");
            }
        }

        @Override
        public String getId() {
            return ActionFactory.CUT.getId();
        }
    }

    /**
     * Class, that overrides the original "undo" action of the SWT Text widget.
     * <p>
     * Intended behavior:
     * <ul>
     * <li>no action</li>
     * </ul>
     */
    private class UndoAction extends Action {
        @Override
        public void run() {
        }

        @Override
        public String getId() {
            return ActionFactory.UNDO.getId();
        }
    }

    /**
     * Class, that overrides the original "redo" action of the SWT Text widget.
     * <p>
     * Intended behavior:
     * <ul>
     * <li>no action</li>
     * </ul>
     */
    private class RedoAction extends Action {
        @Override
        public void run() {
        }

        @Override
        public String getId() {
            return ActionFactory.REDO.getId();
        }
    }

    /**
     * Class, that overrides the original "delete" action of the SWT Text
     * widget.
     * <p>
     * Intended behavior:
     * <ul>
     * <li>no action</li>
     * </ul>
     */
    private class DeleteAction extends Action {
        @Override
        public void run() {
        }

        @Override
        public String getId() {
            return ActionFactory.DELETE.getId();
        }
    }

    /**
     * Class, that overrides the original "select all" action of the SWT Text
     * widget.
     * <p>
     * Intended behavior:
     * <ul>
     * <li>no action</li>
     * </ul>
     */
    private class SelectAllAction extends Action {
        @Override
        public void run() {
        }

        @Override
        public String getId() {
            return ActionFactory.SELECT_ALL.getId();
        }
    }

    /**
     * Class, used to resize the editor area (<i>see: dataAreaText</i>) when the
     * editor part is resized.
     */
    private class ParentPaintListener implements PaintListener {
        public void paintControl(PaintEvent event) {
            int numRows = getNumRowsOfEditorArea();

            changeEditorSize(numRows);
        }

        private void changeEditorSize(int aNumRows) {
            updateEditorHeight(aNumRows);
        }

    }

    /**
     * Class, that listens for changes on the FontRegistry in order to change
     * the editor font, when the preferences are changed.
     */
    private class ThemeChangedListener implements IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            if (FontHelper.EDITOR_FIXED_SIZE.equals(event.getProperty())) {
                if (event.getNewValue() instanceof FontData[]) {
                    FontData[] fontDataArray = (FontData[])event.getNewValue();
                    changeFont(fontDataArray[0]);
                }
            }
        }

        private void changeFont(FontData aFontData) {
            Font font = SWTResourceManager.getFont(aFontData.getName(), aFontData.getHeight(), aFontData.getStyle());
            updateEditorFont(font);
        }
    }

    private class DataAreaTextStatusChangedListener implements StatusChangedListener {
        public void statusChanged(StatusChangedEvent anEvent) {
            if (anEvent.dirty) {
                setEditorDirty();
            }

            updateActionsStatus();
            updateStatusLine();
            
            updateOffsetLabels(anEvent.topIndex);
        }

        private void updateOffsetLabels(int aTopIndex) {
            if (lastTopIndex == aTopIndex || offsetArea.getChildren().length == 0) {
                return;
            }
            drawOffsetLabels(aTopIndex);
        }
    }
}
