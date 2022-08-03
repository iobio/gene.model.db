echo "select features from transcripts where gene_name == 'RAI1' and transcript_id like '%3533%';" > temp.sql
sqlite3 gene.iobio.db < temp.sql | grep UTR | sed -rn 's/.*(\"feature_type\":\"UTR\").*/\1/p'
rm temp.sql
