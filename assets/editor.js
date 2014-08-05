
/* Written by Tyler Filla */

// Constants

const listLevelMax = 4;

// Variables

var headerBar;
var contentArea;
var metricsElement;
var checkboxArea;

var checkboxListItemEdit;

// Incoming set functions

function setHeaderContent(headerContent) {
	headerBar.innerHTML = "<span>" + headerContent + "</span>";
}

function setEditorContent(editorContent) {
	contentArea.innerHTML = editorContent;
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
	var contentHeight = 0;
	
	while (lineWidth <= 0) {
		lineWidth = contentArea.clientWidth*window.devicePixelRatio;
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
	while (contentHeight <= 0) {
		contentHeight = contentArea.offsetHeight*window.devicePixelRatio;
	}
	
	report("lineWidth:" + lineWidth);
	report("lineHeight:" + lineHeight);
	report("lineOffsetX:" + lineOffsetX);
	report("lineOffsetY:" + lineOffsetY);
	report("contentHeight:" + contentHeight);
}

function reportContent() {
	report("content:" + contentArea.innerHTML);
}

function reportIndentControlState() {
	var controlActive = false;
	var enableDecrease = false;
	var enableIncrease = false;
	
	var node = window.getSelection().anchorNode.parentNode;
	var nodeName = node.nodeName.toLowerCase();
	if ((nodeName == "ul" || nodeName == "ol" || nodeName == "li") && !isListCheckbox(node)) {
		controlActive = true;
		
		var listLevel = getListLevel(node);
		enableDecrease = listLevel > 0;
		enableIncrease = listLevel < listLevelMax;
	}
	
	report("responder/indentControlState:" + controlActive + "," + enableDecrease + "," + enableIncrease);
}

// Edit functions

function createListOrdered() {
	contentArea.focus();
	
	document.execCommand("insertText", false, " ");
	document.execCommand("insertOrderedList", false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlState();
}

function createListUnordered() {
	contentArea.focus();
	
	document.execCommand("insertText", false, " ");
	document.execCommand("insertUnorderedList", false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlState();
}

function createListCheckbox() {
	contentArea.focus();

	document.execCommand("insertText", false, " ");
	document.execCommand("insertUnorderedList", false, null);
	document.execCommand("delete", false, null);
	
	var node = window.getSelection().anchorNode.parentNode;
	if (node.nodeName.toLowerCase() == "li") {
		node.classList.add("checkboxListItem");
		node.classList.add("unchecked");
	} else if (node.nodeName.toLowerCase() == "ul") {
		node.classList.add("checkboxList");
		if (node.firstChild && node.firstChild.nodeName.toLowerCase() == "li") {
			node.firstChild.classList.add("checkboxListItem");
			node.firstChild.classList.add("unchecked");
		}
	}
	
	reportContent();
}

function indentDecrease() {
	document.execCommand("insertText", false, " ");
	document.execCommand('outdent', false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlState();
}

function indentIncrease() {
	document.execCommand("insertText", false, " ");
	document.execCommand('indent', false, null);
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlState();
}

// Checkbox list functions

function handleListCheckboxItemOnClick() {
	window.getSelection().removeAllRanges();
	
	var listItem = window.event.target || window.event.srcElement;
	
	if (listItem.classList.contains("unchecked")) {
		listItem.classList.remove("unchecked");
		listItem.classList.add("checked");
	} else if (listItem.classList.contains("checked")) {
		listItem.classList.remove("checked");
		listItem.classList.add("unchecked");
	}
	
	reportContent();
}

function isListCheckbox(element) {
	return element.classList.contains("checkboxList") || element.classList.contains("checkboxListItem");
}

// Event handlers

function bodyOnLoad() {
	headerBar = document.getElementById("headerBar");
	contentArea = document.getElementById("contentArea");
	metricsElement = document.getElementById("metricsElement");
	checkboxArea = document.getElementById("checkboxArea");
	
	report();
	reportMetrics();
	reportContent();
}

function contentAreaOnClick() {
	fixScrollPosition();
	
	var clickedNode = window.event.target || window.event.srcElement;
	if (clickedNode && isListCheckbox(clickedNode)) {
		handleListCheckboxItemOnClick();
	}
	
	reportIndentControlState();
}

function contentAreaOnKeyUp() {
	reportContent();
	reportIndentControlState();
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

function getListLevel(listItem) {
	var level = 0;
	
	while (listItem.parentNode) {
		var nodeName = listItem.nodeName.toLowerCase();
		if (nodeName == "ul" || nodeName == "ol") {
			level++;
		}
		listItem = listItem.parentNode;
	}
	
	return level;
}
