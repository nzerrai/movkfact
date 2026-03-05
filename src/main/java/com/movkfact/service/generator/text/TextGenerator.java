package com.movkfact.service.generator.text;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.Map;
import java.util.Random;

/**
 * Générateur de texte aléatoire (style lorem ipsum).
 * Supporte contrainte maxLength (défaut: 50).
 */
public class TextGenerator extends DataTypeGenerator {
    private static final Random random = new Random();
    private static final String[] WORDS = {
        "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
        "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "dolore",
        "magna", "aliqua", "enim", "minim", "veniam", "quis", "nostrud", "exercitation",
        "ullamco", "laboris", "nisi", "aliquip", "commodo", "consequat", "duis", "aute",
        "irure", "reprehenderit", "voluptate", "velit", "esse", "cillum", "fugiat", "nulla"
    };

    public TextGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        int maxLength = 50;
        Map<String, Object> constraints = columnConfig.getConstraints();
        if (constraints != null && constraints.get("maxLength") != null) {
            maxLength = ((Number) constraints.get("maxLength")).intValue();
        }

        StringBuilder sb = new StringBuilder();
        while (sb.length() < maxLength - 10) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(WORDS[random.nextInt(WORDS.length)]);
        }
        String result = sb.toString().trim();
        return result.length() > maxLength ? result.substring(0, maxLength) : result;
    }
}
