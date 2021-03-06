package com.danielbchapman.jboss.login;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

@Entity
public class User implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @Column(length=80, name="user")
  private String user;
  @Column(name="password")
  @Lob
  private byte[] password;
  @OneToMany(targetEntity=Role.class, fetch=FetchType.LAZY)
  private List<Role> roles;
  
  public String getUser()
  {
    return user;
  }
  public void setUser(String user)
  {
    this.user = user;
  }
  public byte[] getPassword()
  {
    return password;
  }
  public void setPassword(byte[] password)
  {
    this.password = password;
  }
  public List<Role> getRoles()
  {
    return roles;
  }
  public void setRoles(List<Role> roles)
  {
    this.roles = roles;
  }
}
