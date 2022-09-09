#!/usr/bin/env python
# coding: utf-8

# In[55]:


import sqlite3
import pandas as pd
import json


# In[56]:



con = sqlite3.connect("gene.iobio.db")


# # Create new genes.json based on gene.iobio.db genes

# In[57]:


chromosomes = "('chr1', 'chr2','chr3','chr4','chr5','chr6','chr7','chr8','chr9','chr10','chr11','chr12','chr13','chr14','chr15','chr16','chr17','chr18','chr19','chr20','chr21','chr22','chrX','chrY', 'chrM')"


# In[58]:


def run_query(stmt):
    cur = con.cursor()
    res = cur.execute(stmt)
    results = res.fetchall()
    return results


# In[59]:


stmt = "SELECT distinct gene_name, source, build, length(transcripts), chr from genes  order by gene_name, build, source"
results = run_query(stmt)
len(results)


# In[60]:


gene_map = {}
error_gene_map = {}


# In[61]:


def load_error_gene(gene_name, error):
    if gene_name in error_gene_map:
        error_obj = error_gene_map[gene_name]
    else:
        error_obj = {}
        error_gene_map[gene_name] = error_obj
    
    error_obj[error] = True


# In[62]:


gene_map = {}
error_gene_map = {}

for row in results:
    gene_name = row[0]
    source = row[1]
    build = row[2]
    transcript_len = row[3]
    chrom = row[4]
    

    if gene_name in gene_map:
        gene = gene_map[gene_name] 
    else:
        gene = { 'gencode': False, 'refseq': False}
        gene['chr'] = chrom        

    if source == 'gencode':
        gene['gencode'] = True
    elif source == 'refseq':
        gene['refseq'] = True

    if chrom == 'null':
        load_error_gene(gene_name, 'chr_null')
    else:
        if chrom not in chromosomes:
            load_error_gene(gene_name, 'non_std_chr') 
        if gene['chr'] != chrom:
            load_error_gene(gene_name, 'multi_chr') 
        if transcript_len == '0':
            load_error_gene(gene_name, 'no_transcript') 


    gene_map[gene_name] = gene


# In[64]:


stmt = "SELECT source, build, count(*) from genes  where chr = 'null' group by source, build"
results = run_query(stmt)
results


# In[72]:


gene_list = []
for gene_name in gene_map.keys():
    gene_obj = gene_map[gene_name]
    the_gene_obj = {'gene_name': gene_name, 'gencode': gene_obj['gencode'], 'refseq': gene_obj['refseq']}
    gene_list.append(the_gene_obj)

with open('genes.json', 'w') as fp:
    json.dump(gene_list, fp)    


# In[75]:


error_gene_list = []
for gene_name in error_gene_map.keys():
    error_obj = error_gene_map[gene_name]
    error_gene_list.append(error_obj)

with open('genes_invalid.json', 'w') as fp:
    json.dump(error_gene_list, fp)   


# In[ ]:




