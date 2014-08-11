
// Written by Tyler Filla

var headerBar = document.getElementById("headerBar");
var contentArea = document.getElementById("contentArea");
var metricsElement = document.getElementById("metricsElement");

// Events

window.onload = function() {
	reportMetrics();
}

// Reporting

function report(message) {
	alert("report:" + message);
}

function reportMetrics() {
	report("lineWidth=4.0");
}
