package org.tkit.onecx.workspace.api.bff.rs.v1.mappers;

import jakarta.inject.Inject;

import org.mapstruct.*;
import org.tkit.onecx.workspace.api.bff.rs.v1.PathConfig;

import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.GetMenuItemsRequestDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.UserWorkspaceMenuItemDTOV1;
import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.UserWorkspaceMenuStructureDTOV1;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;

@Mapper
public abstract class UserMenuMapper {

    @Inject
    PathConfig pathConfig;

    private String preparedPrefix;

    public abstract UserWorkspaceMenuRequest map(GetMenuItemsRequestDTOV1 userWorkspaceMenuRequestDTO, String token);

    @Mapping(target = "removeMenuItem", ignore = true)
    public abstract UserWorkspaceMenuStructureDTOV1 map(UserWorkspaceMenuStructure userWorkspaceMenuStructure);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    abstract UserWorkspaceMenuItemDTOV1 map(UserWorkspaceMenuItem item);

    @BeforeMapping
    public void beforeMapping(@MappingTarget UserWorkspaceMenuStructureDTOV1 dto,
            UserWorkspaceMenuStructure userWorkspaceMenuStructure) {
        if (!pathConfig.shellMapping().prefix().endsWith("/")) {
            preparedPrefix = pathConfig.shellMapping().prefix() + "/";
        } else {
            preparedPrefix = pathConfig.shellMapping().prefix();
        }
    }

    @AfterMapping
    void afterMapping(@MappingTarget UserWorkspaceMenuItemDTOV1 itemDTOV1, UserWorkspaceMenuItem item) {
        if (pathConfig.shellMapping().enabled() && itemDTOV1.getExternal() != null
                && Boolean.FALSE.equals(itemDTOV1.getExternal())) {
            itemDTOV1.setUrl(mapPath(preparedPrefix, item.getUrl()));
        }
    }

    String mapPath(String prefix, String url) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return prefix + url;
    }
}
