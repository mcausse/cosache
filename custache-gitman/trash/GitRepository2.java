//package io.homs;
//
//import lombok.Data;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class GitRepository2 {
//
//
//    public static void main(String[] args) throws IOException, InterruptedException {
//        var r = new GitRepository2("C:\\java\\workospace\\cosache");
//        System.out.println(r.status());
//        System.out.println(r.log());
//    }
//
//    private final File repositoryPath;
//
//    public GitRepository2(String repositoryPath) {
//        this.repositoryPath = new File(repositoryPath);
//        if (!this.repositoryPath.exists() || !this.repositoryPath.isDirectory()) {
//            throw new IllegalArgumentException("Repository path does not exist or is not a directory");
//        }
//
//        // Check if this is a git repository
//        try {
//            executeCommand("git", "rev-parse", "--is-inside-work-tree");
//        } catch (IOException e) {
//            throw new IllegalArgumentException("Path is not a git repository", e);
//        }
//    }
//
//    // Execute a command in the repository directory
//    private String[] executeCommand(String... command) throws IOException {
//
//        System.out.println(String.join(" ", command));
//
//        ProcessBuilder processBuilder = new ProcessBuilder(command);
//        processBuilder.directory(repositoryPath);
//        processBuilder.redirectErrorStream(true);
//
//        Process process = processBuilder.start();
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        List<String> output = new ArrayList<>();
//        String line;
//
//        while ((line = reader.readLine()) != null) {
//            output.add(line);
//        }
//
//        try {
//            process.waitFor();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new IOException("Command execution interrupted", e);
//        }
//
//        if (process.exitValue() != 0) {
//            throw new IOException("Command failed with exit code: " + process.exitValue() +
//                    " and output: " + String.join("\n", output));
//        }
//
//        return output.toArray(new String[0]);
//    }
//
//    @Data
//    public static class GitStatus {
//
//        private final List<String> stagedFiles = new ArrayList<>();
//        private final List<String> modifiedFiles = new ArrayList<>();
//        private final List<String> untrackedFiles = new ArrayList<>();
//        private final String branch;
//
//        public GitStatus(String branch) {
//            this.branch = branch;
//        }
//
//        public void addStagedFile(String file) {
//            stagedFiles.add(file);
//        }
//
//        public void addModifiedFile(String file) {
//            modifiedFiles.add(file);
//        }
//
//        public void addUntrackedFile(String file) {
//            untrackedFiles.add(file);
//        }
//
//        public List<String> getStagedFiles() {
//            return stagedFiles;
//        }
//
//        public List<String> getModifiedFiles() {
//            return modifiedFiles;
//        }
//
//        public List<String> getUntrackedFiles() {
//            return untrackedFiles;
//        }
//
//        public String getBranch() {
//            return branch;
//        }
//
//        public boolean isClean() {
//            return stagedFiles.isEmpty() && modifiedFiles.isEmpty() && untrackedFiles.isEmpty();
//        }
//    }
//
//    public GitStatus status() throws IOException {
//        String[] lines = executeCommand("git", "status", "--porcelain", "-b");
//
//        // Parse branch name from the first line
//        String branch = "unknown";
//        if (lines.length > 0 && lines[0].startsWith("## ")) {
//            branch = lines[0].substring(3).split("\\.{3}")[0];
//        }
//
//        GitStatus status = new GitStatus(branch);
//
//        // Parse file statuses
//        for (String line : lines) {
//            if (line.startsWith("## ")) continue;
//
//            if (line.length() >= 2) {
//                char statusCode = line.charAt(0);
//                char statusCode2 = line.charAt(1);
//                String filename = line.substring(3);
//
//                if (statusCode == 'M' || statusCode == 'A' || statusCode == 'D' || statusCode == 'R' || statusCode == 'C') {
//                    status.addStagedFile(filename);
//                }
//
//                if (statusCode == '?' && statusCode2 == '?') {
//                    status.addUntrackedFile(filename);
//                } else if (statusCode == ' ' && (statusCode2 == 'M' || statusCode2 == 'D')) {
//                    status.addModifiedFile(filename);
//                }
//            }
//        }
//
//        return status;
//    }
//
//    public static class GitCommit {
//        String hash;
//        String authorName;
//        String authorEmail;
//        LocalDateTime commitDate;
//        String message;
//
//        public GitCommit(String hash, String authorName, String authorEmail, LocalDateTime commitDate, String message) {
//            this.hash = hash;
//            this.authorName = authorName;
//            this.authorEmail = authorEmail;
//            this.commitDate = commitDate;
//            this.message = message;
//        }
//
//        @Override
//        public String toString() {
//            return "GitCommit{" +
//                    "hash='" + hash + '\'' +
//                    ", authorName='" + authorName + '\'' +
//                    ", authorEmail='" + authorEmail + '\'' +
//                    ", commitDate=" + commitDate +
//                    ", message='" + message + '\'' +
//                    '}';
//        }
//    }
//
//    public List<GitCommit> log(int limit) throws IOException {
//        String[] logOutput = executeCommand("git", "log",
//                "--pretty=format:%H|%an|%ae|%ad|%s",
//                "--date=iso",
//                "-n", String.valueOf(limit));
//
//        List<GitCommit> commits = new ArrayList<>();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//        for (String line : logOutput) {
//            String[] parts = line.split("\\|", 5);
//            if (parts.length == 5) {
//                String hash = parts[0];
//                String authorName = parts[1];
//                String authorEmail = parts[2];
//                LocalDateTime date = LocalDateTime.parse(parts[3].substring(0, 19), formatter);
//                String message = parts[4];
//
//                commits.add(new GitCommit(hash, authorName, authorEmail, date, message));
//            }
//        }
//
//        return commits;
//    }
//
//    public List<GitCommit> log() throws IOException {
//        return log(10); // Default to 10 commits
//    }
//
//    public static class PushResult {
//        private final boolean success;
//        private final String remoteBranch;
//        private final String localBranch;
//        private final String summary;
//
//        public PushResult(boolean success, String localBranch, String remoteBranch, String summary) {
//            this.success = success;
//            this.localBranch = localBranch;
//            this.remoteBranch = remoteBranch;
//            this.summary = summary;
//        }
//
//        public boolean isSuccess() {
//            return success;
//        }
//
//        public String getRemoteBranch() {
//            return remoteBranch;
//        }
//
//        public String getLocalBranch() {
//            return localBranch;
//        }
//
//        public String getSummary() {
//            return summary;
//        }
//    }
//
//    public PushResult push(String remote, String branch) throws IOException {
//        String[] output = executeCommand("git", "push", remote, branch);
//
//        // Parse the output to determine success and details
//        boolean success = true;
//        String summary = String.join("\n", output);
//        String localBranch = branch;
//        String remoteBranch = remote + "/" + branch;
//
//        // Look for error indicators in output
//        for (String line : output) {
//            if (line.contains("error:") || line.contains("fatal:")) {
//                success = false;
//                break;
//            }
//        }
//
//        return new PushResult(success, localBranch, remoteBranch, summary);
//    }
//
//    // Default push to origin and current branch
//    public PushResult push() throws IOException {
//        // Get current branch name
//        String[] branchOutput = executeCommand("git", "rev-parse", "--abbrev-ref", "HEAD");
//        String currentBranch = branchOutput[0];
//
//        return push("origin", currentBranch);
//    }
//
//    public static class PullResult {
//        private final boolean success;
//        private final boolean fastForward;
//        private final List<String> updatedFiles;
//        private final String summary;
//        private final Map<String, Integer> stats;
//
//        public PullResult(boolean success, boolean fastForward, List<String> updatedFiles,
//                          String summary, Map<String, Integer> stats) {
//            this.success = success;
//            this.fastForward = fastForward;
//            this.updatedFiles = updatedFiles;
//            this.summary = summary;
//            this.stats = stats;
//        }
//
//        public boolean isSuccess() {
//            return success;
//        }
//
//        public boolean isFastForward() {
//            return fastForward;
//        }
//
//        public List<String> getUpdatedFiles() {
//            return updatedFiles;
//        }
//
//        public String getSummary() {
//            return summary;
//        }
//
//        public Map<String, Integer> getStats() {
//            return stats;
//        }
//    }
//
//    public PullResult pull() throws IOException {
//        String[] output = executeCommand("git", "pull", "--stat");
//
//        boolean success = true;
//        boolean fastForward = false;
//        List<String> updatedFiles = new ArrayList<>();
//        String summary = String.join("\n", output);
//        Map<String, Integer> stats = new HashMap<>();
//
//        for (String line : output) {
//            if (line.contains("error:") || line.contains("fatal:")) {
//                success = false;
//            }
//            if (line.contains("Fast-forward")) {
//                fastForward = true;
//            }
//
//            // Extract updated files
//            if (line.matches("^\\s+.+\\|\\s+\\d+\\s+[\\+\\-]+$")) {
//                String[] parts = line.trim().split("\\|");
//                if (parts.length == 2) {
//                    String filename = parts[0].trim();
//                    updatedFiles.add(filename);
//
//                    // Parse stats
//                    String statsPart = parts[1].trim();
//                    if (statsPart.matches("\\d+\\s+[\\+\\-]+")) {
//                        String[] statsParts = statsPart.split("\\s+");
//                        if (statsParts.length == 2) {
//                            int changes = Integer.parseInt(statsParts[0]);
//                            stats.put(filename, changes);
//                        }
//                    }
//                }
//            }
//        }
//
//        return new PullResult(success, fastForward, updatedFiles, summary, stats);
//    }
//
//    public PullResult pull(String remote, String branch) throws IOException {
//        return pull(); // This is a simplified implementation
//    }
//}