package org.datacommons.service;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class LintResult {
    @Singular List<String> files;
    String error;
}
