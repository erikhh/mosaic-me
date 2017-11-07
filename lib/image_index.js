const async = require('async');
const crypto = require('crypto');
const fs = require('fs');
const mkdirp = require('mkdirp')
const image = require('./image');
var kdTree = require('kd-tree-javascript').kdTree;
const path = require('path');

var imageIndex = module.exports;

var index;
var misses = 0;

// Originals    {imageIndex}/orig/red/green/blue/content-hash.jpg
// Scaled       {imageIndex}/widthxheight/red/green/blue/original-content-hash.jpg

// Add all images in config.dir to the index.
imageIndex.addAllImages = function(config) {
    walkFilesInDirectory(config.dir, function(allFiles){
        var processedCount = 0;
        async.eachLimit(allFiles, 8, function(file, callback){
            if (isImage(file)) {
                imageIndex.addImage(file, config, function(){
                    if (processedCount % 512 === 0) {
                        console.log(new Date() + " - " + processedCount + "/" + allFiles.length);
                    }
                    processedCount++;
                    callback();
                });
            } else {
                callback();
            }
        }, function(err) {
            if (err) {
                console.error(err);
            } else {
                console.log("All complete");
            }
        });
    });
}

// Add an image to our image index.
imageIndex.addImage = function (sourceFile, config, callback) {
    const hash = crypto.createHash('sha');
    const input = fs.createReadStream(sourceFile);
    input.on('data', function(d) {
        hash.update(d);
    });
    input.on('end', function(){
        const hashValue = hash.digest('hex');
        image.get(sourceFile, null, function(err, img) {
            if (err) {
                return callback(err, null);
            }
            var avg = image.average(img);
            var destinationPath = path.join(
                config.indexDir,
                'orig', 
                avg.red.toString(), 
                avg.green.toString(), 
                avg.blue.toString(), 
                hashValue + path.extname(sourceFile));
            mkdirp.mkdirp(path.dirname(destinationPath), function(err, made) {
                if (err && err.code !== 'EEXIST') {
                    callback(err, null);
                }
                image.save(img, destinationPath, callback);
            });
        });
    });
}

imageIndex.selectImage = function(color, config) {
    var nearest = index.nearest(color, config.fuzzyness);
    var selected = nearest[Math.floor((Math.random() * nearest.length))][0];
    return selected;
}

imageIndex.getImage = function(config, color, dims, callback) {
    var filePath = path.join(
        config.indexDir,
        dims.width + 'x' + dims.height,
        color.red.toString(),
        color.green.toString(),
        color.blue.toString(),
        color.fileName
    );
    fs.exists(filePath, function(exists) {
        if(exists) {
            callback(null, filePath);
        } else {
            misses++;
            var originalFilePath = path.join(
                config.indexDir,
                'orig',
                color.red.toString(),
                color.green.toString(),
                color.blue.toString(),
                color.fileName
            );
            image.get(originalFilePath, dims, function(err, img) {
                if (err) {
                    return callback(err, null);
                }
                createDirIfNotExists(filePath, function(err) {
                    if (err) {
                        callback(err, null);
                    } else {
                        image.save(img, filePath, function(err) {
                            if (err) {
                                return callback(err, null);
                            }
                            imageIndex.getImage(config, color, dims, callback);
                        });
                    }
                });
            });
        }
    });
}

imageIndex.loadIndex = function(config, callback) {
    var indexEntries = [];

    walkFilesInDirectory(path.join(config.indexDir, 'orig'), function(files) {
        console.log("Number of files in index %d", files.length);
        async.each(files, function(file, next) {
            if (isImage(file)) {
                var entry = getImageData(file);
                //console.log(entry);
                indexEntries.push(entry);
            }
            next();
        }, function(err) {
            if (err) {
                console.error(err);
                callback(err);
            } else {
                index = new kdTree(indexEntries, colorDistance, ['red', 'green', 'blue']);
                callback();
            }
        });
    });
}

imageIndex.getMisses = function() {
    return misses;
}

const colorDistance = function(l, r) {
    var result = Math.pow((l.red - r.red), 2) 
    + Math.pow((l.green - r.green), 2) 
    + Math.pow((l.blue - r.blue), 2);
    return result;
}

const createDirIfNotExists = function(filePath, callback) {
    mkdirp.mkdirp(path.dirname(filePath), function(err, made) {
        if (err && err.code !== 'EEXIST') {
            callback(err);
        }
        callback();
    });
}

const getImageData = function(imageFile) {
    var pathParts = imageFile.split(path.sep);
    var length = pathParts.length;
    return {
        red: parseInt(pathParts[length - 4]), 
        green: parseInt(pathParts[length - 3]), 
        blue: parseInt(pathParts[length - 2]), 
        fileName: pathParts[length - 1]
    };
}

const isImage = function(file) {
    var exts = new RegExp('.(jpg|png)$');
    return exts.test(file);
}

const walkFilesInDirectory = function(directory, callback) {
    var allFiles = [];
    fs.readdir(directory, function(err, files) {
        if (err) {
            console.error(err);
        }
        async.each(files, function(file, next){
            var filePath = path.join(directory, file);
            fs.stat(filePath, function(err, fileStats) {
                if (err) {
                    next(err);
                } else if (fileStats.isDirectory()) {
                    walkFilesInDirectory(filePath, function(files) {
                        allFiles = allFiles.concat(files);
                        next();
                    });
                } else {
                    allFiles.push(filePath);
                    next();
                }
            });
        }, function(err){
            if (err) {
                console.error(err);
            } else {
              callback(allFiles);  
            }
        })
    });
}
