const async = require('async');
const image = require('./image');
const image_index = require('./image_index');

var mosaic = module.exports;

mosaic.getTile = function(pictureGrid, dimensions, x1, y1, x2, y2, config, callback) {
    //console.log('Rendering x1:', x1, ', y1:', y1, ' x2:', x2, ' y2:', y2);
    var xCoords = range(x1, x2);
    var yCoords = range(y1, y2);
    var tileGrid = { width: Math.abs(x2 - x1), height: Math.abs(y2 - y1) };
    var mosaicContext = image.context({width: tileGrid.width * dimensions.width, height: tileGrid.height * dimensions.height});
    async.eachSeries(xCoords, function(x, nextX) {
        async.eachSeries(yCoords, function(y, nextY) {
            // (width * y) + x
            var pixel = pictureGrid.pixels[(pictureGrid.grid.width * y) + x];
            image_index.getImage(config, pixel, dimensions, function(err, filePath) {
                if (err) {
                    return nextY(err);
                }
                add(mosaicContext, filePath, pixel, x - x1, y - y1, dimensions, config.tint, nextY);
            });
        }, function(err) {
            nextX(err);
        });
    }, function(err) {
        if (err) {
            callback(err, null);
        } else {
            callback(null, mosaicContext);
        }
    });
}

const range = function(from, to) {
    var list = [];
    for(i = from; i < to; i++) {
        list.push(i);
    }
    return list;
}

// Generate a grid for a new picture
mosaic.newPicture = function(buffer, config, callback) {
    var pictureGrid = {
        grid: config.grid,
        pixels: []};
    image.resize(buffer, config.grid, function(err, imageContext) {
        if (err) {
            callback(err, pictureGrid);
        }
        var imagePixels = image.getPixels(imageContext);
        for(var i = 0; i < imagePixels.length; i++) {
            var pixel = imagePixels[i];
            var selectedPixelImage = image_index.selectImage(pixel, config);
            selectedPixelImage.id = pixel.id;
            pictureGrid.pixels.push(selectedPixelImage);
        }
        callback(undefined, pictureGrid);
    });
}

var add = function(ctx, file, pixel, x, y, dimensions, tintVal, callback) {  
    image.get(file, null, function(err, img) {
        if (err) {
            callback(err);
        }
        var t = tint(img, pixel, dimensions, tintVal);
        //var x = (key%config.grid.width)*config.dims.width
        //var y = Math.floor(key/config.grid.width)*config.dims.height
        //console.log('ctx.canvas.width ', ctx.canvas.width, ' x ', x * dimensions.width);
        //console.log('ctx.canvas.height ', ctx.canvas.height, ' y ', y * dimensions.height);
        ctx.drawImage(t.canvas, x * dimensions.width, y * dimensions.height, dimensions.width, dimensions.height);
        img = null
        t = null
        callback();
    });
}

var tint = function(img, pixel, dimensions, tint) {
    // var h = dimensions.height;
    // var w = dimensions.width;
    var ctx = image.context(dimensions);
    ctx.fillStyle = image.hex(pixel);
    ctx.fillRect(0, 0, dimensions.width, dimensions.height);
    ctx.globalAlpha = tint;
    ctx.drawImage(img.canvas, 0, 0);
    return ctx;
}
