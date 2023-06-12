(function(){// avoid variables ending up in the global scope
	let pageOrchestrator = new PageOrchestrator();
	let coursesList, courseAppeals, evaluationDetails;

	window.addEventListener("load", () => {
		if (JSON.parse(sessionStorage.getItem("user")).matricola == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // initialize the components
			pageOrchestrator.refresh();
		} // display initial content
	}, false);

	// constructors
	function PersonalMessage(_message, messagecontainer) {
		this.message = _message;
		this.show = function() {
			messagecontainer.textContent = this.message;
		}
		this.refresh = function(){
			messagecontainer.textContent = "";
		}
	}

	function CoursesList(_alert, _listcontainer) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}

		this.show = function(next) {
			var self = this;
			makeCall("GET", "GoToHomeStudent", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var coursesToShow = JSON.parse(req.responseText);
							if (coursesToShow.length == 0) {
								self.alert.textContent = "No courses are attended by this student";
								return;
							}
							self.update(coursesToShow); // self visible by closure
							if (next) next(); // show the default element of the list if present

						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('user');
						}
						else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(arrayCourses) {
			var listEl, anchor, linkText;
			this.listcontainer.innerHTML = ""; // empty the table body
			// build updated list
			var self = this;
			arrayCourses.forEach(function(course) { // self visible here, not this
				listEl = document.createElement("li");
				anchor = document.createElement("a");
				listEl.appendChild(anchor);
				linkText = document.createTextNode(course.nome);
				anchor.appendChild(linkText);
				//anchor.idCorso= course.id; // make list item clickable
				anchor.setAttribute('idCorso', course.id); // set a custom HTML attribute
				anchor.addEventListener("click", (e) => {
					courseAppeals.listcontainer = e.target.parentNode;
					// dependency via module parameter
					courseAppeals.show(e.target.getAttribute("idCorso")); // the list must know the details container
				}, false);
				anchor.href = "#";
				self.listcontainer.appendChild(listEl);
			});
			this.listcontainer.style.visibility = "visible";

		}

		/*	    this.autoclick = function(courseId) {
				  var e = new Event("click");
				  var selector = "a[courseid='" + courseId + "']";
				  var anchorToClick =  // the first mission or the mission with id = missionId
					(courseId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
				  if (anchorToClick) anchorToClick.dispatchEvent(e);
				}*/
	}

	function CourseAppeals(_alert) {
		this.alert = _alert;
		this.listcontainer;
		this.idCorso;
		this.appealcontainer;

		this.show = function(courseId) {
			this.idCorso = courseId;
			var self = this;
			makeCall("GET", "GoToHomeStudentAppeals?idCorso=" + self.idCorso, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var appealsToShow = JSON.parse(message);
							if (appealsToShow.length == 0) {
								self.alert.textContent = "No appeals for this course!";
								return;
							}
							if(evaluationDetails.appealResult != undefined){
								evaluationDetails.appealResult.refresh();
							}
							evaluationDetails.evaluationContainer.style.visibility = "hidden";
							
							self.alert.textContent = "";
							self.update(appealsToShow); // self visible by closure
							//if (next) next(); // show the default element of the list if present

						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('user');
						}
						else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(appealsArray) {
			var listEl, anchor, linkText;
			if (this.appealcontainer != undefined) {
				this.appealcontainer.remove();
			}

			// build updated list
			var self = this;
			this.appealcontainer = document.createElement("ul");
			this.listcontainer.append(this.appealcontainer);
			appealsArray.forEach(function(appeal) { // self visible here, not this
				listEl = document.createElement("li");
				anchor = document.createElement("a");
				self.appealcontainer.appendChild(listEl);
				listEl.appendChild(anchor);
				linkText = document.createTextNode(appeal.data);
				anchor.appendChild(linkText);
				//anchor.missionid = mission.id; // make list item clickable
				anchor.setAttribute('appealdate', appeal.data); // set a custom HTML attribute
				anchor.addEventListener("click", (e) => {
					// MOSTRO la valutazione
					evaluationDetails.show(self.idCorso, appeal.data);
				}, false);
				anchor.href = "#";
				//self.listcontainer.appendChild(listEl);
			});
			this.listcontainer.style.visibility = "visible";
		};

		this.reset = function() { };
	}
	
	function EvaluationDetails(alert, evaluationContainer, evaluationBody){
		this.alert = alert;
		this.evaluationContainer = evaluationContainer;
		this.evaluationBody = evaluationBody;
		this.appealResult;

		this.show = function(courseId, appealDate) {
			var self = this;
			makeCall("GET", "GetEvaluationByAppeal?idCorso=" + courseId + "&dataAppello=" + appealDate, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var obj = JSON.parse(req.responseText);
							//console.log(obj);
							// Crea un oggetto JavaScript vuoto per conservare gli oggetti ricostruiti
							var evaluationData = {};

							// Scorrere tutte le chiavi nell'oggetto JSON
							for (var key in obj) {
								if (obj.hasOwnProperty(key)) {
									var entry = obj[key];
									var value = JSON.parse(entry);
									evaluationData[key] = value;
								}
								//console.log(key +" " + value)
							}
							//self.evaluationContainer.style.visibility = "hidden";
														
							/*if (obj == undefined) {
								if(self.appealResult != undefined){
									self.evaluationContainier.style.visibility = "hidden";
									self.appealResult.refresh();
								}
								self.alert.textContent = "No evaluation for this appeal!";
								return;
							}*/

							self.alert.textContent = "";
							self.update(evaluationData);
							//self.evaluationContainer.style.visibility = "visible";
						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('user');
						}
						else {
							self.alert.textContent = message;

						}
					}
				}
			);
		};
		
		
		
		this.update = function(evaluationData){
			var self = this;
			if(this.appealResult!= undefined){
				this.appealResult.refresh();
			}
			if(evaluationData.mark == undefined){
				this.evaluationContainer.style.visibility = "hidden";
				this.appealResult = new PersonalMessage("Evaluation for the "+evaluationData.dataAppello+" appeal not yet defined",
					document.getElementById("id_appealresult"));
				this.appealResult.show();
				
			} else {
			
				while (this.evaluationBody.firstChild) {
					this.evaluationBody.removeChild(this.evaluationBody.firstChild);
				}
				this.appealResult = new PersonalMessage("These is your result for the " + evaluationData.dataAppello + " appeal:",
					document.getElementById("id_appealresult"));
				this.appealResult.show();
				
				var newrow = document.createElement("tr");
				this.evaluationBody.appendChild(newrow);
				var newidnumber = document.createElement("td");
				newidnumber.textContent = evaluationData.userId;
				var newname = document.createElement("td");
				newname.textContent = evaluationData.nomeStudente;
				var newsurname = document.createElement("td");
				newsurname.textContent = evaluationData.cognomeStudente;
				var newcourse = document.createElement("td");
				newcourse.textContent = evaluationData.nomeCorso;
				var newappeal = document.createElement("td");
				newappeal.textContent = evaluationData.dataAppello;
				var newmark = document.createElement("td");
				newmark.textContent = evaluationData.mark;
				var newevaluationstate = document.createElement("td");
				newevaluationstate.textContent = evaluationData.statoValutazione;
				newrow.append(newidnumber, newname, newsurname, newcourse, newappeal, newmark, newevaluationstate);
				
				this.evaluationContainer.style.visibility = "visible";
			}



		}


	}

	function PageOrchestrator() {
		var alertContainer = document.getElementById("id_alert");

		this.start = function() {
			let personalMessageName = new PersonalMessage(JSON.parse(sessionStorage.getItem('user')).nome,
				document.getElementById("id_nome"));
			personalMessageName.show();

			let personalMessageSurname = new PersonalMessage(JSON.parse(sessionStorage.getItem('user')).cognome,
				document.getElementById("id_cognome"));
			personalMessageSurname.show();

			coursesList = new CoursesList(
				alertContainer,
				document.getElementById("id_courses"));

			courseAppeals = new CourseAppeals(alertContainer);
			
			evaluationDetails = new EvaluationDetails(alertContainer,document.getElementById("id_evaluationcontainer"),document.getElementById("id_evaluationbody"),document.getElementById("id_appealresult"));
			
			/*	      
					  missionDetails.registerEvents(this); // the orchestrator passes itself --this-- so that the wizard can call its refresh function after updating a mission
			
					  wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
					  wizard.registerEvents(this);  // the orchestrator passes itself --this-- so that the wizard can call its refresh function after creating a mission
			
					  document.querySelector("a[href='Logout']").addEventListener('click', () => {
						window.sessionStorage.removeItem('username');
					  })*/
		};

		this.refresh = function(currentCourse) { // currentCourse initially null at start
			alertContainer.textContent = "";        // not null after creation of status change
			coursesList.reset();
			courseAppeals.reset();
			coursesList.show(function() {
				//coursesList.autoclick(currentMission); 
			}); // closure preserves visibility of this
			//wizard.reset();
		};
	}
})();