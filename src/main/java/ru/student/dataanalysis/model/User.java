package ru.student.dataanalysis.model;

public class User implements Identifiable<Long> {
    private Long id;
    private String login;
    private String passwordHash;
    private String displayName;

    public User() {
    }

    public User(Long id, String login, String passwordHash, String displayName) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    @Override
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    @Override
    public String toString() {
        return "User{id=" + id + ", login='" + login + "', displayName='" + displayName + "'}";
    }
}
