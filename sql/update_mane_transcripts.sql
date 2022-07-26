drop table mane_transcripts;
CREATE TABLE mane_transcripts (gene_name text, transcript_id text, transcript_id_refseq text);
CREATE INDEX idx_mane_transcripts on mane_transcripts(transcript_id);
CREATE INDEX idx_mane_transcripts on mane_transcripts(transcript_id_refseq);
.separator ","
.import /users/tony/work/gene.model.db/data/mane/transcripts.csv mane_transcripts


alter table transcripts add column (is_mane_select text);

UPDATE transcripts
SET
      is_mane_select = (SELECT 'true'
                            FROM mane_transcripts
                            WHERE mane_transcripts.transcript_id = transcripts.transcript_id
                            AND transcripts.build = 'GRCh38'  and transcripts.source = 'gencode')

WHERE
    EXISTS (
        SELECT *
        FROM mane_transcripts
        WHERE mane_transcripts.transcript_id = transcripts.transcript_id 
        AND transcripts.build = 'GRCh38'  and transcripts.source = 'gencode'
    ) ; 



UPDATE transcripts
SET
      is_mane_select = (SELECT 'true'
                            FROM mane_transcripts
                            WHERE mane_transcripts.transcript_id_refseq = transcripts.transcript_id
                            AND transcripts.build = 'GRCh38'  and transcripts.source = 'refseq')

WHERE
    EXISTS (
        SELECT *
        FROM mane_transcripts
        WHERE mane_transcripts.transcript_id_refseq = transcripts.transcript_id 
        AND transcripts.build = 'GRCh38'  and transcripts.source = 'refseq'
    ) ; 

