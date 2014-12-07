package biz.isphere.antcontrib.taskdef;

import org.apache.tools.ant.BuildException;

import biz.isphere.antcontrib.configuration.Configuration;
import biz.isphere.antcontrib.configuration.ConfigurationException;
import biz.isphere.antcontrib.logger.Logger;

public abstract class BasicTask {

    private Configuration config;
    private String jacobDllPath;

    public BasicTask() {

        config = Configuration.getInstance();
    }

    public void setJacobDllPath(String path) {
        jacobDllPath = path;
    }

    public String getJacobDllPath() {
        return jacobDllPath;
    }

    public void execute() {

        try {

            initialize();
            executeTask();

        } catch (ConfigurationException e) {
            throw new BuildException("Failed to run 'winword' task.", e);
        } catch (BuildException e) {
            throw e;
        } catch (Throwable e) {
            throw new BuildException(e);
        } finally {
            terminate();
        }
    }

    protected void initializeTask() {
        return;
    }

    protected abstract void executeTask();

    protected void terminateTask() {
        return;
    }

    private void initialize() throws ConfigurationException {
        if (isNullOrEmpty(jacobDllPath)) {
            config.configureTask();
        } else {
            config.configureTask(jacobDllPath);
        }

        try {
            initializeTask();
        } catch (Throwable e) {
            Logger.logError("Failed to initialize task: " + getName(), e);
        }
    }

    private void terminate() {

        try {
            terminateTask();
        } catch (Throwable e) {
            Logger.logError("Failed to terminate task: " + getName(), e);
        }

        config.endTask();
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }

    protected boolean isNullOrEmpty(Object object) {

        if (object == null) {
            return true;
        }

        if (object instanceof String) {
            if (((String)object).trim().length() == 0) {
                return true;
            }
        }

        return false;
    }
}
