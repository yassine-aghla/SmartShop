package com.smartshop.services;

import com.smartshop.dtos.ClientDTO;
import com.smartshop.entity.Client;
import com.smartshop.entity.CustomerTier;
import com.smartshop.entity.User;
import com.smartshop.exceptions.ClientNotFoundException;
import com.smartshop.exceptions.ClientEmailAlreadyExistsException;
import com.smartshop.exceptions.ClientBusinessException;
import com.smartshop.exceptions.ClientUserNotFoundException;
import com.smartshop.mappers.ClientMapper;
import com.smartshop.repositories.ClientRepository;
import com.smartshop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;


    @Transactional
    public ClientDTO createClient(ClientDTO clientDTO, Long userId) {
        log.info("Création d'un nouveau client avec l'email: {} et userId: {}", clientDTO.getEmail(), userId);
        validateClientData(clientDTO);
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            log.warn("Tentative de création avec un email qui existe déjà: {}", clientDTO.getEmail());
            throw new ClientEmailAlreadyExistsException(
                    "Un client avec l'email '" + clientDTO.getEmail() + "' existe déjà"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User non trouvé avec l'ID: {}", userId);
                    return new ClientUserNotFoundException("User non trouvé avec l'ID: " + userId);
                });

        if (clientRepository.existsByUserId(userId)) {
            log.warn("Un client existe déjà pour le User avec l'ID: {}", userId);
            throw new ClientBusinessException("Un client existe déjà pour cet utilisateur");
        }

        Client client = clientMapper.toEntity(clientDTO);
        client.setUser(user);
        client.setCustomerTier(CustomerTier.BASIC);
        client.setTotalOrders(0);
        client.setTotalSpent(BigDecimal.ZERO);
        client.setIsActive(client.getIsActive());

        Client savedClient = clientRepository.save(client);
        log.info("Client créé avec succès: ID={}, email={}", savedClient.getId(), savedClient.getEmail());

        return clientMapper.toDTO(savedClient);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long id) {
        log.info("Récupération du client avec l'ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Client non trouvé avec l'ID: {}", id);
                    return new ClientNotFoundException("Client non trouvé avec l'ID: " + id);
                });

        return clientMapper.toDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientByEmail(String email) {
        log.info("Récupération du client avec l'email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Client non trouvé avec l'email: {}", email);
                    return new ClientNotFoundException("Client non trouvé avec l'email: " + email);
                });

        return clientMapper.toDTO(client);
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        log.info("Récupération de tous les clients");

        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ClientDTO> getAllActiveClients() {
        log.info("Récupération de tous les clients actifs");

        return clientRepository.findByIsActiveTrue()
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> getActiveClientsPaginated(Pageable pageable) {
        log.info("Récupération des clients actifs avec pagination");

        return clientRepository.findByIsActiveTrue(pageable)
                .map(clientMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> getClientsByTier(CustomerTier tier) {
        log.info("Récupération des clients avec le tier: {}", tier);

        return clientRepository.findByCustomerTier(tier)
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> getClientsByTierPaginated(CustomerTier tier, Pageable pageable) {
        log.info("Récupération des clients avec le tier: {} (paginer)", tier);

        return clientRepository.findByCustomerTier(tier, pageable)
                .map(clientMapper::toDTO);
    }

    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        log.info("Mise à jour du client avec l'ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Client non trouvé pour la mise à jour: ID={}", id);
                    return new ClientNotFoundException("Client non trouvé avec l'ID: " + id);
                });

        if (!client.getEmail().equals(clientDTO.getEmail()) &&
                clientRepository.existsByEmail(clientDTO.getEmail())) {
            log.warn("Tentative de mise à jour avec un email qui existe déjà: {}", clientDTO.getEmail());
            throw new ClientEmailAlreadyExistsException(
                    "Un client avec l'email '" + clientDTO.getEmail() + "' existe déjà"
            );
        }


        client.setNom(clientDTO.getNom());
        client.setEmail(clientDTO.getEmail());
        client.setIsActive(clientDTO.getIsActive());

        Client updatedClient = clientRepository.save(client);
        log.info("Client mis à jour avec succès: ID={}", updatedClient.getId());

        return clientMapper.toDTO(updatedClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        log.info("Suppression du client avec l'ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Client non trouvé pour la suppression: ID={}", id);
                    return new ClientNotFoundException("Client non trouvé avec l'ID: " + id);
                });

        client.setIsActive(false);
        clientRepository.save(client);

        log.info("Client supprimé (soft delete) avec succès: ID={}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return clientRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientStatistics(Long clientId) {
        return getClientById(clientId);
    }


    @Transactional(readOnly = true)
    public long countActiveClients() {
        return clientRepository.countByIsActiveTrue();
    }


    @Transactional(readOnly = true)
    public long countClientsByTier(CustomerTier tier) {
        return clientRepository.countByCustomerTier(tier);
    }


    public void validateClientData(ClientDTO clientDTO) {
        log.debug("Validation des données du client: {}", clientDTO.getNom());


        if (clientDTO.getNom() == null || clientDTO.getNom().trim().isEmpty()) {
            throw new ClientBusinessException("Le nom du client ne peut pas être vide");
        }

        if (clientDTO.getNom().length() < 3 || clientDTO.getNom().length() > 150) {
            throw new ClientBusinessException("Le nom doit contenir entre 3 et 150 caractères");
        }


        if (clientDTO.getEmail() == null || clientDTO.getEmail().trim().isEmpty()) {
            throw new ClientBusinessException("L'email ne peut pas être vide");
        }

        if (!clientDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ClientBusinessException("L'email n'est pas valide");
        }


        if (clientDTO.getCustomerTier() == null) {
            throw new ClientBusinessException("Le tier client ne peut pas être null");
        }

        log.debug("Validation des données du client réussie");
    }
}
