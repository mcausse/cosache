package io.homs.gitman.repository;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Getter
@Repository
public class GitLocalManagerRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GitLocalManagerRepository.class);

    public String executeCommandThrow(String currentRepositoryPath, String... command) {
        CommandResult r = executeCommand(currentRepositoryPath, command);
        if (r.isSucceed()) {
            LOG.info(r.getOutput());
            return r.getOutput();
        }
        LOG.error(r.getOutput() + "\n" + r.getError());
        throw new RuntimeException("Executing: " + String.join(" ", command) + "; " + r.getOutput() + "\n" + r.getError());
    }

    private CommandResult executeCommand(String currentRepositoryPath, String... command) {
        LOG.info("-> " + String.join(" ", command));
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new java.io.File(currentRepositoryPath));
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
//            LOG.info("<- " + output + " " + error);
            return new CommandResult(process.exitValue() == 0, output.toString().trim(), error.toString().trim());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}