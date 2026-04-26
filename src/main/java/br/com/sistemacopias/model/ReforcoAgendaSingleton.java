package br.com.sistemacopias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Uma linha com o documento da agenda semanal serializado (coluna {@code json_payload}).
 */
@Entity
@Table(name = "reforco_agenda_singleton")
public class ReforcoAgendaSingleton {

    public static final String SINGLETON_ID = "singleton";

    @Id
    @Column(length = 36)
    private String id = SINGLETON_ID;

    @Lob
    @Column(name = "json_payload", nullable = false, columnDefinition = "LONGTEXT")
    private String jsonPayload;

    public ReforcoAgendaSingleton() {
    }

    public ReforcoAgendaSingleton(String id, String jsonPayload) {
        this.id = id;
        this.jsonPayload = jsonPayload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public void setJsonPayload(String jsonPayload) {
        this.jsonPayload = jsonPayload;
    }
}
