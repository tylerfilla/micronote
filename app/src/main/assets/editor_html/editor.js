
// Written by Tyler Filla

var headerBar;
var contentArea;
var metricsElement;

// Events

window.onload = function() {
    headerBar = document.getElementById("headerBar");
    contentArea = document.getElementById("contentArea");
    metricsElement = document.getElementById("metricsElement");
    
    reportMetrics();
    
    metricsElement.parentNode.removeChild(metricsElement);
}

// Reporting

function report(message) {
    alert("report:" + message);
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
    
    report("lineWidth=" + lineWidth);
    report("lineHeight=" + lineHeight);
    report("lineOffsetX=" + lineOffsetX);
    report("lineOffsetY=" + lineOffsetY);
    report("contentHeight=" + contentHeight);
}
