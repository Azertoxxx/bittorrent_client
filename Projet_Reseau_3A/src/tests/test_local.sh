#!/usr/bin/env bash

torrent=./piece_1_to_10.torrent
file=./piece_1_to_10

#lancer plusieurs seeders en parallèle (ici 3)
        j=0
for i in peer1 peer2 peer3;do
        sleep 2
        # limitation possible de l'upload à 1Mo/s via -u 1M
        $((j++))
        xterm -e "aria2c --listen-port 200$j -V  -d $i/ $torrent; $SHELL" &
        # echo $((j++))
done