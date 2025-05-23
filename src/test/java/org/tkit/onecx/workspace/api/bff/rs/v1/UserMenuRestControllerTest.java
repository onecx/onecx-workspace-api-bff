package org.tkit.onecx.workspace.api.bff.rs.v1;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.api.bff.rs.v1.controllers.MenuItemRestController;

import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.GetMenuItemsRequestDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.ProblemDetailResponseDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.UserWorkspaceMenuStructureDTOV1;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuItemRestController.class)
class UserMenuRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getUserMenuTest() {
        final String TOKEN = keycloakClient.getAccessToken(ADMIN);
        String workspaceName = "testWorkspace";
        UserWorkspaceMenuRequest request = new UserWorkspaceMenuRequest();
        request.setToken("Bearer " + TOKEN);
        request.setMenuKeys(List.of("main-menu"));

        UserWorkspaceMenuStructure response = new UserWorkspaceMenuStructure();

        UserWorkspaceMenuItem menuItem = new UserWorkspaceMenuItem();
        UserWorkspaceMenuItem child = new UserWorkspaceMenuItem();
        child.name("mainMenuChild").key("MAIN_MENU_CHILD").position(2).url("/child");
        menuItem.key("MAIN_MENU").name("mainMenu").position(1).url("/menuItem1").children(List.of(child));
        response.setWorkspaceName(workspaceName);
        response.setMenu(List.of(menuItem));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/user/" + workspaceName + "/menu")
                        .withBody(JsonBody.json(request))
                        .withMethod(HttpMethod.POST))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        GetMenuItemsRequestDTOV1 requestDTO = new GetMenuItemsRequestDTOV1()
                .workspaceName(workspaceName).menuKeys(List.of("main-menu"));
        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UserWorkspaceMenuStructureDTOV1.class);

        Assertions.assertEquals(output.getMenu().size(), response.getMenu().size());

        mockServerClient.clear("mock");
    }

    @Test
    void getUserMenuWrongWorkspaceIdTest() {
        final String TOKEN = keycloakClient.getAccessToken(ADMIN);
        String workspaceName = "notFound";
        UserWorkspaceMenuRequest request = new UserWorkspaceMenuRequest();
        request.setToken("Bearer " + TOKEN);
        request.setMenuKeys(List.of("main-menu"));

        UserWorkspaceMenuStructure response = new UserWorkspaceMenuStructure();

        UserWorkspaceMenuItem menuItem = new UserWorkspaceMenuItem();
        UserWorkspaceMenuItem child = new UserWorkspaceMenuItem();
        child.name("mainMenuChild").key("MAIN_MENU_CHILD").position(2).url("/child");
        menuItem.key("MAIN_MENU").name("mainMenu").position(1).url("/menuItem1").children(List.of(child));
        response.setWorkspaceName(workspaceName);
        response.setMenu(List.of(menuItem));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/user/" + workspaceName + "/menu")
                        .withBody(JsonBody.json(request))
                        .withMethod(HttpMethod.POST))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        GetMenuItemsRequestDTOV1 requestDTO = new GetMenuItemsRequestDTOV1();
        requestDTO.workspaceName(workspaceName).menuKeys(List.of("main-menu"));
        given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        mockServerClient.clear("mock");
    }

    @Test
    void getUserMenuMissingWorkspaceNameTest() {
        final String TOKEN = keycloakClient.getAccessToken(ADMIN);

        GetMenuItemsRequestDTOV1 requestDTO = new GetMenuItemsRequestDTOV1();
        requestDTO.menuKeys(List.of("main-menu"));
        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTOV1.class);

        Assertions.assertNotNull(output);
    }
}
