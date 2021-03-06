package org.theactingcompany.inventory.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * A simple base object to be extended that handles the ID etc...
 * 
 *************************************************************************** 
 * @author Daniel B. Chapman
 * @since Dec 28, 2010 2010
 * @link http://www.theactingcompany.org
 *************************************************************************** 
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable, Indentifiable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public String toString()
  {
    return this.getClass() + " ID:'" + id + "' " + super.toString();
  }
}
