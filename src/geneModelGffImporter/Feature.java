package geneModelGffImporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Feature {
	private static int idx_seqid = 0;
	private static int idx_source = 1;
	private static int idx_type = 2;
	private static int idx_start = 3;
	private static int idx_end = 4;
	private static int idx_score = 5;
	private static int idx_strand = 6;
	private static int idx_phase = 7;
	private static int idx_attributes = 8;


	
	String seqid = null;
	String source = null;
	String type = null;
	String start = null;
	String end = null;
	String score  = null;
	String strand = null;
	String phase = null;
	String attributes = null;
	
	HashMap<String, String> attributeMap = new HashMap<String, String>();
	
	List<Feature> childFeatures = new ArrayList<Feature>();
	
	public static Feature parse(String line) {
		Feature feature = new Feature();
		String[] tokens = line.split("\t");
		
		feature.seqid = tokens[idx_seqid];
		feature.source = tokens[idx_source];
		feature.type = tokens[idx_type];
		feature.start = tokens[idx_start];
		feature.end = tokens[idx_end];
		feature.score = tokens[idx_score];
		feature.strand = tokens[idx_strand];
		feature.phase = tokens[idx_phase];
		String attributes = tokens[idx_attributes];
		
		tokens = attributes.split(";");
		for (int i = 0; i < tokens.length; i++) {
			String tagValue[] = tokens[i].split("=");
			String tag = tagValue[0];
			String value = tagValue[1];
			feature.attributeMap.put(tag, value);
		}
		return feature;
	}
	
	public void addChild(Feature childFeature) {
		childFeatures.add(childFeature);
	}
	
	public String getID() {
		return this.attributeMap.get("ID");
	}
	
	public Feature cloneTranscriptFromGene() {
		Feature f = new Feature();
		f.seqid = this.seqid;
		f.source = this.source;
		f.type = "transcript";
		f.start = this.start;
		f.end = this.end;
		f.score = this.score;
		f.strand = this.strand;
		f.phase = this.phase;
		f.attributeMap = (HashMap<String, String>)this.attributeMap.clone();
		f.attributeMap.put("Name", "placeholder");
		return f;
	}

}
