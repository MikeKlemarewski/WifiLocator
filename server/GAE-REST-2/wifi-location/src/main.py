import cgi
import os
import datetime
import urllib
import wsgiref.handlers
import csv
import rest
import logging

from src.models import *
from src.friends import *
from src.zones import *
from src.requests import *
from src.users import *
from src.accepts import *
from src.importcsv import CSVImporter
from django.utils import simplejson as json
from google.appengine.ext.webapp import template
from google.appengine.ext import db
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class MainPage(webapp.RequestHandler):
	def get(self):
		'''
		path = os.path.join(os.path.dirname(__file__) + '/../csv/', 'surrey_super_zone.csv')
		areaReader = csv.reader(open(path,'rU'), delimiter=',')
		
		for row in areaReader:
			tempzone = Areas.get_by_id(int(row[1]))
			SuperZones(zone = tempzone, super_zone_name = row[0]).put()
			#Areas(zone_id=int(row[0]),zone_name=row[1]).put()
		print "imported super zone"
	
	
		macReader = csv.reader(open(('surrey_res.csv'),'rU'), delimiter=',')
		

    		for row in macReader:
			curr_area = Areas.all()
			temp = curr_area.filter("zone_id =", (int(row[1])+20))
			#print row[1]
			#print "\n"
			for area in temp:
				#print "[" + str(area.zone_id) + "]"
				BSSIDZones(zones = area, mac_address = row[0]).put()
			
		curr_area = Areas.all()
		temp = curr_area.filter("zone_id =", 10)
		for area in temp:
			ZoneMaps(zones = area, map_name = "twitter_icon.png").put()
		
		username = "Alex"
		user = db.GqlQuery(("SELECT * FROM Users " +
				"WHERE short_name = :1"), username)
		for this in user:
			Friends(user = this, friend_id = 10).put()
			Friends(user = this, friend_id = 13).put()
			Friends(user = this, friend_id = 14).put()
			Friends(user = this, friend_id = 12).put()
			break
		
		catherine = Users.get_by_id(28001)
		Friends(user = catherine, friend_id = 27001).put()
		friend = Users.get_by_id(27001)
		Friends(user = friend, friend_id = 28001).put()
		
		Friends(user = catherine, friend_id = 30001).put()
		friend = Users.get_by_id(30001)
		Friends(user = friend, friend_id = 28001).put()
		
		from datetime import datetime
		
		timetest = datetime.now()
		print timetest.ctime()
		
		mytime = pretty_date(timetest)
		
		print mytime
		
		
		areas = db.GqlQuery("SELECT * "
						"FROM Areas ")
		for items in areas:
			if items.zone_name[:9] == "Galleria4":
				superzone = SuperZones.get_by_id(67001)
				items.super_zone = superzone
				items.put()
			elif items.zone_name[:9] == "Galleria3":
				superzone = SuperZones.get_by_id(66001)
				items.super_zone = superzone
				items.put()		
		'''				
		#SuperZones(super_zone_name = "Galleria 3").put()
		
		#g3_3 = Areas.get_by_id(2345)
		#BSSIDZones(zones = g3_3, mac_address = "00:1f:45:64:0f:41").put()
		super_zones = db.GqlQuery("SELECT * "
					"FROM SuperZones ")
		bssid_query = db.GqlQuery("SELECT * "
				"FROM BSSIDZones ")
		area_query = db.GqlQuery("SELECT * "
						"FROM Areas "
						"ORDER BY zone_id")
		user_query = db.GqlQuery("SELECT * "
					"FROM Users ")
		friend_query = db.GqlQuery("SELECT * "
					"FROM Friends ")
					
		event_query =  db.GqlQuery("SELECT * "
					"FROM Events ")

		
		template_values = {
    		'bssid': bssid_query,
			'areas': area_query,
			'friends' : friend_query,
			'users' : user_query,
			'events' : event_query,
			'super_zones' : super_zones
		}
		path = os.path.join(os.path.dirname(__file__) + '/../templates/', 'index.html')
		self.response.out.write(template.render(path,template_values))


class RequestHandler(webapp.RequestHandler):
	
	def post(self, request_type):
		try:
			
			json_obj = json.loads(self.request.body)
			input = json.dumps(json_obj)
			#print str(input)
			#print "Json object recieved: ", input
			logging.debug("JSON object recieved to RequestHandler: " + str(input))
		except:
			# if json is empty
			logging.error("No JSON received")
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"status" : 11}))
			return
		try:
			#get a list of friends and friends info
			if (request_type == "friendlist"):
	
				getFriendList(self, json_obj)
			
			#send friendship request
			elif (request_type == "friendship"):
				
				sendFriendRequest(self, json_obj)
			
			#get a list of pending friend request
			elif (request_type == "pending/friendships"):
	
				getFriendRequests(self, json_obj)
			
			#get zone_id by mac_address and 
			elif (request_type == "zone"):
				
				updateZone(self, json_obj)
			
			elif (request_type == "events"):
				
				getEvents(self, json_obj)
			
			else: 	
				logging.error("request type unknown")
				self.response.headers['Content-Type'] = "application/json"
				self.response.out.write(json.dumps({"status" : 12}))
		except:
			logging.error("request fail")
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"status" : 13}))
					

class AcceptHandler(webapp.RequestHandler):
	def post(self, accept_type):
		try:
			json_obj = json.loads(self.request.body)
			input = json.dumps(json_obj)
			logging.debug("JSON object received to AcceptHandler: " + str(input))

			if (accept_type == "friendship"):
				acceptFriendRequest(self, json_obj)
						
		except:
			# if json is empty
			logging.error("No JSON received")
			self.response.headers['Content-Type'] = "application/json"
			self.response.out.write(json.dumps({"status" : 11}))
					
	
class EventCreator(webapp.RequestHandler):

	def get(self):
		
		template_values = {
	    		'eventform' : EventForm()
			}
		path = os.path.join(os.path.dirname(__file__) + '/../templates/', 'event.html')
		self.response.out.write(template.render(path,template_values))
	
	def post(self):
		data  = EventForm(data=self.request.POST)
		if data.is_valid():
			entity = data.save(commit = False)
			entity.put()
			print "event created"
			self.redirect('/')
		else:
			template_values = {
	    		'eventform' : data
			}
			path = os.path.join(os.path.dirname(__file__) + '/../templates/', 'event.html')
			self.response.out.write(template.render(path,template_values))



	
def pretty_date(time=False):
		"""
		Get a datetime object or a int() Epoch timestamp and return a
		pretty string like 'an hour ago', 'Yesterday', '3 months ago',
		'just now', etc
		"""
		from datetime import datetime
		now = datetime.now()
		if type(time) is int:
		    diff = now - datetime.fromtimestamp(time)
		elif isinstance(time,datetime):
		    diff = now - time 
		elif not time:
		    diff = now - now
		second_diff = diff.seconds
		day_diff = diff.days

		if day_diff < 0:
		    return ''

		if day_diff == 0:
		    if second_diff < 10:
		        return "just now"
		    if second_diff < 60:
		        return str(second_diff) + " seconds ago"
		    if second_diff < 120:
		        return  "a minute ago"
		    if second_diff < 3600:
		        return str( second_diff / 60 ) + " minutes ago"
		    if second_diff < 7200:
		        return "an hour ago"
		    if second_diff < 86400:
		        return str( second_diff / 3600 ) + " hours ago"
		if day_diff == 1:
		    return "Yesterday"
		if day_diff < 7:
		    return str(day_diff) + " days ago"
		if day_diff < 31:
		    return str(day_diff/7) + " weeks ago"
		if day_diff < 365:
		    return str(day_diff/30) + " months ago"
		return str(day_diff/365) + " years ago"
		

	



# configure the rest dispatcher to know what prefix to expect on request urls
rest.Dispatcher.base_url = "/rest"

# add all models from the current module, and/or...
rest.Dispatcher.add_models_from_module(__name__)

# add all models from some other module, and/or...
#rest.Dispatcher.add_models_from_module(my_model_module)
# add specific models
#rest.Dispatcher.add_models({"zones": Zones})
  
# add specific models (with given names) and restrict the supported methods
#rest.Dispatcher.add_models({'zones' : (Zones, rest.READ_ONLY_MODEL_METHODS)})

# use custom authentication/authorization
#rest.Dispatcher.authenticator = MyAuthenticator()
#rest.Dispatcher.authorizer = MyAuthorizer()

#('/import', CSVImporter)
                                     
def main():
	application = webapp.WSGIApplication([
										('/', MainPage), 
										('/request/(.*)', RequestHandler),
										('/accept/(.*)', AcceptHandler),
										('/getmap/(.*)', MapHandler),
										('/rest/.*', rest.Dispatcher),
										('/getuserid/', GetUserId),
										('/setfriend/', SetFriend),
										('/setuser/', SetUser),
										('/event', EventCreator)],
										debug=True)
	run_wsgi_app(application)

if __name__ == "__friends__":
    main()