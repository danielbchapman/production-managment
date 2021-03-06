package com.danielbchapman.production.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.danielbchapman.production.entity.City;
import com.danielbchapman.production.entity.Season;
import com.danielbchapman.production.entity.Venue;
import com.danielbchapman.production.entity.VenueLog;

/**
 * A bean for accessing venue information.
 */
@Stateless
public class VenueDao implements VenueDaoRemote
{
	private static final long serialVersionUID = 1L;
	//@PersistenceContext
  EntityManager em = EntityInstance.getEm();
  /**
   * Default constructor.
   */
  public VenueDao()
  {
  }
  @Override
  public void saveVenue(Venue venue)
  {
    EntityInstance.saveObject(venue);
    
  }
  @Override
  public Venue getVenue(String user)
  {
    //TODO Auto Generated Sub
    return null;
  }
  @Override
  public ArrayList<Venue> getVenues(Season production)
  {
    //TODO Auto Generated Sub
    return null;
  }
  @Override
  public File getRootGeneralFolder()
  {
    //TODO Auto Generated Sub
    return null;
  }
  @Override
  public Venue getVenue(Long id)
  {
  	if(id == null || id < 1)
  		return null;
  	
    Query q = em.createQuery("SELECT v FROM Venue v WHERE v.id = ?1");
    q.setParameter(1, id);
    
    return (Venue) q.getSingleResult();
  }
  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<Venue> getAllVenues()
  {
    Query q = em.createQuery("SELECT v FROM Venue v ORDER BY v.name");
    List<Venue> results = (List<Venue>)q.getResultList();
    ArrayList<Venue> ret = new ArrayList<Venue>();
    if(results != null)
      for(Venue v : results)
        ret.add(v);
    
    return ret;
  }
  @Override
  public void addLogEntry(String value, Venue venue)
  {
    VenueLog log = new VenueLog();
    log.setDate(new Date());
    log.setNotes(value);
    log.setVenue(venue);
    EntityInstance.saveObject(log);
    
  }
  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<VenueLog> getLogEntries(Venue venue)
  {
    Query q = em.createQuery("SELECT log FROM VenueLog log WHERE log.venue = ?1 ORDER BY log.date");
    q.setParameter(1, venue);
    
    List<VenueLog> results = (List<VenueLog>)q.getResultList();
    ArrayList<VenueLog> ret = new ArrayList<VenueLog>();
    if(results != null)
      for(VenueLog l : results)
        ret.add(l);
    
    return ret;
    
  }
  
	/* (non-Javadoc)
	 * @see com.danielbchapman.production.beans.VenueDaoRemote#getVenuesForCity(com.danielbchapman.production.entity.City)
	 */
	@Override
	public ArrayList<Venue> getVenuesForCity(City city)
	{
		return EntityInstance.getResultList("SELECT v FROM Venue v WHERE v.city = ?1", Venue.class, city);
	}

}
