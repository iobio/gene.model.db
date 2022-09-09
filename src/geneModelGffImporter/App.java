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

	private static String gffFileName     = "";
	private static String geneModelSource = "";  // refseq or gencode
	private static String build           = "";  // GRCh37 or GRCh38

	private static String dbName          = "gene.iobio.db";
	private static String species         = "homo_sapiens";

	private static String ID     = "ID";
	private static String Gene_name       = null;
	private static String Transcript_id   = null;
	private static String Gene_type       = null;
	private static String Transcript_type = null;
	private static String Alias  = "Alias";
	private static String Parent = "Parent";
	private static String Target = "Target";
	private static String Gap    = "Gap";
	private static String Derives_from = "Derives_from";
	private static String Note   = "Note";
	private static String dbxref = "Dbxref";
	private static String Ontology_term = "Ontology_term";

	private static String chrHeaderRec = null;

	private static HashMap<String, String> genePrefixMap = new HashMap<String, String>();
	private static HashMap<String, String> transcriptPrefixMap = new HashMap<String, String>();

	private static HashMap<String, String> seqIdMap = new HashMap<String, String>();

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Specify the command line args: source build gffFileName");
			return;
		}

		geneModelSource = args[0];
		build = args[1];
		gffFileName = args[2];

		Gene_name       = geneModelSource.equals("refseq") ?  "Name" : "gene_name";
		Transcript_id   = geneModelSource.equals("refseq") ?  "Name" : "transcript_id";
		Gene_type       = geneModelSource.equals("refseq") ?  "type" : "gene_type";
		Transcript_type = geneModelSource.equals("refseq") ?  "type" : "transcript_type";
		chrHeaderRec = geneModelSource.equals("refseq") ? "##sequence-region NC" : "##sequence-region chr";

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
			seqIdMap.put("NC_000023.10", "chrX");
			seqIdMap.put("NC_000024.9", "chrY");
			seqIdMap.put("NC_012920.1", "chrMT");


			seqIdMap.put("NT_113878.1", "chr1_gl000191_random");
			seqIdMap.put("NT_167207.1", "chr1_gl000192_random");
			seqIdMap.put("NT_113885.1", "chr4_gl000193_random");
			seqIdMap.put("NT_113888.1", "chr4_gl000194_random");
			seqIdMap.put("NT_113901.1", "chr7_gl000195_random");
			seqIdMap.put("NT_113909.1", "chr8_gl000196_random");
			seqIdMap.put("NT_113907.1", "chr8_gl000197_random");
			seqIdMap.put("NT_113914.1", "chr9_gl000198_random");
			seqIdMap.put("NT_113916.2", "chr9_gl000199_random");
			seqIdMap.put("NT_113915.1", "chr9_gl000200_random");
			seqIdMap.put("NT_113911.1", "chr9_gl000201_random");
			seqIdMap.put("NT_113921.2", "chr11_gl000202_random");
			seqIdMap.put("NT_113941.1", "chr17_gl000203_random");
			seqIdMap.put("NT_113943.1", "chr17_gl000204_random");
			seqIdMap.put("NT_113930.1", "chr17_gl000205_random");
			seqIdMap.put("NT_113945.1", "chr17_gl000206_random");
			seqIdMap.put("NT_113947.1", "chr18_gl000207_random");
			seqIdMap.put("NT_113948.1", "chr19_gl000208_random");
			seqIdMap.put("NT_113949.1", "chr19_gl000209_random");
			seqIdMap.put("NT_113950.2", "chr21_gl000210_random");
			seqIdMap.put("NT_113961.1", "chrUn_gl000211");
			seqIdMap.put("NT_113923.1", "chrUn_gl000212");
			seqIdMap.put("NT_167208.1", "chrUn_gl000213");
			seqIdMap.put("NT_167209.1", "chrUn_gl000214");
			seqIdMap.put("NT_167210.1", "chrUn_gl000215");
			seqIdMap.put("NT_167211.1", "chrUn_gl000216");
			seqIdMap.put("NT_167212.1", "chrUn_gl000217");
			seqIdMap.put("NT_113889.1", "chrUn_gl000218");
			seqIdMap.put("NT_167213.1", "chrUn_gl000219");
			seqIdMap.put("NT_167214.1", "chrUn_gl000220");
			seqIdMap.put("NT_167215.1", "chrUn_gl000221");
			seqIdMap.put("NT_167216.1", "chrUn_gl000222");
			seqIdMap.put("NT_167217.1", "chrUn_gl000223");
			seqIdMap.put("NT_167218.1", "chrUn_gl000224");
			seqIdMap.put("NT_167219.1", "chrUn_gl000225");
			seqIdMap.put("NT_167220.1", "chrUn_gl000226");
			seqIdMap.put("NT_167221.1", "chrUn_gl000227");
			seqIdMap.put("NT_167222.1", "chrUn_gl000228");
			seqIdMap.put("NT_167223.1", "chrUn_gl000229");
			seqIdMap.put("NT_167224.1", "chrUn_gl000230");
			seqIdMap.put("NT_167225.1", "chrUn_gl000231");
			seqIdMap.put("NT_167226.1", "chrUn_gl000232");
			seqIdMap.put("NT_167227.1", "chrUn_gl000233");
			seqIdMap.put("NT_167228.1", "chrUn_gl000234");
			seqIdMap.put("NT_167229.1", "chrUn_gl000235");
			seqIdMap.put("NT_167230.1", "chrUn_gl000236");
			seqIdMap.put("NT_167231.1", "chrUn_gl000237");
			seqIdMap.put("NT_167232.1", "chrUn_gl000238");
			seqIdMap.put("NT_167233.1", "chrUn_gl000239");
			seqIdMap.put("NT_167234.1", "chrUn_gl000240");
			seqIdMap.put("NT_167235.1", "chrUn_gl000241");
			seqIdMap.put("NT_167236.1", "chrUn_gl000242");
			seqIdMap.put("NT_167237.1", "chrUn_gl000243");
			seqIdMap.put("NT_167238.1", "chrUn_gl000244");
			seqIdMap.put("NT_167239.1", "chrUn_gl000245");
			seqIdMap.put("NT_167240.1", "chrUn_gl000246");
			seqIdMap.put("NT_167241.1", "chrUn_gl000247");
			seqIdMap.put("NT_167242.1", "chrUn_gl000248");
			seqIdMap.put("NT_167243.1", "chrUn_gl000249	");
			seqIdMap.put("NT_167244.1", "chr6_apd_hap1");
			seqIdMap.put("NT_113891.2", "chr6_cox_hap2");
			seqIdMap.put("NT_167245.1", "chr6_dbb_hap3");
			seqIdMap.put("NT_167246.1", "chr6_mann_hap4");
			seqIdMap.put("NT_167247.1", "chr6_mcf_hap5");
			seqIdMap.put("NT_167248.1", "chr6_qbl_hap6");
			seqIdMap.put("NT_167249.1", "chr6_ssto_hap7");
			seqIdMap.put("NT_167250.1", "chr4_ctg9_hap1");
			seqIdMap.put("NT_167251.1", "chr17_ctg5_hap1");
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
			seqIdMap.put("NC_000023.11", "chrX");
			seqIdMap.put("NC_000024.10", "chrY");
			seqIdMap.put("NC_012920.1", "chrMT");

			seqIdMap.put("NT_187361.1", "chr1_KI270706v1_random");
			seqIdMap.put("NT_187362.1", "chr1_KI270707v1_random");
			seqIdMap.put("NT_187363.1", "chr1_KI270708v1_random");
			seqIdMap.put("NT_187364.1", "chr1_KI270709v1_random");
			seqIdMap.put("NT_187365.1", "chr1_KI270710v1_random");
			seqIdMap.put("NT_187366.1", "chr1_KI270711v1_random");
			seqIdMap.put("NT_187367.1", "chr1_KI270712v1_random");
			seqIdMap.put("NT_187368.1", "chr1_KI270713v1_random");
			seqIdMap.put("NT_187369.1", "chr1_KI270714v1_random");
			seqIdMap.put("NT_187370.1", "chr2_KI270715v1_random");
			seqIdMap.put("NT_187371.1", "chr2_KI270716v1_random");
			seqIdMap.put("NT_167215.1", "chr3_GL000221v1_random");
			seqIdMap.put("NT_113793.3", "chr4_GL000008v2_random");
			seqIdMap.put("NT_113948.1", "chr5_GL000208v1_random");
			seqIdMap.put("NT_187372.1", "chr9_KI270717v1_random");
			seqIdMap.put("NT_187373.1", "chr9_KI270718v1_random");
			seqIdMap.put("NT_187374.1", "chr9_KI270719v1_random");
			seqIdMap.put("NT_187375.1", "chr9_KI270720v1_random");
			seqIdMap.put("NT_187376.1", "chr11_KI270721v1_random");
			seqIdMap.put("NT_113796.3", "chr14_GL000009v2_random");
			seqIdMap.put("NT_113888.1", "chr14_GL000194v1_random");
			seqIdMap.put("NT_167219.1", "chr14_GL000225v1_random");
			seqIdMap.put("NT_187377.1", "chr14_KI270722v1_random");
			seqIdMap.put("NT_187378.1", "chr14_KI270723v1_random");
			seqIdMap.put("NT_187379.1", "chr14_KI270724v1_random");
			seqIdMap.put("NT_187380.1", "chr14_KI270725v1_random");
			seqIdMap.put("NT_187381.1", "chr14_KI270726v1_random");
			seqIdMap.put("NT_187382.1", "chr15_KI270727v1_random");
			seqIdMap.put("NT_187383.1", "chr16_KI270728v1_random");
			seqIdMap.put("NT_113930.2", "chr17_GL000205v2_random");
			seqIdMap.put("NT_187384.1", "chr17_KI270729v1_random");
			seqIdMap.put("NT_187385.1", "chr17_KI270730v1_random");
			seqIdMap.put("NT_187386.1", "chr22_KI270731v1_random");
			seqIdMap.put("NT_187387.1", "chr22_KI270732v1_random");
			seqIdMap.put("NT_187388.1", "chr22_KI270733v1_random");
			seqIdMap.put("NT_187389.1", "chr22_KI270734v1_random");
			seqIdMap.put("NT_187390.1", "chr22_KI270735v1_random");
			seqIdMap.put("NT_187391.1", "chr22_KI270736v1_random");
			seqIdMap.put("NT_187392.1", "chr22_KI270737v1_random");
			seqIdMap.put("NT_187393.1", "chr22_KI270738v1_random");
			seqIdMap.put("NT_187394.1", "chr22_KI270739v1_random");
			seqIdMap.put("NT_187395.1", "chrY_KI270740v1_random");
			seqIdMap.put("NT_113901.1", "chrUn_GL000195v1");
			seqIdMap.put("NT_167208.1", "chrUn_GL000213v1");
			seqIdMap.put("NT_167209.1", "chrUn_GL000214v1");
			seqIdMap.put("NT_167211.2", "chrUn_GL000216v2");
			seqIdMap.put("NT_113889.1", "chrUn_GL000218v1");
			seqIdMap.put("NT_167213.1", "chrUn_GL000219v1");
			seqIdMap.put("NT_167214.1", "chrUn_GL000220v1");
			seqIdMap.put("NT_167218.1", "chrUn_GL000224v1");
			seqIdMap.put("NT_167220.1", "chrUn_GL000226v1");
			seqIdMap.put("NT_187396.1", "chrUn_KI270302v1");
			seqIdMap.put("NT_187398.1", "chrUn_KI270303v1");
			seqIdMap.put("NT_187397.1", "chrUn_KI270304v1");
			seqIdMap.put("NT_187399.1", "chrUn_KI270305v1");
			seqIdMap.put("NT_187402.1", "chrUn_KI270310v1");
			seqIdMap.put("NT_187406.1", "chrUn_KI270311v1");
			seqIdMap.put("NT_187405.1", "chrUn_KI270312v1");
			seqIdMap.put("NT_187404.1", "chrUn_KI270315v1");
			seqIdMap.put("NT_187403.1", "chrUn_KI270316v1");
			seqIdMap.put("NT_187407.1", "chrUn_KI270317v1");
			seqIdMap.put("NT_187401.1", "chrUn_KI270320v1");
			seqIdMap.put("NT_187400.1", "chrUn_KI270322v1");
			seqIdMap.put("NT_187459.1", "chrUn_KI270329v1");
			seqIdMap.put("NT_187458.1", "chrUn_KI270330v1");
			seqIdMap.put("NT_187461.1", "chrUn_KI270333v1");
			seqIdMap.put("NT_187460.1", "chrUn_KI270334v1");
			seqIdMap.put("NT_187462.1", "chrUn_KI270335v1");
			seqIdMap.put("NT_187465.1", "chrUn_KI270336v1");
			seqIdMap.put("NT_187466.1", "chrUn_KI270337v1");
			seqIdMap.put("NT_187463.1", "chrUn_KI270338v1");
			seqIdMap.put("NT_187464.1", "chrUn_KI270340v1");
			seqIdMap.put("NT_187469.1", "chrUn_KI270362v1");
			seqIdMap.put("NT_187467.1", "chrUn_KI270363v1");
			seqIdMap.put("NT_187468.1", "chrUn_KI270364v1");
			seqIdMap.put("NT_187470.1", "chrUn_KI270366v1");
			seqIdMap.put("NT_187494.1", "chrUn_KI270371v1");
			seqIdMap.put("NT_187491.1", "chrUn_KI270372v1");
			seqIdMap.put("NT_187492.1", "chrUn_KI270373v1");
			seqIdMap.put("NT_187490.1", "chrUn_KI270374v1");
			seqIdMap.put("NT_187493.1", "chrUn_KI270375v1");
			seqIdMap.put("NT_187489.1", "chrUn_KI270376v1");
			seqIdMap.put("NT_187471.1", "chrUn_KI270378v1");
			seqIdMap.put("NT_187472.1", "chrUn_KI270379v1");
			seqIdMap.put("NT_187486.1", "chrUn_KI270381v1");
			seqIdMap.put("NT_187488.1", "chrUn_KI270382v1");
			seqIdMap.put("NT_187482.1", "chrUn_KI270383v1");
			seqIdMap.put("NT_187484.1", "chrUn_KI270384v1");
			seqIdMap.put("NT_187487.1", "chrUn_KI270385v1");
			seqIdMap.put("NT_187480.1", "chrUn_KI270386v1");
			seqIdMap.put("NT_187475.1", "chrUn_KI270387v1");
			seqIdMap.put("NT_187478.1", "chrUn_KI270388v1");
			seqIdMap.put("NT_187473.1", "chrUn_KI270389v1");
			seqIdMap.put("NT_187474.1", "chrUn_KI270390v1");
			seqIdMap.put("NT_187481.1", "chrUn_KI270391v1");
			seqIdMap.put("NT_187485.1", "chrUn_KI270392v1");
			seqIdMap.put("NT_187483.1", "chrUn_KI270393v1");
			seqIdMap.put("NT_187479.1", "chrUn_KI270394v1");
			seqIdMap.put("NT_187476.1", "chrUn_KI270395v1");
			seqIdMap.put("NT_187477.1", "chrUn_KI270396v1");
			seqIdMap.put("NT_187409.1", "chrUn_KI270411v1");
			seqIdMap.put("NT_187408.1", "chrUn_KI270412v1");
			seqIdMap.put("NT_187410.1", "chrUn_KI270414v1");
			seqIdMap.put("NT_187415.1", "chrUn_KI270417v1");
			seqIdMap.put("NT_187412.1", "chrUn_KI270418v1");
			seqIdMap.put("NT_187411.1", "chrUn_KI270419v1");
			seqIdMap.put("NT_187413.1", "chrUn_KI270420v1");
			seqIdMap.put("NT_187416.1", "chrUn_KI270422v1");
			seqIdMap.put("NT_187417.1", "chrUn_KI270423v1");
			seqIdMap.put("NT_187414.1", "chrUn_KI270424v1");
			seqIdMap.put("NT_187418.1", "chrUn_KI270425v1");
			seqIdMap.put("NT_187419.1", "chrUn_KI270429v1");
			seqIdMap.put("NT_187424.1", "chrUn_KI270435v1");
			seqIdMap.put("NT_187425.1", "chrUn_KI270438v1");
			seqIdMap.put("NT_187420.1", "chrUn_KI270442v1");
			seqIdMap.put("NT_187495.1", "chrUn_KI270448v1");
			seqIdMap.put("NT_187422.1", "chrUn_KI270465v1");
			seqIdMap.put("NT_187421.1", "chrUn_KI270466v1");
			seqIdMap.put("NT_187423.1", "chrUn_KI270467v1");
			seqIdMap.put("NT_187426.1", "chrUn_KI270468v1");
			seqIdMap.put("NT_187437.1", "chrUn_KI270507v1");
			seqIdMap.put("NT_187430.1", "chrUn_KI270508v1");
			seqIdMap.put("NT_187428.1", "chrUn_KI270509v1");
			seqIdMap.put("NT_187427.1", "chrUn_KI270510v1");
			seqIdMap.put("NT_187435.1", "chrUn_KI270511v1");
			seqIdMap.put("NT_187432.1", "chrUn_KI270512v1");
			seqIdMap.put("NT_187436.1", "chrUn_KI270515v1");
			seqIdMap.put("NT_187431.1", "chrUn_KI270516v1");
			seqIdMap.put("NT_187438.1", "chrUn_KI270517v1");
			seqIdMap.put("NT_187429.1", "chrUn_KI270518v1");
			seqIdMap.put("NT_187433.1", "chrUn_KI270519v1");
			seqIdMap.put("NT_187496.1", "chrUn_KI270521v1");
			seqIdMap.put("NT_187434.1", "chrUn_KI270522v1");
			seqIdMap.put("NT_187440.1", "chrUn_KI270528v1");
			seqIdMap.put("NT_187439.1", "chrUn_KI270529v1");
			seqIdMap.put("NT_187441.1", "chrUn_KI270530v1");
			seqIdMap.put("NT_187443.1", "chrUn_KI270538v1");
			seqIdMap.put("NT_187442.1", "chrUn_KI270539v1");
			seqIdMap.put("NT_187444.1", "chrUn_KI270544v1");
			seqIdMap.put("NT_187445.1", "chrUn_KI270548v1");
			seqIdMap.put("NT_187450.1", "chrUn_KI270579v1");
			seqIdMap.put("NT_187448.1", "chrUn_KI270580v1");
			seqIdMap.put("NT_187449.1", "chrUn_KI270581v1");
			seqIdMap.put("NT_187454.1", "chrUn_KI270582v1");
			seqIdMap.put("NT_187446.1", "chrUn_KI270583v1");
			seqIdMap.put("NT_187453.1", "chrUn_KI270584v1");
			seqIdMap.put("NT_187447.1", "chrUn_KI270587v1");
			seqIdMap.put("NT_187455.1", "chrUn_KI270588v1");
			seqIdMap.put("NT_187451.1", "chrUn_KI270589v1");
			seqIdMap.put("NT_187452.1", "chrUn_KI270590v1");
			seqIdMap.put("NT_187457.1", "chrUn_KI270591v1");
			seqIdMap.put("NT_187456.1", "chrUn_KI270593v1");
			seqIdMap.put("NT_187497.1", "chrUn_KI270741v1");
			seqIdMap.put("NT_187513.1", "chrUn_KI270742v1");
			seqIdMap.put("NT_187498.1", "chrUn_KI270743v1");
			seqIdMap.put("NT_187499.1", "chrUn_KI270744v1");
			seqIdMap.put("NT_187500.1", "chrUn_KI270745v1");
			seqIdMap.put("NT_187501.1", "chrUn_KI270746v1");
			seqIdMap.put("NT_187502.1", "chrUn_KI270747v1");
			seqIdMap.put("NT_187503.1", "chrUn_KI270748v1");
			seqIdMap.put("NT_187504.1", "chrUn_KI270749v1");
			seqIdMap.put("NT_187505.1", "chrUn_KI270750v1");
			seqIdMap.put("NT_187506.1", "chrUn_KI270751v1");
			seqIdMap.put("NT_187508.1", "chrUn_KI270753v1");
			seqIdMap.put("NT_187509.1", "chrUn_KI270754v1");
			seqIdMap.put("NT_187510.1", "chrUn_KI270755v1");
			seqIdMap.put("NT_187511.1", "chrUn_KI270756v1");
			seqIdMap.put("NT_187512.1", "chrUn_KI270757v1");
			seqIdMap.put("NW_009646194.1", "chr1_KN196472v1_fix");
			seqIdMap.put("NW_009646195.1", "chr1_KN196473v1_fix");
			seqIdMap.put("NW_009646196.1", "chr1_KN196474v1_fix");
			seqIdMap.put("NW_011332687.1", "chr1_KN538360v1_fix");
			seqIdMap.put("NW_011332688.1", "chr1_KN538361v1_fix");
			seqIdMap.put("NW_012132914.1", "chr1_KQ031383v1_fix");
			seqIdMap.put("NW_018654708.1", "chr1_KZ208906v1_fix");
			seqIdMap.put("NW_019805487.1", "chr1_KZ559100v1_fix");
			seqIdMap.put("NW_014040925.1", "chr1_KQ458382v1_alt");
			seqIdMap.put("NW_014040926.1", "chr1_KQ458383v1_alt");
			seqIdMap.put("NW_014040927.1", "chr1_KQ458384v1_alt");
			seqIdMap.put("NW_015495298.1", "chr1_KQ983255v1_alt");
			seqIdMap.put("NW_017852928.1", "chr1_KV880763v1_alt");
			seqIdMap.put("NW_018654706.1", "chr1_KZ208904v1_alt");
			seqIdMap.put("NW_018654707.1", "chr1_KZ208905v1_alt");
			seqIdMap.put("NW_011332689.1", "chr2_KN538362v1_fix");
			seqIdMap.put("NW_011332690.1", "chr2_KN538363v1_fix");
			seqIdMap.put("NW_012132915.1", "chr2_KQ031384v1_fix");
			//seqIdMap.put("NW_021159987.1", "na");
			//seqIdMap.put("NW_021159988.1", "na");
			seqIdMap.put("NW_015495299.1", "chr2_KQ983256v1_alt");
			seqIdMap.put("NW_018654709.1", "chr2_KZ208907v1_alt");
			seqIdMap.put("NW_018654710.1", "chr2_KZ208908v1_alt");
			seqIdMap.put("NW_009646197.1", "chr3_KN196475v1_fix");
			seqIdMap.put("NW_009646198.1", "chr3_KN196476v1_fix");
			seqIdMap.put("NW_011332691.1", "chr3_KN538364v1_fix");
			seqIdMap.put("NW_012132916.1", "chr3_KQ031385v1_fix");
			seqIdMap.put("NW_012132917.1", "chr3_KQ031386v1_fix");
			seqIdMap.put("NW_017363813.1", "chr3_KV766192v1_fix");
			seqIdMap.put("NW_019805491.1", "chr3_KZ559104v1_fix");
			seqIdMap.put("NW_018654711.1", "chr3_KZ208909v1_alt");
			seqIdMap.put("NW_019805488.1", "chr3_KZ559101v1_alt");
			seqIdMap.put("NW_019805489.1", "chr3_KZ559102v1_alt");
			seqIdMap.put("NW_019805490.1", "chr3_KZ559103v1_alt");
			seqIdMap.put("NW_019805492.1", "chr3_KZ559105v1_alt");
			//seqIdMap.put("NW_021159989.1", "na");
			seqIdMap.put("NW_015495300.1", "chr4_KQ983257v1_fix");
			//seqIdMap.put("NW_021159990.1", "na");
			//seqIdMap.put("NW_021159991.1", "na");
			//seqIdMap.put("NW_021159992.1", "na");
			//seqIdMap.put("NW_021159993.1", "na");
			//seqIdMap.put("NW_021159994.1", "na");
			//seqIdMap.put("NW_021159995.1", "na");
			seqIdMap.put("NW_013171799.1", "chr4_KQ090013v1_alt");
			seqIdMap.put("NW_013171800.1", "chr4_KQ090014v1_alt");
			seqIdMap.put("NW_013171801.1", "chr4_KQ090015v1_alt");
			seqIdMap.put("NW_015495301.1", "chr4_KQ983258v1_alt");
			seqIdMap.put("NW_017363814.1", "chr4_KV766193v1_alt");
			seqIdMap.put("NW_016107298.1", "chr5_KV575244v1_fix");
			//seqIdMap.put("NW_021159996.1", "na");
			seqIdMap.put("NW_009646199.1", "chr5_KN196477v1_alt");
			seqIdMap.put("NW_016107297.1", "chr5_KV575243v1_alt");
			seqIdMap.put("NW_018654712.1", "chr5_KZ208910v1_alt");
			seqIdMap.put("NW_009646200.1", "chr6_KN196478v1_fix");
			seqIdMap.put("NW_012132918.1", "chr6_KQ031387v1_fix");
			seqIdMap.put("NW_013171802.1", "chr6_KQ090016v1_fix");
			seqIdMap.put("NW_017363815.1", "chr6_KV766194v1_fix");
			seqIdMap.put("NW_018654713.1", "chr6_KZ208911v1_fix");
			//seqIdMap.put("NW_021159997.1", "na");
			seqIdMap.put("NW_013171803.1", "chr6_KQ090017v1_alt");
			seqIdMap.put("NW_012132919.1", "chr7_KQ031388v1_fix");
			seqIdMap.put("NW_017852929.1", "chr7_KV880764v1_fix");
			seqIdMap.put("NW_017852930.1", "chr7_KV880765v1_fix");
			seqIdMap.put("NW_018654714.1", "chr7_KZ208912v1_fix");
			//seqIdMap.put("NW_021159998.1", "na");
			seqIdMap.put("NW_018654715.1", "chr7_KZ208913v1_alt");
			seqIdMap.put("NW_019805493.1", "chr7_KZ559106v1_alt");
			seqIdMap.put("NW_017852931.1", "chr8_KV880766v1_fix");
			seqIdMap.put("NW_017852932.1", "chr8_KV880767v1_fix");
			seqIdMap.put("NW_018654716.1", "chr8_KZ208914v1_fix");
			seqIdMap.put("NW_018654717.1", "chr8_KZ208915v1_fix");
			seqIdMap.put("NW_019805494.1", "chr8_KZ559107v1_alt");
			seqIdMap.put("NW_009646201.1", "chr9_KN196479v1_fix");
			//seqIdMap.put("NW_021159999.1", "na");
			seqIdMap.put("NW_013171804.1", "chr9_KQ090018v1_alt");
			seqIdMap.put("NW_013171805.1", "chr9_KQ090019v1_alt");
			seqIdMap.put("NW_009646202.1", "chr10_KN196480v1_fix");
			seqIdMap.put("NW_011332692.1", "chr10_KN538365v1_fix");
			seqIdMap.put("NW_011332693.1", "chr10_KN538366v1_fix");
			seqIdMap.put("NW_011332694.1", "chr10_KN538367v1_fix");
			seqIdMap.put("NW_013171807.1", "chr10_KQ090021v1_fix");
			//seqIdMap.put("NW_021160000.1", "na");
			//seqIdMap.put("NW_021160001.1", "na");
			seqIdMap.put("NW_013171806.1", "chr10_KQ090020v1_alt");
			seqIdMap.put("NW_009646203.1", "chr11_KN196481v1_fix");
			seqIdMap.put("NW_013171808.1", "chr11_KQ090022v1_fix");
			seqIdMap.put("NW_015148966.1", "chr11_KQ759759v1_fix");
			seqIdMap.put("NW_017363816.1", "chr11_KV766195v1_fix");
			seqIdMap.put("NW_019805495.1", "chr11_KZ559108v1_fix");
			seqIdMap.put("NW_019805496.1", "chr11_KZ559109v1_fix");
			//seqIdMap.put("NW_021160002.1", "na");
			//seqIdMap.put("NW_021160003.1", "na");
			//seqIdMap.put("NW_021160004.1", "na");
			//seqIdMap.put("NW_021160005.1", "na");
			//seqIdMap.put("NW_021160006.1", "na");
			seqIdMap.put("NW_011332695.1", "chr11_KN538368v1_alt");
			seqIdMap.put("NW_019805497.1", "chr11_KZ559110v1_alt");
			seqIdMap.put("NW_019805498.1", "chr11_KZ559111v1_alt");
			seqIdMap.put("NW_009646204.1", "chr12_KN196482v1_fix");
			seqIdMap.put("NW_011332696.1", "chr12_KN538369v1_fix");
			seqIdMap.put("NW_011332697.1", "chr12_KN538370v1_fix");
			seqIdMap.put("NW_015148967.1", "chr12_KQ759760v1_fix");
			seqIdMap.put("NW_018654718.1", "chr12_KZ208916v1_fix");
			seqIdMap.put("NW_018654719.1", "chr12_KZ208917v1_fix");
			//seqIdMap.put("NW_021160007.1", "na");
			//seqIdMap.put("NW_021160008.1", "na");
			seqIdMap.put("NW_013171809.1", "chr12_KQ090023v1_alt");
			seqIdMap.put("NW_018654720.1", "chr12_KZ208918v1_alt");
			seqIdMap.put("NW_019805499.1", "chr12_KZ559112v1_alt");
			seqIdMap.put("NW_009646205.1", "chr13_KN196483v1_fix");
			seqIdMap.put("NW_011332698.1", "chr13_KN538371v1_fix");
			seqIdMap.put("NW_011332699.1", "chr13_KN538372v1_fix");
			seqIdMap.put("NW_011332700.1", "chr13_KN538373v1_fix");
			//seqIdMap.put("NW_021160009.1", "na");
			//seqIdMap.put("NW_021160010.1", "na");
			//seqIdMap.put("NW_021160011.1", "na");
			//seqIdMap.put("NW_021160012.1", "na");
			seqIdMap.put("NW_013171810.1", "chr13_KQ090024v1_alt");
			seqIdMap.put("NW_013171811.1", "chr13_KQ090025v1_alt");
			seqIdMap.put("NW_018654722.1", "chr14_KZ208920v1_fix");
			//seqIdMap.put("NW_021160013.1", "na");
			seqIdMap.put("NW_018654721.1", "chr14_KZ208919v1_alt");
			//seqIdMap.put("NW_021160014.1", "na");
			seqIdMap.put("NW_011332701.1", "chr15_KN538374v1_fix");
			//seqIdMap.put("NW_021160015.1", "na");
			//seqIdMap.put("NW_021160016.1", "na");
			//seqIdMap.put("NW_021160017.1", "na");
			//seqIdMap.put("NW_021160018.1", "na");
			seqIdMap.put("NW_012132920.1", "chr15_KQ031389v1_alt");
			seqIdMap.put("NW_017852933.1", "chr16_KV880768v1_fix");
			seqIdMap.put("NW_019805500.1", "chr16_KZ559113v1_fix");
			//seqIdMap.put("NW_021160019.1", "na");
			seqIdMap.put("NW_012132921.1", "chr16_KQ031390v1_alt");
			seqIdMap.put("NW_013171812.1", "chr16_KQ090026v1_alt");
			seqIdMap.put("NW_013171813.1", "chr16_KQ090027v1_alt");
			seqIdMap.put("NW_018654723.1", "chr16_KZ208921v1_alt");
			seqIdMap.put("NW_016107299.1", "chr17_KV575245v1_fix");
			seqIdMap.put("NW_017363817.1", "chr17_KV766196v1_fix");
			//seqIdMap.put("NW_021160020.1", "na");
			//seqIdMap.put("NW_021160021.1", "na");
			seqIdMap.put("NW_017363818.1", "chr17_KV766197v1_alt");
			seqIdMap.put("NW_017363819.1", "chr17_KV766198v1_alt");
			seqIdMap.put("NW_019805501.1", "chr17_KZ559114v1_alt");
			seqIdMap.put("NW_013171814.1", "chr18_KQ090028v1_fix");
			seqIdMap.put("NW_018654724.1", "chr18_KZ208922v1_fix");
			seqIdMap.put("NW_019805502.1", "chr18_KZ559115v1_fix");
			seqIdMap.put("NW_014040928.1", "chr18_KQ458385v1_alt");
			seqIdMap.put("NW_019805503.1", "chr18_KZ559116v1_alt");
			seqIdMap.put("NW_009646206.1", "chr19_KN196484v1_fix");
			seqIdMap.put("NW_014040929.1", "chr19_KQ458386v1_fix");
			//seqIdMap.put("NW_021160022.1", "na");
			seqIdMap.put("NW_016107300.1", "chr19_KV575246v1_alt");
			seqIdMap.put("NW_016107301.1", "chr19_KV575247v1_alt");
			seqIdMap.put("NW_016107302.1", "chr19_KV575248v1_alt");
			seqIdMap.put("NW_016107303.1", "chr19_KV575249v1_alt");
			seqIdMap.put("NW_016107304.1", "chr19_KV575250v1_alt");
			seqIdMap.put("NW_016107305.1", "chr19_KV575251v1_alt");
			seqIdMap.put("NW_016107306.1", "chr19_KV575252v1_alt");
			seqIdMap.put("NW_016107307.1", "chr19_KV575253v1_alt");
			seqIdMap.put("NW_016107308.1", "chr19_KV575254v1_alt");
			seqIdMap.put("NW_016107309.1", "chr19_KV575255v1_alt");
			seqIdMap.put("NW_016107310.1", "chr19_KV575256v1_alt");
			seqIdMap.put("NW_016107311.1", "chr19_KV575257v1_alt");
			seqIdMap.put("NW_016107312.1", "chr19_KV575258v1_alt");
			seqIdMap.put("NW_016107313.1", "chr19_KV575259v1_alt");
			seqIdMap.put("NW_016107314.1", "chr19_KV575260v1_alt");
			//seqIdMap.put("NW_021160023.1", "na");
			seqIdMap.put("NW_015148969.1", "chr22_KQ759762v1_fix");
			//seqIdMap.put("NW_021160024.1", "na");
			//seqIdMap.put("NW_021160025.1", "na");
			//seqIdMap.put("NW_021160026.1", "na");
			seqIdMap.put("NW_009646207.1", "chr22_KN196485v1_alt");
			seqIdMap.put("NW_009646208.1", "chr22_KN196486v1_alt");
			seqIdMap.put("NW_014040930.1", "chr22_KQ458387v1_alt");
			seqIdMap.put("NW_014040931.1", "chr22_KQ458388v1_alt");
			seqIdMap.put("NW_015148968.1", "chr22_KQ759761v1_alt");
			//seqIdMap.put("NW_021160027.1", "na");
			//seqIdMap.put("NW_021160028.1", "na");
			//seqIdMap.put("NW_021160029.1", "na");
			//seqIdMap.put("NW_021160030.1", "na");
			//seqIdMap.put("NW_021160031.1", "na");
			seqIdMap.put("NW_017363820.1", "chrX_KV766199v1_alt");
			seqIdMap.put("NW_009646209.1", "chrY_KN196487v1_fix");
			seqIdMap.put("NW_018654725.1", "chrY_KZ208923v1_fix");
			seqIdMap.put("NW_018654726.1", "chrY_KZ208924v1_fix");
			seqIdMap.put("NW_003315905.1", "chr1_GL383518v1_alt");
			seqIdMap.put("NW_003315906.1", "chr1_GL383519v1_alt");
			seqIdMap.put("NW_003315907.2", "chr1_GL383520v2_alt");
			seqIdMap.put("NT_187516.1", "chr1_KI270759v1_alt");
			seqIdMap.put("NT_187514.1", "chr1_KI270760v1_alt");
			seqIdMap.put("NT_187518.1", "chr1_KI270761v1_alt");
			seqIdMap.put("NT_187515.1", "chr1_KI270762v1_alt");
			seqIdMap.put("NT_187519.1", "chr1_KI270763v1_alt");
			seqIdMap.put("NT_187521.1", "chr1_KI270764v1_alt");
			seqIdMap.put("NT_187520.1", "chr1_KI270765v1_alt");
			seqIdMap.put("NT_187517.1", "chr1_KI270766v1_alt");
			seqIdMap.put("NW_003315908.1", "chr2_GL383521v1_alt");
			seqIdMap.put("NW_003315909.1", "chr2_GL383522v1_alt");
			seqIdMap.put("NW_003571033.2", "chr2_GL582966v2_alt");
			seqIdMap.put("NT_187523.1", "chr2_KI270767v1_alt");
			seqIdMap.put("NT_187528.1", "chr2_KI270768v1_alt");
			seqIdMap.put("NT_187522.1", "chr2_KI270769v1_alt");
			seqIdMap.put("NT_187525.1", "chr2_KI270770v1_alt");
			seqIdMap.put("NT_187530.1", "chr2_KI270771v1_alt");
			seqIdMap.put("NT_187524.1", "chr2_KI270772v1_alt");
			seqIdMap.put("NT_187526.1", "chr2_KI270773v1_alt");
			seqIdMap.put("NT_187529.1", "chr2_KI270774v1_alt");
			seqIdMap.put("NT_187531.1", "chr2_KI270775v1_alt");
			seqIdMap.put("NT_187527.1", "chr2_KI270776v1_alt");
			seqIdMap.put("NW_003315913.1", "chr3_GL383526v1_alt");
			seqIdMap.put("NW_003871060.2", "chr3_JH636055v2_alt");
			seqIdMap.put("NT_187533.1", "chr3_KI270777v1_alt");
			seqIdMap.put("NT_187536.1", "chr3_KI270778v1_alt");
			seqIdMap.put("NT_187532.1", "chr3_KI270779v1_alt");
			seqIdMap.put("NT_187537.1", "chr3_KI270780v1_alt");
			seqIdMap.put("NT_187538.1", "chr3_KI270781v1_alt");
			seqIdMap.put("NT_187534.1", "chr3_KI270782v1_alt");
			seqIdMap.put("NT_187535.1", "chr3_KI270783v1_alt");
			seqIdMap.put("NT_187539.1", "chr3_KI270784v1_alt");
			seqIdMap.put("NT_167250.2", "chr4_GL000257v2_alt");
			seqIdMap.put("NW_003315914.1", "chr4_GL383527v1_alt");
			seqIdMap.put("NW_003315915.1", "chr4_GL383528v1_alt");
			seqIdMap.put("NT_187542.1", "chr4_KI270785v1_alt");
			seqIdMap.put("NT_187543.1", "chr4_KI270786v1_alt");
			seqIdMap.put("NT_187541.1", "chr4_KI270787v1_alt");
			seqIdMap.put("NT_187544.1", "chr4_KI270788v1_alt");
			seqIdMap.put("NT_187545.1", "chr4_KI270789v1_alt");
			seqIdMap.put("NT_187540.1", "chr4_KI270790v1_alt");
			seqIdMap.put("NW_003315917.2", "chr5_GL339449v2_alt");
			seqIdMap.put("NW_003315918.1", "chr5_GL383530v1_alt");
			seqIdMap.put("NW_003315919.1", "chr5_GL383531v1_alt");
			seqIdMap.put("NW_003315920.1", "chr5_GL383532v1_alt");
			seqIdMap.put("NW_003571036.1", "chr5_GL949742v1_alt");
			seqIdMap.put("NT_187547.1", "chr5_KI270791v1_alt");
			seqIdMap.put("NT_187548.1", "chr5_KI270792v1_alt");
			seqIdMap.put("NT_187550.1", "chr5_KI270793v1_alt");
			seqIdMap.put("NT_187551.1", "chr5_KI270794v1_alt");
			seqIdMap.put("NT_187546.1", "chr5_KI270795v1_alt");
			seqIdMap.put("NT_187549.1", "chr5_KI270796v1_alt");
			seqIdMap.put("NT_167244.2", "chr6_GL000250v2_alt");
			seqIdMap.put("NW_003315921.1", "chr6_GL383533v1_alt");
			seqIdMap.put("NW_004166862.2", "chr6_KB021644v2_alt");
			seqIdMap.put("NT_187552.1", "chr6_KI270797v1_alt");
			seqIdMap.put("NT_187553.1", "chr6_KI270798v1_alt");
			seqIdMap.put("NT_187554.1", "chr6_KI270799v1_alt");
			seqIdMap.put("NT_187555.1", "chr6_KI270800v1_alt");
			seqIdMap.put("NT_187556.1", "chr6_KI270801v1_alt");
			seqIdMap.put("NT_187557.1", "chr6_KI270802v1_alt");
			seqIdMap.put("NW_003315922.2", "chr7_GL383534v2_alt");
			seqIdMap.put("NT_187562.1", "chr7_KI270803v1_alt");
			seqIdMap.put("NT_187558.1", "chr7_KI270804v1_alt");
			seqIdMap.put("NT_187560.1", "chr7_KI270805v1_alt");
			seqIdMap.put("NT_187559.1", "chr7_KI270806v1_alt");
			seqIdMap.put("NT_187563.1", "chr7_KI270807v1_alt");
			seqIdMap.put("NT_187564.1", "chr7_KI270808v1_alt");
			seqIdMap.put("NT_187561.1", "chr7_KI270809v1_alt");
			seqIdMap.put("NT_187567.1", "chr8_KI270810v1_alt");
			seqIdMap.put("NT_187565.1", "chr8_KI270811v1_alt");
			seqIdMap.put("NT_187568.1", "chr8_KI270812v1_alt");
			seqIdMap.put("NT_187570.1", "chr8_KI270813v1_alt");
			seqIdMap.put("NT_187566.1", "chr8_KI270814v1_alt");
			seqIdMap.put("NT_187569.1", "chr8_KI270815v1_alt");
			seqIdMap.put("NT_187571.1", "chr8_KI270816v1_alt");
			seqIdMap.put("NT_187573.1", "chr8_KI270817v1_alt");
			seqIdMap.put("NT_187572.1", "chr8_KI270818v1_alt");
			seqIdMap.put("NT_187574.1", "chr8_KI270819v1_alt");
			seqIdMap.put("NT_187575.1", "chr8_KI270820v1_alt");
			seqIdMap.put("NT_187576.1", "chr8_KI270821v1_alt");
			seqIdMap.put("NT_187577.1", "chr8_KI270822v1_alt");
			seqIdMap.put("NW_003315928.1", "chr9_GL383539v1_alt");
			seqIdMap.put("NW_003315929.1", "chr9_GL383540v1_alt");
			seqIdMap.put("NW_003315930.1", "chr9_GL383541v1_alt");
			seqIdMap.put("NW_003315931.1", "chr9_GL383542v1_alt");
			seqIdMap.put("NT_187578.1", "chr9_KI270823v1_alt");
			seqIdMap.put("NW_003315934.1", "chr10_GL383545v1_alt");
			seqIdMap.put("NW_003315935.1", "chr10_GL383546v1_alt");
			seqIdMap.put("NT_187579.1", "chr10_KI270824v1_alt");
			seqIdMap.put("NT_187580.1", "chr10_KI270825v1_alt");
			seqIdMap.put("NW_003315936.1", "chr11_GL383547v1_alt");
			seqIdMap.put("NW_003871073.1", "chr11_JH159136v1_alt");
			seqIdMap.put("NW_003871074.1", "chr11_JH159137v1_alt");
			seqIdMap.put("NT_187581.1", "chr11_KI270826v1_alt");
			seqIdMap.put("NT_187582.1", "chr11_KI270827v1_alt");
			seqIdMap.put("NT_187583.1", "chr11_KI270829v1_alt");
			seqIdMap.put("NT_187584.1", "chr11_KI270830v1_alt");
			seqIdMap.put("NT_187585.1", "chr11_KI270831v1_alt");
			seqIdMap.put("NT_187586.1", "chr11_KI270832v1_alt");
			seqIdMap.put("NW_003315938.1", "chr12_GL383549v1_alt");
			seqIdMap.put("NW_003315939.2", "chr12_GL383550v2_alt");
			seqIdMap.put("NW_003315940.1", "chr12_GL383551v1_alt");
			seqIdMap.put("NW_003315941.1", "chr12_GL383552v1_alt");
			seqIdMap.put("NW_003315942.2", "chr12_GL383553v2_alt");
			seqIdMap.put("NW_003571049.1", "chr12_GL877875v1_alt");
			seqIdMap.put("NW_003571050.1", "chr12_GL877876v1_alt");
			seqIdMap.put("NT_187589.1", "chr12_KI270833v1_alt");
			seqIdMap.put("NT_187590.1", "chr12_KI270834v1_alt");
			seqIdMap.put("NT_187587.1", "chr12_KI270835v1_alt");
			seqIdMap.put("NT_187591.1", "chr12_KI270836v1_alt");
			seqIdMap.put("NT_187588.1", "chr12_KI270837v1_alt");
			seqIdMap.put("NT_187592.1", "chr13_KI270838v1_alt");
			seqIdMap.put("NT_187593.1", "chr13_KI270839v1_alt");
			seqIdMap.put("NT_187594.1", "chr13_KI270840v1_alt");
			seqIdMap.put("NT_187595.1", "chr13_KI270841v1_alt");
			seqIdMap.put("NT_187596.1", "chr13_KI270842v1_alt");
			seqIdMap.put("NT_187597.1", "chr13_KI270843v1_alt");
			seqIdMap.put("NT_187598.1", "chr14_KI270844v1_alt");
			seqIdMap.put("NT_187599.1", "chr14_KI270845v1_alt");
			seqIdMap.put("NT_187600.1", "chr14_KI270846v1_alt");
			seqIdMap.put("NT_187601.1", "chr14_KI270847v1_alt");
			seqIdMap.put("NW_003315943.1", "chr15_GL383554v1_alt");
			seqIdMap.put("NW_003315944.2", "chr15_GL383555v2_alt");
			seqIdMap.put("NT_187603.1", "chr15_KI270848v1_alt");
			seqIdMap.put("NT_187605.1", "chr15_KI270849v1_alt");
			seqIdMap.put("NT_187606.1", "chr15_KI270850v1_alt");
			seqIdMap.put("NT_187604.1", "chr15_KI270851v1_alt");
			seqIdMap.put("NT_187602.1", "chr15_KI270852v1_alt");
			seqIdMap.put("NW_003315945.1", "chr16_GL383556v1_alt");
			seqIdMap.put("NW_003315946.1", "chr16_GL383557v1_alt");
			seqIdMap.put("NT_187607.1", "chr16_KI270853v1_alt");
			seqIdMap.put("NT_187610.1", "chr16_KI270854v1_alt");
			seqIdMap.put("NT_187608.1", "chr16_KI270855v1_alt");
			seqIdMap.put("NT_187609.1", "chr16_KI270856v1_alt");
			seqIdMap.put("NT_167251.2", "chr17_GL000258v2_alt");
			seqIdMap.put("NW_003315952.3", "chr17_GL383563v3_alt");
			seqIdMap.put("NW_003315953.2", "chr17_GL383564v2_alt");
			seqIdMap.put("NW_003315954.1", "chr17_GL383565v1_alt");
			seqIdMap.put("NW_003315955.1", "chr17_GL383566v1_alt");
			seqIdMap.put("NW_003871091.1", "chr17_JH159146v1_alt");
			seqIdMap.put("NW_003871092.1", "chr17_JH159147v1_alt");
			seqIdMap.put("NT_187614.1", "chr17_KI270857v1_alt");
			seqIdMap.put("NT_187615.1", "chr17_KI270858v1_alt");
			seqIdMap.put("NT_187616.1", "chr17_KI270859v1_alt");
			seqIdMap.put("NT_187612.1", "chr17_KI270860v1_alt");
			seqIdMap.put("NT_187611.1", "chr17_KI270861v1_alt");
			seqIdMap.put("NT_187613.1", "chr17_KI270862v1_alt");
			seqIdMap.put("NW_003315956.1", "chr18_GL383567v1_alt");
			seqIdMap.put("NW_003315957.1", "chr18_GL383568v1_alt");
			seqIdMap.put("NW_003315958.1", "chr18_GL383569v1_alt");
			seqIdMap.put("NW_003315959.1", "chr18_GL383570v1_alt");
			seqIdMap.put("NW_003315960.1", "chr18_GL383571v1_alt");
			seqIdMap.put("NW_003315961.1", "chr18_GL383572v1_alt");
			seqIdMap.put("NT_187617.1", "chr18_KI270863v1_alt");
			seqIdMap.put("NT_187618.1", "chr18_KI270864v1_alt");
			seqIdMap.put("NW_003315962.1", "chr19_GL383573v1_alt");
			seqIdMap.put("NW_003315963.1", "chr19_GL383574v1_alt");
			seqIdMap.put("NW_003315964.2", "chr19_GL383575v2_alt");
			seqIdMap.put("NW_003315965.1", "chr19_GL383576v1_alt");
			seqIdMap.put("NW_003571054.1", "chr19_GL949746v1_alt");
			seqIdMap.put("NT_187621.1", "chr19_KI270865v1_alt");
			seqIdMap.put("NT_187619.1", "chr19_KI270866v1_alt");
			seqIdMap.put("NT_187620.1", "chr19_KI270867v1_alt");
			seqIdMap.put("NT_187622.1", "chr19_KI270868v1_alt");
			seqIdMap.put("NW_003315966.2", "chr20_GL383577v2_alt");
			seqIdMap.put("NT_187623.1", "chr20_KI270869v1_alt");
			seqIdMap.put("NT_187624.1", "chr20_KI270870v1_alt");
			seqIdMap.put("NT_187625.1", "chr20_KI270871v1_alt");
			seqIdMap.put("NW_003315967.2", "chr21_GL383578v2_alt");
			seqIdMap.put("NW_003315968.2", "chr21_GL383579v2_alt");
			seqIdMap.put("NW_003315969.2", "chr21_GL383580v2_alt");
			seqIdMap.put("NW_003315970.2", "chr21_GL383581v2_alt");
			seqIdMap.put("NT_187626.1", "chr21_KI270872v1_alt");
			seqIdMap.put("NT_187627.1", "chr21_KI270873v1_alt");
			seqIdMap.put("NT_187628.1", "chr21_KI270874v1_alt");
			seqIdMap.put("NW_003315971.2", "chr22_GL383582v2_alt");
			seqIdMap.put("NW_003315972.2", "chr22_GL383583v2_alt");
			seqIdMap.put("NT_187629.1", "chr22_KI270875v1_alt");
			seqIdMap.put("NT_187630.1", "chr22_KI270876v1_alt");
			seqIdMap.put("NT_187631.1", "chr22_KI270877v1_alt");
			seqIdMap.put("NT_187632.1", "chr22_KI270878v1_alt");
			seqIdMap.put("NT_187633.1", "chr22_KI270879v1_alt");
			seqIdMap.put("NT_187634.1", "chrX_KI270880v1_alt");
			seqIdMap.put("NT_187635.1", "chrX_KI270881v1_alt");
			seqIdMap.put("NT_187646.1", "chr1_KI270892v1_alt");
			seqIdMap.put("NT_187647.1", "chr2_KI270893v1_alt");
			seqIdMap.put("NT_187648.1", "chr2_KI270894v1_alt");
			seqIdMap.put("NT_187649.1", "chr3_KI270895v1_alt");
			seqIdMap.put("NT_187650.1", "chr4_KI270896v1_alt");
			seqIdMap.put("NT_187651.1", "chr5_KI270897v1_alt");
			seqIdMap.put("NT_187652.1", "chr5_KI270898v1_alt");
			seqIdMap.put("NT_113891.3", "chr6_GL000251v2_alt");
			seqIdMap.put("NT_187653.1", "chr7_KI270899v1_alt");
			seqIdMap.put("NT_187654.1", "chr8_KI270900v1_alt");
			seqIdMap.put("NT_187655.1", "chr8_KI270901v1_alt");
			seqIdMap.put("NT_187656.1", "chr11_KI270902v1_alt");
			seqIdMap.put("NT_187657.1", "chr11_KI270903v1_alt");
			seqIdMap.put("NT_187658.1", "chr12_KI270904v1_alt");
			seqIdMap.put("NT_187660.1", "chr15_KI270905v1_alt");
			seqIdMap.put("NT_187659.1", "chr15_KI270906v1_alt");
			seqIdMap.put("NW_003871093.1", "chr17_JH159148v1_alt");
			seqIdMap.put("NT_187662.1", "chr17_KI270907v1_alt");
			seqIdMap.put("NT_187663.1", "chr17_KI270908v1_alt");
			seqIdMap.put("NT_187661.1", "chr17_KI270909v1_alt");
			seqIdMap.put("NT_187664.1", "chr17_KI270910v1_alt");
			seqIdMap.put("NT_187666.1", "chr18_KI270911v1_alt");
			seqIdMap.put("NT_187665.1", "chr18_KI270912v1_alt");
			seqIdMap.put("NW_003571055.2", "chr19_GL949747v2_alt");
			seqIdMap.put("NW_004504305.1", "chr22_KB663609v1_alt");
			seqIdMap.put("NT_187667.1", "chrX_KI270913v1_alt");
			seqIdMap.put("NT_187678.1", "chr3_KI270924v1_alt");
			seqIdMap.put("NT_187679.1", "chr4_KI270925v1_alt");
			seqIdMap.put("NT_167245.2", "chr6_GL000252v2_alt");
			seqIdMap.put("NT_187680.1", "chr8_KI270926v1_alt");
			seqIdMap.put("NT_187681.1", "chr11_KI270927v1_alt");
			seqIdMap.put("NW_003571056.2", "chr19_GL949748v2_alt");
			seqIdMap.put("NT_187682.1", "chr22_KI270928v1_alt");
			seqIdMap.put("NT_187688.1", "chr3_KI270934v1_alt");
			seqIdMap.put("NT_167246.2", "chr6_GL000253v2_alt");
			seqIdMap.put("NW_003571057.2", "chr19_GL949749v2_alt");
			seqIdMap.put("NT_187689.1", "chr3_KI270935v1_alt");
			seqIdMap.put("NT_167247.2", "chr6_GL000254v2_alt");
			seqIdMap.put("NW_003571058.2", "chr19_GL949750v2_alt");
			seqIdMap.put("NT_187690.1", "chr3_KI270936v1_alt");
			seqIdMap.put("NT_167248.2", "chr6_GL000255v2_alt");
			seqIdMap.put("NW_003571059.2", "chr19_GL949751v2_alt");
			seqIdMap.put("NT_187691.1", "chr3_KI270937v1_alt");
			seqIdMap.put("NT_167249.2", "chr6_GL000256v2_alt");
			seqIdMap.put("NW_003571060.1", "chr19_GL949752v1_alt");
			seqIdMap.put("NT_187692.1", "chr6_KI270758v1_alt");
			seqIdMap.put("NW_003571061.2", "chr19_GL949753v2_alt");
			seqIdMap.put("NT_187693.1", "chr19_KI270938v1_alt");
			seqIdMap.put("NT_187636.1", "chr19_KI270882v1_alt");
			seqIdMap.put("NT_187637.1", "chr19_KI270883v1_alt");
			seqIdMap.put("NT_187638.1", "chr19_KI270884v1_alt");
			seqIdMap.put("NT_187639.1", "chr19_KI270885v1_alt");
			seqIdMap.put("NT_187640.1", "chr19_KI270886v1_alt");
			seqIdMap.put("NT_187641.1", "chr19_KI270887v1_alt");
			seqIdMap.put("NT_187642.1", "chr19_KI270888v1_alt");
			seqIdMap.put("NT_187643.1", "chr19_KI270889v1_alt");
			seqIdMap.put("NT_187644.1", "chr19_KI270890v1_alt");
			seqIdMap.put("NT_187645.1", "chr19_KI270891v1_alt");
			seqIdMap.put("NT_187668.1", "chr19_KI270914v1_alt");
			seqIdMap.put("NT_187669.1", "chr19_KI270915v1_alt");
			seqIdMap.put("NT_187670.1", "chr19_KI270916v1_alt");
			seqIdMap.put("NT_187671.1", "chr19_KI270917v1_alt");
			seqIdMap.put("NT_187672.1", "chr19_KI270918v1_alt");
			seqIdMap.put("NT_187673.1", "chr19_KI270919v1_alt");
			seqIdMap.put("NT_187674.1", "chr19_KI270920v1_alt");
			seqIdMap.put("NT_187675.1", "chr19_KI270921v1_alt");
			seqIdMap.put("NT_187676.1", "chr19_KI270922v1_alt");
			seqIdMap.put("NT_187677.1", "chr19_KI270923v1_alt");
			seqIdMap.put("NT_187683.1", "chr19_KI270929v1_alt");
			seqIdMap.put("NT_187684.1", "chr19_KI270930v1_alt");
			seqIdMap.put("NT_187685.1", "chr19_KI270931v1_alt");
			seqIdMap.put("NT_187686.1", "chr19_KI270932v1_alt");
			seqIdMap.put("NT_187687.1", "chr19_KI270933v1_alt");
			seqIdMap.put("NT_113949.2", "chr19_GL000209v2_alt");

		}

	}

	static void loadData(Connection conn, Map<String, Feature>geneMap, Map<String, Feature>transcriptMap) {
		for( String geneId : geneMap.keySet()) {
			Feature gene = geneMap.get(geneId);
			Boolean success = insertGene(conn, gene);
			if (success) {
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
	}

	static boolean insertGene(Connection conn, Feature gene) {
		//System.out.println("inserting gene " + gene.attributeMap.get(Gene_name));
		String chr = geneModelSource.equals("refseq") ? seqIdMap.get(gene.seqid) : gene.seqid;
		if (chr != null) {
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
			    return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}


		} else {
			return false;
		}

	}

	static boolean insertTranscript(Connection conn, Feature gene, Feature transcript) {
		String features = "";
		String chr = geneModelSource.equals("refseq") ? seqIdMap.get(transcript.seqid) : transcript.seqid;
		if (chr != null) {
			for (Feature feature : transcript.childFeatures) {
				if (features.length() == 0) {
					features += "[";
				} else {
					features += ",";
				}
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
				//System.out.println("   transcript " + transcript.attributeMap.get(Transcript_id));
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
			    return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		} else {
			return false;
		}

	}


}
