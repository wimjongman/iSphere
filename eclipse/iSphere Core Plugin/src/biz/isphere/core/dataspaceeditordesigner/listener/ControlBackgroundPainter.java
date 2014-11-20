package biz.isphere.core.dataspaceeditordesigner.listener;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

import biz.isphere.core.internal.ColorHelper;

public class ControlBackgroundPainter extends MouseTrackAdapter {

    private Color selectedControlBackgroundColor;

    private Control currentControl;
    private Color oldBackgroundColor;

    public ControlBackgroundPainter(Color color) {
        this.selectedControlBackgroundColor = color;
    }

    public void mouseEnter(MouseEvent event) {

        Object object = event.getSource();
        if (!(object instanceof Control)) {
            currentControl = null;
            oldBackgroundColor = null;
            return;
        }

        Control control = (Control)event.getSource();

        currentControl = control;
        oldBackgroundColor = control.getBackground();

        control.setBackground(selectedControlBackgroundColor);
    }

    @Override
    public void mouseExit(MouseEvent event) {
        if (currentControl != event.getSource()) {
            return;
        }

        if (oldBackgroundColor.equals(ColorHelper.getDefaultBackgroundColor())) {
            /*
             * Ugly hack for WDSCi to reset the background color to the default
             * color.
             */
            currentControl.setBackground(null);
        } else {
            currentControl.setBackground(oldBackgroundColor);
        }

        currentControl = null;
        oldBackgroundColor = null;
    }
}
