## Run Application

```shell
$ ./gradlew -Ddcimport.jar=/full/path/to/jar.jar run
```

## Call lint API

```shell
$ curl http://localhost:8080/lint -F file=@McfOnly.mcf -o results.zip
```

In the above `curl`, `McfOnly.mcf` is the MCF file on the client. This gets uploaded to the server which executes 
the `lint` operation and returns the generated files in a ZIP file. The above `curl` stores it in `results.zip`.

The contents of the zip file will look similar to the following.

```shell
$ unzip -l results.zip 
Archive:  results.zip
  Length      Date    Time    Name
---------  ---------- -----   ----
    21091  06-01-2023 20:51   report.json
    26358  06-01-2023 20:51   summary_report.html
---------                     -------
    47449                     2 files
```