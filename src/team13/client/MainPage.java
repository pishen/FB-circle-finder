package team13.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	private Button loginButton = new Button("login");
	private Label statusLabel = new Label();
	private Label errorMsgLabel = new Label();
	private HorizontalPanel loginPanel = new HorizontalPanel();
	private Button showFriendsButton = new Button("show friends");
	private FlexTable friendsFlexTable = new FlexTable();
	private VerticalPanel mainPanel = new VerticalPanel();
	private ScrollPanel scrollPanel = new ScrollPanel();
	private CellList<String> friendsCellList = new CellList<String>(new TextCell());
	private Button testButton = new Button("test");
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		if(Window.Location.getHash().length() == 0){
			statusLabel.setText("not logged in");
			showFriendsButton.setEnabled(false);
		}else{
			statusLabel.setText("logged in");
			loginButton.setEnabled(false);
		}
		
		errorMsgLabel.setStyleName("errorMessage");
		errorMsgLabel.setVisible(false);
		
		loginPanel.add(loginButton);
		loginPanel.add(statusLabel);
		
		scrollPanel.add(friendsCellList);
		scrollPanel.setHeight("50px");
		
		mainPanel.add(errorMsgLabel);
		mainPanel.add(loginPanel);
		mainPanel.add(showFriendsButton);
		mainPanel.add(testButton);
		//mainPanel.add(friendsFlexTable);
		mainPanel.add(scrollPanel);
		
		RootPanel.get("main-content").add(mainPanel);
		
		loginButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				loginFB();
			}
		});
		
		showFriendsButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				showFriends();
			}
		});
		
		testButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				List<String> testList = Arrays.asList("A", "B", "C");
				friendsCellList.setRowData(1, testList);
			}
		});
	}
	
	private void loginFB(){
		String url = "https://www.facebook.com/dialog/oauth?client_id=195431067216915&redirect_uri="
				+ Window.Location.getHref() + "&response_type=token";
		Window.Location.replace(url);
	}
	
	private void showFriends(){
		String url = "https://graph.facebook.com/me/friends?" + Window.Location.getHash().substring(1);
		url = URL.encode(url);
		
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		
		try {
			Request request = builder.sendRequest(null, new RequestCallback(){

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					if(200 == response.getStatusCode()){
						//clear errors
						errorMsgLabel.setVisible(false);
						fillTable(response.getText());
					}else{
						displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					displayError("Couldn't retrieve JSON");
				}
				
			});
		} catch (RequestException e) {
			displayError("Couldn't retrieve JSON");
		}
	}
	
	private void displayError(String error){
		errorMsgLabel.setText("Error: " + error);
		errorMsgLabel.setVisible(true);
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
		dataProvider.addDataDisplay(friendsCellList);
		List<String> list = dataProvider.getList();
		for(String friend: friendsList){
			list.add(friend);
		}
	}
}
