
package com.rms.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rms.Exception.ResourceNotFoundException;
import com.rms.Repository.ResourceRepository;
import com.rms.entity.Resource;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    // CREATE RESOURCE
    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    // GET ALL RESOURCES
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    // GET RESOURCE BY ID
    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id));
    }

    // UPDATE RESOURCE
    public Resource updateResource(Long id, Resource resourceDetails) {

        Resource existingResource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found with id: " + id));

        existingResource.setName(resourceDetails.getName());
        existingResource.setDescription(resourceDetails.getDescription());
        existingResource.setAvailable(resourceDetails.getAvailable());

        return resourceRepository.save(existingResource);
    }

    // DELETE RESOURCE
    public void deleteResource(Long id) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found with id: " + id));

        resourceRepository.delete(resource);
    }
}

