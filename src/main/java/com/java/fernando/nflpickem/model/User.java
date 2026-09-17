package com.java.fernando.nflpickem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String email;

    private String username;
    private String displayName;
    private String name;

    public User() {}

    public User(String email, String displayName) {
        this.email = email != null ? email.toLowerCase().trim() : null;
        this.displayName = displayName;
        this.username = this.email;
        this.name = displayName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    public String getUsername() { return username != null ? username : email; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName != null ? displayName : name; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getName() { return displayName != null ? displayName : (username != null ? username : email); }
    public void setName(String name) { this.name = name; }
}