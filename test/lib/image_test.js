const expect = require('chai').expect;
const fs = require('fs');

const image = require('../../lib/image');

beforeEach(function() {});

describe('Image test', function() {
    it('should resize image', function(done) {
        var buffer = fs.readFileSync('./test_resources/rainbow.jpg');
        var dimensions = {width: 256, height: 256};
        image.resize(buffer, dimensions, function(err, imageContext) {
            expect(err).not;
            expect(imageContext).not.null.and.not.undefined;
            expect(imageContext.canvas.width).to.equal(256);
            expect(imageContext.canvas.height).to.equal(256);
            done();
        });
    });

    it ('should get pixels', function(done) {
        var buffer = fs.readFileSync('./test_resources/rainbow.jpg');
        var dimensions = {width: 128, height: 128};
        image.resize(buffer, dimensions, function(err, imageContext) {
            var pixels = image.getPixels(imageContext);
            expect(pixels.length).to.equal(dimensions.width * dimensions.height);
            expect(pixels[326].id).to.equal(326);
            expect(pixels[326].red).to.equal(137);
            expect(pixels[326].green).to.equal(166);
            expect(pixels[326].blue).to.equal(193);
            done();
        });
    });

    it ('should load image from disk without resizing', function(done) {
        var file = './test_resources/rainbow.jpg';
        image.get(file, null, function(err, imageContext) {
            expect(err).not;
            expect(imageContext).not.null.and.not.undefined;
            expect(imageContext.canvas.width).to.equal(1920);
            expect(imageContext.canvas.height).to.equal(1200);
            done();
        });
    });

    it ('should load image from disk downscaling it', function(done) {
        var file = './test_resources/rainbow.jpg';
        image.get(file, {width: 64, height: 128}, function(err, imageContext) {
            expect(err).not;
            expect(imageContext).not.null.and.not.undefined;
            expect(imageContext.canvas.width).to.equal(64);
            expect(imageContext.canvas.height).to.equal(128);
            done();
        });
    });

    it ('calculate average rgb from image', function(done) {
        var file = './test_resources/kind-of-blue.jpg';
        image.get(file, null, function(err, imageContext) {
            var rgbAverage = image.average(imageContext);
            expect(rgbAverage.red).to.equal(6);
            expect(rgbAverage.green).to.equal(166);
            expect(rgbAverage.blue).to.equal(187);
            done();
        });
    });
});