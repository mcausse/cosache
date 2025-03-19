package io.homs.gitman.service.ent;

import lombok.Value;

@Value
public class FileStatus {
    String fileName;
    String statusCode;
}
