package org.handler.service;

import java.nio.file.Path;
import java.util.List;

public interface ImportDataService {
    public void importFile(Path path) throws Exception;

    public void transformAndSaveJsonl(Path inputPath, Path outputPath) throws Exception;
}
