echo "select source, build, is_canonical, count(*) from transcripts where build == 'GRCh38' and source = 'gencode' group by source, build, is_canonical" > temp.sql
echo 'IS CANONICAL'
sqlite3 gene.iobio.db < temp.sql
rm temp.sql

echo "select source, build, count(*) from transcripts where ccds_id like 'CC%' group by source, build" > temp.sql
echo 'CCDS_ID'
sqlite3 gene.iobio.db < temp.sql
rm temp.sql


