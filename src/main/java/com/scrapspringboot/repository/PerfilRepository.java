package com.scrapspringboot.repository;

import com.scrapspringboot.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Perfil findByUsuarioId(Long usuarioId);

    List<Perfil> findByNombreLikeIgnoreCase(String nombre);



}