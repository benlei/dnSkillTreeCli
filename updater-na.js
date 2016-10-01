//======================================
// Patch Downloader
//======================================
// This file requires the following ENV vars to be set in order for it to work:
// - DN_VERSION_PATH - The path to your Version.cfg
// - DN_OUT_DIR - The directory where the patches will be downloaded to.
//======================================

// "import" necessary stuff
var JSystem = Java.type("java.lang.System");
var JFile = Java.type("java.io.File");
var JFiles = Java.type("java.nio.file.Files");
var JString = Java.type("java.lang.String");
var JFileOutputStream = Java.type("java.io.FileOutputStream");
var JURL = Java.type("java.net.URL");
var JArray = Java.type("byte[]");
var JStandardCopyOption = Java.type("java.nio.file.StandardCopyOption");

// Nashorn script uses to download paks from web
var VERSION_FILE = JSystem.getenv("DN_VERSION_PATH");
var OUTPUT_PATH = JSystem.getenv("DN_OUT_DIR");
var VERSION_URL = "http://download2.nexon.net/Game/DragonNest/patch/PatchInfoServer.cfg";
var DOWNLOAD_URL = "http://download2.nexon.net/Game/DragonNest/patch/%1$08d/Patch%1$08d.pak";

// helper functions
var write = function(path, contents) {
    var out = new JFileOutputStream(path);
    out.write(contents);
    out.close()
};

var getVersion = function(version) {
    var re = /(\d+)/g;
    var match = re.exec(version);
    return parseInt(match[1])
};


// get the current version
var version = getVersion(new JString(JFiles.readAllBytes(new JFile(VERSION_FILE).toPath())));

// get server version
var serverVersionURL = new JURL(VERSION_URL);
var input = serverVersionURL.openStream();
var bytes = new JArray(8192);
var serverVersion = "";
var read;
while ((read=input.read(bytes)) != -1) {
    serverVersion = serverVersion + (new JString(bytes, 0, read))
}
input.close();
serverVersion = getVersion(serverVersion);

if (version == serverVersion) {
    print("Client and server version is " + version);
    print("No need to update");
    exit(1)
} else if (version > serverVersion) {
    print(JString.format("ERROR: Client reports version %d, but Server reports version %d.", version, serverVersion));
    exit(1)
}

for (var i = version + 1; key <= serverVersion; key++) {
    var url = new JURL(JString.format(DOWNLOAD_URL, key.intValue()));
    var output = new JFile(OUTPUT_PATH, "Patch" + key + ".pak");
    input = url.openStream();
    JFiles.copy(input, output.toPath(), JStandardCopyOption.REPLACE_EXISTING);
    print("Downloaded " + url.toString() + " to " + output.getPath())
}

var output = new JFileOutputStream(VERSION_FILE);
output.write((new JString(serverVersion)).getBytes());
output.close();
print("Updated " + VERSION_FILE + " to " + serverVersion);
exit(0);
