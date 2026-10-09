package ru.student.dataanalysis.service;

import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;

@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {
    private final DataPointService dataPointService;

    public DataAnalysisServiceImpl(DataPointService dataPointService) {
        this.dataPointService = dataPointService;
    }

    @Override
    public DoubleSummaryStatistics analyze(Long dataSetId, String metric) {
        List<Double> values = dataPointService.findByDataSet(dataSetId).stream()
                .filter(point -> point.getMetric().equalsIgnoreCase(metric))
                .map(point -> point.getValue())
                .toList();
        if (values.isEmpty()) throw new IllegalArgumentException("No values found for metric " + metric);
        return values.stream().mapToDouble(Double::doubleValue).summaryStatistics();
    }

    @Override
    public String format(Long dataSetId, String metric) {
        DoubleSummaryStatistics stats = analyze(dataSetId, metric);
        return String.format(Locale.ROOT,
                "metric=%s, count=%d, sum=%.3f, average=%.3f, min=%.3f, max=%.3f",
                metric, stats.getCount(), stats.getSum(), stats.getAverage(), stats.getMin(), stats.getMax());
    }
}
