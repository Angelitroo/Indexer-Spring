package com.scrapspringboot.dto;

import lombok.Data;

@Data
public class PerfilDTO {
    private Long id;
    private String nombre;
    private String imagen;
    private Boolean activado;
    private Boolean baneado;
    private Long Usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean getActivado() {
        return activado;
    }

    public void setActivado(Boolean activado) {
        this.activado = activado;
    }

    public Boolean getBaneado() {
        return baneado;
    }

    public void setBaneado(Boolean baneado) {
        this.baneado = baneado;
    }

    public Long getUsuario() {
        return Usuario;
    }

    public void setUsuario(Long usuario) {
        Usuario = usuario;
    }
}