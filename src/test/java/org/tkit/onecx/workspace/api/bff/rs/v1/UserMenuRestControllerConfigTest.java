package org.tkit.onecx.workspace.api.bff.rs.v1;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.tkit.onecx.workspace.api.bff.rs.v1.AbstractTest.ADMIN;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.api.bff.rs.v1.controllers.MenuItemRestController;

import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.GetMenuItemsRequestDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.UserWorkspaceMenuStructureDTOV1;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@TestHTTPEndpoint(MenuItemRestController.class)
public class UserMenuRestControllerConfigTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @InjectMock
    PathConfig pathConfig;

    @Inject
    Config config;

    public static class ConfigProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @Mock
        PathConfig config() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(PathConfig.class);
        }
    }

    @BeforeEach
    void beforeEach() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(PathConfig.class);

        Mockito.when(pathConfig.shellMapping()).thenReturn(new PathConfig.ShellMappingConfig() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public String prefix() {
                return tmp.shellMapping().prefix();
            }
        });
    }

    @Test
    void getUserMenuWithModifiedPrefixTest() {
        final String TOKEN = keycloakClient.getAccessToken(ADMIN);
        String workspaceName = "testWorkspace";
        UserWorkspaceMenuRequest request = new UserWorkspaceMenuRequest();
        request.setToken("Bearer " + TOKEN);
        request.setMenuKeys(List.of("main-menu"));

        UserWorkspaceMenuStructure response = new UserWorkspaceMenuStructure();

        UserWorkspaceMenuItem menuItemInternal = new UserWorkspaceMenuItem();
        UserWorkspaceMenuItem menuItemExternal = new UserWorkspaceMenuItem();
        menuItemInternal.key("MAIN_MENU_INTERNAL").name("mainMenuInternal").position(1).url("/menuItem1").external(false);
        menuItemExternal.key("MAIN_MENU_EXTERNAL").name("mainMenuExternal").position(1).url("/menuItem2").external(true);
        response.setWorkspaceName(workspaceName);
        response.setMenu(List.of(menuItemInternal, menuItemExternal));

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

        Assertions.assertEquals("customPrefix/menuItem1", output.getMenu().get(0).getUrl());
        Assertions.assertEquals("/menuItem2", output.getMenu().get(1).getUrl());
        mockServerClient.clear("mock");
    }

}
