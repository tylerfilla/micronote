
// Written by Tyler Filla

/* Localized document elements */

var header;
var content;
var text;
var lines;

/* Styling */

function createNotepadLines() {
    var targetNumLines  = Math.floor(Math.max(content.clientHeight, text.clientHeight)/28 + 0.5);
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

function uploadContent() {
    alert("content=" + text.innerHTML);
}

/* Initialization */

function initialize() {
    // Give us a notepad feel...
    createNotepadLines();
    
    // Make contentArea editable
    text.contentEditable = true;
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

function textOnKeyUp() {
    // Create notepad lines in background
    createNotepadLines();
    
    // Upload content to app
    uploadContent();
}

function windowOnLoad() {
    // Localize document elements
    header  = document.getElementById("header");
    content = document.getElementById("content"); 
    text    = document.getElementById("text");
    lines   = document.getElementById("lines");
    
    // Initialize
    initialize();
    
    // Register events that rely on window having loaded
    content.onclick = contentOnClick;
    text.onclick    = textOnClick;
    text.onkeyup    = textOnKeyUp;
}

function windowOnResize() {
    // Create notepad lines in background
    createNotepadLines();
}

window.onload   = windowOnLoad;
window.onresize = windowOnResize;
