echo "select source, build, is_mane_select, count(*) from transcripts where build == 'GRCh38' group by source, build, is_canonical" > temp.sql
echo 'IS MANE SELECT'
sqlite3 gene.iobio.db < temp.sql
rm temp.sql