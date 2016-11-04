These are the steps involved in creating the gene.iobio.db
 
1.  Create the schema for gene.iobio.db by running sql/schema.ddl in sqlite3.  
	.read schema.ddl

2.  Populate the gene.iobio.db from the gff3 files for each genome build
    -  You can get the gff3 files from Gencode and Ensembl (for refseq).
    -  Modify src/geneModelGffImporter to point to the correct gff3 file and specify the correct build
    -  Compile the java app.  The easiest way is to run in eclipse as a Java project.  
       - Import existing java project into Eclipse (.project file)
       - Select 'Run' menu and 'Run configurations'.  Run the 'compile' configuration under 'Maven build'.
       - The class files will be created in the target/classes directory.
    -  Run the java app.  Select 'Run' menu and 'Run configurations'.  Select 'run' under 'Java application'.
    -  Redo the same above steps for any other gff files (for other builds).


3. Update the gene.iobio.db transcripts table to specify the UTR features, which are not included in the GFF3, 
   but can be determined by reading the EXON and CDS features.
   -  This is a long running update statement, so it works better to update one reference (for a build) at a time.
   -  Run the bash shell script ./scripts/determineUTR.sh which will run the node.js script to perform the updates.
      When this script finished, both build GRCh37 and GRCh38 will be updated.  Update the .sh script if you
      want to run the updates on other builds.


4.  Update the canonical information on the gene.iobio.db transcripts.
    For build GRCh38, there is a is_canonical flag that can be used.  This flag can be found using
    the UCSC table browser.  For build GRCh37, there is a ccds_id that can be used to identifes the 
    common transcripts from The consensus coding sequence (CCDS) project. 
    How to get canonical and ccsd flags on transcripts
	-  For GRCh38, you can get the canonical designation for a transcript using the UCSC 
	    table browser, use instructions outlined here:
	 	 	https://groups.google.com/a/soe.ucsc.edu/forum/#!topic/genome/_6asF5KciPc
	 	Remember to join to tables to get the gencode transcript id (align id)
	-  Use excel to convert tsv to csv
	-  For GRCh37, you can get the ccds_id from the UCSC table browser, using a similar technique
	    described above.  
	-  Use excel to convert tsv to csv
	-  Run sql script sql/update_transcripts_for_canonical.sql which will update both the is_canonical for
	   GRCh38 transcripts and the ccds_id for GRCh37 transcripts

5.  Update the xref tables to map gencode transcripts to refseq transcripts
    -  For GRCh38, use UCSC table browser. (see doc/howto_ucsc_table_browser_xref_transcripts.png)
    -  For GRCh37, use Ensembl Biomart.  (see doc/howto_ensembl_biomart_xref_transcripts.png)
    -  Use excel to convert tsv to csv
    -  Now run sql script insert_transcripts_xref.sql in gene.iobio.db


 


