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

import com.google.common.base.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.core.issue.DefaultIssue;
import org.sonar.db.DbClient;
import org.sonar.server.computation.batch.BatchReportReader;
import org.sonar.server.computation.component.Component;
import org.sonar.server.computation.scm.Changeset;
import org.sonar.server.computation.scm.ScmInfo;
import org.sonar.server.computation.scm.ScmInfoRepository;

/**
 * Detect the SCM author and SQ assignee.
 * <p/>
 * It relies on:
 * <ul>
 *   <li>SCM information sent in the report for modified files</li>
 *   <li>sources lines stored in database for non-modified files</li>
 * </ul>
 */
public class IssueAssigner2 extends IssueVisitor {

  private final DbClient dbClient;
  private final ScmInfoRepository scmInfoRepository;
  private final BatchReportReader reportReader;
  private final DefaultAssignee defaultAssigne;
  private final ScmAccountToUser scmAccountToUser;

  private long lastCommitDate = 0L;
  private String lastCommitAuthor = null;
  private ScmInfo scmChangesets = null;

  public IssueAssigner2(DbClient dbClient, ScmInfoRepository scmInfoRepository, BatchReportReader reportReader,
                        ScmAccountToUser scmAccountToUser, DefaultAssignee defaultAssigne) {
    this.dbClient = dbClient;
    this.scmInfoRepository = scmInfoRepository;
    this.reportReader = reportReader;
    this.scmAccountToUser = scmAccountToUser;
    this.defaultAssigne = defaultAssigne;
  }

  @Override
  public void onIssue(Component component, DefaultIssue issue) {
    if (issue.isNew()) {
      // optimization - do not load SCM data of this component if there are no new issues
      loadScmChangesetsIfNeeded(component);

      String scmAuthor = guessScmAuthor(issue.line());
      issue.setAuthorLogin(scmAuthor);
      if (scmAuthor != null) {
        String assigneeLogin = scmAccountToUser.getNullable(scmAuthor);
        if (assigneeLogin == null) {
          issue.setAssignee(defaultAssigne.getLogin());
        } else {
          issue.setAssignee(assigneeLogin);
        }
      }
    }
  }

  private void loadScmChangesetsIfNeeded(Component component) {
    if (scmChangesets == null) {
      scmChangesets = scmInfoRepository.getScmInfo(component);
      computeLastCommitDateAndAuthor();
    }
  }

  @Override
  public void afterComponent(Component component) {
    lastCommitDate = 0L;
    lastCommitAuthor = null;
    scmChangesets = null;
  }

  /**
   * Get the SCM login of the last committer on the line. When line is zero,
   * then get the last committer on the file.
   */
  @CheckForNull
  private String guessScmAuthor(@Nullable Integer line) {
    String author = null;
    if (line != null) {
      Optional<Changeset> changesetOptional = scmChangesets.getForLine(line);
      if (changesetOptional.isPresent()) {
        Changeset changeset = scmChangesets.getForLine(line).get();
        author = changeset.getAuthor();
      }
    }
    return StringUtils.defaultIfEmpty(author, lastCommitAuthor);
  }

  private void computeLastCommitDateAndAuthor() {
    for (Changeset changeset : scmChangesets.getForLines()) {
      String author = changeset.getAuthor();
      Long date = changeset.getDate();
      if (author != null && date != null && date > lastCommitDate) {
        lastCommitDate = date;
        lastCommitAuthor = author;
      }
    }
  }
}
