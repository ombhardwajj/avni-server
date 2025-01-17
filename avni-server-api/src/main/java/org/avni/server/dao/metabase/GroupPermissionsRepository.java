package org.avni.server.dao.metabase;

import org.avni.server.domain.metabase.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class GroupPermissionsRepository extends MetabaseConnector {

    public GroupPermissionsRepository(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

        public Group save(Group permissionsGroup) {
        String url = metabaseApiUrl + "/permissions/group";
        GroupPermissionsBody body = new GroupPermissionsBody(permissionsGroup.getName());
        HttpEntity<Map<String, Object>> entity = createJsonEntity(body);
        Group response = restTemplate.postForObject(url, entity, Group.class);
        return response;
    }

    public GroupPermissionsGraphResponse getPermissionsGraph() {
        String url = metabaseApiUrl + "/permissions/graph";
        return getForObject(url, GroupPermissionsGraphResponse.class);
    }

    public void updatePermissionsGraph(GroupPermissionsService permissions) {
        String url = metabaseApiUrl + "/permissions/graph";
        Map<String, Object> requestBody = permissions.getUpdatedPermissionsGraph();
        sendPutRequest(url, requestBody);
    }

    public List<GroupPermissionResponse> getAllGroups() {
        String url = metabaseApiUrl + "/permissions/group";
        GroupPermissionResponse[] response = getForObject(url, GroupPermissionResponse[].class);
        return Arrays.asList(response);
    }

    public void updateGroupPermissions(int groupId, int databaseId) {
        GroupPermissionsService groupPermissions = new GroupPermissionsService(getPermissionsGraph());
        groupPermissions.updatePermissions(groupId, databaseId);
        updatePermissionsGraph(groupPermissions);
    }


    public Group findOrCreateGroup(String name, int databaseId, int collectionId) {
        List<GroupPermissionResponse> existingGroups = getAllGroups();

        for (GroupPermissionResponse group : existingGroups) {
            if (group.getName().equals(name)) {
                return new Group(group.getName(), group.getId());
            }
        }

        Group newGroup = save(new Group(name));
        updateGroupPermissions(newGroup.getId(), databaseId);
        return newGroup;
    }
}
