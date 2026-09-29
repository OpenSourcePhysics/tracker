package test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.opensourcephysics.media.BrowserZipExport;
import org.opensourcephysics.media.core.ImageVideoRecorder;
import org.opensourcephysics.media.core.ImageVideoType;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;

/** Regression checks for packaged frames, including single-frame ZIP exports. */
public class ImageVideoZipTest {
    private static class Recorder extends ImageVideoRecorder {
        Recorder(String extension) {
            super(new VideoIO.ZipImageVideoType(new ImageVideoType(
                    new VideoFileFilter(extension, new String[] {extension}))));
        }

        void removeFirstFrame() {
            tempFiles.get(0).delete();
        }
    }

    public static void main(String[] args) throws Exception {
        File directory = Files.createTempDirectory("tracker-zip-test-").toFile();
        for (int count : new int[] {1, 3, 12}) {
            checkArchive(directory, "jpg", count);
        }
        checkArchive(directory, "png", 3);
        File failed = new File(directory, "missing.zip");
        Recorder recorder = new Recorder("jpg");
        recorder.createVideo(failed.getAbsolutePath());
        recorder.addFrame(frame(0));
        recorder.removeFirstFrame();
        try {
            BrowserZipExport.saveVideo(recorder);
            throw new AssertionError("Missing frame was not reported");
        } catch (IOException expected) {
            if (failed.exists()) throw new AssertionError("Failed export produced a download");
        } finally {
            recorder.reset();
        }
        System.out.println("PASS: JPEG/PNG entries, single and multiple frames, ordering, dimensions, no loose images, missing-frame failure");
    }

    private static BufferedImage frame(int index) {
        BufferedImage image = new BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(index % 2 == 0 ? Color.RED : Color.BLUE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.dispose();
        return image;
    }

    private static void checkArchive(File root, String extension, int count) throws Exception {
        File directory = new File(root, extension + count);
        directory.mkdir();
        File target = new File(directory, "clip.zip");
        Recorder recorder = new Recorder(extension);
        recorder.createVideo(target.getAbsolutePath());
        for (int i = 0; i < count; i++) recorder.addFrame(frame(i));
        String saved = BrowserZipExport.saveVideo(recorder);
        if (!target.getAbsolutePath().equals(saved)) throw new AssertionError("Wrong saved path: " + saved);
        if (directory.list().length != 1) throw new AssertionError("Loose files beside archive");
        try (ZipFile archive = new ZipFile(target)) {
            if (archive.size() != count) throw new AssertionError("Wrong frame count");
            String[] names = ImageVideoRecorder.getFileNames("clip." + extension, count, extension);
            Set<String> seen = new HashSet<>();
            for (String name : names) {
                ZipEntry entry = archive.getEntry(name);
                if (entry == null || !seen.add(name)) throw new AssertionError("Missing/duplicate frame " + name);
                BufferedImage image = ImageIO.read(archive.getInputStream(entry));
                if (image == null || image.getWidth() != 32 || image.getHeight() != 24)
                    throw new AssertionError("Invalid frame " + name);
            }
        } finally {
            recorder.reset();
        }
    }
}
