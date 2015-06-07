
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

/* Initialization */

function initialize() {
    // Make contentArea editable
    text.contentEditable = true;
    
    // Give us a notepad feel...
    createNotepadLines();
}

/* Events */

window.onload = function() {
    // Localize document elements
    header  = document.getElementById("header");
    content = document.getElementById("content"); 
    text    = document.getElementById("text");
    lines   = document.getElementById("lines");
    
    // Initialize
    initialize();
    
    content.onclick = function() {
        text.focus();
    }
    
    text.onkeyup = function() {
        createNotepadLines();
    }
}
