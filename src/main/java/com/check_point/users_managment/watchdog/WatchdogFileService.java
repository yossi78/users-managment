package com.check_point.users_managment.watchdog;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class WatchdogFileService {


    private static final String WATCHDOG_FILES_PATH = "c/temp/usersService/watchdog/";
    private final ObjectMapper objectMapper;

    public WatchdogFileService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void appendOperation(WatchdogOperation watchdogOperation) {
        try {
            File newWatchDogFile = new File(WATCHDOG_FILES_PATH + System.currentTimeMillis() + ".json");
            newWatchDogFile.createNewFile();
            objectMapper.writeValue(newWatchDogFile, watchdogOperation);
        } catch (IOException e) {
            log.error("Couldn't create new watchdog file the exception is:", e);
        }
    }

    private List<String> getAllFileNames() {
        File folder = new File(WATCHDOG_FILES_PATH);
        List<String> fileNames = new ArrayList<>();

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileNames.add(file.getName());
                    }
                }
            }
        }
        Collections.sort(fileNames);
        return fileNames;
    }


    public List<WatchdogOperation> readAllOperations() {
        try {
            List<WatchdogOperation> watchdogOperations = new ArrayList<>();
            List<String> fileNames = getAllFileNames();
            for (String fileName : fileNames) {
                watchdogOperations.add(readOperation(fileName));
            }
            return watchdogOperations;
        } catch (Exception e) {
            log.error("Error reading all the watchdog files, the exception is:", e);
            return List.of();
        }

    }

    public WatchdogOperation readOperation(String fileName) throws JsonProcessingException {
        return objectMapper.readValue(WATCHDOG_FILES_PATH + fileName, WatchdogOperation.class);
    }

}
