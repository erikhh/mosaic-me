const expect = require('chai').expect;
const fs = require('fs');
const path = require('path');

const imageIndex = require('../../lib/image_index');

var config = {indexDir: "./work/testIndex"};

beforeEach(function() {
});

describe('Image index test', function() {

    it('should not error', function(done) {
        var sourceImage = './test_resources/rainbow.jpg';
    
        imageIndex.addImage(sourceImage, config, function(err) {
            expect(err).not;
            done();
        });
    });

    it('should copy the image under its average rgb folder', function(done) {
        var sourceImage = './test_resources/markiezenhof.jpg';

        imageIndex.addImage(sourceImage, config, function(err) {
            expect(err).to.be.undefined;
            var imagePath = path.join(config.indexDir, 'orig/162/141/109/e7c12cc211c5e03993c39fe0418facde1a08664d.jpg');
            expect(fs.existsSync(imagePath)).to.be.true;
            done();
        });
    });
});
