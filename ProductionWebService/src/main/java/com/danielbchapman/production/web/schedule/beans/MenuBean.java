package com.danielbchapman.production.web.schedule.beans;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.danielbchapman.jboss.login.Roles;
import com.danielbchapman.production.Utility;
import com.danielbchapman.production.web.production.beans.SeasonBean;

@SessionScoped
public class MenuBean implements Serializable
{
	private static final long serialVersionUID = 3L;
	private MenuItem[] items;
	private MenuItem[] allButHomeItems;
	private MenuItem selectedItem = new MenuItem("Null", "./");

	public MenuBean()
	{
	}

	public ActionMenuItem<Object> createActionMenuItem(String name, String beanBinding,
			String voidMethodName)
	{
		Object bean = Utility.getBean(beanBinding);
		try
		{
			return new ActionMenuItem<Object>(name, bean, bean.getClass().getMethod(voidMethodName));
		}
		catch(SecurityException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public MenuItem[] getAllButHome()
	{
		getItems();
		if(allButHomeItems != null)
			return allButHomeItems;
		
		allButHomeItems = new MenuItem[items.length - 2];
		for(int i = 0; i < allButHomeItems.length; i++)
			allButHomeItems[i] = items[i + 1];
		
		return allButHomeItems;
	}
	public MenuItem[] getItems()
	{
		if(items == null)
		{
			MenuItem production = new MenuItem("Production", "season.xhtml", Roles.USER);
			production.addMenuItem(new SeasonMenuItem("season.xhtml", Roles.USER,
					SeasonBean.Selection.SUMMARY));
			production.addMenuItem(new SeasonMenuItem("season.xhtml", Roles.USER,
					SeasonBean.Selection.BUDGET));
			production.addMenuItem(new SeasonMenuItem("season.xhtml", Roles.USER,
					SeasonBean.Selection.DEPARTMENTS));
			production.addMenuItem(new SeasonMenuItem("season.xhtml", Roles.USER,
					SeasonBean.Selection.PETTY_CASH));
			production.addMenuItem(new SeasonMenuItem("season.xhtml", Roles.USER,
					SeasonBean.Selection.REPORTS));
			production.addMenuItem(new SeasonMenuItem("season.xhtml", Roles.USER,
					SeasonBean.Selection.CONTACTS));
			production.addMenuItem(createActionMenuItem("Change Season", "seasonBean", "nullifySeason"));

			MenuItem inventory = new MenuItem("Inventory", "inventory.xhtml", Roles.GUEST);
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.GENERAL));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.LIGHTING));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.PROPS));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.SCENIC));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.SOUND));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.STAGE_MANAGEMENT));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.GUEST,
					InventoryBean.Selection.WARDROBE));
			inventory.addMenuItem(new InventoryMenuItem("inventory.xhtml", Roles.INVENTORY_ADMIN,
					InventoryBean.Selection.PROBLEMS));

			MenuItem schedule = new MenuItem("Scheduling", "setTimeZone.xhtml", Roles.GUEST);
			schedule.addMenuItem(new ScheduleMenuItem("setTimeZone.xhtml", Roles.GUEST,
					ScheduleBean.Selection.CALENDAR));
			schedule.addMenuItem(new ScheduleMenuItem("setTimeZone.xhtml", Roles.SCHEDULER,
					ScheduleBean.Selection.CITY));
			schedule.addMenuItem(new ScheduleMenuItem("setTimeZone.xhtml", Roles.SCHEDULER,
					ScheduleBean.Selection.PERFORMANCES));

			MenuItem settings = new MenuItem("Settings", "settings.xhtml", Roles.ADMIN);
			settings.addMenuItem(new MenuItem("Error Page", "errorPage.xhtml", Roles.ADMIN));

			items = new MenuItem[] { new MenuItem("Home", "index.xhtml"), production, schedule,
					new MenuItem("Venues", "venues.xhtml", Roles.SCHEDULER), inventory, settings,
					new MenuItem("Logout", "logoutbasic.jsp") };
			items[0].setSelected(true);
		}
		return items;
	}

	public MenuItem getSelectedItem()
	{
		return selectedItem;
	}

	public MenuItem getTmpAction()
	{
		MenuItem[] tmp = getItems();
		return tmp[0];
	}

	public boolean isAdmin()
	{
		return isUserInRole(Roles.ADMIN);
	}

	public boolean isGuest()
	{
		return isUserInRole(Roles.GUEST);
	}

	public boolean isInventoryAdmin()
	{
		return isUserInRole(Roles.INVENTORY_ADMIN);
	}

	public boolean isInventoryGeneral()
	{
		return isUserInRole(Roles.INVENTORY_GENERAL);
	}

	public boolean isInventoryLighting()
	{
		return isUserInRole(Roles.INVENTORY_LIGHTING);
	}

	public boolean isInventoryProps()
	{
		return isUserInRole(Roles.INVENTORY_PROPS);
	}

	public boolean isInventoryScenic()
	{
		return isUserInRole(Roles.INVENTORY_SCENIC);
	}

	public boolean isInventorySound()
	{
		return isUserInRole(Roles.INVENTORY_SOUND);
	}

	public boolean isInventoryStageManagement()
	{
		return isUserInRole(Roles.INVENTORY_STAGE_MANAGEMENT);
	}

	public boolean isInventoryWardrobe()
	{
		return isUserInRole(Roles.INVENTORY_WARDROBE);
	}

	public boolean isScheduler()
	{
		return isUserInRole(Roles.SCHEDULER);
	}

	public boolean isUser()
	{
		return isUserInRole(Roles.USER);
	}

	public boolean isUserInRole(Roles role)
	{
		return Utility.getBean(LoginBean.class).isUserInRole(role);
	}

	public class ActionMenuItem<T> extends MenuItem implements Serializable
	{
		private static final long serialVersionUID = 3L;

		private Method voidMethod;
		private T bean;

		public ActionMenuItem(String name, T bean, Method voidMethod)
		{
			super(name, "#");
			this.bean = bean;
			this.voidMethod = voidMethod;
		}

		@Override
		public void onSelect(ActionEvent evt)
		{
			try
			{
				voidMethod.invoke(bean, new Object[] {});
			}
			catch(IllegalArgumentException e)
			{
				throw new RuntimeException(e.getMessage(), e);
			}
			catch(IllegalAccessException e)
			{
				throw new RuntimeException(e.getMessage(), e);
			}
			catch(InvocationTargetException e)
			{
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		@Override
		public void onSubMenuItemSelect(ActionEvent evt)
		{
			onSelect(evt);
		}
	}

	public class InventoryMenuItem extends MenuItem implements Serializable
	{
		private static final long serialVersionUID = 3L;

		private InventoryBean.Selection key;

		public InventoryMenuItem(String link, Roles role, InventoryBean.Selection key)
		{
			super(key.label, link, role);
			this.key = key;
		}

		@Override
		public void onSubMenuItemSelect(ActionEvent evt)
		{
			InventoryBean bean = Utility.getBean(InventoryBean.class);

			if(key == InventoryBean.Selection.GENERAL)
			{
				bean.getGeneral().doModeSearch(evt);
				bean.selectGeneral(evt);
			}

			if(key == InventoryBean.Selection.LIGHTING)
			{
				bean.getLighting().doModeSearch(evt);
				bean.selectLighting(evt);
			}

			if(key == InventoryBean.Selection.PROPS)
			{
				bean.getProps().doModeSearch(evt);
				bean.selectProps(evt);
			}

			if(key == InventoryBean.Selection.SCENIC)
			{
				bean.getScenic().doModeSearch(evt);
				bean.selectScenic(evt);
			}

			if(key == InventoryBean.Selection.SOUND)
			{
				bean.getSound().doModeSearch(evt);
				bean.selectSound(evt);
			}

			if(key == InventoryBean.Selection.STAGE_MANAGEMENT)
			{
				bean.getStageManagement().doModeSearch(evt);
				bean.selectStageManagement(evt);
			}

			if(key == InventoryBean.Selection.WARDROBE)
			{
				bean.getWardrobe().doModeSearch(evt);
				bean.selectWardrobe(evt);
			}

			if(key == InventoryBean.Selection.PROBLEMS)
			{
				bean.selectProblems(evt);
			}

			super.onSubMenuItemSelect(evt);
		}

	}

	public class MenuItem implements Serializable
	{
		private static final long serialVersionUID = 3L;
		public final static String SELECTED = "topLevelMenuItemSelected";
		public final static String BASE = "topLevelMenuItem";

		private String link;
		private boolean selected;
		private ArrayList<MenuItem> subItems = new ArrayList<MenuItem>();
		private String name;
		private Roles role;

		public MenuItem(String name, String link)
		{
			this.link = link;
			this.name = name;
			role = null;
			selected = false;
		}

		public MenuItem(String name, String link, Roles role)
		{
			this.link = link;
			this.name = name;
			this.role = role;
			selected = false;
		}

		public void addMenuItem(MenuItem item)
		{
			subItems.add(item);
		}

		public void fireRedirect(ActionEvent evt)
		{
			if(evt != null)
			{
				try
				{
					FacesContext.getCurrentInstance().getExternalContext().redirect(this.link);
				}
				catch(IOException e)
				{
					e.printStackTrace();
					try
					{
						FacesContext
								.getCurrentInstance()
								.getExternalContext()
								.responseSendError(500,
										"FATAL ERROR: Application Menu Unstable [MenueBean$MenueItem:81], please log out and try again.");
						FacesContext.getCurrentInstance().responseComplete();
					}
					catch(IOException e1)
					{
						FacesContext.getCurrentInstance().getExternalContext().setResponseStatus(500);
						FacesContext.getCurrentInstance().responseComplete();
						e1.printStackTrace();
					}
				}
			}
		}

		public String getLink()
		{
			return link;
		}

		public String getName()
		{
			return name;
		}

		public Roles getRole()
		{
			return role;
		}

		public String getStyle()
		{
			if(selected)
				return SELECTED;
			else
				return BASE;
		}

		public ArrayList<MenuItem> getSubItems()
		{
			return subItems;
		}

		public boolean isRenderedViaRole()
		{
			if(role == null)
				return false;

			if(role == Roles.ADMIN)
				return isAdmin();

			if(role == Roles.USER)
				return isUser();

			if(role == Roles.GUEST)
				return isGuest();

			if(role == Roles.SCHEDULER)
				return isScheduler();

			if(role == Roles.INVENTORY_ADMIN)
				return isInventoryAdmin();

			return false;
		}

		public boolean isSelected()
		{
			return selected;
		}

		public void onSelect(ActionEvent evt)
		{
			if(evt != null)
			{
				MenuItem selected = null;

				for(MenuItem item : items)
					if(name.equals(item.getName()))
					{
						item.setSelected(true);
						selected = item;
						MenuBean.this.selectedItem = this;
					}
					else
						item.setSelected(false);

				try
				{
					FacesContext.getCurrentInstance().getExternalContext().redirect(selected.link);
				}
				catch(IOException e)
				{
					e.printStackTrace();
					try
					{
						FacesContext
								.getCurrentInstance()
								.getExternalContext()
								.responseSendError(500,
										"FATAL ERROR: Application Menu Unstable [MenueBean$MenueItem:81], please log out and try again.");
						FacesContext.getCurrentInstance().responseComplete();
					}
					catch(IOException e1)
					{
						FacesContext.getCurrentInstance().getExternalContext().setResponseStatus(500);
						FacesContext.getCurrentInstance().responseComplete();
						e1.printStackTrace();
					}
				}
			}
		}

		public void onSubMenuItemSelect(ActionEvent evt)
		{
			fireRedirect(evt);
		}

		public boolean removeMenuItem(MenuItem item)
		{
			return subItems.remove(item);
		}

		public void setLink(String url)
		{
			this.link = url;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public void setRole(Roles role)
		{
			this.role = role;
		}

		public void setSelected(boolean selected)
		{
			this.selected = selected;
		}
	}

	public class ScheduleMenuItem extends MenuItem implements Serializable
	{
		private static final long serialVersionUID = 3L;
		private ScheduleBean.Selection key;

		public ScheduleMenuItem(String link, Roles role, ScheduleBean.Selection key)
		{
			super(key.label, link, role);
			this.key = key;
		}

		@Override
		public void onSubMenuItemSelect(ActionEvent evt)
		{
			ScheduleBean bean = Utility.getBean(ScheduleBean.class);

			if(key == ScheduleBean.Selection.CALENDAR)
				bean.doSelectCalendar(evt);

			if(key == ScheduleBean.Selection.CITY)
				bean.doSelectCities(evt);

			if(key == ScheduleBean.Selection.PERFORMANCES)
				bean.doSelectPerformances(evt);

			super.onSubMenuItemSelect(evt);
		}
	}

	public class SeasonMenuItem extends MenuItem implements Serializable
	{
		private static final long serialVersionUID = 3L;

		private SeasonBean.Selection key;

		public SeasonMenuItem(String link, Roles role, SeasonBean.Selection key)
		{
			super(key.label, link, role);
			this.key = key;
		}

		@Override
		public void onSubMenuItemSelect(ActionEvent evt)
		{
			SeasonBean bean = Utility.getBean(SeasonBean.class);

			if(key == SeasonBean.Selection.BUDGET)
				bean.getSelection().selectBudget(evt);

			if(key == SeasonBean.Selection.DEPARTMENTS)
				bean.getSelection().selectDepartment(evt);

			if(key == SeasonBean.Selection.PETTY_CASH)
				bean.getSelection().selectPettyCash(evt);

			if(key == SeasonBean.Selection.SUMMARY)
				bean.getSelection().selectSummary(evt);

			if(key == SeasonBean.Selection.REPORTS)
				bean.getSelection().selectReports(evt);

			if(key == SeasonBean.Selection.CONTACTS)
				bean.getSelection().selectContacts(evt);

			super.onSubMenuItemSelect(evt);
		}

	}
}
