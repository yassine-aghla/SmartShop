package com.smartshop.controllers;

import com.smartshop.dtos.ClientDTO;
import com.smartshop.entity.CustomerTier;
import com.smartshop.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        log.info("GET /api/clients - Récupération de tous les clients");
        List<ClientDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ClientDTO>> getAllActiveClients() {
        log.info("GET /api/clients/active - Récupération des clients actifs");
        List<ClientDTO> clients = clientService.getAllActiveClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/active/paginated")
    public ResponseEntity<Page<ClientDTO>> getActiveClientsPaginated(Pageable pageable) {
        log.info("GET /api/clients/active/paginated - Récupération des clients actifs paginiés");
        Page<ClientDTO> clients = clientService.getActiveClientsPaginated(pageable);
        return ResponseEntity.ok(clients);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        log.info("GET /api/clients/{} - Récupération du client", id);
        ClientDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientDTO> getClientByEmail(@PathVariable String email) {
        log.info("GET /api/clients/email/{} - Récupération du client", email);
        ClientDTO client = clientService.getClientByEmail(email);
        return ResponseEntity.ok(client);
    }


    @GetMapping("/tier/{tier}")
    public ResponseEntity<List<ClientDTO>> getClientsByTier(@PathVariable CustomerTier tier) {
        log.info("GET /api/clients/tier/{} - Récupération des clients par tier", tier);
        List<ClientDTO> clients = clientService.getClientsByTier(tier);
        return ResponseEntity.ok(clients);
    }


    @GetMapping("/tier/{tier}/paginated")
    public ResponseEntity<Page<ClientDTO>> getClientsByTierPaginated(
            @PathVariable CustomerTier tier,
            Pageable pageable) {
        log.info("GET /api/clients/tier/{}/paginated - Récupération des clients par tier (paginer)", tier);
        Page<ClientDTO> clients = clientService.getClientsByTierPaginated(tier, pageable);
        return ResponseEntity.ok(clients);
    }


    @GetMapping("/{id}/statistics")
    public ResponseEntity<ClientDTO> getClientStatistics(@PathVariable Long id) {
        log.info("GET /api/clients/{}/statistics - Récupération des statistiques", id);
        ClientDTO client = clientService.getClientStatistics(id);
        return ResponseEntity.ok(client);
    }


    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        log.info("GET /api/clients/exists/email/{} - Vérification de l'existence de l'email", email);
        boolean exists = clientService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }


    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveClients() {
        log.info("GET /api/clients/count/active - Comptage des clients actifs");
        long count = clientService.countActiveClients();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/count/tier/{tier}")
    public ResponseEntity<Long> countClientsByTier(@PathVariable CustomerTier tier) {
        log.info("GET /api/clients/count/tier/{} - Comptage des clients par tier", tier);
        long count = clientService.countClientsByTier(tier);
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<?> createClient(
            @Valid @RequestBody ClientDTO clientDTO,
            @RequestParam Long userId) {
        try {
            log.info("POST /api/clients - Création d'un nouveau client: {} (userId={})", clientDTO.getNom(), userId);
            ClientDTO createdClient = clientService.createClient(clientDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (Exception e) {
            HashMap<String,String>err=new HashMap<>();
            err.put("err",e.getMessage());
            return ResponseEntity.ok(err);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientDTO clientDTO) {
        log.info("PUT /api/clients/{} - Mise à jour du client", id);
        ClientDTO updatedClient = clientService.updateClient(id, clientDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.info("DELETE /api/clients/{} - Suppression du client", id);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}