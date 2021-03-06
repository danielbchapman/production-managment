package com.danielbchapman.production.web.schedule.beans;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.primefaces.component.schedule.Schedule;
import org.primefaces.event.DateSelectEvent;
import org.primefaces.event.ScheduleEntrySelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import com.danielbchapman.production.AbstractPrintController;
import com.danielbchapman.production.Utility;
import com.danielbchapman.production.beans.CalendarDaoRemote;
import com.danielbchapman.production.beans.SeasonDaoRemote;
import com.danielbchapman.production.beans.VenueDaoRemote;
import com.danielbchapman.production.dto.MonthDto;
import com.danielbchapman.production.entity.Day;
import com.danielbchapman.production.entity.Event;
import com.danielbchapman.production.entity.EventMapping;
import com.danielbchapman.production.entity.Performance;
import com.danielbchapman.production.entity.Performance.PerformanceEvent;
import com.danielbchapman.production.entity.PerformanceAdvance;
import com.danielbchapman.production.entity.PerformanceSchedule;
import com.danielbchapman.production.entity.Season;
import com.danielbchapman.production.entity.Venue;
import com.danielbchapman.production.entity.Week;
import com.danielbchapman.production.models.IndexedScheduleModel;
import com.danielbchapman.production.web.production.beans.AdministrationBean;
import com.danielbchapman.production.web.production.beans.SeasonBean;
import com.danielbchapman.production.web.schedule.beans.LocationBean.HotelWrapper;

/**
 * This is the backing logic for the schedule bean which is backed by the Primefaces Schedule
 * component. This is a separate application which allows for a different backing toolset from the
 * budgeting.
 * 
 *************************************************************************** 
 * @author Daniel B. Chapman
 * @since Jan 2, 2011 2011
 * @link http://www.theactingcompany.org
 *************************************************************************** 
 */
@ViewScoped
@ManagedBean(name="scheduleBean")
public class ScheduleBean implements Serializable
{
	private static final long serialVersionUID = 3L;
	private ArrayList<SelectItem> allSeasons;
	private CalendarDaoRemote calendarDao;
	private Selection currentSelection = Selection.CALENDAR;
	private Day day;
	private DayUI dayUi = new DayUI(new Day());
	private String eventEntityId;
	private EventMapping eventEntityRef;
	private HashMap<Long, HashSet<String>> tracking = new HashMap<Long, HashSet<String>>();

//	private ReentrantLock modelLock = new ReentrantLock();
	//@formatter:off
	private SelectItem[] hourItems = 
			new SelectItem[] 
					{ 
			new SelectItem(0, "00"),
			new SelectItem(1, "1"),
			new SelectItem(2, "2"),
			new SelectItem(3, "3"),
			new SelectItem(4, "4"),
			new SelectItem(5, "5"),
			new SelectItem(6, "6"),
			new SelectItem(7, "7"),
			new SelectItem(8, "8"),
			new SelectItem(9, "9"),
			new SelectItem(10, "10"),
			new SelectItem(11, "11"),
			new SelectItem(12, "12"),
			new SelectItem(13, "13"),
			new SelectItem(14, "14"),
			new SelectItem(15, "15"),
			new SelectItem(16, "16"),
			new SelectItem(17, "17"),
			new SelectItem(18, "18"),
			new SelectItem(19, "19"),
			new SelectItem(20, "20"),
			new SelectItem(21, "21"),
			new SelectItem(22, "22"),
			new SelectItem(23, "23") 
					};
	//@formatter:on

	// private Logger log = Logger.getLogger(ScheduleBean.class);
//	private Date loginDate = new Date();
	private SelectItem[] meridianItems = new SelectItem[] { new SelectItem("AM"),
			new SelectItem("PM"), };
	private SelectItem[] minuteItems = new SelectItem[] { new SelectItem(0, "00"),
			new SelectItem(15, "15"), new SelectItem(30, "30"), new SelectItem(45, "45") };
	private PerformanceSchedules performanceSchedules;

	private PerformanceUI performanceUi = new PerformanceUI(new Date(), null);

	private IndexedScheduleModel scheduleModelRef;
	private ArrayList<Week> scheduleModelWeeks; // This is a hook for debuging

	private SeasonDaoRemote seasonDao;

	private ComplexEntityScheduleEvent selectedEvent;

	private Season selectedSeason;

	private TimeUI timeUi = new TimeUI(new Date(), new Date());
	private Long tmpSeason;
	//@formatter:off
	private SelectItem[] travelItems = 
			new SelectItem[] 
					{
			new SelectItem(TravelItems.NONE.toString()), 
			new SelectItem(TravelItems.HOTEL.toString()),
			new SelectItem(TravelItems.OVERNIGHT.toString()),
			new SelectItem(TravelItems.TRAVEL_DAY.toString()) 
					};
	//@formatter:on	

	@Getter 
	private SeasonUI seasonUi = new SeasonUI();
	
//	private Boolean userInDaylightSavings;
	private VenueDaoRemote venueDao;
	@Getter
	@Setter
	private Schedule scheduleBinding;

	private boolean virtualEvent;
	@Getter
	@Setter
	private CalendarPrintController calendarPrintController = new CalendarPrintController();
	@Getter
	@Setter
	private MonthPrintController monthController = new MonthPrintController();
	
	public TimeZone getServerTimezone()
	{
		//Hooked
		return TimeZone.getDefault();
	}

	public String getServerLocale()
	{
		return Locale.US.getDisplayName();
	}
	
	public void cancel(ActionEvent evt)
	{
		System.out.println("CANCELING...");
		day = new Day();
		eventSelect(new Event(), null);
	}

	public void confirmSeason(ActionEvent evt)
	{
		if(tmpSeason != null)
		{
			selectedSeason = getSeasonDao().getSeason(tmpSeason);
			scheduleModelRef = null;
			performanceSchedules = null;
		}
	}

	public void doSelectCalendar(ActionEvent evt)
	{
		currentSelection = Selection.CALENDAR;
	}

	public void doSelectCities(ActionEvent evt)
	{
		currentSelection = Selection.CITY;
	}

	public void doSelectPerformances(ActionEvent evt)
	{
		currentSelection = Selection.PERFORMANCES;
	}

	public ArrayList<SelectItem> getAllSeasons()
	{
		if(allSeasons == null)
		{
			ArrayList<Season> tmpSeasons = getSeasonDao().getSeasons();
			allSeasons = new ArrayList<SelectItem>();

			for(Season p : tmpSeasons)
				allSeasons.add(new SelectItem(p.getId(), p.getName()));

			if(tmpSeasons != null && tmpSeasons.size() > 0)
			{
				selectedSeason = Utility.getBean(SeasonBean.class).getSeason();
				tmpSeason = selectedSeason.getId();
			}

		}
		return allSeasons;
	}

	public Locale getBaseLocale()
	{
		return Locale.US;
	}

	public Day getDay()
	{
		return day;
	}

	public DayUI getDayUi()
	{
		return dayUi;
	}

	public EventMapping getEventEntity()
	{
		return eventEntityRef;
	}

	public SelectItem[] getHourItems()
	{
		return hourItems;
	}

	public SelectItem[] getMeridianItems()
	{
		return meridianItems;
	}

	public SelectItem[] getMinuteItems()
	{
		return minuteItems;
	}

	public TimeUI getNewTimeUiInstance(Date start)
	{
		Calendar plus2 = Calendar.getInstance();
		plus2.setTime(start);
		plus2.add(Calendar.HOUR, 2);
		TimeUI ret = new TimeUI(start, plus2.getTime());
		return ret;
	}

	public PerformanceSchedules getPerformanceSchedules()
	{
		if(performanceSchedules == null)
			performanceSchedules = new PerformanceSchedules();

		return performanceSchedules;
	}

	/**
	 * @param season
	 *          the season to search
	 * @return a list of performances for this season
	 */
	public ArrayList<Performance> getPerformancesForSeason(Season season)
	{
		return getCalendarDao().getPerformances(season);
	}

	/**
	 * @see com.danielbchapman.production.beans.CalendarDaoRemote#getPerformances(Season, Venue)
	 * @param seaon
	 *          the season
	 * @param venue
	 *          the venue
	 * @return
	 */
	public ArrayList<Performance> getPerformancesForSeasonAndVenue(Season season, Venue venue)
	{
		return getCalendarDao().getPerformances(season, venue);
	}

	public PerformanceUI getPerformanceUi()
	{
		if(performanceUi == null)
			performanceUi = new PerformanceUI(new Date(), null);
		return performanceUi;
	}

	public synchronized IndexedScheduleModel getScheduleModel()
	{
		
		System.out.println("--Model method called");
		if(scheduleModelRef != null)
			return scheduleModelRef;
		
//		long start = System.currentTimeMillis();
//		System.out.println("--FULL MODEL REFRESH: ");
//		synchronized(ScheduleBean.this)
//		{
//			modelLock.lock();
		IndexedScheduleModel prepareRef = new IndexedScheduleModel();	
			
		try
		{
			boolean companySecurity = Utility.getBean(LoginBean.class).isCompanyMember(); 
			scheduleModelWeeks = getCalendarDao().getAllWeeks(selectedSeason);

			for(Week w : scheduleModelWeeks)
			{
				ArrayList<Day> days = getCalendarDao().getActiveDaysForWeek(w);
				for(Day day : days)
				{
				  //FIXME HACK
				  //Update time by 12 hours
				  day.setDate(Utility.addHour(day.getDate(), 12));
					renderDay(day, prepareRef, null);

					ArrayList<EventMapping> events = getCalendarDao().getEventsAndPerformancesForDay(day);
					for(EventMapping e : events)
					{
					  //FIXME HACK
					  Date start = e.getStart();
					  Date end = e.getEnd();
					  int offset = -4;
					  
					  if(e.getStart() != null)
					    e.setStart(Utility.addHour(start, offset));
					  
            if(e.getEnd() != null)
              e.setEnd(Utility.addHour(end, offset));
            
						if(!companySecurity)
							if(!e.isPublicEvent() && !e.isPerformance())
								continue;
						
						if(e instanceof Performance)
							renderPerformance(day, (Performance) e, prepareRef, companySecurity);
						else
						{
							ComplexEntityScheduleEvent se = createEvent(e, null);
							renderEvent(day, se, prepareRef);
						}
					}
				}
			}
			scheduleModelRef = prepareRef;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
		
		return getScheduleModel();
	}

	/**
	 * @return the scheduleModelWeeks
	 */
	public ArrayList<Week> getScheduleModelWeeks()
	{
		return scheduleModelWeeks;
	}

	public ScheduleEvent getSelectedEvent()
	{
		return selectedEvent;
	}

	public Season getSelectedSeason()
	{
		if(selectedSeason == null)
			selectedSeason = Utility.getBean(SeasonBean.class).getSeason();
		
		if(selectedSeason == null)
			selectedSeason = Utility.getBean(SeasonBean.class).getDefaultSeason();
		
		return selectedSeason;
	}

	public TimeUI getTimeUi()
	{
		return timeUi;
	}

	public Long getTmpSeason()
	{
		return tmpSeason;
	}

	public SelectItem[] getTravelItems()
	{
		return travelItems;
	}

	public boolean isDayEvent()
	{
		if(selectedEvent == null || selectedEvent.getBackingEntity() == null)
			return false;

		if(selectedEvent.isAllDay())
			return true;
		else
			return false;
	}

	public boolean isEvent()
	{
		if(selectedEvent == null || selectedEvent.getBackingEntity() == null)
			return false;

		if(selectedEvent.isAllDay())
			return false;
		else
			return true;
	}

	public boolean isExistingDay()
	{
		if(day == null || day.getId() == null)
			return false;
		return true;
	}

	public boolean isExistingEvent()
	{
		if(eventEntityRef == null || eventEntityRef.getId() == null)
			return false;
		return true;
	}

	public boolean isNewEvent()
	{
		if(selectedEvent == null)
			return false;

		if(selectedEvent.getBackingEntity() == null)
			return true;
		else
			return false;
	}

	public boolean isSelectedCalendar()
	{
		return Selection.CALENDAR == currentSelection;
	}

	public boolean isSelectedCities()
	{
		return Selection.CITY == currentSelection;
	}

	public boolean isSelectedPerformances()
	{
		return Selection.PERFORMANCES == currentSelection;
	}

	public boolean isVirtualEvent()
	{
		return virtualEvent;
	}

	public void onDateSelect(DateSelectEvent selectEvent)
	{
		selectedEvent = null;
		virtualEvent = false;// Allow Selection
		Date selection = selectEvent.getDate();
		selection = Utility.addHour(selection, 4);
		performanceUi = new PerformanceUI(selection, null);

		if(selectEvent != null)// Debugging what is passed as far as "date"
		{
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss Z");
			FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_INFO, "Date Information", "@Raw(" + df.format(selection) + ")");
			addMessage(error);
		}

		if(!getCalendarDao().dayExists(selection, selectedSeason))
		{
			selectedEvent = new ComplexEntityScheduleEvent("[NO DESCRIPTION]", selection, selection,
					true);
			Day unsavedDay = new Day();
			unsavedDay.setDate(selection);
			selectedEvent.setBackingEntity(unsavedDay);// This must be saved when confirmed.
			day = unsavedDay;
			dayUi = new DayUI(day);
		}
		else
		{
			selectedEvent = new ComplexEntityScheduleEvent("[NO DESCRIPTION]", selection, selection,
					false);
			Event unsavedEvent = new Event();

			// by definition this day exists
			Day backingDay = getCalendarDao().getOrCreateDay(selection, selectedSeason);
			unsavedEvent.setDay(backingDay);

			selectedEvent.setBackingEntity(unsavedEvent);
			eventSelect(unsavedEvent, selectedEvent.getId());

			Calendar tmp = Calendar.getInstance();
			tmp.setTime(selection);

			if(tmp.get(Calendar.HOUR_OF_DAY) == 0)
			{
				tmp.setTime(new Date());
				tmp.set(Calendar.MINUTE, 0);
			}

			timeUi = getNewTimeUiInstance(tmp.getTime());
			performanceUi.setCityId(backingDay.getCastLocation() == null ? null : backingDay
					.getCastLocation().getId());
		}
	}

	public void onEventSelect(ScheduleEntrySelectEvent selectEvent)
	{
		virtualEvent = false;
		selectedEvent = (ComplexEntityScheduleEvent) selectEvent.getScheduleEvent();

		if(selectedEvent == null)// Testing
		{
			FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to Load",
					"Unable to load the event, the model returned a null value. Please select your event again.");
			addMessage(error);
			return;
		}

		if(selectedEvent.getBackingEntity() instanceof Performance.PerformanceEvent)
		{
			virtualEvent = true;// Show the "virtualEvent dialog"
			return;
		}

		if(selectedEvent.getBackingEntity() instanceof Day)
		{
			day = (Day) selectedEvent.getBackingEntity();
			dayUi = new DayUI(day);
		}

		if(selectedEvent.getBackingEntity() instanceof Event)
		{
			eventSelect((Event) selectedEvent.getBackingEntity(), selectedEvent.getId());
			eventSelect(selectedEvent);
			timeUi = new TimeUI(eventEntityRef.getStartDate(), eventEntityRef.getEndDate());
		}
//I removed an else... Performance subclasses event
		
		if(selectedEvent.getBackingEntity() instanceof Performance)
		{
			eventSelect(selectedEvent);
			Performance p = (Performance) selectedEvent.getBackingEntity();

			if(p.getId() == null)
				performanceUi = new PerformanceUI(p.getStartDate(), null);
			else
				performanceUi = new PerformanceUI(p.getStartDate(), p);

			performanceUi.setCrewCall(p.isCrewCall());
			performanceUi.setFightCall(p.isFightCall());
			performanceUi.setStrike(p.isStrike());
			performanceUi.setTalkback(p.isTalkback());
			/* Create a reference to the city */
			performanceUi.cityId = p.getDay().getCastLocation() == null ? null : p.getDay()
					.getCastLocation().getId();
			performanceUi.setVenueId(p.getVenue() == null ? null : p.getVenue().getId());
			performanceUi.getVenueItems();
		}
	}

	public void removeDay(ActionEvent evt)
	{
		if(Utility.getBean(LoginBean.class).isScheduler())
		{
			Long id = day.getId().longValue();
			getCalendarDao().removeItem(day);
			
			deleteEvents(id, getScheduleModel());
					
			day = new Day();
			eventSelectClear();				
		}
	}

	public void removeEvent(ActionEvent evt)
	{
		if(Utility.getBean(LoginBean.class).isScheduler())
		{
			Long id = eventEntityRef.getDay().getId();
			getCalendarDao().removeItem(eventEntityRef);
			
			HashSet<String> ids = tracking.get(id);
			ids.remove(eventEntityId);
			tracking.put(id, ids);
			scheduleModelRef.deleteEvent(selectedEvent);
			day = new Day();
			eventSelectClear();
		}
	}

	public void removePerformance(ActionEvent evt)
	{
		if(Utility.getBean(LoginBean.class).isScheduler())
		{
			
			Long dayId = eventEntityRef.getDay().getId();
			getCalendarDao().removeItem(eventEntityRef);
			
			HashSet<String> events = tracking.get(dayId);
			HashSet<String> kill = new HashSet<String>();
			IndexedScheduleModel local = getScheduleModel();
			
			for(String id : events)
			{
				ComplexEntityScheduleEvent ref = (ComplexEntityScheduleEvent) local.getEvent(id);
				if(!(ref == null))
				{
					Object ent = ref.getBackingEntity();
					if(ent instanceof Performance || ent instanceof PerformanceEvent)
						kill.add(id);					
				}
			}
			
			for(String s : kill)
				local.deleteEvent(s);

			day = new Day();
			eventSelectClear();			
		}
	}

	public void saveDay(ActionEvent evt)
	{
		System.out.println("Save Day Called");
		if(evt != null && day != null && day.getDate() != null && Utility.getBean(LoginBean.class).isScheduler())
		{

			System.out.println("Saving Day-> " + day + "\n\t ->" + dayUi);
			Date date = selectedEvent.getStartDate();
			Week week = getCalendarDao().getOrCreateWeek(date, selectedSeason);

			Day ref = getCalendarDao().getOrCreateDay(date, week);

			/*
			 * Duplicates were being created, we should use a DTO in this case, but since the entity is
			 * the DTO. . .
			 */
			LocationBean location = (LocationBean) Utility.getBean("locationBean");

			ref.setCastLocation(location.getCity(dayUi.castCity));
			ref.setCrewLocation(location.getCity(dayUi.crewCity));
			ref.setCastHotel(location.getHotel(dayUi.castHotel));
			ref.setCrewHotel(location.getHotel(dayUi.crewHotel));
			ref.setCastTravel(day.getCastTravel());
			ref.setCrewTravel(day.getCrewTravel());
			ref.setLabel(day.getLabel());
			ref.setMilageInformation(day.getMilageInformation());
			ref.setNotes(day.getNotes());
			ref.setTheaterInformation(day.getTheaterInformation());

			/* The spaghetti is strong here */
			if(!TravelItems.HOTEL.toString().equals(ref.getCastTravel()))
				ref.setCastHotel(null);

			if(!TravelItems.HOTEL.toString().equals(ref.getCrewTravel()))
				ref.setCrewHotel(null);

			getCalendarDao().saveDay(ref);
			
			renderDay(ref, getScheduleModel(), selectedEvent.getId());
			day = new Day();
			dayUi = new DayUI(day);
			eventSelectClear();
		}
		else
		{
			System.out.println("There was an issue saving the day-> " + day);
		}
	}

	public void saveEvent(ActionEvent evt)
	{
		System.out.println("Save Event Called");
		if(eventEntityRef != null && Utility.getBean(LoginBean.class).isScheduler())
		{
			// Date date = selectedEvent.getStartDate();//BUG - > Use the hard reference
			EventMapping tmpEvent = (EventMapping) selectedEvent.getBackingEntity();
			Day assignedDay = tmpEvent.getDay();
			Date date = assignedDay.getDate();// == null? selectedEvent.getStartDate() :
			// assignedDay.getDate();

			if(date == null)// (!calendarDao.dayExists(date, selectedProduction))
			{
				FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to Save Event",
						"There is no day registered for '" + date
						+ "' please create the day, then save an event.");
				addMessage(error);
				return;
			}

			// Week week = calendarDao.getOrCreateWeek(date, selectedProduction);
			// Day d = calendarDao.getOrCreateDay(date, week);

			eventEntityRef.setDay(assignedDay);
			eventEntityRef.setStart(timeUi.getStartTime(assignedDay.getDate()));
			Date endTime = timeUi.getEndTime(assignedDay.getDate());

			if(endTime.before(eventEntityRef.getStartDate()))
			{
				Calendar tmpCal = Calendar.getInstance();
				tmpCal.setTime(eventEntityRef.getStartDate());

				if(tmpCal.get(Calendar.HOUR) > 22)
					tmpCal.add(Calendar.HOUR, 2);
				else
				{
					tmpCal.set(Calendar.HOUR_OF_DAY, 23);
					tmpCal.set(Calendar.MINUTE, 45);
				}

				endTime = tmpCal.getTime();
			}

			eventEntityRef.setEnd(endTime);
			eventEntityRef.setDay(assignedDay);
			selectedEvent.setEndDate(endTime);
			selectedEvent.setStartDate(timeUi.getStartTime(assignedDay.getDate()));
			selectedEvent.setBackingEntity(eventEntityRef);
			getCalendarDao().saveEvent(eventEntityRef);
			Utility.raiseInfo("Event Updated", eventEntityRef.getDescription());
			
			renderEvent(assignedDay, createEvent(eventEntityRef, selectedEvent.getId()), getScheduleModel(), null);
		}

		day = new Day();
		eventSelectClear();
	}

	public void savePerformance(ActionEvent evt)
	{
		if(eventEntityRef != null && Utility.getBean(LoginBean.class).isScheduler())
		{
			Performance tmpPerf;
			PerformanceSchedule schedule = getCalendarDao().getPerformanceSchedule(
					getPerformanceSchedules().getEditableId());

			if(selectedEvent.getBackingEntity() instanceof Performance)
				tmpPerf = (Performance) selectedEvent.getBackingEntity();
			else
			{
				tmpPerf = new Performance();
				Event tmpEvent = (Event) selectedEvent.getBackingEntity();
				tmpPerf.setDay(tmpEvent.getDay());
				tmpPerf.setCast(true);
				tmpPerf.setCrew(true);
				tmpPerf.setDescription(schedule.getName());
				tmpPerf.setPerformanceSchedule(schedule);
			}

			Day assignedDay = tmpPerf.getDay();

			if(assignedDay.getDate() == null)// (!calendarDao.dayExists(date, selectedProduction))
			{
				FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to Save Event",
						"There is no day registered for '" + assignedDay
						+ "' please create the day, then save an event.");
				addMessage(error);
				return;
			}
			Venue venue = performanceUi.getVenueId() == null ? null : getVenueDao().getVenue(
					performanceUi.getVenueId());

			tmpPerf.setDay(assignedDay);
			tmpPerf.setStart(performanceUi.getStartTime(assignedDay.getDate()));
			tmpPerf.setCrewCall(performanceUi.isCrewCall());
			tmpPerf.setFightCall(performanceUi.isFightCall());
			tmpPerf.setTalkback(performanceUi.isTalkback());
			tmpPerf.setStrike(performanceUi.isStrike());
			tmpPerf.setVenue(venue);

			Calendar endTimeCal = Calendar.getInstance();
			endTimeCal.setTime(tmpPerf.getStart());
			endTimeCal.add(Calendar.MINUTE, schedule.getPerformanceLength());
			tmpPerf.setEnd(endTimeCal.getTime());
			tmpPerf.setDay(assignedDay);

			getCalendarDao().savePerformance(tmpPerf);
			Utility.raiseInfo("Performance Saved", tmpPerf.getDescription());
			
			renderPerformance(assignedDay, tmpPerf, getScheduleModel(), true);
		}

		day = new Day();
		eventSelectClear();
	}

	public void setDay(Day day)
	{
		this.day = day;
	}

	public void setEventEntity(Event event)
	{
		this.eventEntityRef = event;
	}

	public void setSelectedEvent(ScheduleEvent selectedEvent)
	{
		this.selectedEvent = (ComplexEntityScheduleEvent) selectedEvent;
	}

	public void setSelectedSeason(Season selectedProduction)
	{
		this.selectedSeason = selectedProduction;
	}

	public void setTmpSeason(Long tmpSeason)
	{
		this.tmpSeason = tmpSeason;
	}
	
	public void testMonths(ActionEvent evt)
	{
		Calendar cStart = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cStart.setTime(new Date());
		cEnd.setTime(new Date());
		cStart.set(Calendar.MONTH, 7);
		cEnd.set(Calendar.MONTH, 11);
		ArrayList<MonthDto> months = getCalendarDao().getMonths(
				cStart.getTime(), 
				cEnd.getTime(),
				Utility.getBean(SeasonBean.class).getSeason());
		
		System.out.println("Start " + cStart.getTime() + " End " + cEnd.getTime());
		for(MonthDto dto : months)
			System.out.println(dto);
	}

	private ComplexEntityScheduleEvent createEvent(EventMapping mapping, String id)
	{
		ComplexEntityScheduleEvent se;
		if(mapping.getEnd() == null)
		{
			Calendar c = Calendar.getInstance();
			c.setTime(mapping.getStartDate());
			c.add(Calendar.HOUR, 2);
			se = new ComplexEntityScheduleEvent(
					mapping.getDescription(), 
					mapping.getStartDate(), 
					c.getTime(), 
					false);							
		}
		else
			se = new ComplexEntityScheduleEvent(
					mapping.getDescription(), 
					mapping.getStartDate(), 
					mapping.getEndDate(), 
					false);

		se.setBackingEntity(mapping);
		se.setId(id);
		
		return se;
	}
	private void renderPerformance(Day day, Performance perf, ScheduleModel model, boolean all)
	{
		ComplexEntityScheduleEvent se = createEvent(perf, null);

		if(all && seasonUi.details)
		{
			ArrayList<Performance.PerformanceEvent> performanceEvents = perf.getEventSequence();
			for(Performance.PerformanceEvent seq : performanceEvents)
			{
				ComplexEntityScheduleEvent sub = 
						new ComplexEntityScheduleEvent( 
								seq.getDescription(), 
								seq.getStartDate(), 
								seq.getEndDate(), 
								false);
				
				sub.setBackingEntity(seq);
				renderEvent(day, sub, model, "eventPerformanceLocked");
			}				
		}

		renderEvent(day, se, model, "eventPerformance");					
	}
	
	private void renderEvent(Day day, ComplexEntityScheduleEvent event, ScheduleModel model)
	{
		renderEvent(day, event, model, null);
	}
	
	private void renderEvent(Day day, ComplexEntityScheduleEvent event, ScheduleModel model, String styleClass)
	{
		if(styleClass == null)
		{
			styleClass = "none";
			
			if(event.getBackingEntity() instanceof EventMapping)
			{
				EventMapping map = (EventMapping) event.getBackingEntity();
				
				if(map.isCast() && !map.isCrew())
				{
					if(!seasonUi.isCast())
						return;
					styleClass = "eventScheduleCast";
				}
					
				if(!map.isCast() && map.isCrew())
				{
					if(!seasonUi.isCrew())
						return;
					styleClass = "eventScheduleCrew";
				}
					
				if(map.isCast() && map.isCrew())
					styleClass = "eventSchedule";
				
				if(!map.isCast() && !map.isCrew())
					styleClass = "eventSchedule";	
			}
			
		}
		event.setStyleClass(styleClass);
		if(event.getId() == null)
			model.addEvent(event);
		else
			model.updateEvent(event);
		
		if(tracking.get(day.getId()) == null)
				tracking.put(day.getId(), new HashSet<String>());
		
		tracking.get(day.getId()).add(event.getId());
	}
	
	private void renderDay(Day day, ScheduleModel model, String eventId)
	{
		ComplexEntityScheduleEvent dayEvent =
				new ComplexEntityScheduleEvent(
						"CAST: " + day.getCastLocation() 
						+ " CREW: " + day.getCrewLocation(),
						day.getDate(), 
						day.getDate(),
						true
						);

		dayEvent.setId(eventId);
		dayEvent.setBackingEntity(day);
		renderEvent(day, dayEvent, model, "eventDay");
	}
	
	private void deleteEvents(Long dayId, IndexedScheduleModel model)
	{
		HashSet<String> events = tracking.get(dayId);
		if(events != null)
			for(String id : events)
				model.deleteEvent(id);
		
		tracking.remove(dayId);
	}
	
	private void eventSelect(ComplexEntityScheduleEvent event)
	{
		if(event.getBackingEntity() instanceof Day)
		{
			Utility.raiseError("Unable to select Day" , "The method 'eventSelect' is valid only for EventMappings");
			return;
		}
			
		eventSelect((EventMapping) event.getBackingEntity(), event.getId());
	}
	
	private void eventSelect(EventMapping map, String id)
	{
		eventEntityRef = map;
		eventEntityId = id;
	}
	
	private void eventSelectClear()
	{
		eventEntityRef = new Event();
		eventEntityId = null;
		selectedEvent = null;
	}
	
	private void addMessage(FacesMessage message)
	{
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	private CalendarDaoRemote getCalendarDao()
	{
		if(calendarDao == null)
			calendarDao = Utility.getObjectFromContext(CalendarDaoRemote.class,
					Utility.Namespace.PRODUCTION);

		return calendarDao;
	}

	@SuppressWarnings("unused")
  private TimeZone getClientTimeZone()
	{
		LoginBean login = Utility.getBean(LoginBean.class);
		TimeZone zone = login.getTimeZone();
		return zone; // client zone;
	}

	private SeasonDaoRemote getSeasonDao()
	{
		if(seasonDao == null)
			seasonDao = Utility.getObjectFromContext(SeasonDaoRemote.class, Utility.Namespace.PRODUCTION);

		return seasonDao;
	}

	private VenueDaoRemote getVenueDao()
	{
		if(venueDao == null)
			venueDao = Utility.getObjectFromContext(VenueDaoRemote.class, Utility.Namespace.PRODUCTION);

		return venueDao;
	}

	@Data
	public class SeasonUI implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private boolean cast = true;
		private boolean crew = true;
		private boolean details = true;
	}
	@Data
	@EqualsAndHashCode(callSuper = false)
	public class MonthPrintController extends AbstractPrintController
	{
		private Date startDate;
		private Date endDate;
		private boolean printAll = true;
		private boolean cast = true;
		private boolean crew = true;
		private boolean details = true;
		
		public MonthPrintController()
		{
			super("schedules", "sub", "weekly", "tour", "schedule");
		}

		private static final long serialVersionUID = 1L;

		@Override
		protected Collection<MonthDto> getData()
		{
			Season season = Utility.getBean(ScheduleBean.class).getSelectedSeason();
			
			Collection<MonthDto> months = getCalendarDao().getMonths(startDate, endDate, season); 
			return months;
		}

		@Override
		protected Map<String, Object> getParameters()
		{
			HashMap<String, Object> params = new HashMap<String, Object>();

			File root = new File(Utility.getBean(AdministrationBean.class).getReportingDocumentRoot());
			//@formatter:off
			String path = new File(
						root.getAbsoluteFile() 
						+ File.separator 
						+ "schedules"
						+ File.separator
					).getAbsolutePath() + File.separator;
			//@formatter:on
			params.put("FILE_PATH", path);
			params.put("SUBREPORT_DIR", path);
			params.put("PRINT_CAST", new Boolean(cast));
			params.put("PRINT_CREW", new Boolean(crew));
			params.put("PRINT_DETAILS", new Boolean(details));
			
			return params;
		}

		@Override
		protected String getReportName()
		{
			SimpleDateFormat df = new SimpleDateFormat("MM-dd-yy");
			return "Monthy-" + df.format(new Date()) ;
		}
	}
	
	@Data
	@EqualsAndHashCode(callSuper = false)
	public class CalendarPrintController extends AbstractPrintController
	{
		private Date startDate;
		private Date endDate;
		private boolean printAll = true;
		private boolean cast = true;
		private boolean crew = true;
		private boolean details = true;
		
		public CalendarPrintController()
		{
			super("schedules", "sub", "_daily", "monthly");
		}

		private static final long serialVersionUID = 1L;

		@Override
		protected Collection<Week> getData()
		{
			ArrayList<Week> weeks = null;
			Season season = Utility.getBean(ScheduleBean.class).getSelectedSeason();

			if(printAll || startDate == null || endDate == null)
				weeks = getCalendarDao().getAllWeeks(season);
			else
				weeks = getCalendarDao().getWeeksInRange(startDate, endDate, season, false);
			
			/* Trims empty weeks */
			for(int i = 0; i < weeks.size(); i++)
				if(weeks.get(i).isEmpty())
					weeks.remove(i);
				else
					break;
			
			for(int i = weeks.size() -1 ; i >= 0; i--)
				if(weeks.get(i).isEmpty())
					weeks.remove(i);
				else
					break;
			return weeks;
		}

		@Override
		protected Map<String, Object> getParameters()
		{
			HashMap<String, Object> params = new HashMap<String, Object>();

			File root = new File(Utility.getBean(AdministrationBean.class).getReportingDocumentRoot());
			//@formatter:off
			String path = new File(
						root.getAbsoluteFile() 
						+ File.separator 
						+ "schedules"
						+ File.separator
					).getAbsolutePath() + File.separator;
			//@formatter:on
			params.put("FILE_PATH", path);
			params.put("SUBREPORT_DIR", path);
			params.put("PRINT_CAST", new Boolean(cast));
			params.put("PRINT_CREW", new Boolean(crew));
			params.put("PRINT_DETAILS", new Boolean(details));
			
			return params;
		}

		@Override
		protected String getReportName()
		{
			SimpleDateFormat df = new SimpleDateFormat("MM-dd-yy");
			return "Weekly-" + df.format(new Date()) ;
		}
		
	}
	
	@Data
	@EqualsAndHashCode(callSuper=false)
	public class ComplexEntityScheduleEvent extends DefaultScheduleEvent implements Serializable
	{
		private static final long serialVersionUID = 3L;
		private Object backingEntity;

		public ComplexEntityScheduleEvent(String title, Date start, Date end, boolean allDay)
		{
			super(title, start, end, allDay);
		}
	}

	@Data
	public class DayUI implements Serializable
	{
		private static final long serialVersionUID = 3L;
		private Long castCity;
		private Long castHotel;
		private ArrayList<SelectItem> castHotels;
		private Long crewCity;
		private Long crewHotel;
		private ArrayList<SelectItem> crewHotels;
		private boolean showCastHotel;
		private boolean showCrewHotel;

		private UIComponent bindingCrewHotels;
		private UIComponent bindingCastHotels;

		public DayUI(Day day)
		{
			if(day != null)
			{
				if(day.getCastLocation() != null)
					castCity = day.getCastLocation().getId();

				if(day.getCrewLocation() != null)
					crewCity = day.getCrewLocation().getId();

				if(TravelItems.HOTEL.toString().equals(day.getCastTravel()))
					showCastHotel = true;
				else
					showCastHotel = false;

				if(TravelItems.HOTEL.toString().equals(day.getCrewTravel()))
					showCrewHotel = true;
				else
					showCrewHotel = false;

				castHotel = day.getCastHotel() == null ? null : day.getCastHotel().getId();
				crewHotel = day.getCrewHotel() == null ? null : day.getCrewHotel().getId();
			}

		}

		public ArrayList<SelectItem> getCastHotels()
		{
			if(castHotels == null)
				if(castCity != null)
				{
					castHotels = new ArrayList<SelectItem>();
					ArrayList<HotelWrapper> tmpHotels = Utility.getBean(LocationBean.class).getHotelsForCity(
							castCity);
					for(HotelWrapper h : tmpHotels)
						castHotels.add(new SelectItem(h.getHotel().getId(), h.getHotel().getName()));
				}
				else
					castHotels = new ArrayList<SelectItem>();

			return castHotels;
		}

		public ArrayList<SelectItem> getCrewHotels()
		{
			if(crewHotels == null)
				if(crewCity != null)
				{
					crewHotels = new ArrayList<SelectItem>();
					ArrayList<HotelWrapper> tmpHotels = Utility.getBean(LocationBean.class).getHotelsForCity(
							crewCity);
					for(HotelWrapper h : tmpHotels)
						crewHotels.add(new SelectItem(h.getHotel().getId(), h.getHotel().getName()));
				}
				else
					crewHotels = new ArrayList<SelectItem>();

			return crewHotels;
		}

		public void onSelectShowCastHotels()
		{
			if(TravelItems.HOTEL.toString().equals(getDay().getCastTravel()))
				showCastHotel = true;
			else
				showCastHotel = false;

			castHotels = null;
		}

		public void onSelectShowCrewHotels()
		{
			if(TravelItems.HOTEL.toString().equals(getDay().getCrewTravel()))
				showCrewHotel = true;
			else
				showCrewHotel = false;

			crewHotels = null;
		}

		public void selectCastCity(ValueChangeEvent evt)
		{
			castCity = (Long) evt.getNewValue();
			castHotels = null;
		}

		public void selectCrewCity(ValueChangeEvent evt)
		{
			crewCity = (Long) evt.getNewValue();
			crewHotels = null;
		}
	}

	public class PerformanceSchedules implements Serializable
	{
		private static final long serialVersionUID = 3L;
		private Long editableId;
		private ArrayList<SelectItem> performanceScheduleItems;
		private PerformanceSchedule wrapper;

		public void createPerformance(ActionEvent evt)
		{
			if(!Utility.getBean(LoginBean.class).isScheduler())
			{
				Utility.raiseWarning("Access Denied",
						"You lack the proper permissions");
				return;
			}

			getCalendarDao().savePerformanceSchedule(wrapper);
			Utility.raiseInfo("Performance Updated",
					"A performance schedule was created for " + wrapper.getName());
			wrapper = null;
			performanceScheduleItems = null;
		}

		public Long getEditableId()
		{
			return editableId;
		}

		public ArrayList<SelectItem> getPerforanceScheduleItems()
		{
			if(performanceScheduleItems == null)
			{
				ArrayList<PerformanceSchedule> tmpSchedules = getCalendarDao()
						.getPerformanceSchedulesForSeason(selectedSeason);
				performanceScheduleItems = new ArrayList<SelectItem>();

				for(PerformanceSchedule s : tmpSchedules)
					performanceScheduleItems.add(new SelectItem(s.getId(), s.getName()));
			}

			return performanceScheduleItems;
		}

		public PerformanceSchedule getWrapper()
		{
			if(wrapper == null)
				wrapper = new PerformanceSchedule(true);

			return wrapper;
		}

		public void setEditableId(Long editableId)
		{
			this.editableId = editableId;
		}

		public void setWrapper(PerformanceSchedule wrapper)
		{
			this.wrapper = wrapper;
		}
	}

	public class PerformanceUI extends TimeUI implements Serializable
	{
		private static final long serialVersionUID = 3L;
		private Long cityId;
		private boolean crewCall;
		private boolean fightCall;
		private String productionName;
		private boolean strike;
		private boolean talkback;
		private Long venueId;
		private ArrayList<SelectItem> venueItems;
		private PerformanceAdvance advance;
		private Performance performance;

		public PerformanceUI(Date start, Performance performance)
		{
			super(start, start);
			strike = true;
			fightCall = true;
			crewCall = true;
			this.performance = performance;
			this.advance = performance == null ? null : performance.getAdvance() == null ? null
					: performance.getAdvance().getId() == null ? null : performance.getAdvance();
		}

		/**
		 * Create an advance object that represents a current set of information that has been sent to a
		 * venue.
		 * 
		 * @param evt
		 */
		public void doCreateAdvance(ActionEvent evt)
		{
			if(performance.getVenue() == null)
			{
				Utility.raiseWarning("Unable to Advance",
						"You must have a venue selected to create an advance.");
				return;
			}

			if(!Utility.getBean(LoginBean.class).isScheduler())
			{
				Utility.raiseWarning("Access Denied",
						"You lack the proper permissions");
				return;
			}

			getCalendarDao().createPerformanceAdvance(performance);
			Utility.raiseInfo("Advance Created", "Advances are updated via the Venue module");
			//refreshModel(evt);
		}

		/**
		 * Save the advance to the system--this shouldn't really be needed.
		 * 
		 * @param evt
		 */
		public void doSaveAdvance(ActionEvent evt)
		{
			if(advance == null)
			{
				Utility.raiseError("Null Pointer", "The performance advance is null, can not save");
				return;
			}

			if(!Utility.getBean(LoginBean.class).isScheduler())
			{
				Utility.raiseWarning("Access Denied",
						"You lack the proper permissions");
				return;
			}

			getCalendarDao().savePerformanceAdvance(advance);
			Utility.raiseInfo("Advance Updated", "");
		}

		public Long getCityId()
		{
			return cityId;
		}

		public String getProductionName()
		{
			return productionName;
		}

		public Long getVenueId()
		{
			return venueId;
		}

		public ArrayList<SelectItem> getVenueItems()
		{
			if(venueItems == null)
			{

				if(cityId == null)
				{
					Utility.raiseWarning("No City Selected",
							"The cast has not been assigned to a city, venues can not be assigned");
					return new ArrayList<SelectItem>();
				}

				venueItems = new ArrayList<SelectItem>();
				ArrayList<Venue> tmpVenues = getVenueDao().getVenuesForCity(
						Utility.getBean(LocationBean.class).getCity(cityId));

				boolean found = false;

				for(Venue v : tmpVenues)
				{
					if(v.getId().equals(venueId))
						found = true;

					venueItems.add(new SelectItem(v.getId(), v.getName()));
				}

				if(!found && venueId != null)
				{
					Venue tmpVenue = getVenueDao().getVenue(venueId);
					venueItems.add(new SelectItem(venueId, tmpVenue.getName()));
				}

				if(venueItems.size() == 0)
				{
					venueItems = null;
					return new ArrayList<SelectItem>();
				}

			}
			return venueItems;
		}

		public boolean isCrewCall()
		{
			return crewCall;
		}

		public boolean isFightCall()
		{
			return fightCall;
		}

		/**
		 * @return the showAdvance
		 */
		public boolean isShowAdvance()
		{
			return advance == null && performance != null;
		}

		public boolean isStrike()
		{
			return strike;
		}

		public boolean isTalkback()
		{
			return talkback;
		}

		public void setCityId(Long cityId)
		{
			this.cityId = cityId;
		}

		public void setCrewCall(boolean crewCall)
		{
			this.crewCall = crewCall;
		}

		public void setFightCall(boolean fightCall)
		{
			this.fightCall = fightCall;
		}

		public void setProductionName(String productionName)
		{
			this.productionName = productionName;
		}

		public void setStrike(boolean strike)
		{
			this.strike = strike;
		}

		public void setTalkback(boolean talkback)
		{
			this.talkback = talkback;
		}

		public void setVenueId(Long venueId)
		{
			this.venueId = venueId;
		}
	}

	public enum Selection
	{
		CALENDAR("schedule", "Calendar"), CITY("cities", "City Management"), PERFORMANCES("advance",
				"Performance and Advance");

		public static Selection parseValue(String sub)
		{
			if(sub == null)
				return Selection.CITY;

			for(Selection s : Selection.values())
				if(s.toString().equals(sub.trim()))
					return s;

			return Selection.CITY;
		}

		String label;
		String value;

		Selection(String value, String label)
		{
			this.value = value;
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public String toString()
		{
			return value;
		}
	}

	public class TimeUI implements Serializable
	{
		private static final long serialVersionUID = 3L;
		final static String AM = "AM";
		final static String PM = "PM";
		private Date end;
		private int endHour;
		private String endMeridian;
		private int endMinute;
		private Date start;
		private int startHour;
		private String startMeridian;

		private int startMinute;

		public TimeUI(Date start, Date end)
		{
			this.start = start;
			if(end == null)
			{

				Calendar tmp = Calendar.getInstance();
				tmp.setTime(start);
				tmp.add(Calendar.HOUR_OF_DAY, 2);
				end = tmp.getTime();
			}

			this.end = end;
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			startC.setTime(start);
			endC.setTime(end);
			startHour = startC.get(Calendar.HOUR_OF_DAY);
			startMinute = startC.get(Calendar.MINUTE);
			endHour = endC.get(Calendar.HOUR_OF_DAY);
			endMinute = endC.get(Calendar.MINUTE);
			startMeridian = startC.get(Calendar.AM_PM) == Calendar.AM ? AM : PM;
			endMeridian = endC.get(Calendar.AM_PM) == Calendar.AM ? AM : PM;

		}

		public int getEndHour()
		{
			return endHour;
		}

		public String getEndMeridian()
		{
			return endMeridian;
		}

		public int getEndMinute()
		{
			return endMinute;
		}

		public Date getEndTime(Date daysDate)
		{
			Calendar time = Calendar.getInstance();
			time.setTime(daysDate);
			time.set(Calendar.HOUR_OF_DAY, endHour);
			time.set(Calendar.MINUTE, endMinute);
			// time.set(Calendar.AM_PM, "PM".equals(endMeridian)? Calendar.PM : Calendar.AM);
			return time.getTime();
		}

		public int getStartHour()
		{
			return startHour;
		}

		public String getStartMeridian()
		{
			return startMeridian;
		}

		public int getStartMinute()
		{
			return startMinute;
		}

		public Date getStartTime(Date daysDate)
		{
			Calendar time = Calendar.getInstance();
			time.setTime(daysDate);
			time.set(Calendar.HOUR_OF_DAY, startHour);
			time.set(Calendar.MINUTE, startMinute);
			// time.set(Calendar.AM_PM, "PM".equals(startMeridian) ? Calendar.PM : Calendar.AM);
			return time.getTime();
		}

		public void setEndHour(int endHour)
		{
			this.endHour = endHour;
		}

		public void setEndMeridian(String endMeridian)
		{
			this.endMeridian = endMeridian;
		}

		public void setEndMinute(int endMinute)
		{
			this.endMinute = endMinute;
		}

		public void setStartHour(int startHour)
		{
			this.startHour = startHour;
		}

		public void setStartMeridian(String startMeridian)
		{
			this.startMeridian = startMeridian;
		}

		public void setStartMinute(int startMinute)
		{
			this.startMinute = startMinute;
		}

		@Override
		public String toString()
		{
			StringBuilder b = new StringBuilder();

			b.append("Start (");
			b.append(start);
			b.append(") @ ");
			b.append(startHour);
			b.append(":");
			b.append(startMinute);
			b.append(" ");
			b.append(startMeridian);

			b.append(" End (");
			b.append(end);
			b.append(") @ ");
			b.append(endHour);
			b.append(":");
			b.append(endMinute);
			b.append(" ");
			b.append(endMeridian);

			return b.toString();
		}

	}

	public enum TravelItems
	{
		HOTEL("Hotel"), NONE("N/A"), OVERNIGHT("Overnight"), TRAVEL_DAY("Travel Day");

		String s;

		TravelItems(String s)
		{
			this.s = s;
		}

		@Override
		public String toString()
		{
			return s;
		}
	}
}
