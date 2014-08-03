
/* Written by Tyler Filla */

// Variables

var headerBar, editArea, metricsElement;

// Incoming set functions

function setHeaderContent(headerContent) {
	headerBar.innerHTML = "<span>" + headerContent + "</span>";
}

function setEditorContent(editorContent) {
	editArea.innerHTML = editorContent;
}

// Outgoing report functions

function report(message) {
	window.alert(message);
}

function reportMetrics() {
	var lineWidth = 0;
	var lineHeight = 0;
	var lineOffsetX = 0;
	var lineOffsetY = 0;
	
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
	
	report("lineWidth:" + lineWidth);
	report("lineHeight:" + lineHeight);
	report("lineOffsetX:" + lineOffsetX);
	report("lineOffsetY:" + lineOffsetY);
	report("contentHeight:" + editArea.offsetHeight*window.devicePixelRatio);
}

function reportContent() {
	report("content:" + document.getElementById("editArea").innerHTML);
}

function reportIndentControlActive() {
	var message = "responder/indentControlActive:";
	
	var nodeName = window.getSelection().anchorNode.parentNode.nodeName.toLowerCase();
	if (nodeName == "li" || nodeName == "ul" || nodeName == "ol") {
		message += "true";
	} else {
		message += "false";
	}
	
	report(message);
}

// Edit functions

function createListOrdered() {
	editArea.focus();
	
	document.execCommand("insertText", false, " ");
	document.execCommand("insertOrderedList", false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlActive();
}

function createListUnordered() {
	editArea.focus();
	
	document.execCommand("insertText", false, " ");
	document.execCommand("insertUnorderedList", false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlActive();
}

function indentDecrease() {
	document.execCommand("insertText", false, " ");
	document.execCommand('outdent', false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlActive();
}

function indentIncrease() {
	document.execCommand("insertText", false, " ");
	document.execCommand('indent', false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlActive();
}

// Event handlers

function bodyOnLoad() {
	headerBar = document.getElementById("headerBar");
	editArea = document.getElementById("editArea");
	metricsElement = document.getElementById("metricsElement");
	
	report();
	reportMetrics();
}

function editAreaOnKeyUp() {
	reportContent();
	reportIndentControlActive();
}

function editAreaOnClick() {
	reportIndentControlActive();
	fixScrollPosition();
}

// Miscellaneous

function fixScrollPosition() {
	// The page tends to scroll up upon (re-)focusing the editArea.
	// This function waits until this is done, then resets.
	// This is a workaround, and although a little ugly, it works just fine.
	
	var target = document.body.scrollTop;
	var prev = 0;
	var goal = 6;
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
