package com.danielbchapman.production.beans;

import java.io.Serializable;
import java.util.ArrayList;

import javax.ejb.Remote;

import com.danielbchapman.production.entity.Employee;

/**
 * A simple accessor interface for Employee
 ***************************************************************************
 * @author Daniel B. Chapman 
 * @since Feb 4, 2011 2011
 * @link http://www.theactingcompany.org
 ***************************************************************************
 */
@Remote
public interface EmplyoeeDaoRemote extends Serializable
{
	static final long serialVersionUID = 1L;
  public void addEmployee(String name);
  public ArrayList<Employee> getEmployees();
}
