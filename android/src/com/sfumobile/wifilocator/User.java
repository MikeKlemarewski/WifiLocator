package com.sfumobile.wifilocator;

public final class User {
	 private static User instance;
	 private String zoneName, mapPath, lastUpdate;
	 private int user_id;
	 private User(){
		 
	 }
	 
	 public static User getInstance(){
		 if (instance==null){
			 instance = new User();
		 } 
		 return instance;
	 }
	 
	 public void set_userID(int id) {
		 user_id = id;
	 }
	 
	 public void set_zone(String zone){
		 zoneName = zone;
	 }
	 
	 public void set_map(String map){
		 mapPath = map;
	 }
	 
	 public void set_update(String up) {
		 lastUpdate = up;
	 }
	 
	 public int get_userID(){
		 return user_id;
	 }
	 
	 public String get_zone(){
		 return zoneName;
	 }
	 
	 public String get_map(){
		 return mapPath;
	 }
	 
	 public String get_update(){
		 return lastUpdate;
	 }
}
