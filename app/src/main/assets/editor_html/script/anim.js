
// Written by Tyler Filla

// anim.js
// A flexible, general-purpose JavaScript animation framework 

var anim = {
    interpolator: {
        linear: function(x, min, max) {
            return x;
        },
        sin: function(x, min, max) {
            return map(Math.sin(map(x, min, max, 0, Math.PI/2)), 0, 1, min, max);
        },
        cos: function(x, min, max) {
            return map(Math.cos(map(x, min, max, 0, Math.PI/2)), 1, 0, min, max);
        },
        fullsin: function(x, min, max) {
            return map(Math.sin(map(x, min, max, -Math.PI/2, Math.PI/2)), -1, 1, min, max);
        },
    },
    run: function(interpolator, begin, end, duration, steps, out) {
        var current = begin;
        var speed = (end - begin)/duration;
        var spread = Math.abs(end - begin);
        
        var interval = setInterval(function() {
            current += speed*(duration/steps);
            if (!out(interpolator(current, Math.min(begin, end), Math.max(begin, end))) || Math.abs(current - begin) >= spread) {
                clearInterval(interval);
            }
        }, duration/steps);
        
        return {
            stop: function() {
                clearInterval(interval);
            },
        };
    },
};

function map(x, a1, a2, b1, b2) {
    return b1 + (x - a1)*(b2 - b1)/(a2 - a1);
}
