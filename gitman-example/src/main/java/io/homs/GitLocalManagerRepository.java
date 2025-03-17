package io.homs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GitLocalManagerRepository {

    public static void main(String[] args) throws IOException, InterruptedException {
        var r = new GitLocalManagerRepository();
        r.repositoryPath = "C:\\java\\workospace\\cosache";
        System.out.println(r.getBranches());
        System.out.println(r.getRecentCommits(20));
    }

    @Value("${git.repository.path}")
    private String repositoryPath;

    public List<Branch> getBranches() {
        List<Branch> branches = new ArrayList<>();
        String currentBranch = getCurrentBranch();

        try {
            String output = executeCommand("git", "branch");
            for (String line : output.split("\n")) {
                line = line.trim();
                String branchName = line.startsWith("*") ? line.substring(2) : line;
                boolean isSelected = line.startsWith("*");
                branches.add(new Branch(branchName, isSelected));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return branches;
    }

    public String getCurrentBranch() {
        try {
            return executeCommand("git", "rev-parse", "--abbrev-ref", "HEAD").trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<Commit> getRecentCommits(int limit) {
        List<Commit> commits = new ArrayList<>();

        try {
            String output = executeCommand("git", "log", "--format=%H|%s|%an|%ar", "-n", String.valueOf(limit));
            for (String line : output.split("\n")) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String hash = parts[0];
                    String message = parts[1];
                    String author = parts[2];
                    String timeAgo = parts[3];
                    commits.add(new Commit(hash, message, author, timeAgo));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return commits;
    }

    // 	$ git status
    // 	On branch feature/0.0.3
    // 	Changes to be committed:
    // 	  (use "git restore --staged <file>..." to unstage)
    // 	        new file:   gitman-example/src/main/java/io/homs/GitLocalManagerRepository.java
    // 	        renamed:    gitman-example/src/main/java/io/homs/GitRepository.java -> gitman-example/src/main/java/io/homs/GitRepository1.java
    // 	        new file:   gitman-example/src/main/java/io/homs/GitRepository2.java
    // 	        new file:   gitman-example/src/main/resources/templates/git-local-manager.html
    //
    // 	Changes not staged for commit:
    // 	  (use "git add <file>..." to update what will be committed)
    // 	  (use "git restore <file>..." to discard changes in working directory)
    // 	        modified:   gitman-example/src/main/java/io/homs/GitController.java
    // 	        modified:   gitman-example/src/main/java/io/homs/GitLocalManagerRepository.java
    // 	        modified:   gitman-example/src/main/java/io/homs/GitRepository1.java
    // 	        modified:   gitman-example/src/main/java/io/homs/GitRepository2.java
    // 	        modified:   gitman-example/src/main/resources/application.properties
    // 	        modified:   gitman-example/src/main/resources/templates/git-local-manager.html
    //
    // 	Untracked files:
    // 	  (use "git add <file>..." to include in what will be committed)
    // 	        jou.txt
    //

    //    mohms@jou MINGW64 /c/java/workospace/cosache (feature/0.0.3)
    //    $ git status --porcelain
    //    M gitman-example/src/main/java/io/homs/GitController.java
    //    AM gitman-example/src/main/java/io/homs/GitLocalManagerRepository.java
    //    RM gitman-example/src/main/java/io/homs/GitRepository.java -> gitman-example/src/main/java/io/homs/GitRepository1.java
    //    AM gitman-example/src/main/java/io/homs/GitRepository2.java
    //    M gitman-example/src/main/resources/application.properties
    //    AM gitman-example/src/main/resources/templates/git-local-manager.html
    //    ?? jou.txt

    public GitStatus getStatus() {
        GitStatus status = new GitStatus();

        try {
            String output = executeCommand("git", "status", "--porcelain");
            String mergeStatus = executeCommand("git", "status");

            if (mergeStatus.contains("merge") && (mergeStatus.contains("in progress") || mergeStatus.contains("conflict"))) {
                status.setMergeConflict(true);
            }

            for (String line : output.split("\n")) {
                if (line.trim().isEmpty()) continue;

                String statusCode = line.substring(0, 2);
                String fileName = line.substring(3);

                if (statusCode.charAt(0) == 'M' || statusCode.charAt(0) == 'A' || statusCode.charAt(0) == 'D' || statusCode.charAt(0) == 'R') {
                    status.getStagedFiles().add(new FileStatus(fileName, statusCode));
                } else if (statusCode.charAt(1) == 'M' || statusCode.equals(" D")) {
                    status.getModifiedFiles().add(new FileStatus(fileName, statusCode));
                } else if (statusCode.equals("??")) {
                    status.getUntrackedFiles().add(new FileStatus(fileName, statusCode));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

    public List<StashEntry> getStashes() {
        List<StashEntry> stashes = new ArrayList<>();

        try {
            String output = executeCommand("git", "stash", "list");
            for (String line : output.split("\n")) {
                if (line.trim().isEmpty()) continue;

                // Parse stash format: stash@{0}: WIP on branch: commit message
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String stashId = line.substring(0, colonIndex).trim();
                    String description = line.substring(colonIndex + 1).trim();

                    // Extract time information - assuming format typically includes "n days ago"
                    String timeAgo = "";
                    if (description.contains(" days ago") || description.contains(" hours ago") ||
                            description.contains(" minutes ago") || description.contains(" weeks ago")) {
                        for (String part : description.split(" ")) {
                            if (part.matches("\\d+")) {
                                int timeValue = Integer.parseInt(part);
                                int nextIndex = description.indexOf(part) + part.length() + 1;
                                String timeUnit = description.substring(nextIndex).split(" ")[0];
                                timeAgo = timeValue + " " + timeUnit + " ago";
                                break;
                            }
                        }
                    }

                    stashes.add(new StashEntry(stashId, description, timeAgo));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stashes;
    }

    // Git operations
    public boolean switchBranch(String branchName) {
        try {
            executeCommand("git", "checkout", branchName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stageFile(String fileName) {
        try {
            executeCommand("git", "add", fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unstageFile(String fileName) {
        try {
            executeCommand("git", "restore", "--staged", fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean discardChanges(String fileName) {
        try {
            executeCommand("git", "restore", fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean commitChanges(String message) {
        try {
            executeCommand("git", "commit", "-m", message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stashChanges(String message) {
        try {
            if (message != null && !message.trim().isEmpty()) {
                executeCommand("git", "stash", "save", message);
            } else {
                executeCommand("git", "stash");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean applyStash(String stashId) {
        try {
            executeCommand("git", "stash", "apply", stashId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean popStash(String stashId) {
        try {
            executeCommand("git", "stash", "pop", stashId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dropStash(String stashId) {
        try {
            executeCommand("git", "stash", "drop", stashId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String executeCommand(String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new java.io.File(repositoryPath));
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return output.toString();
    }

    // Entity classes
    public static class Branch {
        private final String name;
        private final boolean current;

        public Branch(String name, boolean current) {
            this.name = name;
            this.current = current;
        }

        public String getName() {
            return name;
        }

        public boolean isCurrent() {
            return current;
        }

        @Override
        public String toString() {
            return "Branch{" +
                    "name='" + name + '\'' +
                    ", current=" + current +
                    '}';
        }
    }

    public static class Commit {
        private final String hash;
        private final String message;
        private final String author;
        private final String timeAgo;

        public Commit(String hash, String message, String author, String timeAgo) {
            this.hash = hash;
            this.message = message;
            this.author = author;
            this.timeAgo = timeAgo;
        }

        public String getHash() {
            return hash;
        }

        public String getMessage() {
            return message;
        }

        public String getAuthor() {
            return author;
        }

        public String getTimeAgo() {
            return timeAgo;
        }

        @Override
        public String toString() {
            return "Commit{" +
                    "hash='" + hash + '\'' +
                    ", message='" + message + '\'' +
                    ", author='" + author + '\'' +
                    ", timeAgo='" + timeAgo + '\'' +
                    '}';
        }
    }

    public static class GitStatus {
        private boolean mergeConflict = false;
        private final List<FileStatus> stagedFiles = new ArrayList<>();
        private final List<FileStatus> modifiedFiles = new ArrayList<>();
        private final List<FileStatus> untrackedFiles = new ArrayList<>();

        public boolean isMergeConflict() {
            return mergeConflict;
        }

        public void setMergeConflict(boolean mergeConflict) {
            this.mergeConflict = mergeConflict;
        }

        public List<FileStatus> getStagedFiles() {
            return stagedFiles;
        }

        public List<FileStatus> getModifiedFiles() {
            return modifiedFiles;
        }

        public List<FileStatus> getUntrackedFiles() {
            return untrackedFiles;
        }

        @Override
        public String toString() {
            return "GitStatus{" +
                    "mergeConflict=" + mergeConflict +
                    ", stagedFiles=" + stagedFiles +
                    ", modifiedFiles=" + modifiedFiles +
                    ", untrackedFiles=" + untrackedFiles +
                    '}';
        }
    }

    public static class FileStatus {
        private final String fileName;
        private final String statusCode;

        public FileStatus(String fileName, String statusCode) {
            this.fileName = fileName;
            this.statusCode = statusCode;
        }

        public String getFileName() {
            return fileName;
        }

        public String getStatusCode() {
            return statusCode;
        }


        @Override
        public String toString() {
            return "FileStatus{" +
                    "fileName='" + fileName + '\'' +
                    ", statusCode='" + statusCode + '\'' +
                    '}';
        }
    }

    public static class StashEntry {
        private final String id;
        private final String description;
        private final String timeAgo;

        public StashEntry(String id, String description, String timeAgo) {
            this.id = id;
            this.description = description;
            this.timeAgo = timeAgo;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getTimeAgo() {
            return timeAgo;
        }

        @Override
        public String toString() {
            return "StashEntry{" +
                    "id='" + id + '\'' +
                    ", description='" + description + '\'' +
                    ", timeAgo='" + timeAgo + '\'' +
                    '}';
        }
    }
}