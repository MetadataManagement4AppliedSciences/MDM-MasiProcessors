# MDM-MasiProcessors

The MDM-MasiProcessors implements several staging processors for adding information
to the digital objects during ingest.

## How to build

In order to build the MDM-MasiProcessors you'll need:

* Java SE Development Kit 8 or higher
* Apache Maven 3

Change to the folder where the sources are located, e.g.: /home/user/MDM-MasiProcessors/. 
Afterwards, just call:

```
user@localhost:/home/user/metastore/$ bash buildMDM-MasiProcessors.sh
[...]
[INFO] Building zip: /home/user/MDM-MasiProcessors/zip/MDM-MasiProcessors-1.0-release.zip
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 27.257 s
[INFO] Finished at: 2017-12-12T16:11:32+01:00
[INFO] Final Memory: 67M/1052M
[INFO] ------------------------------------------------------------------------
user@localhost:/home/user/MDM-MasiProcessors/$
```

As soon as the assembly process has finished there will be a file named `MDM-MasiProcessors-1.0.zip` 
located at /home/user/MDM-MasiProcessors/zip, which is the distribution package of the client 
containing everything you need to install the tool. Extract the zip file to a directory of your
choice and refer to the contained manual for further instructions.

## More Information

* [Bugtracker](http://datamanager.kit.edu/bugtracker/thebuggenie/)

## License

The MDM-MasiProcessors is licensed under the Apache License, Version 2.0.


