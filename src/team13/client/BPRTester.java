package team13.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import team13.client.bpr.BPRQuery;
import team13.client.bpr.BPRTrain;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

public class BPRTester {
	private List<FBUser> circleList;
	private List<FBUser> friendsList;
	private HashMap<String, FBUser> userMap = new HashMap<String, FBUser>();
	private String meId;
	
	private BPRTrain bprTrain = new BPRTrain();
	private BPRQuery bprQuery;
	
	private enum EntityType{
		USER, PHOTO, QUERY, POST
	};
	
	public void init(){
		circleList = MainPage.currentPage.getCircleList();
		friendsList = MainPage.currentPage.getFriendsList();
		
		deriveMeInfo();
		deriveFriendsInfo();
		derivePhotosInfo();
		
		//TODO change to async
		bprTrain.trainUniform(50*5000);
		//
		
		bprQuery = new BPRQuery(bprTrain);
		
		predictFriendsList(Arrays.asList(meId));
	}
	
	public void addSelectedAndPredict(FBUser selectedFBUser){
		circleList.add(selectedFBUser);
		List<String> queryList = new ArrayList<String>();
		for(FBUser fbUser: circleList){
			queryList.add(fbUser.getId());
		}
		predictFriendsList(queryList);
	}

	private void predictFriendsList(List<String> queryList){
		//prediction
		List<String> predictStrList = bprQuery.query(queryList, EntityType.USER.ordinal());
		List<String> list2= new ArrayList<String>();
		for(int i=0;i<25;i++)
			list2.add(predictStrList.get(i));
		predictStrList = list2;
		//
		friendsList.clear();

		for(String userId: predictStrList){
			friendsList.add(userMap.get(userId));
		}
	}
	
	private void deriveMeInfo(){
		JSONObject meJObject = FBFetcher.currentFetcher.getJSON("me");
		meId = meJObject.get("id").isString().stringValue();
		String meName = meJObject.get("name").isString().stringValue();
		//
		FBUser me = new FBUser(meId, meName);
		circleList.add(me);
		userMap.put(meId, me);
		//
		bprTrain.addEntity(meId, EntityType.USER.ordinal());
	}
	
	private void deriveFriendsInfo(){
		JSONObject friendsJObject = FBFetcher.currentFetcher.getJSON("friends");
		JSONArray friendsJArray = friendsJObject.get("data").isArray();
		
		for(int i = 0; i < friendsJArray.size(); i++){
			JSONObject friendJObject = friendsJArray.get(i).isObject();
			String friendId = friendJObject.get("id").isString().stringValue();
			String friendName = friendJObject.get("name").isString().stringValue();
			//
			FBUser friend = new FBUser(friendId, friendName);
			userMap.put(friendId, friend);
			//
			bprTrain.addEntity(friendId, EntityType.USER.ordinal());
		}
	}
	
	private void derivePhotosInfo(){
		JSONObject photosJObject = FBFetcher.currentFetcher.getJSON("photos");
		JSONArray photosJArray = photosJObject.get("data").isArray();
		
		for(int i = 0; i < photosJArray.size(); i++){
			JSONObject photoJObject = photosJArray.get(i).isObject();
			String photoId = photoJObject.get("id").isString().stringValue();
			
			//TODO can tags be null?
			boolean photoAdded = false;
			ArrayList<String> dataList = null;
			JSONObject tagsJObject = photoJObject.get("tags").isObject(); //TODO check get('tag')
			JSONArray tagsJArray = tagsJObject.get("data").isArray();
			
			for(int j = 0; j < tagsJArray.size(); j++){
				JSONObject tagJObject = tagsJArray.get(j).isObject();
				String userId = tagJObject.get("id").isString().stringValue();
				if(userMap.containsKey(userId)){
					if(photoAdded == false){
						photoAdded = true;
						dataList = new ArrayList<String>();
						dataList.add(photoId);
					}
					dataList.add(userId);
				}
			}
			if(photoAdded){
				bprTrain.addEntity(photoId, EntityType.PHOTO.ordinal());
				bprTrain.addData(dataList);
			}
		}
	}

}
