const fs = require('fs');
const expect = require('chai').expect;
const image = require('../../lib/image');
const image_index = require('../../lib/image_index');

const mosaic = require('../../lib/mosaic');

var config = {
    grid: {
        width: 256, 
        height: 256
    },
    indexDir: './work/indexed',
    fuzzyness: 10
};

before(function(done) {
    this.timeout(6000);
    image_index.loadIndex(config, function(err){
        if (err) {
            console.error(err);
        }
        done();
    });
});

describe('Mosaic test', function() {
    it('should not error', function(done) {
        var buffer = fs.readFileSync('./test_resources/rainbow.jpg');
        mosaic.newPicture(buffer, config,  function(err, pictureGrid) {
            expect(err).is.undefined;
            expect(pictureGrid).not.null.and.not.undefined;
            done();
        })
    }).timeout(3000);

    it ('should render tile', function(done) {
        var pictureGrid = JSON.parse(fs.readFileSync('./test_resources/colorful.json', 'utf8'));
        var dimensions = {width: 4, height: 4};
        mosaic.getTile(pictureGrid, dimensions, 128, 0, 256, 128, config, function(err, imageContext) {
            if (err) {
                console.error(err);
            }
            expect(err).is.null;
            image.save(imageContext, './work/' + new Date().getTime() + '.jpg', function(err) {
                expect(err).is.undefined;    
                done();
            });
        });
    }).timeout(0);
});