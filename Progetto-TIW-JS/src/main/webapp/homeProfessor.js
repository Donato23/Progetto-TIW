(function(){// avoid variables ending up in the global scope
	let pageOrchestrator = new PageOrchestrator();
	let coursesList, courseAppeals, registeredStudentsDetails, singleStudentDetails, reportDetails, multipleInsertionModalPage;
	let publishButton, reportButton, multipleInsertionButton, sendMultipleInsertionButton;
	let multipleInsertionModal;

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
			multipleInsertionButton.style.visibility = "hidden";
		}

		this.show = function() {
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
							multipleInsertionButton.style.visibility = "hidden";
							
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
							var registeredStudents = JSON.parse(req.responseText);
							
							singleStudentDetails.studentDetailsContainer.style.visibility = "hidden";
													
							if (message === "[]") { // the server's response is empty
								if(self.registeredStudentsString != undefined){
									self.registeredstudentscontainer.style.visibility = "hidden";
									publishButton.style.visibility = "hidden";
									reportButton.style.visibility = "hidden";
									reportDetails.reportContainer.style.visibility = "hidden";
									multipleInsertionButton.style.visibility = "hidden";
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
							multipleInsertionButton.style.visibility = "visible";
							
							self.update(registeredStudents, appealDate, courseId);
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
			
			console.log(registeredStudentsMap);
			
			publishButton.querySelector("input[type = 'hidden'][name = 'appealDate']").value = appealDate;
			publishButton.querySelector("input[type = 'hidden'][name = 'courseId']").value = courseId;
			reportButton.querySelector("input[type = 'hidden'][name = 'dataAppello']").value = appealDate;
			reportButton.querySelector("input[type = 'hidden'][name = 'idCorso']").value = courseId;
			multipleInsertionButton.querySelector("input[type = 'hidden'][name = 'dataAppello']").value = appealDate;
			multipleInsertionButton.querySelector("input[type = 'hidden'][name = 'idCorso']").value = courseId;
			
			this.registeredStudentsString = new PersonalMessage("These are the registered students for the " + appealDate + " appeal:",
					document.getElementById("id_registeredstudentsstring"));
			this.registeredStudentsString.show();

			registeredStudentsMap.forEach(function(user){
					var newrow = document.createElement("tr");
					self.registeredstudentscontainerbody.appendChild(newrow);
					var newidnumber = document.createElement("td");
					newidnumber.textContent = user.studentId;
					var newname = document.createElement("td");
					newname.textContent = user.studentName;
					var newsurname = document.createElement("td");
					newsurname.textContent = user.studentSurname;
					var newemail = document.createElement("td");
					newemail.textContent = user.studentEmail;
					var newdegree = document.createElement("td");
					newdegree.textContent = user.studentDegree;
					var newmark = document.createElement("td");
					newmark.textContent = user.studentMark;
					var newevaluationstate = document.createElement("td");
					newevaluationstate.textContent = user.studentEvaluationState;

					newrow.append(newidnumber, newname, newsurname, newemail, newdegree, newmark, newevaluationstate);
					
					if(user.studentEvaluationState === "INSERITO" || user.studentEvaluationState === "NON_INSERITO"){
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
						newInputStudentId.value = user.studentId;
						var newInputModify = document.createElement("input");
						newInputModify.setAttribute("type", "button");
						newInputModify.setAttribute("name", "Modify");
						newInputModify.setAttribute("value", "Modify");
						
						newInputModify.addEventListener("click",  function(e) { // called when one clicks the button
							publishButton.style.visibility = "hidden";
							reportButton.style.visibility = "hidden";
							multipleInsertionButton.style.visibility = "hidden";
							
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
					
					self.registeredstudentscontainerbody.appendChild(newrow);
			});
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
	
	function ModalPage(alert, modalContainerBody, modalContainer){
		this.alert = alert;
		this.modalContainerBody = modalContainerBody;
		this.modalContainer = modalContainer;
		this.registeredStudentsWithoutEvaluation;
		
		this.show = function(courseId, appealDate) {
			var self = this;
			makeCall("GET", "GetRegisteredStudentsWithoutEvaluationByAppeal?idCorso=" + courseId + "&dataAppello=" + appealDate, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							self.registeredStudentsWithoutEvaluation = JSON.parse(req.responseText);
							
							singleStudentDetails.studentDetailsContainer.style.visibility = "hidden";
													
							if (message === "[]") { // the server's response is empty
								self.alert.textContent = "Function not available: there are no students without an evaluation!";
								return;
							}
							
							multipleInsertionModal.style.display = "block";
							self.update(self.registeredStudentsWithoutEvaluation, appealDate, courseId);
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

		this.update = function(registeredStudentsWithoutEvaluation, appealDate, courseId) {
			var self = this;
			
			while (this.modalContainerBody.firstChild) {
			  this.modalContainerBody.removeChild(this.modalContainerBody.firstChild);
			}
			
			let form = document.getElementById("id_multipleinsertionsubmitbutton");
			form.querySelector("input[type = 'hidden'][name = 'appealDate']").value = appealDate
			form.querySelector("input[type = 'hidden'][name = 'courseId']").value = courseId;

			registeredStudentsWithoutEvaluation.forEach(function(user){
				var newrow = document.createElement("tr");
				var newidnumber = document.createElement("td");
				newidnumber.textContent = user.studentId;
				var newname = document.createElement("td");
				newname.textContent = user.studentName;
				var newsurname = document.createElement("td");
				newsurname.textContent = user.studentSurname;
				var newemail = document.createElement("td");
				newemail.textContent = user.studentEmail;
				var newdegree = document.createElement("td");
				newdegree.textContent = user.studentDegree;
				var newmark = document.createElement("td");
				var newmarkform = document.createElement("form");
				newmark.appendChild(newmarkform);
				newmarkform.setAttribute("id", "id_modalevaluationform");
				newmarkform.setAttribute("action", "#");
				var newmarkselection = document.createElement("select");
				newmarkselection.setAttribute("name", "modalEvaluation");
				newmarkselection.setAttribute("id", "id_modalevaluationselect" + user.studentId);
				newmarkform.appendChild(newmarkselection); 
				var newmarkselectionempty = document.createElement("option");
				newmarkselectionempty.textContent = "";
				var newmarkselectionassente = document.createElement("option");
				newmarkselectionassente.textContent = "ASSENTE";
				var newmarkselectionrimandato = document.createElement("option");
				newmarkselectionrimandato.textContent = "RIMANDATO";
				var newmarkselectionriprovato = document.createElement("option");
				newmarkselectionriprovato.textContent = "RIPROVATO";
				var newmarkselection18 = document.createElement("option");
				newmarkselection18.textContent = "18";
				var newmarkselection19 = document.createElement("option");
				newmarkselection19.textContent = "19";
				var newmarkselection20 = document.createElement("option");
				newmarkselection20.textContent = "20";
				var newmarkselection21 = document.createElement("option");
				newmarkselection21.textContent = "21";
				var newmarkselection22 = document.createElement("option");
				newmarkselection22.textContent = "22";
				var newmarkselection23 = document.createElement("option");
				newmarkselection23.textContent = "23";
				var newmarkselection24 = document.createElement("option");
				newmarkselection24.textContent = "24";
				var newmarkselection25 = document.createElement("option");
				newmarkselection25.textContent = "25";
				var newmarkselection26 = document.createElement("option");
				newmarkselection26.textContent = "26";
				var newmarkselection27 = document.createElement("option");
				newmarkselection27.textContent = "27";
				var newmarkselection28 = document.createElement("option");
				newmarkselection28.textContent = "28";
				var newmarkselection29 = document.createElement("option");
				newmarkselection29.textContent = "29";
				var newmarkselection30 = document.createElement("option");
				newmarkselection30.textContent = "30";
				var newmarkselection30L = document.createElement("option");
				newmarkselection30L.textContent = "30L";
				newmarkselection.append(newmarkselectionempty, newmarkselectionassente, newmarkselectionrimandato, newmarkselectionriprovato, 
					newmarkselection18, newmarkselection19, newmarkselection20, newmarkselection21, newmarkselection22, newmarkselection23, 
					newmarkselection24, newmarkselection25, newmarkselection26, newmarkselection27, newmarkselection28, newmarkselection29, 
					newmarkselection30, newmarkselection30L);
				var newevaluationstate = document.createElement("td");
				newevaluationstate.textContent = user.studentEvaluationState;

				newrow.append(newidnumber, newname, newsurname, newemail, newdegree, newmark, newevaluationstate);
				self.modalContainerBody.appendChild(newrow);
			});
		};
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

			multipleInsertionButton = document.getElementById("id_multipleinsertionbutton");
			let closeModal = document.getElementsByClassName("close")[0];
			multipleInsertionModal = document.getElementById("id_multipleinsertionmodal");
			multipleInsertionModalPage = new ModalPage(alertContainer, document.getElementById("id_multipleinsertioncontainerbody"), document.getElementById("id_multipleinsertioncontainer"));
			multipleInsertionButton.addEventListener("click", function(e){
				let form = e.target.closest("form");
				let appealDate = form.querySelector("input[type = 'hidden'][name = 'dataAppello']").value;
				let courseId = form.querySelector("input[type = 'hidden'][name = 'idCorso']").value;
				multipleInsertionModalPage.show(courseId, appealDate);
				
			}, false);
			
			closeModal.addEventListener("click", function(e){
				multipleInsertionModal.style.display = "none";
			}, false);
			
			// When the user clicks anywhere outside of the modal, close it
			window.onclick = function(event) {
			  if (event.target == multipleInsertionModal) {
			    multipleInsertionModal.style.display = "none";
			  }
			}
			
			sendMultipleInsertionButton = document.getElementById("id_multipleinsertionsubmitbutton");
			sendMultipleInsertionButton.addEventListener("click", function(e){
				let form = e.target.closest("form");
				let appealDate = form.querySelector("input[type = 'hidden'][name = 'appealDate']").value;
				let courseId = form.querySelector("input[type = 'hidden'][name = 'courseId']").value;
				// ciclo lungo tutta la lista di studenti con voto non_inserito
				multipleInsertionModalPage.registeredStudentsWithoutEvaluation.forEach(function(student){
					let studentEvaluation = document.getElementById("id_modalevaluationselect" + student.studentId).value;
					// se è stato inserito un voto
					if(studentEvaluation !== ""){
						//chiamo la servlet per la modifica del voto sul singolo studentId
						makeCall("POST", "ModifyEvaluation?appealDate=" + appealDate + "&courseId=" + courseId + "&studentId=" + student.studentId + "&evaluation=" + studentEvaluation, form,
						    function(req) { // callback of the POST HTTP request
						      if (req.readyState === 4) { // response has arrived
						        let message = req.responseText; // get the body of the response
						        if (req.status === 200) { // if no errors
									//aggiorno la tabella degli studenti iscritti
									registeredStudentsDetails.show(courseId, appealDate);
						        } else {
						          alertContainer.textContent = message; // report the error contained in the response body
						        }
						      }
						    }
						);
					}
				});
				
				//chiudo la pagina modale
				multipleInsertionModal.style.display = "none";
			}, false);
			
			document.querySelector("a[href='Logout']").addEventListener('click', () => {
		        window.sessionStorage.removeItem('user');
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
			coursesList.show();
		};
	}
})();