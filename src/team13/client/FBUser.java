package team13.client;

public class FBUser {
	private String id;
	private String userName;
	private String avatarURL;
	
	public FBUser(String id, String userName){
		this.id = id;
		this.userName = userName;
		avatarURL = "http://graph.facebook.com/" + id + "/picture";
	}
	
	public String getId(){
		return id;
	}
	
	public String getUserName(){
		return userName;
	}
	
	public String getAvatarURL(){
		return avatarURL;
	}

}
