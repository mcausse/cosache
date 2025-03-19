package io.homs.gitman.service.ent;

import lombok.Value;

import java.util.List;

@Value
public class Commit {
    String hash;
    String message;
    String author;
    String timeAgo;
    List<String> branchRefs;
}
