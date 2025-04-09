#!/usr/bin/env bash

torrent=./test_10_pieces.JPG.torrent
file=./test_10_pieces.JPG

rm -rf aria2_seeder_1
mkdir aria2_seeder_1
cp $torrent $file aria2_seeder_1
sleep 2
# limitation possible de l'upload à 1Mo/s via -u 1M
aria2c --listen-port 2001 -V  -d aria2_seeder_1/ $torrent; $SHELL
