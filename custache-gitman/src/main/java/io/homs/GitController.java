package io.homs;

import io.homs.custache.CachedModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = GitController.BASE_URL)
public class GitController {

    public static final String BASE_URL = "/git";

    @Autowired
    CachedModelAndView cachedModelAndView;

    @Autowired
    GitLocalManagerRepository gitRepository;

    @GetMapping
    public String index() {

        // TODO
        List<GitLocalManagerRepository.Branch> branches = gitRepository.getBranches();

        GitLocalManagerRepository.GitStatus status = gitRepository.getStatus();

        List<GitLocalManagerRepository.Commit> commits = gitRepository.getRecentCommits(10);

        return cachedModelAndView.getOrParse("git-local-manager.html")
                .with("branches", branches)
                .with("status", status)
                .with("commits", commits)
                .with("servlet-context", "/" + BASE_URL)
                .evaluate();
    }
}
