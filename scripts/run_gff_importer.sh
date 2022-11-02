java -classpath target/geneModelGffImporter-0.0.1-SNAPSHOT.jar:sqlite-jdbc-3.8.11.2.jar geneModelGffImporter.App gencode GRCh37 ./data/gencode/gencode.v19.annotation.gff3
java -classpath target/geneModelGffImporter-0.0.1-SNAPSHOT.jar:sqlite-jdbc-3.8.11.2.jar geneModelGffImporter.App gencode GRCh38 ./data/gencode/gencode.v41.annotation.gff3
java -classpath target/geneModelGffImporter-0.0.1-SNAPSHOT.jar:sqlite-jdbc-3.8.11.2.jar geneModelGffImporter.App refseq GRCh37 ./data/refseq/GRCh37_latest_genomic.gff
java -classpath target/geneModelGffImporter-0.0.1-SNAPSHOT.jar:sqlite-jdbc-3.8.11.2.jar geneModelGffImporter.App refseq GRCh38 ./data/refseq/GRCh38_latest_genomic.gff

echo "select source, build, count(*) from genes group by source, build;" > temp.sql
sqlite3 gene.iobio.db < temp.sql
rm temp.sql