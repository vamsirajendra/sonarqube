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

package org.sonar.server.test.ws;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.web.UserRole;
import org.sonar.core.util.Uuids;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.MyBatis;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.component.ComponentDtoFunctions;
import org.sonar.server.test.index.CoveredFileDoc;
import org.sonar.server.test.index.TestIndex;
import org.sonar.server.user.UserSession;
import org.sonar.server.ws.WsUtils;
import org.sonarqube.ws.WsTests;

public class CoveredFilesAction implements TestsWsAction {

  public static final String TEST_ID = "testId";

  private final DbClient dbClient;
  private final TestIndex index;
  private final UserSession userSession;

  public CoveredFilesAction(DbClient dbClient, TestIndex index, UserSession userSession) {
    this.dbClient = dbClient;
    this.index = index;
    this.userSession = userSession;
  }

  @Override
  public void define(WebService.NewController controller) {
    WebService.NewAction action = controller.createAction("covered_files")
      .setDescription("Get the list of source files covered by a test. Require Browse permission on test file's project")
      .setSince("4.4")
      .setResponseExample(Resources.getResource(getClass(), "tests-example-covered-files.json"))
      .setHandler(this)
      .addPagingParams(100);

    action
      .createParam(TEST_ID)
      .setRequired(true)
      .setDescription("Test ID")
      .setExampleValue(Uuids.UUID_EXAMPLE_01);
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    String testId = request.mandatoryParam(TEST_ID);
    userSession.checkComponentUuidPermission(UserRole.CODEVIEWER, index.searchByTestUuid(testId).fileUuid());

    List<CoveredFileDoc> coveredFiles = index.coveredFiles(testId);
    Map<String, ComponentDto> componentsByUuid = buildComponentsByUuid(coveredFiles);

    WsTests.CoveredFilesResponse.Builder responseBuilder = WsTests.CoveredFilesResponse.newBuilder();
    if (!coveredFiles.isEmpty()) {
      for (CoveredFileDoc doc : coveredFiles) {
        WsTests.CoveredFilesResponse.CoveredFile.Builder fileBuilder = WsTests.CoveredFilesResponse.CoveredFile.newBuilder();
        fileBuilder.setId(doc.fileUuid());
        fileBuilder.setCoveredLines(doc.coveredLines().size());
        ComponentDto component = componentsByUuid.get(doc.fileUuid());
        if (component != null) {
          fileBuilder.setKey(component.key());
          fileBuilder.setLongName(component.longName());
        }

        responseBuilder.addFiles(fileBuilder);
      }
    }
    WsUtils.writeProtobuf(responseBuilder.build(), request, response);
  }

  private Map<String, ComponentDto> buildComponentsByUuid(List<CoveredFileDoc> coveredFiles) {
    List<String> sourceFileUuids = Lists.transform(coveredFiles, new CoveredFileToFileUuidFunction());
    DbSession dbSession = dbClient.openSession(false);
    List<ComponentDto> components;
    try {
      components = dbClient.componentDao().selectByUuids(dbSession, sourceFileUuids);
    } finally {
      MyBatis.closeQuietly(dbSession);
    }
    return Maps.uniqueIndex(components, ComponentDtoFunctions.toUuid());
  }

  private static class CoveredFileToFileUuidFunction implements Function<CoveredFileDoc, String> {
    @Override
    public String apply(@Nonnull CoveredFileDoc coveredFile) {
      return coveredFile.fileUuid();
    }
  }

}
