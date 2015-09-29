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
package org.sonar.server.computation.scm;

import org.sonar.batch.protocol.output.BatchReport;
import org.sonar.db.DbClient;
import org.sonar.server.computation.batch.BatchReportReader;
import org.sonar.server.computation.component.Component;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScmInfoRepositoryImpl implements ScmInfoRepository {
  private final BatchReportReader batchReportReader;
  private final DbClient dbClient;

  public ScmInfoRepositoryImpl(BatchReportReader batchReportReader, DbClient dbClient) {
    this.batchReportReader = batchReportReader;
    this.dbClient = dbClient;
  }

  @Override
  public ScmInfo getScmInfo(Component component) {
    checkNotNull(component, "Component cannot be bull");
    BatchReport.Changesets changesets = batchReportReader.readChangesets(component.getReportAttributes().getRef());
    if (changesets == null) {
      return new DbScmInfo(dbClient, component);
    } else {
      return new ReportScmInfo(changesets);
    }
  }
}
