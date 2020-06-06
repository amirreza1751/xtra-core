package com.xtra.core.model;
import lombok.Data;

@Data
public class User {

    private long id;
    private String username;
    private String password;
    private String email;
    private String _2FASec;
    private boolean isBanned = false;
    private UserType userType;
    private Role role;
}
