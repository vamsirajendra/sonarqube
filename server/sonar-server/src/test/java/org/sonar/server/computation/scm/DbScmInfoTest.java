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
import org.sonar.db.DbClient;
import org.sonar.db.DbTester;
import org.sonar.db.protobuf.DbFileSources;
import org.sonar.db.source.FileSourceDto;
import org.sonar.server.computation.component.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.sonar.server.computation.component.ReportComponent.builder;
import static org.sonar.server.source.index.FileSourceTesting.newFakeData;

public class DbScmInfoTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  static final String PROJECT_UUID = "PROJECT_UUID";

  static final int FILE_REF = 1;
  static final String FILE_UUID = "FILE_UUID";

  static final Component FILE = builder(Component.Type.FILE, FILE_REF).setUuid(FILE_UUID).setKey("FILE_KEY").build();

  @Rule
  public DbTester dbTester = DbTester.create(System2.INSTANCE);

  DbClient dbClient = dbTester.getDbClient();

  @Test
  public void create_scm_info_with_some_changesets() throws Exception {
    insertFileSource(newFakeData(10).build());

    ScmInfo scmInfo = new DbScmInfo(dbClient, FILE);

    assertThat(scmInfo.getForLines()).hasSize(10);
  }

  @Test
  public void return_changeset_for_a_given_line() throws Exception {
    DbFileSources.Data.Builder fileDataBuilder = DbFileSources.Data.newBuilder();
    addLine(fileDataBuilder, 1, "john", 123456789L, "rev-1");
    addLine(fileDataBuilder, 2, "henry", 1234567810L, "rev-2");
    addLine(fileDataBuilder, 3, "henry", 1234567810L, "rev-2");
    addLine(fileDataBuilder, 4, "john", 123456789L, "rev-1");
    insertFileSource(fileDataBuilder.build());

    ScmInfo scmInfo = new DbScmInfo(dbClient, FILE);

    assertThat(scmInfo.getForLines()).hasSize(4);

    assertThat(scmInfo.getForLine(4)).isPresent();
    Changeset changeset = scmInfo.getForLine(4).get();
    assertThat(changeset.getAuthor()).isEqualTo("john");
    assertThat(changeset.getDate()).isEqualTo(123456789L);
    assertThat(changeset.getRevision()).isEqualTo("rev-1");
  }

  @Test
  public void return_latest_changeset() throws Exception {
    DbFileSources.Data.Builder fileDataBuilder = DbFileSources.Data.newBuilder();
    addLine(fileDataBuilder, 1, "john", 123456789L, "rev-1");
    // Older changeset
    addLine(fileDataBuilder, 2, "henry", 1234567810L, "rev-2");
    addLine(fileDataBuilder, 3, "john", 123456789L, "rev-1");
    insertFileSource(fileDataBuilder.build());

    ScmInfo scmInfo = new DbScmInfo(dbClient, FILE);

    assertThat(scmInfo.getLatestChangeset()).isPresent();
    Changeset latestChangeset = scmInfo.getLatestChangeset().get();
    assertThat(latestChangeset.getAuthor()).isEqualTo("henry");
    assertThat(latestChangeset.getDate()).isEqualTo(1234567810L);
    assertThat(latestChangeset.getRevision()).isEqualTo("rev-2");
  }

  @Test
  public void create_empty_scm_info() throws Exception {
    insertFileSource(DbFileSources.Data.newBuilder().build());
    ScmInfo scmInfo = new DbScmInfo(dbClient, FILE);

    assertThat(scmInfo.getForLines()).isEmpty();
  }

  @Test
  public void fail_with_ISE_when_file_has_no_source() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("The file 'ReportComponent{ref=1, key='FILE_KEY', type=FILE}' has no source");

    ScmInfo scmInfo = new DbScmInfo(dbClient, FILE);
    scmInfo.getForLines();
  }

  private void insertFileSource(DbFileSources.Data fileData) {
    dbTester.getDbClient().fileSourceDao().insert(new FileSourceDto()
      .setFileUuid(FILE_UUID)
      .setProjectUuid(PROJECT_UUID)
      .setSourceData(fileData));
  }

  private static void addLine(DbFileSources.Data.Builder dataBuilder, Integer line, String author, Long date, String revision) {
    dataBuilder.addLinesBuilder()
      .setLine(line)
      .setScmAuthor(author)
      .setScmDate(date)
      .setScmRevision(revision);
  }

}
