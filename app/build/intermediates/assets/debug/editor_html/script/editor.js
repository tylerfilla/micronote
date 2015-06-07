
// Written by Tyler Filla

/* Document elements */

var headerBar;
var contentArea;

/* Events */

window.onload = function() {
    // Localize reused document elements
    headerBar   = document.getElementById("headerBar");
    contentArea = document.getElementById("contentArea");
    
    // Make contentArea editable
    contentArea.contentEditable = true;
}
