
// Written by Tyler Filla

var baselineScrollTop = 20;

function reportMetrics() {
	var editArea = document.getElementById("editArea");
	var metricsElement = document.getElementById("metricsElement");
	
	var lineWidth = 0;
	var lineHeight = 0;
	var lineOffsetX = 0;
	var lineOffsetY = 0;
	
	window.alert("nop"); // Certain devices freeze during following loops without this
	
	while (lineWidth <= 0) {
		lineWidth = editArea.clientWidth*window.devicePixelRatio;
	}
	while (lineHeight <= 0) {
		lineHeight = metricsElement.clientHeight*window.devicePixelRatio;
	}
	while (lineOffsetX <= 0) {
		lineOffsetX = metricsElement.getBoundingClientRect().left*window.devicePixelRatio;
	}
	while (lineOffsetY <= 0) {
		lineOffsetY = (metricsElement.getBoundingClientRect().top + window.pageYOffset - metricsElement.ownerDocument.documentElement.clientTop)*window.devicePixelRatio;
	}
	
	window.alert("lineWidth:" + lineWidth);
	window.alert("lineHeight:" + lineHeight);
	window.alert("lineOffsetX:" + lineOffsetX);
	window.alert("lineOffsetY:" + lineOffsetY);
	window.alert("contentHeight:" + editArea.offsetHeight*window.devicePixelRatio);
	
	reportContent();
}

function reportContent() {
	window.alert("content:" + document.getElementById("editArea").innerHTML);
}

function createListOrdered() {
	document.getElementById("editArea").focus();
	
	document.execCommand("insertText", false, " ");
	document.execCommand("insertOrderedList", false, null);
	document.execCommand("delete", false, null);
}

function createListUnordered() {
	document.getElementById("editArea").focus();
	
	document.execCommand("insertText", false, " ");
	document.execCommand("insertUnorderedList", false, null);
	document.execCommand("delete", false, null);
}

function setHeaderContent(headerContent) {
	document.getElementById("headerBar").innerHTML = "<span>" + headerContent + "</span>";
}

function setEditorContent(editorContent) {
	document.getElementById("editArea").innerHTML = editorContent;
}

function onKeyUp(editArea) {
	reportContent();
}

function onClick(editArea) {
	var target = document.body.scrollTop;
	var prev = 0;
	var goal = 5;
	var count = 0;
	
	var interval = window.setInterval(function() {
		var scroll = document.body.scrollTop;
		if (scroll == prev) {
			count++;
			if (count >= goal) {
				window.clearInterval(interval);
				window.scrollTo(0, target);
			}
		} else {
			count = 0;
		}
		prev = scroll;
	}, 50);
}
