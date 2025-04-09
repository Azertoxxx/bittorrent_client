#!/usr/bin/env bash

for i in 0 1 2 3 4 5 6 7 8 9; do
	cat piece_$i >> piece_1_to_10
	rm piece_$i
done
