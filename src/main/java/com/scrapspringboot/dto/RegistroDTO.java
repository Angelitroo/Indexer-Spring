package com.scrapspringboot.dto;

import com.scrapspringboot.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistroDTO {

    private String nombre;
    private String imagen;

    private Boolean activado = false;
    private Boolean baneado = false;

    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    private String email;
    private String username;
    private String password;
    private Rol rol = Rol.USER;

    public boolean isActivado() {
        return Boolean.TRUE.equals(activado);
    }

    public boolean isBaneado() {
        return Boolean.TRUE.equals(baneado);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
