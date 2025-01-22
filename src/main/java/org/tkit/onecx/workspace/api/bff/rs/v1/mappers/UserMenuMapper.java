package org.tkit.onecx.workspace.api.bff.rs.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.GetMenuItemsRequestDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.UserWorkspaceMenuItemDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.UserWorkspaceMenuStructureDTOV1;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;

@Mapper
public interface UserMenuMapper {

    UserWorkspaceMenuRequest map(GetMenuItemsRequestDTOV1 userWorkspaceMenuRequestDTO, String token);

    @Mapping(target = "removeMenuItem", ignore = true)
    UserWorkspaceMenuStructureDTOV1 map(UserWorkspaceMenuStructure userWorkspaceMenuStructure);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    UserWorkspaceMenuItemDTOV1 map(UserWorkspaceMenuItem item);
}
