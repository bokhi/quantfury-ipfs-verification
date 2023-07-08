#!/bin/bash

data_dir="data"
mkdir -p $data_dir

while read -r line; do
    cid=$(echo $line | cut -d ' ' -f 1)
    password=$(echo $line | cut -d ' ' -f 2)

    echo "Processing CID $cid with password $password"

    if [ ! -f "$data_dir/$cid.zip" ]; then
        curl -s https://ipfs.quantfury.com/ipfs/$cid -o $data_dir/$cid.zip
    fi

    7z x -p"$password" -y -o$data_dir $data_dir/$cid.zip
    mv $data_dir/*.csv $data_dir/data.csv
    dos2unix $data_dir/data.csv

    echo "Number of trades:"
    tail -n +2 $data_dir/data.csv | wc -l

    echo "Sum of spreads:"
    tail -n +2 $data_dir/data.csv | awk -F, '{print $8}' | paste -sd+ - | bc -l

    rm $data_dir/data.csv
done < cids_passwords.txt
