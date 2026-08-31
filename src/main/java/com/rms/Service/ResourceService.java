
package com.rms.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.Exception.ResourceNotFoundException;
import com.rms.Repository.ResourceRepository;
import com.rms.dto.ResourceRequest;
import com.rms.entity.Resource;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(
            ResourceRepository resourceRepository) {

        this.resourceRepository =
                resourceRepository;
    }

    // CREATE
    @Transactional
    public Resource createResource(
            ResourceRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Resource request is required");
        }

        Resource resource =
                new Resource();

        resource.setName(
                request.getName());

        resource.setDescription(
                request.getDescription());

        resource.setAvailable(
                request.getAvailable());

        return resourceRepository.save(
                resource);
    }

    // GET BY ID
    public Resource getResourceById(
            Long id) {

        return resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));
    }

    // GET ALL
    public List<Resource> getAllResources() {

        return resourceRepository.findAll();
    }

    // UPDATE
    @Transactional
    public Resource updateResource(
            Long id,
            ResourceRequest request) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found"));

        resource.setName(
                request.getName());

        resource.setDescription(
                request.getDescription());

        resource.setAvailable(
                request.getAvailable());

        return resourceRepository.save(
                resource);
    }

    // DELETE
    @Transactional
    public void deleteResource(
            Long id) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found"));

        resourceRepository.delete(
                resource);
    }
}

