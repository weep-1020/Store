package com.weep.dto;

public class AdminUpdateUserRequest {
    private String username;
    private String email;
    private String phone;
    private String pic;
    private Integer status;
    private Long roleId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
