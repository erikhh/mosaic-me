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
    console.log(req.params);
    var img = fs.readFileSync('./public/images/sample.jpg');
    res.writeHead(200, {'Content-Type': 'image/jpeg' });
    res.end(img, 'binary');
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

module.exports = router;

