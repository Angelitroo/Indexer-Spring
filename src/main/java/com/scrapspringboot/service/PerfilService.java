package com.scrapspringboot.service;

import com.scrapspringboot.dto.*;
import com.scrapspringboot.exception.RecursoNoEncontrado;
import com.scrapspringboot.model.Perfil;
import com.scrapspringboot.model.Usuario;
import com.scrapspringboot.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class PerfilService {
    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<PerfilDTO> getAll() {
        List<Perfil> perfiles = perfilRepository.findAll();

        if (perfiles.isEmpty()) {
            throw new RecursoNoEncontrado("No se encontraron perfiles");
        }

        return perfiles.stream()
                .map(perfil -> {
                    PerfilDTO dto = new PerfilDTO();
                    dto.setId(perfil.getId());
                    dto.setNombre(perfil.getNombre());
                    dto.setImagen(perfil.getImagen());
                    dto.setBaneado(perfil.isBaneado());
                    dto.setUsuario(perfil.getUsuario().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public PerfilDTO getById(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("Perfil con id " + id + " no encontrado"));

        PerfilDTO dto = new PerfilDTO();
        dto.setId(perfil.getId());
        dto.setNombre(perfil.getNombre());
        dto.setImagen(perfil.getImagen());
        dto.setBaneado(perfil.isBaneado());
        dto.setUsuario(perfil.getUsuario().getId());
        return dto;
    }

    public List<PerfilDTO> getByNombre(String nombre) {
        String patron = nombre + "%";

        List<Perfil> perfiles = perfilRepository.findByNombreLikeIgnoreCase(patron);

        if (perfiles == null || perfiles.isEmpty()) {
            throw new RecursoNoEncontrado("No se encontraron perfiles con el nombre: " + nombre);
        }

        return perfiles.stream().map(perfil -> {
            PerfilDTO dto = new PerfilDTO();
            dto.setId(perfil.getId());
            dto.setNombre(perfil.getNombre());
            dto.setImagen(perfil.getImagen());
            dto.setBaneado(perfil.isBaneado());
            return dto;
        }).collect(Collectors.toList());
    }

    public PerfilDTO guardar(PerfilDTO perfilDTO, Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontrado("Usuario con id " + idUsuario + " no encontrado"));

        Perfil perfil = new Perfil();
        perfil.setId(perfilDTO.getId());
        perfil.setNombre(perfilDTO.getNombre());
        perfil.setImagen(perfilDTO.getImagen());
        perfil.setBaneado(perfilDTO.getBaneado());
        perfil.setUsuario(usuario);

        Perfil perfilGuardado = perfilRepository.save(perfil);

        PerfilDTO dto = new PerfilDTO();
        dto.setId(perfilGuardado.getId());
        dto.setNombre(perfilGuardado.getNombre());
        dto.setImagen(perfilGuardado.getImagen());
        dto.setBaneado(perfilGuardado.isBaneado());
        dto.setUsuario(perfilGuardado.getUsuario().getId());

        return dto;
    }

    @Transactional
    public String eliminar(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("Perfil con id " + id + " no encontrado"));

        Usuario usuario = perfil.getUsuario();

        if (!perfilRepository.existsById(id)) {
            throw new RecursoNoEncontrado("Perfil con id " + id + " no encontrado");
        }

        perfilRepository.delete(perfil);
        usuarioRepository.delete(usuario);

        return "Eliminado correctamente";
    }



    public PerfilActualizarDTO actualizar(PerfilActualizarDTO perfilActualizarDTO, Long idPerfil) {
        Perfil perfil = perfilRepository.findById(idPerfil)
                .orElseThrow(() -> new RecursoNoEncontrado("Perfil con id " + idPerfil + " no encontrado"));

        perfil.setNombre(perfilActualizarDTO.getNombre());
        perfil.setImagen(perfilActualizarDTO.getImagen());

        if (perfilActualizarDTO.getUsername() != null && !perfilActualizarDTO.getUsername().isEmpty()) {
            perfil.getUsuario().setUsername(perfilActualizarDTO.getUsername());
        }

        if (perfilActualizarDTO.getPassword() != null && !perfilActualizarDTO.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(perfilActualizarDTO.getPassword());
            perfil.getUsuario().setPassword(encodedPassword);
            perfilActualizarDTO.setPassword(encodedPassword);
        }

        Perfil perfilActualizado = perfilRepository.save(perfil);

        PerfilActualizarDTO dto = new PerfilActualizarDTO();
        dto.setId(perfilActualizado.getId());
        dto.setNombre(perfilActualizado.getNombre());
        dto.setImagen(perfilActualizado.getImagen());
        dto.setEmail(perfilActualizado.getUsuario().getEmail());
        dto.setUsername(perfilActualizado.getUsuario().getUsername());
        dto.setPassword(perfilActualizarDTO.getPassword());

        return dto;
    }



    public PerfilActualizarDTO getActualizadoById(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("Perfil con id " + id + " no encontrado"));

        PerfilActualizarDTO dto = new PerfilActualizarDTO();
        dto.setId(perfil.getId());
        dto.setNombre(perfil.getNombre());
        dto.setImagen(perfil.getImagen());
        dto.setUsername(perfil.getUsuario().getUsername());
        dto.setPassword(perfil.getUsuario().getPassword());
        return dto;
    }
}