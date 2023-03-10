package ir.darkdeveloper.bitkip.models;

import lombok.*;

import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueModel {
    private int id;
    private String name;
    private boolean editable;
    private boolean canAddDownload;


    public QueueModel(String name, boolean editable, boolean canAddDownload) {
        this.name = name;
        this.editable = editable;
        this.canAddDownload = canAddDownload;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (QueueModel) o;
        return id == that.id && editable == that.editable &&
                canAddDownload == that.canAddDownload && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, editable, canAddDownload);
    }
}
