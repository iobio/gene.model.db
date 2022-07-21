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
    the UCSC table browser.  
    How to get canonical and ccsd flags on transcripts:
	-  For GRCh38, you can get the canonical designation for a transcript using the UCSC 
	    table browser, use instructions outlined here:
	 	 	https://groups.google.com/a/soe.ucsc.edu/forum/#!topic/genome/_6asF5KciPc


```

In a new browser window navigate to the Table Browser: http://genome.ucsc.edu/cgi-bin/hgTables

1. Select hg38, and set "group:" "Genes and Gene Predictions" to track "GENCODE v22" (this should be the default selection).
2. Change table from "knownGene" to "knownCanonical". 
This step is a good opportunity to click the "describe table schema" button to see more about the table data you are requesting.


3. Change "output format" to "selected fields from primary and related tables".

This step allows you add information from other tables beyond the knownCanonical table.
4. Click "get output".
This screen is where we can add requests to get information from other tables, we are going to request information from the hg38 refGene table and the hg38 knownToEnsembl.
5. Scroll down to the "Linked Tables" section and click the box next to "hg38 refGene" and the box next to "hg38 knownToEnsembl".
6. Scroll to the very bottom and click the "allow selection from checked tables". 
Now we can select the fields we want from each of these three tables.
7. Under the "Select Fields from hg38.knownCanonical" click the box next to transcript.
This will be the transcript location driving all the related table output.
8. Under hg38.knownToEnsembl fields click "check all". 
9. Under hg38.refGene fields click "check all". 
10. Click "get output".

The results will be rows like the following: 

#hg38.knownCanonical.transcript    hg38.knownToEnsembl.name    hg38.knownToEnsembl.value    hg38.refGene.bin    hg38.refGene.name    hg38.refGene.chrom    hg38.refGene.strand    hg38.refGene.txStart    hg38.refGene.txEnd    hg38.refGene.cdsStart    hg38.refGene.cdsEnd    hg38.refGene.exonCount    hg38.refGene.exonStarts    hg38.refGene.exonEnds    hg38.refGene.score    hg38.refGene.name2    hg38.refGene.cdsStartStat    hg38.refGene.cdsEndStat    hg38.refGene.exonFrames

uc001ggs.5    uc001ggs.5    ENST00000367772.7    29    NM_181093    chr1    -    169853075    169893959    169853712    169888840    14    169853075,169854269,169855795,169859040,169862612,169864368,169866895,169868927,169870254,169873695,169875977,169878633,169888675,169893787,    169853772,169854964,169855957,169859212,169862797,169864508,169866973,169869039,169870357,169873752,169876091,169878819,169888890,169893959,    0    SCYL3    cmpl    cmpl    0,1,1,0,1,2,2,1,0,0,0,0,0,-1,
The first field (hg38.knownCanonical.transcript) is the id used from knownCanonical that is driving the selection of all the other data. The second two fields are the entire knownToEnsembl table that exists to provide the related ENST id (ENST00000367772.7), the remaining fields are all the fields from the refGene table that correspond to the entries in the knownCanonical table.

Please do make these selections independently. Here is a session to compare your steps against to help see the output: http://genome.ucsc.edu/cgi-bin/hgTables?hgS_doOtherUser=submit&hgS_otherUserName=Brian%20Lee&hgS_otherUserSessionName=hg38.refGene.canonical


In a new browser window navigate to the Table Browser: http://genome.ucsc.edu/cgi-bin/hgTables

1. Select hg38, and set "group:" "Genes and Gene Predictions" to track "GENCODE v22" (this should be the default selection).
2. Change table from "knownGene" to "knownCanonical". 
This step is a good opportunity to click the "describe table schema" button to see more about the table data you are requesting.


3. Change "output format" to "selected fields from primary and related tables".

This step allows you add information from other tables beyond the knownCanonical table.
4. Click "get output".
This screen is where we can add requests to get information from other tables, we are going to request information from the hg38 refGene table and the hg38 knownToEnsembl.
5. Scroll down to the "Linked Tables" section and click the box next to "hg38 refGene" and the box next to "hg38 knownToEnsembl".
6. Scroll to the very bottom and click the "allow selection from checked tables". 
Now we can select the fields we want from each of these three tables.
7. Under the "Select Fields from hg38.knownCanonical" click the box next to transcript.
This will be the transcript location driving all the related table output.
8. Under hg38.knownToEnsembl fields click "check all". 
9. Under hg38.refGene fields click "check all". 
10. Click "get output".

The results will be rows like the following: 

#hg38.knownCanonical.transcript    hg38.knownToEnsembl.name    hg38.knownToEnsembl.value    hg38.refGene.bin    hg38.refGene.name    hg38.refGene.chrom    hg38.refGene.strand    hg38.refGene.txStart    hg38.refGene.txEnd    hg38.refGene.cdsStart    hg38.refGene.cdsEnd    hg38.refGene.exonCount    hg38.refGene.exonStarts    hg38.refGene.exonEnds    hg38.refGene.score    hg38.refGene.name2    hg38.refGene.cdsStartStat    hg38.refGene.cdsEndStat    hg38.refGene.exonFrames

uc001ggs.5    uc001ggs.5    ENST00000367772.7    29    NM_181093    chr1    -    169853075    169893959    169853712    169888840    14    169853075,169854269,169855795,169859040,169862612,169864368,169866895,169868927,169870254,169873695,169875977,169878633,169888675,169893787,    169853772,169854964,169855957,169859212,169862797,169864508,169866973,169869039,169870357,169873752,169876091,169878819,169888890,169893959,    0    SCYL3    cmpl    cmpl    0,1,1,0,1,2,2,1,0,0,0,0,0,-1,
The first field (hg38.knownCanonical.transcript) is the id used from knownCanonical that is driving the selection of all the other data. The second two fields are the entire knownToEnsembl table that exists to provide the related ENST id (ENST00000367772.7), the remaining fields are all the fields from the refGene table that correspond to the entries in the knownCanonical table.

Please do make these selections independently. Here is a session to compare your steps against to help see the output: http://genome.ucsc.edu/cgi-bin/hgTables?hgS_doOtherUser=submit&hgS_otherUserName=Brian%20Lee&hgS_otherUserSessionName=hg38.refGene.canonical
```

	 	Remember to join to tables to get the gencode transcript id (align id)
	-  Use excel to convert tsv to csv
	-  For GRCh37, you can get the ccds_id from the UCSC table browser, using a similar technique described above. 
	<p align="center">
		  <img src="doc/howto_ccds_GRCh37_ucsc_browser.png" width="350"/>
	</p>
	 
	-  Use excel to convert tsv to csv
	-  Run sql script sql/update_transcripts_for_canonical.sql which will update both the is_canonical for GRCh38 transcripts and the ccds_id for GRCh37 transcripts


5.  Update the xref tables to map gencode transcripts to refseq transcripts
    -  For GRCh38, use UCSC table browser. 
		<p align="center">
		  <img src="doc/howto_ucsc_table_browser_xref_transcripts.png" width="350"/>
		</p>    

	 
    -  For GRCh37, use Ensembl Biomart.  
		<p align="center">
		  <img src="doc/howto_ensembl_biomart_xref_transcripts.png" width="700"/>
		</p>    
    -  Use excel to convert tsv to csv
    -  Now run sql script insert_transcripts_xref.sql in gene.iobio.db


 


