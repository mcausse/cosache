package io.homs.gitman;

import io.homs.custache.CachedModelAndView;
import io.homs.gitman.service.GitLocalManagerService;
import io.homs.gitman.service.ent.Branch;
import io.homs.gitman.service.ent.Commit;
import io.homs.gitman.service.ent.GitStatus;
import io.homs.gitman.service.ent.StashEntry;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = GitController.BASE_URL)
public class GitController {

    public static final String BASE_URL = "git";

    @Autowired
    CachedModelAndView cachedModelAndView;

    @Autowired
    GitLocalManagerService gitService;

    @GetMapping
    public String index(@RequestParam(name = "successNotification", required = false) String successNotification
            /*TODO limitació amb diff; això és un URL param, amb limit de longitud; refact to PUT with body*/
    ) {

        List<Branch> branches = gitService.getBranches();
        GitStatus status = gitService.getStatus();
        List<Commit> commits = gitService.getRecentCommits(20);
        List<StashEntry> stashes = gitService.getStashes();

        final List<Repository> repositoryPaths = new ArrayList<>();
        for (String repoPath : gitService.getRepositoryPaths()) {
            repositoryPaths.add(new Repository(repoPath, repoPath.equals(gitService.getCurrentRepositoryPath())));
        }

        return cachedModelAndView.getOrParse("git-local-manager.html")
                .with("repositories", repositoryPaths)
                .with("branches", branches)
                .with("status", status)
                .with("commits", commits)
                .with("stashes", stashes)
                .with("successNotification", successNotification == null ? null : successNotification.trim())
                .with("servlet-context", "/" + BASE_URL)
                .with("uuid", UUID.randomUUID())
                .evaluate();
    }

    @Value
    public static class Repository {
        String path;
        boolean selected;
    }

    @PatchMapping("/repository")
    public void setRepositoryPath(@RequestParam(name = "repositoryPath") String repositoryPath) {
        gitService.setCurrentRepositoryPath(repositoryPath);
    }

    @PatchMapping("/branch")
    public String checkoutBranch(@RequestParam(name = "branchName") String branchName) {
        return gitService.switchBranch(branchName);
    }

    @PatchMapping("/branch/create")
    public String createBranch(@RequestParam(name = "branchName") String branchName) {
        return gitService.createBranch(branchName);
    }

    @PatchMapping("/commit/pull")
    public String pull() {
        return gitService.pullCurrentBranch();
    }

    @PatchMapping("/commit/push")
    public String push() {
        return gitService.pushCurrentBranch();
    }

    @PatchMapping("/commit")
    public String commit(@RequestParam(name = "message") String message) {
        return gitService.commitChanges(message);
    }

    @PatchMapping("/commit/undo-last")
    public String undoLastLocalCommit() {
        return gitService.undoLastLocalCommit();
    }

    @PatchMapping("/branch/fetch")
    public String fetch() {
        return gitService.fetch();
    }

    @PatchMapping("/stash/push")
    public String stashPush() {
        return gitService.stashChanges(null);
    }

    @PatchMapping("/stash/pop")
    public String stashPop(@RequestParam(name = "stashId") String stashId) {
        return gitService.popStash(stashId);
    }

    @PatchMapping("/stash/apply")
    public String stashApply(@RequestParam(name = "stashId") String stashId) {
        return gitService.applyStash(stashId);
    }

    @PatchMapping("/stash/drop")
    public String stashDrop(@RequestParam(name = "stashId") String stashId) {
        return gitService.dropStash(stashId);
    }

    @PatchMapping("/stage/add")
    public String add(@RequestParam(name = "fileName") String fileName) {
        return gitService.stageFile(fileName);
    }

    @PatchMapping("/stage/restore")
    public String restore(@RequestParam(name = "fileName") String fileName) {
        return gitService.discardChanges(fileName);
    }

    @PatchMapping("/stage/restore-staged")
    public String restoreStaged(@RequestParam(name = "fileName") String fileName) {
        return gitService.unstageFile(fileName);
    }

    @PatchMapping("/stage/add-all")
    public String addAll() {
        return gitService.stageAll();
    }

    @PatchMapping("/stage/restore-staged-all")
    public String restoreStagedAll() {
        return gitService.unstageAll();
    }

    @PatchMapping("/stage/diff")
    public String diff(@RequestParam(name = "fileName") String fileName) {
        return gitService.diff(fileName);
    }
}
