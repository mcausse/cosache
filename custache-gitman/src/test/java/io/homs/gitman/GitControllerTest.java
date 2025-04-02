//package io.homs.gitman.controller;
//
//import io.homs.custache.CachedModelAndView;
//import io.homs.gitman.repository.GitLocalManagerRepository;
//import io.homs.gitman.service.GitLocalManagerService;
//import io.homs.gitman.service.ent.Branch;
//import io.homs.gitman.service.ent.Commit;
//import io.homs.gitman.service.ent.FileStatus;
//import io.homs.gitman.service.ent.GitStatus;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class GitControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private GitLocalManagerService gitService;
//
//    @MockBean
//    private GitLocalManagerRepository gitRepository;
//
//    @MockBean
//    private CachedModelAndView cachedModelAndView;
//
//    private static final String TEST_REPO_PATH = "/test/repo/path";
//    private static final String TEST_BRANCH = "main";
//    private static final String TEST_FILE = "test.txt";
//    private static final String TEST_COMMIT_MSG = "Test commit";
//    private static final String TEST_STASH_ID = "stash@{0}";
//
//    @BeforeEach
//    public void setup() {
//        // Mock GitLocalManagerService's repository dependency
//        when(gitRepository.executeCommandThrow(anyString(), eq("git"), any(String[].class)))
//                .thenReturn("Mock command output");
//
//        // Mock CachedModelAndView for the index page
//        CachedModelAndView.ViewBuilder viewBuilder = new CachedModelAndView.ViewBuilder();
//        when(cachedModelAndView.getOrParse(anyString())).thenReturn(viewBuilder);
//        when(viewBuilder.with(anyString(), any())).thenReturn(viewBuilder);
//        when(viewBuilder.evaluate()).thenReturn("<html>Mock HTML</html>");
//
//        // Set up common mock returns for git service methods
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("branch")))
//                .thenReturn("* main\n  develop\n  feature/test");
//
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("rev-parse"), eq("--abbrev-ref"), eq("HEAD")))
//                .thenReturn(TEST_BRANCH);
//
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("log"), any(), any(), any()))
//                .thenReturn("abc123|Commit message|user@example.com|(HEAD -> main)|2 days ago");
//
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("status"), eq("--porcelain")))
//                .thenReturn("M test.txt\n?? new.txt");
//
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("status")))
//                .thenReturn("On branch main\nChanges not staged for commit");
//
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("stash"), eq("list")))
//                .thenReturn("stash@{0}: WIP on main: abc123 Stashed changes");
//
//        when(gitRepository.executeCommandThrow(eq(TEST_REPO_PATH), eq("git"), eq("remote"), eq("show")))
//                .thenReturn("origin");
//    }
//
//    @Test
//    public void testGetIndexPage() throws Exception {
//        mockMvc.perform(get("/git"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("<html>Mock HTML</html>"));
//    }
//
//    @Test
//    public void testSetRepositoryPath() throws Exception {
//        mockMvc.perform(patch("/git/repository")
//                        .param("repositoryPath", TEST_REPO_PATH)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//
//        // Verify that the service method was called with the right parameter
//        verify(gitService).setCurrentRepositoryPath(TEST_REPO_PATH);
//    }
//
//    @Test
//    public void testCheckoutBranch() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("checkout"), eq(TEST_BRANCH)))
//                .thenReturn("Switched to branch '" + TEST_BRANCH + "'");
//
//        mockMvc.perform(patch("/git/branch")
//                        .param("branchName", TEST_BRANCH)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Switched to branch '" + TEST_BRANCH + "'"));
//    }
//
//    @Test
//    public void testCreateBranch() throws Exception {
//        String newBranch = "feature/new-branch";
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("checkout"), eq("-b"), eq(newBranch)))
//                .thenReturn("Switched to a new branch '" + newBranch + "'");
//
//        mockMvc.perform(patch("/git/branch/create")
//                        .param("branchName", newBranch)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Switched to a new branch '" + newBranch + "'"));
//    }
//
//    @Test
//    public void testPull() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("pull"), eq("origin"), eq(TEST_BRANCH)))
//                .thenReturn("Already up to date.");
//
//        mockMvc.perform(patch("/git/commit/pull")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Already up to date."));
//    }
//
//    @Test
//    public void testPush() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("push"), eq("origin"), eq(TEST_BRANCH)))
//                .thenReturn("Everything up-to-date");
//
//        mockMvc.perform(patch("/git/commit/push")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Everything up-to-date"));
//    }
//
//    @Test
//    public void testCommit() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("commit"), eq("-m"), eq(TEST_COMMIT_MSG)))
//                .thenReturn("[main abc123] " + TEST_COMMIT_MSG);
//
//        mockMvc.perform(patch("/git/commit")
//                        .param("message", TEST_COMMIT_MSG)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("[main abc123] " + TEST_COMMIT_MSG));
//    }
//
//    @Test
//    public void testUndoLastLocalCommit() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("reset"), eq("HEAD~")))
//                .thenReturn("Unstaged changes after reset");
//
//        mockMvc.perform(patch("/git/commit/undo-last")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Unstaged changes after reset"));
//    }
//
//    @Test
//    public void testFetch() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("fetch")))
//                .thenReturn("Fetching origin");
//
//        mockMvc.perform(patch("/git/branch/fetch")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Fetching origin"));
//    }
//
//    @Test
//    public void testStashPush() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("stash"), eq("push")))
//                .thenReturn("Saved working directory and index state WIP on main");
//
//        mockMvc.perform(patch("/git/stash/push")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Saved working directory and index state WIP on main"));
//    }
//
//    @Test
//    public void testStashPop() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("stash"), eq("pop"), eq(TEST_STASH_ID)))
//                .thenReturn("Applied stash");
//
//        mockMvc.perform(patch("/git/stash/pop")
//                        .param("stashId", TEST_STASH_ID)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Applied stash"));
//    }
//
//    @Test
//    public void testStageFile() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("add"), eq(TEST_FILE)))
//                .thenReturn("");
//
//        mockMvc.perform(patch("/git/stage/add")
//                        .param("fileName", TEST_FILE)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testStageAll() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("add"), eq(".")))
//                .thenReturn("");
//
//        mockMvc.perform(patch("/git/stage/add-all")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testDiscardChanges() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("restore"), eq(TEST_FILE)))
//                .thenReturn("");
//
//        mockMvc.perform(patch("/git/stage/restore")
//                        .param("fileName", TEST_FILE)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testUnstageFile() throws Exception {
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("restore"), eq("--staged"), eq(TEST_FILE)))
//                .thenReturn("");
//
//        mockMvc.perform(patch("/git/stage/restore-staged")
//                        .param("fileName", TEST_FILE)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testDiff() throws Exception {
//        String diffOutput = "@@ -1,3 +1,4 @@\n+New line\n Context line\n-Removed line\n+Added line";
//        when(gitRepository.executeCommandThrow(any(), eq("git"), eq("diff"), eq(TEST_FILE)))
//                .thenReturn(diffOutput);
//
//        mockMvc.perform(patch("/git/stage/diff")
//                        .param("fileName", TEST_FILE)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string(diffOutput));
//    }
//}