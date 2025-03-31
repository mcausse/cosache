package io.homs.gitman.service;

import io.homs.gitman.repository.GitLocalManagerRepository;
import io.homs.gitman.service.ent.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GitLocalManagerService {

    private static final Logger LOG = LoggerFactory.getLogger(GitLocalManagerService.class);

//    public static void main(String[] args) throws IOException, InterruptedException {
//        var r = new GitLocalManagerRepository();
//        r.repositoryPath = "D:/gitrepos/nplh-core";
//        System.out.println(r.getBranches());
//        System.out.println(r.getRecentCommits(20));
//    }

    @Autowired
    GitLocalManagerRepository gitRepository;


    @Getter
    private final List<String> repositoryPaths;

    @Getter
    @Setter
    private String currentRepositoryPath;

    public GitLocalManagerService(@Value("${git.repository.paths}") List<String> repositoryPaths) {
        this.repositoryPaths = repositoryPaths;
        this.currentRepositoryPath = repositoryPaths.get(0);
    }

    public List<Branch> getBranches() {
        List<Branch> branches = new ArrayList<>();
//        String currentBranch = getCurrentBranch();

        String output = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "branch");
        for (String line : output.split("\n")) {
            line = line.trim();
            String branchName = line.startsWith("*") ? line.substring(2) : line;
            boolean isSelected = line.startsWith("*");
            branches.add(new Branch(branchName, isSelected));
        }

        return branches;
    }

    public String getCurrentBranch() {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "rev-parse", "--abbrev-ref", "HEAD").trim();
    }

    public List<Commit> getRecentCommits(int limit) {
        List<Commit> commits = new ArrayList<>();

        String output = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "log", "--format=%H|%s|%ae|%d|%ar", "-n", String.valueOf(limit));
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

        String output = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "status", "--porcelain");

        String mergeStatus = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "status").toLowerCase();
        if (mergeStatus.contains("merge") && (mergeStatus.contains("in progress") || mergeStatus.contains("conflict"))) {
            status.setMergeConflict(true);
        }

        for (String line : output.split("\n")) {
            if (line.trim().isEmpty()) continue;

            String statusCode = line.substring(0, 2);
            String fileName = line.substring(3).replace("\"", "");

            if (statusCode.charAt(0) == 'M' || statusCode.charAt(0) == 'A' || statusCode.charAt(0) == 'D' || statusCode.charAt(0) == 'R') {
                status.getStagedFiles().add(new FileStatus(fileName, statusCode));
            } else if (statusCode.charAt(1) == 'M' || statusCode.equals(" D")) {
                status.getModifiedFiles().add(new FileStatus(fileName, statusCode));
            } else if (statusCode.equals("??")) {
                status.getUntrackedFiles().add(new FileStatus(fileName, statusCode));
            }
        }

        return status;
    }

    public List<StashEntry> getStashes() {
        List<StashEntry> stashes = new ArrayList<>();

        String output = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "list");
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

        return stashes;
    }


    public String switchBranch(String branchName) {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "checkout", branchName);
    }

    public String stageFile(String fileName) {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "add", fileName);
    }

    public String unstageFile(String fileName) {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "restore", "--staged", fileName);
    }

    public String unstageAll() {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "restore", "--staged", ".")
                + gitRepository.executeCommandThrow(currentRepositoryPath, "git", "restore", ".");
    }

    public String discardChanges(String fileName) {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "restore", fileName);
    }

    public String commitChanges(String message) {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "commit", "-m", message);
    }

    public String stashChanges(String message) {
        if (message != null && !message.trim().isEmpty()) {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "save", message);
        } else {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "push");
        }
    }

    public String applyStash(String stashId) {
        if (stashId.isEmpty()) {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "apply");
        } else {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "apply", stashId);
        }
    }

    public String popStash(String stashId) {
        if (stashId.isEmpty()) {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "pop");
        } else {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "pop", stashId);
        }
    }

    public String dropStash(String stashId) {
        if (stashId.isEmpty()) {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "drop");
        } else {
            return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "stash", "drop", stashId);
        }
    }

    public String pullCurrentBranch() {
        String remoteName = getRemoteName();
        String currentBranch = getCurrentBranch();
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "pull", remoteName, currentBranch);
    }

    public String pushCurrentBranch() {
        String remoteName = getRemoteName();
        String currentBranch = getCurrentBranch();
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "push", remoteName, currentBranch);
    }

    public String getRemoteName() {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "remote", "show").trim();
    }

    public String stageAll() {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "add", ".").trim();
    }

    public String fetch() {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "fetch").trim();
    }

    public String undoLastLocalCommit() {
        return gitRepository.executeCommandThrow(currentRepositoryPath, "git", "reset", "HEAD~").trim();
    }

    public String diff(String fileName) {
        String r = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "diff", fileName).trim();
        if (r.isEmpty()) {
            r = gitRepository.executeCommandThrow(currentRepositoryPath, "git", "diff", "--cached", fileName).trim();
            if (r.isEmpty()) {
                r = gitRepository.executeCommandThrow(currentRepositoryPath, "echo", fileName).trim();
            }
        }
        return r;
    }
}
