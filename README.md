# Bittorent Client

## Test the projetct
* Start Opentracker : ./opentracker.debug -i 127.0.0.1 - p 6969 

* Navigate to Projet_Reseau_3A : cd Projet_Reseau_3A

* To execute the Mono Client leecher : 
    * execute aria2c as seeder : aria2c --listen-port 2001 -V -d <PATH/file> <file.torrent> 
    * ava -jar Leecher_Mono.jar <file.torrent>

* To execute the seeder : 
    * java -jar Seeder.jar <file> <file.torrent>
    * download the file with a bittorent client, Vuze for exemple.