package team13.client;

import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

public class BPRTester {
	public static BPRTester currentTester;
	private List<FBUser> circleList;
	private List<FBUser> friendsList;
	
	public BPRTester(){
		BPRTester.currentTester = this;
	}
	
	public void init(){
		circleList = MainPage.currentPage.getCircleList();
		friendsList = MainPage.currentPage.getFriendsList();
		putMeToCircle();
		fillFriendsList();
	}
	
	private void putMeToCircle(){
		JSONObject meJObject = FBFetcher.currentFetcher.getJSON("me");
		String meId = meJObject.get("id").isString().stringValue();
		String meName = meJObject.get("name").isString().stringValue();
		FBUser me = new FBUser(meId, meName);
		circleList.add(me);
	}
	
	private void fillFriendsList(){
		JSONObject friendsJObject = FBFetcher.currentFetcher.getJSON("friends");
		JSONArray friendsJArray = friendsJObject.get("data").isArray();
		
		for(int i = 0; i < friendsJArray.size(); i++){
			JSONObject friendJObject = friendsJArray.get(i).isObject();
			String id = friendJObject.get("id").isString().stringValue();
			String name = friendJObject.get("name").isString().stringValue();
			FBUser friend = new FBUser(id, name);
			friendsList.add(friend);
		}
	}
}
