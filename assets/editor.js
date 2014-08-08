
/* Written by Tyler Filla */

// Constants

const listLevelMax = 4;

// Variables

var headerBar;
var contentArea;
var metricsElement;
var checkboxArea;

var lastRange;
var featureScanTemp;

// Incoming control functions

function setHeaderContent(headerContent) {
	headerBar.innerHTML = "<span>" + headerContent + "</span>";
}

function setEditorContent(editorContent) {
	contentArea.innerHTML = editorContent;
	contentArea.blur();
	
	featureScan();
}

function contentAreaFocus() {
	contentArea.focus();
	
	featureClear();
}

function contentAreaBlur() {
	contentArea.blur();
	
	featureScan();
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
	
	var node = window.getSelection().anchorNode;
	if (node) {
		node = node.parentNode;
		var nodeName = node.nodeName.toLowerCase();
		if ((nodeName == "ul" || nodeName == "ol" || nodeName == "li") && !isListCheckbox(node)) {
			controlActive = true;
			
			var listLevel = getListLevel(node);
			enableDecrease = listLevel > 0;
			enableIncrease = listLevel < listLevelMax;
		}
	}
	
	report("responder/indentControlState:" + controlActive + "," + enableDecrease + "," + enableIncrease);
}

// Edit functions

function createListOrdered() {
	contentArea.focus();

	var selection = window.getSelection();
	
	if (getListLevel(selection.anchorNode) > 0) {
		return;
	}
	
	var range = selection.getRangeAt(0);
	
	document.execCommand("insertText", false, " ");
	
	var existingContent = "<br />";
	if (selection.anchorNode && selection.anchorNode.parentNode) {
		var existingContent = selection.anchorNode.parentNode.innerHTML;
		selection.anchorNode.parentNode.removeChild(selection.anchorNode);
	}
	
	var nodeList = document.createElement("ol");
	
	var nodeListItem = document.createElement("li");
	nodeListItem.innerHTML = existingContent;
	
	nodeList.appendChild(nodeListItem);
	
	range.insertNode(nodeList);
	selection.removeAllRanges();
	range.setEndAfter(nodeListItem);
	range.collapse();
	selection.addRange(range);
	
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlState();
}

function createListUnordered() {
	contentArea.focus();

	var selection = window.getSelection();
	
	if (getListLevel(selection.anchorNode) > 0) {
		return;
	}
	
	var range = selection.getRangeAt(0);
	
	document.execCommand("insertText", false, " ");
	
	var existingContent = "<br />";
	if (selection.anchorNode && selection.anchorNode.parentNode) {
		var existingContent = selection.anchorNode.parentNode.innerHTML;
		selection.anchorNode.parentNode.removeChild(selection.anchorNode);
	}
	
	var nodeList = document.createElement("ul");
	
	var nodeListItem = document.createElement("li");
	nodeListItem.innerHTML = existingContent;
	
	nodeList.appendChild(nodeListItem);
	
	range.insertNode(nodeList);
	selection.removeAllRanges();
	range.setEndAfter(nodeListItem);
	range.collapse();
	selection.addRange(range);
	
	document.execCommand("delete", false, null);
	
	reportContent();
	reportIndentControlState();
}

function createListCheckbox() {
	contentArea.focus();

	var selection = window.getSelection();
	
	if (getListLevel(selection.anchorNode) > 0) {
		return;
	}
	
	var range = selection.getRangeAt(0);
	
	document.execCommand("insertText", false, " ");
	
	var existingContent = "<br />";
	if (selection.anchorNode && selection.anchorNode.parentNode) {
		var existingContent = selection.anchorNode.parentNode.innerHTML;
		selection.anchorNode.parentNode.removeChild(selection.anchorNode);
	}
	
	var nodeList = document.createElement("ul");
	nodeList.classList.add("checkboxList");
	
	var nodeListItem = document.createElement("li");
	nodeListItem.classList.add("checkboxListItem");
	nodeListItem.classList.add("unchecked");
	nodeListItem.innerHTML = existingContent;
	
	nodeList.appendChild(nodeListItem);
	
	range.insertNode(nodeList);
	selection.removeAllRanges();
	range.setEndAfter(nodeListItem);
	range.collapse();
	selection.addRange(range);
	
	document.execCommand("delete", false, null);
	
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
	if (window.event.clientX > 40) {
		return;
	}
	
	var selection = window.getSelection();
	selection.removeAllRanges();
	selection.addRange(lastRange);
	
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

function onListCheckboxItemAdded(listItem) {
	if (listItem.classList.contains("checkboxList")) {
		var items = listItem.getElementsByTagName("li");
		for (var i = 0; i < items.length; i++) {
			var item = items[i];
			if (item.innerText.trim() == "") {
				listItem = item;
				break;
			}
		}
	}
	
	if (listItem.classList.contains("checked")) {
		listItem.classList.remove("checked");
		listItem.classList.add("unchecked");
	}
}

// Feature recognition

function featureClear() {
	var elements = contentArea.getElementsByTagName("*");
	for (var i = 0; i < elements.length; i++) {
		var element = elements[i];
		
		if (element.classList.contains("feature")) {
			if (element.nodeName.toLowerCase() == "a" && element.href) {
				var selection = window.getSelection();
				
				var oldRange = selection.getRangeAt(0);
				var linkRange = document.createRange();
				linkRange.selectNode(element);
				
				selection.removeAllRanges();
				selection.addRange(linkRange);
				
				document.execCommand("unlink", false, null);
				document.execCommand("removeFormat", false, null);
				
				selection.removeAllRanges();
				selection.addRange(oldRange);
			}
		}
	}
	report(contentArea.innerHTML);
}

function featureScan(element) {
	if (!element) {
		featureScanTemp = new Array();
		
		featureScan(contentArea);
		
		while (featureScanTemp.length > 0) {
			var action = featureScanTemp.pop();
			switch (action) {
			case "insertNodeAtRange":
				var node = featureScanTemp.pop();
				var range = featureScanTemp.pop();
				
				var selection = window.getSelection();
				var oldRanges = new Array();
				
				for (var i = 0; i < selection.rangeCount; i++) {
					oldRanges.push(selection.getRangeAt(i));
				}
				
				selection.removeAllRanges();
				selection.addRange(range);
				
				document.execCommand("insertHTML", false, "&nbsp;");
				range.insertNode(node);
				
				selection.removeAllRanges();
				
				for (var i = 0; i < oldRanges.length; i++) {
					selection.addRange(oldRanges[i]);
				}
				report(contentArea.innerHTML);
				
				break;
			}
		}
		
		return;
	}
	
	var children = element.childNodes;
	for (var i = 0; i < children.length; i++) {
		featureScan(children[i]);
	}
	
	if (element.nodeType == 3) {
		var type = "";
		var end = false;
		
		var startPos = -1;
		var endPos = -1;
	
		var accum = "";
		var accumLink = "";
		
		for (var i = 0; i < element.data.length; i++) {
			var char = element.data.charAt(i);
			
			if (startPos < 0) {
				startPos = i;
			}
			
			if (i == element.data.length - 1
					|| char == ' '
					|| char == '\t'
					|| char == '\n'
					|| char == '\u00A0') { // &nbsp;
				end = true;
				endPos = i + 1;
			}
			
			switch (type) {
			case "link":
				accumLink += char;
				
				if (end) {
					var selection = window.getSelection();
					
					var linkRange = document.createRange();
					linkRange.setStart(element, startPos);
					linkRange.setEnd(element, endPos);
					
					selection.removeAllRanges();
					selection.addRange(linkRange);
					
					var linkNode = document.createElement("a");
					linkNode.href = normalizeUrl(accumLink.trim());
					linkNode.classList.add("feature");
					linkNode.innerText = accumLink;
					
					featureScanTemp.push(linkRange);
					featureScanTemp.push(linkNode);
					featureScanTemp.push("insertNodeAtRange");
					
					selection.removeAllRanges();
					
					report(contentArea.innerHTML);
				}
				break;
			default:
				accum += char;
				
				if (accum.toLowerCase().indexOf("http://") == 0
						|| accum.toLowerCase().indexOf("https://") == 0
						|| accum.toLowerCase().indexOf("www.") == 0) {
					type = "link";
					accumLink = accum;
					accum = "";
				}
				break;
			}
			
			if (end) {
				type = "";
				end = false;
				
				startPos = -1;
				endPos = -1;
				
				accum = "";
				accumLink = "";
			}
		}
	}
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
	if (clickedNode) {
		if (clickedNode.href) {
			contentArea.blur();
			window.location = clickedNode.href;
		} else if (isListCheckbox(clickedNode)) {
			handleListCheckboxItemOnClick();
		}
	}
	
	reportIndentControlState();
	
	if (window.getSelection().rangeCount > 0) {
		lastRange = window.getSelection().getRangeAt(0);
	} else {
		lastRange = null;
	}
}

function contentAreaOnKeyUp() {
	var keyCode = window.event.which || window.event.keyCode;
	if (keyCode == 13) {
		var node = window.getSelection().anchorNode.parentNode;
		if (isListCheckbox(node)) {
			onListCheckboxItemAdded(node);
		}
	}
	
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
	if (!listItem) {
		return;
	}
	
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

function normalizeUrl(url) {
	if (url.search("://") < 0) {
		url = "http://" + url;
	}
	
	return url;
}
