drop table gencode_grch37_transcript_ccds;
CREATE TABLE gencode_grch37_transcript_ccds (gene_name text, transcript_id text, ccdsId text, level text, transcript_class text);
CREATE INDEX idx_gencode_grch37_transcript_ccds on gencode_grch37_transcript_ccds(transcript_id);
.separator ","
.import data/canonical/Gencode19_GRCh37_CCDS_from_UCSC_hgTables.csv gencode_grch37_transcript_ccds

drop table gencode_grch38_transcript_canonical;
CREATE TABLE gencode_grch38_transcript_canonical (chr text, start int, end int, transcript_id text, gene_name text, transcript_id_refseq text );
CREATE INDEX idx_gencode_grch38_transcript_canonical on gencode_grch38_transcript_canonical(transcript_id);

.separator ","
.import data/canonical/Gencode41_GRCh38_knownCanonical_from_UCSC_hgTables.csv gencode_grch38_transcript_canonical


UPDATE transcripts
SET
      ccds_id = (SELECT gencode_grch37_transcript_ccds.ccdsId
                            FROM gencode_grch37_transcript_ccds
                            WHERE gencode_grch37_transcript_ccds.transcript_id = transcripts.transcript_id
                            AND transcripts.build = 'GRCh37'  and transcripts.source = 'gencode')

WHERE
    EXISTS (
        SELECT *
        FROM gencode_grch37_transcript_ccds
        WHERE gencode_grch37_transcript_ccds.transcript_id = transcripts.transcript_id 
        AND transcripts.build = 'GRCh37'  and transcripts.source = 'gencode'
    );




UPDATE transcripts
SET
      is_canonical = (SELECT 'true'
                            FROM gencode_grch38_transcript_canonical
                            WHERE gencode_grch38_transcript_canonical.transcript_id = transcripts.transcript_id
                            AND transcripts.build = 'GRCh38'  and transcripts.source = 'refseq')

WHERE
    EXISTS (
        SELECT *
        FROM gencode_grch38_transcript_canonical
        WHERE gencode_grch38_transcript_canonical.transcript_id = transcripts.transcript_id 
        AND transcripts.build = 'GRCh38'  and transcripts.source = 'refseq'
    ) ;     


