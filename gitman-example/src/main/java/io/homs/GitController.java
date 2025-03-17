package io.homs;

import io.homs.custache.CachedModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/git")
public class GitController {

    @Autowired
    CachedModelAndView cachedModelAndView;

    @Autowired
    GitRepository gitRepository;

    @GetMapping
    public String index() {

        // TODO

        return cachedModelAndView.getOrParse("gitinillo-template.html")
//                .with("repositories", repositoryInfos)
//                .with("servlet-context", "/" + REPOSITORIES_BASE_URL)
                .evaluate();
    }
}
