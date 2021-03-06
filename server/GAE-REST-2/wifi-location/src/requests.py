import cgi
import os
import datetime
import urllib
import wsgiref.handlers
import csv
import rest
import logging
from google.appengine.runtime import apiproxy_errors
from django.utils import simplejson as json
from datetime import datetime
from src.models import *
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

def sendFriendRequest(self,json_obj):
	try:
		
		user_obj = Users.get_by_id(int(json_obj["user_id"]))
		friend_obj = Users.get_by_id(int(json_obj["friend_id"]))
	
		#user can't add itself
		if (json_obj["user_id"]) == (json_obj["friend_id"]) :
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"request_id" : "unknown", "status" : 4}))
			return
		#check if user or friend is valid
		elif not user_obj :
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"request_id" : "unknown", "status" : 1}))
			return
		elif not friend_obj:
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"request_id" : "unknown", "status" : 2}))
			return
		
			
		#check if request already exist
		q = db.GqlQuery(("SELECT * FROM FriendRequests " + "WHERE user_id = :1 and friend_id = :2" ), int(json_obj["friend_id"]),int(json_obj["user_id"]))

		if q.count() > 0:
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"request_id" : q[0].key().id(), "status" : 3}))
			return
		
		
		p = db.GqlQuery(("SELECT * FROM FriendRequests " + "WHERE user_id = :1 and friend_id = :2" ), int(json_obj["user_id"]),int(json_obj["friend_id"]))
		if p.count() > 0:
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"request_id" : p[0].key().id(), "status" : 3}))
			return

		k = db.GqlQuery(("SELECT * FROM Friends " + "WHERE user = :1 and friend_id = :2" ), Users.get_by_id(int(json_obj["user_id"])),int(json_obj["friend_id"]))
		
		if k.count() > 0:
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"request_id" : "unknown", "status" : 5}))
			return
			
		#sends request	   
		request = FriendRequests(user_id = int(json_obj["friend_id"]), friend_id = int(json_obj["user_id"]))
		request.put()
		#self.response.out.write("request_sent")
		self.response.headers['Content-Type'] = "application/json"
		self.response.out.write(json.dumps({"request_id" : request.key().id(), "status" : 0}))
	
	
	except apiproxy_errors.OverQuotaError, message:
		logging.error(message)
		self.response.headers['Content-Type'] = "application/json"
		self.response.out.write(json.dumps({"status" : 10}))
	

def getFriendRequests(self, json_obj):
	try:
		user_obj = Users.get_by_id(int(json_obj["user_id"]))
		data = dict()
		data["requests"] = []
		if not user_obj :
			#data["Requests"].append({"request_id" : "unknown"})
			data["status"] = 1
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps(data))
			return

		q = db.GqlQuery(("SELECT * FROM FriendRequests " + "WHERE user_id = :1"), int(json_obj["user_id"]))
		if q.count() == 0:
			data["status"] = 2
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps(data))
			return
		for requests in q:
			friend = Users.get_by_id(requests.friend_id)
			friend_first_name = friend.first_name
			friend_last_name = friend.last_name			
			data["requests"].append({
									'first_name' : friend_first_name,
									'last_name' : friend_last_name,
									'request_id' : requests.key().id()})

			data["status"] = 0
		self.response.headers['Content-Type'] = "application/json"
		self.response.out.write(json.dumps(data))

	except apiproxy_errors.OverQuotaError, message:
		logging.error(message)
		self.response.headers['Content-Type'] = "application/json"
		self.response.out.write(json.dumps({"status" : 10}))
		
def getEvents(self, json_obj):
	data = dict()
	user_obj = Users.get_by_id(int(json_obj["user_id"]))
	zones = db.GqlQuery(("SELECT * FROM BSSIDZones " +
		"WHERE mac_address = :1"), urllib.unquote_plus(json_obj["mac_address"]))	
	if zones.count() == 0:
		#BSSID UNKNOWN
		data["status"] = 1

		self.response.headers['Content-Type'] = "application/json"
		self.response.out.write(json.dumps(data))
		return
	data["events"] = []
	curr_zone = zones[0].zones
	user_obj.last_location = curr_zone.key()
	user_obj.put()
	logging.debug("user information updated")
	logging.debug("location: " + curr_zone.zone_name)
	try:
		curr_super_zone = curr_zone.super_zone

		for events in curr_super_zone.event_super_zone:
			#TODO: return super zone map
			data["events"].append({'name' : events.name,
								   'organizer' : events.organizer,
								   'location' : events.super_zone.super_zone_name,
								   'start_time' :  datetime.ctime(events.start_time),
								   'end_time' : datetime.ctime(events.end_time),
								   'location' : events.location,
								   'description' : events.description})
		data["status"] = 0
		self.response.headers['Content-Type'] = "application/json"
		self.response.out.write(json.dumps(data))
		
	except: 
	
	
		logging.debug("cant not get events")
	
