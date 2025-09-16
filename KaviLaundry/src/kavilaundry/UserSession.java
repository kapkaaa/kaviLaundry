/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

public class UserSession {
    private static int currentUserId;
    private static String currentUsername;
    private static int currentRoleId;
    private static String currentRoleName;
    
    public static void setCurrentUser(int userId, String username, int roleId, String roleName) {
        currentUserId = userId;
        currentUsername = username;
        currentRoleId = roleId;
        currentRoleName = roleName;
    }
    
    public static int getCurrentUserId() { return currentUserId; }
    public static String getCurrentUsername() { return currentUsername; }
    public static int getCurrentRoleId() { return currentRoleId; }
    public static String getCurrentRoleName() { return currentRoleName; }
    
    public static void clearSession() {
        currentUserId = 0;
        currentUsername = null;
        currentRoleId = 0;
        currentRoleName = null;
    }
}