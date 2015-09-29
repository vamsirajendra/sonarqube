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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.System2;
import org.sonar.batch.protocol.output.BatchReport;
import org.sonar.db.DbClient;
import org.sonar.db.DbTester;
import org.sonar.db.protobuf.DbFileSources;
import org.sonar.db.source.FileSourceDto;
import org.sonar.server.computation.batch.BatchReportReaderRule;
import org.sonar.server.computation.component.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.sonar.server.computation.component.ReportComponent.builder;

public class ScmInfoRepositoryImplTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  static final int FILE_REF = 1;
  static final Component FILE = builder(Component.Type.FILE, FILE_REF).setKey("FILE_KEY").setUuid("FILE_UUID").build();

  @Rule
  public BatchReportReaderRule reportReader = new BatchReportReaderRule();

  @Rule
  public DbTester dbTester = DbTester.create(System2.INSTANCE);

  DbClient dbClient = dbTester.getDbClient();

  ScmInfoRepositoryImpl underTest = new ScmInfoRepositoryImpl(reportReader, dbClient);

  @Test
  public void read_from_report() throws Exception {
    addChangesetInReport("john", 123456789L, "rev-1");

    ScmInfo scmInfo = underTest.getScmInfo(FILE);
    assertThat(scmInfo.getForLines()).hasSize(1);
  }

  @Test
  public void read_from_db() throws Exception {
    addChangesetInDb("henry", 123456789L, "rev-1");

    ScmInfo scmInfo = underTest.getScmInfo(FILE);
    assertThat(scmInfo.getForLines()).hasSize(1);
  }

  @Test
  public void read_from_report_even_if_data_in_db_exists() throws Exception {
    addChangesetInDb("henry", 123456789L, "rev-1");

    addChangesetInReport("john", 1234567810L, "rev-2");

    ScmInfo scmInfo = underTest.getScmInfo(FILE);

    assertThat(scmInfo.getForLine(1)).isPresent();
    Changeset changeset = scmInfo.getForLine(1).get();
    assertThat(changeset.getAuthor()).isEqualTo("john");
    assertThat(changeset.getDate()).isEqualTo(1234567810L);
    assertThat(changeset.getRevision()).isEqualTo("rev-2");
  }

  @Test
  public void fail_with_NPE_when_component_is_null() throws Exception {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("Component cannot be bull");

    underTest.getScmInfo(null);
  }

  private void addChangesetInDb(String author, Long date, String revision) {
    DbFileSources.Data.Builder fileDataBuilder = DbFileSources.Data.newBuilder();
    fileDataBuilder.addLinesBuilder()
      .setLine(1)
      .setScmAuthor(author)
      .setScmDate(date)
      .setScmRevision(revision);
    dbTester.getDbClient().fileSourceDao().insert(new FileSourceDto()
      .setFileUuid(FILE.getUuid())
      .setProjectUuid("PROJECT_UUID")
      .setSourceData(fileDataBuilder.build()));
  }

  private void addChangesetInReport(String author, Long date, String revision){
    reportReader.putChangesets(BatchReport.Changesets.newBuilder()
      .setComponentRef(FILE_REF)
      .addChangeset(BatchReport.Changesets.Changeset.newBuilder()
        .setAuthor(author)
        .setDate(date)
        .setRevision(revision)
        .build())
      .addChangesetIndexByLine(0)
      .build());
  }
}
