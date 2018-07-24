package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import java.util.Set;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;

public class PermissionRequesManager {
    public static PermissionRequest getPermission(PermissionRequest.RequestStatus status, Set<User> parents,
                                                  User child, Group group, String message){
        PermissionRequest request = new PermissionRequest();
        request.setAction(status.toString());
        setsParents(parents, request);
        request.setStatus(WGServerProxy.PermissionStatus.PENDING);
        request.setRequestingUser(child);
        request.setGroupG(group);
//        PermissionRequest.Authorizor authority = new PermissionRequest.Authorizor();
//        authority.setUsers(parents);
//        request.setAuthorizors(authority);
        request.setMessage(message);
        return request;
    }

    private static void setsParents(Set<User> parents, PermissionRequest request) {
        User[] users = (User[]) parents.toArray();
        request.setUserA(users[0]);
        request.setUserB(users[1]);
    }
}
