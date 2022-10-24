(function() {
	
	angular.module('axelor.scheduler', ['underscore'], function($locationProvider) {
		$locationProvider.html5Mode({
			  enabled: true,
			  requireBase: false
		});
    })
	.controller('SchdulerController', ['$http','$scope', '_', '$location','$window', function($http, $scope, _, $location, $window) {
		
		var planingObj = "com.axelor.apps.project.db.ProjectPlanning";
		var projectObj = "com.axelor.apps.project.db.Project";
		var userObj = "com.axelor.auth.db.User";
		var taskObj = "com.axelor.team.db.TeamTask";
		var view_name = "timeline";
		
		scheduler.locale.labels.timeline_tab = "Month";
		scheduler.locale.labels.section_custom="Section";
		scheduler.config.details_on_create=false;
		scheduler.config.drag_create = true;
		scheduler.config.dblclick_create = true;
		scheduler.config.details_on_dblclick=false;
		scheduler.config.xml_date="%Y-%m-%d %H:%i";
		
		scheduler.config.show_loading = true;
		
		scheduler.attachEvent("onEventChanged", updateRecord);
		scheduler.attachEvent("onEventDeleted", deleteRecord);
		scheduler.attachEvent("onEventAdded", addRecord);
		
		
		scheduler.attachEvent("onEventCreated", function(id,e){
		    scheduler.getEvent(id).timepercent = 100;
		    scheduler.updateEvent(id); // renders the updated event
		    return true;
		});
		
		scheduler.attachEvent("onTemplatesReady", function(){
			scheduler.xy.scale_height = 50;
		});

		scheduler.templates.event_bar_text = function(start,end,event){
		      return event.timepercent + "% " +  event.text;
		};
		
		
		scheduler.locale.labels.section_timepercent = "Time %";
		var timepercent_options = [
			{key:0 , label:"0%"},
			{key:10 , label:"10%"},
			{key:20 , label:"20%"},
			{key:30 , label:"30%"},
			{key:40 , label:"40%"},
			{key:50 , label:"50%"},
			{key:60 , label:"60%"},
			{key:70 , label:"70%"},
			{key:80 , label:"80%"},
			{key:90 , label:"90%"},
			{key:100 , label:"100%"}
		];
		
		scheduler.locale.labels.section_task = "Task";
		var tasks = [];
		
		scheduler.attachEvent("onBeforeLightbox", updateLightBox);
		
		scheduler.locale.labels.section_tasks = "Tasks";
		
		
		if ($location.search().userIds) {
			fetchUsers();
		}
		
		function fetchUsers() {
			var userIds = $location.search().userIds.split(",");
			$http.post("../../ws/rest/" + userObj + "/search", {
				"offset": 0,
				"fields": ["fullName","id"],
				"data": {
					"_domain": "self.id in :userIds",
					"_domainContext": {userIds :userIds }
				}
			}).success(function(response){
				var users = response.data;
				if (users) {
					fetchProjects(users, userIds);
				}
			});
		}
		
		function fetchProjects(users, userIds) {
			
			$http.post("../../ws/rest/" + projectObj + "/search", {
				"offset": 0,
				"fields": ["fullName","id","membersUserSet"],
				"data": {
					"_domain": "self.membersUserSet.id in :userIds",
					"_domainContext": {userIds :userIds}
				}
			}).success(function(response){
				var projects = response.data;
				if (projects) {
					projects = _.uniq(projects, function(p){ return p.id; });
					fetchTasks(users, projects);
				}
			});
		}
		
		function fetchTasks(users, projects){
			
			$http.post("../../ws/rest/" + taskObj + "/search", {
				"offset": 0,
				"fields": ["name","id","project"],
				"data": {
					"_domain": "self.project.id in (:projectIds)",
					"_domainContext": {projectIds :_.map(projects, function (p){return p.id})}
				}
			}).success(function(response){
				if(response.data) {
					tasks = _.map(response.data, function(t){return {key:t.id, label:t.name, project:t.project.id}});
				}
				var groupData = createGroupData(users, projects);
				initScheduler(groupData);
			});
			
			
		}
	
		$scope.refreshData = function() {
			$window.location.reload();
		}
		
		function initScheduler(groupData) {
			
			$http.post("../../ws/rest/" + planingObj + "/search", {
				"offset": 0,
				"sortBy": ["fromDate"],
				"fields": ["fromDate","toDate","project","user","task","description","timepercent"],
				"data": {}
			}).success(function(response){
				var plannigData = createPlanningData(response.data);

				scheduler.createTimelineView({
					section_autoheight: false,
					name:	view_name,
					x_unit:	"day",
					x_date: "%D<br><b>%j</b>",
					x_step: 1,
					x_size: 31,
					y_unit: groupData,
					y_property:	"section_id",
					render: "tree",
					folder_dy:20,
					round_position:true,
					dy:60
				});
				scheduler.date[view_name + '_start'] = scheduler.date.month_start;
				scheduler.init('scheduler_here',new Date(),"timeline");
				scheduler.attachEvent("onBeforeViewChange", function(old_mode,old_date,mode,date){
					var year = date.getFullYear();
					var month= (date.getMonth() + 1);
					var d = new Date(year, month, 0);
					scheduler.matrix[view_name].x_size = d.getDate();//number of days in month;
					return true;
				});
				
				scheduler.date['add_' + view_name] = function(date, step){
					if(step > 0){
						step = 1;
					}else if(step < 0){
						step = -1;
					}
					return scheduler.date.add(date, step, "month")
				};
				
				scheduler.parse(plannigData,"json");
			});
			
		}
		
		function formatDate(date) {
			return moment(date, moment.ISO_8601, true).format("YYYY-MM-DD")
		}
		
		function createGroupData(users, projects) {
			var userGroup = _.map(users, function(user) {
				return {key:user.id, label:user.fullName, open: true,children: []}
			});
			
			_.each(projects, function(project) {
				_.each(project.membersUserSet, function(member){
					var user = _.findWhere(userGroup, {key:member.id});
					if (user){
						user.children.push({key:member.id + "-" + project.id, label: project.fullName});
					}
				});
			});
			
			
			return userGroup;
			
		}
		
		function createPlanningData(data) {
			
			var planinngData = [];
			
			if (data) {
				planinngData= _.map(data, function(plan){
						return {
						"start_date" : formatDate(plan.fromDate),
						"end_date": formatDate(plan.toDate),
						"text": plan.description,
						"section_id": plan.user.id + "-" + plan.project.id,
						"recordId": plan.id,
						"version" : plan.version,
						"task": plan.task ? plan.task.id : null,
						"timepercent": plan.timepercent ? plan.timepercent : 100
						}
				});
			}
						
			return planinngData;
		}
		
		function updateRecord(lineId, line) {
			var ids = line.section_id.split("-")
			var datas = {
					"id" : line.recordId,
					"version" : line.version,
					"fromDate" : line.start_date.toJSON(),
					"toDate": line.end_date.toJSON(),
					"description": line.text,
					"user": {"id" : ids[0]},
					"project": {"id" : ids[1]},
					"timepercent": line.timepercent
			}
			if (line.task) {
				datas["task"] = {"id": line.task}
			}
			$http.post("../../ws/rest/" + planingObj + "/" + line.recordId, {
				data : datas
			}).success(function(response){
				line.version = response.data[0].version;
				updateTotalPlannedHrs(ids[1], line.task);
			});
		}
		
		function deleteRecord(lineId, line) {
			if (line.recordId) {
				$http.delete("../../ws/rest/" + planingObj + "/" + line.recordId).success(function(response){
					updateTotalPlannedHrs(line.section_id.split("-")[1], line.task);
				});
			}
		}
		
		function addRecord(lineId, line) {
			console.log("Add record : "+ line.timepercent);
			if (line.section_id) {
				var ids = line.section_id.split("-")
				var datas = {
					"fromDate" : line.start_date.toJSON(),
					"toDate": line.end_date.toJSON(),
					"description": line.text,
					"user": {"id" : ids[0]},
					"project": {"id" : ids[1]},
					"timepercent": line.timepercent
				}
				if (line.task) {
					datas["task"] = {"id": line.task}
				}
				$http.put("../../ws/rest/" + planingObj, {
					data : datas
				}).success(function(response){
					line.version = 0;
					line.recordId = response.data[0].id;
					updateTotalPlannedHrs(ids[1], line.task);
				})
			}
		}
		
		function updateLightBox(id) {
			scheduler.resetLightbox();
			var ev = scheduler.getEvent(id);
			scheduler.config.lightbox.sections = [
				{ name: "description", height: 50, map_to: "text", type: "textarea", focus: true },
				{ name: "task", options: _.filter(tasks, function(t){return t.project == ev.section_id.split('-')[1]}), map_to: "task", type: "combo",  height:30, filtering: true},
				{ name: "timepercent", options: timepercent_options, map_to: "timepercent", type: "combo",  height:30 },
				{ name: "time", height: 72, type: "time", map_to: "auto"}
			];
			
			return true;
		}
		
		function updateTotalPlannedHrs(projectId, taskId) {
			
			$http.post("../../ws/action",{
				action : "action-project-method-update-project-planned-time",
				model: planingObj,
				data : {
					"project" : {"id":projectId}
				}
			}).success(function(response){
			});
			
			if (taskId) {
				$http.post("../../ws/action",{
					action : "action-project-method-update-task-planned-time",
					model: planingObj,
					data : {
						"task" : {"id":taskId}
					}
				}).success(function(response){
				});
			}
			
		}
		
	}])
	
}).call(this);