package geneModelGffImporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.sql.*;

public class App {
	
	private static String gffFileName     = "/Users/tonyd/work/geneModelGffImporter/data/gencode.v19.annotation.gff3";
	private static String dbName          = "gene.iobio.db";
	private static String geneModelSource = "gencode";  // refseq or gencode
	private static String species         = "homo_sapiens";
	private static String build           = "GRCh37";  // GRCh37 or GRCh38

	private static String ID     = "ID";
	private static String Gene_name       = geneModelSource.equals("refseq") ?  "Name" : "gene_name";
	private static String Transcript_id   = geneModelSource.equals("refseq") ?  "Name" : "transcript_id";
	private static String Gene_type       = geneModelSource.equals("refseq") ?  "type" : "gene_type";
	private static String Transcript_type = geneModelSource.equals("refseq") ?  "type" : "transcript_type";
	private static String Alias  = "Alias";
	private static String Parent = "Parent";
	private static String Target = "Target";
	private static String Gap    = "Gap";
	private static String Derives_from = "Derives_from";
	private static String Note   = "Note";
	private static String dbxref = "Dbxref";
	private static String Ontology_term = "Ontology_term";

	private static String chrHeaderRec = geneModelSource.equals("refseq") ? "##sequence-region NC" : "##sequence-region chr";
	
	private static HashMap<String, String> genePrefixMap = new HashMap<String, String>();
	private static HashMap<String, String> transcriptPrefixMap = new HashMap<String, String>(); 
	
	private static HashMap<String, String> seqIdMap = new HashMap<String, String>();
	
	public static void main(String[] args) {
		initMaps();
		TreeMap<String, Feature> refMap = new TreeMap<String, Feature>();
		
		Feature gene = null;
		Feature transcript = null;
		HashMap<String, Feature> geneMap = new HashMap<String, Feature>();
		HashMap<String, Feature> transcriptMap = new HashMap<String, Feature>();
		
		Connection c = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }

		Scanner s;
		int refCount = 0;
		String ref = null;
		try {
			s = new Scanner(new File(gffFileName)).useDelimiter("\t");
			String line = "";			
		    while(s.hasNext()) {
		    	line = s.nextLine();
		    	if (line.startsWith(chrHeaderRec)) {
		    		
		    		if (ref != null) {
			    		loadData(c, geneMap, transcriptMap);		    			
		    		}
		    		
		    		String[] tokens = line.split(" ");
		    		ref = tokens[1];
		    		System.out.println(ref);
		    		refCount++;
		    		
		    		
		    		geneMap = new HashMap<String, Feature>();
		    		transcriptMap = new HashMap<String, Feature>();
			    	continue;
		    	} else if (line.startsWith("#")) {
		    		continue;
		    	}
		    	Feature feature = Feature.parse(line);
		    	
		    	if (feature.type.equals("gene")) {		    			    		
		    		gene = feature;
		    		
		    		geneMap.put(gene.attributeMap.get(ID), gene);
		    	} else if (feature.type.indexOf("transcript") >= 0 || feature.type.equals("mRNA") || feature.type.equals("miRNA") || feature.type.equals("ncRNA") || feature.type.equals("rRNA") ||feature.type.equals("snoRNA") || feature.type.equals("snRNA") || feature.type.equals("tRNA") || feature.type.equals("misc_RNA") || feature.type.equals("processed_transcript") || feature.type.equals("transcript") || feature.type.equals("scRNA")) {
		    		gene = geneMap.get(feature.attributeMap.get(Parent));
		    		
		    		if (gene != null) {
		    			gene.addChild(feature);
			    		transcriptMap.put(feature.attributeMap.get(ID), feature);
		    		} else {
		    			System.err.println("Can't find gene " + feature.attributeMap.get(Parent) + " for transcript " + feature.attributeMap.get(ID));
		    		}
		    	} else if (feature.type.equals("exon") || feature.type.equals("CDS") ||feature.type.equals("stop_codon") || feature.type.equals("start_codon")) {
		    		transcript = transcriptMap.get(feature.attributeMap.get(Parent));
		    		if (transcript != null) {
			    		transcript.addChild(feature); 		    			
		    		} else {
		    			gene = geneMap.get(feature.attributeMap.get(Parent));
		    			if (gene != null) {
		    				// Make a  "dummy" transcript 
		    				transcript = gene.cloneTranscriptFromGene();
		    				transcript.addChild(feature);
		    				transcriptMap.put(transcript.attributeMap.get(ID), transcript);
		    			} else {
		    				System.err.println("Can't find transcript (or gene) " + feature.attributeMap.get(Parent) + " for feature " + feature.type + " " + feature.attributeMap.get(ID));
		    			}
		    		}
		    	}
		    }
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch( Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	static void initMaps() {
		if (geneModelSource.equals("refseq") && build.equals("GRCh37")) {
			seqIdMap.put("NC_000001.10", "chr1");	
			seqIdMap.put("NC_000002.11", "chr2");
			seqIdMap.put("NC_000003.11", "chr3");
			seqIdMap.put("NC_000004.11", "chr4");
			seqIdMap.put("NC_000005.9",  "chr5");
			seqIdMap.put("NC_000006.11", "chr6");
			seqIdMap.put("NC_000007.13", "chr7");
			seqIdMap.put("NC_000008.10", "chr8");
			seqIdMap.put("NC_000009.11", "chr9");
			seqIdMap.put("NC_000010.10", "chr10");
			seqIdMap.put("NC_000011.9", "chr11");
			seqIdMap.put("NC_000012.11", "chr12");
			seqIdMap.put("NC_000013.10", "chr13");
			seqIdMap.put("NC_000014.8", "chr14");
			seqIdMap.put("NC_000015.9", "chr15");
			seqIdMap.put("NC_000016.9", "chr16");
			seqIdMap.put("NC_000017.10", "chr17");
			seqIdMap.put("NC_000018.9", "chr18");
			seqIdMap.put("NC_000019.9", "chr19");
			seqIdMap.put("NC_000020.10", "chr20");	
			seqIdMap.put("NC_000021.8", "chr21");
			seqIdMap.put("NC_000022.10", "chr22");
			seqIdMap.put("NC_000023.10", "chrY");
			seqIdMap.put("NC_000024.9", "chrX");
			seqIdMap.put("NC_012920.1", "chrMT");
		} else if (geneModelSource.equals("refseq") && build.equals("GRCh38")) {
			seqIdMap.put("NC_000001.11", "chr1");
			seqIdMap.put("NC_000002.12", "chr2");
			seqIdMap.put("NC_000003.12", "chr3");
			seqIdMap.put("NC_000004.12", "chr4");
			seqIdMap.put("NC_000005.10", "chr5");
			seqIdMap.put("NC_000006.12", "chr6");
			seqIdMap.put("NC_000007.14", "chr7");
			seqIdMap.put("NC_000008.11", "chr8");
			seqIdMap.put("NC_000009.12", "chr9");
			seqIdMap.put("NC_000010.11", "chr10");
			seqIdMap.put("NC_000011.10", "chr11");
			seqIdMap.put("NC_000012.12", "chr12");
			seqIdMap.put("NC_000013.11", "chr13");
			seqIdMap.put("NC_000014.9", "chr14");
			seqIdMap.put("NC_000015.10", "chr15");
			seqIdMap.put("NC_000016.10", "chr16");
			seqIdMap.put("NC_000017.11", "chr17");
			seqIdMap.put("NC_000018.10", "chr18");
			seqIdMap.put("NC_000019.10", "chr19");
			seqIdMap.put("NC_000020.11", "chr20");
			seqIdMap.put("NC_000021.9", "chr21");
			seqIdMap.put("NC_000022.11", "chr22");
			seqIdMap.put("NC_000023.11", "chrY");
			seqIdMap.put("NC_000024.10", "chrX");
			seqIdMap.put("NC_012920.1", "chrMT");
		}

	}
	
	static void loadData(Connection conn, Map<String, Feature>geneMap, Map<String, Feature>transcriptMap) {
		for( String geneId : geneMap.keySet()) {
			Feature gene = geneMap.get(geneId);
			insertGene(conn, gene);
			for (Feature tx : gene.childFeatures) {				
				Feature transcript = transcriptMap.get(tx.attributeMap.get(ID));
				if (transcript != null) {					
					insertTranscript(conn, gene, transcript);
				} else {
					System.err.println("Cannot find transcript " + tx.attributeMap.get(ID) + " " + tx.attributeMap.get(Transcript_id) + " " + tx.attributeMap.get(Parent));
				}
			}
		}
	}
	
	static void insertGene(Connection conn, Feature gene) {
		System.out.println("inserting gene " + gene.attributeMap.get(Gene_name));
		String chr = geneModelSource.equals("refseq") ? seqIdMap.get(gene.seqid) : gene.seqid;
		String transcripts = "";		
		for (Feature transcript : gene.childFeatures) {	
			if (transcripts.length() == 0) {
				transcripts += "[";
			} else {
				transcripts += ",";
			}
			transcripts += "\"" + transcript.attributeMap.get(Transcript_id) + "\"";
		}
		if (transcripts.length() > 0) {
			transcripts += "]";
		}
		Statement stmt;
		try {
			stmt = conn.createStatement();
			String sql = "INSERT INTO genes "
		      		+ " (chr, seqid, annotation_source, feature_type, start, end, score, strand, phase, gene_name, gene_type, gene_status, level, transcripts, source, species, build) " 
		            + "VALUES (" 
		      		+ "'" + chr + "'" + ","
		      		+ "'" + gene.seqid + "'" + ","
		      		+ "'" + gene.source + "'" + ","
		      		+ "'gene'" + "," 
		      		+ gene.start + ","
		      		+ gene.end +  ","
		      		+ "\".\"" + ","  // score
		      		+ "'" + gene.strand + "'" + "," 
		      		+ "'" + gene.phase + "'" + ","
		      		+ "'" + gene.attributeMap.get(Gene_name) + "'" + ","
		      		+ "'" + (gene.type == null ? "" : gene.attributeMap.get(Gene_type)) + "'" + ","      // gene type
		      		+ "\".\"" + "," //status
		      		+ "\".\"" + "," // level
		      		+ "'" + transcripts + "'"  + "," 
		      		+ "'" + geneModelSource + "'"  + "," 
		      		+ "'" + species + "'"  + "," 
		      		+ "'" + build + "'"  
		            + ");"; 
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block			
			e.printStackTrace();
		}
		
	}
	
	static void insertTranscript(Connection conn, Feature gene, Feature transcript) {
		String features = "";		
		for (Feature feature : transcript.childFeatures) {	
			if (features.length() == 0) {
				features += "[";
			} else {
				features += ",";
			}
			String chr = geneModelSource.equals("refseq") ? seqIdMap.get(transcript.seqid) : transcript.seqid;
			features += "{\"chr\":" + "\"" + chr + "\","
					 +  "\"seqid\":" + "\"" + feature.seqid + "\","
					 +  "\"annotation_source\":" + "\"" + feature.source + "\","
					 +  "\"feature_type\":" + "\"" + feature.type + "\","
					 +  "\"start\":" + "\"" + feature.start + "\","
					 +  "\"end\":" + "\"" + feature.end + "\","
					 +  "\"score\":" + "\"" + "." + "\","
					 +  "\"strand\":" + "\"" + feature.strand + "\","
					 +  "\"phase\":" + "\"" + feature.phase + "\","
					 +  "\"transcript_id\":" + "\"" + transcript.attributeMap.get(Transcript_id) + "\"}";
		}
		if (features.length() > 0) {
			features += "]";
		}
		Statement stmt;
		try {
			System.out.println("   transcript " + transcript.attributeMap.get(Transcript_id));
			String chr = geneModelSource.equals("refseq") ? seqIdMap.get(transcript.seqid) : transcript.seqid;
			stmt = conn.createStatement();
			String sql = "INSERT INTO transcripts "
		      		+ " (chr, seqid, annotation_source, feature_type, start, end, score, strand, phase, transcript_id, gene_name, transcript_type, transcript_status, level, features, source, species, build) " 
		            + "VALUES (" 
		      		+ "'" + chr + "'" + ","
		      		+ "'" + transcript.seqid + "'" + ","
		      		+ "'" + transcript.source + "'" + ","
		      		+ "'" + "transcript" + "'" + "," 
		      		+ transcript.start + ","
		      		+ transcript.end +  ","
		      		+ "\".\"" + ","  // score
		      		+ "'" + transcript.strand + "'" + "," 
		      		+ "'" + transcript.phase + "'" + ","
		      		+ "'" + transcript.attributeMap.get(Transcript_id) + "'" + ","
		      		+ "'" + gene.attributeMap.get(Gene_name) + "'" + ","
		      		+ "'" + transcript.attributeMap.get(Transcript_type) + "'" + ","
		      		+ "\".\"" + "," //status
		      		+ "\".\"" + "," // level
		      		+ "'" + features + "'"  + ","
		      		+ "'" + geneModelSource + "'"  + ","
		      		+ "'" + species + "'"  + "," 
		      		+ "'" + build + "'"   
		      		+ ");"; 
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block			
			e.printStackTrace();
		}
		
	}


}
