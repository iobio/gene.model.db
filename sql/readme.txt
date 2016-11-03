How to get xref from gencode to refseq transcripts
 1.  For GRCh38, use UCSC table browser. (see howto_ucsc_table_browser_xref_transcripts.png)
 2.  For GRCh27, use Ensembl Biomart.  (see howto_ensembl_biomart_xref_transcripts.png)
 3.  Use excel to convert tsv to csv
 4.  Now run sql script insert_transcripts_xref.sql in gene.iobio.db


 How to get canonical and ccsd flags on transcripts
 1.  For GRCh38, you can get the canonical designation for a transcript using the UCSC 
     table browser, use instructions outlined here:
 	 	https://groups.google.com/a/soe.ucsc.edu/forum/#!topic/genome/_6asF5KciPc
 	 Remember to join to tables to get the gencode transcript id (align id)
 2.  Use excel to convert tsv to csv
 3.  For GRCh37, you can get the ccds_id from the UCSC table browser, using a similar technique
     described above.  
 4.  Use excel to convert tsv to csv
 5.  Run sql script update_transcripts_for_canonical.sql



