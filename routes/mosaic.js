const config = require('../config');
const express = require('express');
const fs = require('fs');
const image_index = require('../lib/image_index');
const mosaic = require('../lib/mosaic');
const multer = require('multer');
const path = require('path');

var router = express.Router();
var upload = multer()

/* GET mosaic of me. */
router.get('/:x/:y/:z', function(req, res, next) {
    //console.log(req.params);
    findMostRecentMosaicFile(config.mosaicDir)
        .then(function(pictureGrid) {
            var tile = calculateTile(pictureGrid, config.minTileDimension, req.params.x, req.params.y, req.params.z);
            mosaic.getTile(pictureGrid, tile.dimensions, tile.x1, tile.y1, tile.x2, tile.y2, config, function(err, mosaicContext) {
                res.writeHead(200, {'Content-Type': 'image/jpeg' });
                streamImage(res, mosaicContext);
            });
        });
});

/* POST a new image to mosaic with */
router.post('/', upload.single('picture'), function(req, res, next) {
    mosaic.newPicture(req.file.buffer, config, function(err, pictureGrid) {
        if (err) {
            res.send(err);
        }
        fs.writeFile(
            path.join(config.mosaicDir, new Date().getTime() + '.json'),
            JSON.stringify(pictureGrid), 
            'utf-8',
            function(err) {
                if (err) {
                    res.send(err);
                }
                res.redirect('/'); 
            });
    });
});

image_index.loadIndex(config, function(err) {
    if (err) {
        console.error(err);
    }
});

const streamImage = function(res, context) {
    try {
        var stream = context.canvas.createJPEGStream();
        stream.on('data', function(chunk) {
            res.write(chunk, 'binary');
        }).on('end', function() {
         res.end();
        }).on('error', function(err) {
            console.log("Save error: " + err);
            res.send(err);
        });
    } catch (err) {
        res.send(err);
    }
}

const calculateTile = function(mosaic, minTileDimension, x, y, z) {
    var tile = {dimensions: {width: 0, height: 0}, x1: 0, y1: 0, x2: 0, y2: 0};
    tile.dimensions.width = timesTimesTwo(minTileDimension.width, z - 1);
    tile.dimensions.height = timesTimesTwo(minTileDimension.height, z - 1);

    var tileGridWidth = timesDivideTwo(mosaic.grid.width, z - 1);
    var tileGridHeight = timesDivideTwo(mosaic.grid.height, z - 1);
    
    tile.x1 = x  * tileGridWidth;
    tile.y1 = y * tileGridHeight;
    tile.x2 = tile.x1 + tileGridWidth;
    tile.y2 = tile.y1 + tileGridHeight;
    
    return tile;
}

const timesTimesTwo = function(val, timesTwo) {
    var result = val;
    for(var i = 0; i < timesTwo; i++) {
        result = result * 2;
    }
    return result;
}

const timesDivideTwo = function(val, divdeTwo) {
    var result = val;
    for(var i = 0; i < divdeTwo; i++) {
        result = result / 2;
    }
    return result;
}

const findMostRecentMosaicFile = function(mosaicDir) {
    return new Promise(function(result, reject) {
        fs.readdir(mosaicDir, function(err, files) {
            if (err) {
                reject(err);
            }
            var promises = [];
            for (var index = 0; index < files.length; index++) {
                if (files[index].endsWith('.json')) {
                    promises.push(getCreationTime(path.join(mosaicDir, files[index])));
                }
            }
            if (promises.length <= 0) {
                reject("No mosaic files found");
            }
            Promise.all(promises).then((creationTimes) => {
                creationTimes.sort(function(lh, rh) {
                    return rh.createTime - lh.createTime;
                });
                fs.readFile(creationTimes[0].file, function(err, data) {
                    if (err) {
                        reject(err);
                    }
                    result(JSON.parse(data));
                });
            });
        });
    });
}

const getCreationTime = function(file) {
    return new Promise(function (resolve, reject) {
        fs.stat(file, function(err, stats) {
            if (err) {
                reject(err);
            }
            resolve({file: file, createTime: stats.ctime});
        });
    });
}

module.exports = router;

