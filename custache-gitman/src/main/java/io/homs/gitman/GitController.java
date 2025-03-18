package io.homs.gitman;

import io.homs.custache.CachedModelAndView;
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
    GitLocalManagerRepository gitRepository;

    @GetMapping
    public String index() {

        List<GitLocalManagerRepository.Branch> branches = gitRepository.getBranches();
        GitLocalManagerRepository.GitStatus status = gitRepository.getStatus();
        List<GitLocalManagerRepository.Commit> commits = gitRepository.getRecentCommits(10);
        List<GitLocalManagerRepository.StashEntry> stashes = gitRepository.getStashes();

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
}
