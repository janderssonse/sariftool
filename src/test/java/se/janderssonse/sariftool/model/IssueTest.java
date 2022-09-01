// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import se.janderssonse.sariftool.model.sonar.Issue;

public class IssueTest {

    Optional<Issue.Severity> blocker = Optional.of(Issue.Severity.BLOCKER);
    Optional<Issue.Severity> critical = Optional.of(Issue.Severity.CRITICAL);
    Optional<Issue.Severity> info = Optional.of(Issue.Severity.INFO);
    Optional<Issue.Severity> major = Optional.of(Issue.Severity.MAJOR);
    Optional<Issue.Severity> minor = Optional.of(Issue.Severity.MINOR);
    Optional<Issue.Severity> empty = Optional.empty();

    @Test
    public void severity_is_mapped_to_type() {

        assertEquals(Issue.Type.BUG, Issue.severityToType(blocker).get());
        assertEquals(Issue.Type.VULNERABILITY, Issue.severityToType(critical).get());
        assertEquals(Issue.Type.CODE_SMELL, Issue.severityToType(info).get());
        assertEquals(Issue.Type.CODE_SMELL, Issue.severityToType(major).get());
        assertEquals(Issue.Type.CODE_SMELL, Issue.severityToType(minor).get());
        assertTrue(Issue.severityToType(empty).isEmpty());

    }

}
