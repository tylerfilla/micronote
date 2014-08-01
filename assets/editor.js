
// Written by Tyler Filla

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
	
	reportContent();
}

function reportContent() {
	var editArea = document.getElementById("editArea");
	
	window.alert("content:" + editArea.innerHTML);
	window.alert("contentHeight:" + editArea.offsetHeight*window.devicePixelRatio);
}

function setHeaderContent(headerContent) {
	document.getElementById("headerBar").innerHTML = "<span>" + headerContent + "</span>";
}

function setEditorContent(editorContent) {
	document.getElementById("editArea").innerHTML = editorContent;
}
