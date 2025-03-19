package io.homs.gitman.repository;

import lombok.Value;

@Value
public class CommandResult {
    boolean succeed;
    String output;
    String error;
}