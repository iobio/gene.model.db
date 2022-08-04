echo "select source, build, count(*) from genes group by source, build;" > temp.sql
echo "GENE COUNTS"
sqlite3 gene.iobio.db < temp.sql
rm temp.sql

echo "select source, build, count(*) from transcripts group by source, build;" > temp.sql
echo "\nTRANSCRIPT COUNTS"
sqlite3 gene.iobio.db < temp.sql
rm temp.sql