package com.danielbchapman.production.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.*;

/**
 * A simple supertype to group a set of budgets by a single department
 * 
 */
@Entity
public class Budget implements Serializable
{

	private static final long serialVersionUID = 1L;

	@Temporal(value = TemporalType.TIMESTAMP)
	private Date date;
	@ManyToOne(targetEntity = Department.class)
	private Department department;
	@OneToMany(targetEntity=BudgetEntry.class, mappedBy="budget")
	private Collection<BudgetEntry> entries;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(length=50)
	private String name;
	@ManyToOne(targetEntity = Season.class)
	private Season season;
	private Double startingBudget;

	public Budget()
	{
		super();
	}

	public Date getDate()
	{
		return date;
	}

	public Department getDepartment()
	{
		return department;
	}

	
	public Collection<BudgetEntry> getEntries()
	{
		return entries;
	}

	public Long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Season getSeason()
	{
		return season;
	}

	public Double getStartingBudget()
	{
		return startingBudget;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public void setDepartment(Department department)
	{
		this.department = department;
	}

	public void setEntries(Collection<BudgetEntry> entries)
	{
		this.entries = entries;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}


	public void setSeason(Season season)
	{
		this.season = season;
	}


	public void setStartingBudget(Double startingBudget)
	{
		this.startingBudget = startingBudget;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return name;
	}
}
