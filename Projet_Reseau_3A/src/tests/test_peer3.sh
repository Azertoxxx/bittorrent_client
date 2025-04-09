#!/usr/bin/env bash

torrent=./test_5_dernières_pieces.JPG.torrent
file=./test_5_dernières_pieces.JPG

rm -rf aria2_seeder_3
mkdir aria2_seeder_3
cp $torrent $file aria2_seeder_3
sleep 2
# limitation possible de l'upload à 1Mo/s via -u 1M
aria2c --listen-port 2003 -V  -d aria2_seeder_3/ $torrent; $SHELL