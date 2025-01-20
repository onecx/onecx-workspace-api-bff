package org.tkit.onecx.workspace.api.bff.rs.controllers;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.api.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.api.bff.rs.mappers.UserMenuMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.MenuItemApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.user.client.api.UserMenuInternalApi;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class MenuItemRestController implements MenuItemApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Context
    HttpHeaders headers;

    @Inject
    UserMenuMapper mapper;

    @Inject
    @RestClient
    UserMenuInternalApi userMenuClient;

    @Override
    public Response getMenuItems(GetMenuItemsRequestDTO getMenuItemsRequestDTO) {
        var token = headers.getRequestHeader(AUTHORIZATION).get(0);
        UserWorkspaceMenuRequest request = mapper.map(getMenuItemsRequestDTO, token);
        try (Response response = userMenuClient.getUserMenu(getMenuItemsRequestDTO.getWorkspaceName(), request)) {
            return Response.status(response.getStatus())
                    .entity(mapper.map(response.readEntity(UserWorkspaceMenuStructure.class))).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
