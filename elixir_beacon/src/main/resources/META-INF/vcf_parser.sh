#!/bin/bash

#############
# EXECUTION #
#############
# 2 arguments required: dataset ID and VCF file
# > /.vcf_parser dataset_id filename

###################
# LAODING INTO DB #
###################
# To load the generated file into the DB:
# > cat filename.SNPs | psql -h host -p port -U username -c \
# "copy beacon_data_table (dataset_id,chromosome,start,variantId,reference,alternate,\"end\","type",sv_length,variant_cnt,call_cnt,sample_cnt,frequency) from stdin using delimiters ';' csv" database_name

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

OUTPUT_FILENAME=$1'_'$VCF_FILENAME'.variants.csv'
OUTPUT_SAMPLES_FILENAME=$1'_'$VCF_FILENAME'.samples.csv'


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
#Column number in VCF file (#parameter index in awk function): 1=crhom (#1), 2=pos (#2), 3=ID (#3), 4=ref (#4), 5=alt (#5), 7=filter (#6), 8=info(#7)
#This awk script also parses some keys inside the INFO field. Some of these keys may contain more than one value
#separated with commas if there is more than one alternate allele. In order to know this, the meta-information has been already scanned.
grep -v "^#" $2 | cut -f1,2,3,4,5,7,8 | awk -v ds=$1 -v header_ac=$HEADER_AC -v header_an=$HEADER_AN -v header_ns=$HEADER_NS -v header_af=$HEADER_AF \
	-v header_end=$HEADER_END -v header_svlen=$HEADER_SVLEN -v header_svtype=$HEADER_SVTYPE -v header_type=$HEADER_TYPE '
{
	if ( $6 == "PASS" || $6 == "InDel" || $6 == "." ) {
		numIdField=split($3,idField,";")
		numAltField=split($5,altField,",")
		numInfoField=split($7, infoField, ";")

		# Remember that the split() function returns the values starting with index 1
		acTokens[1]=null
		anTokens[1]=null
		afTokens[1]=null
		nsTokens[1]=null
		variantTokens[1]=null
		callTokens[1]=null
		sampleTokens[1]=null
		frequencyTokens[1]=null
		fieldEndTokens[1]=null
		endTokens[1]=null
		svLenTokens[1]=null
		svLengthTokens[1]=null
		structuralVariantTypeTokens[1]=null
		svTypeTokens[1]=null

		for(i = 1; i <= numInfoField; i++) {
			if(infoField[i] ~ /^[[:blank:]]*AC=/) {
				split(infoField[i], acTokens, "=")

				if(header_ac == 1) {
					split(acTokens[2], variantTokens, ",")
				} else {
					variantTokens[1]=acTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*AN=/) {
				split(infoField[i], anTokens, "=")

				if(header_an == 1) {
					split(anTokens[2], callTokens, ",")
				} else {
					callTokens[1]=anTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*NS=/) {
				split(infoField[i], nsTokens, "=")

				if(header_ns == 1) {
					split(nsTokens[2], sampleTokens, ",")
				} else {
					sampleTokens[1]=nsTokens[2]
				}
				continue
			}

			if(infoField[i] ~ /^[[:blank:]]*AF=/) {
				split(infoField[i], afTokens, "=")

				if(header_af == 1) {
					split(afTokens[2], afTokens, ",")
				} else {
					afTokens[1]=afTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*END=/) {
				split(infoField[i], fieldEndTokens, "=")

				if(header_end == 1) {
					split(fieldEndTokens[2], endTokens, ",")
				} else {
					endTokens[1]=fieldEndTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*SVLEN=/) {
				split(infoField[i], svLenTokens, "=")

				if(header_svlen == 1) {
					split(svLenTokens[2], svLengthTokens, ",")
				} else {
					svLengthTokens[1]=svLenTokens[2]
				}
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*INDEL/) {
				structuralVariantTypeTokens[1] = "INDEL"
				continue
			}
			if(infoField[i] ~ /^[[:blank:]]*SVTYPE=/ || infoField[i] ~ /^[[:blank:]]*TYPE=/) {
				split(infoField[i], svTypeTokens, "=")

				if(infoField[i] ~ /SVTYPE=/ && header_svtype == 1 \
					|| infoField[i] ~ /TYPE=/ && header_type == 1) {
					structuralVariantTypeTokens[1]=svTypeTokens[2]
				} else {
					split(svTypeTokens[2], structuralVariantTypeTokens, ",")
				}
				continue
			}
		}

		for( i = 1; i <= numAltField; i ++ ) {
			position=null
			variantId=null
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
				variantCount=variantTokens[1]
			}

			if(header_an == 1) {
				callCount=callTokens[i]
			} else {
				callCount=callTokens[1]
			}

			if(header_ns == 1) {
				sampleCount=sampleTokens[i]
			} else {
				sampleCount=sampleTokens[1]
			}

			if(header_af == 1) {
				frequency=afTokens[i]
			} else {
				frequency=afTokens[1]
			}

			if(header_end == 1) {
				end=endTokens[i]
			} else {
				end=endTokens[1]
			}

			if(header_svlen == 1) {
				svLen=svLengthTokens[i]
			} else {
				svLen=svLengthTokens[1]
			}

			if(header_svtype == 1 || header_type == 1) {
				type=toupper(structuralVariantTypeTokens[i])
			} else {
				type=toupper(structuralVariantTypeTokens[1])
			}
			# As VCF is quite flexible, sometimes we can find ALT=<INS:ME:ALU> and SVTYPE=ALU but we want to capture that it is an INS:ME
			if(altField[i] ~ /<INS:ME/) {
				type="INS:ME"
			} else if(altField[i] ~ /<DEL:ME/) {
				type="DEL:ME"
			} else if(altField[i] ~ /<DUP:TANDEM/) {
				type="DUP:TANDEM"
			}

			alt=altField[i]
			variantId=idField[i]

			position = ($2-1)
			if(end != null) {
				end = end-1

			}
			if(type == "DEL" || type == "DUP" || type == "CNV" || type == "INS:ME" || type == "INS:ME" || type == "INS:ME") {
				if(end == null) {
					# Calculate "end" value
					if (svLen != null) {
						end = position + (svLen < 0 ? -svLen : svLen)
					} else {
						end = position + (length($4) - length(alt))
					}
				}
				if (svLen == null) {
					# Calculate "svLength"
					svLen = position - end
				}
			}
			if(type == null) {
				type = "SNP"
			}
			print ds";"$1";"position";"variantId";"$4";"alt";"end";"type";"svLen";"variantCount";"callCount";"sampleCount";"frequency
		}
	}
}' > $OUTPUT_FILENAME
########################################

#Output format: dataset_id;chromosome;position;variantId;referenceBases;alternateBases;end;structuralVariantType (e.g. DUP, DEL, INDEL, CNV,...);svLength;variantCount;callCount;sampleCount;frequency
echo "Output file: $OUTPUT_FILENAME"

########################################
########################################
