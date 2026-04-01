package com.apa.clipfarmer.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FileUtils}.
 *
 * @author alexpages
 */
class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void deleteDirectory_existingDirWithFiles_deletesCompletely() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(subDir.resolve("file1.txt"), "content");
        Files.writeString(tempDir.resolve("file2.txt"), "content");

        FileUtils.deleteDirectory(tempDir);

        assertThat(tempDir).doesNotExist();
    }

    @Test
    void deleteDirectory_emptyDirectory_deletesIt() throws IOException {
        Path emptyDir = Files.createDirectory(tempDir.resolve("empty"));

        FileUtils.deleteDirectory(emptyDir);

        assertThat(emptyDir).doesNotExist();
    }

    @Test
    void deleteDirectory_nonExistentPath_doesNotThrow() {
        Path nonExistent = tempDir.resolve("does-not-exist");

        // Should silently do nothing
        FileUtils.deleteDirectory(nonExistent);

        assertThat(nonExistent).doesNotExist();
    }

    @Test
    void deleteDirectory_nestedDirectories_deletesAllLevels() throws IOException {
        Path level1 = Files.createDirectory(tempDir.resolve("l1"));
        Path level2 = Files.createDirectory(level1.resolve("l2"));
        Files.writeString(level2.resolve("deep.txt"), "deep content");

        FileUtils.deleteDirectory(tempDir);

        assertThat(tempDir).doesNotExist();
    }
}
