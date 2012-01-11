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
	private int currentTrain; 
	//private int totalTrain;
	private BPRQuery bprQuery;
	
	private Timer trainingTimer;
	private boolean trainingTimerCanceled;
	private static final int TIMER_INTERVAL = 5; //ms
	private static final int TRAIN_PER_ITER = 10;
	private static final int STATUS_PER_ITER = 5;
	
	private Timer statusesTimer;
	private boolean statusesTimerCanceled;
	private JSONArray statusesJArray;
	private int addedStatusCount;
	private int currentStatus;
	
	private Timer predictingTimer;
	private boolean predictingTimerCanceled;
	private int predictingIterCount;
	
	private enum EntityType{
		USER, PHOTO, QUERY, STATUS
	};
	
	public void init(){
		circleList = MainPage.currentPage.getCircleList();
		friendsList = MainPage.currentPage.getFriendsList();
		
		deriveMeInfo();
		deriveFriendsInfo();
		derivePhotosInfo();
		deriveStatusesInfo(); //will trigger a timer
	}
	
	public void startTraining(){
		//TODO how many iter should be enough?
		//totalTrain = 100000;

		if(trainingTimer == null){
			trainingTimer = new Timer(){
				@Override
				public void run() {
					trainIter();
				}
			};
		}
		trainingTimer.scheduleRepeating(TIMER_INTERVAL);
		trainingTimerCanceled = false;
		MainPage.currentPage.setAddRemoveButtonEnabled(false);
	}
	
	public void stopTraining(){
		trainingTimer.cancel();
		trainingTimerCanceled = true;
		MainPage.currentPage.setStatus("training stopped at iteration " + currentTrain, true);
		//trigger new prediction
		bprQuery = new BPRQuery(bprTrain);
		predictFriendsListInit();
	}
	
	private void trainIter(){
		if(trainingTimerCanceled == false){
			currentTrain += TRAIN_PER_ITER;
			MainPage.currentPage.setStatus("training for iteration " + currentTrain, false);
			bprTrain.trainUniform(TRAIN_PER_ITER);
		}
	}
	
	public void addSelectedAndPredict(FBUser selectedFBUser){
		circleList.add(selectedFBUser);
		predictFriendsListInit();
	}
	
	public void removeSelectedAndPredict(FBUser selectedFBUser){
		if(selectedFBUser.getId().equals(meId) == false){
			circleList.remove(selectedFBUser);
			predictFriendsListInit();
		}
	}
	
	//TODO change to async would require modifying BPRQuery.query()
	private void predictFriendsListInit(){
		//can't work until changing to async
		MainPage.currentPage.setAddRemoveButtonEnabled(false);
		MainPage.currentPage.setStatus("predicting...", false);
		MainPage.currentPage.setTrainButtonEnabled(false);
		//
		
		//create query
		List<String> queryList = new ArrayList<String>();
		for(FBUser fbUser: circleList){
			queryList.add(fbUser.getId());
		}
		
		friendsList.clear();
		
		bprQuery.initQuery(queryList, EntityType.USER.ordinal());
		
		//prediction
		predictingTimer = new Timer(){
			@Override
			public void run() {
				predictFriendsListIter();
			}
		};
		predictingIterCount = 0;
		predictingTimer.scheduleRepeating(TIMER_INTERVAL);
		predictingTimerCanceled = false;
	}
	
	private void predictFriendsListIter(){
		if(predictingTimerCanceled == false){
			if(predictingIterCount < 100){
				for(int i = 0; i < 100; i++){
					bprQuery.queryIter();
				}
				predictingIterCount++;
			}else{
				predictingTimer.cancel();
				predictingTimerCanceled = true;
				predictFriendsListFinish();
			}
		}
	}
	
	private void predictFriendsListFinish(){
		//get result
		List<String> predictStrList = bprQuery.getRank();
		
		//add result to friend list

		for(String userId: predictStrList){
			friendsList.add(userMap.get(userId));
		}
				
		MainPage.currentPage.resetFriendsCellListStatus();
		MainPage.currentPage.setTrainButtonEnabled(true);
		MainPage.currentPage.setAddRemoveButtonEnabled(true);
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
		statusesJArray = statusesJObject.get("data").isArray();
		
		addedStatusCount = 0;
		
		statusesTimer = new Timer(){
			@Override
			public void run() {
				//MainPage.currentPage.setStatus("deriving status..." + currentStatus + "/" + statusesJArray.size(), false);
				for(int i = 0; i < STATUS_PER_ITER; i++){
					deriveStatusInfo();
				}
			}
		};
		statusesTimer.scheduleRepeating(TIMER_INTERVAL);
		statusesTimerCanceled = false;
	}
	
	private void deriveStatusInfo(){
		if(currentStatus >= statusesJArray.size()){
			if(statusesTimerCanceled == false){
				//deriving finished
				statusesTimer.cancel();
				statusesTimerCanceled = true;
				//
				if(addedStatusCount == 0){
					Window.alert("You have no status message.");
				}
				//enable training button
				MainPage.currentPage.setStatus("", true);
				MainPage.currentPage.enableTraining();
			}
		}else{
			JSONObject statusJObject = statusesJArray.get(currentStatus).isObject();
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
			
			currentStatus++;
		}
	}
}
