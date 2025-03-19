package io.homs.gitman;

import io.homs.custache.CachedModelAndView;
import io.homs.gitman.service.GitLocalManagerService;
import io.homs.gitman.service.ent.Branch;
import io.homs.gitman.service.ent.Commit;
import io.homs.gitman.service.ent.GitStatus;
import io.homs.gitman.service.ent.StashEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = GitController.BASE_URL)
public class GitController {

    public static final String BASE_URL = "git";

    @Autowired
    CachedModelAndView cachedModelAndView;

    @Autowired
    GitLocalManagerService gitRepository;

    @GetMapping
    public String index() {

        List<Branch> branches = gitRepository.getBranches();
        GitStatus status = gitRepository.getStatus();
        List<Commit> commits = gitRepository.getRecentCommits(10);
        List<StashEntry> stashes = gitRepository.getStashes();

        return cachedModelAndView.getOrParse("git-local-manager.html")
                .with("repositoryPaths", gitRepository.getRepositoryPaths())
                .with("branches", branches)
                .with("status", status)
                .with("commits", commits)
                .with("stashes", stashes)
                .with("servlet-context", "/" + BASE_URL)
                .evaluate();
    }

    @PatchMapping("/branch")
    public String checkoutBranch(@RequestParam(name = "branchName") String branchName) {
        return gitRepository.switchBranch(branchName);
    }

    @PatchMapping("/commit/pull")
    public String pull() {
        return gitRepository.pullCurrentBranch();
    }

    @PatchMapping("/stash/push")
    public String stashPush() {
        return gitRepository.stashChanges(null);
    }

    @PatchMapping("/stash/pop")
    public String stashPop(@RequestParam(name = "stashId") String stashId) {
        return gitRepository.popStash(stashId);
    }

    @PatchMapping("/stash/apply")
    public String stashApply(@RequestParam(name = "stashId") String stashId) {
        return gitRepository.applyStash(stashId);
    }

    @PatchMapping("/stash/drop")
    public String stashDrop(@RequestParam(name = "stashId") String stashId) {
        return gitRepository.dropStash(stashId);
    }

    @PatchMapping("/stage/add")
    public String add(@RequestParam(name = "fileName") String fileName) {
        return gitRepository.stageFile(fileName);
    }
    @PatchMapping("/stage/restore")
    public String restore(@RequestParam(name = "fileName") String fileName) {
        return gitRepository.discardChanges(fileName);
    }
    @PatchMapping("/stage/restore-staged")
    public String restoreStaged(@RequestParam(name = "fileName") String fileName) {
        return gitRepository.unstageFile(fileName);
    }
    @PatchMapping("/stage/add-all")
    public String addAll() {
        return gitRepository.stageAll();
    }
    @PatchMapping("/stage/restore-staged-all")
    public String restoreStagedAll() {
        return gitRepository.unstageAll();
    }
}
