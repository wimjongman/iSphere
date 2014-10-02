/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.build.nls.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import biz.isphere.build.nls.configuration.Configuration;
import biz.isphere.build.nls.utils.FileUtil;

/**
 * This class represents an Eclipse project. It is the top level class of the
 * class hierarchy.
 * <p>
 * 
 * @author Thomas Raddatz
 */
public class EclipseProject {

    File fPath;
    Map<String, NLSResourceBundle> fNLSBundle;

    public EclipseProject(String projectName) throws Exception {
        fPath = new File(Configuration.getInstance().getWorkspacePath() + projectName);
        fNLSBundle = new HashMap<String, NLSResourceBundle>();
    }

    public String getPath() {
        return FileUtil.fixAbsolutePath(fPath.getPath());
    }

    public String getName() {
        return fPath.getName();
    }

    public String[] getLanguageIDs() {
        Set<String> languages = new TreeSet<String>();
        for (String bundleKey : fNLSBundle.keySet()) {
            NLSResourceBundle bundle = fNLSBundle.get(bundleKey);
            languages.addAll(bundle.getLanguageKeys());
        }
        return languages.toArray(new String[languages.size()]);
    }

    public NLSResourceBundle[] getBundles() {
        return fNLSBundle.values().toArray(new NLSResourceBundle[fNLSBundle.values().size()]);
    }

    public void addBundle(NLSResourceBundle bundle) {
        fNLSBundle.put(bundle.getID(), bundle);
    }

    public void loadNLSPropertiesFiles(FileSelectionEntry[] files) throws Exception {
        for (FileSelectionEntry selectedFile : files) {
            loadFileAndCreateBundle(this, selectedFile.getPath(), selectedFile.getPattern(), selectedFile.isSubdirectories());
        }
    }

    public void updateNLSPropertiesFiles() throws Exception {
        for (NLSResourceBundle bundle : fNLSBundle.values()) {
            bundle.updateFiles(getName());
        }
    }

    private void loadFileAndCreateBundle(EclipseProject project, String relativePath, String pattern, boolean searchSubDirectories) throws Exception {
        File[] files = new File(project.getPath() + relativePath).listFiles();
        String regex = pattern.replace("?", ".?").replace("*", ".*?").replace("\\", "\\\\");
        for (File file : files) {
            if (file.isFile() && matchesPattern(file, regex)) {
                NLSPropertiesFile nlsFile = new NLSPropertiesFile(project.getPath(), file);
                NLSResourceBundle bundle = fNLSBundle.get(nlsFile.getResourceNameWithoutLanguageID());
                if (bundle == null) {
                    bundle = new NLSResourceBundle(nlsFile.getResourceNameWithoutLanguageID());
                    fNLSBundle.put(nlsFile.getResourceNameWithoutLanguageID(), bundle);
                }
                bundle.add(nlsFile);
            } else if (file.isDirectory() && searchSubDirectories) {
                loadFileAndCreateBundle(project, file.getAbsolutePath().substring(project.getPath().length()), pattern, searchSubDirectories);
            }
        }
    }

    private boolean matchesPattern(File file, String regex) {
        return file.getName().matches(regex);
    }

    @Override
    public String toString() {
        return fPath.getName();
    }

}
