package io.homs.gitman.service.ent;

import lombok.Value;

@Value
public class StashEntry {
    String id;
    String description;
    String timeAgo;
}
