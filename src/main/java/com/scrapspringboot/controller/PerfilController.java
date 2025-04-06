package com.scrapspringboot.controller;

import com.scrapspringboot.dto.*;
import com.scrapspringboot.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/perfiles")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;


    @GetMapping("/")
    public List<PerfilDTO> getAll() {
        return perfilService.getAll();
    }

    @GetMapping("/{id}")
    public PerfilDTO getById(@PathVariable Long id) {
        return perfilService.getById(id);
    }

    @GetMapping({"/buscar/{nombre}"})
    public ResponseEntity<List<PerfilDTO>> buscarPorNombre(@PathVariable String nombre) {
        List<PerfilDTO> perfiles = perfilService.getByNombre(nombre);

        return ResponseEntity.ok(perfiles);
    }

    @PostMapping("/guardar/{idUsuario}")
    public PerfilDTO guardar(@RequestBody PerfilDTO perfilDTO, @PathVariable Long idUsuario) {
        return perfilService.guardar(perfilDTO, idUsuario);
    }

    @GetMapping("actualizado/{id}")
    public PerfilActualizarDTO getActualizadoById(@PathVariable Long id) {
        return perfilService.getActualizadoById(id);
    }

    @PutMapping("/actualizar/{id}")
    public PerfilActualizarDTO actualizar(@RequestBody PerfilActualizarDTO perfilActualizarDTO, @PathVariable Long id) {
        return perfilService.actualizar(perfilActualizarDTO, id);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        try {
            String resultado = perfilService.eliminar(id);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}