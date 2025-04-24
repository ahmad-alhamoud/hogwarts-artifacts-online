package com.ahmad.hogwartsartifactsonline.artifact;

import com.ahmad.hogwartsartifactsonline.artifact.converter.ArtifactDtoToArtifactConverter;
import com.ahmad.hogwartsartifactsonline.artifact.converter.ArtifactToArtifactDtoConverter;
import com.ahmad.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import com.ahmad.hogwartsartifactsonline.client.imagestorage.ImageStorageClient;
import com.ahmad.hogwartsartifactsonline.system.Result;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;
    private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;
    private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;
    private final ImageStorageClient imageStorageClient;

    public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConverter artifactDtoConverter, ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter, ImageStorageClient imageStorageClient) {
        this.artifactService = artifactService;
        this.artifactToArtifactDtoConverter = artifactDtoConverter;
        this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
        this.imageStorageClient = imageStorageClient;
    }

    @GetMapping("/{artifactId}")
    public Result findArtifactById(@PathVariable String artifactId) {
        Artifact foundArtifact = this.artifactService.findArtifactById(artifactId);
        ArtifactDto artifactDto = artifactToArtifactDtoConverter.convert(foundArtifact);
        return new Result(true, StatusCode.SUCCESS, "Find One Success", artifactDto);
    }


    @GetMapping
    public Result findAllArtifacts(Pageable pageable) {
        Page<Artifact> artifactPage = artifactService.findAll(pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage
                .map(artifactToArtifactDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find All Success", artifactDtoPage);
    }

    @PostMapping
    public Result addArtifact(@RequestBody @Valid ArtifactDto artifactDto) {
        Artifact newArtifact = artifactDtoToArtifactConverter.convert(artifactDto);
        Artifact savedArtifact = artifactService.save(newArtifact);
        ArtifactDto savedArtifactDto = artifactToArtifactDtoConverter.convert(savedArtifact);
        return new Result(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
    }

    @PutMapping("/{artifactId}")
    public Result updateArtifact(@PathVariable String artifactId, @RequestBody @Valid ArtifactDto artifactDto) {
        Artifact updatedArtifact = artifactService.update(artifactId, artifactDtoToArtifactConverter.convert(artifactDto));

        ArtifactDto newArtifactDto = artifactToArtifactDtoConverter.convert(updatedArtifact);

        return new Result(true, StatusCode.SUCCESS, "Update Artifact Success", newArtifactDto);
    }

    @DeleteMapping("/{artifactId}")
    public Result deleteArtifact(@PathVariable String artifactId) {
        artifactService.delete(artifactId);
        return new Result(true, StatusCode.SUCCESS, "Delete Artifact Success");
    }

    @GetMapping("/summary")
    public Result summarizeArtifacts() throws JsonProcessingException {
        List<Artifact> foundArtifacts = artifactService.findAll();
        List<ArtifactDto> artifactDtos = foundArtifacts.stream()
                .map(artifactToArtifactDtoConverter::convert).collect(Collectors.toList());

        String artifactSummary = artifactService.summarize(artifactDtos);
        return new Result(true, StatusCode.SUCCESS, "Summarize Success", artifactSummary);
    }

    @PostMapping("/search")
    public Result findArtifactsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Artifact> artifactPage = artifactService.findByCriteria(searchCriteria, pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage
                .map(artifactToArtifactDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Search Success", artifactDtoPage);
    }

    @PostMapping("/images")
    public Result uploadImage(@RequestParam String containerName, @RequestParam MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()){
            String imageUrl = imageStorageClient.uploadImage(containerName, file.getOriginalFilename(), inputStream, file.getSize());
            return new Result(true, StatusCode.SUCCESS, "Upload Image Success", imageUrl);
        }
    }

}
