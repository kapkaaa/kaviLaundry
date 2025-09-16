/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

/**
 *
 * @author Admin
 */
public class User {
    private int idUser;
    private String username;
    private String password;
    private int roleId;
    private String createdAt;
    
    // Constructors
    public User() {}
    
    public User(int idUser, String username, String password, int roleId, String createdAt) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
