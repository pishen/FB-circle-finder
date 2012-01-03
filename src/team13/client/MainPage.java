package team13.client;

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MainPage implements EntryPoint {

	/*our own instances*/
	public static MainPage currentPage;
	
	private Button authenticButton = new Button("Login");
	private Button addButton = new Button("<");
	private Label errorMsgLabel = new Label();
	private Label hintLabel = new Label();
	private Image runningBar = new Image("images/running.gif");
	private CellList<FBUser> circleCellList = new CellList<FBUser>(new FBUserCell());
	private CellList<FBUser> friendsCellList = new CellList<FBUser>(new FBUserCell());
	private HorizontalPanel loginPanel = new HorizontalPanel();
	private ScrollPanel circlePanel = new ScrollPanel();
	private ScrollPanel friendsPanel = new ScrollPanel();
	private FlexTable statusTable = new FlexTable();
	private FlexTable twoListTable = new FlexTable();
	private VerticalPanel mainPanel = new VerticalPanel();
	private ListDataProvider<FBUser> circleProvider = new ListDataProvider<FBUser>();
	private ListDataProvider<FBUser> friendsProvider = new ListDataProvider<FBUser>();
	
	private FBFetcher fbFetcher = new FBFetcher();
	private BPRTester bprTester = new BPRTester();
	private boolean loggedIn;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		MainPage.currentPage = this;
		
		if(Window.Location.getHash().length() == 0){
			loggedIn = false;
		}else{
			loggedIn = true;
		}
		
		initUI();
		
		if(loggedIn){
			fbFetcher.fetchNeededJSON();
		}
		
	}
	
	public void checkJSON(){
		if(fbFetcher.isFetchFinished()){
			bprTester.init();
			setStatus("finished initializing", true);
		}
	}
	
	private void initUI(){
		//setup UI property
		if(!loggedIn){
			hintLabel.setText("Please login as your Facebook account -> ");
			statusTable.addStyleName("hiddenWidget");
		}else{
			hintLabel.setVisible(false);
			authenticButton.setText("Logout");
			//authenticButton.setEnabled(false);
		}
		
		errorMsgLabel.setVisible(false);
		errorMsgLabel.addStyleName("errorMessage");
		
		addButton.setEnabled(false);
		
		authenticButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if(!loggedIn){
					fbFetcher.loginFB();
				}else{
					fbFetcher.logoutFB();
				}
			}
		});
		
		circleProvider.addDataDisplay(circleCellList);
		friendsProvider.addDataDisplay(friendsCellList);
		
		hintLabel.addStyleName("hintLabel");

		loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		loginPanel.add(hintLabel);
		loginPanel.add(authenticButton);
		loginPanel.addStyleName("loginPanel");
		
		circlePanel.add(circleCellList);
		circlePanel.addStyleName("scrollPanel");
		
		friendsPanel.add(friendsCellList);
		friendsPanel.addStyleName("scrollPanel");
		
		twoListTable.setText(0, 0, "Circle");
		twoListTable.setText(0, 2, "Friends");
		twoListTable.setWidget(1, 0, circlePanel);
		twoListTable.setWidget(1, 1, addButton);
		twoListTable.setWidget(1, 2, friendsPanel);
		
		statusTable.setText(0, 0, "initializing...");
		statusTable.setWidget(1, 0, runningBar);
		statusTable.addStyleName("statusTable");
		statusTable.getCellFormatter().addStyleName(0, 0, "centerAlign");
		statusTable.getCellFormatter().addStyleName(1, 0, "centerAlign");
		//statusTable.getColumnFormatter().setStyleName(0, "centerAlign");
		
		mainPanel.add(errorMsgLabel);
		mainPanel.add(loginPanel);
		mainPanel.add(statusTable);
		mainPanel.add(twoListTable);
		mainPanel.addStyleName("mainPanel");
		
		RootPanel.get("main-content").add(mainPanel);
	}
	
	public void setStatus(String text, boolean hideRunningBar){
		statusTable.setText(0, 0, text);
		if(hideRunningBar){
			statusTable.getCellFormatter().addStyleName(1, 0, "hiddenWidget");
		}else{
			statusTable.getCellFormatter().removeStyleName(1, 0, "hiddenWidget");
		}
	}
	
	public void displayError(String error){
		errorMsgLabel.setText("Error: " + error);
		errorMsgLabel.setVisible(true);
	}
	
	public List<FBUser> getCircleList(){
		return circleProvider.getList();
	}
	
	public List<FBUser> getFriendsList(){
		return friendsProvider.getList();
	}

}
