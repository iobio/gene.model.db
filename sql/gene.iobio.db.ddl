CREATE TABLE "genes" (chr text, annotation_source text, feature_type text, start int, end int, score text, strand text, phase text, gene_name text, gene_type text, gene_status text, level int, transcripts text, source text, seqid text, species text, build text);
CREATE INDEX gene_name on "genes" (gene_name);
CREATE TABLE "transcripts" (chr text, annotation_source text, feature_type text, start int, end int, score text, strand text, phase text, transcript_id text, gene_name text, transcript_type text, transcript_status text, level int, features text, source text, seqid text, build text, species text, ccds_id text, is_canonical text);
CREATE INDEX transcript_id on transcripts (transcript_id);
CREATE INDEX start on genes (start);
CREATE INDEX end on genes (end);
CREATE TABLE xref_transcript (gencode_id text, refseq_id text, species text, build text);
CREATE INDEX idx_genes_composite on genes(source, species, build);
CREATE TABLE genes_wo_transcripts (gene_name text PRIMARY KEY);
