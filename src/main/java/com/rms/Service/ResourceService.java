package com.rms.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rms.Repository.ResourceRepository;
import com.rms.dto.ResourceRequest;
import com.rms.dto.ResourceResponse;
import com.rms.entity.Resource;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourceResponse createResource(ResourceRequest request) {

        Resource resource = new Resource();

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setAvailable(request.getAvailable());
        resource.setPrice(request.getPrice());

        Resource savedResource =
                resourceRepository.save(resource);

        return convertToResponse(savedResource);
    }

    public List<ResourceResponse> getAllResources() {

        return resourceRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ResourceResponse getResourceById(Long id) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource not found with id: " + id
                                )
                        );

        return convertToResponse(resource);
    }

    public ResourceResponse updateResource(
            Long id,
            ResourceRequest request) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource not found with id: " + id
                                )
                        );

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setAvailable(request.getAvailable());
        resource.setPrice(request.getPrice());

        Resource updatedResource =
                resourceRepository.save(resource);

        return convertToResponse(updatedResource);
    }

    public void deleteResource(Long id) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource not found with id: " + id
                                )
                        );

        resourceRepository.delete(resource);
    }

    private ResourceResponse convertToResponse(
            Resource resource) {

        ResourceResponse response =
                new ResourceResponse();

        response.setId(resource.getId());
        response.setName(resource.getName());
        response.setDescription(resource.getDescription());
        response.setType(resource.getType());
        response.setAvailable(resource.getAvailable());
        response.setPrice(resource.getPrice());

        return response;
    }
}