package kinkmatch.api.dto;

import kinkmatch.api.model.Role;

import java.util.UUID;

public class UserResponse {
    public UUID id;
    public String email;
    public Role role;
}
