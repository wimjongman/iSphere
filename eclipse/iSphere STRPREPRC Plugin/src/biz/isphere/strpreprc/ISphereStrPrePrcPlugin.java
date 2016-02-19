package biz.isphere.strpreprc;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import biz.isphere.strpreprc.model.HeaderTemplates;

/**
 * The activator class controls the plug-in life cycle
 */
public class ISphereStrPrePrcPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "biz.isphere.strpreprc"; //$NON-NLS-1$

    // The shared instance
    private static ISphereStrPrePrcPlugin plugin;

    /**
     * The constructor
     */
    public ISphereStrPrePrcPlugin() {
        super();
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {

        plugin = null;
        super.stop(context);

        HeaderTemplates.dispose();
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static ISphereStrPrePrcPlugin getDefault() {
        return plugin;
    }

}
