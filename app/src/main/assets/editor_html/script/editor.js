
// Written by Tyler Filla

/* Globals */

// Auto upload state
var autoUploadCounter         = 0;
var autoUploadPreviousContent = "";

// Editor configuration
var config = {};

// Current note info
var note = {};

// Externally localized resources
var res = {};

// Intervals and timeouts
var listIntervals = new Array();
var listTimeouts  = new Array();

// Localized document elements
var header;
var headerText;
var content;
var contentLines;
var contentText;

/* Aesthetics */

function aestheticsRebuild() {
    // Recreate notepad background lines
    createNotepadLines(true);
}

function aestheticsUpdate() {
    // Update notepad background lines
    createNotepadLines(false);
}

function createNotepadLines(recreate) {
    if (recreate || !config.showNotepadLines) {
        while (contentLines.firstChild) {
            contentLines.removeChild(contentLines.firstChild);
        }
    }
    
    if (config.showNotepadLines) {
        var targetNumLines  = Math.floor(Math.max(content.clientHeight, contentText.clientHeight)/32);
        var currentNumLines = contentLines.childNodes.length;
        
        if (currentNumLines < targetNumLines) {
            for (var i = 0; i < targetNumLines - currentNumLines; i++) {
                var line = document.createElement("div");
                line.classList.add("line");
                contentLines.appendChild(line);
            }
        } else {
            for (var i = currentNumLines - targetNumLines; i > 0; i--) {
                contentLines.removeChild(contentLines.lastChild);
            }
        }
    }
}

/* Header */

function updateHeader() {
    // Convert to calculable date and time
    var lastModifiedDate = new Date(Number(note.lastModified));
    var lastModifiedTime = Number(note.lastModified);
    
    // Time since modification (in ms)
    var timeSince = Date.now() - lastModifiedTime;
    
    // Check for just-created signature
    if (lastModifiedTime == 0) {
        headerText.innerText = "New";
        return;
    }
    
    /* Generate preformatted date strings */
    
    var date      = lastModifiedDate.getDate();
    var date2     = (date < 10 ? "0" : "") + date; // Always two digits
    var month     = lastModifiedDate.getMonth();
    var monthAbbr = utilGetMonthAbbr(month);
    var year      = lastModifiedDate.getFullYear();
    var yearShort = String(year).substring(2);
    
    var strDateNoYear = "[date w/o year]";
    var strDateYear   = "[date]";
    
    switch (config.formatDate) {
    case "MONTH_D_YYYY":
        strDateNoYear = monthAbbr + " " + date;
        strDateYear   = strDateNoYear + ", " + year;
        break;
    case "MONTH_D_YY":
        strDateNoYear = monthAbbr + " " + date;
        strDateYear   = strDateNoYear + ", " + yearShort;
        break;
    case "M_DD_YYYY_SLASH":
        strDateNoYear = month + "/" + date2;
        strDateYear   = strDateNoYear + "/" + year;
        break;
    case "M_DD_YYYY_DASH":
        strDateNoYear = month + "-" + date2;
        strDateYear   = strDateNoYear + "-" + year;
        break;
    case "M_DD_YY_SLASH":
        strDateNoYear = month + "/" + date2;
        strDateYear   = strDateNoYear + "/" + yearShort;
        break;
    case "M_DD_YY_DASH":
        strDateNoYear = month + "-" + date2;
        strDateYear   = strDateNoYear + "-" + yearShort;
        break;
    case "DD_M_YYYY_SLASH":
        strDateNoYear = date2 + "/" + month;
        strDateYear   = strDateNoYear + "/" + year;
        break;
    case "DD_M_YYYY_DASH":
        strDateNoYear = date2 + "-" + month;
        strDateYear   = strDateNoYear + "-" + year;
        break;
    case "DD_M_YY_SLASH":
        strDateNoYear = date2 + "/" + month;
        strDateYear   = strDateNoYear + "/" + yearShort;
        break;
    case "DD_M_YY_DASH":
        strDateNoYear = date2 + "-" + month;
        strDateYear   = strDateNoYear + "-" + yearShort;
        break;
    }
    
    /* Generate preformatted time string */
    
    var strTime = "[time]";
    
    switch (config.formatTime) {
    case "_12_HOUR":
        strTime = (lastModifiedDate.getHours()%12) + ":" + (lastModifiedDate.getMinutes() < 10 ? "0" : "") + lastModifiedDate.getMinutes() + " " + (lastModifiedDate.getHours() > 12 ? "PM" : "AM");
        break;
    case "_24_HOUR":
        strTime = lastModifiedDate.getHours() + ":" + (lastModifiedDate.getMinutes() < 10 ? "0" : "") + lastModifiedDate.getMinutes();
        break;
    }
    
    // Switch against preferred scheme
    switch (config.timestampScheme) {
    case "CASCADE_5_SEC":
        if (timeSince < 60*1000) {
            headerText.innerText = Math.floor(timeSince/1000 + 0.5) + " sec";
        } else if (timeSince < 60*60*1000) {
            headerText.innerText = Math.floor(timeSince/(60*1000) + 0.5) + " min";
        } else if (timeSince < 24*60*60*1000) {
            headerText.innerText = strTime;
        } else if (timeSince < 365*24*60*60*1000) {
            headerText.innerText = strDateNoYear;
        } else {
            headerText.innerText = strDateYear;
        }
        break;
    case "CASCADE_4_MIN":
        if (timeSince < 60*60*1000) {
            headerText.innerText = Math.floor(timeSince/(60*1000) + 0.5) + " min";
        } else if (timeSince < 24*60*60*1000) {
            headerText.innerText = strTime;
        } else if (timeSince < 365*24*60*60*1000) {
            headerText.innerText = strDateNoYear;
        } else {
            headerText.innerText = strDateYear;
        }
        break;
    case "CASCADE_3_TIME":
        if (timeSince < 24*60*60*1000) {
            headerText.innerText = strTime;
        } else if (timeSince < 365*24*60*60*1000) {
            headerText.innerText = strDateNoYear;
        } else {
            headerText.innerText = strDateYear;
        }
        break;
    case "CASCADE_2_DATE_NOYEAR":
        if (timeSince < 365*24*60*60*1000) {
            headerText.innerText = strDateNoYear;
        } else {
            headerText.innerText = strDateYear;
        }
        break;
    case "CASCADE_1_DATE_YEAR":
        headerText.innerText = strDateYear;
        break;
    case "FULL":
        headerText.innerText = strTime + " " + strDateYear;
        break;
    case "UNIX":
        headerText.innerText = Math.floor(lastModifiedTime/1000 + 0.5);
        break;
    }
}

/* Configuration */

function applyConfig() {
    // Apply background color
    document.body.style.backgroundColor = utilARGBIntToRGBStr(config.backgroundColor);
    
    // Apply text color
    contentText.style.color = utilARGBIntToRGBStr(config.textColor);
    
    // Apply text size
    contentText.style.fontSize = config.textSize + "px";
    
    // Update header
    updateHeader();
    
    // Rebuild aesthetics
    aestheticsRebuild();
}

/* Auto upload */

function autoUploadAction() {
    sendPageMessage("~content=" + contentText.innerHTML);
}

function autoUploadDetect() {
    if (contentText.innerHTML != autoUploadPreviousContent) {
        // Save copy of content for future comparison
        autoUploadPreviousContent = contentText.innerHTML;
        
        // Schedule auto upload in 5 iterations
        autoUploadCounter = 5;
    }
}

function autoUploadIterate() {
    autoUploadCounter--;
    
    if (autoUploadCounter == 0) {
        autoUploadAction();
    } else if (autoUploadCounter < 0) {
        autoUploadCounter = -1;
    }
}

/* Incoming communication handling */

function handleIncomingAssignment(key, value) {
    // Debug log
    console.log("appmessage: assign '" + key + "' as '" + value + "'");
    
    // Default handler function
    var handler = function(value) {
        console.log("appmessage: handler error: no handler method for key '" + key + "'");
    };
    
    // Select appropriate handler function for key
    switch (key) {
    case "config":
        handler = handleIncomingAssignmentConfig;
        break;
    case "note":
        handler = handleIncomingAssignmentNote;
        break;
    case "res":
        handler = handleIncomingAssignmentRes;
        break;
    }
    
    // Call selected handler
    handler(value);
}

function handleIncomingAssignmentConfig(value) {
    // Parse JSON object and store to global 'config'
    if (window.JSON) {
        config = JSON.parse(value);
    } else {
        config = eval(value);
    }
    
    // Apply configuration
    applyConfig();
}

function handleIncomingAssignmentNote(value) {
    // Parse JSON object and store to global 'note'
    if (window.JSON) {
        note = JSON.parse(value);
    } else {
        note = eval(value);
    }
    
    // Set content text
    contentText.innerHTML = note.content;
    
    // Sneak past auto upload detection
    autoUploadPreviousContent = note.content;
    
    // Simulate onKeyup event
    contentText.onkeyup();
    
    // Update header
    updateHeader();
    
    // Rebuild aesthetics
    aestheticsRebuild();
}

function handleIncomingAssignmentRes(value) {
    // Parse JSON object and store to global 'res'
    if (window.JSON) {
        res = JSON.parse(value);
    } else {
        res = eval(value);
    }
}

function handleIncomingCommand(command) {
    // Debug log
    console.log("appmessage: command '" + message + "'");
    
    // Default handler function
    var handler = function() {
        console.log("appmessage: handler error: no handler method for command '" + command + "'");
    };
    
    // Select appropriate handler function for command
    switch (command) {
    default:
        // NOP
        break;
    }
    
    // Call selected handler
    handler();
}

/* Communication */

function onReceiveAppMessage(message) {
    // Validate message
    if (!message || message.length <= 1) {
        console.log("appmessage: protocol error: invalid message");
        return;
    }
    
    // Remove and save action character
    var action = message.charAt(0);
    message = message.substring(1);
    
    // Check action character and act accordingly
    if (action == '~') {
        if (message.indexOf("=") > 0) {
            var key   = message.substring(0, message.indexOf("="));
            var value = message.substring(message.indexOf("=") + 1);
            
            handleIncomingAssignment(key, value);
        } else {
            console.log("appmessage: protocol error: no assignment operator");
        }
    } else if (action == '!') {
        handleIncomingCommand(message);
    } else {
        console.log("appmessage: protocol error: invalid action character '" + action + "'");
    }
}

function sendPageMessage(message) {
    alert(message);
}

/* Utilities */

function utilARGBIntToRGBStr(argbInt) {
    // Convert color from a 32-bit ARGB integer to a RGB string
    return "rgb(" + ((argbInt & 0xff0000) >> 16) + ", " + ((argbInt & 0xff00) >> 8) + ", " + (argbInt & 0xff) + ")";
}

function utilGetMonthAbbr(month) {
    return ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"][month];
}

/* Initialization */

function initialize() {
    // Build aesthetics
    aestheticsRebuild();
    
    // Content change and auto upload interval
    listIntervals.push(setInterval(function() {
        autoUploadDetect();
        autoUploadIterate();
    }, 50));
    
    // Request command interval
    listIntervals.push(setInterval(function() {
        sendPageMessage("!request");
    }, 500));
}

/* Event handling */

function contentOnClick(event) {
    // Redirect focus to text area
    contentText.focus();
    
    // Move caret to end
    var range = document.createRange();
    range.selectNodeContents(contentText);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
}

function contentTextOnClick(event) {
    // Don't propagate event
    event.cancelBubble = true;
}

function contentTextOnKeyup(event) {
    // Update aesthetics
    aestheticsUpdate();
}

function windowOnLoad(event) {
    // Localize document elements
    header       = document.getElementById("header");
    headerText   = document.getElementById("headerText");
    content      = document.getElementById("content");
    contentLines = document.getElementById("contentLines");
    contentText  = document.getElementById("contentText");
    
    // Initialize
    initialize();
    
    // Register events that rely on window having loaded
    content.onclick     = contentOnClick;
    contentText.onclick = contentTextOnClick;
    contentText.onkeyup = contentTextOnKeyup;
    
    // Simulate window resize
    window.onresize();
}

function windowOnUnload(event) {
    // Clear intervals
    for (var i = 0; i < listIntervals.length; i++) {
        clearInterval(listIntervals[i]);
    }
    
    // Clear timeouts
    for (var i = 0; i < listTimeouts.length; i++) {
        clearTimeout(listTimeouts[i]);
    }
}

function windowOnResize(event) {
    // Resize content height
    content.style.height = (window.innerHeight - 68 - 16) + "px";
    
    // Update aesthetics
    aestheticsUpdate();
}

window.onload   = windowOnLoad;
window.onunload = windowOnUnload;
window.onresize = windowOnResize;
