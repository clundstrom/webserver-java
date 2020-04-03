For intelliJ setup

1. Mark src folder as source
2. Pick an output-folder
3. Set project SDK to Java 11.0.1

-------------------------------------------------

How to run Server:
1. Local port is set in program Arguments.
2. Enter path as argument (relative path)

Example:
8080 static

3. Run Webservice.java
4. I added some extra files to the previously supplied webfolder. All can be found under static.

Custom files/folders:

* uploader.html - Form which handles uploading of PNGs for VG1
* corruptFile.txt - Used to simulate 500 error. You might need to deny read access to this file manually if you want to check the 500 error.
* protected.html - Used to check Error 302. Try (protected.html?pass=1dv701) grant access.
* favicon.ico - Supplied because I tested in a browser.
* uploads - Upload folder
* update.txt - Used to demonstrate PUT request. Curl PUT to update.txt







