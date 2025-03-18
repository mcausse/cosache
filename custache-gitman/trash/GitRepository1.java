//package io.homs;
//
//import org.springframework.stereotype.Repository;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
////@Repository
//public class GitRepository1 {
//
//    public static void main(String[] args) throws IOException, InterruptedException {
//        var r = new GitRepository1("C:\\java\\workospace\\cosache");
//        System.out.println(r.isValidGitRepository());
//        System.out.println(r.status());
//    }
//
//    private final String repositoryPath;
//    private final Runtime runtime;
//
//    public GitRepository1(String repositoryPath) {
//        this.repositoryPath = repositoryPath;
//        this.runtime = Runtime.getRuntime();
//
//        // Validate if the path is a Git repository
//        if (!isValidGitRepository()) {
//            throw new IllegalArgumentException("The provided path is not a valid Git repository: " + repositoryPath);
//        }
//    }
//
//    private boolean isValidGitRepository() {
//        try {
//            Process process = executeCommand("git", "rev-parse", "--is-inside-work-tree");
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line = reader.readLine();
//                return "true".equals(line);
//            }
//        } catch (IOException | InterruptedException e) {
//            return false;
//        }
//    }
//
//    private Process executeCommand(String... command) throws IOException, InterruptedException {
//        ProcessBuilder processBuilder = new ProcessBuilder(command);
//        processBuilder.directory(new File(repositoryPath));
//        Process process = processBuilder.start();
//        process.waitFor();
//        return process;
//    }
//
//    private String getCommandOutput(Process process) throws IOException {
//        StringBuilder output = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//        }
//        return output.toString().trim();
//    }
//
//    private String getCommandError(Process process) throws IOException {
//        StringBuilder error = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                error.append(line).append("\n");
//            }
//        }
//        return error.toString().trim();
//    }
//
//    public String status() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "status");
//        return getCommandOutput(process);
//    }
//
//    public String push() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "push");
//        String output = getCommandOutput(process);
//        String error = getCommandError(process);
//
//        return output.isEmpty() ? error : output;
//    }
//
//    public String push(String remote, String branch) throws IOException, InterruptedException {
//        Process process = executeCommand("git", "push", remote, branch);
//        String output = getCommandOutput(process);
//        String error = getCommandError(process);
//
//        return output.isEmpty() ? error : output;
//    }
//
//    public String pull() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "pull");
//        String output = getCommandOutput(process);
//        String error = getCommandError(process);
//
//        return output.isEmpty() ? error : output;
//    }
//
//    public String pull(String remote, String branch) throws IOException, InterruptedException {
//        Process process = executeCommand("git", "pull", remote, branch);
//        String output = getCommandOutput(process);
//        String error = getCommandError(process);
//
//        return output.isEmpty() ? error : output;
//    }
//
//    public String log() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "log");
//        return getCommandOutput(process);
//    }
//
//    public String log(int limit) throws IOException, InterruptedException {
//        Process process = executeCommand("git", "log", "-n", String.valueOf(limit));
//        return getCommandOutput(process);
//    }
//
//    public String log(String format) throws IOException, InterruptedException {
//        Process process = executeCommand("git", "log", "--pretty=format:" + format);
//        return getCommandOutput(process);
//    }
//
//    public String stashPush() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "stash", "push");
//        return getCommandOutput(process);
//    }
//
//    public String stashPush(String message) throws IOException, InterruptedException {
//        Process process = executeCommand("git", "stash", "push", "-m", message);
//        return getCommandOutput(process);
//    }
//
//    public String stashPop() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "stash", "pop");
//        String output = getCommandOutput(process);
//        String error = getCommandError(process);
//
//        return output.isEmpty() ? error : output;
//    }
//
//    public String stashPop(int index) throws IOException, InterruptedException {
//        Process process = executeCommand("git", "stash", "pop", "stash@{" + index + "}");
//        String output = getCommandOutput(process);
//        String error = getCommandError(process);
//
//        return output.isEmpty() ? error : output;
//    }
//
//    public List<String> getStashList() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "stash", "list");
//        String output = getCommandOutput(process);
//
//        List<String> stashes = new ArrayList<>();
//        if (!output.isEmpty()) {
//            String[] lines = output.split("\n");
//            for (String line : lines) {
//                stashes.add(line);
//            }
//        }
//
//        return stashes;
//    }
//
//    public String getBranch() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "rev-parse", "--abbrev-ref", "HEAD");
//        return getCommandOutput(process);
//    }
//
//    public List<String> getBranches() throws IOException, InterruptedException {
//        Process process = executeCommand("git", "branch");
//        String output = getCommandOutput(process);
//
//        List<String> branches = new ArrayList<>();
//        if (!output.isEmpty()) {
//            String[] lines = output.split("\n");
//            for (String line : lines) {
//                branches.add(line.trim().replace("* ", ""));
//            }
//        }
//
//        return branches;
//    }
//}