var sqlite3 = require('sqlite3').verbose();
var db      = new sqlite3.Database('./gene.iobio.db');
var extend  = require('node.extend');

if (process.argv.length < 5) {
	console.log("You must provide the arguments chromosome, source [refseq or gencode], and build [GRCh37 or GRCh38].");
	return;	
}
var chr = process.argv[2];
console.log("chr " + chr);

var source = process.argv[3];
console.log("source " + source);

var build = process.argv[4];
console.log("build " + build);

var sqlString = "select transcript_id, source, gene_name, features from transcripts "
+ " where source = '" + source + "'" 
+ " and build = '" + build + "'" 
+ " and chr = '" + chr + "'" 
+ " order by gene_name, transcript_id";

var counter = 1;
db.all(sqlString,function(err,rows) {
	if (err) {
		console.log(err.message)
	} else {
		console.log(rows.length + ' rows');
		rows.forEach(function(transcript) {

			var features =  JSON.parse(transcript['features']);
			
			var isUpdate = determineUTRs(transcript, features);
			if (isUpdate) {
				process.stdout.write(transcript.gene_name + " ");
				var updateStmt = "update transcripts set features =  ?"
				 + " where transcript_id = ?"
				 + " and gene_name = ?" 
				 + " and source = ?" 
                 + " and build = ?";
				db.run(updateStmt, 
					[JSON.stringify(features), transcript.transcript_id, transcript.gene_name, source, build], 
					function(err) {
						if (err) {
							console.log("An error occurred on update stmt " + updateStmt);
							console.log(err);					
						}
					});
			} else {
				process.stdout.write(" . ");
			}
			counter++;

		});

	}

}); 


function orderFeatures(feature1, feature2) {
	if (feature1.start < feature2.start) {
		return -1;
	} else if (feature1.start > feature2.start) {
		return 1;
	} else {
		return 0;
	}

}

function determineUTRs(transcript, features) {
	var isUpdate = false;

	var exonFeatures = features.filter(function(feature) {
		return feature.feature_type.toLowerCase() == "exon";
	}).sort(orderFeatures);
	var cdsFeatures = features.filter(function(feature) {
		return feature.feature_type.toLowerCase() == "cds";
	}).sort(orderFeatures);
	var utrFeatures = features.filter(function(feature) {
		return feature.feature_type.toLowerCase() == "utr";
	}).sort(orderFeatures);
	var utrLookup = {};
	utrFeatures.forEach(function(utr) {
		utrLookup[utr.start + "-" + utr.end] = utr;
	});

	exonFeatures.forEach(function(exon) {
		var  cdsMatching = cdsFeatures.filter(function(cds) {
			return exon.start <= cds.start && exon.end >= cds.end;
		});
		var utr = extend({}, exon);
		utr.feature_type = "UTR";
		if (cdsMatching.length == 1) {
			var cdsMatch = cdsMatching[0];
			if (cdsMatch.start == exon.start && 
				cdsMatch.end == exon.end) {
				// exon matches cds exactly.  so this
				// cannot be a utr
				utr = null;
			} else if (cdsMatch.start == exon.start) {
				// the utr is the end of the exon that doesn't overlap with the cds
				utr.start = cdsMatch.end;
				utr.end   = exon.end;
			} else if (cdsMatch.end == exon.end) {
				// the utr is the beginning of the exon that doesn't overlap the cds
				utr.start = exon.start;
				utr.end   = cdsMatch.start;
			} 
		} else if (cdsMatching.length == 0) {
			// the exon doesn't overlap with a cds, so the entire exon is the utr.
			utr.start = exon.start;
			utr.end   = exon.end;
		} else if (cdsMatching.length > 1) {
			console.log('WARNING - Unable to determine utrs for transcript ' + transcript.gene_name + " " + transcript.transcript_id + '.  More than one cds contained within exon ' + exon.start +  '- ' + exon.end);
		}
		if (utr && utrLookup[utr.start + "-" + utr.end] == null) {
			features.push(utr);
			isUpdate = true;
		}

	})
	return isUpdate;
}


