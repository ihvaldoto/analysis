package ru.student.dataanalysis.model;

import java.time.LocalDateTime;

public class DataPoint implements Identifiable<Long> {
    private Long id;
    private Long dataSetId;
    private String metric;
    private double value;
    private String unit;
    private LocalDateTime recordedAt;

    public DataPoint() {
    }

    public DataPoint(Long id, Long dataSetId, String metric, double value, String unit, LocalDateTime recordedAt) {
        this.id = id;
        this.dataSetId = dataSetId;
        this.metric = metric;
        this.value = value;
        this.unit = unit;
        this.recordedAt = recordedAt;
    }

    @Override
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDataSetId() { return dataSetId; }
    public void setDataSetId(Long dataSetId) { this.dataSetId = dataSetId; }
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    @Override
    public String toString() {
        return "DataPoint{id=" + id + ", dataSetId=" + dataSetId + ", metric='" + metric
                + "', value=" + value + " " + unit + ", recordedAt=" + recordedAt + "}";
    }
}
