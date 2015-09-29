/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.computation.issue;

import org.junit.Test;
import org.sonar.batch.protocol.output.BatchReport;
import org.sonar.core.issue.DefaultIssue;
import org.sonar.server.computation.batch.BatchReportReaderRule;
import org.sonar.server.computation.component.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.server.computation.component.ReportComponent.builder;

public class IssueAssignerTest {

  static final int FILE_REF = 1;
  static final Component FILE = builder(Component.Type.FILE, FILE_REF).setKey("FILE_KEY").setUuid("FILE_UUID").build();

  static final BatchReport.Changesets.Builder FILE_CHANGESET_BUILDER = BatchReport.Changesets.newBuilder().setComponentRef(FILE_REF);

  @org.junit.Rule
  public BatchReportReaderRule reportReader = new BatchReportReaderRule();

  ScmAccountToUser scmAccountToUser = mock(ScmAccountToUser.class);
  DefaultAssignee defaultAssignee = mock(DefaultAssignee.class);

  IssueAssigner underTest = new IssueAssigner(null, null, reportReader, scmAccountToUser, defaultAssignee);

  @Test
  public void set_author_to_issue() throws Exception {
    addChangeset("john", 123456789L, "rev-1");

    DefaultIssue issue = new DefaultIssue()
      .setNew(true)
      .setLine(1);

    underTest.onIssue(FILE, issue);

    assertThat(issue.authorLogin()).isEqualTo("john");
  }

  @Test
  public void set_assignee_to_issue() throws Exception {
    when(scmAccountToUser.getNullable("john")).thenReturn("John C.");

    addChangeset("john", 123456789L, "rev-1");
    reportReader.putChangesets(FILE_CHANGESET_BUILDER
      .addChangesetIndexByLine(0)
      .build());

    DefaultIssue issue = new DefaultIssue()
      .setNew(true)
      .setLine(1);

    underTest.onIssue(FILE, issue);

    assertThat(issue.assignee()).isEqualTo("John C.");
  }

  @Test
  public void set_default_assignee_if_author_not_found() throws Exception {
    when(scmAccountToUser.getNullable("john")).thenReturn(null);
    when(defaultAssignee.getLogin()).thenReturn("John C");
    addChangeset("john", 123456789L, "rev-1");
    reportReader.putChangesets(FILE_CHANGESET_BUILDER
      .addChangesetIndexByLine(0)
      .build());

    DefaultIssue issue = new DefaultIssue()
      .setNew(true)
      .setLine(1);

    underTest.onIssue(FILE, issue);

    assertThat(issue.assignee()).isEqualTo("John C");
  }

  @Test
  public void set_last_committer_when_line_is_null() throws Exception {
    addChangeset("john", 123456789L, "rev-1");
    // Older changeset
    addChangeset("henry", 1234567810L, "rev-2");
    reportReader.putChangesets(FILE_CHANGESET_BUILDER
      .addChangesetIndexByLine(0)
      .addChangesetIndexByLine(1)
      .addChangesetIndexByLine(0)
      .build());

    when(scmAccountToUser.getNullable("henry")).thenReturn("Henry V");

    DefaultIssue issue = new DefaultIssue()
      .setNew(true)
      .setLine(null);

    underTest.onIssue(FILE, issue);

    assertThat(issue.assignee()).isEqualTo("Henry V");
  }

  @Test
  // TODO
  public void set_last_committer_when_line_is_bigger_than_changeset() throws Exception {
    addChangeset("john", 123456789L, "rev-1");
    when(scmAccountToUser.getNullable("henry")).thenReturn("Henry V");

    DefaultIssue issue = new DefaultIssue()
      .setNew(true)
      .setLine(3);

    underTest.onIssue(FILE, issue);

    assertThat(issue.assignee()).isEqualTo("Henry V");
  }

  @Test
  // TODO
  public void nothing_to_do_if_issue_is_not_new() throws Exception {

  }

  private void addChangeset(String author, Long date, String revision) {
    FILE_CHANGESET_BUILDER.addChangeset(BatchReport.Changesets.Changeset.newBuilder()
      .setAuthor(author)
      .setDate(date)
      .setRevision(revision)
      .build());
  }
}
