(function(){// avoid variables ending up in the global scope
	let pageOrchestrator = new PageOrchestrator();
	let coursesList, courseAppeals, studentsDetails;
	
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
	}
	 
	function CoursesList(_alert, _listcontainer) {
	    this.alert = _alert;
	    this.listcontainer = _listcontainer;

	    this.reset = function() {
	      	this.listcontainer.style.visibility = "hidden";
	    }

	    this.show = function(next) {
	      var self = this;
	      makeCall("GET", "GoToHomeProfessor", null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var coursesToShow = JSON.parse(req.responseText);
	              if (coursesToShow.length == 0) {
	                self.alert.textContent = "No courses are taught by this professor!";
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
	          }}
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
	
	function CourseAppeals(_alert){
		this.alert = _alert;
	    this.listcontainer;
	    this.idCorso;
	    this.appealcontainer;

		this.show = function(courseId){
			this.idCorso = courseId;
			var self = this;
		  makeCall("GET", "GoToHomeProfessorAppeals?idCorso=" + self.idCorso, null,
		    function(req) {
		      if (req.readyState == 4) {
		        var message = req.responseText;
		        if (req.status == 200) {
		          var appealsToShow = JSON.parse(message);
		          if (appealsToShow.length == 0) {
		            self.alert.textContent = "No appeals for this course!";
		            return;
		          }
		          self.update(appealsToShow); // self visible by closure
		          //if (next) next(); // show the default element of the list if present
		        
		      } else if (req.status == 403) {
		          window.location.href = req.getResponseHeader("Location");
		          window.sessionStorage.removeItem('user');
		          }
		          else {
		        self.alert.textContent = message;
		      }}
		    }
		  );
		};
		
		this.update = function(appealsArray){
			var listEl, anchor, linkText;
			if(this.appealcontainer != undefined){
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
				// MOSTRO GLI ISCRITTI
				studentsDetails.show(self.idCorso, appeal.data);
	        }, false);
	        anchor.href = "#";
	        //self.listcontainer.appendChild(listEl);
	      });
	      this.listcontainer.style.visibility = "visible";
		};
		
		this.reset = function(){};
	}
	
	function StudentsDetails(alert, registeredstudentscontainerbody){
		this.alert = alert;
		this.registeredstudentscontainerbody = registeredstudentscontainerbody;
	    
	    this.show = function(courseId, appealDate){
			var self = this;
	      makeCall("GET", "GetRegisteredStudentsByAppeal?idCorso=" + courseId + "&dataAppello=" + appealDate, null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var registeredStudents = JSON.parse(req.responseText);
	              console.log(typeof registeredStudent);
	              self.update(registeredStudents); // self is the object on which the function
	              // is applied
	              self.registeredstudentscontainer.style.visibility = "visible";

/*	            PER IL PULSANTE MODIFICA  
				switch (registeredStudents.evaluationstate) {
	                case "OPEN":
	                  self.expensecontainer.style.visibility = "hidden";
	                  self.expenseform.style.visibility = "visible";
	                  self.expenseform.missionid.value = mission.id;
	                  self.closeform.style.visibility = "hidden";
	                  break;
	                case "REPORTED":
	                  self.expensecontainer.style.visibility = "visible";
	                  self.expenseform.style.visibility = "hidden";
	                  self.closeform.missionid.value = mission.id;
	                  self.closeform.style.visibility = "visible";
	                  break;
	                case "CLOSED":
	                  self.expensecontainer.style.visibility = "visible";
	                  self.expenseform.style.visibility = "hidden";
	                  self.closeform.style.visibility = "hidden";
	                  break;
	              }*/
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
	    
	    this.update = function(registeredStudentsMap){
			var self = this;
			
			var newrow = document.createElement("tr");
			this.registeredstudentscontainerbody.appendChild(newrow);
			
			registeredStudentsMap.forEach(function(student, mark){
				var newrow = document.createElement("tr");
				this.registeredstudentscontainerbody.appendChild(newrow);
				
				var newidnumber = document.createElement("td");
				newidnumber.textContent = student.matricola;
				var newname = document.createElement("td");
				newname.textContent = student.nome;
				var newsurname = document.createElement("td");
				newsurname.textContent = student.cognome;
				var newemail = document.createElement("td");
				newemail.textContent = student.mail;
				var newdegree = document.createElement("td");
				newdegree.textContent = student.corsodilaurea;
				var newmark = document.createElement("td");
				newmark.textContent = mark.voto;
				var newevaluationstate = document.createElement("td");
				newevaluationstate.textContent = mark.statovalutazione;
				
				newrow.append(newidnumber, newname, newsurname, newemail, newdegree, newmark, newevaluationstate);
			})

		};
	}
	
	function PageOrchestrator(){
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
	      
	      studentsDetails = new StudentsDetails(alertContainer, document.getElementById("id_registeredstudentscontainerbody"));

/*	      
	      missionDetails.registerEvents(this); // the orchestrator passes itself --this-- so that the wizard can call its refresh function after updating a mission

	      wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
	      wizard.registerEvents(this);  // the orchestrator passes itself --this-- so that the wizard can call its refresh function after creating a mission

	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })*/
	    };
	    
	   this.refresh = function(currentMission) { // currentMission initially null at start
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