package com.divtech.employee_management.dto;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressTracker {
    private AtomicInteger totalRows = new AtomicInteger(0);
    private AtomicInteger processedRows = new AtomicInteger(0);

    public void setTotalRows(int rows) {
        totalRows.set(rows);
    }

    public void incrementProcessedRows() {
        processedRows.incrementAndGet();
    }

    public int getProgressPercentage() {
        if (totalRows.get() == 0) return 0;
        return (processedRows.get() * 100) / totalRows.get();
    }
}
