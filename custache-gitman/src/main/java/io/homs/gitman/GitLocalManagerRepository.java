package io.homs.gitman;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Repository
public class GitLocalManagerRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GitLocalManagerRepository.class);

//    public static void main(String[] args) throws IOException, InterruptedException {
//        var r = new GitLocalManagerRepository();
//        r.repositoryPath = "D:/gitrepos/nplh-core";
//        System.out.println(r.getBranches());
//        System.out.println(r.getRecentCommits(20));
//    }


    private final List<String> repositoryPaths;

    private final String currentRepositoryPath;

    public GitLocalManagerRepository(@Value("${git.repository.paths}") List<String> repositoryPaths) {
        this.repositoryPaths = repositoryPaths;
        this.currentRepositoryPath = repositoryPaths.get(0);
    }

    public List<Branch> getBranches() {
        List<Branch> branches = new ArrayList<>();
        String currentBranch = getCurrentBranch();

        try {
            String output = executeCommandThrow("git", "branch");
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
            return executeCommandThrow("git", "rev-parse", "--abbrev-ref", "HEAD").trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<Commit> getRecentCommits(int limit) {
        List<Commit> commits = new ArrayList<>();

        try {
            String output = executeCommandThrow("git", "log", "--format=%H|%s|%ae|%d|%ar", "-n", String.valueOf(limit));
            for (String line : output.split("\n")) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    String hash = parts[0];
                    String message = parts[1];
                    String author = parts[2];

                    // ...| (HEAD -> feature/NPLH-11301-WIBU-modules-skeleton, origin/feature/NPLH-11301-WIBU-modules-skeleton) |5 hours ago
                    String branchRefsStr = parts[3].trim();
                    List<String> branchRefs = null;
                    if (!branchRefsStr.trim().isEmpty()) {
                        branchRefs = Arrays.asList(branchRefsStr.substring(1, branchRefsStr.length() - 1).split(", "));
                    }

                    String timeAgo = parts[4];

                    commits.add(new Commit(hash, message, author, timeAgo, branchRefs));
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
            String output = executeCommandThrow("git", "status", "--porcelain");
            String mergeStatus = executeCommandThrow("git", "status");

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
            String output = executeCommandThrow("git", "stash", "list");
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
            String r = executeCommandThrow("git", "checkout", branchName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stageFile(String fileName) {
        try {
            executeCommandThrow("git", "add", fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unstageFile(String fileName) {
        try {
            executeCommandThrow("git", "restore", "--staged", fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean discardChanges(String fileName) {
        try {
            executeCommandThrow("git", "restore", fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean commitChanges(String message) {
        try {
            executeCommandThrow("git", "commit", "-m", message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stashChanges(String message) {
        try {
            if (message != null && !message.trim().isEmpty()) {
                executeCommandThrow("git", "stash", "save", message);
            } else {
                executeCommandThrow("git", "stash");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean applyStash(String stashId) {
        try {
            executeCommandThrow("git", "stash", "apply", stashId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean popStash(String stashId) {
        try {
            executeCommandThrow("git", "stash", "pop", stashId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dropStash(String stashId) {
        try {
            executeCommandThrow("git", "stash", "drop", stashId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @lombok.Value
    public static class CommandResult {
        boolean succeed;
        String output;
        String error;
    }

    private String executeCommandThrow(String... command) throws IOException {
        CommandResult r = executeCommand(command);
        if (r.isSucceed()) {
            return r.getOutput();
        }
        throw new RuntimeException("Executing: " + String.join(" ", command) + "; " + r.getError());
    }

    private CommandResult executeCommand(String... command) throws IOException {
        LOG.info("-> " + String.join(" ", command));

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

        LOG.info("<- " + output);
        String outputStr = output.toString().trim();
        String errorStr = error.toString().trim();

        return new CommandResult(errorStr.isEmpty(), outputStr, errorStr);
    }

    // Entity classes
    @Getter
    public static class Branch {
        private final String name;
        private final boolean current;

        public Branch(String name, boolean current) {
            this.name = name;
            this.current = current;
        }

        @Override
        public String toString() {
            return "Branch{" +
                    "name='" + name + '\'' +
                    ", current=" + current +
                    '}';
        }
    }

    @Getter
    public static class Commit {
        private final String hash;
        private final String message;
        private final String author;
        private final String timeAgo;
        private final List<String> branchRefs;

        public Commit(String hash, String message, String author, String timeAgo, List<String> branchRefs) {
            this.hash = hash;
            this.message = message;
            this.author = author;
            this.timeAgo = timeAgo;
            this.branchRefs = branchRefs;
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

    @Getter
    public static class GitStatus {
        private boolean mergeConflict = false;
        private final List<FileStatus> stagedFiles = new ArrayList<>();
        private final List<FileStatus> modifiedFiles = new ArrayList<>();
        private final List<FileStatus> untrackedFiles = new ArrayList<>();

        public void setMergeConflict(boolean mergeConflict) {
            this.mergeConflict = mergeConflict;
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

    @Getter
    public static class FileStatus {
        private final String fileName;
        private final String statusCode;

        public FileStatus(String fileName, String statusCode) {
            this.fileName = fileName;
            this.statusCode = statusCode;
        }


        @Override
        public String toString() {
            return "FileStatus{" +
                    "fileName='" + fileName + '\'' +
                    ", statusCode='" + statusCode + '\'' +
                    '}';
        }
    }

    @Getter
    public static class StashEntry {
        private final String id;
        private final String description;
        private final String timeAgo;

        public StashEntry(String id, String description, String timeAgo) {
            this.id = id;
            this.description = description;
            this.timeAgo = timeAgo;
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