const Canvas = require('canvas');
const fs = require('fs');

var image = module.exports;

// Calculate the average red, green and blue value of all the pixels in the image
image.average = function(ctx, pixels) {
    pixels = pixels || image.getPixels(ctx);
    
    var average = {red: 0, green: 0, blue: 0};
    var length = pixels.length
    for (var i = 0; i < length; i++) {
        average.red += pixels[i].red / length;
        average.green += pixels[i].green / length;
        average.blue += pixels[i].blue / length;
    }
    
    for (var key in average) {
        average[key] = Math.round(average[key]);
    }

    return average;
  }

// Make an image context of specified size
image.context = function(dimensions) {
    //console.log("making context  ", dimensions);
    var canvas = new Canvas(dimensions.width, dimensions.height);
    var context = canvas.getContext('2d');
    canvas = null;
    //console.log("made context w: ", context.canvas.width, ' h: ', context.canvas.height);
    return context;
}

// Load an image from disk
image.get = function(file, dimensions, callback) {
    fs.readFile(file, function(err, buffer){
        if (err) {
            return callback(err, null);
        }
        if (typeof dims !== 'object') {
            dims = false;
        }
        try {
            image.resize(buffer, dimensions, callback);
        } catch (err) {
            console.error("Failed to load ", file, err);
        }
    }); 
}

// Retrieve the pixel data for an image
image.getPixels = function(context) {
    var width = context.canvas.width;
    var height = context.canvas.height;
    var length = width * height;

    var pixels = [];
    var data = context.getImageData(0, 0, width, height).data;

    for (var i = 0; i < length * 4; i += 4) {
        var red = data[i];
        var green = data[i + 1];
        var blue = data[i + 2];
        var entry = { id: i / 4, red: red, green: green, blue: blue };
        pixels.push(entry);
    }
    return pixels;
}

image.hex = function(pixel) {
    var r = hex(pixel.red);
    var g = hex(pixel.green);
    var b = hex(pixel.blue);
    return '#'+r+g+b;
}
  
var hex = function(val) {
    val = val.toString(16);
    return val.length == 1 ? '0' + val : val;
}

// Resize an image to the specified dimensions
image.resize = function(buffer, dimensions, callback) {
    var img = new Canvas.Image();
    img.src = buffer;
  
    var canvasW = dimensions ? dimensions.width : img.width;
    var canvasH = dimensions ? dimensions.height : img.height;
    var imgW = canvasW;
    var imgH = canvasH;
    var imgX = 0;
    var imgY = 0;
  
    var wRatio = canvasW / img.width;
    var hRatio = canvasH / img.height;
  
    if (wRatio < hRatio) {
      imgW = img.width * hRatio;
      imgX = -parseInt((imgW - canvasW) / 2, 10);
    }
    else if (wRatio > hRatio) {
      imgH = img.height * wRatio;
      imgY = -parseInt((imgH - canvasH) / 2, 10);
    }
    
    var context = image.context({width: canvasW, height: canvasH});
    context.drawImage(img, imgX, imgY, imgW, imgH);
    img = null;
    callback(null, context);
}

// Save an image context
image.save = function(context, save, callback) {
    try {
        var out = fs.createWriteStream(save)
        var stream = context.canvas.createJPEGStream()
        
        stream.on('data', function(chunk) {
        out.write(chunk);
        }).on('end', function() {
        out.end(null, null, function(err) {
            if (err) {
                return callback(err);
            }
            callback();
        });
        }).on('error', function(err) {
        console.log("Save error: " + err);
        callback(err);
        });
    } catch (err) {
        callback(err);
    }
  }