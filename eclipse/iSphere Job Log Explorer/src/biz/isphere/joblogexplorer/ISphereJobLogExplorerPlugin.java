/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import biz.isphere.joblogexplorer.model.JobLogParserConfiguration;

/**
 * The activator class controls the plug-in life cycle
 */
public class ISphereJobLogExplorerPlugin extends AbstractUIPlugin {

    // Names of images used to represent checkboxes
    public static final String IMAGE_CHECKED = "checked.gif"; //$NON-NLS-1$
    public static final String IMAGE_UNCHECKED = "unchecked.gif"; //$NON-NLS-1$
    public static final String SEARCH_UP = "search_up_small.png"; //$NON-NLS-1$
    public static final String SEARCH_DOWN = "search_down_small.png"; //$NON-NLS-1$

    // The plug-in ID
    public static final String PLUGIN_ID = "biz.isphere.joblogexplorer"; //$NON-NLS-1$

    // The shared instance
    private static ISphereJobLogExplorerPlugin plugin;

    private static URL installURL;

    /**
     * The constructor
     */
    public ISphereJobLogExplorerPlugin() {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        plugin = this;
        installURL = context.getBundle().getEntry("/"); //$NON-NLS-1$
        
        new JobLogParserConfiguration().createSampleConfigurationFile();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static ISphereJobLogExplorerPlugin getDefault() {
        return plugin;
    }

    public static URL getInstallURL() {
        return installURL;
    }

    public Image getImage(String key) {
        return getImageRegistry().get(key);
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(IMAGE_CHECKED, getImageDescriptor(IMAGE_CHECKED));
        reg.put(IMAGE_UNCHECKED, getImageDescriptor(IMAGE_UNCHECKED));
        reg.put(SEARCH_UP, getImageDescriptor(SEARCH_UP));
        reg.put(SEARCH_DOWN, getImageDescriptor(SEARCH_DOWN));
    }

    private ImageDescriptor getImageDescriptor(String name) {
        String iconPath = "icons/"; //$NON-NLS-1$
        try {
            URL url = new URL(installURL, iconPath + name);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }
}
