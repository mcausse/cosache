package io.homs.gitman.service.ent;

import lombok.Value;

@Value
public class Branch {
    String name;
    boolean current;
}

