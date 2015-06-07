
// Written by Tyler Filla

/* Localized document elements */

var headerBar;
var contentArea;

/* Events */

window.onload = function() {
    // Localize document elements
    headerBar   = document.getElementById("headerBar");
    contentArea = document.getElementById("contentArea");
    
    // Make contentArea editable
    contentArea.contentEditable = true;
}
