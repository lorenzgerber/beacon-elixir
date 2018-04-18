#!/bin/bash

#############
# EXECUTION #
#############
# 2 arguments required: dataset ID and VCF file
# > /.vcf_parser dataset_id filename

# To load the generated file into the DB:
# > cat filename.SNPs | psql -h host -p port -U username -c "copy beacon_data_table (dataset_id,start,chromosome,reference,alternate,\"end\","type",sv_length,variant_cnt,call_cnt,sample_cnt,frequency) from stdin using delimiters ';' csv" database_name

#########
# NOTES #
#########
# Definitions from VCF specification #
# INFO - additional information:
# - AN : total number of alleles in called genotypes
# - AC : allele count in genotypes, for each ALT allele, in the same order as listed
# - NS : Number of samples with data
# - AF : allele frequency for each ALT allele in the same order as listed: use this when estimated from primary data, not called genotypes

# These fields correspond to these Beacon fields:
# AN -> callCount
# AC -> variantCount
# NS -> sampleCount
# AF -> frequency




VCF_FILENAME=$(basename "$2")
VCF_FILENAME="${VCF_FILENAME%.*}"

echo 'Dataset Id: ' $1
echo 'Parsing file:' $VCF_FILENAME 

OUTPUT_FILENAME=$1'_'$VCF_FILENAME'.SNPs'
OUTPUT_SAMPLES_FILENAME=$1'_'$VCF_FILENAME'.samples'


########################################
# 				Parsing VCF 		   #
########################################
echo "Parsing meta-information..."

while IFS='' read -r line; 
do
	#Parse some fields in the header section to know if there is one value per alternate allele (Number=A)
	if [[ $line == \#\#INFO*=AC*Number=A* ]]
	then
		HEADER_AC=1;
	fi
	if [[ $line == \#\#INFO*=AN*Number=A* ]]
	then
		HEADER_AN=1;
	fi
	if [[ $line == \#\#INFO*=AF*Number=A* ]]
	then
		HEADER_AF=1;
	fi
	if [[ $line == \#\#INFO*=NS*Number=A* ]]
	then
		HEADER_NS=1;
	fi
	if [[ $line == \#\#INFO*=END*Number=A* ]]
	then
		HEADER_END=1;
	fi
	if [[ $line == \#\#INFO*=SVLEN*Number=A* ]]
	then
		HEADER_SVLEN=1;
	fi
	if [[ $line == \#\#INFO*=SVTYPE*Number=A* ]]
	then
		HEADER_SVTYPE=1;
	fi
	if [[ $line == \#\#INFO*=TYPE*Number=A* ]]
	then
		HEADER_TYPE=1;
	fi
	if [[ $line == \#CHROM* ]]
	then
		#Parse samples in the #CHROM line
		echo $line | cut -d$' ' -f10- | awk -F "" -v ds=$1 '
		{
			numFields=split($0,fields," ");
			for(i=1;i<=numFields;i++) { 
				print ds";"fields[i]
			}
		}' > $OUTPUT_SAMPLES_FILENAME;
	fi
	if [[ $line != \#* ]] 
	then 
		break;
	fi
done < "$2"
echo "Output samples file: $OUTPUT_SAMPLES_FILENAME"
#NOTE: Extracting this data using a while loop is much faster than using grep (to filter rows beginning with #) and awk
########################################
########################################

echo "Parsing VCF..."
#Column number in VCF file (#parameter index in awk function): 1=crhom (#1), 2=pos (#2), 4=ref (#3), 5=alt (#4), 7=filter (#5), 8=info(#6)
#This awk script also parses some keys inside the INFO field. Some of these keys may contain more than one value 
#separated with commas if there is more than one alternate allele. In order to know this, the meta-information has been already scanned.
grep -v "^#" $2 | cut -f1,2,4,5,7,8 | awk -v ds=$1 -v header_ac=$HEADER_AC -v header_an=$HEADER_AN -v header_ns=$HEADER_NS -v header_af=$HEADER_AF \
	-v header_end=$HEADER_END -v header_svlen=$HEADER_SVLEN -v header_svtype=$HEADER_SVTYPE -v header_type=$HEADER_TYPE '
{
	if ( $5 == "PASS" || $5 == "InDel" || $5 == "." ) {
		numAltField=split($4,altField,",")
		numInfoField=split($6, infoField, ";")

		acTokens[0]=null
		anTokens[0]=null
		afTokens[0]=null
		nsTokens[0]=null
		variantTokens[0]=null
		callTokens[0]=null
		sampleTokens[0]=null
		frequencyTokens[0]=null
		fieldEndTokens[0]=null
		endTokens[0]=null
		svLenTokens[0]=null
		svLengthTokens[0]=null
		structuralVariantType[0]=null
		svTypeTokens[0]=null

		for(i = 1; i <= numInfoField; i++) {
			if(infoField[i] ~ /^[[:blank:]]*AC=/) {
				split(infoField[i], acTokens, "=")

				if(header_ac == 1) {
					split(acTokens[2], variantTokens, ",")	
				} else {
					variantTokens[0]=acTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*AN=/) {
				split(infoField[i], anTokens, "=")

				if(header_an == 1) {
					split(anTokens[2], callTokens, ",")	
				} else {
					callTokens[0]=anTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*NS=/) {
				split(infoField[i], nsTokens, "=")

				if(header_ns == 1) {
					split(nsTokens[2], sampleTokens, ",")
				} else {
					sampleTokens[0]=nsTokens[2]
				}
				continue
			}

			if(infoField[i] ~ /^[[:blank:]]*AF=/) {
				split(infoField[i], afTokens, "=")

				if(header_af == 1) {
					split(afTokens[2], afTokens, ",")
				} else {
					afTokens[0]=afTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*END=/) {
				split(infoField[i], fieldEndTokens, "=")

				if(header_end == 1) {
					split(fieldEndTokens[2], endTokens, ",")
				} else {
					endTokens[0]=fieldEndTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*SVLEN=/) {
				split(infoField[i], svLenTokens, "=")

				if(header_svlen == 1) {
					split(svLenTokens[2], svLengthTokens, ",")	
				} else {
					svLengthTokens[0]=svLenTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*INDEL/) {
				structuralVariantType[0] = "INDEL"
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*SVTYPE=/ || infoField[i] ~ /^[[:blank:]]*TYPE=/) {
				split(infoField[i], svTypeTokens, "=")
				
				if(infoField[i] ~ /SVTYPE=/ && header_svtype == 1 \
					|| infoField[i] ~ /TYPE=/ && header_type == 1) {
					structuralVariantType[0]=svTypeTokens[2]
				} else {
					split(svTypeTokens[2], structuralVariantType, ",")	
				}
				continue
			}
		}

		for( i = 1; i <= numAltField; i ++ ) {
			position=null
			alt=null
			end=null
			type=null
			svLen=null
			variantCount=null
			callCount=null
			sampleCount=null
			frequency=null

			if(header_ac == 1) {
				variantCount=variantTokens[i]
			} else {
				variantCount=variantTokens[0]
			}

			if(header_an == 1) {
				callCount=callTokens[i]
			} else {
				callCount=callTokens[0]
			}

			if(header_ns == 1) {
				sampleCount=sampleTokens[i]
			} else {
				sampleCount=sampleTokens[0]
			}

			if(header_af == 1) {
				frequency=afTokens[i]
			} else {
				frequency=afTokens[0]
			}

			if(header_end == 1) {
				end=endTokens[i]
			} else {
				end=endTokens[0]
			}

			if(header_svlen == 1) {
				svLen=svLengthTokens[i]
			} else {
				svLen=svLengthTokens[0]
			}

			if(header_svtype == 1 || header_type == 1) {
				type=toupper(structuralVariantType[i])
			} else {
				type=toupper(structuralVariantType[0])
			}

			if(altField[i] ~ /DEL/ || altField[i] ~ /DUP/ ) {
				alt="."
				if(altField[i] ~ /DEL/) {
					type = "DEL"
				} else if(altField[i] ~ /DUP/) {
					type = "DUP"
				}
			} else {
				alt=altField[i]
			}

			position = ($2-1)
			if(end != null) {
				end = end-1

			} 
			if(type == "DEL") {
				if(end == null) {
					if (svLen != null) {
						end = position + (svLen < 0 ? -svLen : svLen)
					} else {
						end = position + (length($3) - length(alt))
					}	
				} 
				if (svLen == null) {
					svLen = position - end
				}
			}
			if(type == null) {
				type = "SNP"
			}
			print ds";"position";"$1";"$3";"alt";"end";"type";"svLen";"variantCount";"callCount";"sampleCount";"frequency
		}
	}
}' > $OUTPUT_FILENAME
########################################

#Output format: dataset_id;position;chromosome;referenceBases;alternateBases;end;structuralVariantType (e.g. DUP, DEL, INDEL);svLength;variantCount;callCount;sampleCount;frequency
echo "Output file: $OUTPUT_FILENAME"

########################################
########################################