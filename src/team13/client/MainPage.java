package team13.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
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
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	/*our own instances*/
	public static MainPage currentPage;
	
	private Button authenticButton = new Button("Login");
	private Button addButton = new Button("<");
	private Label errorMsgLabel = new Label();
	private Label hintLabel = new Label();
	//private Label statusLabel = new Label();
	//private Image myAvatar = new Image();
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
	
	private Controller controller = new Controller();
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
		
		initUIProperty();
		initUIToPage();
		
		if(loggedIn){
			controller.fetchNeededJSON();
		}
		
	}
	
	private void initUIProperty(){
		if(!loggedIn){
			hintLabel.setText("Please login as your Facebook account -> ");
			statusTable.addStyleName("hiddenWidget");
		}else{
			hintLabel.setVisible(false);
			//authenticButton.setText("Logout");
			authenticButton.setEnabled(false);
		}
		
		hintLabel.addStyleName("hintLabel");
		
		errorMsgLabel.setStyleName("errorMessage");
		errorMsgLabel.setVisible(false);
		
		addButton.setEnabled(false);
		
		authenticButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if(!loggedIn){
					controller.login();
				}else{
					controller.logout();
				}
			}
		});
		
		circleProvider.addDataDisplay(circleCellList);
		friendsProvider.addDataDisplay(friendsCellList);
	}
	
	private void initUIToPage(){
		loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		loginPanel.add(hintLabel);
		loginPanel.add(authenticButton);
		loginPanel.addStyleName("loginPanel");
		
		circlePanel.add(circleCellList);
		friendsPanel.add(friendsCellList);
		circlePanel.addStyleName("scrollPanel");
		friendsPanel.addStyleName("scrollPanel");
		
		twoListTable.setText(0, 0, "Circle");
		twoListTable.setText(0, 2, "Friends");
		twoListTable.setWidget(1, 0, circlePanel);
		twoListTable.setWidget(1, 1, addButton);
		twoListTable.setWidget(1, 2, friendsPanel);
		
		statusTable.addStyleName("statusTable");
		statusTable.setText(0, 0, "initializing...");
		statusTable.setWidget(1, 0, runningBar);
		
		mainPanel.add(errorMsgLabel);
		mainPanel.add(loginPanel);
		mainPanel.add(statusTable);
		mainPanel.add(twoListTable);
		mainPanel.addStyleName("mainPanel");
		
		RootPanel.get("main-content").add(mainPanel);
	}
	
	public void setStatus(String text, boolean hidden){
		statusTable.setText(0, 0, text);
		if(hidden){
			statusTable.getCellFormatter().addStyleName(1, 0, "hiddenWidget");
		}else{
			statusTable.getCellFormatter().removeStyleName(1, 0, "hiddenWidget");
		}
		
	}
	
	public void displayError(String error){
		errorMsgLabel.setText("Error: " + error);
		errorMsgLabel.setVisible(true);
	}
	
	public void cleanError(){
		errorMsgLabel.setVisible(false);
	}
	
	public List<FBUser> getCircleList(){
		return circleProvider.getList();
	}
	
	public List<FBUser> getFriendsList(){
		return friendsProvider.getList();
	}
	
	public void setHintLabel(String text){
		hintLabel.setText(text);
	}
	
	private void fillTable(String jsonStr){
		JSONObject friendsMainJObject = JSONParser.parseStrict(jsonStr).isObject();
		JSONArray friendsJArray = friendsMainJObject.get("data").isArray();
		
		List<String> friendsList = new ArrayList<String>();
		
		for(int i = 0; i < friendsJArray.size(); i++){
			JSONObject friendJObject = friendsJArray.get(i).isObject();
			String name = friendJObject.get("name").isString().stringValue();
			//friendsFlexTable.setText(i, 0, name);
			friendsList.add(name);
		}
		
		ListDataProvider<String> dataProvider = new ListDataProvider<String>();
		//dataProvider.addDataDisplay(friendsCellList);
		List<String> list = dataProvider.getList();
		for(String friend: friendsList){
			list.add(friend);
		}
	}
}
