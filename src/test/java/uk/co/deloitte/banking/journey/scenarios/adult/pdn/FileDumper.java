package uk.co.deloitte.banking.journey.scenarios.adult.pdn;

import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.PDNTestDataResultDataHolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

public class FileDumper {
    public static void dumpToFile(String filename, Collection<PDNTestDataResultDataHolder> data) throws IOException {
        Path resourceDirectory = Paths.get("src","test","resources");
        Path path = Paths.get(resourceDirectory.toAbsolutePath() + File.separator + filename);
        if (Files.exists(path)) {
            Files.deleteIfExists(path);
        }
        Files.createFile(path);

        for (PDNTestDataResultDataHolder item : data) {
            Files.write(path, item.toCSVFormat().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        }
    }
}
