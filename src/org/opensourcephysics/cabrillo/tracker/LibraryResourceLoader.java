package org.opensourcephysics.cabrillo.tracker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;

/** Prepares remote library projects for the synchronous Tracker/ZIP readers. */
final class LibraryResourceLoader {

	private LibraryResourceLoader() {
	}

	/**
	 * Downloads asynchronously into SwingJS's virtual file cache. The continuation
	 * receives a local URI only after all bytes are available, or null on failure.
	 * This is called only in JavaScript; desktop Java keeps its existing loader.
	 */
	static void load(String target, String fileName, Function<String, Void> whenDone) {
		if (fileName == null) {
			fileName = XML.getName(ResourceLoader.getNonURIPath(target));
		}
		File localFile = ResourceLoader.getOSPCacheFile(target, fileName);
		String localPath = localFile.getAbsolutePath();
		String localURI = ResourceLoader.getURIPath(localPath);
		byte[] cached = OSPRuntime.getCachedBytes(localPath);
		if (cached != null && cached.length > 0) {
			whenDone.apply(localURI);
			return;
		}
		URL url;
		try {
			url = new URL(ResourceLoader.getURIPath(target));
		} catch (MalformedURLException ex) {
			whenDone.apply(null);
			return;
		}
		ResourceLoader.getURLContentsAsync(url, (bytes) -> {
			if (bytes == null || bytes.length == 0) {
				whenDone.apply(null);
				return null;
			}
			OSPRuntime.jsutil.setFileBytes(localFile, bytes);
			OSPRuntime.cacheJSFile(localFile, true);
			whenDone.apply(localURI);
			return null;
		});
	}
}
