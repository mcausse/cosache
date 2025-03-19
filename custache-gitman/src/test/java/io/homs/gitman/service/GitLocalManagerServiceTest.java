package io.homs.gitman.service;

import io.homs.gitman.repository.GitLocalManagerRepository;
import io.homs.gitman.service.ent.Branch;
import io.homs.gitman.service.ent.Commit;
import io.homs.gitman.service.ent.GitStatus;
import io.homs.gitman.service.ent.StashEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitLocalManagerServiceTest {

    @Mock
    private GitLocalManagerRepository gitRepository;

    private GitLocalManagerService service;

    private final String REPOSITORY_PATH = "/path/to/repo";
    private final List<String> REPOSITORY_PATHS = Arrays.asList(REPOSITORY_PATH, "/path/to/another/repo");

    @BeforeEach
    void setUp() {
        // En lugar de usar @InjectMocks, creamos manualmente el servicio con los parámetros necesarios
        service = new GitLocalManagerService(REPOSITORY_PATHS);
        // Inyectamos manualmente el mock del repositorio
        service.gitRepository = gitRepository;
    }

    @Test
    void testGetBranches() {
        // Arrange
        String gitOutput = "* master\n  feature/branch1\n  feature/branch2";
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("branch")))
                .thenReturn(gitOutput);

        // Act
        List<Branch> branches = service.getBranches();

        // Assert
        assertThat(branches).hasSize(3);
        assertThat(branches.get(0).getName()).isEqualTo("master");
        assertThat(branches.get(0).isCurrent()).isTrue();
        assertThat(branches.get(1).getName()).isEqualTo("feature/branch1");
        assertThat(branches.get(1).isCurrent()).isFalse();
        assertThat(branches.get(2).getName()).isEqualTo("feature/branch2");
        assertThat(branches.get(2).isCurrent()).isFalse();
    }

    @Test
    void testGetCurrentBranch() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("rev-parse"), eq("--abbrev-ref"), eq("HEAD")))
                .thenReturn("master\n");

        // Act
        String currentBranch = service.getCurrentBranch();

        // Assert
        assertThat(currentBranch).isEqualTo("master");
    }

    @Test
    void testGetRecentCommits() {
        // Arrange
        String gitOutput = "hash1|Initial commit|user@example.com|(HEAD -> master)|2 days ago\n" +
                "hash2|Update README|user@example.com|(origin/develop)|5 days ago";
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("log"), eq("--format=%H|%s|%ae|%d|%ar"), eq("-n"), eq("10")))
                .thenReturn(gitOutput);

        // Act
        List<Commit> commits = service.getRecentCommits(10);

        // Assert
        assertThat(commits).hasSize(2);
        assertThat(commits.get(0).getHash()).isEqualTo("hash1");
        assertThat(commits.get(0).getMessage()).isEqualTo("Initial commit");
        assertThat(commits.get(0).getAuthor()).isEqualTo("user@example.com");
        assertThat(commits.get(0).getTimeAgo()).isEqualTo("2 days ago");
        assertThat(commits.get(0).getBranchRefs()).contains("HEAD -> master");

        assertThat(commits.get(1).getHash()).isEqualTo("hash2");
        assertThat(commits.get(1).getMessage()).isEqualTo("Update README");
        assertThat(commits.get(1).getBranchRefs()).contains("origin/develop");
    }

    @Test
    void testGetRecentCommitsWithEmptyBranchRefs() {
        // Arrange
        String gitOutput = "hash1|Initial commit|user@example.com||2 days ago";
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("log"), eq("--format=%H|%s|%ae|%d|%ar"), eq("-n"), eq("10")))
                .thenReturn(gitOutput);

        // Act
        List<Commit> commits = service.getRecentCommits(10);

        // Assert
        assertThat(commits).hasSize(1);
        assertThat(commits.get(0).getHash()).isEqualTo("hash1");
        assertThat(commits.get(0).getBranchRefs()).isNull();
    }

    @Test
    void testGetStatus() {
        // Arrange
        String porcelainOutput = "M  file1.txt\n" +
                " M file2.txt\n" +
                "?? file3.txt\n" +
                "A  file4.txt\n" +
                "D  file5.txt\n" +
                "R  oldfile.txt -> newfile.txt";
        String statusOutput = "On branch master\nAll conflicts resolved";

        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("status"), eq("--porcelain")))
                .thenReturn(porcelainOutput);
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("status")))
                .thenReturn(statusOutput);

        // Act
        GitStatus status = service.getStatus();

        // Assert
        assertThat(status.isMergeConflict()).isFalse();
        assertThat(status.getStagedFiles()).hasSize(4);
        assertThat(status.getModifiedFiles()).hasSize(1);
        assertThat(status.getUntrackedFiles()).hasSize(1);

        assertThat(status.getStagedFiles().get(0).getFileName()).isEqualTo("file1.txt");
        assertThat(status.getStagedFiles().get(0).getStatusCode()).isEqualTo("M ");

        assertThat(status.getModifiedFiles().get(0).getFileName()).isEqualTo("file2.txt");
        assertThat(status.getModifiedFiles().get(0).getStatusCode()).isEqualTo(" M");

        assertThat(status.getUntrackedFiles().get(0).getFileName()).isEqualTo("file3.txt");
        assertThat(status.getUntrackedFiles().get(0).getStatusCode()).isEqualTo("??");
    }

    @Test
    void testGetStatusWithMergeConflict() {
        // Arrange
        String porcelainOutput = "UU file1.txt";
        String statusOutput = "On branch master\nMerge conflict in file1.txt\nFix conflicts and run 'git commit'";

        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("status"), eq("--porcelain")))
                .thenReturn(porcelainOutput);
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("status")))
                .thenReturn(statusOutput);

        // Act
        GitStatus status = service.getStatus();

        // Assert
        assertThat(status.isMergeConflict()).isTrue();
    }

    @Test
    void testGetStashes() {
        // Arrange
        String stashOutput = "stash@{0}: WIP on master: 1234567 Fix bug\n" +
                "stash@{1}: On feature/branch: 7654321 Add feature";
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("list")))
                .thenReturn(stashOutput);

        // Act
        List<StashEntry> stashes = service.getStashes();

        // Assert
        assertThat(stashes).hasSize(2);
        assertThat(stashes.get(0).getId()).isEqualTo("stash@{0}");
        assertThat(stashes.get(0).getDescription()).isEqualTo("WIP on master: 1234567 Fix bug");
        assertThat(stashes.get(1).getId()).isEqualTo("stash@{1}");
        assertThat(stashes.get(1).getDescription()).isEqualTo("On feature/branch: 7654321 Add feature");
    }

    @Test
    void testSwitchBranch() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("checkout"), eq("feature/branch")))
                .thenReturn("Switched to branch 'feature/branch'");

        // Act
        String result = service.switchBranch("feature/branch");

        // Assert
        assertThat(result).isEqualTo("Switched to branch 'feature/branch'");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "checkout", "feature/branch");
    }

    @Test
    void testStageFile() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("add"), eq("file.txt")))
                .thenReturn("");

        // Act
        String result = service.stageFile("file.txt");

        // Assert
        assertThat(result).isEqualTo("");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "add", "file.txt");
    }

    @Test
    void testUnstageFile() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("restore"), eq("--staged"), eq("file.txt")))
                .thenReturn("");

        // Act
        String result = service.unstageFile("file.txt");

        // Assert
        assertThat(result).isEqualTo("");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "restore", "--staged", "file.txt");
    }

    @Test
    void testDiscardChanges() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("restore"), eq("file.txt")))
                .thenReturn("");

        // Act
        String result = service.discardChanges("file.txt");

        // Assert
        assertThat(result).isEqualTo("");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "restore", "file.txt");
    }

    @Test
    void testCommitChanges() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("commit"), eq("-m"), eq("Test commit")))
                .thenReturn("[master 1234567] Test commit\n 1 file changed, 10 insertions(+), 5 deletions(-)");

        // Act
        String result = service.commitChanges("Test commit");

        // Assert
        assertThat(result).isEqualTo("[master 1234567] Test commit\n 1 file changed, 10 insertions(+), 5 deletions(-)");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "commit", "-m", "Test commit");
    }

    @Test
    void testStashChangesWithMessage() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("save"), eq("Test stash")))
                .thenReturn("Saved working directory and index state On master: Test stash");

        // Act
        String result = service.stashChanges("Test stash");

        // Assert
        assertThat(result).isEqualTo("Saved working directory and index state On master: Test stash");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "save", "Test stash");
    }

    @Test
    void testStashChangesWithoutMessage() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("push")))
                .thenReturn("Saved working directory and index state WIP on master: 1234567 last commit");

        // Act
        String result = service.stashChanges("");

        // Assert
        assertThat(result).isEqualTo("Saved working directory and index state WIP on master: 1234567 last commit");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "push");
    }

    @Test
    void testApplyStashWithId() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("apply"), eq("stash@{1}")))
                .thenReturn("On branch master\nChanges not staged for commit");

        // Act
        String result = service.applyStash("stash@{1}");

        // Assert
        assertThat(result).isEqualTo("On branch master\nChanges not staged for commit");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "apply", "stash@{1}");
    }

    @Test
    void testApplyStashWithoutId() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("apply")))
                .thenReturn("On branch master\nChanges not staged for commit");

        // Act
        String result = service.applyStash("");

        // Assert
        assertThat(result).isEqualTo("On branch master\nChanges not staged for commit");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "apply");
    }

    @Test
    void testPopStashWithId() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("pop"), eq("stash@{1}")))
                .thenReturn("On branch master\nChanges not staged for commit\nDropped stash@{1}");

        // Act
        String result = service.popStash("stash@{1}");

        // Assert
        assertThat(result).isEqualTo("On branch master\nChanges not staged for commit\nDropped stash@{1}");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "pop", "stash@{1}");
    }

    @Test
    void testPopStashWithoutId() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("pop")))
                .thenReturn("On branch master\nChanges not staged for commit\nDropped refs/stash@{0}");

        // Act
        String result = service.popStash("");

        // Assert
        assertThat(result).isEqualTo("On branch master\nChanges not staged for commit\nDropped refs/stash@{0}");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "pop");
    }

    @Test
    void testDropStashWithId() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("drop"), eq("stash@{1}")))
                .thenReturn("Dropped stash@{1}");

        // Act
        String result = service.dropStash("stash@{1}");

        // Assert
        assertThat(result).isEqualTo("Dropped stash@{1}");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "drop", "stash@{1}");
    }

    @Test
    void testDropStashWithoutId() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("stash"), eq("drop")))
                .thenReturn("Dropped refs/stash@{0}");

        // Act
        String result = service.dropStash("");

        // Assert
        assertThat(result).isEqualTo("Dropped refs/stash@{0}");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "stash", "drop");
    }

    @Test
    void testPullCurrentBranch() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("remote"), eq("show")))
                .thenReturn("origin");
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("rev-parse"), eq("--abbrev-ref"), eq("HEAD")))
                .thenReturn("master");
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("pull"), eq("origin"), eq("master")))
                .thenReturn("Updating 1234567..7654321\nFast-forward");

        // Act
        String result = service.pullCurrentBranch();

        // Assert
        assertThat(result).isEqualTo("Updating 1234567..7654321\nFast-forward");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "remote", "show");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "rev-parse", "--abbrev-ref", "HEAD");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "pull", "origin", "master");
    }

    @Test
    void testGetRemoteName() {
        // Arrange
        when(gitRepository.executeCommandThrow(eq(REPOSITORY_PATH), eq("git"), eq("remote"), eq("show")))
                .thenReturn("origin");

        // Act
        String result = service.getRemoteName();

        // Assert
        assertThat(result).isEqualTo("origin");
        verify(gitRepository).executeCommandThrow(REPOSITORY_PATH, "git", "remote", "show");
    }

    @Test
    void testGetRepositoryPaths() {
        // Act
        List<String> paths = service.getRepositoryPaths();

        // Assert
        assertThat(paths).isEqualTo(REPOSITORY_PATHS);
    }

    @Test
    void testGetCurrentRepositoryPath() {
        // Act
        String path = service.getCurrentRepositoryPath();

        // Assert
        assertThat(path).isEqualTo(REPOSITORY_PATH);
    }
}