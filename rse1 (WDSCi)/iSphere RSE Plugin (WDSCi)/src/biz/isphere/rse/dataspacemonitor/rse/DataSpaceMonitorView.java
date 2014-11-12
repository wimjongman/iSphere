package biz.isphere.rse.dataspacemonitor.rse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.dataspace.rse.AbstractWrappedDataSpace;
import biz.isphere.core.dataspaceeditor.rse.AbstractDropDataObjectListerner;
import biz.isphere.core.dataspaceeditor.rse.IDialogView;
import biz.isphere.core.dataspaceeditor.rse.RemoteObject;
import biz.isphere.core.dataspacemonitor.rse.AbstractDataSpaceMonitorView;
import biz.isphere.core.internal.IControlDecoration;
import biz.isphere.rse.dataareaeditor.WrappedDataSpace;
import biz.isphere.rse.dataspaceeditor.rse.DropDataObjectListener;
import biz.isphere.rse.internal.RSEControlDecoration;

public class DataSpaceMonitorView extends AbstractDataSpaceMonitorView {

    @Override
    protected AbstractDropDataObjectListerner createDropListener(IDialogView editor) {
        return new DropDataObjectListener(editor);
    }

    @Override
    protected AbstractWrappedDataSpace createDataSpaceWrapper(RemoteObject remoteObject) throws Exception {
        return new WrappedDataSpace(remoteObject);
    }

    protected void createControlPopupMenu(Composite dialogEditor, Control control) {
        Menu controlMenu = new Menu(dialogEditor);
        control.setMenu(controlMenu);
        controlMenu.addMenuListener(new PopupWidget(getDecorator(control)));
    }

    protected void createControlDecorator(Control control) {
        IControlDecoration decorator = new RSEControlDecoration(control, SWT.LEFT);
        decorator.setImage(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_WATCHING).createImage());
        decorator.hide();
        decorator.setMarginWidth(5);

        setDecorator(control, decorator);
    }

    private void setDecorator(Control control, IControlDecoration decorator) {
        control.setData(decorator);
    }

    private IControlDecoration getDecorator(Control control) {
        Object data = control.getData();
        if (data instanceof IControlDecoration) {
            return (IControlDecoration)data;
        }
        return null;
    }
}
