package org.fao.geonet.harvester.push.tasks;

import org.fao.geonet.kernel.setting.SettingManager;

import java.io.File;
import java.io.IOException;

public abstract class HarvesterDataTask {
    public static final String SYSTEM_HARVESTER_SYNCH_TOOLS_PATH = "system/harvester/synch/toolsPath";

    SettingManager settingManager;

    public HarvesterDataTask(SettingManager settingManager) {
        this.settingManager = settingManager;
    }

    public abstract int synch(String harvesterUuid) throws IOException, InterruptedException;

    protected ProcessBuilder setupProcess(String tool, String harvesterUuid) {
        String synchToolsPath = settingManager.getValue(SYSTEM_HARVESTER_SYNCH_TOOLS_PATH);

        // set up the command and parameter
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(tool,
            harvesterUuid
        );
        processBuilder.directory(new File(synchToolsPath));

        return processBuilder;
    }

    protected int runTask(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        try {
            Process process = processBuilder.inheritIO().start();

            int exitCode = process.waitFor();

            return exitCode;
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
