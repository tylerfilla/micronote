
// Written by Tyler Filla

/* Globals */

var autoUploadCounter = 0;
var contentPrevious = "";

// Localized document elements
var header;
var content;
var text;
var lines;

/* Function */

function uploadContent() {
    sendPageMessage("~content=" + text.innerHTML);
}

function handleAutoUpload() {
    autoUploadCounter--;
    
    if (autoUploadCounter == 0) {
        uploadContent();
    } else if (autoUploadCounter < 0) {
        autoUploadCounter = -1;
    }
}

function detectContentChange() {
    if (text.innerHTML != contentPrevious) {
        // Save copy of content for future comparison
        contentPrevious = text.innerHTML;
        
        // Create notepad lines in background
        createNotepadLines();
        
        // Trigger auto upload
        autoUploadCounter = 10;
    }
}

/* Styling */

function createNotepadLines() {
    var targetNumLines  = Math.floor(Math.max(content.clientHeight, text.clientHeight)/28);
    var currentNumLines = lines.childNodes.length;
    
    if (currentNumLines < targetNumLines) {
        for (var i = 0; i < targetNumLines - currentNumLines; i++) {
            var line = document.createElement("div");
            line.classList.add("line");
            lines.appendChild(line);
        }
    } else {
        for (var i = currentNumLines - targetNumLines; i > 0; i--) {
            lines.removeChild(lines.lastChild);
        }
    }
}

/* Communication */

function onReceiveAppMessage(message) {
    if (message.charAt(0) == '~') {
        message = message.substring(1);
        
        // Content updates
        if (message.substring(0, 7) == "content") {
            text.innerHTML = message.substring(8);
            
            // Create notepad lines in background
            createNotepadLines();
        }
        
        // Header updates
        if (message.substring(0, 6) == "header") {
            header.innerText = message.substring(7);
        }
    }
}

function sendPageMessage(message) {
    alert(message);
}

/* Initialization */

function initialize() {
    // Give us a notepad feel...
    createNotepadLines();
    
    // Make content area editable
    text.contentEditable = true;
    
    // Set auto upload interval
    setInterval(handleAutoUpload, 100);
}

/* Event handling */

function contentOnClick() {
    // Redirect focus to text area
    text.focus();
    
    // Move caret to end
    var range = document.createRange();
    range.selectNodeContents(text);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
}

function textOnClick(event) {
    // Don't propagate event
    event.cancelBubble = true;
}

function windowOnLoad() {
    // Localize document elements
    header  = document.getElementById("header");
    content = document.getElementById("content"); 
    text    = document.getElementById("text");
    lines   = document.getElementById("lines");
    
    // Initialize
    initialize();
    
    // Simulate a window resize
    windowOnResize();
    
    // Register events that rely on window having loaded
    content.onclick = contentOnClick;
    text.onclick    = textOnClick;
    
    // Update message interval
    setInterval(function() {
        sendPageMessage("!update");
    }, 50);
    
    // Content change detection interval
    setInterval(function() {
        detectContentChange();
    }, 50);
}

function windowOnResize() {
    // Resize content height
    content.style.height = (window.innerHeight - 60) + "px";
    
    // Create notepad lines in background
    createNotepadLines();
}

window.onload   = windowOnLoad;
window.onresize = windowOnResize;
