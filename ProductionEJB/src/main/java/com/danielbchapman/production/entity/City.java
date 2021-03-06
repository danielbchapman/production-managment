package com.danielbchapman.production.entity;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

/**
 * A city, like one that we live in.
 * 
 *************************************************************************** 
 * @author Daniel B. Chapman
 * @since Jun 16, 2011
 * @link http://www.theactingcompany.org
 *************************************************************************** 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name" , "stateOrTerritory"}))
public class City extends BaseEntity implements Comparable<City>
{
	private static final long serialVersionUID = 1L;

	private Hotel castHotel;
	private String country = "USA";
	private Hotel crewHotel;
	@OneToMany(cascade = { CascadeType.ALL }, targetEntity = Hospital.class, fetch = FetchType.EAGER)
	private Collection<Hospital> hospitals;
	@OneToMany(cascade = { CascadeType.ALL }, targetEntity = Hotel.class, fetch = FetchType.EAGER)
	private Collection<Hotel> hotels;
	private String name = "";
	private Hospital selectedHospital;
	private String stateOrTerritory = "";
	private String taxiServiceAddress = "";
	private String taxiServiceName = "";
	private String taxiServicePhone = "";
	@Getter
	@Setter
	@Column(length=64)
	private String timeZone = "Not Set";

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(City o)
	{
		if(o == null)
			return 1;
		if(o.getName() == null)
			return 1;
		if(getName() == null)
			return -1;

		return getName().compareTo(o.getName());
	}

	public Hotel getCastHotel()
	{
		return castHotel;
	}

	public String getCountry()
	{
		return country;
	}

	public Hotel getCrewHotel()
	{
		return crewHotel;
	}

	public Collection<Hospital> getHospitals()
	{
		return hospitals;
	}

	public Collection<Hotel> getHotels()
	{
		return hotels;
	}

	public String getName()
	{
		return name;
	}

	public Hospital getSelectedHospital()
	{
		return selectedHospital;
	}

	public String getStateOrTerritory()
	{
		return stateOrTerritory;
	}

	public String getTaxiServiceAddress()
	{
		return taxiServiceAddress;
	}

	public String getTaxiServiceName()
	{
		return taxiServiceName;
	}

	public String getTaxiServicePhone()
	{
		return taxiServicePhone;
	}

	public void setCastHotel(Hotel castHotel)
	{
		this.castHotel = castHotel;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public void setCrewHotel(Hotel crewHotel)
	{
		this.crewHotel = crewHotel;
	}

	public void setHospitals(Collection<Hospital> hospitals)
	{
		this.hospitals = hospitals;
	}

	public void setHotels(Collection<Hotel> hotels)
	{
		this.hotels = hotels;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSelectedHospital(Hospital selectedHospital)
	{
		this.selectedHospital = selectedHospital;
	}

	public void setStateOrTerritory(String stateOrTerritory)
	{
		this.stateOrTerritory = stateOrTerritory;
	}

	public void setTaxiServiceAddress(String taxiServiceAddress)
	{
		this.taxiServiceAddress = taxiServiceAddress;
	}

	public void setTaxiServiceName(String taxiServiceName)
	{
		this.taxiServiceName = taxiServiceName;
	}

	public void setTaxiServicePhone(String taxiServicePhone)
	{
		this.taxiServicePhone = taxiServicePhone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.danielbchapman.production.entity.BaseEntity#toString()
	 */
	@Override
	public String toString()
	{
		return name;
	}

	/**
	 * @return a combination of City + State/Territory
	 * 
	 */
	public String toStringDetailed()
	{
		return name + ", " + stateOrTerritory;
	}
}
