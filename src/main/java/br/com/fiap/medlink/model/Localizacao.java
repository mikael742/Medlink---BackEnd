package br.com.fiap.medlink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class Localizacao {

    private static final double RAIO_TERRA_KM = 6371.0;

    private String cep;
    private Double latitude;
    private Double longitude;

    public Localizacao() {
    }

    public Localizacao(String cep, Double latitude, Double longitude) {
        this.cep = cep;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @JsonIgnore
    public boolean temCoordenadas() {
        return latitude != null && longitude != null;
    }

    /** Distância em linha reta (fórmula de Haversine), em km. */
    public double distanciaKmAte(Localizacao outra) {
        if (!this.temCoordenadas() || outra == null || !outra.temCoordenadas()) {
            throw new IllegalStateException("Localização sem coordenadas");
        }
        double dLat = Math.toRadians(outra.latitude - this.latitude);
        double dLon = Math.toRadians(outra.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(outra.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAIO_TERRA_KM * c;
    }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
