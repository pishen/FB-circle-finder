package team13.client;


public class Controller {
	public static Controller currentController;
	private FBFetcher fbFetcher = new FBFetcher();
	private BPRTester bprTester = new BPRTester();
	
	public Controller(){
		Controller.currentController = this;
	}
	
	public void login(){
		fbFetcher.loginFB();
	}
	
	public void logout(){
		fbFetcher.logoutFB();
	}
	
	public void fetchNeededJSON(){
		MainPage.currentPage.setHintLabel("Logged in.");
		fbFetcher.fetchNeededJSON();
	}
	
	public void checkJSON(){
		if(FBFetcher.currentFetcher.isFetchFinished()){
			BPRTester.currentTester.init();
			MainPage.currentPage.setStatus("finish initializing", true);
		}
	}
}
