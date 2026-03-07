package com.example.vt.web.repo;

import com.example.vt.web.entity.Vet;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface VetRepo extends CrudRepository<Vet, UUID> {
    Vet findByFirstName(String firstName);
}
