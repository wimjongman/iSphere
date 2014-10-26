/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataareaeditor.controls;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.Messages;
import biz.isphere.core.dataareaeditor.events.StatusChangedEvent;
import biz.isphere.core.dataareaeditor.events.StatusChangedListener;
import biz.isphere.core.internal.ColorHelper;
import biz.isphere.core.internal.FontHelper;

/**
 * Wrapped SWT Text widget that implements the specific need of a Text widget
 * for editing data areas. The most important difference to the standard SWT
 * widget is, that the text is displayed in lines having a fixed length.
 */
public class DataAreaText {

    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final String CRLF = CR + LF;
    private static final String EOL_CHAR = "�";

    Text textControl;

    int maxLength;
    int lineLength;
    int lineLengthIcludingCR;

    private boolean isInsertMode = false;
    private boolean hasFocus = false;
    private boolean isDirty;

    private StatusChangedEvent lastStatusChangedEvent = null;

    Vector<StatusChangedListener> statusChangedListeners = new Vector<StatusChangedListener>();

    public DataAreaText(Composite aParent, int aStyle, int aLineLength) {

        textControl = new Text(aParent, aStyle);
        lineLength = aLineLength;
        lineLengthIcludingCR = lineLength + CRLF.length();

        textControl.addKeyListener(new TextControlKeyListener());
        textControl.addMouseListener(new TextControlMouseListener());
        textControl.addFocusListener(new TextControlFocusListener());
        textControl.addPaintListener(new TextControlPaintListener());
    }

    public void addStatusChangedListener(StatusChangedListener listener) {
        statusChangedListeners.addElement(listener);
    }

    public void removeStatusChangedListener(StatusChangedListener listener) {
        statusChangedListeners.removeElement(listener);
    }

    public void setFont(Font aFont) {
        textControl.setFont(aFont);
    }

    public void setLayoutData(Object aLayoutData) {
        textControl.setLayoutData(aLayoutData);
    }

    public void setTextLimit(int aLimit) {
        maxLength = aLimit;
        int maxLengthWithCR = getNumberOfCompleteRows() * lineLengthIcludingCR + getNumberOfRemainingChars() + EOL_CHAR.length();
        textControl.setTextLimit(maxLengthWithCR);
    }

    public void setText(String aText) {
        textControl.setText(toScreen(StringHelper.getFixLength(aText, maxLength)));
    }

    public String getText() {
        return fromScreen(textControl.getText());
    }

    public boolean hasFocus() {
        return hasFocus;
    }

    public void setSelection(int aStart, int anEnd) {
        int start = aStart + (aStart / lineLength) * CRLF.length();
        if (start > textControl.getTextLimit()) {
            start = textControl.getTextLimit() - 1;
        }
        textControl.setSelection(start, start + (anEnd - aStart));
    }

    public void replaceTextRange(Point aSelection, String aNewText) {
        int start = aSelection.x;
        int length = aSelection.y;
        performReplaceTextRange(start, length, aNewText);
    }

    /**
     * Returns the start index and the end index of the selected text, where
     * Point.x is the start index and Point.y is the end index.
     * 
     * @return selection with start and end index
     */
    public Point getSelection() {
        Point selection = textControl.getSelection();
        int length = selection.y - selection.x;
        int start = selection.x - (selection.x / lineLengthIcludingCR) * CRLF.length();
        selection = new Point(start, start + length);
        return selection;
    }

    /**
     * Returns the currently selected text.
     * 
     * @return selected text
     */
    public String getSelectionText() {
        Point selection = getSelection();
        if (selection.y == 0) {
            return "";
        }
        String text = fromScreen(textControl.getText());
        return text.substring(selection.x, selection.y);
    }

    public boolean isEnabled() {
        return textControl.isEnabled();
    }

    public void setFocus() {
        textControl.setFocus();
    }

    public void resetDirtyFlag() {
        fireStatusChangedEvent(false);
    }

    private int getSelectedLength(Point aSelection) {
        return aSelection.y - aSelection.x;
    }

    private void setSelection(int aPosition) {
        setSelection(aPosition, aPosition);
    }

    private String fromScreen(String aValue) {
        String tValue;
        if (aValue.endsWith(EOL_CHAR)) {
            tValue = aValue.substring(0, aValue.length() - 1);
        } else {
            tValue = aValue;
        }

        String[] tLines = tValue.replaceAll(CR, "").split(LF);
        StringBuilder stringValue = new StringBuilder();
        for (String line : tLines) {
            stringValue.append(line);
        }
        return stringValue.toString();
    }

    private String toScreen(String stringValue) {
        StringBuffer wrapped = new StringBuffer();

        int offset = 0;
        while (offset + lineLength < stringValue.length()) {
            if (offset > 0) {
                wrapped.append(CRLF);
            }
            wrapped.append(stringValue.substring(offset, offset + lineLength));
            offset = offset + lineLength;
        }

        if (offset < stringValue.length()) {
            if (offset > 0) {
                wrapped.append(CRLF);
            }
            wrapped.append(stringValue.substring(offset));
        }

        return wrapped.toString() + EOL_CHAR;
    }

    private int getCaretPosition() {
        int position = (getCaretRow() - 1) * lineLength + getCaretColumn();
        return position;
    }

    private int getCaretColumn() {
        int column = (textControl.getSelection().x + 1) - (getCaretRow() - 1) * lineLengthIcludingCR;
        return column;
    }

    private int getCaretRow() {
        int row = (textControl.getSelection().x + 1) / lineLengthIcludingCR;
        if ((textControl.getSelection().x + 1) % lineLengthIcludingCR != 0) {
            row++;
        }
        return row;
    }

    private int getTotalNumberOfRows() {
        int rows = maxLength / lineLength;
        if (maxLength % lineLength != 0) {
            rows++;
        }
        return rows;
    }

    private int getNumberOfCompleteRows() {
        return maxLength / lineLength;
    }

    private int getNumberOfRemainingChars() {
        return maxLength - getNumberOfCompleteRows() * lineLength;
    }

    private void performBackspace() {
        if (getCaretPosition() <= 1) {
            return;
        }

        Point selection = getSelection();
        if (getSelectedLength(selection) > 0) {
            deleteSelection(selection.x, selection.y);
        } else {
            deleteSelection(selection.x - 1, selection.x);
        }
    }

    private void performDelete() {
        if (getCaretPosition() > maxLength) {
            return;
        }

        Point selection = getSelection();
        if (getSelectedLength(selection) > 0) {
            deleteSelection(selection.x, selection.y);
        } else {
            deleteSelection(selection.x, selection.x + 1);
        }
    }

    private void performInsert(char character) {
        Point selection = getSelection();
        int length = selection.y - selection.x;
        performReplaceTextRange(selection.x, length, Character.toString(character));
    }

    private void performOverwrite(char character) {
        Point selection = getSelection();
        int length = selection.y - selection.x;
        if (length == 0) {
            length = 1;
        }
        performReplaceTextRange(selection.x, length, Character.toString(character));
    }

    private void performReplaceTextRange(int aStart, int aLength, String aNewText) {
        if (getCaretPosition() + aNewText.length() - 1 > maxLength) {
            return;
        }

        String text = getText();
        if (isInsertMode && !hasEnoughSpace(text, aLength, aNewText.length())) {
            fireStatusChangedEvent(Messages.Not_enough_space_to_insert_text);
            return;
        }

        text = delete(text, aStart, aStart + aLength);
        text = insert(text, aStart, aNewText);
        updateSelection(text, aStart + aNewText.length());
    }

    private void deleteSelection(int aStart, int anEnd) {
        String text = getText();
        text = delete(text, aStart, anEnd);
        updateSelection(text, aStart);
    }

    private void updateSelection(String aText, int aStart) {
        setText(aText);
        setSelection(aStart);
        fireStatusChangedEvent(true, "");
    }

    private String delete(String aText, int aStart, int anEnd) {
        if (aStart == anEnd) {
            return aText;
        }
        aText = aText.substring(0, aStart) + aText.substring(anEnd) + StringHelper.getFixLength(" ", anEnd - aStart);
        return aText;
    }

    private String insert(String text, int aStart, String aText) {
        if (aText.length() == 0) {
            return text;
        }
        text = text.substring(0, aStart) + aText + text.substring(aStart, maxLength - aText.length());
        return text;
    }

    private boolean hasEnoughSpace(String text, int aLength, int aNewLengh) {
        if (StringHelper.trimR(text).length() - aLength + aNewLengh <= maxLength) {
            return true;
        }
        return false;
    }

    private boolean isControlKey(KeyEvent anEvent) {
        return (anEvent.stateMask & SWT.CTRL) == SWT.CTRL;
    }

    private void fireStatusChangedEvent(boolean anIsDirty) {
        isDirty = anIsDirty;
        fireStatusChangedEvent();
    }

    private void fireStatusChangedEvent(boolean anIsDirty, String aText) {
        isDirty = anIsDirty;
        fireStatusChangedEvent(aText);
    }

    private void fireStatusChangedEvent() {
        StatusChangedEvent event = new StatusChangedEvent(textControl, textControl.getTopIndex(), getCaretPosition(), getCaretRow(),
            getCaretColumn(), isInsertMode, isDirty);
        if (lastStatusChangedEvent == null || !lastStatusChangedEvent.equals(event)) {
            fireStatusChangedEvent(event);
            lastStatusChangedEvent = event;
        }
    }

    private void fireStatusChangedEvent(String aMessage) {
        StatusChangedEvent event = new StatusChangedEvent(textControl, textControl.getTopIndex(), getCaretPosition(), getCaretRow(),
            getCaretColumn(), isInsertMode, isDirty);
        event.message = aMessage;
        if (lastStatusChangedEvent == null || !lastStatusChangedEvent.equals(event)) {
            fireStatusChangedEvent(event);
            lastStatusChangedEvent = event;
        }
    }

    private void fireStatusChangedEvent(StatusChangedEvent anEvent) {
        for (StatusChangedListener listener : statusChangedListeners) {
            listener.statusChanged(anEvent);
        }
    }

    /**
     * Inner class, that listens for key strokes in order to perform a
     * specialized action.
     */
    private class TextControlKeyListener implements KeyListener {

        public void keyReleased(KeyEvent anEvent) {
            fireStatusChangedEvent();
        }

        public void keyPressed(KeyEvent anEvent) {
            if (anEvent.keyCode == SWT.INSERT) {
                // Toggle insert mode
                isInsertMode = !isInsertMode;

            } else if (anEvent.keyCode == SWT.HOME) {
                // HOME

            } else if (anEvent.keyCode == SWT.END) {
                // END
                if (isControlKey(anEvent)) {
                    // END of text
                    textControl.setSelection(textControl.getText().length() - EOL_CHAR.length());
                } else {
                    // END of line
                    textControl.setSelection(getCaretRow() * lineLengthIcludingCR - CRLF.length());
                }
                anEvent.doit = false;

            } else if (anEvent.keyCode == SWT.ARROW_UP | anEvent.keyCode == SWT.ARROW_DOWN) {
                // Cursor UP, DOWN

            } else if (anEvent.keyCode == SWT.ARROW_LEFT) {
                // Cursor LEFT
                if (getCaretPosition() <= 1) {
                    anEvent.doit = false;
                }

            } else if (anEvent.keyCode == SWT.ARROW_RIGHT) {
                // Cursor RIGHT
                if (getCaretPosition() > maxLength) {
                    anEvent.doit = false;
                }

            } else if (anEvent.keyCode == SWT.DEL) {
                // DELETE
                performDelete();
                anEvent.doit = false;

            } else if (anEvent.keyCode == SWT.BS) {
                // BACKSPACE
                performBackspace();
                anEvent.doit = false;

            } else if (StringHelper.isAsciiPrintable(anEvent.character)) {
                // typed character
                if (isInsertMode) {
                    performInsert(anEvent.character);
                } else {
                    performOverwrite(anEvent.character);
                }
                anEvent.doit = false;
            }
        }
    };

    /**
     * Inner class that listens for mouse events in order to update the status
     * bar.
     */
    private class TextControlMouseListener extends MouseAdapter {
        @Override
        public void mouseDown(MouseEvent anEvent) {
            fireStatusChangedEvent();
        }
    }

    /**
     * Inner class that listens for the focus of the widget in order to let the
     * editor decide whether or not to perform a CUT or PASTE event.
     */
    private class TextControlFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            hasFocus = true;
            fireStatusChangedEvent();
        }

        @Override
        public void focusLost(FocusEvent e) {
            hasFocus = false;
        }
    }

    private class TextControlPaintListener implements PaintListener {
        public void paintControl(PaintEvent event) {
            paintBackground(event);
        }

        public void paintBackground(PaintEvent event) {

            Text drawable = (Text)event.getSource();
            int rows = getTotalNumberOfRows();
            Color color = ColorHelper.getUnreachableBackgroundColor();

            GC gc = event.gc;
            gc.setBackground(color);

            int charWidht = FontHelper.getFontCharWidth(drawable);
            int charHeight = FontHelper.getFontCharHeight(drawable);
            int editableAreaHeight = rows * charHeight;
            gc.fillRectangle(0, editableAreaHeight, drawable.getClientArea().width, drawable.getClientArea().height - editableAreaHeight);

            // Remaining space of last row
            if (maxLength % lineLength == 0) {
                return;
            }

            // Have to add 2 for the EOL_CHAR, because of inaccurate charWidth
            int x = (getNumberOfRemainingChars() + 2) * charWidht;
            int y = getNumberOfCompleteRows() * charHeight;
            int width = drawable.getClientArea().width - x;
            int height = drawable.getClientArea().height - y;
            gc.fillRectangle(x, y, width, height);
        }
    }

}
