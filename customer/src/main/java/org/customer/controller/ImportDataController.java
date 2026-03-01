package org.customer.controller;

import lombok.RequiredArgsConstructor;
import org.handler.service.ImportDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportDataController {
    private final ImportDataService importDataService;

    @PostMapping
    public String importFile(@RequestParam String filePath) throws Exception {
        importDataService.importFile(Path.of(filePath));

        return "Import completed.";
    }
}