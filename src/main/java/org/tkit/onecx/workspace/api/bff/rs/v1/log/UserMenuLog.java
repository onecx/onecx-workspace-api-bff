package org.tkit.onecx.workspace.api.bff.rs.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.api.rs.external.v1.model.GetMenuItemsRequestDTOV1;

@ApplicationScoped
public class UserMenuLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, GetMenuItemsRequestDTOV1.class,
                        x -> GetMenuItemsRequestDTOV1.class.getSimpleName() + "[workspace:"
                                + ((GetMenuItemsRequestDTOV1) x).getWorkspaceName() + "]"));
    }
}
