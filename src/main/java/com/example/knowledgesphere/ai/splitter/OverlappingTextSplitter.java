package com.example.knowledgesphere.ai.splitter;

import com.example.knowledgesphere.ai.model.BlockType;
import com.example.knowledgesphere.ai.model.DocumentBlock;
import com.example.knowledgesphere.ai.model.StructuredPage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.BreakIterator;
import java.util.*;

@Component
public class OverlappingTextSplitter {

    private static final int MAX_TOKENS = 220;
    private static final int OVERLAP_TOKENS = 40;
    private static final int MIN_TOKENS = 5;

    public List<Document> split(List<StructuredPage> pages) {

        List<Document> chunks = new ArrayList<>();

        ChunkContext context = new ChunkContext();

        int chunkIndex = 1;

        for (StructuredPage page : pages) {

            context.reset(page);

            chunkIndex = processPage(
                    page,
                    chunkIndex,
                    chunks,
                    context
            );
        }

        flushChunk(
                context,
                pages.isEmpty() ? null : pages.get(pages.size() - 1),
                chunkIndex,
                chunks
        );

        return removeDuplicates(chunks);
    }

    private int processPage(
            StructuredPage page,
            int chunkIndex,
            List<Document> chunks,
            ChunkContext context
    ) {

        int paragraph = 1;

        for (DocumentBlock block : page.getBlocks()) {

            if (block == null)
                continue;

            BlockType type = block.getType();

            switch (type) {

                case HEADING -> {

                    flushChunk(
                            context,
                            page,
                            chunkIndex,
                            chunks
                    );

                    chunkIndex++;

                    String headingText = block.getMarkdown() != null && !block.getMarkdown().isBlank() ? block.getMarkdown() : block.getText();
                    context.currentHeading =
                            normalize(headingText);

                }

                case PARAGRAPH -> {
                    chunkIndex = processParagraph(
                            block.getText(),
                            page,
                            paragraph++,
                            chunkIndex,
                            context,
                            chunks
                    );
                }

                case LIST -> chunkIndex =
                        processParagraph(
                                block.getMarkdown() != null && !block.getMarkdown().isBlank() ? block.getMarkdown() : block.getText(),
                                page,
                                paragraph++,
                                chunkIndex,
                                context,
                                chunks
                        );

                case TABLE -> chunkIndex =
                        processTable(
                                block,
                                page,
                                paragraph++,
                                chunkIndex,
                                context,
                                chunks
                        );

                case IMAGE -> chunkIndex =
                        processImage(
                                block,
                                page,
                                paragraph++,
                                chunkIndex,
                                context,
                                chunks
                        );

                default -> chunkIndex =
                        processParagraph(
                                block.getMarkdown() != null && !block.getMarkdown().isBlank()
                                        ? block.getMarkdown()
                                        : block.getText(),
                                page,
                                paragraph++,
                                chunkIndex,
                                context,
                                chunks
                        );
            }
        }

        flushChunk(
                context,
                page,
                chunkIndex,
                chunks
        );

        return chunkIndex + 1;
    }

    /*
     * ============================================
     * Paragraph Processing
     * ============================================
     */

    private int processParagraph(
            String text,
            StructuredPage page,
            int paragraph,
            int chunkIndex,
            ChunkContext context,
            List<Document> chunks
    ) {

        text = normalize(text);

        if (text.isBlank())
            return chunkIndex;

        List<String> sections =
                splitIntoSections(text);

        for (String section : sections) {

            int tokens =
                    estimateTokens(section);

            if (tokens > MAX_TOKENS) {

                chunkIndex =
                        splitLargeText(
                                section,
                                page,
                                paragraph,
                                chunkIndex,
                                context,
                                chunks,
                                "PARAGRAPH"
                        );

                continue;
            }

            if (context.currentTokens + tokens > MAX_TOKENS) {

                flushChunk(
                        context,
                        page,
                        chunkIndex,
                        chunks
                );

                chunkIndex++;

                preserveOverlap(context);
            }

            context.currentParagraph = paragraph;

            context.builder
                    .append(section)
                    .append("\n\n");

            context.currentTokens += tokens;
        }

        return chunkIndex;
    }

    /*
     * ============================================
     * Table Processing
     * ============================================
     */

    private int processTable(
            DocumentBlock block,
            StructuredPage page,
            int paragraph,
            int chunkIndex,
            ChunkContext context,
            List<Document> chunks
    ) {

        flushChunk(context, page, chunkIndex, chunks);
        chunkIndex++;

        String tableText = getTableText(block);
        if (tableText == null || tableText.isBlank()) {
            return chunkIndex;
        }

        List<String> rows = extractRows(tableText);
        if (rows.isEmpty()) {
            return chunkIndex;
        }

        String header = "";
        String separator = "";
        int dataStartIndex = 0;

        if (rows.size() >= 2 && rows.get(1).matches("^[|\\s:-]+$")) {
            header = rows.get(0).trim();
            separator = rows.get(1).trim();
            dataStartIndex = 2;
        } else {
            header = rows.get(0).trim();
            dataStartIndex = 1;
        }

        StringBuilder currentChunk = new StringBuilder();
        if (context.currentHeading != null && !context.currentHeading.isBlank()) {
            currentChunk.append("Section: ").append(context.currentHeading).append("\n\n");
        }
        
        String headerBlock = header + "\n" + (separator.isEmpty() ? "" : separator + "\n");
        currentChunk.append(headerBlock);
        
        int currentTokens = estimateTokens(currentChunk.toString());
        boolean hasData = false;

        for (int i = dataStartIndex; i < rows.size(); i++) {
            String row = rows.get(i).trim();
            if (row.isBlank()) continue;
            
            int rowTokens = estimateTokens(row + "\n");
            
            if (currentTokens + rowTokens > MAX_TOKENS && hasData) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("blockType", "TABLE");
                chunks.add(createChunk(currentChunk.toString(), page, paragraph, chunkIndex++, context, metadata));
                
                currentChunk.setLength(0);
                if (context.currentHeading != null && !context.currentHeading.isBlank()) {
                    currentChunk.append("Section: ").append(context.currentHeading).append("\n\n");
                }
                currentChunk.append(headerBlock);
                currentTokens = estimateTokens(currentChunk.toString());
                hasData = false;
            }
            
            currentChunk.append(row).append("\n");
            currentTokens += rowTokens;
            hasData = true;
        }

        if (hasData || rows.size() <= dataStartIndex) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("blockType", "TABLE");
            chunks.add(createChunk(currentChunk.toString(), page, paragraph, chunkIndex++, context, metadata));
        }

        return chunkIndex;
    }

    /*
     * ============================================
     * Image Processing
     * ============================================
     */

    private int processImage(
            DocumentBlock block,
            StructuredPage page,
            int paragraph,
            int chunkIndex,
            ChunkContext context,
            List<Document> chunks
    ) {

        flushChunk(
                context,
                page,
                chunkIndex,
                chunks
        );

        chunkIndex++;

        String captionText = block.getMarkdown() != null && !block.getMarkdown().isBlank() ? block.getMarkdown() : block.getText();
        String caption =
                normalize(captionText);

        if (caption.isBlank())
            return chunkIndex;

        Map<String, Object> metadata =
                new HashMap<>();

        metadata.put("blockType", "IMAGE");

        chunks.add(
                createChunk(
                        caption,
                        page,
                        paragraph,
                        chunkIndex,
                        context,
                        metadata
                )
        );

        return chunkIndex + 1;
    }

    /*
     * ============================================
     * Flush Current Chunk
     * ============================================
     */

    private void flushChunk(
            ChunkContext context,
            StructuredPage page,
            int chunkIndex,
            List<Document> chunks
    ) {

        String text = normalize(
                context.builder.toString()
        );

        if (text.isBlank()) {

            context.builder.setLength(0);
            context.currentTokens = 0;
            return;
        }

        if (context.currentTokens < MIN_TOKENS) {

            context.builder.setLength(0);
            context.currentTokens = 0;
            return;
        }

        Map<String, Object> metadata =
                new HashMap<>();

        metadata.put("blockType", "TEXT");

        chunks.add(

                createChunk(
                        text,
                        page,
                        context.currentParagraph,
                        chunkIndex,
                        context,
                        metadata
                )

        );

        context.builder.setLength(0);
        context.currentTokens = 0;
    }

    /*
     * ============================================
     * Create Spring AI Document
     * ============================================
     */

    private Document createChunk(

            String text,

            StructuredPage page,

            int paragraph,

            int chunk,

            ChunkContext context,

            Map<String, Object> extraMetadata

    ) {

        Map<String, Object> metadata =
                new HashMap<>();

        if (page != null && page.getMetadata() != null) {
            metadata.putAll(page.getMetadata());
        }

        metadata.put("page",
                page.getPageNumber());

        metadata.put("paragraph",
                paragraph);

        metadata.put("chunk",
                chunk);

        metadata.put("heading",
                context.currentHeading);

        metadata.put("ocr",
                page.isOcr());

        metadata.put(
                "tokens",
                estimateTokens(text)
        );

        metadata.put(
                "tableCount",
                page.getTables().size()
        );

        metadata.put(
                "imageCount",
                page.getImageCaptions().size()
        );

        if (extraMetadata != null) {
            metadata.putAll(extraMetadata);
        }

        return new Document(
                normalize(text),
                metadata
        );
    }
    /*
     * ============================================
     * Split Large Paragraph
     * ============================================
     */

    private int splitLargeText(
            String text,
            StructuredPage page,
            int paragraph,
            int chunkIndex,
            ChunkContext context,
            List<Document> chunks,
            String blockType
    ) {

        List<String> sentences =
                splitIntoSentences(text);

        StringBuilder builder =
                new StringBuilder();

        int tokens = 0;

        for (String sentence : sentences) {

            int sentenceTokens =
                    estimateTokens(sentence);

            if (tokens + sentenceTokens > MAX_TOKENS) {

                Map<String, Object> metadata =
                        new HashMap<>();

                metadata.put("blockType", blockType);

                chunks.add(
                        createChunk(
                                builder.toString(),
                                page,
                                paragraph,
                                chunkIndex++,
                                context,
                                metadata
                        )
                );

                builder.setLength(0);

                tokens = 0;
            }

            builder.append(sentence)
                    .append(" ");

            tokens += sentenceTokens;
        }

        if (!builder.isEmpty()) {

            Map<String, Object> metadata =
                    new HashMap<>();

            metadata.put("blockType", blockType);

            chunks.add(
                    createChunk(
                            builder.toString(),
                            page,
                            paragraph,
                            chunkIndex++,
                            context,
                            metadata
                    )
            );
        }

        return chunkIndex;
    }
    /*
     * ============================================
     * Semantic Sections
     * ============================================
     */

    private List<String> splitIntoSections(String text) {

        List<String> sections =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        String[] lines =
                text.split("\n");

        for (String line : lines) {

            line = normalize(line);

            if (line.isBlank())
                continue;

            if (line.startsWith("#")) {

                if (!current.isEmpty()) {

                    sections.add(
                            current.toString()
                    );

                    current.setLength(0);
                }

                sections.add(line);

                continue;
            }

            current.append(line)
                    .append(" ");
        }

        if (!current.isEmpty()) {

            sections.add(
                    current.toString()
            );
        }

        return sections;
    }
    /*
     * ============================================
     * Sentence Splitter
     * ============================================
     */

    private List<String> splitIntoSentences(
            String text
    ) {

        BreakIterator iterator =
                BreakIterator.getSentenceInstance(Locale.US);

        iterator.setText(text);

        List<String> sentences =
                new ArrayList<>();

        int start =
                iterator.first();

        for (

                int end = iterator.next();

                end != BreakIterator.DONE;

                start = end,

                        end = iterator.next()

        ) {

            String sentence =
                    text.substring(start, end)
                            .trim();

            if (!sentence.isBlank()) {

                sentences.add(sentence);
            }

        }

        if (sentences.isEmpty()) {

            sentences.add(text);
        }

        return sentences;
    }
    /*
     * ============================================
     * Preserve Overlap
     * ============================================
     */

    private void preserveOverlap(
            ChunkContext context
    ) {

        String[] words =
                context.builder.toString()
                        .split("\\s+");

        if (words.length <= OVERLAP_TOKENS)
            return;

        StringBuilder overlap =
                new StringBuilder();

        for (

                int i = words.length - OVERLAP_TOKENS;

                i < words.length;

                i++

        ) {

            overlap.append(words[i])
                    .append(" ");
        }

        context.builder.setLength(0);

        context.builder.append(overlap);

        context.currentTokens =
                estimateTokens(
                        overlap.toString()
                );
    }
    /*
     * ============================================
     * Remove Duplicate Chunks
     * ============================================
     */

    private List<Document> removeDuplicates(
            List<Document> chunks
    ) {

        List<Document> result =
                new ArrayList<>();

        Set<String> fingerprints =
                new HashSet<>();

        for (Document doc : chunks) {

            String hash =
                    fingerprint(doc.getText());

            if (fingerprints.add(hash)) {

                result.add(doc);
            }

        }

        return result;
    }
    /*
     * ============================================
     * Token Estimation
     * ============================================
     */

    private int estimateTokens(String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        // Approximation:
        // 1 token ≈ 0.75 words
        return Math.max(
                1,
                (int) Math.ceil(text.split("\\s+").length * 0.75)
        );
    }
    /*
     * ============================================
     * Normalize Text
     * ============================================
     */

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
    /*
     * ============================================
     * Fingerprint
     * ============================================
     */

    private String fingerprint(String text) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    md.digest(
                            normalize(text).getBytes()
                    );

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : hash) {

                sb.append(
                        String.format("%02x", b)
                );
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {

            return normalize(text);
        }
    }
    /*
     * ============================================
     * Chunk Context
     * ============================================
     */

    private static class ChunkContext {

        private final StringBuilder builder =
                new StringBuilder();

        private int currentTokens = 0;

        private int currentParagraph = 1;

        private String currentHeading = "";

        void reset(StructuredPage page) {

            builder.setLength(0);

            currentTokens = 0;

            currentParagraph = 1;

            currentHeading = "";
        }
    }
    private boolean looksLikeFlattenedTable(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String[] lines = text.split("\\R");

        // Multiple lines usually indicate structured/tabular content
        if (lines.length >= 3) {
            int structuredLines = 0;
            List<String> valuesFor1D = new ArrayList<>();

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isBlank()) {
                    continue;
                }
                valuesFor1D.add(trimmed);
                String[] values = trimmed.split("\\s{2,}|\\t");
                if (values.length >= 2) {
                    structuredLines++;
                }
            }

            if (structuredLines >= 2) {
                return true;
            }
            
            // Check for 1D flattened table using detectColumnCount logic
            int maxCols = Math.min(10, valuesFor1D.size());
            for (int i = 1; i < maxCols; i++) {
                if (valuesFor1D.get(i).matches("[A-Za-z]+-\\d+")) {
                    return true; // Found a data row indicator
                }
            }
        }

        return false;
    }

    private int processFlattenedTable(
            String text,
            StructuredPage page,
            int paragraph,
            int chunkIndex,
            ChunkContext context,
            List<Document> chunks
    ) {

        flushChunk(
                context,
                page,
                chunkIndex,
                chunks
        );

        chunkIndex++;

        if (text == null || text.isBlank()) {
            return chunkIndex;
        }

        String[] lines = text.split("\\R");
        List<List<String>> rows = new ArrayList<>();
        
        List<String> valuesFor1D = new ArrayList<>();
        int structuredLines = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;
            valuesFor1D.add(trimmed);
            if (trimmed.split("\\s{2,}|\\t").length >= 2) {
                structuredLines++;
            }
        }

        if (structuredLines >= 2) {
            // 2D case
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isBlank()) continue;
                List<String> values = Arrays.stream(trimmed.split("\\s{2,}|\\t"))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList();
                if (values.size() >= 2) {
                    rows.add(values);
                }
            }
        } else {
            // 1D case
            int columnCount = detectColumnCount(valuesFor1D);
            if (columnCount < 1) columnCount = 1;
            
            for (int i = 0; i < valuesFor1D.size(); i += columnCount) {
                List<String> row = new ArrayList<>();
                for (int j = 0; j < columnCount; j++) {
                    if (i + j < valuesFor1D.size()) {
                        row.add(valuesFor1D.get(i + j));
                    }
                }
                rows.add(row);
            }
        }

        // Not enough structured rows → treat as paragraph
        if (rows.size() < 2) {
            return processParagraph(
                    text,
                    page,
                    paragraph,
                    chunkIndex,
                    context,
                    chunks
            );
        }

        List<String> headers = rows.get(0);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("blockType", "TABLE");
        metadata.put("detectedFromParagraph", true);

        StringBuilder tableChunk = new StringBuilder();
        tableChunk.append("Table:\n");
        int currentTableTokens = estimateTokens("Table:\n");

        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            StringBuilder rowText = new StringBuilder();
            
            for (int i = 0; i < row.size(); i++) {
                String columnName;
                if (i < headers.size()) {
                    columnName = headers.get(i);
                } else {
                    columnName = "Column " + (i + 1);
                }
                rowText.append(columnName).append(": ").append(row.get(i)).append(" | ");
            }
            rowText.append("\n");
            
            int rowTokens = estimateTokens(rowText.toString());

            if (currentTableTokens + rowTokens > MAX_TOKENS && currentTableTokens > estimateTokens("Table:\n")) {
                chunks.add(
                        createChunk(
                                tableChunk.toString(),
                                page,
                                paragraph,
                                chunkIndex++,
                                context,
                                metadata
                        )
                );
                tableChunk = new StringBuilder();
                tableChunk.append("Table:\n");
                currentTableTokens = estimateTokens("Table:\n");
            }

            tableChunk.append(rowText);
            currentTableTokens += rowTokens;
        }

        if (currentTableTokens > estimateTokens("Table:\n")) {
            chunks.add(
                    createChunk(
                            tableChunk.toString(),
                            page,
                            paragraph,
                            chunkIndex++,
                            context,
                            metadata
                    )
            );
        }

        return chunkIndex;
    }

    private int detectColumnCount(
            List<String> values
    ) {

        /*
         * For your current tables:
         *
         * Level
         * Pay Scale
         * Annual Ceiling
         *
         * = 3 columns
         *
         * This method detects where headers end and
         * actual row values begin.
         */

        int maxColumns =
                Math.min(10, values.size());

        for (int i = 1;
             i < maxColumns;
             i++) {

            String value =
                    values.get(i);

            /*
             * Table data usually starts when we encounter
             * a value that looks like:
             *
             * E-1
             * A
             * 1000
             * Employee 1
             *
             * For your PDFs, E-1 / E-2 etc.
             * is the first data row.
             */
            if (value.matches("[A-Za-z]+-\\d+")) {
                return i;
            }
        }

        /*
         * Fallback.
         */
        return Math.min(3, values.size());
    }

    private String getTableText(DocumentBlock block) {

        if (block == null) {
            return "";
        }

        String markdown = block.getMarkdown();

        if (markdown != null && !markdown.isBlank()) {
            return normalize(markdown);
        }

        String text = block.getText();

        if (text != null && !text.isBlank()) {
            return normalize(text);
        }

        return "";
    }

    private List<String> extractRows(String tableText) {

        List<String> rows = new ArrayList<>();

        if (tableText == null || tableText.isBlank()) {
            return rows;
        }

        String[] lines = tableText.split("\\R");

        for (String line : lines) {

            String cleaned = line.trim();

            if (cleaned.isBlank()) {
                continue;
            }

            rows.add(cleaned);
        }

        return rows;
    }

    private String buildTableRowChunk(
            ChunkContext context,
            String header,
            String row
    ) {

        StringBuilder builder = new StringBuilder();

        // Preserve the section/heading context
        if (context.currentHeading != null &&
                !context.currentHeading.isBlank()) {

            builder.append("Section: ")
                    .append(context.currentHeading)
                    .append("\n");
        }

        // Preserve table column meaning
        if (header != null && !header.isBlank()) {

            builder.append("Table Header: ")
                    .append(header)
                    .append("\n");
        }

        // Preserve the complete row
        builder.append("Table Row: ")
                .append(row);

        return builder.toString();
    }
}

