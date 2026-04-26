package br.com.sistemacopias.dto;

public class ImportResult {
    private final int importedCount;
    private final int skippedCount;

    public ImportResult(int importedCount, int skippedCount) {
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }
}
