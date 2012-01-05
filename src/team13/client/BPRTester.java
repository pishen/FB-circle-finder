package team13.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import team13.client.bpr.BPRQuery;
import team13.client.bpr.BPRTrain;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

public class BPRTester {
	private List<FBUser> circleList;
	private List<FBUser> friendsList;
	private HashMap<String, FBUser> userMap = new HashMap<String, FBUser>();
	private String meId;
	
	private BPRTrain bprTrain = new BPRTrain();
	private int currentIteration, totalIteration;
	private BPRQuery bprQuery;
	
	private Timer trainingTimer;
	private boolean canceled;
	private static final int TRAIN_INTERVAL = 5; //ms
	private static final int ITER_PER_TRAIN = 100;
	
	private enum EntityType{
		USER, PHOTO, QUERY, STATUS
	};
	
	public void init(){
		circleList = MainPage.currentPage.getCircleList();
		friendsList = MainPage.currentPage.getFriendsList();
		
		deriveMeInfo();
		deriveFriendsInfo();
		derivePhotosInfo();
		deriveStatusesInfo();
		
		totalIteration = 250000;

		trainingTimer = new Timer(){
			@Override
			public void run() {
				train();
			}
		};
		trainingTimer.scheduleRepeating(TRAIN_INTERVAL);
		canceled = false;
		
	}
	
	private void train(){
		if(currentIteration < totalIteration){
			currentIteration += ITER_PER_TRAIN;
			MainPage.currentPage.setStatus("training..." + currentIteration + "/" + totalIteration, false);
			bprTrain.trainUniform(ITER_PER_TRAIN);
		}else if(!canceled){
			trainingTimer.cancel();
			canceled = true;
			//fire first prediction
			bprQuery = new BPRQuery(bprTrain);
			predictFriendsList();
		}
	}
	
	public void addSelectedAndPredict(FBUser selectedFBUser){
		circleList.add(selectedFBUser);
		predictFriendsList();
	}
	
	//TODO change to async
	private void predictFriendsList(){
		MainPage.currentPage.setAddEnabled(false);
		MainPage.currentPage.setStatus("predicting...", false);
		
		//create query
		List<String> queryList = new ArrayList<String>();
		for(FBUser fbUser: circleList){
			queryList.add(fbUser.getId());
		}
		
		//prediction
		//TODO fix adding policy
		List<String> predictStrList = bprQuery.query(queryList, EntityType.USER.ordinal());
		/*
		List<String> list2= new ArrayList<String>();
		for(int i=0;i<25;i++)
			list2.add(predictStrList.get(i));
		predictStrList = list2;
		*/
		
		//add result to friend list
		friendsList.clear();

		for(String userId: predictStrList){
			friendsList.add(userMap.get(userId));
		}
		
		MainPage.currentPage.setStart(); //TODO fix
		MainPage.currentPage.setAddEnabled(true);
		MainPage.currentPage.setStatus("finish predicting", true);
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
		
		int addedPhotoCount = 0;
		
		//go through each photo
		for(int i = 0; i < photosJArray.size(); i++){
			JSONObject photoJObject = photosJArray.get(i).isObject();
			String photoId = photoJObject.get("id").isString().stringValue();
			
			boolean photoAdded = false;
			ArrayList<String> dataList = null;  //<p1, u1, u2, u3, ...> for BPR data
			
			if(photoJObject.containsKey("tags")){
				JSONObject tagsJObject = photoJObject.get("tags").isObject();
				JSONArray tagsJArray = tagsJObject.get("data").isArray();
				
				//go through each tag to grab the user info
				for(int j = 0; j < tagsJArray.size(); j++){
					JSONObject tagJObject = tagsJArray.get(j).isObject();
					String userId = tagJObject.get("id").isString().stringValue();
					if(userMap.containsKey(userId)){
						//add the user to co-occurrence if he is friend
						if(photoAdded == false){
							photoAdded = true;
							dataList = new ArrayList<String>();
							dataList.add(photoId);
						}
						dataList.add(userId);
					}
				}
			}
			
			if(photoAdded){
				bprTrain.addEntity(photoId, EntityType.PHOTO.ordinal());
				bprTrain.addData(dataList);
				
				addedPhotoCount++;
			}
			
		}
		
		if(addedPhotoCount == 0){
			Window.alert("You have no photo info.");
		}
	}
	
	private void deriveStatusesInfo(){
		JSONObject statusesJObject = FBFetcher.currentFetcher.getJSON("statuses");
		JSONArray statusesJArray = statusesJObject.get("data").isArray();
		
		int addedStatusCount = 0;
		
		//go through each status message
		for(int i = 0; i < statusesJArray.size(); i++){
			JSONObject statusJObject = statusesJArray.get(i).isObject();
			String statusId = statusJObject.get("id").isString().stringValue();
			
			boolean statusAdded = false;
			ArrayList<String> dataList = null;  //<s1, u1, u2, u3, ...> for BPR data
			
			if(statusJObject.containsKey("likes")){
				JSONObject likesJObject = statusJObject.get("likes").isObject();
				JSONArray likesJArray = likesJObject.get("data").isArray();
				//go through each like to grab the user info
				for(int j = 0; j < likesJArray.size(); j++){
					JSONObject likeJObject = likesJArray.get(j).isObject();
					String userId = likeJObject.get("id").isString().stringValue();
					if(userMap.containsKey(userId)){
						//add the user to co-occurrence if he is friend
						if(statusAdded == false){
							statusAdded = true;
							dataList = new ArrayList<String>();
							dataList.add(statusId);
						}
						dataList.add(userId);
					}
				}
			}
			
			if(statusJObject.containsKey("comments")){
				JSONObject commentsJObject = statusJObject.get("comments").isObject();
				JSONArray commentsJArray = commentsJObject.get("data").isArray();
				//go through each comment to grab the user info
				for(int j = 0; j < commentsJArray.size(); j++){
					JSONObject commentJObject = commentsJArray.get(j).isObject();
					String userId = commentJObject.get("from").isObject().get("id").isString().stringValue();
					if(userMap.containsKey(userId)){
						//add the user to co-occurrence if he is friend
						if(statusAdded == false){
							statusAdded = true;
							dataList = new ArrayList<String>();
							dataList.add(statusId);
						}
						if(!dataList.contains(userId)){ //may have duplicate users
							dataList.add(userId);
						}
					}
				}
			}
			
			if(statusAdded){
				bprTrain.addEntity(statusId, EntityType.STATUS.ordinal());
				bprTrain.addData(dataList);
				
				addedStatusCount++;
			}
		}
		
		if(addedStatusCount == 0){
			Window.alert("You have no status message.");
		}
	}

}
