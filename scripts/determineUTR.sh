#!/bin/bash
refs=( "chr1" "chr2" "chr3" "chr4" "chr5" "chr6" "chr7" "chr8" "chr9" "chr10" "chr11" "chr12" "chr13" "chr14" "chr15" "chr16" "chr17" "chr18" "chr19" "chr20" "chr21" "chr22" "chrX" "chrY" "chrMT"  "chrM")

echo "gencode GRCh37"
for i in "${refs[@]}"
do
   :
   # run determineUTR.js for each chromosome
   node determineUTR.js $i gencode GRCh37
done


echo "gencode GRCh38"
for i in "${refs[@]}"
do
   :
   # run determineUTR.js for each chromosome
   node determineUTR.js $i gencode GRCh38
done

echo "refseq GRCh37"
for i in "${refs[@]}"
do
   :
   # run determineUTR.js for each chromosome
   node determineUTR.js $i refseq GRCh37
done
node determineUTR.js other refseq GRCh37

echo "refseq GRCh38"
for i in "${refs[@]}"
do
   :
   # run determineUTR.js for each chromosome
   node determineUTR.js $i refseq GRCh38
done
node determineUTR.js other refseq GRCh38
