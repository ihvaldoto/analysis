package ru.student.dataanalysis.model;

public class DataSet implements Identifiable<Long> {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;

    public DataSet() {
    }

    public DataSet(Long id, String name, String description, Long ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
    }

    @Override
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    @Override
    public String toString() {
        return "DataSet{id=" + id + ", name='" + name + "', description='" + description
                + "', ownerId=" + ownerId + "}";
    }
}
