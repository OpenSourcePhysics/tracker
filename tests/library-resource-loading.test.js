// Run with: node tests/library-resource-loading.test.js (requires a JDK).
// Compile and execute the production Java helper against an asynchronous OSP
// transport double, without requiring Eclipse or the SwingJS build output.
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const root = path.resolve(__dirname, '..');
const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'tracker-library-'));
const sources = {
  'org/opensourcephysics/controls/XML.java': `
package org.opensourcephysics.controls;
public class XML {
  public static String getName(String path) {return path.substring(path.lastIndexOf('/') + 1);}
}`,
  'org/opensourcephysics/display/OSPRuntime.java': `
package org.opensourcephysics.display;
import java.io.File;
import java.util.*;
public class OSPRuntime {
  public static final Map<String, byte[]> cache = new HashMap<>();
  public static final Map<File, byte[]> files = new HashMap<>();
  public static final JSUtil jsutil = new JSUtil();
  public static byte[] getCachedBytes(String path) {return cache.get(path);}
  public static void cacheJSFile(File file, boolean add) {
    if (!files.containsKey(file)) throw new AssertionError("File has no bytes");
    cache.put(file.getAbsolutePath(), files.get(file));
  }
  public static class JSUtil {
    public boolean setFileBytes(File file, Object bytes) {files.put(file, (byte[]) bytes); return true;}
  }
}`,
  'org/opensourcephysics/tools/ResourceLoader.java': `
package org.opensourcephysics.tools;
import java.io.File;
import java.net.URL;
import java.util.function.Function;
public class ResourceLoader {
  public static Function<byte[], Void> pending;
  public static URL requested;
  public static int requests;
  public static File getOSPCacheFile(String url, String name) {
    return new File("/TEMP/osp-" + url.substring(url.indexOf("://") + 3).replace('/', '_'), name);
  }
  public static String getNonURIPath(String path) {return path;}
  public static String getURIPath(String path) {
    return path.startsWith("/") ? new File(path).toURI().toString() : path;
  }
  public static void getURLContentsAsync(URL url, Function<byte[], Void> done) {
    if (pending != null) throw new AssertionError("Unexpected duplicate request");
    requests++; requested = url; pending = done;
  }
  public static void complete(byte[] bytes) {
    Function<byte[], Void> done = pending; pending = null; done.apply(bytes);
  }
}`,
  'org/opensourcephysics/cabrillo/tracker/LibraryResourceLoaderTest.java': `
package org.opensourcephysics.cabrillo.tracker;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;
public class LibraryResourceLoaderTest {
  static final String TARGET = "https://opensourcephysics.github.io/resources/CAB/experiments/BallToss.zip";
  static final List<String> results = new ArrayList<>();
  static final byte[] ZIP = {80, 75, 3, 4, 0, -1, -128, 127};
  static Function<String, Void> done = uri -> {
    if (uri != null) {
      String local = new File(java.net.URI.create(uri)).getAbsolutePath();
      check(Arrays.equals(OSPRuntime.getCachedBytes(local), ZIP), "Continuation ran before binary cache was ready");
    }
    results.add(uri); return null;
  };
  static void check(boolean ok, String message) {if (!ok) throw new AssertionError(message);}
  static void reset() {results.clear(); OSPRuntime.cache.clear(); OSPRuntime.files.clear();}
  public static void main(String[] args) {
    LibraryResourceLoader.load(TARGET, null, done);
    check(results.isEmpty(), "Must wait for asynchronous download");
    check(ResourceLoader.requested.toString().equals(TARGET), "Wrong source URL");
    ResourceLoader.complete(ZIP);
    check(results.size() == 1 && results.get(0).endsWith("BallToss.zip"), "Missing local ZIP URI");
    int count = ResourceLoader.requests;
    results.clear(); LibraryResourceLoader.load(TARGET, null, done);
    check(results.size() == 1 && ResourceLoader.requests == count, "Cache hit downloaded again");
    reset();
    String compadre = "https://www.compadre.org/osp/document/ServeFile.cfm?ID=123";
    LibraryResourceLoader.load(compadre, "Ball Toss.trz", done);
    check(results.isEmpty(), "comPADRE did not wait");
    ResourceLoader.complete(ZIP);
    check(results.size() == 1 && results.get(0).endsWith("Ball%20Toss.trz"), "Lost supplied filename or spaces");
    reset(); LibraryResourceLoader.load(TARGET, null, done); ResourceLoader.complete(null);
    check(results.size() == 1 && results.get(0) == null && OSPRuntime.cache.isEmpty(), "Failed request cached or did not finish");
    reset(); LibraryResourceLoader.load(TARGET, null, done); ResourceLoader.complete(new byte[0]);
    check(results.size() == 1 && results.get(0) == null && OSPRuntime.cache.isEmpty(), "Empty response cached or did not finish");
    reset(); LibraryResourceLoader.load("bad://invalid", "bad.trz", done);
    check(results.size() == 1 && results.get(0) == null && ResourceLoader.pending == null, "Bad URL did not finish");
    reset(); LibraryResourceLoader.load(TARGET, null, done); ResourceLoader.complete(ZIP);
    check(results.size() == 1 && results.get(0) != null, "Retry after failure did not succeed");
    System.out.println("PASS: asynchronous handoff, binary bytes, local cache, comPADRE filename, spaces, failures, retry");
  }
}`
};
try {
  for (const [name, source] of Object.entries(sources)) {
    const dest = path.join(temp, name);
    fs.mkdirSync(path.dirname(dest), {recursive: true});
    fs.writeFileSync(dest, source);
  }
  execFileSync('java', ['com.sun.tools.javac.Main', '-d', temp,
    path.join(root, 'src/org/opensourcephysics/cabrillo/tracker/LibraryResourceLoader.java'),
    ...Object.keys(sources).map(name => path.join(temp, name))], {stdio: 'inherit'});
  execFileSync('java', ['-cp', temp, 'org.opensourcephysics.cabrillo.tracker.LibraryResourceLoaderTest'], {stdio: 'inherit'});
} finally {
  fs.rmSync(temp, {recursive: true, force: true});
}
