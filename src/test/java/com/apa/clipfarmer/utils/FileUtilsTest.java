package com.apa.clipfarmer.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

    @TempDir
    private Path tempDir;

    @Test
    void deleteDirectoryRemovesNestedFilesAndTheDirectoryItself() throws IOException {
        Path nestedDir = tempDir.resolve("nested");
        Files.createDirectories(nestedDir);
        Files.writeString(nestedDir.resolve("file.txt"), "content");
        Files.writeString(tempDir.resolve("root-file.txt"), "content");

        FileUtils.deleteDirectory(tempDir);

        assertThat(Files.exists(tempDir)).isFalse();
    }

    @Test
    void deleteDirectoryOnNonExistentPathDoesNotThrow() {
        Path missing = tempDir.resolve("does-not-exist");

        assertThatCode(() -> FileUtils.deleteDirectory(missing)).doesNotThrowAnyException();
    }
}
