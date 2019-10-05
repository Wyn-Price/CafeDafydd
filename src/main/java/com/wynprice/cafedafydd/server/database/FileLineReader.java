package com.wynprice.cafedafydd.server.database;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@RequiredArgsConstructor
public class FileLineReader implements Iterable<String> {
    private final String line;
    private int index;

    public String getNextEntry() {
        int arrayLevel = 0;
        StringBuilder out = new StringBuilder();
        while(this.hasMore()) {
            char c = this.nextChar();
            if(c == '[') {
                arrayLevel++;
            } else if(c == ']') {
                arrayLevel--;
            } else if(c == ',' && arrayLevel == 0) {
                break;
            } else {
                out.append(c);
            }
        }
        if(arrayLevel != 0) {
            throw new IllegalArgumentException("Line " + this.line + " has unbalanced brackets");
        }
        return out.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return FileLineReader.this.hasMore();
            }

            @Override
            public String next() {
                return FileLineReader.this.getNextEntry();
            }
        };
    }

    public char nextChar() {
        return this.line.charAt(this.index++);
    }

    public char peakNext() {
        return this.line.charAt(this.index);
    }

    public boolean hasMore() {
        return this.index < line.length();
    }
}
