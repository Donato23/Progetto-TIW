(function(){// avoid variables ending up in the global scope
	let pageOrchestrator = new PageOrchestrator();
	let coursesList, courseAppeals, registeredStudentsDetails, singleStudentDetails, reportDetails;
	let publishButton, reportButton;

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
			publishButton.style.visibility = "hidden";
			reportButton.style.visibility = "hidden";
			reportDetails.reportContainer.style.visibility = "hidden";
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
							
							if(registeredStudentsDetails.registeredStudentsString != undefined){
								registeredStudentsDetails.registeredStudentsString.refresh();
							}
							registeredStudentsDetails.registeredstudentscontainer.style.visibility = "hidden";
							singleStudentDetails.studentDetailsContainer.style.visibility = "hidden";
							publishButton.style.visibility = "hidden";
							reportButton.style.visibility = "hidden";
							reportDetails.reportContainer.style.visibility = "hidden";
							
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
					// MOSTRO GLI ISCRITTI
					registeredStudentsDetails.show(self.idCorso, appeal.data);
				}, false);
				anchor.href = "#";
				//self.listcontainer.appendChild(listEl);
			});
			this.listcontainer.style.visibility = "visible";
		};

		this.reset = function() { };
	}
	

	function RegisteredStudentsDetails(_alert, registeredstudentscontainerbody, registeredstudentscontainer) {
		this.alert = _alert;
		this.registeredstudentscontainerbody = registeredstudentscontainerbody;
		this.registeredstudentscontainer = registeredstudentscontainer;
		this.registeredStudentsString;
		this.currentOrder = 'asc'; // Ordine corrente (inizialmente ascendente)

		this.show = function(courseId, appealDate) {
			var self = this;
			makeCall("GET", "GetRegisteredStudentsByAppeal?idCorso=" + courseId + "&dataAppello=" + appealDate, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var obj = JSON.parse(req.responseText);
							var objLength = 0;
							
							// Crea un oggetto JavaScript vuoto per conservare gli oggetti ricostruiti
							var reconstructedRegisteredStudents = {};

							// Scorrere tutte le chiavi nell'oggetto JSON
							for (var key in obj) {
								if (obj.hasOwnProperty(key)) {
									objLength += 1;
									var entry = obj[key];
									var originalKey = JSON.parse(entry.key);
									var value = JSON.parse(entry.value);
									// Ricostruisci l'oggetto originale utilizzando la chiave originale
									reconstructedRegisteredStudents[JSON.stringify(originalKey)] = value;
								}
							}
							
							singleStudentDetails.studentDetailsContainer.style.visibility = "hidden";
														
							if (objLength == 0) {
								if(self.registeredStudentsString != undefined){
									self.registeredstudentscontainer.style.visibility = "hidden";
									publishButton.style.visibility = "hidden";
									reportButton.style.visibility = "hidden";
									reportDetails.reportContainer.style.visibility = "hidden";
									self.registeredStudentsString.refresh();
								}
								self.alert.textContent = "No registered students for this appeal!";
								return;
							}

							self.alert.textContent = "";
							self.registeredstudentscontainer.style.visibility = "visible";
							publishButton.style.visibility = "visible";
							reportButton.style.visibility = "visible";
							reportDetails.reportContainer.style.visibility = "hidden";
							
							
							self.update(reconstructedRegisteredStudents, appealDate, courseId);
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

		this.update = function(registeredStudentsMap, appealDate, courseId) {
			var self = this;
			
			while (this.registeredstudentscontainerbody.firstChild) {
			  this.registeredstudentscontainerbody.removeChild(this.registeredstudentscontainerbody.firstChild);
			}
			
			publishButton.querySelector("input[type = 'hidden'][name = 'appealDate']").value = appealDate;
			publishButton.querySelector("input[type = 'hidden'][name = 'courseId']").value = courseId;
			reportButton.querySelector("input[type = 'hidden'][name = 'dataAppello']").value = appealDate;
			reportButton.querySelector("input[type = 'hidden'][name = 'idCorso']").value = courseId;
			
			this.registeredStudentsString = new PersonalMessage("These are the registered students for the " + appealDate + " appeal:",
					document.getElementById("id_registeredstudentsstring"));
			this.registeredStudentsString.show();

			var originalStudent;
			for (var user in registeredStudentsMap) {
				if (registeredStudentsMap.hasOwnProperty(user)) {					
					originalStudent = JSON.parse(user);
					var newrow = document.createElement("tr");
					this.registeredstudentscontainerbody.appendChild(newrow);
					var newidnumber = document.createElement("td");
					newidnumber.textContent = originalStudent.matricola;
					var newname = document.createElement("td");
					newname.textContent = originalStudent.nome;
					var newsurname = document.createElement("td");
					newsurname.textContent = originalStudent.cognome;
					var newemail = document.createElement("td");
					newemail.textContent = originalStudent.mail;
					var newdegree = document.createElement("td");
					newdegree.textContent = originalStudent.corsoDiLaurea;
					var newmark = document.createElement("td");
					newmark.textContent = registeredStudentsMap[user].mark;
					var newevaluationstate = document.createElement("td");
					newevaluationstate.textContent = registeredStudentsMap[user].statoValutazione;

					newrow.append(newidnumber, newname, newsurname, newemail, newdegree, newmark, newevaluationstate);
					
					
					
					
					// Se è possibile modificare il voto dello studente, aggiungo il pulsane modifica
					if(registeredStudentsMap[user].statoValutazione === "INSERITO" || registeredStudentsMap[user].statoValutazione === "NON_INSERITO"){
						var newButton = document.createElement("td");
						var newForm = document.createElement("form");
						//newForm.setAttribute("id", "id_modifybutton")
						newForm.setAttribute("action", "#");
						var newInputAppealDate = document.createElement("input");
						newInputAppealDate.setAttribute("type", "hidden");
						newInputAppealDate.setAttribute("name", "appealDate");
						newInputAppealDate.value = appealDate;
						var newInputCourseId = document.createElement("input");
						newInputCourseId.setAttribute("type", "hidden");
						newInputCourseId.setAttribute("name", "courseId");
						newInputCourseId.value = courseId;
						var newInputStudentId = document.createElement("input");
						newInputStudentId.setAttribute("type", "hidden");
						newInputStudentId.setAttribute("name", "studentId");
						newInputStudentId.value = originalStudent.matricola;
						var newInputModify = document.createElement("input");
						newInputModify.setAttribute("type", "button");
						newInputModify.setAttribute("name", "Modify");
						newInputModify.setAttribute("value", "Modify");
						
						newInputModify.addEventListener("click",  function(e) { // called when one clicks the button
							publishButton.style.visibility = "hidden";
							reportButton.style.visibility = "hidden";
							
					        let form = e.target.closest("form"); // example of DOM navigation from event object
					        if (form.checkValidity()) {
					          let appealDate = form.querySelector("input[type = 'hidden'][name = 'appealDate']").value;
					          let courseId = form.querySelector("input[type = 'hidden'][name = 'courseId']").value;
					          let studentId = form.querySelector("input[type = 'hidden'][name = 'studentId']").value;
					          
					          e.preventDefault();
					          
					          makeCall("GET", "ModifyEvaluation?appealDate=" + appealDate + "&courseId=" + courseId + "&studentId=" + studentId, form,
					            function(req) {
					              if (req.readyState === 4) { // response has arrived
					                let message = req.responseText; // get the body of the response
					                if (req.status === 200) { // if no errors
					                  singleStudentDetails.update(JSON.parse(message), appealDate, courseId, studentId);
					                } else {
					                  self.alert.textContent = message; // report the error contained in the response body
					                }
					              }
					            }
					          );
					        } else {
					          form.reportValidity(); // trigger the client-side HTML error messaging
					        }
						}, false);

						newForm.append(newInputAppealDate, newInputCourseId, newInputStudentId, newInputModify);
						newButton.appendChild(newForm);
						newrow.appendChild(newButton);
					}

					this.registeredstudentscontainerbody.appendChild(newrow);
				}
			}

		};
		
		this.sortByColumn = function(columnIndex, columnType) {
			var self = this;
			var rows = Array.from(self.registeredstudentscontainerbody.getElementsByTagName('tr'));
			
			this.detectOrder(columnIndex,columnType);
			
			rows.sort(function(a, b) {

				var valueA = a.cells[columnIndex].innerText.toLowerCase();
				var valueB = b.cells[columnIndex].innerText.toLowerCase();

				if (self.currentOrder === "desc") {//devo ordinare in modo ascendente ovvero invertire l'ordine
					if (columnType !== "mark") {
						if (valueA == undefined)
							return -1; //valore nullo più piccolo di qualsiasi altro valore;
						if (valueB == undefined)
							return 1;

						return valueA.localeCompare(valueB);
					} else {
						if (valueA == undefined)
							return -1; //valore nullo più piccolo di qualsiasi altro valore;
						if (valueB == undefined)
							return 1;
						if ((!isNaN(parseInt(valueA)) && !isNaN(parseInt(valueB)))
							|| (isNaN(parseInt(valueA)) && isNaN(parseInt(valueB)))) {
							return valueA.localeCompare(valueB);
						}
						if ((!isNaN(parseInt(valueA)) && isNaN(parseInt(valueB)))) {
							return 1;
						}
						if ((isNaN(parseInt(valueA)) && !isNaN(parseInt(valueB)))) {
							return -1;
						}
					}
				}
				else {

					if (columnType !== "mark") {
						if (valueA == undefined)
							return 1; //valore nullo più piccolo di qualsiasi altro valore;
						if (valueB == undefined)
							return -1;

						return valueB.localeCompare(valueA);
					} else {
						if (valueA == undefined)
							return 1; //valore nullo più piccolo di qualsiasi altro valore;
						if (valueB == undefined)
							return -1;
						if ((!isNaN(parseInt(valueA)) && !isNaN(parseInt(valueB)))
							|| (isNaN(parseInt(valueA)) && isNaN(parseInt(valueB)))) {
							return valueB.localeCompare(valueA);
						}
						if ((!isNaN(parseInt(valueA)) && isNaN(parseInt(valueB)))) {
							return -1;
						}
						if ((isNaN(parseInt(valueA)) && !isNaN(parseInt(valueB)))) {
							return 1;
						}
					}
				}

			});

			// Rimuovi le righe esistenti dalla tabella
			while (self.registeredstudentscontainerbody.firstChild) {
				self.registeredstudentscontainerbody.removeChild(self.registeredstudentscontainerbody.firstChild);
			}

			// Aggiungi le righe ordinate alla tabella
			rows.forEach(function(row) {
				self.registeredstudentscontainerbody.appendChild(row);
			});
			
		}
		
		this.detectOrder = function(columnIndex, columnType) {
			var self = this;
			var rows = Array.from(self.registeredstudentscontainerbody.getElementsByTagName('tr'));
			var valueA;
			var valueB;
			//Trovo il tipo di ordine corrente per la colonna selezionata
			if (columnType !== "mark") {
				for (var i = 0; i < rows.length - 1; i++) {
					valueA = rows[i].cells[columnIndex].innerText.toLowerCase();
					valueB = rows[i + 1].cells[columnIndex].innerText.toLowerCase();

					if (valueA.localeCompare(valueB) < 0) {
						self.currentOrder = "asc";
						break;
					} else if (valueA.localeCompare(valueB) > 0) {
						self.currentOrder = "desc";
						break;
					} else {
						self.currentOrder = "asc";
					}

				}
			}
			else {
				var comparation = 0;
				for (var i = 0; i < rows.length - 1; i++) {
					valueA = rows[i].cells[columnIndex].innerText.toLowerCase();
					valueB = rows[i + 1].cells[columnIndex].innerText.toLowerCase();
					if (valueA == undefined) {
						comparation = 1; //valore nullo più piccolo di qualsiasi altro valore;
					}
					else if (valueB == undefined) {
						comparation = -1;
					}
					else if ((!isNaN(parseInt(valueA)) && !isNaN(parseInt(valueB)))
						|| (isNaN(parseInt(valueA)) && isNaN(parseInt(valueB)))) {
						comparation = valueA.localeCompare(valueB);
					}
					else if ((!isNaN(parseInt(valueA)) && isNaN(parseInt(valueB)))) {
						comparation = 1;
					}
					else if ((isNaN(parseInt(valueA)) && !isNaN(parseInt(valueB)))) {
						comparation = -1;
					}


					if (comparation < 0) {
						self.currentOrder = "asc";
						break;
					} else if (comparation > 0) {
						self.currentOrder = "desc";
						break;
					} else {
						self.currentOrder = "asc";
					}

				}
			}
		}

	}
	
	function SingleStudentDetails(alert, studentDetailsContainer){
		this.alert = alert;
		this.studentDetailsContainer = studentDetailsContainer;
		
		this.update = function(studentDetails, appealDate, courseId, studentId){
			// setting visibility
			this.studentDetailsContainer.style.visibility = "visible";
			
			//setting student's data
			document.getElementById("id_studentid").textContent = studentDetails.matricola;
			document.getElementById("id_studentname").textContent = studentDetails.nome;
			document.getElementById("id_studentsurname").textContent = studentDetails.cognome;
			document.getElementById("id_studentemail").textContent = studentDetails.mail;
			document.getElementById("id_studentdegreecourse").textContent = studentDetails.corsoDiLaurea;
			
			// adding event listener for evaluation submit
			document.getElementById("id_modifyevaluationbutton").addEventListener("click", function(e){
				let form = e.target.closest("form"); // example of DOM navigation from event object
			    if (form.checkValidity()){
			      let newEvaluation = form.querySelector("select[name = 'evaluation']").value;
			      
			      e.preventDefault();
			      
			      makeCall("POST", "ModifyEvaluation?appealDate=" + appealDate + "&courseId=" + courseId + "&studentId=" + studentId + "&evaluation=" + newEvaluation, form,
			        function(req) { // callback of the POST HTTP request
			          if (req.readyState === 4) { // response has arrived
			            let message = req.responseText; // get the body of the response
			            if (req.status === 200) { // if no errors
			              registeredStudentsDetails.show(courseId, appealDate); // goes back to showing the table of registered students
			            } else {
			              alertContainer.textContent = message; // report the error contained in the response body
			            }
			          }
			        }
			      );
			    } else {
			      form.reportValidity(); // trigger the client-side HTML error messaging
			    }
			});
		};
		
		this.register = function(){};
	}
	
	function ReportDetails(alert, reportContainer, reportContainerBody){
		this.alert = alert;
		this.reportContainer = reportContainer;
		this.reportContainerBody = reportContainerBody;
		
		this.show = function(reportId, courseId, appealDate){
			var self = this;
			makeCall("GET", "CreateReport?idReport=" + reportId + "&idCorso=" + courseId + "&dataAppello=" + appealDate, null,
				function(req) {
					if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								if(JSON.parse(message) !== null){
									reportDetails.update(JSON.parse(message), courseId, appealDate);
								}
							}
						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('user');
						}
						else {
							self.alert.textContent = message;

						}
					}
			);
		}
		
		this.update = function(report, courseId, appealDate){	
			var self = this;
			
			document.getElementById("id_reportnumber").textContent = report.id;
			document.getElementById("id_reportdate").textContent = report.data;
			document.getElementById("id_reporttime").textContent = report.ora;
			document.getElementById("id_reportcourseid").textContent = courseId;
			document.getElementById("id_reportappealdate").textContent = appealDate;
			
			report.studentsData.forEach(function(sData){
				/*console.log(sData.studentId);
				console.log(sData.studentName);
				console.log(sData.studentSurname);
				console.log(sData.studentMark);*/
				
				var newRow = document.createElement("tr");
				var newStudentId = document.createElement("td");
				newStudentId.textContent = sData.studentId;
				var newStudentName = document.createElement("td");
				newStudentName.textContent = sData.studentName;
				var newStudentSurname = document.createElement("td");
				newStudentSurname.textContent = sData.studentSurname;
				var newStudentMark = document.createElement("td");
				newStudentMark.textContent = sData.studentMark;
				
				newRow.append(newStudentId, newStudentName, newStudentSurname, newStudentMark);
				self.reportContainerBody.appendChild(newRow);
			});
			
			this.reportContainer.style.visibility = "visible";
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

			registeredStudentsDetails = new RegisteredStudentsDetails(alertContainer, document.getElementById("id_registeredstudentscontainerbody"), document.getElementById("id_registeredstudentscontainer"));

			singleStudentDetails = new SingleStudentDetails(alertContainer, document.getElementById("id_studentdetailscontainer"));
			
			publishButton = document.getElementById("id_publishbutton");
			publishButton.addEventListener("click", function(e){
				let form = e.target.closest("form");
			    if (form.checkValidity()){
			      let appealDate = form.querySelector("input[type = 'hidden'][name = 'appealDate']").value;
				  let courseId = form.querySelector("input[type = 'hidden'][name = 'courseId']").value;
			      
			      e.preventDefault();
			      
			      makeCall("POST", "PublishEvaluation?appealDate=" + appealDate + "&courseId=" + courseId, form,
			        function(req) { 
			          if (req.readyState === 4) {
			            let message = req.responseText; 
			            if (req.status === 200) { 
			              registeredStudentsDetails.show(courseId, appealDate);
			            } else {
			              alertContainer.textContent = message;
			            }
			          }
			        }
			      );
			    } else {
			      form.reportValidity(); // trigger the client-side HTML error messaging
			    }
			}, false);
			
			reportDetails = new ReportDetails(alertContainer, document.getElementById("id_reportdetailscontainer"), document.getElementById("id_reportstudentdetailscontainerbody"));
			
			reportButton = document.getElementById("id_createreportbutton");
			reportButton.addEventListener("click", function(e){
				let form = e.target.closest("form");
			    if (form.checkValidity()){
			      let appealDate = form.querySelector("input[type = 'hidden'][name = 'dataAppello']").value;
				  let courseId = form.querySelector("input[type = 'hidden'][name = 'idCorso']").value;
			      
			      e.preventDefault();
			      
			      makeCall("POST", "CreateReport?dataAppello=" + appealDate + "&idCorso=" + courseId, form,
			        function(req) { 
			          if (req.readyState === 4) {
			            let message = req.responseText; 
			            if (req.status === 200) { 
							if(message!== null && JSON.parse(message) !== -1){
								registeredStudentsDetails.show(courseId, appealDate);
								reportDetails.show(JSON.parse(message), courseId, appealDate);
							}else{
								alertContainer.textContent = "Impossible to create a new report, there are no published evaluations";
							}
			            } else {
			              	alertContainer.textContent = message;
			            }
			          }
			        }
					);
				} else {
					form.reportValidity(); // trigger the client-side HTML error messaging
				}
			}, false);
			
			
			// Aggiungi un gestore di eventi di clic alle etichette delle colonne
			var columnLabels = document.querySelectorAll('.column-label');

			columnLabels.forEach(function(label) {
				console.log("label" + label);
				var anchor = label.querySelector('a');
				var columnType = anchor.getAttribute("data-column");
				label.querySelector('a').addEventListener('click', function(event) {
					// Ottieni l'indice dell'ancora cliccata all'interno delle etichette di colonna
					columnIndex = Array.from(label.parentNode.children).indexOf(label);

					// Utilizza l'indice della colonna per eseguire le operazioni desiderate
					console.log("Colonna selezionata: " + columnIndex);
					console.log("columnType: " + columnType);
					registeredStudentsDetails.sortByColumn(columnIndex, columnType);
				});
			});

			
			/*	      
					  missionDetails.registerEvents(this); // the orchestrator passes itself --this-- so that the wizard can call its refresh function after updating a mission
			
					  wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
					  wizard.registerEvents(this);  // the orchestrator passes itself --this-- so that the wizard can call its refresh function after creating a mission
			
					  document.querySelector("a[href='Logout']").addEventListener('click', () => {
						window.sessionStorage.removeItem('username');
					  })*/
		};

		this.refresh = function(currentCourse) { // currentMission initially null at start
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