package ru.student.dataanalysis.service;

import java.util.DoubleSummaryStatistics;

public interface DataAnalysisService {
    DoubleSummaryStatistics analyze(Long dataSetId, String metric);

    String format(Long dataSetId, String metric);
}
