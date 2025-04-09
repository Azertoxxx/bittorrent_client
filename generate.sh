#!/usr/bin/env bash


for i in 0 1 2 3 4 5 6 7 8 9; do
	dd if=/dev/zero bs=16383 count=1 | LANG=C tr "\000" $i > piece_$i;echo >> piece_$i 
done 
