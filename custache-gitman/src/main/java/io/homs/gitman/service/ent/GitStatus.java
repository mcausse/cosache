package io.homs.gitman.service.ent;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GitStatus {
    boolean mergeConflict = false;
    final List<FileStatus> stagedFiles = new ArrayList<>();
    final List<FileStatus> modifiedFiles = new ArrayList<>();
    final List<FileStatus> untrackedFiles = new ArrayList<>();

    public void setMergeConflict(boolean mergeConflict) {
        this.mergeConflict = mergeConflict;
    }

    public int getTotalFiles() {
        return stagedFiles.size() + modifiedFiles.size() + untrackedFiles.size();
    }

    public boolean hasSomeFile() {
        return !stagedFiles.isEmpty() || !modifiedFiles.isEmpty() || !untrackedFiles.isEmpty();
    }
}
