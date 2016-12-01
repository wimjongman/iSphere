package biz.isphere.joblogexplorer.editor.tableviewer.filters;

import org.eclipse.jface.viewers.Viewer;

import biz.isphere.joblogexplorer.model.JobLogMessage;

public abstract class AbstractMessagePropertyFilter implements IMessagePropertyFilter {

    public static final String UI_SPCVAL_ALL = "*ALL"; //$NON-NLS-1$
    public static final String UI_SPCVAL_BLANK = "*BLANK"; //$NON-NLS-1$

    protected String value;

    public void setValue(String value) {
        this.value = value;
    }

    public boolean select(Viewer tableViewer, Object parentElement, JobLogMessage element) {

        if (UI_SPCVAL_ALL.equals(value)) {
            return true;
        }

        return false;
    }
}
