#!/usr/bin/env bash

torrent=./test_5_premières_pieces.JPG.torrent
file=./test_5_premières_pieces.JPG

rm -rf aria2_seeder_2
mkdir aria2_seeder_2
cp $torrent $file aria2_seeder_2
sleep 2
# limitation possible de l'upload à 1Mo/s via -u 1M
aria2c --listen-port 2002 -V  -d aria2_seeder_2/ $torrent; $SHELL