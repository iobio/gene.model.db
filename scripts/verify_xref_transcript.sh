echo "select transcripts.source, transcripts.build, count(*) from transcripts, xref_transcript
where transcripts.transcript_id = xref_transcript.gencode_id
and transcripts.build = 'GRCh37'" > temp.sql
echo "XREF GENCODE -> REFSEQ"
sqlite3 gene.iobio.db < temp.sql
echo "select transcripts.source, transcripts.build, count(*) from transcripts, xref_transcript
where transcripts.transcript_id = xref_transcript.gencode_id
and transcripts.build = 'GRCh38'" > temp.sql
sqlite3 gene.iobio.db < temp.sql
rm temp.sql


echo "select transcripts.source, transcripts.build, count(*) from transcripts, xref_transcript
where substr(transcripts.transcript_id, 1, instr(transcripts.transcript_id,'.')-1) = xref_transcript.refseq_id
and transcripts.build = 'GRCh37' and xref_transcript.build = 'GRCh37'" > temp.sql
echo "XREF REFSEQ -> GENCODE"
sqlite3 gene.iobio.db < temp.sql
echo "select transcripts.source, transcripts.build, count(*) from transcripts, xref_transcript
where substr(transcripts.transcript_id, 1, instr(transcripts.transcript_id,'.')-1) = xref_transcript.refseq_id
and transcripts.build = 'GRCh38' and xref_transcript.build = 'GRCh38'
and xref_transcript.refseq_id != ''" > temp.sql
sqlite3 gene.iobio.db < temp.sql
rm temp.sql
