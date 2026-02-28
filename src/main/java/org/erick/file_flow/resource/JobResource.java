package org.erick.file_flow.resource;

import org.erick.file_flow.JobService;
import org.erick.file_flow.domain.Job;
import org.erick.file_flow.domain.JobDocument;
import org.erick.file_flow.dto.DocumentRequest;
import org.erick.file_flow.dto.DocumentResponse;
import org.erick.file_flow.dto.ErrorResponse;
import org.erick.file_flow.dto.JobRequest;
import org.erick.file_flow.dto.JobResponse;
import org.erick.file_flow.dto.ListOfDocuments;
import org.erick.file_flow.dto.URLResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs", description = "Endpoints para gerenciamento de jobs e documentos")
public class JobResource { 

    @Autowired
    private JobService jobService;

    @Value("${app.url.expiration-seconds:3600}")
    private Integer URLexpiresInSeconds;
    
    @PostMapping("/create")
    @Operation(
        summary = "Criar job",
        description = "Cria um novo job com a quantidade total de documentos informada.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Job criado com sucesso",
            content = @Content(schema = @Schema(implementation = JobResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Nao autenticado"
        )
    })
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        Job job = jobService.createJob(request.totalDocuments());
        JobResponse response = new JobResponse(
            job.getId(), 
            job.getStatus().getDescription(),  
            job.getDocuments().size(),
            job.getDocumentsCreated(),
            job.getDoneDocuments(),
            job.getFailedDocuments()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/documents/create")
    @Operation(
        summary = "Criar documento no job",
        description = "Cria um novo documento vinculado a um job existente."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Documento criado com sucesso",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Regra de negocio violada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<DocumentResponse> createDocument(
            @Parameter(description = "Identificador do job", example = "1") @PathVariable Long jobId,
            @RequestBody DocumentRequest documentRequest) {
        JobDocument document = jobService.createDocumentIntoJob(
            documentRequest.originalFilename(),
            documentRequest.contentType(),
            jobId
        );
        DocumentResponse response = new DocumentResponse(
            jobId,
            document.getId(),
            document.getStatus().name(),
            null,
            null,
            document.getRawKey(),
            document.getResultKey(),
            null,
            document.getSizeBytes(),
            document.geteTag()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/documents/{documentId}/upload-url")
    @Operation(
        summary = "Gerar URL de upload",
        description = "Gera uma URL temporaria para upload do documento."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "URL gerada com sucesso",
            content = @Content(schema = @Schema(implementation = URLResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Regra de negocio violada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<URLResponse> getGeneratedURL(
            @Parameter(description = "Identificador do job", example = "1") @PathVariable Long jobId,
            @Parameter(description = "Identificador do documento", example = "10") @PathVariable Long documentId) {
        String url = jobService.getGeneratedURL(jobId, documentId);
        URLResponse response = new URLResponse(jobId, documentId, url, URLexpiresInSeconds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    @Operation(
        summary = "Consultar job",
        description = "Retorna os dados de um job pelo identificador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Job encontrado",
            content = @Content(schema = @Schema(implementation = JobResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Regra de negocio violada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<JobResponse> getJob(
            @Parameter(description = "Identificador do job", example = "1") @PathVariable Long jobId) {
        Job job = jobService.findById(jobId);
        return ResponseEntity.ok(
            new JobResponse(
                job.getId(),
                job.getStatus().getDescription(), 
                job.getTotalDocuments(), 
                job.getDocumentsCreated(), 
                job.getDoneDocuments(), 
                job.getFailedDocuments()
            )
        );
    }

    @GetMapping("/{jobId}/documents")
    @Operation(
        summary = "Listar documentos do job",
        description = "Retorna todos os documentos associados a um job."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Documentos encontrados",
            content = @Content(schema = @Schema(implementation = ListOfDocuments.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Regra de negocio violada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ListOfDocuments> getDocumentsByJobId(
            @Parameter(description = "Identificador do job", example = "1") @PathVariable Long jobId) {
        Job job = jobService.findById(jobId);
        ListOfDocuments response = new ListOfDocuments(jobId, job.getDocuments().stream()
        .map(document -> new DocumentResponse(
            jobId,
            document.getId(),
            document.getStatus().name(),
            document.getOriginalFilename(),
            document.getContentType(),
            document.getRawKey(),
            document.getResultKey(),
            document.getErrorMessage(),
            document.getSizeBytes(),
            document.geteTag()
        )).toList()            
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}/documents/{documentId}")
    @Operation(
        summary = "Consultar documento",
        description = "Retorna os dados de um documento especifico dentro de um job."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Documento encontrado",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Regra de negocio violada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<DocumentResponse> getDocumentById(
            @Parameter(description = "Identificador do job", example = "1") @PathVariable Long jobId,
            @Parameter(description = "Identificador do documento", example = "10") @PathVariable Long documentId) {
        Job job = jobService.findById(jobId);
        JobDocument document = job.getDocuments().stream()
            .filter(doc -> doc.getId().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        DocumentResponse response = new DocumentResponse(
            jobId,
            document.getId(),
            document.getStatus().name(),
            document.getOriginalFilename(),
            document.getContentType(),
            document.getRawKey(),
            document.getResultKey(),
            document.getErrorMessage(),
            document.getSizeBytes(),
            document.geteTag()
        );
        return ResponseEntity.ok(response);
    }

}
