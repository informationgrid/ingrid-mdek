var fs = require('fs'),
    path = require('path'),
    xpath   = require('xpath'),
    xml2js = require('xml2js'),
    dom     = require('xmldom').DOMParser,
    xmlbuilder = require('xmlbuilder'),
    async = require('async'),
    request = require('request');

var dataDir = path.join(__dirname, "data");
var select = xpath.useNamespaces({"gmd": "http://www.isotc211.org/2005/gmd", "gco":"http://www.isotc211.org/2005/gco"});
var cmd = process.argv[2];
if (!cmd) {
    console.log("Usage etl_baw_test_data.js <insert|update|delete>");
    process.exit(1);
}


var files = fs.readdirSync(dataDir);
console.log(files);

async.eachSeries(files, function(file, callback) {
    console.log("Work on " + file);
    var data = fs.readFileSync(path.join(dataDir, file), { encoding : 'UTF-8' });
    var data = data.replace(/<\?xml version="1\.0" encoding="UTF-8"\?>/g, '');
    var doc = new dom().parseFromString(data);
    var uuid = select('/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString/text()', doc).toString();
    console.log("Got UUID " + uuid);

    var transaction = xmlbuilder.create('csw:Transaction');
    transaction.att("service", "CSW");
    transaction.att("version", "2.0.2");
    transaction.att("xmlns:csw", "http://www.opengis.net/cat/csw/2.0.2");
    transaction.att("xmlns:ogc", "http://www.opengis.net/ogc");
    transaction.att("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    transaction.att("xsi:schemaLocation", "http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd");


    if (cmd == "insert") {
        var cmdEl = transaction.ele("csw:Insert");
        cmdEl.raw(data);
        cmdEl.up();
    } else if (cmd == "update") {
        var cmdEl = transaction.ele("csw:Update");
        cmdEl.raw(data);
        cmdEl.ele("csw:Constraint", { 'version': '1.1.0'})
            .ele("ogc:Filter")
                .ele("ogc:PropertyIsEqualTo")
                    .ele("ogc:PropertyName", "uuid").up()
                    .ele("ogc:Literal", uuid).up()
                .up()
            .up()
        .up()
        cmdEl.up();
    } else if (cmd == "delete") {
        var cmdEl = transaction.ele("csw:Delete");
        cmdEl.ele("csw:Constraint", { 'version': '1.1.0'})
            .ele("ogc:Filter")
                .ele("ogc:PropertyIsEqualTo")
                    .ele("ogc:PropertyName", "uuid").up()
                    .ele("ogc:Literal", uuid).up()
                .up()
            .up()
        .up()
        cmdEl.up();
    }

    var postData = transaction.end({ pretty: true });

    var options = {
      method: 'post',
      body: postData, // Javascript object
      url: "http://192.168.0.164:9905/csw-t?SERVICE=CSW&REQUEST=Transaction&catalog=/ingrid-group:iplug-ige", // http://192.168.0.237:8090/csw-t?SERVICE=CSW&REQUEST=Transaction&catalog=/ingrid-group:ige-iplug-HH
      headers: {
        'Content-Type': 'application/xml'
      },
      'auth': {
        'user': 'api',
        'pass': 'api',
        'sendImmediately': false
      }
    }

    request(options, function (err, res, body) {
      if (err) {
        console.log('Error :', err)
        return
      }
      console.log(' Response :', body)

      callback();

    });

}, function (err) {
  if (err) { throw err; }
  console.log('Finished!');
});
