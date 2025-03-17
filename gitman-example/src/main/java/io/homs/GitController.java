package io.homs;

import io.homs.custache.CachedModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/git")
public class GitController {

    @Autowired
    CachedModelAndView cachedModelAndView;

    @Autowired
    GitLocalManagerRepository gitRepository;

    @GetMapping
    public String index() {

        // TODO
        List<GitLocalManagerRepository.Branch> branches = gitRepository.getBranches();

        GitLocalManagerRepository.GitStatus status = gitRepository.getStatus();

        return cachedModelAndView.getOrParse("git-local-manager.html")
                .with("branches", branches)
                .with("status", status)
//                .with("repositories", repositoryInfos)
//                .with("servlet-context", "/" + REPOSITORIES_BASE_URL)
                .evaluate();
    }
}
